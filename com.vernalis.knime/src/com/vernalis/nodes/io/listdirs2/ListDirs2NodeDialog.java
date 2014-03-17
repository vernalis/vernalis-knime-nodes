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
import org.knime.core.node.util.FilesHistoryPanel;

/**
 * <code>NodeDialog</code> for the "ListDirs" Node.
 */
public class ListDirs2NodeDialog extends DefaultNodeSettingsPane {
	private final FilesHistoryPanel m_history =
    		new FilesHistoryPanel ("list_dir");
    /**
     * New pane for configuring the ListDirs node.
     */
    protected ListDirs2NodeDialog() {
    	super();
    	createNewGroup("Select Folder(s)");
        addDialogComponent (new DialogComponentFileChooser(new SettingsModelString(ListDirs2NodeModel.CFG_PATH,null),
        		"list_dir", JFileChooser.OPEN_DIALOG, true));
        
        addDialogComponent (new DialogComponentBoolean(new SettingsModelBoolean(ListDirs2NodeModel.CFG_SUB_DIRS,false), "Include Sub-folders"));
        closeCurrentGroup();
    }
}

