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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.knime.chem.types.SmilesAdapterCell;
import org.knime.chem.types.SmilesCellFactory;
import org.knime.core.data.AdapterCell;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.vector.bytevector.DenseByteVector;
import org.knime.core.data.vector.bytevector.DenseByteVectorCell;
import org.knime.core.data.vector.bytevector.DenseByteVectorCellFactory;

/**
 * The abstract implementation of the Fragmentation 'Value' component.
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The type of the molecule object
 */
public abstract class AbstractFragmentValue<T> implements Comparable<AbstractFragmentValue<T>> {

	protected String SMILES;

	protected abstract void toolkitCanonicalize();

	protected final String ID;
	protected boolean ignoreIDsForComparisons = false;
	protected DenseByteVector graphDistFP;
	protected Integer HAC = null;

	/**
	 * Constructor from a SMILES string, with no ID and IDs not ignored in
	 * comparisons
	 * 
	 * @param smiles
	 *            The SMILES String
	 */
	protected AbstractFragmentValue(String smiles) {
		this(smiles, null, false);
	}

	/**
	 * Constructor from a SMILES string and ID, which is not ignored in
	 * comparisons
	 * 
	 * @param smiles
	 *            The SMILES string
	 * @param ID
	 *            The ID
	 */
	protected AbstractFragmentValue(String smiles, String ID) {
		this(smiles, ID, false);
	}

	/**
	 * Full constructor
	 * 
	 * @param smiles
	 *            The SMILES String
	 * @param ID
	 *            The ID
	 * @param ignoreIDsForComparisons
	 *            Is the ID ignored in comparisons?
	 */
	protected AbstractFragmentValue(String smiles, String ID, boolean ignoreIDsForComparisons) {
		if (smiles.matches("^\\[[0-9]*?\\*[H]?\\]$")) {
			this.SMILES = smiles.replaceAll("^\\[([0-9]*?)\\*.*", "[$1*][H]");
		} else {
			this.SMILES = smiles;
		}
		this.ID = ID;
		this.ignoreIDsForComparisons = ignoreIDsForComparisons;
	}

	/**
	 * @return is the component a 3D fragmentation?
	 */
	public boolean getIs3D() {
		return false;
	}

	/**
	 * @return The AdapterCell for the fragment (currently the SMILES cell, but
	 *         a 3D implementation may use something different!)
	 */
	public AdapterCell getAdapterCell() {
		return getSMILESCell();
	}

	/**
	 * @return The SMILES String
	 */
	public String getSMILES() {
		return SMILES;
	}

	/**
	 * Get the SMILES as a {@link DataCell}
	 */
	public SmilesAdapterCell getSMILESCell() {
		return (SmilesAdapterCell) SmilesCellFactory.createAdapterCell(getSMILES());
	}

	/**
	 * @return The ID of the molecule. May be {@code null}
	 */
	public String getID() {
		return ID;
	}

	/**
	 * @return Return the ID as a {@link DataCell} (may be
	 *         {@link DataType#getMissingCell()})
	 */
	public DataCell getIDCell() {
		StringCell retVal;
		if (ID == null) {
			retVal = (StringCell) DataType.getMissingCell();
		} else {
			retVal = new StringCell(ID);
		}
		return retVal;
	}

	/**
	 * @return The Graph distances between the attachment points as
	 *         {@link DenseByteVectorCell} fingerprint. The bytes are in order 0
	 *         - 1-2 dist, 1 - 1-3 dist, 2 -2-3 dist etc; Distances are the
	 *         number of bonds from *a to *b in each case, in order to
	 *         distinguish *a-*b (1) from *a-C(R)-*b (2) etc.
	 */
	public DataCell getAttachmentPointGraphDistanceFingerprint() {
		if (graphDistFP == null) {
			int numBytes = countAttachmentPoints();
			if (numBytes > 1) {
				numBytes = calcBytes(numBytes);
			} else {
				numBytes = 0;
			}
			graphDistFP = initialistGraphDistanceFingerprint(numBytes);
		}
		if (graphDistFP == null) {
			// Couldnt create a fingerprint
			return DataType.getMissingCell();
		}
		return new DenseByteVectorCellFactory(graphDistFP).createDataCell();
	}

