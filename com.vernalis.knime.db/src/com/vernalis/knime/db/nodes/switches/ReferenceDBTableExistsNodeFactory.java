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

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.database.port.DBSessionPortObject;

/**
 * {@link NodeFactory} implementation for the Database Table Exists node
 * 
 * @since 07-Sep-2022
 * @since v1.36.0

 * @author S Roughley
 *
 */
public class ReferenceDBTableExistsNodeFactory
		extends ConfigurableNodeFactory<ReferenceDBTableExistsNodeModel> {

	private static final PortType[] portTypes =
			PortTypeRegistry.getInstance().availablePortTypes().stream()
					.filter(pt -> !pt.isHidden() && !pt.isOptional())
					.toArray(PortType[]::new);

	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
		PortsConfigurationBuilder pcb = new PortsConfigurationBuilder();
		pcb.addExchangeableInputPortGroup("Switchable input",
				BufferedDataTable.TYPE, portTypes);
		pcb.addFixedInputPortGroup("DB Session Connection",
				DBSessionPortObject.TYPE);
		// We add 2 fixed outputs. We will ignore the type of these and make
		// them match the switchable input
		pcb.addFixedOutputPortGroup("DB Table Exists", BufferedDataTable.TYPE);
		pcb.addFixedOutputPortGroup("DB Table Does not Exist",
				BufferedDataTable.TYPE);
		return Optional.of(pcb);
	}

	@Override
	protected ReferenceDBTableExistsNodeModel
			createNodeModel(NodeCreationConfiguration creationConfig) {
		return new ReferenceDBTableExistsNodeModel(creationConfig);
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<ReferenceDBTableExistsNodeModel> createNodeView(
			int viewIndex, ReferenceDBTableExistsNodeModel nodeModel) {
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
