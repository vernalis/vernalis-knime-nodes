/*******************************************************************************
 * Copyright (c) 2018,2023, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.collection.list2set;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.nodes.collection.abstrct.AbstractMultiCollectionNodeDialog;

/**
 * Node dialog for the List to Set node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class List2SetNodeDialog extends AbstractMultiCollectionNodeDialog {

	private static final String SORT_SET_MEMBERS = "Sort set members";

	/**
	 * Constructor
	 */
	List2SetNodeDialog() {
		super("List Columns", false, true, false);

		addDialogComponent(new DialogComponentBoolean(createSortedModel(),
				SORT_SET_MEMBERS));
	}

	/**
	 * @return The settings model for the sort set members option
	 */
	static SettingsModelBoolean createSortedModel() {
		return new SettingsModelBoolean(SORT_SET_MEMBERS, false);
	}

}
