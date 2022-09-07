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
package com.vernalis.knime.testing.nodes.missingvals;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * NodeFactory implementation for the Empty Column Test node
 * 
 * @since 07-Sep-2022
 * @since v1.36.0

 * @author S Roughley
 *
 */
public class EmptyColumnTestNodeFactory
		extends NodeFactory<EmptyColumnTestNodeModel> {

	@Override
	public EmptyColumnTestNodeModel createNodeModel() {
		return new EmptyColumnTestNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<EmptyColumnTestNodeModel> createNodeView(int viewIndex,
			EmptyColumnTestNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new EmptyColumnTestNodeDialogPane();
	}

}
