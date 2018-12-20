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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.knime.chem.types.SmilesCell;
import org.knime.chem.types.SmilesCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;

import com.vernalis.knime.chem.speedysmiles.helpers.SmilesHelpers;
import com.vernalis.knime.mmp.MMPConstants;

/**
 * The abstract implementation of a Fragmenation Key. A key comprises a number
 * of Leafs, which must be canonicalised and sorted to a canonical order. The
 * indices of the attachment points need to be assigned once the Leafs are
 * sorted.
 * 
 * @author S.Roughley
 *
 * @param <T>
 *            The type of the molecule object
 */
public abstract class AbstractFragmentKey<T> implements Comparable<AbstractFragmentKey<T>> {

	protected ArrayList<AbstractLeaf<T>> m_keyComponents;

	/**
	 * Factory method to return a leaf implementation from a SMILES String
	 * representaiton
	 * 
	 * @param keyAsString
	 *            the SMILES String of the key
	 * @return The correct {@link AbstractLeaf} implementation
	 */
	protected abstract AbstractLeaf<T> getLeafFromString(String keyAsString);

	/**
	 * Constructor - initialises an empty key
	 */
	public AbstractFragmentKey() {
		m_keyComponents = new ArrayList<>();
	}

	/**
	 * Constructor initialises from an existing key object
	 * 
	 * @param existingKey
	 *            The existing object
	 */
	public AbstractFragmentKey(AbstractFragmentKey<T> existingKey) {
		this.m_keyComponents = new ArrayList<>(existingKey.m_keyComponents);

	}

	public AbstractFragmentKey(String keyAsString) {
		this();
		try {
			addSMILESComponent(keyAsString);
		} catch (IllegalArgumentException iae) {
			// Do nothing - null arguments are allowed!
		}
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
			m_keyComponents.add(getLeafFromString(smiles));
		} else {
			for (String smi : smiles.split("\\.")) {
				m_keyComponents.add(getLeafFromString(smi));
			}
		}
	}

	/**
	 * Method to add a single Leaf to the component
	 * 
	 * @param t
	 *            The Leaf to add
	 */
	public void addLeaf(AbstractLeaf<T> t) {
		if (t != null) {
			m_keyComponents.add(t);
		}
	}

	/**
	 * Method to add all the components from a second key to this key
	 * 
	 * @param otherKey
	 *            A second {@link AbstractFragmentKey}
	 */
	public void mergeKeys(AbstractFragmentKey<T> otherKey) {
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
		for (AbstractLeaf<T> leaf : m_keyComponents) {
			retVal.put(newIdx++, leaf.getOriginalIndex());
		}
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AbstractFragmentKey [m_keyComponents=" + m_keyComponents + "]";
	}

	/**
	 * Method to get the key as a concatenated multi-component SMILES string,
	 * sorted in reverse natural order of components. NB, the SMILES components
	 * themselves are canonicalised by the {@link AbstractLeaf} class
	 * 
	 * @return
	 */
	public String getKeyAsString() {
		// Firstly, we sort in descending order
		Collections.sort(m_keyComponents, Collections.reverseOrder());

		boolean isFirst = true;
		StringBuilder sb = new StringBuilder();
		int idx = 1;
		for (AbstractLeaf<T> leaf : m_keyComponents) {
			if (!isFirst) {
				sb.append(".");
			} else {
				isFirst = false;
			}
			String smi = leaf.getIndexedCanonicalSmiles(idx++);
			sb.append(smi);
		}
		String smi = sb.toString();
		if (smi.matches("^\\[[0-9]*?\\*[H]?\\]$")) {
			smi = smi.replaceAll("^\\[([0-9]*?)\\*.*", "[$1*][H]");
		}

		return smi;
	}

	/**
	 * Method to get the fragment key as a {@link SmilesCell}
	 * 
	 * @see AbstractFragmentKey#getKeyAsString() getKeyAsString()
	 * 
	 * @return A {@link SmilesCell} of the correct type
	 */
	public DataCell getKeyAsDataCell() {
		return SmilesCellFactory.createAdapterCell(this.getKeyAsString());
	}

	/**
	 * @return The cell type to use in specs
	 */
	public DataType getCellType() {
		return MMPConstants.DEFAULT_OUTPUT_MOLECULE_COMPONENT_TYPE;
	}

	/**
	 * Private function to calculate the number of heavy atoms in this object-
	 * allows calculation for multiple components
	 * 
	 * @return The number of heavy atoms
	 */
	private Integer calcHAC() {
		return SmilesHelpers.countHAC(this.getKeyAsString());

	}

	/**
	 * Function to calculate the ratio of constant (HAs from the
	 * {@link AbstractFragmentKey}) to varying (HAs from the
	 * {@link AbstractFragmentValue}) heavy atoms
	 */
	public double getConstantToVaryingAtomRatio(AbstractFragmentValue<T> value) {
		return (double) this.calcHAC() / (double) value.getNumberChangingAtoms();
	}

	/**
	 * Function to return the ratio of constant (HAs from the
	 * {@link AbstractFragmentKey}) to varying (HAs from the
	 * {@link AbstractFragmentValue}) heavy atoms as a {@link DoubleCell}
	 */
	public DataCell getConstantToVaryingAtomRatioCell(AbstractFragmentValue<T> value) {
		return new DoubleCell(getConstantToVaryingAtomRatio(value));
	}

	/**
	 * Returns the leaf with the given index
	 * 
	 * @param idx
	 *            The index (1-based)
	 * @return The {@link AbstractLeaf}
	 */
	public AbstractLeaf<T> getLeafWithIdx(int idx) throws IndexOutOfBoundsException {
		// Firstly, we sort in descending order
		Collections.sort(m_keyComponents, Collections.reverseOrder());
		return m_keyComponents.get(idx - 1);
	}

	@Override
	public int compareTo(AbstractFragmentKey<T> that) {
		return this.getKeyAsString().compareTo(that.getKeyAsString());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_keyComponents == null) ? 0 : (this.getKeyAsString()).hashCode());
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
		AbstractFragmentKey<T> other = (AbstractFragmentKey<T>) obj;
		if (m_keyComponents == null) {
			if (other.m_keyComponents != null)
				return false;
		} else if (!this.getKeyAsString().equals(other.getKeyAsString()))
			// We compare the actual ordered strings, as the internal order does
			// not matter
			return false;
		return true;
	}

}
