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
package com.vernalis.knime.db.nodes.switches;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import com.vernalis.knime.database.TableTypes;

/**
 * Node Settings Pane implementation for the Database Table Exists nodes
 * 
 * @author S Roughley
 *
 */
public class DBTableExistsNodeDialog extends DefaultNodeSettingsPane {

	private static final String IGNORE_CASE = "Ignore Case";
	private static final String CHECK_AVAILABILITY_AT_CONFIGURE =
			"Check availability at Configure";
	private static final String TABLE_NAME = "Table Name";
	private static final String TABLE_TYPES = "Table Type(s)";

	/**
	 * Constructor
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	public DBTableExistsNodeDialog() {
		super();

		final SettingsModelString queryTableNameMdl =
				createQueryTableNameModel();
		FlowVariableModel fvm = createFlowVariableModel(queryTableNameMdl);
		addDialogComponent(new DialogComponentString(queryTableNameMdl,
				TABLE_NAME, true, 35, fvm));
		addDialogComponent(new DialogComponentBoolean(createIgnoreCaseModel(),
				IGNORE_CASE));
		addDialogComponent(
				new DialogComponentStringListSelection(createTableTypesModel(),
						TABLE_TYPES, TableTypes.getAllNames()));
		addDialogComponent(
				new DialogComponentBoolean(createCheckAtConfigureModel(),
						CHECK_AVAILABILITY_AT_CONFIGURE));
	}

	/**
	 * @return The settings model for the ignore case setting
	 */
	static SettingsModelBoolean createIgnoreCaseModel() {
		return new SettingsModelBoolean(IGNORE_CASE, true);
	}

	/**
	 * @return The settings model for the check availability at configure model
	 */
	static SettingsModelBoolean createCheckAtConfigureModel() {
		return new SettingsModelBoolean(CHECK_AVAILABILITY_AT_CONFIGURE, false);
	}

	/**
	 * @return The settings model for the query table name model
	 */
	static SettingsModelString createQueryTableNameModel() {
		return new SettingsModelString(TABLE_NAME, null);
	}

	/**
	 * @return The settings model for the table type(s) option
	 */
	static SettingsModelStringArray createTableTypesModel() {
		return new SettingsModelStringArray(TABLE_TYPES,
				TableTypes.getDefaults());
	}

}
