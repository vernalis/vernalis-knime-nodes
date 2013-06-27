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
package com.vernalis.nodes.smartsviewer;

import org.knime.chem.types.SmartsValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * <code>NodeDialog</code> for the "SmartsViewer" Node.
 * Retrieves a SMARTSViewer visualisation of a columns of SMARTS strings using the service at www.smartsviewer.de
 */
public class SmartsViewerNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the SmartsViewer node.
     */
    protected SmartsViewerNodeDialog() {
    	super();
    	createNewGroup("Select SMARTS Column");
    	addDialogComponent (new DialogComponentColumnNameSelection(
    			new SettingsModelString(SmartsViewerNodeModel.CFG_SMARTS,null),
    			"Select a column containing the SMARTS Strings:", 0, true, SmilesValue.class, StringValue.class,SmartsValue.class));
        closeCurrentGroup();

        createNewGroup("Renderer Settings");
//        addDialogComponent (new DialogComponentButtonGroup(
//        		new SettingsModelString(SmartsViewerNodeModel.CFG_IMG_FORMAT,"png"), "Select image format",
//        		false, new String[] {"PNG Image","SVG Image"}, new String[] {"png","svg"}));
        
        addDialogComponent (new DialogComponentButtonGroup(
        		new SettingsModelString(SmartsViewerNodeModel.CFG_LEGEND,"both"), "Select legend type",
        		false, new String[] {"Both","None","Static","Dynamic"}, new String[] {"both","none","static","dynamic"}));
        
        addDialogComponent (new DialogComponentButtonGroup(
        		new SettingsModelString(SmartsViewerNodeModel.CFG_VIS_MODUS,"1"), "Select Visualition modus",
        		false, new String[] {"Complete","Element Symbols"}, new String[] {"1","2"}));
        
        closeCurrentGroup();
        
        
    }
}
