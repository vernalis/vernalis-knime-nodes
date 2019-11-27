/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.misc.trimtable;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * Node Dialog Pane for the 'Trim Table' node
 * 
 * @author s.roughley
 *
 */
public class TrimTableNodeDialog extends DefaultNodeSettingsPane {

	private static final String TEST_COLUMNS = "Test Columns";
	private static final String TRIM_BEHAVIOUR = "Trim Behaviour";
	private static final String TRIM_END = "Trim End";
	private static final String TRIM_START = "Trim Start";

	public TrimTableNodeDialog() {

		addDialogComponent(
				new DialogComponentColumnFilter2(createColsModel(), 0));
		createNewGroup("Trim Options");
		setHorizontalPlacement(true);
		addDialogComponent(
				new DialogComponentBoolean(createTrimStartModel(), TRIM_START));
		addDialogComponent(
				new DialogComponentBoolean(createTrimEndModel(), TRIM_END));

		addDialogComponent(
				new DialogComponentButtonGroup(createTrimBehaviourModel(),
						TRIM_BEHAVIOUR, false, TrimBehaviour.values()));
	}

	/**
	 * @return The settings model for the {@value #TEST_COLUMNS} filter
	 */
	static SettingsModelColumnFilter2 createColsModel() {
		return new SettingsModelColumnFilter2(TEST_COLUMNS);
	}

	/**
	 * @return The settings model for the {@value #TRIM_START} setting
	 */
	static SettingsModelBoolean createTrimStartModel() {
		return new SettingsModelBoolean(TRIM_START, true);
	}

	/**
	 * @return The settings model for the {@value #TRIM_END} setting
	 */
	static SettingsModelBoolean createTrimEndModel() {
		return new SettingsModelBoolean(TRIM_END, true);
	}

	/**
	 * @return The settings model for the {@value #TRIM_BEHAVIOUR} option
	 */
	static SettingsModelString createTrimBehaviourModel() {
		return new SettingsModelString(TRIM_BEHAVIOUR,
				TrimBehaviour.getDefault().getActionCommand());
	}

}
