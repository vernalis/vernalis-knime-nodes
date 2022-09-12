/*******************************************************************************
 * Copyright (c) 2018, 2020, Vernalis (R&D) Ltd
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
package com.vernalis.knime.database.nodes.switches;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.port.database.DatabaseConnectionPortObject;

/**
 * {@link NodeFactory} implementation for the Database Table Exists node
 * 
 * @author s.roughley
 *
 */
public class DatabaseTableExistsNodeFactory
		extends NodeFactory<DatabaseTableExistsNodeModel> {

	@Override
	public DatabaseTableExistsNodeModel createNodeModel() {
		return new DatabaseTableExistsNodeModel(
				DatabaseConnectionPortObject.TYPE);
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<DatabaseTableExistsNodeModel> createNodeView(int viewIndex,
			DatabaseTableExistsNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new DatabaseTableExistsNodeDialog();
	}

}
