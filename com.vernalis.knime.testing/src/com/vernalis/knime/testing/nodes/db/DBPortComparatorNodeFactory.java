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
package com.vernalis.knime.testing.nodes.db;

import java.util.Optional;

import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.database.port.DBDataPortObject;
import org.knime.database.port.DBSessionPortObject;

/**
 * NodeFactory class for the DB Port Comparator node
 * 
 * @since 07-Sep-2022
 * @since v1.36.0

 * @author S Roughley
 *
 */
public class DBPortComparatorNodeFactory
		extends ConfigurableNodeFactory<DBPortComparatorNodeModel> {

	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
		PortsConfigurationBuilder pcb = new PortsConfigurationBuilder();
		// We only put 1 input here, which we use to know what incoming port
		// type we have
		pcb.addExchangeableInputPortGroup("Test Connection",
				DBSessionPortObject.TYPE, DBSessionPortObject.TYPE,
				DBDataPortObject.TYPE);
		return Optional.of(pcb);
	}

	@Override
	protected DBPortComparatorNodeModel
			createNodeModel(NodeCreationConfiguration creationConfig) {
		return new DBPortComparatorNodeModel(creationConfig);
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<DBPortComparatorNodeModel> createNodeView(int viewIndex,
			DBPortComparatorNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane
			createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		return new DBPortComparatorNodeDialog(creationConfig);
	}

}
