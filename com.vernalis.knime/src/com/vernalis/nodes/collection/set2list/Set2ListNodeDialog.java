/*******************************************************************************
 * Copyright (c) 2019,2023, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.collection.set2list;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.nodes.collection.abstrct.AbstractMultiCollectionNodeDialog;

/**
 * Node Dialog for the Set to List node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class Set2ListNodeDialog extends AbstractMultiCollectionNodeDialog {

	private static final String SORT_LIST_MEMBERS = "Sort list members";

	/**
	 * Constructor
	 */
	Set2ListNodeDialog() {
		super("Set Columns", false, false, true);
		addDialogComponent(new DialogComponentBoolean(createSortedModel(),
				SORT_LIST_MEMBERS));
	}

	/**
	 * @return the settings model for the sort list members option
	 */
	static SettingsModelBoolean createSortedModel() {
		return new SettingsModelBoolean(SORT_LIST_MEMBERS, false);
	}

}
