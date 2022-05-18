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
package com.vernalis.knime.flowcontrol.nodes.switches.endifcase;

import java.util.Optional;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

/**
 * NodeFactory implementation for the Configurable End IF/Case node
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class EndIfCaseNodeFactory
		extends ConfigurableNodeFactory<EndIfCaseNodeModel> {

	private static final PortType[] portTypes =
			PortTypeRegistry.getInstance().availablePortTypes().stream()
					.filter(pt -> !pt.isHidden() && !pt.isOptional())
					.toArray(PortType[]::new);

	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
		PortsConfigurationBuilder b = new PortsConfigurationBuilder();

		// We add exactly one output - which is exchangeable
		b.addExchangeableOutputPortGroup("Output", BufferedDataTable.TYPE,
				portTypes);

		// Now we add two fixed inputs (these will be made to the same as the
		// output in the NodeModel)
		b.addFixedInputPortGroup("Fixed Inputs", BufferedDataTable.TYPE,
				BufferedDataTable.TYPE);

		// Now we add an extendable group inputs - again these will match the
		// output
		b.addExtendableInputPortGroup("More Inputs", new PortType[0],
				BufferedDataTable.TYPE);

		return Optional.of(b);
	}

	@Override
	protected EndIfCaseNodeModel
			createNodeModel(NodeCreationConfiguration creationConfig) {
		return new EndIfCaseNodeModel(creationConfig);
	}

	@Override
	protected NodeDialogPane
			createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		return new EndIfCaseNodeDialog(creationConfig);
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<EndIfCaseNodeModel> createNodeView(int viewIndex,
			EndIfCaseNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

}
