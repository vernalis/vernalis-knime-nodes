/*******************************************************************************
 * Copyright (c) 2014, Vernalis (R&D) Ltd
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
 *******************************************************************************/
package com.vernalis.knime.flowvar.nodes.io.write;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "WriteVariables" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author S. Roughley <knime@vernalis.com>
 */
public class WriteVariablesNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * Constructor for the Write Variables Node Dialog
	 */
	protected WriteVariablesNodeDialog() {
		super();
		SettingsModelString fname = createFilenameModel();
		addDialogComponent(new DialogComponentFileChooser(fname,
				WriteVariablesNodeDialog.class.getName(),
				JFileChooser.SAVE_DIALOG, false,
				createFlowVariableModel(fname), ".variables"));
		addDialogComponent(new DialogComponentBoolean(createOverwriteModel(),
				"Overwrite existing file?"));

	}

	/**
	 * Creates the overwrite file model.
	 */
	static final SettingsModelBoolean createOverwriteModel() {
		return new SettingsModelBoolean("Overwrite file", false);
	}

	/**
	 * Creates the filename model.
	 */
	static final SettingsModelString createFilenameModel() {
		return new SettingsModelString("filename", "");
	}
}
