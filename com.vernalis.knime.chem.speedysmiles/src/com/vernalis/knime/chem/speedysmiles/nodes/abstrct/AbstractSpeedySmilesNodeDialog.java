/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
package com.vernalis.knime.chem.speedysmiles.nodes.abstrct;

import org.knime.chem.types.SmilesValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * Base Node Dialog Class for SpeedySMILES nodes. Supplies a node dialog with a
 * column selection dropdown for SMILES strings. Optionally a 'remove input
 * column' option is also added
 * 
 * @author S Roughley
 */
public class AbstractSpeedySmilesNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * Convenience overloaded constructor which omits the option to remove input
	 * column
	 */
	public AbstractSpeedySmilesNodeDialog() {
		this(false, false);

	}

	/**
	 * Constructor for the node panel. Provides SMILES column selection and an
	 * optional 'remove input column' option
	 * 
	 * @param allowInputColRemoval
	 *            if true then a dialog checkbox is added to allow input column
	 *            removal
	 * @param groupColumnSelection
	 *            If true, then the column selection components are in a group
	 *            within the tab
	 */
	@SuppressWarnings("unchecked")
	public AbstractSpeedySmilesNodeDialog(boolean allowInputColRemoval,
			boolean groupColumnSelection) {
		if (groupColumnSelection) {
			createNewGroup("Column Selection");
		}
		addDialogComponent(
				new DialogComponentColumnNameSelection(createColumnNameModel(),
						"Select SMILES column", 0, SmilesValue.class));
		if (allowInputColRemoval) {
			addDialogComponent(new DialogComponentBoolean(
					createRemoveInputColumnModel(), "Remove input column"));
		}
		if (groupColumnSelection) {
			closeCurrentGroup();
		}
	}

	/**
	 * @return Settings Model for the column name
	 */
	public static SettingsModelString createColumnNameModel() {
		return new SettingsModelString("Column name", "");
	}

	/**
	 * @return Settings Model for removing the input column
	 */
	public static SettingsModelBoolean createRemoveInputColumnModel() {
		return new SettingsModelBoolean("Remove input column", false);
	}
}
