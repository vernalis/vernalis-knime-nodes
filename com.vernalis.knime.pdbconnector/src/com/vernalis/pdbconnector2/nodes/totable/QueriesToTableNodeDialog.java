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

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * The {@link DefaultNodeSettingsPane} implementation for the 'Queries to Table'
 * node
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class QueriesToTableNodeDialog extends DefaultNodeSettingsPane {

	private static final String RCSB_PDB_ADVANCED_QUERY_DEFINITION =
			"RCSB PDB Advanced Query Definition";
	private static final String TREAT_MISSING_INPORT_CONNECTIONS_AS_EMPTY_QUERIES =
			"Treat Missing inport connections as empty queries?";
	private static final String NO_QUERY_AT_INPORT_BEHAVIOUR =
			"No Query at inport behaviour";
	private static final String COLUMN_NAME = "Column Name";

	/**
	 * Constructor
	 */
	public QueriesToTableNodeDialog() {
		super();
		addDialogComponent(new DialogComponentString(createColumnNameModel(),
				COLUMN_NAME, true, 30));
		addDialogComponent(new DialogComponentButtonGroup(
				createNoQueryBehaviourModel(), NO_QUERY_AT_INPORT_BEHAVIOUR,
				false, NoQueryBehaviour.values()));
		addDialogComponent(new DialogComponentBoolean(
				createTreatMissingPortAsEmptyQueryModel(),
				TREAT_MISSING_INPORT_CONNECTIONS_AS_EMPTY_QUERIES));
	}

	/**
	 * @return The model for the 'Treat missing inport as empty query' setting
	 */
	static SettingsModelBoolean createTreatMissingPortAsEmptyQueryModel() {
		return new SettingsModelBoolean(
				TREAT_MISSING_INPORT_CONNECTIONS_AS_EMPTY_QUERIES, false);
	}

	/**
	 * @return The model for the 'No query at inport behaviour' setting
	 */
	static SettingsModelString createNoQueryBehaviourModel() {
		return new SettingsModelString(NO_QUERY_AT_INPORT_BEHAVIOUR,
				NoQueryBehaviour.getDefault().getActionCommand());
	}

	/**
	 * @return The model for the 'Column Name' setting
	 */
	static SettingsModelString createColumnNameModel() {
		return new SettingsModelString(COLUMN_NAME,
				RCSB_PDB_ADVANCED_QUERY_DEFINITION);
	}

}
