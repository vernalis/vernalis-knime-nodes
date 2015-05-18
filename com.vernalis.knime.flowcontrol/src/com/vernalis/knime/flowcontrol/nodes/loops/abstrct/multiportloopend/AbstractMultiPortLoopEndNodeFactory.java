/*******************************************************************************
 * Copyright (c) 2014, Vernalis (R&D) Ltd
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
package com.vernalis.knime.flowcontrol.nodes.loops.abstrct.multiportloopend;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "AbstractMultiPortLoopEnd" Node. Loop end
 * node to handle optional input ports n
 * 
 * @author "Stephen Roughley  <s.roughley@vernalis.com>"
 */
public abstract class AbstractMultiPortLoopEndNodeFactory extends
		NodeFactory<AbstractMultiPortLoopEndNodeModel> {

	/** The number of ports. */
	final Integer m_numPorts;

	/** The has optional inputs setting. */
	final boolean m_hasOptionalInputs;

	/**
	 * Instantiates a new abstract multi port loop end node factory.
	 * 
	 * @param numPorts
	 *            the num ports
	 */
	public AbstractMultiPortLoopEndNodeFactory(final int numPorts) {
		this.m_numPorts = numPorts;
		this.m_hasOptionalInputs = true;
	}

	/**
	 * Instantiates a new abstract multi port loop end node factory.
	 * 
	 * @param numPorts
	 *            the num ports
	 * @param hasOptionalPorts
	 *            the has optional ports
	 */
	public AbstractMultiPortLoopEndNodeFactory(final int numPorts,
			final boolean hasOptionalPorts) {
		this.m_numPorts = numPorts;
		this.m_hasOptionalInputs = hasOptionalPorts;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractMultiPortLoopEndNodeModel createNodeModel() {
		return new AbstractMultiPortLoopEndNodeModel(m_numPorts,
				m_hasOptionalInputs);
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
	public NodeView<AbstractMultiPortLoopEndNodeModel> createNodeView(
			final int viewIndex,
			final AbstractMultiPortLoopEndNodeModel nodeModel) {
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
		return new AbstractMultiPortLoopEndNodeDialog(m_numPorts,
				m_hasOptionalInputs);
	}

}
