/**************************************************************************
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
 **************************************************************************/
package com.vernalis.nodes.io.txt;

import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "SaveTxtLocal" Node. Node to save a PDB cell
 * column (as string) to a local file with path specified in a second column
 */
public class SaveTxtLocalNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * Settings model key and dialog text
	 */
	private static final String PARENT_DIRECTORY = "Parent Directory";
	/**
	 * Settings model key and dialog text
	 */
	private static final String SAVE_ALL_FILES_IN_PARENT_FOLDER =
			"Save all files in parent folder";

	/**
	 * New pane for configuring the SavePDBLocal node.
	 */
	@SuppressWarnings("unchecked")
	protected SaveTxtLocalNodeDialog() {
		super();
		createNewGroup("File Column");
		addDialogComponent(new DialogComponentColumnNameSelection(
				new SettingsModelString(
						SaveTxtLocalNodeModel.CFG_FILE_COLUMN_NAME, null),
				"Select a column containing the String Cells:", 0, true,
				StringValue.class));

		closeCurrentGroup();

		createNewGroup("File path Column:");
		addDialogComponent(new DialogComponentColumnNameSelection(
				new SettingsModelString(
						SaveTxtLocalNodeModel.CFG_PATH_COLUMN_NAME, null),
				"Enter name of column for file paths:", 0, true,
				StringValue.class));

		final SettingsModelBoolean useParentFolderModel =
				createUseParentFolderModel();
		addDialogComponent(new DialogComponentBoolean(useParentFolderModel,
				SAVE_ALL_FILES_IN_PARENT_FOLDER));
		final SettingsModelString folderNameModel = createFolderNameModel();
		final DialogComponentFileChooser fileChooser =
				new DialogComponentFileChooser(folderNameModel,
						getClass().getName(), JFileChooser.SAVE_DIALOG, true,
						createFlowVariableModel(folderNameModel));
		fileChooser.setBorderTitle(PARENT_DIRECTORY);
		addDialogComponent(fileChooser);
		useParentFolderModel.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				folderNameModel
						.setEnabled(useParentFolderModel.getBooleanValue());

			}
		});
		folderNameModel.setEnabled(useParentFolderModel.getBooleanValue());

		addDialogComponent(
				new DialogComponentBoolean(
						new SettingsModelBoolean(
								SaveTxtLocalNodeModel.CFG_OVERWRITE, true),
						"Overwrite files?"));

		addDialogComponent(new DialogComponentString(
				new SettingsModelString(
						SaveTxtLocalNodeModel.CFG_SUCCESS_COLUMN_NAME, null),
				"Enter name of column for save successful flag:"));

		closeCurrentGroup();
	}

	/**
	 * @return Settings model for the parent directory option
	 */
	static SettingsModelString createFolderNameModel() {
		return new SettingsModelString(PARENT_DIRECTORY, null);
	}

	/**
	 * @return Settings model for the use parent directory option
	 */
	static SettingsModelBoolean createUseParentFolderModel() {
		return new SettingsModelBoolean(SAVE_ALL_FILES_IN_PARENT_FOLDER, false);
	}
}
