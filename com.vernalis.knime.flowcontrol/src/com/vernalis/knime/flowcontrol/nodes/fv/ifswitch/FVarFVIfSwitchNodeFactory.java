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
package com.vernalis.knime.flowcontrol.nodes.fv.ifswitch;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;

import com.vernalis.knime.flowcontrol.nodes.abstrct.ifswitch.AbstractIfSwitchNodeDialog;
import com.vernalis.knime.flowcontrol.nodes.abstrct.ifswitch.AbstractIfSwitchNodeModel;

/**
 * <code>NodeFactory</code> for the "FVarFVIfSwitch" Node. Flow variable if
 * switch
 * 
 * @author "Stephen Roughley  <s.roughley@vernalis.com>"
 */

public class FVarFVIfSwitchNodeFactory extends
		NodeFactory<AbstractIfSwitchNodeModel> {

	/** The port type of the node. */
	PortType m_PortType = FlowVariablePortObject.TYPE;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractIfSwitchNodeModel createNodeModel() {
		return new AbstractIfSwitchNodeModel(m_PortType);
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
	public NodeView<AbstractIfSwitchNodeModel> createNodeView(
			final int viewIndex, final AbstractIfSwitchNodeModel nodeModel) {
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
		return new AbstractIfSwitchNodeDialog();
	}

}
