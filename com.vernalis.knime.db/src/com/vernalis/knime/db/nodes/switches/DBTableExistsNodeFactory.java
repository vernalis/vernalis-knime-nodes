/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.db.nodes.switches;

import java.util.Optional;

import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.database.port.DBSessionPortObject;

/**
 * {@link NodeFactory} implementation for the Configurable Database Table Exists
 * node
 * 
 * @since 07-Sep-2022
 * @since v1.36.0

 * @author S Roughley
 *
 */
public class DBTableExistsNodeFactory
		extends ConfigurableNodeFactory<DBTableExistsNodeModel> {

	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
		PortsConfigurationBuilder pcb = new PortsConfigurationBuilder();
		pcb.addFixedInputPortGroup("Input DB Session",
				DBSessionPortObject.TYPE);
		pcb.addExchangeableOutputPortGroup("Table Exists",
				DBSessionPortObject.TYPE, DBSessionPortObject.TYPE,
				FlowVariablePortObject.TYPE);
		pcb.addExchangeableOutputPortGroup("Table Does Not Exist",
				DBSessionPortObject.TYPE, DBSessionPortObject.TYPE,
				FlowVariablePortObject.TYPE);
		return Optional.of(pcb);
	}

	@Override
	protected DBTableExistsNodeModel
			createNodeModel(NodeCreationConfiguration creationConfig) {
		return new DBTableExistsNodeModel(creationConfig);
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<DBTableExistsNodeModel> createNodeView(int viewIndex,
			DBTableExistsNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane
			createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		return new DBTableExistsNodeDialog();
	}

}
