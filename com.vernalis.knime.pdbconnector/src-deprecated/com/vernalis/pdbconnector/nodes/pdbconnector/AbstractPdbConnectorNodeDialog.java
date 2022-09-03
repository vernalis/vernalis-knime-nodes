/*******************************************************************************
 * Copyright (c) 2016, 2020 Vernalis (R&D) Ltd, based on earlier PDB Connector work.
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

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentLabel;

import com.vernalis.pdbconnector.config.PdbConnectorConfig2;

/**
 * The node dialog pane for the PDB Connector node family
 * 
 */
@Deprecated
public class AbstractPdbConnectorNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * Instantiates a new pdb connector node dialog.
	 * 
	 * @param config
	 *            the configuration
	 * @param hasQueryBuilder
	 *            Does the node dialog include a query builder?
	 * @param runQuery
	 *            Does the node execute a query? (If it does, and the node does
	 *            not have a query builder, then a text box is included for the
	 *            XML Query to be entered)
	 * @param runReport
	 *            Does the node run a report?
	 * 
	 * @throws IllegalArgumentException
	 *             if an invalid combination of boolean parameters is supplied
	 */
	public AbstractPdbConnectorNodeDialog(final PdbConnectorConfig2 config,
			boolean hasQueryBuilder, boolean runQuery, boolean runReport)
			throws IllegalArgumentException {
		super();
		addDialogComponent(
				new DialogComponentLabel(PdbConnectorConfig2.ERROR_MSG));
	}

}
