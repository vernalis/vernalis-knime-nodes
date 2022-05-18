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
package com.vernalis.knime.flowcontrol.nodes.db.endcase;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.database.DatabasePortObject;

import com.vernalis.knime.flowcontrol.nodes.abstrct.endswitch.AbstractEndSwitchNodeDialog;
import com.vernalis.knime.flowcontrol.nodes.abstrct.endswitch.AbstractEndSwitchNodeModel;

/**
 * <code>NodeFactory</code> for the "FlowVarEndIf" Node. Flow Variable End If
 * node to collect inactive and active branches and return an active port
 * 
 * @author "Stephen Roughley  knime@vernalis.com"
 */
@Deprecated
public class DatabaseEndCaseNodeFactory extends
		NodeFactory<AbstractEndSwitchNodeModel> {

	/** The number of ports. */
	int m_ports = 3;

	/** The port type. */
	PortType m_portType = DatabasePortObject.TYPE;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractEndSwitchNodeModel createNodeModel() {
		return new AbstractEndSwitchNodeModel(m_portType, m_ports);
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
	public NodeView<AbstractEndSwitchNodeModel> createNodeView(
			final int viewIndex, final AbstractEndSwitchNodeModel nodeModel) {
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
		return new AbstractEndSwitchNodeDialog();
	}

}
