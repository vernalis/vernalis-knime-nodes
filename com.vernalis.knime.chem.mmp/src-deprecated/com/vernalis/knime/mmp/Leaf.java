/*******************************************************************************
 * Copyright (c) 2015, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License, Version 3, as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.mmp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.RDKit.ExplicitBitVect;
import org.RDKit.Int_Pair;
import org.RDKit.Match_Vect;
import org.RDKit.RDKFuncs;
import org.RDKit.RWMol;
import org.RDKit.UInt_Vect;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.vector.bitvector.DenseBitVector;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCellFactory;
import org.knime.core.node.NodeLogger;

/**
 * This class provides a canonicalisation mechanism for single-attachment point
 * SMILES strings, and also record the index of the incoming attachment point
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * @deprecated
 */
@Deprecated
public class Leaf implements Comparable<Leaf> {
	private final String canonicalSmiles;
	private final int originalIndex;

	/**
	 * Overloaded constructor. The SMILES is canonicalised after removal of
	 * Hydrogens, and the original attachment point index stored for reference
	 * 
	 * @param smiles
	 *            The SMILES String
	 * @throws IllegalArgumentException
	 *             If no or >1 attachment points are found
	 */
	@Deprecated
	public Leaf(String smiles) throws IllegalArgumentException {
		this(smiles, true);
	}

	/**
	 * Constructor with optional removal of hydrogen atoms. The SMILES is
	 * canonicalised and the incoming attachment point index stored for
	 * reference
	 * 
	 * @param smiles
	 *            The SMILES String
	 * @param removeHs
	 *            Should hydrogen atoms be removed?
	 * @throws IllegalArgumentException
	 *             If no or >1 attachment points are found
	 */
	public Leaf(String smiles, boolean removeHs) throws IllegalArgumentException {
		Pattern p = Pattern.compile("\\[([0-9]+)\\*\\]");
		Matcher m = p.matcher(smiles);
		if (!m.find()) {
			throw new IllegalArgumentException("No Attachment Point found in SMILES String");
		}
		originalIndex = Integer.parseInt(m.group(1));
		if (m.find()) {
			throw new IllegalArgumentException(
					"More than one attachment point found in SMILES String");
		}
		// System.out.print(smiles + "\t--->\t");
		if (removeHs) {
			smiles = RDKitFragmentationUtils.removeHydrogens(smiles);
		}
		if (smiles.matches("^\\[[0-9]*?\\*[H]?\\]$")) {
			smiles = smiles.replaceAll("^\\[([0-9]*?)\\*.*", "[$1*][H]");
		}
		// System.out.println(smiles);
		RWMol mol = RWMol.MolFromSmiles(smiles.replaceAll("\\[[0-9]+\\*\\]", "[*]"), 0, false);
		mol.sanitizeMol();
		// mol.findSSSR();
		canonicalSmiles = mol.MolToSmiles(true);
		mol.delete();
	}

	/**
	 * @return The Canonical SMILES. NB The attachment point will have been
	 *         de-indexed, and thus will be [*]
	 */
	public String getCanonicalSmiles() {
		return canonicalSmiles;
	}

	/**
	 * Get the canonical SMILES representation with the attachment point indexed
	 * 
	 * @param idx
	 *            The index for the attachment point
	 * @return The canonical SMILES with the attachment point labelled
	 *         isotopically with the given index
	 * @throws IllegalArgumentException
	 *             if idx<0
	 * 
	 */
	public String getIndexedCanonicalSmiles(int idx) throws IllegalArgumentException {
		if (idx < 0) {
			throw new IllegalArgumentException("Index must be >= 0");
		}
		return canonicalSmiles.replaceAll("\\[[\\d]*\\*\\]", "[" + idx + "*]");
	}

	/**
	 * @return The original attachment point index
	 */
	public int getOriginalIndex() {
		return originalIndex;
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
	public ExplicitBitVect getMorganFingerprint(int radius, int numBits, boolean useChirality,
			boolean useBondTypes) {

		RWMol mol = RWMol.MolFromSmiles(canonicalSmiles, 0, false);
		UInt_Vect apIdx = new UInt_Vect();
		// TODO: Rewrite to loop through atoms?
		ExplicitBitVect retVal;
		Match_Vect matches = null;
		Int_Pair pair = null;
		try {
			RDKFuncs.sanitizeMol(mol);
			// Get the atom ID of the attachment point
			matches = mol.getSubstructMatch(MatchedPairsMultipleCutsNodePlugin.AP_QUERY_MOL);
			pair = matches.get(0);
			apIdx.add(pair.getSecond());
			retVal = RDKFuncs.getMorganFingerprintAsBitVect(mol, radius, numBits, null, apIdx,
					useChirality, useBondTypes);
		} catch (Exception e) {
			retVal = null;
			NodeLogger.getLogger("Key Leaf Generation Fingerprinter")
					.info("Unable to generate fingerprint for Leaf " + canonicalSmiles);
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

	/**
	 * Wrapper method for
	 * {@link #getMorganFingerprint(int, int, boolean, boolean)} to return the
	 * RDKit Morgan fingerprint, rooted at the attachment point, as a KNIME
	 * {@link DenseBitVectorCell}
	 * 
	 * @param radius
	 *            The radius of the fingerprint
	 * @param numBits
	 *            The number of bits in the fingerprint
	 * @param useChirality
	 *            Use chirality during fingerprint generation
	 * @param useBondTypes
	 *            Use bond types during fingerprint generation
	 * @return The RDKit binary Morgan Fingerprint as a KNIME DenseBitVector
	 *         Cell
	 */
	public DataCell getMorganFingerprintCell(int radius, int numBits, boolean useChirality,
			boolean useBondTypes) {
		ExplicitBitVect ebv = getMorganFingerprint(radius, numBits, useChirality, useBondTypes);
		if (ebv == null) {
			return DataType.getMissingCell();
		}
		DenseBitVector dbv = RdkitTypeConvertors.rdkitFpToDenseBitVector(ebv);
		ebv.delete();
		if (dbv == null) {
			// We shouldn't get here!
			return DataType.getMissingCell();
		}
		return new DenseBitVectorCellFactory(dbv).createDataCell();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Leaf o) {
		int smiComp = this.canonicalSmiles.compareTo(o.canonicalSmiles);
		if (smiComp != 0) {
			return smiComp;
		}
		return Integer.compare(this.originalIndex, o.originalIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((canonicalSmiles == null) ? 0 : canonicalSmiles.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Leaf other = (Leaf) obj;
		if (canonicalSmiles == null) {
			if (other.canonicalSmiles != null)
				return false;
		} else if (!canonicalSmiles.equals(other.canonicalSmiles))
			return false;
		// if(originalIndex!=other.originalIndex){
		// return false;
		// }
		return true;
	}

}
