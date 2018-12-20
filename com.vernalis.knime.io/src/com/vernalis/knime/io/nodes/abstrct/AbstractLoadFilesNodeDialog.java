/*******************************************************************************
 * Copyright (c) 2015, 2018, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License, Version 3, as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.io.nodes.abstrct;

import javax.swing.JFileChooser;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.io.FileEncodingWithGuess;
import com.vernalis.knime.dialog.components.DialogComponentListFilesSelector;
import com.vernalis.knime.dialog.components.SettingsModelStringArrayFlowVarReplacable;

/**
 * <code>NodeDialog</code> for the load files Nodes.
 * 
 * @author S. Roughley
 */
public class AbstractLoadFilesNodeDialog extends DefaultNodeSettingsPane {

	private static final String INCLUDE_FILENAMES_IN_OUTPUT_TABLE =
			"Include filenames in output table";
	private final SettingsModelStringArrayFlowVarReplacable m_fileNames;

	// /**
	// * New pane for configuring the node.
	// *
	// * @param historyId
	// * The History ID for the file history
	// */
	// public AbstractLoadFilesNodeDialog(String historyId) {
	//
	// m_fileNames = createFilenamesModel();
	// FlowVariableModel fvm = createFlowVariableModel(m_fileNames);
	// addDialogComponent(new DialogComponentListFilesSelector(m_fileNames,
	// "Select file(s)", true,
	// "Select files to load. Each will will be loaded to a new table row.",
	// historyId,
	// JFileChooser.OPEN_DIALOG, fvm));
	//
	// addDialogComponent(new
	// DialogComponentButtonGroup(createFileEncodingModel(),
	// "Select file encoding", true, FileEncodingWithGuess.values()));
	//
	// addDialogComponent(new DialogComponentBoolean(createIncludePathsModel(),
	// "Include paths in output table"));
	// addDialogComponent(new
	// DialogComponentBoolean(createIncludeFilenamesModel(),
	// INCLUDE_FILENAMES_IN_OUTPUT_TABLE));
	// addDialogComponent(new
	// DialogComponentBoolean(createIncludeFilenameAsRowIDModel(),
	// "Include filename in Row IDs"));
	//
	// }

	/**
	 * New pane for configuring the LoadTextFiles node.
	 * 
	 * @param historyID
	 *            The History ID for the file history
	 * @param fileTypes
	 *            The acceptable file extensions
	 */
	public AbstractLoadFilesNodeDialog(String historyID, String... fileTypes) {

		m_fileNames = createFilenamesModel();
		FlowVariableModel fvm = createFlowVariableModel(m_fileNames);
		addDialogComponent(new DialogComponentListFilesSelector(m_fileNames,
				"Select file(s)", true,
				"Select files to load.  Each will will be loaded to a new table row.",
				historyID, JFileChooser.OPEN_DIALOG, fvm, fileTypes));

		addDialogComponent(new DialogComponentButtonGroup(
				createFileEncodingModel(), "Select file encoding", false,
				FileEncodingWithGuess.values()));
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentBoolean(createIncludePathsModel(),
				"Include paths in output table"));
		addDialogComponent(
				new DialogComponentBoolean(createIncludeFilenameAsRowIDModel(),
						"Include filename in Row IDs"));
		addDialogComponent(
				new DialogComponentBoolean(createIncludeFilenamesModel(),
						INCLUDE_FILENAMES_IN_OUTPUT_TABLE));
		setHorizontalPlacement(false);
	}

	static SettingsModelBoolean createIncludeFilenamesModel() {
		return new SettingsModelBoolean(INCLUDE_FILENAMES_IN_OUTPUT_TABLE,
				true);
	}

	/**
	 * @return Settings Model for the file encoding choice
	 */
	static SettingsModelString createFileEncodingModel() {
		return new SettingsModelString("File Encoding",
				FileEncodingWithGuess.getDefaultMethod().getActionCommand());
	}

	/**
	 * @return Settings Model of the include filename as row id option
	 */
	static SettingsModelBoolean createIncludeFilenameAsRowIDModel() {
		return new SettingsModelBoolean("Include filenames in RowIDs", true);
	}

	/**
	 * @return Settings model for the selected filenames
	 */
	static SettingsModelStringArrayFlowVarReplacable createFilenamesModel() {
		return new SettingsModelStringArrayFlowVarReplacable("Selected Files",
				null);
	}

	/**
	 * @return Settings model for the include paths columns setting
	 */
	static SettingsModelBoolean createIncludePathsModel() {
		return new SettingsModelBoolean("Include paths in output", true);
	}
}
