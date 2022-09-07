/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.testing.nodes.missingvals;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;

/**
 * Node dialog pane for the Empty Column Test Node
 * 
 * @since 07-Sep-2022
 * @since v1.36.0

 * @author S Roughley
 *
 */
public class EmptyColumnTestNodeDialogPane extends DefaultNodeSettingsPane {

	/**
	 * Constructor
	 */
	public EmptyColumnTestNodeDialogPane() {
		super();
		addDialogComponent(new DialogComponentColumnFilter2(
				createFilterSettingsModel(), 0));
	}

	/**
	 * @return model for the Columns setting
	 */
	public static SettingsModelColumnFilter2 createFilterSettingsModel() {
		return new SettingsModelColumnFilter2("columns");
	}
}
