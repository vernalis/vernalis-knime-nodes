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
package com.vernalis.knime.db.nodes.replaceheader;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.DataValueColumnFilter;

/**
 * Node Settings Pane for the DB Replace Column Header node
 * 
 * @author S Roughley
 *
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public class DBReplaceColumnHeaderNodeDialog extends DefaultNodeSettingsPane {

	private static final String MISSING_COLUMN_BEHAVIOUR =
			"Missing Column Behaviour";
	private static final String VALUE_COLUMN = "Value Column";
	private static final String LOOKUP_COLUMN = "Lookup Column";
	/** Column Filter for String Columns */
	@SuppressWarnings("unchecked")
	static final ColumnFilter STRING_FILTER =
			new DataValueColumnFilter(StringValue.class);

	/**
	 * Constructor
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	DBReplaceColumnHeaderNodeDialog() {
		addDialogComponent(new DialogComponentColumnNameSelection(
				createLookupColumnNameModel(), LOOKUP_COLUMN, 1,
				STRING_FILTER));
		addDialogComponent(new DialogComponentColumnNameSelection(
				createValueColumnNameModel(), VALUE_COLUMN, 1, STRING_FILTER));
		addDialogComponent(new DialogComponentButtonGroup(
				createMissingColumnModel(), MISSING_COLUMN_BEHAVIOUR, false,
				MissingColumnAction.values()));
	}

	/**
	 * @return model for the Missing Column Behaviour setting
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	static SettingsModelString createMissingColumnModel() {
		return new SettingsModelString(MISSING_COLUMN_BEHAVIOUR,
				MissingColumnAction.getDefault().getActionCommand());
	}

	/**
	 * @return model for the Value Column setting
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	static SettingsModelString createValueColumnNameModel() {
		return new SettingsModelString(VALUE_COLUMN, null);
	}

	/**
	 * @return model for the Lookup Column setting
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	static SettingsModelString createLookupColumnNameModel() {
		return new SettingsModelString(LOOKUP_COLUMN, null);
	}

}
