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
package com.vernalis.pdbconnector2.nodes.totable;

import java.util.Optional;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.pdbconnector2.ports.RCSBQueryPortObject;

/**
 * The {@link ConfigurableNodeFactory} implementation for the 'Queries to Table'
 * node
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class QueriesToTableNodeFactory
		extends ConfigurableNodeFactory<QueriesToTableNodeModel> {

	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
		final PortsConfigurationBuilder b = new PortsConfigurationBuilder();
		b.addExtendableInputPortGroup("input",
				ArrayUtils.of(RCSBQueryPortObject.TYPE, 1),
				RCSBQueryPortObject.TYPE);
		b.addFixedOutputPortGroup("Output", BufferedDataTable.TYPE);
		return Optional.of(b);
	}

	@Override
	protected QueriesToTableNodeModel
			createNodeModel(NodeCreationConfiguration creationConfig) {
		return new QueriesToTableNodeModel(
				creationConfig.getPortConfig().get());
	}

	@Override
	protected NodeDialogPane
			createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		return new QueriesToTableNodeDialog();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<QueriesToTableNodeModel> createNodeView(int viewIndex,
			QueriesToTableNodeModel nodeModel) {
		throw new IndexOutOfBoundsException();
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

}
