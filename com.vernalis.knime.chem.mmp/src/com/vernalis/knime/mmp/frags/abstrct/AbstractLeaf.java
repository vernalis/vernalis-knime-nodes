/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.frags.abstrct;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.knime.core.data.DataCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.node.NodeLogger;

/**
 * The abstract implementation of a 'Leaf' resulting from a fragmentation. A
 * 'Leaf' is an individual component of a Key, which may have 1 or more leaves.
 * The leaf handles canonicalisation of the individual components, and provides
 * a comparable to sort Leafs into a canonical order within the Key
 * 
 * @param <T>
 *            The type of molecule object for the toolkit
 */
public abstract class AbstractLeaf<T> implements Comparable<AbstractLeaf<T>> {
	protected final NodeLogger logger = NodeLogger.getLogger(this.getClass());
	protected final String canonicalSmiles;
	protected final int originalIndex;

	/**
	 * Constructor. The SMILES is canonicalised and the incoming attachment
	 * point index stored for reference
	 * 
	 * @param smiles
	 *            The SMILES String
	 * @throws IllegalArgumentException
	 *             If no or &gt;1 attachment points are found
	 */
	public AbstractLeaf(String smiles) throws IllegalArgumentException {
		Pattern p = Pattern.compile("\\[([0-9]+)\\*\\H?]");
		Matcher m = p.matcher(smiles);
		if (!m.find()) {
			throw new IllegalArgumentException("No Attachment Point found in SMILES String");
		}
		originalIndex = Integer.parseInt(m.group(1));
		if (m.find()) {
			throw new IllegalArgumentException(
					"More than one attachment point found in SMILES String");
		}

		if (smiles.matches("^\\[[0-9]*?\\*[H]?\\]$")) {
			smiles = smiles.replaceAll("^\\[([0-9]*?)\\*.*", "[$1*][H]");
		}
		canonicalSmiles = toolkitCanonicalize(smiles.replaceAll("\\[[0-9]+\\*\\]", "[*]"));
	}

	/**
	 * Method to return the RDKit Morgan fingerprint, rooted at the attachment
	 * point, as a KNIME {@link DenseBitVectorCell}
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
	public abstract DataCell getMorganFingerprintCell(int radius, int numBits, boolean useChirality,
			boolean useBondTypes);

	/**
	 * Method to return a canonical SMILES string using the implementing toolkit
	 * 
	 * @param smiles
	 *            The SMILES to canonicalise
	 * @return The canonical form of the SMILES
	 */
	protected abstract String toolkitCanonicalize(String smiles);

	/**
	 * @return The Canonical SMILES. NB The attachment point will have been
	 *         de-indexed, and thus will be [*]
	 */
	public String getCanonicalSmiles() {
		return canonicalSmiles;
	}

	/**
	 * @return is the fragment part of a 3D fragmentation
	 */
	public boolean is3D() {
		return false;
	}

	/**
	 * Get the canonical SMILES representation with the attachment point indexed
	 * 
	 * @param idx
	 *            The index for the attachment point
	 * @return The canonical SMILES with the attachment point labelled
	 *         isotopically with the given index
	 * @throws IllegalArgumentException
	 *             if idx &lt; 0
	 * 
	 */
	public String getIndexedCanonicalSmiles(int idx) throws IllegalArgumentException {
		if (idx < 0) {
			throw new IllegalArgumentException("Index must be >= 0");
		}
		return canonicalSmiles.replaceAll("\\[[\\d]*\\*\\]", "[" + idx + "*]")
				.replaceAll("(?<!\\[)\\*(?!\\])", "[" + idx + "*]");
	}

	/**
	 * @return The original attachment point index
	 */
	public int getOriginalIndex() {
		return originalIndex;
	}

	@Override
	public int compareTo(AbstractLeaf<T> o) {
		int smiComp = this.canonicalSmiles.compareTo(o.canonicalSmiles);
		if (smiComp != 0) {
			return smiComp;
		}
		return Integer.compare(this.originalIndex, o.originalIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AbstractLeaf [canonicalSmiles=" + canonicalSmiles + ", originalIndex="
				+ originalIndex + ", " + (is3D() ? "3D" : "2D") + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((canonicalSmiles == null) ? 0 : canonicalSmiles.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		AbstractLeaf<T> other = (AbstractLeaf<T>) obj;
		if (canonicalSmiles == null) {
			if (other.canonicalSmiles != null)
				return false;
		} else if (!canonicalSmiles.equals(other.canonicalSmiles))
			return false;
		return true;
	}

}
