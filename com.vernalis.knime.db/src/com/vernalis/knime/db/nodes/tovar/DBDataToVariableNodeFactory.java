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
package com.vernalis.knime.db.nodes.tovar;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.database.port.DBDataPortObject;

/**
 * {@link NodeFactory} class for the deprecated (only provided for Database to
 * DB migration) DB Data to Variable node
 * 
 * @author S Roughley
 *
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public class DBDataToVariableNodeFactory
		extends NodeFactory<DBToVariableNodeModel> {

	@Override
	public DBToVariableNodeModel createNodeModel() {
		return new DBToVariableNodeModel(
				new PortType[] { DBDataPortObject.TYPE },
				new PortType[] { FlowVariablePortObject.TYPE });
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<DBToVariableNodeModel> createNodeView(int viewIndex,
			DBToVariableNodeModel nodeModel) {
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
