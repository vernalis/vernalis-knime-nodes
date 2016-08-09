/*******************************************************************************
 * Copyright (c) 2014, 2015, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
/**
 * 
 */
package com.vernalis.knime.mmp.nodes.uniquifyids;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * Node Dialog for the Uniquify IDs node
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class UniquifyIdsNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * Constructor
	 */
	@SuppressWarnings("unchecked")
	public UniquifyIdsNodeDialog() {
		super();

		addDialogComponent(new DialogComponentColumnNameSelection(
				createIDModel(), "Select the ID column", 0, StringValue.class));
	}

	/** Create model for ID column */
	protected static SettingsModelString createIDModel() {
		return new SettingsModelString("ID Column", null);
	}

}
