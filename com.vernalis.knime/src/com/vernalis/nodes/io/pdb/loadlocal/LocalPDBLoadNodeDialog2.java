/**
  * ------------------------------------------------------------------------
  *  Copyright (C) 2013, 2019 Vernalis (R&D) Ltd
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License, Version 3, as
  *  published by the Free Software Foundation.
  *
  *  This program is distributed in the hope that it will be useful, but
  *  WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, see <http://www.gnu.org/licenses>.
  * ------------------------------------------------------------------------
  */
package com.vernalis.nodes.io.pdb.loadlocal;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * LocalPDBLoadNode dialog class
 */
public class LocalPDBLoadNodeDialog2 extends DefaultNodeSettingsPane {

	/**
	 * Settings key and dialog text for remove input column
	 */
	private static final String REMOVE_INPUT_COLUMN = "Remove input column";

	/**
	 * New pane for configuring the LocalPDBLoad node.
	 */
	@SuppressWarnings("unchecked")
	protected LocalPDBLoadNodeDialog2() {
		super();
		createNewGroup("PDB Paths");
		addDialogComponent(new DialogComponentColumnNameSelection(
				new SettingsModelString(
						LocalPDBLoadNodeModel2.CFG_PATH_COLUMN_NAME, null),
				"Select a column containing the PDB paths:", 0, true,
				StringValue.class));

		addDialogComponent(new DialogComponentBoolean(
				createRemoveInputColumnMdl(), REMOVE_INPUT_COLUMN));
		closeCurrentGroup();

		createNewGroup("Destination Column:");
		addDialogComponent(new DialogComponentString(
				new SettingsModelString(
						LocalPDBLoadNodeModel2.CFG_FILE_COLUMN_NAME, null),
				"Enter name of column for PDB cells:"));

		closeCurrentGroup();
	}

	/**
	 * @return Settings model for the 'Remove input column' option
	 */
	static SettingsModelBoolean createRemoveInputColumnMdl() {
		// False matches legacy behaviour
		return new SettingsModelBoolean(REMOVE_INPUT_COLUMN, false);
	}
}
