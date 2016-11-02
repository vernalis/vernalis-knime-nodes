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
package com.vernalis.knime.perfmon.nodes.timing.abstrct;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "TimingStart" Node. Loop start for execution
 * timing
 *
 * @author S. Roughley
 */
public class AbstractPerfMemMonTimingStartNodeFactory
		extends NodeFactory<AbstractMemMonPerfLoopStartNodeModel> {

	protected final int numPorts;

	/**
	 * @param numPorts
	 */
	public AbstractPerfMemMonTimingStartNodeFactory(int numPorts) {
		super();
		this.numPorts = numPorts;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractMemMonPerfLoopStartNodeModel createNodeModel() {
		return new AbstractMemMonPerfLoopStartNodeModel(BufferedDataTable.TYPE, numPorts);
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
	public NodeView<AbstractMemMonPerfLoopStartNodeModel> createNodeView(final int viewIndex,
			final AbstractMemMonPerfLoopStartNodeModel nodeModel) {
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
		return new AbstractMemMonPerfLoopStartNodeDialog();
	}

}
