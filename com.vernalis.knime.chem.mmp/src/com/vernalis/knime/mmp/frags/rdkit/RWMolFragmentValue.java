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

import java.util.HashMap;
import java.util.Map;

import org.RDKit.Atom;
import org.RDKit.GenericRDKitException;
import org.RDKit.Int_List;
import org.RDKit.MolSanitizeException;
import org.RDKit.RDKFuncs;
import org.RDKit.ROMol;
import org.RDKit.RWMol;
import org.RDKit.SmilesParseException;
import org.knime.core.data.vector.bytevector.DenseByteVector;

import com.vernalis.knime.mmp.ToolkitException;
import com.vernalis.knime.mmp.frags.abstrct.AbstractFragmentValue;

/**
 * Simple container class to hold an ID string and SMILES String together and
 * allow comparison and sorting for use in HashSet etc
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class RWMolFragmentValue extends AbstractFragmentValue<RWMol> {

	/**
	 * Constructor - a SMILES string must be supplied. The ID is null, and IDs
	 * are therefore ignored for comparisons
	 * 
	 * @param smiles
	 *            The SMILES String
	 */
	public RWMolFragmentValue(String smiles) {
		super(smiles);
	}

	/**
	 * Alternative constructor allowing the supplying of both SMILES String and
	 * ID. IDs are not ignored for comparisons
	 * 
	 * @param smiles
	 *            The SMILES String
	 * @param ID
	 *            The ID
	 */
	public RWMolFragmentValue(String smiles, String ID) {
		super(smiles, ID);
	}

	/**
	 * Alternative constructor allowing the supplying of both SMILES String and
	 * ID. IDs may be ignored for comparisons
	 * 
	 * @param smiles
	 *            The SMILES String
	 * @param ID
	 *            The ID
	 * @param ignoreIDsForComparisons
	 *            {@code true} if IDs should be ignored when comparing objects
	 */
	public RWMolFragmentValue(String smiles, String ID,
			boolean ignoreIDsForComparisons) {
		super(smiles, ID, ignoreIDsForComparisons);
	}

	/**
	 * Constructor from an RDKit molecule object and ID
	 * 
	 * @param value
	 *            The molecule object
	 * @param ID
	 *            The ID
	 */
	public RWMolFragmentValue(ROMol value, String ID) {
		this(value.MolToSmiles(true), ID);
	}

	@Override
	protected void toolkitCanonicalize() throws ToolkitException {
		try {
			RWMol mol = RWMol.MolFromSmiles(SMILES, 0, false);
			mol.sanitizeMol();
			SMILES = mol.MolToSmiles(true);
			mol.delete();
		} catch (MolSanitizeException | GenericRDKitException
				| SmilesParseException e) {
			throw new ToolkitException(e);
		}
	}

	@Override
	protected DenseByteVector initialistGraphDistanceFingerprint(int numBytes) {
		DenseByteVector retVal = new DenseByteVector(numBytes);
		if (numBytes == 0) {
			return retVal;
		}
		int apCount = countAttachmentPoints();
		int idx = 0;
		RWMol mol = null;
		try {
			mol = RWMol.MolFromSmiles(SMILES, 0, false);

			// First we need to find the atom indices of the attachment points
			Map<Integer, Integer> attPtAtomIndices = new HashMap<>();
			for (int i = 0; i < mol.getNumAtoms(); i++) {
				Atom at = mol.getAtomWithIdx(i);
				if (at.getAtomicNum() == 0) {
					attPtAtomIndices.put((int) at.getIsotope(), i);
				}
				at.delete();
			}

			// Now calculate the pairwise distance
			for (int i = 1; i < apCount; i++) {
				for (int j = i + 1; j <= apCount; j++) {
					Int_List shortestPath = RDKFuncs.getShortestPath(mol,
							attPtAtomIndices.get(i), attPtAtomIndices.get(j));
					// No of bonds from *i to *j (we use this to distinguish
					// e.g.
					// *a-*b from *a-C-*b)
					int pathLength = (int) (shortestPath.size() - 1);
					retVal.set(idx++, pathLength > 255 ? 255 : pathLength);
					shortestPath.delete();
				}
			}
		} catch (Exception e) {
			return null;
		} finally {
			if (mol != null) {
				mol.delete();
			}
		}
		return retVal;
	}

}
