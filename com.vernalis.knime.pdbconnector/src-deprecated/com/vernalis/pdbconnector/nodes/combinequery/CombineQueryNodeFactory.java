/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.pdbconnector.nodes.combinequery;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * Factory Class for the Combine Query Node
 */
@Deprecated
public class CombineQueryNodeFactory
		extends NodeFactory<CombineQueryNodeModel> {

	/**
	 * Constructor for the node factory class
	 */
	public CombineQueryNodeFactory() {

	}

	@Override
	public CombineQueryNodeModel createNodeModel() {
		return new CombineQueryNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new CombineQueryNodeDialog();
	}

	@Override
	public NodeView<CombineQueryNodeModel> createNodeView(int viewIndex,
			CombineQueryNodeModel nodeModel) {
		return null;
	}

}
