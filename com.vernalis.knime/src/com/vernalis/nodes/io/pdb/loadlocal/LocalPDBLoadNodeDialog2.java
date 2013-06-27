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
package com.vernalis.nodes.io.pdb.loadlocal;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * LocalPDBLoadNode dialog class
 */
public class LocalPDBLoadNodeDialog2 extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the LocalPDBLoad node.
     */
    protected LocalPDBLoadNodeDialog2() {
      	super();
    	createNewGroup("PDB Paths");
        addDialogComponent (new DialogComponentColumnNameSelection(
        		new SettingsModelString(LocalPDBLoadNodeModel2.CFG_PATH_COLUMN_NAME,null),
        		"Select a column containing the PDB paths:", 0, true, StringValue.class));
        
        closeCurrentGroup();
        
        createNewGroup("Destination Column:");
        addDialogComponent (new DialogComponentString(new SettingsModelString(
        		LocalPDBLoadNodeModel2.CFG_FILE_COLUMN_NAME, null), "Enter name of column for PDB cells:"));
        
        closeCurrentGroup();
    }
}

