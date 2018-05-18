/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
 ******************************************************************************/
package com.vernalis.knime.chem.speedysmiles.nodes.count.abstrct;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import com.vernalis.knime.chem.speedysmiles.helpers.CalculatedProperty;

/**
 * <code>NodeFactory</code> for the SpeedySMILES count, using an enum
 * implementation of {@link CalculatedProperty}
 *
 * @author S Roughley
 */
public class AbstractSpeedySmilesEnumBasedCountNodeFactory<T extends Enum<T> & CalculatedProperty<Integer>>
		extends NodeFactory<AbstractSpeedySmilesEnumBasedCountNodeModel<T, Integer>> {
	protected final T[] elements;

	/**
	 * Node Factory constructor
	 * 
	 * @param elements
	 *            The properties to calculate
	 */
	public AbstractSpeedySmilesEnumBasedCountNodeFactory(T[] elements) {
		super();
		this.elements = elements;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractSpeedySmilesEnumBasedCountNodeModel<T, Integer> createNodeModel() {
		return new AbstractSpeedySmilesEnumBasedCountNodeModel<>(elements);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNrNodeViews() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<AbstractSpeedySmilesEnumBasedCountNodeModel<T, Integer>> createNodeView(
			final int viewIndex,
			final AbstractSpeedySmilesEnumBasedCountNodeModel<T, Integer> nodeModel) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new AbstractSpeedySmilesEnumBasedCountNodeDialog<>(elements);
	}

}
