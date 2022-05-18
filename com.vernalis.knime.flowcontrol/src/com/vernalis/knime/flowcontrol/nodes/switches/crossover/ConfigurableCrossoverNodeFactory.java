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
package com.vernalis.knime.flowcontrol.nodes.switches.crossover;

import java.util.Arrays;
import java.util.Optional;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;

import static com.vernalis.knime.flowcontrol.nodes.switches.ConfigurableSwitchNodeConstants.portTypes;

/**
 * NodeFactory for the Configurable crossover node
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class ConfigurableCrossoverNodeFactory
		extends ConfigurableNodeFactory<ConfigurableCrossoverNodeModel> {

	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
		PortsConfigurationBuilder b = new PortsConfigurationBuilder();

		// We allow 1 exchangeable port - this will actually govern the type for
		// both inputs and both outputs, which all must be the same type
		// We dont allow Flow Variable ports, as they dont make sense in this
		// context
		b.addExchangeableInputPortGroup("Ports", BufferedDataTable.TYPE,
				Arrays.stream(portTypes).filter(
						pType -> !pType.equals(FlowVariablePortObject.TYPE))
						.toArray(PortType[]::new));

		return Optional.of(b);
	}

	@Override
	protected ConfigurableCrossoverNodeModel
			createNodeModel(NodeCreationConfiguration creationConfig) {
		return new ConfigurableCrossoverNodeModel(creationConfig);
	}

	@Override
	protected NodeDialogPane
			createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		return new ConfigurableCrossoverNodeDialog();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<ConfigurableCrossoverNodeModel> createNodeView(
			int viewIndex, ConfigurableCrossoverNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

}
