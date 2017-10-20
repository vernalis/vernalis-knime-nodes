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
 * <code>NodeFactory</code> for the "Timing End with memory monitoring" loop end
 * nodes
 *
 * @author S. Roughley
 */
public class AbstractPerfMemMonTimingEndNodeFactory
		extends NodeFactory<AbstractMemMonPerfLoopEndNodeModel> {
	private final int numPorts;

	/**
	 * @param numPorts
	 */
	public AbstractPerfMemMonTimingEndNodeFactory(int numPorts) {
		super();
		this.numPorts = numPorts;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractMemMonPerfLoopEndNodeModel createNodeModel() {
		return new AbstractMemMonPerfLoopEndNodeModel(BufferedDataTable.TYPE, numPorts);
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
	public NodeView<AbstractMemMonPerfLoopEndNodeModel> createNodeView(final int viewIndex,
			final AbstractMemMonPerfLoopEndNodeModel nodeModel) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {
		return null;
	}

}
