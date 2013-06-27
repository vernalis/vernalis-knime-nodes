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
package com.vernalis.nodes.io.pdb.savelocal;

import org.knime.bio.types.PdbValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;



/**
 * <code>NodeDialog</code> for the "SavePDBLocal" Node.
 * Node to save a PDB cell column (as string) to a local file with path specified in a second column
 */
public class SavePDBLocalNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the SavePDBLocal node.
     */
    protected SavePDBLocalNodeDialog() {
    	super();
    	createNewGroup("PDB Column");
    	addDialogComponent (new DialogComponentColumnNameSelection(
    			new SettingsModelString(SavePDBLocalNodeModel.CFG_PDB_COLUMN_NAME,null),
    			"Select a column containing the PDB Cells:", 0, true, PdbValue.class));

    	closeCurrentGroup();

    	createNewGroup("File path Column:");
    	addDialogComponent (new DialogComponentColumnNameSelection(
    			new SettingsModelString(SavePDBLocalNodeModel.CFG_PATH_COLUMN_NAME, null),
    			"Enter name of column for file paths:", 0, true, StringValue.class));
    	
    	addDialogComponent (new DialogComponentBoolean(new SettingsModelBoolean(
    			SavePDBLocalNodeModel.CFG_OVERWRITE, true), "Overwrite files?"));
    	
        addDialogComponent (new DialogComponentString(new SettingsModelString(
        		SavePDBLocalNodeModel.CFG_SUCCESS_COLUMN_NAME, null), "Enter name of column for save successful flag:"));
        
    	closeCurrentGroup();
    }
}

