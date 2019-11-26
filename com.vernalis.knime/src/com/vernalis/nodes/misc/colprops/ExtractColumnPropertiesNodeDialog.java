/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.misc.colprops;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;

/**
 * The Node Settings Pane for the Extract Column Properties node
 * 
 * @author s.roughley
 *
 */
public class ExtractColumnPropertiesNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * Constructor
	 */
	public ExtractColumnPropertiesNodeDialog() {
		createNewGroup("Select columns to extract properties for");
		addDialogComponent(
				new DialogComponentColumnFilter2(createColumnsModel(), 0));
	}

	/**
	 * @return The selected column settings model
	 */
	static SettingsModelColumnFilter2 createColumnsModel() {
		return new SettingsModelColumnFilter2("Columns");
	}

}
