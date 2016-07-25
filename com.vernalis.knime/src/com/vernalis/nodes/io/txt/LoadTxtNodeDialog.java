/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2013, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.io.txt;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.io.FileEncodingWithGuess;

/**
 * <code>NodeDialog</code> for the "LoadAEVs" Node. Node to Load text files to a
 * column in the table
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author SDR
 */
public class LoadTxtNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring the LoadAEVs node.
	 */
	@SuppressWarnings("unchecked")
	protected LoadTxtNodeDialog() {
		super();
		createNewGroup("File Paths");
		addDialogComponent(new DialogComponentColumnNameSelection(
				new SettingsModelString(LoadTxtNodeModel.CFG_PATH_COLUMN_NAME,
						null), "Select a column containing the file paths:", 0,
				true, StringValue.class));

		addDialogComponent(new DialogComponentButtonGroup(
				new SettingsModelString(LoadTxtNodeModel.CFG_ENCODING,
						FileEncodingWithGuess.getDefaultMethod()
								.getActionCommand()), "Select file encoding",
				true, FileEncodingWithGuess.values()));
		
		closeCurrentGroup();

		createNewGroup("Destination Column:");
		addDialogComponent(new DialogComponentString(new SettingsModelString(
				LoadTxtNodeModel.CFG_FILE_COLUMN_NAME, "Text File"),
				"Enter name of column for loaded files:"));

		closeCurrentGroup();
	}
}
