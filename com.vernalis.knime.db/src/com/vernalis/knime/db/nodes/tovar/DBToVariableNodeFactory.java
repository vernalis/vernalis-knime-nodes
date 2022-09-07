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
/**
 * 
 */
package com.vernalis.knime.db.nodes.tovar;

import java.util.Optional;

import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.database.port.DBDataPortObject;
import org.knime.database.port.DBSessionPortObject;

/**
 * @author S Roughley
 *
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public class DBToVariableNodeFactory
		extends ConfigurableNodeFactory<DBToVariableNodeModel> {

	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
		PortsConfigurationBuilder builder = new PortsConfigurationBuilder();
		builder.addExchangeableInputPortGroup("Input", DBSessionPortObject.TYPE,
				DBSessionPortObject.TYPE, DBDataPortObject.TYPE);
		builder.addFixedOutputPortGroup("Output", FlowVariablePortObject.TYPE);
		return Optional.of(builder);
	}

	@Override
	protected DBToVariableNodeModel
			createNodeModel(NodeCreationConfiguration creationConfig) {
		return new DBToVariableNodeModel(creationConfig);
	}

	@Override
	protected NodeDialogPane
			createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		return null;
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<DBToVariableNodeModel> createNodeView(int viewIndex,
			DBToVariableNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return false;
	}

}
