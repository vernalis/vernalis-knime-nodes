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

import java.util.Collection;

import org.apache.batik.css.engine.value.AbstractValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;

import com.vernalis.knime.mmp.ToolkitException;
import com.vernalis.knime.mmp.fragmentors.MoleculeFragmentationException;

/**
 * Abstract class to contain a fragmentation pattern. The class serves as a
 * fragmentation validator and factory class. An in coming fragmentation is
 * parsed and components assigned to be Leafs, which are added to the
 * fragmentation Key, or as the Fragmentation Value.
 * 
 * <p>
 * Takes with n leaf components, each with 1 attachment point, and 1 value
 * component which must have n attachment points.
 * </p>
 * <p>
 * The parser implements comparable, and sorting is as follows:
 * <ol>
 * <li>Sorted by increasing number of cuts</li>
 * <li>Sorted by the canonicalised SMILES</li>
 * </ol>
 * </p>
 * 
 * <p>
 * NB This API is possibly unstable in the current version and future versions
 * may change
 * </p>
 * 
 * @param <T>
 *            The type of molecule object used in the the fragmentation
 */
public abstract class AbstractMulticomponentFragmentationParser<T>
		implements Comparable<AbstractMulticomponentFragmentationParser<T>> {

	protected final AbstractFragmentKey<T> m_key;

	protected AbstractFragmentValue<T> m_value = null;
	protected final String smiles;
	protected final int numCuts;
	protected final String canonicalisedSMILES;
	private DataCell rendering = DataType.getMissingCell();

	/**
	 * Constructor from a value and variable number of leaf components
	 * 
	 * @param value
	 *            The value component
	 * @param keyComponents
	 *            The key component(s)
	 * @throws MoleculeFragmentationException
	 *             If this is an invalid fragmentation
	 * @throws ToolkitException
	 *             If the toolkit threw an exception
	 */
	@SafeVarargs
	public AbstractMulticomponentFragmentationParser(T value, T... keyComponents)
			throws MoleculeFragmentationException, ToolkitException {
		m_value = createValueFromMolecule(value);
		m_key = createFragmentKey();
		for (T comp : keyComponents) {
			if (isMultiComponent(comp)) {
				for (T subComp : getComponentsFromMultiComponent(comp)) {
					m_key.addLeaf(createLeafFromMolecule(subComp));
				}
			} else {
				m_key.addLeaf(createLeafFromMolecule(comp));
			}
		}

		if (m_key.getNumComponents() != m_value.countAttachmentPoints()) {
			throw new MoleculeFragmentationException(
					"Different number of key components to attachment points");
		}
		// finally, set the indices and canonicalise
		m_value.setAttachmentPointIndices(m_key);
		numCuts = m_value.countAttachmentPoints();
		canonicalisedSMILES = m_key.getKeyAsString() + "." + m_value.getSMILES();
		smiles = canonicalisedSMILES;
	}

	/**
	 * Constructor from a value and collection of leaf components
	 * 
	 * @param value
	 *            The value component
	 * @param keyComponents
	 *            The key component(s)
	 * @throws MoleculeFragmentationException
	 *             If this is an invalid fragmentation
	 * @throws ToolkitException
	 *             If the toolkit threw an exception
	 */
	public AbstractMulticomponentFragmentationParser(T value, Collection<T> keyComponents)
			throws MoleculeFragmentationException, ToolkitException {
		m_value = createValueFromMolecule(value);
		m_key = createFragmentKey();
		for (T comp : keyComponents) {
			if (isMultiComponent(comp)) {
				for (T subComp : getComponentsFromMultiComponent(comp)) {
					m_key.addLeaf(createLeafFromMolecule(subComp));
				}
			} else {
				m_key.addLeaf(createLeafFromMolecule(comp));
			}
		}

		if (m_key.getNumComponents() != m_value.countAttachmentPoints()) {
			throw new MoleculeFragmentationException(
					"Different number of key components to attachment points");
		}
		// finally, set the indices and canonicalise
		m_value.setAttachmentPointIndices(m_key);
		numCuts = m_value.countAttachmentPoints();
		canonicalisedSMILES = m_key.getKeyAsString() + "." + m_value.getSMILES();
		smiles = canonicalisedSMILES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AbstractMulticomponentSmilesFragmentParser [numCuts=");
		builder.append(numCuts);
		builder.append(", canonicalisedSMILES=");
		builder.append(canonicalisedSMILES);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Method for the toolkit to convert a multi-component molecule object to
	 * multiple single component objects
	 * 
	 * @param multiCompMol
	 *            The multicomponent molecule object
	 * @return An array of the original components
	 * @throws ToolkitException
	 */
	protected abstract T[] getComponentsFromMultiComponent(T multiCompMol) throws ToolkitException;

	/**
	 * Method for the toolkit to check whether a molecule object has multiple
	 * components
	 * 
	 * @param mol
	 *            The molecule to check
	 * @return <code>true</code> if the molecule contains more than one
	 *         component
	 */
	protected abstract boolean isMultiComponent(T mol);

	/**
	 * Method for toolkit to return correct {@link AbstractLeaf} implementation
	 * from component object
	 * 
	 * @param comp
	 *            The molecule component
	 * @return The toolkit {@link AbstractLeaf} implementation
	 * @throws ToolkitException
	 */
	protected abstract AbstractLeaf<T> createLeafFromMolecule(T comp) throws ToolkitException;

	/**
	 * Method for toolkit to return correct {@link AbstractValue} implementation
	 * from component object
	 * 
	 * @param comp
	 *            The molecule component
	 * @return The toolkit {@link AbstractValue} implementation
	 * @throws ToolkitException
	 */
	protected abstract AbstractFragmentValue<T> createValueFromMolecule(T value)
			throws ToolkitException;

	/**
	 * @return The correct empty fragment key implementation for the toolkit,
	 *         for the individual Leaf components to be added
	 */
	protected abstract AbstractFragmentKey<T> createFragmentKey();

	/**
	 * @return The fragment key component
	 */
	public AbstractFragmentKey<T> getKey() {
		return m_key;
	}

	/**
	 * @return The fragment value component
	 */
	public AbstractFragmentValue<T> getValue() {
		return m_value;
	}

	/**
	 * @return The number of cuts the fragmentation represents
	 */
	public int getNumCuts() {
		return numCuts;
	}

	/**
	 * @return The canonicalised SMILES string
	 */
	public String getCanonicalSMILES() {
		return canonicalisedSMILES;
	}

	/**
	 * @return the rendering
	 */
	public DataCell getRenderingCell() {
		return rendering;
	}

	/**
	 * @param rendering
	 *            the rendering to set
	 */
	public void setRenderingCell(DataCell rendering) {
		this.rendering = rendering;
	}

	@Override
	public int compareTo(AbstractMulticomponentFragmentationParser<T> o) {
		// Initially, we sort by increasing number of cuts
		if (numCuts > o.numCuts) {
			return 1;
		} else if (numCuts < o.numCuts) {
			return -1;
		} else {
			// Same number of cuts, so we compare the strings
			return canonicalisedSMILES.compareTo(o.canonicalisedSMILES);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((canonicalisedSMILES == null) ? 0 : canonicalisedSMILES.hashCode());
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
		AbstractMulticomponentFragmentationParser<T> other =
				(AbstractMulticomponentFragmentationParser<T>) obj;
		if (canonicalisedSMILES == null) {
			if (other.canonicalisedSMILES != null) {
				return false;
			}
		} else if (!canonicalisedSMILES.equals(other.canonicalisedSMILES)) {
			return false;
		}
		return true;
	}

}
