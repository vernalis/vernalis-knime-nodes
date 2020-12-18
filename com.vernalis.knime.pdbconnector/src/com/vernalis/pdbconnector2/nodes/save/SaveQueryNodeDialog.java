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
package com.vernalis.pdbconnector2.nodes.save;

import javax.swing.JFileChooser;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * {@link DefaultNodeSettingsPane} implementation for the 'Write Query' node
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class SaveQueryNodeDialog extends DefaultNodeSettingsPane {

	private static final String OVERWRITE_EXISTING_FILE =
			"Overwrite existing file?";
	private static final String OUTPUT_FILE_LOCATION = "Output File Location";

	/**
	 * Constructor
	 */
	public SaveQueryNodeDialog() {
		createNewGroup(OUTPUT_FILE_LOCATION);
		final SettingsModelString fileModel = createFileModel();
		FlowVariableModel fvm = createFlowVariableModel(fileModel);

		addDialogComponent(new DialogComponentFileChooser(fileModel,
				SaveQueryNodeDialog.class.getCanonicalName(),
				JFileChooser.SAVE_DIALOG, false, fvm, ".xml"));
		addDialogComponent(new DialogComponentBoolean(
				createOverWriteExistingFileModel(), OVERWRITE_EXISTING_FILE));
	}

	/**
	 * @return The model for the 'Overwrite existing file' setting
	 */
	static SettingsModelBoolean createOverWriteExistingFileModel() {
		return new SettingsModelBoolean(OVERWRITE_EXISTING_FILE, false);
	}

	/**
	 * @return The model for the 'Output file location' setting
	 */
	static SettingsModelString createFileModel() {
		return new SettingsModelString(OUTPUT_FILE_LOCATION, null);
	}

}
