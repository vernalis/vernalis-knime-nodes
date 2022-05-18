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
package com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch;

import java.util.Optional;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;

import com.vernalis.knime.flowcontrol.nodes.switches.ConfigurableSwitchNodeConstants;

import static com.vernalis.knime.flowcontrol.nodes.switches.ConfigurableSwitchNodeConstants.portTypes;

/**
 * Node Factory for the Configurable IF/Case Switch (Flow Variable Value) node
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class ConfigurableFvvalIfSwitchNodeFactory
		extends ConfigurableNodeFactory<ConfigurableFvvalIfSwitchNodeModel> {

	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
		PortsConfigurationBuilder b = new PortsConfigurationBuilder();

		// We add exactly one input - which is exchangeable
		// If this is a FlowVariable, it will be made optional, otherwise it is
		// the type used for all outputs which are not defined 'variable'
		b.addExchangeableInputPortGroup(
				ConfigurableSwitchNodeConstants.INPUT_GROUP,
				BufferedDataTable.TYPE, portTypes);

		// Now we add an exchangeable output port which forms the required first
		// 'If' condition 'true' port
		// This can be either Flow Variable or 'Data', which means actually
		// whatever the input is (handled in the NodeModel)
		b.addExchangeableOutputPortGroup(
				ConfigurableSwitchNodeConstants.OUTPUT_GROUP,
				BufferedDataTable.TYPE, BufferedDataTable.TYPE,
				FlowVariablePortObject.TYPE);

		// Now we add an extendable group of ports, each of which is either
		// 'Data' or Flow Variable, as above
		b.addExtendableOutputPortGroup(
				ConfigurableSwitchNodeConstants.MORE_OUTPUTS_GROUP,
				new PortType[0], BufferedDataTable.TYPE,
				FlowVariablePortObject.TYPE);

		// Finally a exchangeable output port for the 'Else' result - types
		// again as
		// above
		b.addExchangeableOutputPortGroup(
				ConfigurableSwitchNodeConstants.ELSE_OUTPUT_GROUP,
				BufferedDataTable.TYPE, BufferedDataTable.TYPE,
				FlowVariablePortObject.TYPE);
		return Optional.of(b);
	}

	@Override
	protected ConfigurableFvvalIfSwitchNodeModel
			createNodeModel(NodeCreationConfiguration creationConfig) {
		return new ConfigurableFvvalIfSwitchNodeModel(creationConfig);
	}

	@Override
	protected NodeDialogPane
			createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		return new ConfigurableFvvalIfSwitchNodeDialog(creationConfig);
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<ConfigurableFvvalIfSwitchNodeModel> createNodeView(
			int viewIndex, ConfigurableFvvalIfSwitchNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

}
