/*******************************************************************************
 * Copyright (c) 2014, 2015, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
/**
 * 
 */
package com.vernalis.knime.mmp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.knime.chem.types.SmilesCell;
import org.knime.chem.types.SmilesCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.def.DoubleCell;

/**
 * A simple class to hold the fragment key as an ArrayList, which is then sorted
 * and concatenated to a multicomponent SMILES
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class FragmentKey2 implements Comparable<FragmentKey2> {
	protected ArrayList<Leaf> m_keyComponents;
	protected boolean m_removeHs;

	/**
	 * Constructor - initialises an empty key, H's removed by default;
	 */
	@Deprecated
	public FragmentKey2() {
		this(true);
	}

	public FragmentKey2(boolean removeHs) {
		m_keyComponents = new ArrayList<>();
		m_removeHs = removeHs;
	}

	/**
	 * Constructor initialises from an existing key object
	 * 
	 * @param existingKey
	 *            The existing object
	 */
	@Deprecated
	public FragmentKey2(FragmentKey2 existingKey) {
		this.m_keyComponents = new ArrayList<>(existingKey.m_keyComponents);
		this.m_removeHs = existingKey.m_removeHs;
	}

	/**
	 * Constructor initialising from a SMILES string, with one or more
	 * components. H's are removed
	 * 
	 * @param keyAsString
	 *            The key as a SMILES string
	 */
	public FragmentKey2(String keyAsString) {
		this(keyAsString, true);
	}

	public FragmentKey2(String keyAsString, boolean removeHs) {
		if (keyAsString == null) {
			m_keyComponents = new ArrayList<>();
		} else if (keyAsString.indexOf(".") < 0) {
			m_keyComponents = new ArrayList<>();
			m_keyComponents.add(new Leaf(keyAsString, removeHs));
		} else {
			m_keyComponents = new ArrayList<>();
			for (String smi : keyAsString.split("\\.")) {
				m_keyComponents.add(new Leaf(smi, removeHs));
			}
		}
		m_removeHs = removeHs;
	}

	/**
	 * Method to add a component as its SMILES string to the key.
	 * Multi-component SMILES will be split and added correctly
	 * 
	 * @param smiles
	 *            The SMILES string to add
	 */
	public void addSMILESComponent(String smiles) {
		if (smiles == null) {
			throw new IllegalArgumentException("A non-null string must be supplied");
		}
		if (smiles.indexOf(".") < 0) {
			m_keyComponents.add(new Leaf(smiles, m_removeHs));
		} else {
			for (String smi : smiles.split("\\.")) {
				m_keyComponents.add(new Leaf(smi, m_removeHs));
			}
		}
	}

	/**
	 * Method to add a single Leaf to the component
	 * 
	 * @param leaf
	 *            The Leaf to add
	 */
	public void addLeaf(Leaf leaf) {
		if (leaf != null) {
			m_keyComponents.add(leaf);
		}
	}

	/**
	 * Method to add all the components from a second key to this key
	 * 
	 * @param otherKey
	 *            A second {@link FragmentKey2}
	 */
	public void mergeKeys(FragmentKey2 otherKey) {
		this.m_keyComponents.addAll(otherKey.m_keyComponents);
	}

	/** Method to return the number of components in the Key object */
	public int getNumComponents() {
		return m_keyComponents.size();
	}

	/**
	 * Method to return a map with Keys as the original attachment point index
	 * and values as the new index in each case
	 */
	public HashMap<Integer, Integer> getAttachmentPointIndexLookup() {
		// Firstly, we sort in descending order
		Collections.sort(m_keyComponents, Collections.reverseOrder());

		HashMap<Integer, Integer> retVal = new HashMap<>();
		int newIdx = 1;
		for (Leaf leaf : m_keyComponents) {
			retVal.put(newIdx++, leaf.getOriginalIndex());
		}
		return retVal;
	}

	/**
	 * Method to get the key as a concatenated multi-component SMILES string,
	 * sorted in reverse natural order of components. NB, the SMILES components
	 * themselves are canonicalised by the {@link Leaf} class
	 * 
	 * @param removeExplicitHs
	 *            Should explicit hydrogens be removed?
	 * @param trackCutConnectivity
	 *            Should bond cut indices by tracked? Only use as {@code false}
	 *            during Smiles fragmentation parsing
	 * @return
	 */
	public String getKeyAsString(boolean removeExplicitHs, boolean trackCutConnectivity) {
		// Firstly, we sort in descending order
		Collections.sort(m_keyComponents, Collections.reverseOrder());

		boolean isFirst = true;
		StringBuilder sb = new StringBuilder();
		int idx = 1;
		for (Leaf leaf : m_keyComponents) {
			if (!isFirst) {
				sb.append(".");
			} else {
				isFirst = false;
			}
			String smi = trackCutConnectivity ? leaf.getIndexedCanonicalSmiles(idx++)
					: leaf.getCanonicalSmiles();
			sb.append(smi);
		}
		String smi = sb.toString();
		if (removeExplicitHs) {
			smi = RDKitFragmentationUtils.removeHydrogens(smi);
		}
		if (smi.matches("^\\[[0-9]*?\\*[H]?\\]$")) {
			smi = smi.replaceAll("^\\[([0-9]*?)\\*.*", "[$1*][H]");
		}

		return smi;
	}

	/**
	 * Returns a SmilesCell representation of the key.
	 * 
	 * @param trackCutConnectivity
	 * 
	 * @param Remove
	 *            Explicit H atoms?
	 * 
	 * @return A {@link DataCell} containing the SMILES representation of the
	 *         Key.
	 * @see #getKeyAsString(boolean)
	 * @deprecated Track cut connectivity option is no longer used
	 */
	@Deprecated
	public DataCell getKeyAsDataCell(boolean removeExplicitHs, boolean trackCutConnectivity) {
		return SmilesCellFactory
				.create(this.getKeyAsString(removeExplicitHs, trackCutConnectivity));
	}

	/**
	 * Overloaded {@link #getKeyAsDataCell(boolean, boolean)} method variant, in
	 * which the default track cut connectivity is set to {@code true}
	 * 
	 * @param removeExplicitHs
	 *            Should explicit Hydrogens be removed from the SMILES String?
	 * @return A {@link SmilesCell} of the correct type
	 */
	public DataCell getKeyAsDataCell(boolean removeExplicitHs) {
		return getKeyAsDataCell(removeExplicitHs, true);
	}

	/**
	 * Private function to calculate the number of heavy atoms in this object-
	 * allows calculation for multiple components
	 * 
	 * @return
	 */
	private Integer calcHAC() {
		int cnt = 0;
		String SMILES = this.getKeyAsString(false, false);
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
		cnt -= countAttachmentPoints(SMILES);
		return cnt;
	}

	/**
	 * Function to calculate the ratio of constant (HAs from the
	 * {@link FragmentKey2}) to varying (HAs from the {@link FragmentValue})
	 * heavy atoms
	 */
	public double getConstantToVaryingAtomRatio(FragmentValue2 value) {
		return (double) this.calcHAC() / (double) value.getNumberChangingAtoms();
	}

	/**
	 * Function to return the ratio of constant (HAs from the
	 * {@link FragmentKey2}) to varying (HAs from the {@link FragmentValue})
	 * heavy atoms as a {@link DoubleCell}
	 */
	public DataCell getConstantToVaryingAtomRatioCell(FragmentValue2 value) {
		return new DoubleCell(getConstantToVaryingAtomRatio(value));
	}

	/**
	 * Returns the leaf with the given index
	 * 
	 * @param idx
	 *            The index (1-based)
	 * @return The {@link Leaf}
	 */
	public Leaf getLeafWithIdx(int idx) throws IndexOutOfBoundsException {
		// Firstly, we sort in descending order
		Collections.sort(m_keyComponents, Collections.reverseOrder());
		return m_keyComponents.get(idx - 1);
	}

	/**
	 * Private function to calculate the number of attachment points in the
	 * object
	 * 
	 * @return The number of attachment points
	 */
	private Integer countAttachmentPoints(String SMILES) {
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

	@Override
	public int compareTo(FragmentKey2 that) {
		return this.getKeyAsString(false, true).compareTo(that.getKeyAsString(false, true));
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
		result = prime * result
				+ ((m_keyComponents == null) ? 0 : (this.getKeyAsString(false, true)).hashCode());
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
		FragmentKey2 other = (FragmentKey2) obj;
		if (m_keyComponents == null) {
			if (other.m_keyComponents != null)
				return false;
		} else if (!this.getKeyAsString(false, true).equals(other.getKeyAsString(false, true)))
			// We compare the actual ordered strings, as the internal order does
			// not matter
			return false;
		return true;
	}

}
