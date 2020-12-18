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
package com.vernalis.pdbconnector2.nodes.execute;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * {@link NodeFactory} implementation for the PDB Connector Query Executer node
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class PdbConnector2QueryExecutionNodeFactory
		extends NodeFactory<PdbConnector2QueryExecutionNodeModel> {

	/**
	 * Constructor
	 */
	public PdbConnector2QueryExecutionNodeFactory() {
	}

	@Override
	public PdbConnector2QueryExecutionNodeModel createNodeModel() {
		return new PdbConnector2QueryExecutionNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<PdbConnector2QueryExecutionNodeModel> createNodeView(
			int viewIndex, PdbConnector2QueryExecutionNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new PdbConnector2QueryExecutionNodeDialog();
	}

}