	/**
	 * Simple utility method to calculate the number of bytes in the count
	 * fingerprint (nC2, n!/(2!(n-2)!)
	 * 
	 * @param numAPs
	 *            The number of APs (>1)
	 * @return The number of attachment points
	 */
	private int calcBytes(int numAPs) {
		return factorial(numAPs) / (2 * factorial(numAPs - 2));
	}

	/**
	 * Simple utility method to calculate <i>small</i> factorials
	 */
	private int factorial(int n) {
		int retVal = 1;
		for (int i = n; i > 0; i--) {
			retVal *= i;
		}
		return retVal;
	}

	/**
	 * Calculate the graph distances between attachment points as a
	 * {@link DenseByteVector}. The method may return <code>null</code> if no
	 * fingerprint is possible, but should return a 0-length fingerprint for 1
	 * attachment point. Distances >=255 should be shown as 255. Distances are
	 * the number of bonds from *a to *b in each case, in order to distinguish
	 * *a-*b (1) from *a-C(R)-*b (2) etc.
	 * <p>
	 * The bytes are in the order:
	 * 
	 * <pre>
	 * Byte	Distance
	 *  0	  1-2
	 *  1	  1-3
	 * ...	  ...
	 * n-1	  1-n
	 *  n	  2-3
	 * ...	  ...
	 *(n-1)!	(n-1)-n
	 * </pre>
	 * 
	 * @param numBytes
	 *            The number of bytes in the fingerprint
	 * @return The fingerprint as a {@link DenseByteVector} or <code>null</code>
	 *         if no fingerprint is possible
	 */
	protected abstract DenseByteVector initialistGraphDistanceFingerprint(int numBytes);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AbstractFragmentValue [SMILES=" + SMILES + ", ID=" + ID
				+ ", ignoreIDsForComparisons=" + ignoreIDsForComparisons + ", "
				+ (getIs3D() ? "3D" : "2D") + "]";
	}

	/**
	 * Get the number of changing atoms (i.e. the number of heavy atoms not '*')
	 * in the {@link AbstractFragmentValue} {@link #SMILES}
	 */
	public int getNumberChangingAtoms() {
		if (HAC == null) {
			HAC = this.calcHAC();
		}
		return HAC.intValue();
	}

	/** Get the number of changing atoms as an {@link IntCell} */
	public DataCell getNumberChangingAtomsCell() {
		return new IntCell(getNumberChangingAtoms());
	}

	/**
	 * Private function to calculate the number of heavy atoms in this object-
	 * allows calculation for multiple components
	 * 
	 * @return
	 */
	private Integer calcHAC() {
		int cnt = 0;
		// NB define length as l as otherwise smiles.length
		// is re-calculated on each iteration, apparently
		for (int i = 0, l = SMILES.length(); i < l; i++) {
			char x = SMILES.charAt(i);
			if (x == '.')
				// skip - but keep counting!
				continue;
			if (x == '[') {
				cnt++;
				// skip to ]
				while (SMILES.charAt(i) != ']')
					i++;
				continue;
			}

			// Deal with aromatic atoms without []
			if (x == 'c' || x == 'o' || x == 'n' || x == 's') {
				cnt++;
				continue;
			}

			// Deal with other atoms
			if (Character.isUpperCase(x))
				cnt++;
		}
		// Now correct for [H]
		cnt -= (SMILES.indexOf("[H]") >= 0) ? SMILES.split("\\[H\\]").length - 1 : 0;
		// And correct for attachment points
		cnt -= countAttachmentPoints();
		return cnt;
	}

