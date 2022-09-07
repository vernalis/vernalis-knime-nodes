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
package com.vernalis.knime.testing.nodes.database;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.database.DatabasePortObjectSpec;

/**
 * Node Dialog pane for the Database Port Comparator node
 * 
 * @since 07-Sep-2022
 * @since v1.36.0

 * @author S Roughley
 *
 */
public class DatabasePortComparatorNodeDialog extends DefaultNodeSettingsPane {

	private static final String COMPARE_OUTPUT_TABLE_SPECS =
			"Compare output table specs";
	private static final String IGNORE_SQL_COMMENTS = "Ignore SQL Comments";
	private static final String COMPARE_SQL = "Compare SQL";
	private static final String COMPARE_DATABASE_TYPES =
			"Compare database types";
	private static final String COMPARE_USERNAMES = "Compare Usernames";
	private static final String COMPARE_URLS = "Compare URLs";
	private static final String COMPARE_DRIVERS = "Compare drivers";
	private static final String COMPARE_PORT_TYPES = "Compare port types";
	private boolean hasComparableSqlInput;
	private final SettingsModelBoolean cpSqlMdl;
	private final SettingsModelBoolean ignoreSqlCommentMdl;
	private final SettingsModelBoolean cpTableSpecsMdl;

	/**
	 * Constructor
	 */
	public DatabasePortComparatorNodeDialog() {
		addDialogComponent(new DialogComponentBoolean(
				createComparePortTypesModel(), COMPARE_PORT_TYPES));
		addDialogComponent(new DialogComponentBoolean(
				createCompareDriversModel(), COMPARE_DRIVERS));
		addDialogComponent(new DialogComponentBoolean(createCompareURLModel(),
				COMPARE_URLS));
		addDialogComponent(new DialogComponentBoolean(
				createCompareUsernameModel(), COMPARE_USERNAMES));
		addDialogComponent(new DialogComponentBoolean(
				createCompareDbtypeModel(), COMPARE_DATABASE_TYPES));
		cpSqlMdl = createCompareSqlModel();
		cpSqlMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateEnableStatus();

			}
		});
		addDialogComponent(new DialogComponentBoolean(cpSqlMdl, COMPARE_SQL));
		ignoreSqlCommentMdl = createIgnoreSqlCommentsModel();
		addDialogComponent(new DialogComponentBoolean(ignoreSqlCommentMdl,
				IGNORE_SQL_COMMENTS));
		cpTableSpecsMdl = createCompareTableSpecsModel();
		addDialogComponent(new DialogComponentBoolean(cpTableSpecsMdl,
				COMPARE_OUTPUT_TABLE_SPECS));
		updateEnableStatus();
	}

	/**
	 * @return model for the Compare port types setting
	 */
	static SettingsModelBoolean createComparePortTypesModel() {
		return new SettingsModelBoolean(COMPARE_PORT_TYPES, true);
	}

	/**
	 * @return model for the compare drivers setting
	 */
	static SettingsModelBoolean createCompareDriversModel() {
		return new SettingsModelBoolean(COMPARE_DRIVERS, true);
	}

	/**
	 * @return model for the Compare URLs setting
	 */
	static SettingsModelBoolean createCompareURLModel() {
		return new SettingsModelBoolean(COMPARE_URLS, true);
	}

	/**
	 * @return model for the Compare Usernames setting
	 */
	static SettingsModelBoolean createCompareUsernameModel() {
		return new SettingsModelBoolean(COMPARE_USERNAMES, true);
	}

	/**
	 * @return model for the Compare Database types setting
	 */
	static SettingsModelBoolean createCompareDbtypeModel() {
		return new SettingsModelBoolean(COMPARE_DATABASE_TYPES, true);
	}

	/**
	 * @return model for the Compare SQL setting
	 */
	static SettingsModelBoolean createCompareSqlModel() {
		return new SettingsModelBoolean(COMPARE_SQL, true);
	}

	/**
	 * @return model for the Ignore SQL Comments setting
	 */
	static SettingsModelBoolean createIgnoreSqlCommentsModel() {
		return new SettingsModelBoolean(IGNORE_SQL_COMMENTS, true);
	}

	/**
	 * @return model for the Compare output table specs setting
	 */
	static SettingsModelBoolean createCompareTableSpecsModel() {
		return new SettingsModelBoolean(COMPARE_OUTPUT_TABLE_SPECS, true);
	}

	private void updateEnableStatus() {
		cpSqlMdl.setEnabled(hasComparableSqlInput);
		cpTableSpecsMdl.setEnabled(hasComparableSqlInput);
		ignoreSqlCommentMdl.setEnabled(
				hasComparableSqlInput && cpSqlMdl.getBooleanValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane#
	 * loadAdditionalSettingsFrom(org.knime.core.node.NodeSettingsRO,
	 * org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings,
			PortObjectSpec[] specs) throws NotConfigurableException {
		hasComparableSqlInput = specs[0] instanceof DatabasePortObjectSpec
				&& specs[1] instanceof DatabasePortObjectSpec;
		updateEnableStatus();
		super.loadAdditionalSettingsFrom(settings, specs);
	}

}
