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
package com.vernalis.knime.flowcontrol.nodes.switches.emptytable;

import java.util.Optional;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;

import com.vernalis.knime.flowcontrol.nodes.switches.ConfigurableSwitchNodeConstants;

import static com.vernalis.knime.flowcontrol.nodes.switches.ConfigurableSwitchNodeConstants.portTypes;

/**
 * NodeFactory class for the 'Configurable Empty Table Switch' node factory
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class EmptyTableSwitchNodeFactory
		extends ConfigurableNodeFactory<EmptyTableSwitchNodeModel> {

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<EmptyTableSwitchNodeModel> createNodeView(int viewIndex,
			EmptyTableSwitchNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return false;
	}

	@Override
	protected NodeDialogPane
			createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		return null;
	}

	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {

		PortsConfigurationBuilder b = new PortsConfigurationBuilder();

		// We add exactly one input - which is fixed
		b.addFixedPortGroup("First Port", BufferedDataTable.TYPE);

		// Now we add a second, exchangeable brogup
		b.addExchangeablePortGroup(ConfigurableSwitchNodeConstants.INPUT_GROUP,
				BufferedDataTable.TYPE, portTypes);

		return Optional.of(b);
	}

	@Override
	protected EmptyTableSwitchNodeModel
			createNodeModel(NodeCreationConfiguration creationConfig) {
		return new EmptyTableSwitchNodeModel(creationConfig);
	}

}
