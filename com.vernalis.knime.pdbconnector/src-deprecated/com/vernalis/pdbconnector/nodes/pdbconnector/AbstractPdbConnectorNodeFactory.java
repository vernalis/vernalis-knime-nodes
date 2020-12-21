/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd, based on earlier PDB Connector work.
 * 
 * Copyright (c) 2012, 2014 Vernalis (R&D) Ltd and Enspiral Discovery Limited
 * 
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
package com.vernalis.pdbconnector.nodes.pdbconnector;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import com.vernalis.pdbconnector.config.PdbConnectorConfig2;

/**
 * Abstract Node Factory class for the PDB Connector node family. Nodes which
 * build or execute a query have views for the XML query and a logical
 * representation of the XML query
 * 
 * @author S.Roughley
 *
 */
@Deprecated
public abstract class AbstractPdbConnectorNodeFactory
		extends NodeFactory<AbstractPdbConnectorNodeModel> {

	protected boolean m_hasQueryBuilder, m_runQuery, m_runReport;

	/**
	 * Constructor
	 * 
	 * @param hasQueryBuilder
	 *            Does the node have a query builder?
	 * @param runQuery
	 *            Does the node execute a query?
	 * @param runReport
	 *            Does the node execute a report?
	 */
	public AbstractPdbConnectorNodeFactory(boolean hasQueryBuilder,
			boolean runQuery, boolean runReport) {
		m_hasQueryBuilder = hasQueryBuilder;
		m_runQuery = runQuery;
		m_runReport = runReport;
	}

	@Override
	public AbstractPdbConnectorNodeModel createNodeModel() {
		return new AbstractPdbConnectorNodeModel(
				PdbConnectorConfig2.getInstance(), m_hasQueryBuilder,
				m_runQuery, m_runReport);
	}

	@Override
	protected int getNrNodeViews() {
		return m_hasQueryBuilder || m_runQuery ? 2 : 0;
	}

	@Override
	public NodeView<AbstractPdbConnectorNodeModel> createNodeView(int viewIndex,
			AbstractPdbConnectorNodeModel nodeModel) {
		if (m_hasQueryBuilder || m_runQuery) {
			if (viewIndex == 0) {
				return new AbstractXMLAdvancedQueryNodeView<>(nodeModel);
			} else if (viewIndex == 1) {
				return new AbstractXMLAdvancedQueryLogicalNodeView<>(nodeModel);
			}
		}
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new AbstractPdbConnectorNodeDialog(
				PdbConnectorConfig2.getInstance(), m_hasQueryBuilder,
				m_runQuery, m_runReport);
	}

}
