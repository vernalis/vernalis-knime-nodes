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
package com.vernalis.knime.flowcontrol.nodes.switches.ifcaseswitch;

import java.util.Optional;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;

import static com.vernalis.knime.flowcontrol.nodes.switches.ConfigurableSwitchNodeConstants.INPUT_GROUP;
import static com.vernalis.knime.flowcontrol.nodes.switches.ConfigurableSwitchNodeConstants.MORE_OUTPUTS_GROUP;
import static com.vernalis.knime.flowcontrol.nodes.switches.ConfigurableSwitchNodeConstants.portTypes;
import static com.vernalis.knime.flowcontrol.nodes.switches.ifcaseswitch.ConfigurableIfCaseSwitchNodeDialog.FIRST_OUTPUT;
import static com.vernalis.knime.flowcontrol.nodes.switches.ifcaseswitch.ConfigurableIfCaseSwitchNodeDialog.SECOND_OUTPUT;

/**
 * Node Factory for the Configurable IF/Case Switch node
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class ConfigurableIfCaseSwitchNodeFactory
		extends ConfigurableNodeFactory<ConfigurableIfCaseSwitchNodeModel> {

	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
		PortsConfigurationBuilder b = new PortsConfigurationBuilder();

		// We add exactly one input - which is exchangeable
		// If this is a FlowVariable, it will be made optional, otherwise it is
		// the type used for all outputs which are not defined 'variable'
		b.addExchangeableInputPortGroup(INPUT_GROUP, BufferedDataTable.TYPE,
				portTypes);

		// Now we add an exchangeable output port which forms the required first
		// output
		// This can be either Flow Variable or 'Data', which means actually
		// whatever the input is (handled in the NodeModel)
		b.addExchangeableOutputPortGroup(FIRST_OUTPUT, BufferedDataTable.TYPE,
				BufferedDataTable.TYPE, FlowVariablePortObject.TYPE);
		// And the second..
		b.addExchangeableOutputPortGroup(SECOND_OUTPUT, BufferedDataTable.TYPE,
				BufferedDataTable.TYPE, FlowVariablePortObject.TYPE);

		// Now we add an extendable group of ports, each of which is either
		// 'Data' or Flow Variable, as above
		b.addExtendableOutputPortGroup(MORE_OUTPUTS_GROUP, new PortType[0],
				BufferedDataTable.TYPE, FlowVariablePortObject.TYPE);

		return Optional.of(b);
	}

	@Override
	protected ConfigurableIfCaseSwitchNodeModel
			createNodeModel(NodeCreationConfiguration creationConfig) {
		return new ConfigurableIfCaseSwitchNodeModel(creationConfig);
	}

	@Override
	protected NodeDialogPane
			createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		return new ConfigurableIfCaseSwitchNodeDialog(creationConfig);
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<ConfigurableIfCaseSwitchNodeModel> createNodeView(
			int viewIndex, ConfigurableIfCaseSwitchNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

}
