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
package com.vernalis.knime.mmp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.knime.chem.types.SmilesCell;
import org.knime.core.data.DataCell;
import org.knime.core.data.def.DoubleCell;

/**
 * A simple class to hold the fragment key as an ArrayList, which is then sorted
 * and concatenated to a multicomponent SMILES
 * 
 * @author "Stephen Roughley knime@vernalis.com"
 * @deprecated Implementations should use {@link RDKitFragmentKey}
 */
@Deprecated
public class FragmentKey implements Comparable<FragmentKey> {
	protected ArrayList<String> m_keyComponents;

	/**
	 * Constructor - initialises an empty key
	 */
	public FragmentKey() {
		m_keyComponents = new ArrayList<>();
	}

	/**
	 * Constructor initialises from an existing object
	 * 
	 * @param existingKey
	 *            The existing object
	 */
	public FragmentKey(FragmentKey existingKey) {
		this.m_keyComponents = new ArrayList<>(existingKey.m_keyComponents);
	}

	/**
	 * Constructor initialising from a SMILES string, with one or more
	 * components
	 * 
	 * @param keyAsString
	 *            The key as a SMILES string
	 */
	public FragmentKey(String keyAsString) {
		if (keyAsString == null) {
			m_keyComponents = new ArrayList<>();
		} else if (keyAsString.indexOf(".") < 0) {
			m_keyComponents = new ArrayList<>();
			m_keyComponents.add(keyAsString);
		} else {
			m_keyComponents = new ArrayList<>(Arrays.asList(keyAsString.split("\\.")));
		}
	}

	/**
	 * Method to add a component as its SMILES string to the key.
	 * Multi-component SMILES will be split and added correctly
	 * 
	 * @param smiles
	 *            The SMILES string to add
	 */
	public void addComponent(String smiles) {
		if (smiles == null) {
			throw new IllegalArgumentException("A non-null string must be supplied");
		}
		if (smiles.indexOf(".") < 0) {
			m_keyComponents.add(smiles);
		} else {
			m_keyComponents.addAll(Arrays.asList(smiles.split(".")));
		}
	}

	/**
	 * Method to add all the components from a second key to this key
	 * 
	 * @param otherKey
	 *            A second {@link FragmentKey}
	 */
	public void mergeKeys(FragmentKey otherKey) {
		this.m_keyComponents.addAll(otherKey.m_keyComponents);
	}

	/** Method to return the number of components in the Key object */
	public int getNumComponents() {
		return m_keyComponents.size();
	}

	/**
	 * Method to get the key as a concatenated multi-component SMILES string,
	 * sorted in reverse natural order of components. NB, the SMILES components
	 * themselves are not canonicalised
	 * 
	 * @param removeExplicitHs
	 *            Remove Explicit H atoms?
	 */
	public String getKeyAsString(boolean removeExplicitHs) {
		// Firstly, we sort in descending order
		Collections.sort(m_keyComponents, Collections.reverseOrder());

		boolean isFirst = true;
		StringBuilder sb = new StringBuilder();
		for (String comp : m_keyComponents) {
			if (!isFirst) {
				sb.append(".");
			} else {
				isFirst = false;
			}
			String smi = comp;
			if (removeExplicitHs) {
				// Use a quick and dirty string replacement which will leave
				// [nH] alone (correctly!)
				smi = smi.replace("[H]", "").replace("()", "");
			}
			if (smi.equals("[*H]") || smi.equals("[*]")) {
				smi = "[*][H]";
			}

			sb.append(smi);

		}
		return sb.toString();
	}

	/**
	 * Returns a SmilesCell representation of the key.
	 * 
	 * @param removeExplicitHs
	 *            Remove Explicit H atoms?
	 * 
	 * @return A {@link DataCell} containing the SMILES representation of the
	 *         Key.
	 * @see #getKeyAsString(boolean)
	 */
	public DataCell getKeyAsDataCell(boolean removeExplicitHs) {
		return new SmilesCell(this.getKeyAsString(removeExplicitHs));
	}

	/**
	 * Private function to calculate the number of heavy atoms in this object-
	 * allows calculation for multiple components
	 * 
	 * @return The number of heavy atoms in the component
	 */
	private Integer calcHAC() {
		int cnt = 0;
		String SMILES = this.getKeyAsString(false);
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
	 * {@link FragmentKey}) to varying (HAs from the {@link FragmentValue})
	 * heavy atoms
	 */
	public double getConstantToVaryingAtomRatio(FragmentValue value) {
		return (double) this.calcHAC() / (double) value.getNumberChangingAtoms();
	}

	/**
	 * Function to return the ratio of constant (HAs from the
	 * {@link FragmentKey}) to varying (HAs from the {@link FragmentValue})
	 * heavy atoms as a {@link DoubleCell}
	 */
	public DataCell getConstantToVaryingAtomRatioCell(FragmentValue value) {
		return new DoubleCell(getConstantToVaryingAtomRatio(value));
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
	public int compareTo(FragmentKey that) {
		return this.getKeyAsString(false).compareTo(that.getKeyAsString(false));
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
				+ ((m_keyComponents == null) ? 0 : (this.getKeyAsString(false)).hashCode());
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
		FragmentKey other = (FragmentKey) obj;
		if (m_keyComponents == null) {
			if (other.m_keyComponents != null)
				return false;
		} else if (!this.getKeyAsString(false).equals(other.getKeyAsString(false)))
			// We compare the actual ordered strings, as the internal order does
			// not matter
			return false;
		return true;
	}

}
