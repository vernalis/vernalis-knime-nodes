/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.nodes.combine;

import java.util.Optional;

import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.pdbconnector2.ports.RCSBQueryPortObject;

/**
 * {@link ConfigurableNodeFactory} implementation for the PDB Connector Combine
 * Queries node
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class CombineQueriesNodeFactory
		extends ConfigurableNodeFactory<CombineQueriesNodeModel> {

	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
		final PortsConfigurationBuilder b = new PortsConfigurationBuilder();
		b.addExtendableInputPortGroup("input",
				ArrayUtils.of(RCSBQueryPortObject.TYPE, 2),
				RCSBQueryPortObject.TYPE);
		b.addFixedOutputPortGroup("Combined Query", RCSBQueryPortObject.TYPE);
		return Optional.of(b);
	}

	@Override
	protected CombineQueriesNodeModel
			createNodeModel(NodeCreationConfiguration creationConfig) {
		return new CombineQueriesNodeModel(
				creationConfig.getPortConfig().get());
	}

	@Override
	protected NodeDialogPane
			createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		return new CombineQueriesNodeDialog();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<CombineQueriesNodeModel> createNodeView(int viewIndex,
			CombineQueriesNodeModel nodeModel) {
		throw new IndexOutOfBoundsException();
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

}
