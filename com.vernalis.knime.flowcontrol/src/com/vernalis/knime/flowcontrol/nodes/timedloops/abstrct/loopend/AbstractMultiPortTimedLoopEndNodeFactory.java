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
package com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopend;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import com.vernalis.knime.flowcontrol.nodes.loops.abstrct.multiportloopend.AbstractMultiPortLoopEndNodeDialog;

/**
 * Abstract <code>NodeFactory</code> for the Timed Loop End nodes Node. Loop End
 * node for timed loops, exposing unprocessed rows in the last output port
 * 
 * @author "Stephen Roughley  knime@vernalis.com"
 */
public abstract class AbstractMultiPortTimedLoopEndNodeFactory extends
		NodeFactory<AbstractMultiPortTimedLoopEndNodeModel> {

	/** The number of ports (there will be one extra output port). */
	final int m_numPorts;

	/** True if the 2nd and upwards input ports are optional. */
	final boolean m_hasOptionalInputs;

	/**
	 * Constructor with default assumption that there are optional input ports.
	 * 
	 * @param numPorts
	 *            The number of input ports
	 */
	public AbstractMultiPortTimedLoopEndNodeFactory(final int numPorts) {
		this.m_numPorts = numPorts;
		this.m_hasOptionalInputs = true;
	}

	/**
	 * Constructor allowing specification of optional or required ports.
	 * 
	 * @param numPorts
	 *            The number of input ports
	 * @param hasOptionalPorts
	 *            False if all input ports are required
	 */
	public AbstractMultiPortTimedLoopEndNodeFactory(final int numPorts,
			final boolean hasOptionalPorts) {
		this.m_numPorts = numPorts;
		this.m_hasOptionalInputs = hasOptionalPorts;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractMultiPortTimedLoopEndNodeModel createNodeModel() {
		return new AbstractMultiPortTimedLoopEndNodeModel(m_numPorts,
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
	public NodeView<AbstractMultiPortTimedLoopEndNodeModel> createNodeView(
			final int viewIndex,
			final AbstractMultiPortTimedLoopEndNodeModel nodeModel) {
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
