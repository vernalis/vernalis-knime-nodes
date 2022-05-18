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
package com.vernalis.knime.flowcontrol.nodes.loops.abstrct.multiportloopend;

import java.util.Optional;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.port.PortType;

/**
 * <code>NodeFactory</code> for the "MultiPortLoopEnd" Node. Loop end node to
 * handle optional input ports n
 * 
 * @author Stephen Roughley knime@vernalis.com
 * @since v1.29.0
 */
public class ConfigurableMultiPortLoopEndNodeFactory
		extends ConfigurableNodeFactory<AbstractMultiPortLoopEndNodeModel> {

	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
		PortsConfigurationBuilder b = new PortsConfigurationBuilder();
		b.addExtendablePortGroup("Loop End Table",
				new PortType[] { BufferedDataTable.TYPE },
				BufferedDataTable.TYPE);
		return Optional.of(b);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractMultiPortLoopEndNodeModel
			createNodeModel(NodeCreationConfiguration creationConfig) {
		return new AbstractMultiPortLoopEndNodeModel(
				creationConfig.getPortConfig().get());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNrNodeViews() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<AbstractMultiPortLoopEndNodeModel> createNodeView(
			final int viewIndex,
			final AbstractMultiPortLoopEndNodeModel nodeModel) {
		return new AbstractMultiPortLoopEndNodeView(nodeModel);
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
	public NodeDialogPane
			createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		return new AbstractMultiPortLoopEndNodeDialog(
				creationConfig.getPortConfig().get());
	}

}