	/**
	 * Method to set the attachment point indices. a {@link AbstractFragmentKey}
	 * argument is required in order to perform the correct mapping. The SMILES
	 * string is canonicalised afterwards by a call to {@link #canonicalize()}
	 * 
	 * @param key
	 *            The {@link AbstractFragmentKey} argument for the mapping
	 * @return A map of the indices (mainly to allow subclasses to call this
	 *         implementation and subsequently reuse the map without
	 *         recalculation; Key is original AP index, Value is the new Index
	 */
	public Map<Integer, Integer> setAttachmentPointIndices(AbstractFragmentKey<T> key) {
		HashMap<Integer, Integer> apLookup = key.getAttachmentPointIndexLookup();
		for (Entry<Integer, Integer> ent : apLookup.entrySet()) {
			SMILES = SMILES.replace("[" + ent.getValue() + "*]", "[" + ent.getKey() + "*]");
		}
		canonicalize();
		return apLookup;
	}

	/**
	 * Canonicalise the SMILES String. This calls {@link #toolkitCanonicalize()}
	 * if the value is more than '*-*'
	 */
	public void canonicalize() {
		if (SMILES.matches("\\[\\d+\\*\\](-)?\\[\\d+\\*\\]")) {
			canonicalizeBondOnlyValue();
		} else if (SMILES.matches("^\\[[0-9]*?\\*[H]?\\]$")) {
			SMILES = SMILES.replaceAll("^\\[([0-9]*?)\\*.*", "[$1*][H]");
		} else {
			toolkitCanonicalize();
		}
	}

	/**
	 * We always want *-* to canonicalize with the highest index first
	 */
	protected void canonicalizeBondOnlyValue() {
		if (SMILES.matches("\\[\\d+\\*\\](-)?\\[\\d+\\*\\]")) {
			// Bond
			Pattern p = Pattern.compile("\\[([0-9]+)\\*\\]");
			Matcher m = p.matcher(SMILES);
			m.find();
			int firstIdx = Integer.parseInt(m.group(1));
			m.find();
			int secondIdx = Integer.parseInt(m.group(1));
			if (secondIdx > firstIdx) {
				SMILES = "[" + secondIdx + "*][" + firstIdx + "*]";
			}
		}
	}

	/**
	 * Private function to calculate the number of attachment points in the
	 * object
	 * 
	 * @return The number of attachment points
	 */
	public Integer countAttachmentPoints() {
		int result;
		try {
			result = SMILES.split("\\[[0-9]*?\\*").length;
		} catch (Exception e) {
			try {
				result = SMILES.split("\\*").length;
			} catch (Exception e1) {
				result = 1;
			}
		}
		// Result will hold one more than it should
		result--;
		return result;
	}

	/**
	 * @param other
	 *            Check if are the same SMILES string
	 * @return {@code true} if the two values have the same SMILES string
	 */
	public boolean isSameSMILES(AbstractFragmentValue<T> other) {
		return this.SMILES.equals(other.SMILES);
	}

	@Override
	public int compareTo(AbstractFragmentValue<T> that) {
		// Comparison is lexicographical of the string representations
		return this.toString().compareTo(that.toString());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if (!ignoreIDsForComparisons) {
			result = prime * result + ((ID == null) ? 0 : ID.hashCode());
		}
		result = prime * result + ((SMILES == null) ? 0 : SMILES.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("unchecked")
		AbstractFragmentValue<T> other = (AbstractFragmentValue<T>) obj;
		if (!ignoreIDsForComparisons) {
			if (ID == null) {
				if (other.ID != null) {
					return false;
				}
			} else if (!ID.equals(other.ID)) {
				return false;
			}
		}
		if (SMILES == null) {
			if (other.SMILES != null) {
				return false;
			}
		} else if (!SMILES.equals(other.SMILES)) {
			return false;
		}
		return true;
	}

}
