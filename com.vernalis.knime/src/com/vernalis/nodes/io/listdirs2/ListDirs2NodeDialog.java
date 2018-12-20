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
package com.vernalis.nodes.io.listdirs2;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "ListDirs" Node.
 */
public class ListDirs2NodeDialog extends DefaultNodeSettingsPane {
	/**
	 * New pane for configuring the ListDirs node.
	 */
	protected ListDirs2NodeDialog() {
		super();
		createNewGroup("Select Folder(s)");
		addDialogComponent(new DialogComponentFileChooser(
				new SettingsModelString(ListDirs2NodeModel.CFG_PATH, null), "list_dir",
				JFileChooser.OPEN_DIALOG, true));

		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(ListDirs2NodeModel.CFG_SUB_DIRS, false),
				"Include Sub-folders"));
		closeCurrentGroup();

		createNewGroup("Output Option(s)");
		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(ListDirs2NodeModel.CFG_FOLDER_NAME, true), "Folder Name"));
		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(ListDirs2NodeModel.CFG_INCL_CTG_PATH, true),
				"Containing (Parent) Folder Path and URL"));
		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(ListDirs2NodeModel.CFG_IS_VIS, true), "Is Visible?"));
		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(ListDirs2NodeModel.CFG_LAST_MOD, true), "Last Modified"));

	}
}
