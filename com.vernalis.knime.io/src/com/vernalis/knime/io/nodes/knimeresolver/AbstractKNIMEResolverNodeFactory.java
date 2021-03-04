/*******************************************************************************
 * Copyright (c) 2021, Vernalis (R&D) Ltd
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
package com.vernalis.knime.io.nodes.knimeresolver;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * {@link NodeFactory} for the KNIME URI Resolver nodes
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.30.0
 */
public abstract class AbstractKNIMEResolverNodeFactory
		extends NodeFactory<KNIMEResolverNodeModel> {

	private final boolean flowVar;

	/**
	 * @param flowVar
	 *            whether the node is the flow variable or table variant
	 */
	AbstractKNIMEResolverNodeFactory(boolean flowVar) {
		this.flowVar = flowVar;
	}

	@Override
	public KNIMEResolverNodeModel createNodeModel() {
		return new KNIMEResolverNodeModel(flowVar);
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<KNIMEResolverNodeModel> createNodeView(int viewIndex,
			KNIMEResolverNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new KNIMEResolverNodeDialog(flowVar);
	}

}
