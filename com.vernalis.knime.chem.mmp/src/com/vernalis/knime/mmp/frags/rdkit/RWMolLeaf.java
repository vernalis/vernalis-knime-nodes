/*******************************************************************************
 * Copyright (c) 2017,2021 Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *  This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.knime.mmp.frags.rdkit;

import org.RDKit.ExplicitBitVect;
import org.RDKit.GenericRDKitException;
import org.RDKit.Int_Pair;
import org.RDKit.Int_Vect;
import org.RDKit.Match_Vect;
import org.RDKit.MolSanitizeException;
import org.RDKit.RDKFuncs;
import org.RDKit.RWMol;
import org.RDKit.SmilesParseException;
import org.RDKit.UInt_Vect;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.vector.bitvector.DenseBitVector;
import org.knime.core.data.vector.bitvector.DenseBitVectorCellFactory;
import org.knime.core.node.NodeLogger;

import com.vernalis.knime.mmp.MatchedPairsMultipleCutsNodePlugin;
import com.vernalis.knime.mmp.ToolkitException;
import com.vernalis.knime.mmp.frags.abstrct.AbstractLeaf;

/**
 * This class provides a canonicalisation mechanism for single-attachment point
 * SMILES strings, and also record the index of the incoming attachment point
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class RWMolLeaf extends AbstractLeaf<RWMol> {

	/**
	 * ConstructorThe SMILES is canonicalised and the incoming attachment point
	 * index stored for reference
	 * 
	 * @param smiles
	 *            The SMILES String
	 * @throws IllegalArgumentException
	 *             If no or >1 attachment points are found
	 * @throws ToolkitException
	 *             If the toolkit implementation threw an exception
	 */
	public RWMolLeaf(String smiles)
			throws IllegalArgumentException, ToolkitException {
		super(smiles);
	}

	/**
	 * Constructor from {@link RWMol} object
	 * 
	 * @param comp
	 *            The component
	 * @throws IllegalArgumentException
	 *             if no or &gt;1 attachment points are found
	 * @throws ToolkitException
	 *             If the toolkit implementation threw an exception
	 */
	public RWMolLeaf(RWMol comp)
			throws IllegalArgumentException, ToolkitException {
		this(comp.MolToSmiles(true));
	}

	@Override
	protected String toolkitCanonicalize(String smiles)
			throws ToolkitException {
		try {
			RWMol mol = RWMol.MolFromSmiles(smiles, 0, false);
			mol.sanitizeMol();
			String retVal = mol.MolToSmiles(true);
			mol.delete();
			return retVal;
		} catch (MolSanitizeException | GenericRDKitException
				| SmilesParseException e) {
			throw new ToolkitException(e);
		}
	}

	/**
	 * Method to return the RDKit format ({@link ExplicitBitVect}) Morgan
	 * fingerprint, rooted at the attachment point
	 * 
	 * @param radius
	 *            The radius of the fingerprint
	 * @param numBits
	 *            The number of bits in the fingerprint
	 * @param useChirality
	 *            Use chirality during fingerprint generation
	 * @param useBondTypes
	 *            Use bond types during fingerprint generation
	 * @return The RDKit binary Morgan Fingerprint. NB It is the caller's
	 *         responsibility to call #delete() on the returned
	 *         {@link ExplicitBitVect}
	 * @see #getMorganFingerprintCell(int, int, boolean, boolean)
	 */
	public ExplicitBitVect getMorganFingerprint(int radius, int numBits,
			boolean useChirality, boolean useBondTypes) {

		RWMol mol = RWMol.MolFromSmiles(canonicalSmiles, 0, false);
		UInt_Vect apIdx = new UInt_Vect();
		// TODO: Rewrite to loop through atoms?
		ExplicitBitVect retVal;
		Match_Vect matches = null;
		Int_Pair pair = null;
		try {
			RDKFuncs.sanitizeMol(mol);
			// Get the atom ID of the attachment point
			matches = mol.getSubstructMatch(
					MatchedPairsMultipleCutsNodePlugin.AP_QUERY_MOL);
			pair = matches.get(0);
			apIdx.add(pair.getSecond());
			retVal = RDKFuncs.getMorganFingerprintAsBitVect(mol, radius,
					numBits, null, apIdx, useChirality, useBondTypes);
		} catch (Exception e) {
			retVal = null;
			NodeLogger.getLogger("Key Leaf Generation Fingerprinter")
					.info("Unable to generate fingerprint for Leaf "
							+ canonicalSmiles);
		} finally {
			mol.delete();
			apIdx.delete();
			if (pair != null) {
				pair.delete();
			}
			if (matches != null) {
				matches.delete();
			}
		}
		return retVal;
	}

	@Override
	public DataCell getMorganFingerprintCell(int radius, int numBits,
			boolean useChirality, boolean useBondTypes) {
		ExplicitBitVect ebv = getMorganFingerprint(radius, numBits,
				useChirality, useBondTypes);
		if (ebv == null) {
			return DataType.getMissingCell();
		}
		DenseBitVector dbv = new DenseBitVector(ebv.getNumBits());
		Int_Vect onBits = ebv.getOnBits();
		for (int i = 0; i < onBits.size(); i++) {
			dbv.set(onBits.get(i), true);
		}
		onBits.delete();
		ebv.delete();
		return new DenseBitVectorCellFactory(dbv).createDataCell();
	}

}
