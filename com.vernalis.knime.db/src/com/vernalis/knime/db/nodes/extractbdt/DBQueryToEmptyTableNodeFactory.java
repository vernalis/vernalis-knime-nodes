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
package com.vernalis.knime.db.nodes.extractbdt;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * {@link NodeFactory} implementation for the DB Query to Empty Table node
 * 
 * @author S Roughley
 *
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public class DBQueryToEmptyTableNodeFactory
		extends NodeFactory<DBQueryToEmptyTableNodeModel> {

	@Override
	public DBQueryToEmptyTableNodeModel createNodeModel() {
		return new DBQueryToEmptyTableNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<DBQueryToEmptyTableNodeModel> createNodeView(int viewIndex,
			DBQueryToEmptyTableNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return false;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return null;
	}

}
