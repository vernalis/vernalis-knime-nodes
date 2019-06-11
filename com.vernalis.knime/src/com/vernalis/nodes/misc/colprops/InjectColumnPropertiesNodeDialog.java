/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.misc.colprops;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

/**
 * Node Settings Pane for the Inject Column Properties node
 * 
 * @author s.roughley
 *
 */
public class InjectColumnPropertiesNodeDialog extends DefaultNodeSettingsPane {

	private static final String USE_INCOMING_TABLE_SPEC_DURING_CONFIGURE =
			"Use incoming table spec during configure";
	private static final String APPLY_TO_ALL_COLUMNS = "Apply to all columns";
	private static final String OVERWRITE_DUPLICATE_PROPERTIES =
			"Overwrite duplicate properties";
	private static final String CLEAR_EXISTING_PROPERTIES =
			"Clear existing properties";

	/**
	 * Constructor
	 */
	public InjectColumnPropertiesNodeDialog() {
		final SettingsModelBoolean clearExistingPropertiesMdl =
				createClearExistingPropertiesModel();
		final SettingsModelBoolean overwriteDuplicatePropertiesMdl =
				createOverwriteDuplicatePropertiesModel();
		final SettingsModelBoolean applyToAllColumnsMdl =
				createApplyToAllColumnsModel();

		clearExistingPropertiesMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				overwriteDuplicatePropertiesMdl.setEnabled(
						!clearExistingPropertiesMdl.getBooleanValue());
				applyToAllColumnsMdl.setEnabled(
						clearExistingPropertiesMdl.getBooleanValue());
			}
		});
		overwriteDuplicatePropertiesMdl
				.setEnabled(!clearExistingPropertiesMdl.getBooleanValue());
		applyToAllColumnsMdl
				.setEnabled(clearExistingPropertiesMdl.getBooleanValue());

		addDialogComponent(new DialogComponentBoolean(
				clearExistingPropertiesMdl, CLEAR_EXISTING_PROPERTIES));
		addDialogComponent(new DialogComponentBoolean(applyToAllColumnsMdl,
				APPLY_TO_ALL_COLUMNS));
		addDialogComponent(
				new DialogComponentBoolean(overwriteDuplicatePropertiesMdl,
						OVERWRITE_DUPLICATE_PROPERTIES));

		addDialogComponent(new DialogComponentBoolean(
				createUseIncomingSpecAtConfigureModel(),
				USE_INCOMING_TABLE_SPEC_DURING_CONFIGURE));
	}

	/**
	 * @return Settings Model for the 'Use Incoming Table Spec during configure'
	 *         option
	 */
	static SettingsModelBoolean createUseIncomingSpecAtConfigureModel() {
		return new SettingsModelBoolean(
				USE_INCOMING_TABLE_SPEC_DURING_CONFIGURE, true);
	}

	/**
	 * @return Settings model for the 'Apply to All Columns' option
	 */
	static SettingsModelBoolean createApplyToAllColumnsModel() {
		return new SettingsModelBoolean(APPLY_TO_ALL_COLUMNS, true);
	}

	/**
	 * @return Settings Model for the 'Overwrite duplicate properties' option
	 */
	static SettingsModelBoolean createOverwriteDuplicatePropertiesModel() {
		return new SettingsModelBoolean(OVERWRITE_DUPLICATE_PROPERTIES, true);
	}

	/**
	 * @return Settings Model for the 'Clear existing properties' option
	 */
	static SettingsModelBoolean createClearExistingPropertiesModel() {
		return new SettingsModelBoolean(CLEAR_EXISTING_PROPERTIES, false);
	}

}
