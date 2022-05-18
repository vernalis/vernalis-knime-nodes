/*******************************************************************************
 * Copyright (c) 2014,2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.flowcontrol.nodes.fv.endif;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import com.vernalis.knime.flowcontrol.nodes.abstrct.fvendswitch.AbstractFlowVarEndSwitchNodeModel;

/**
 * <code>NodeFactory</code> for the "FlowVarEndIf" Node. Flow Variable End If
 * node to collect inactive and active branches and return an active port
 * 
 * @author "Stephen Roughley  knime@vernalis.com"
 */
@Deprecated
public class FlowVarEndIfNodeFactory extends
		NodeFactory<AbstractFlowVarEndSwitchNodeModel> {

	/** The number of ports. */
	int m_ports = 2;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractFlowVarEndSwitchNodeModel createNodeModel() {
		return new AbstractFlowVarEndSwitchNodeModel(m_ports);
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
	public NodeView<AbstractFlowVarEndSwitchNodeModel> createNodeView(
			final int viewIndex,
			final AbstractFlowVarEndSwitchNodeModel nodeModel) {
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
