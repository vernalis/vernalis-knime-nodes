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
package com.vernalis.pdbconnector2.nodes.load;

import javax.swing.JFileChooser;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * {@link DefaultNodeSettingsPane} implementation for the PDB Connector Read
 * Query node
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class LoadQueryNodeDialog extends DefaultNodeSettingsPane {

	private static final String INPUT_FILE_LOCATION = "Input File Location";

	/**
	 * Constructor
	 */
	public LoadQueryNodeDialog() {
		createNewGroup(INPUT_FILE_LOCATION);

		final SettingsModelString fileModel = createFileModel();
		FlowVariableModel fvm = createFlowVariableModel(fileModel);
		addDialogComponent(new DialogComponentFileChooser(fileModel,
				LoadQueryNodeDialog.class.getCanonicalName(),
				JFileChooser.OPEN_DIALOG, false, fvm, ".xml"));
	}

	/**
	 * @return The model for the 'Input File Location' setting
	 */
	static SettingsModelString createFileModel() {
		return new SettingsModelString(INPUT_FILE_LOCATION, null);
	}

}
