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
package com.vernalis.nodes.list2set;

import org.knime.core.data.collection.ListDataValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;

public class List2SetNodeDialog extends DefaultNodeSettingsPane {

	private static final String SORT_SET_MEMBERS = "Sort set members";

	List2SetNodeDialog() {
		createNewGroup("Select List Columns");
		addDialogComponent(
				new DialogComponentColumnFilter2(createColumnFilterModel(), 0));
		closeCurrentGroup();
		addDialogComponent(new DialogComponentBoolean(createSortedModel(),
				SORT_SET_MEMBERS));
	}

	static SettingsModelBoolean createSortedModel() {
		return new SettingsModelBoolean(SORT_SET_MEMBERS, false);
	}

	@SuppressWarnings("unchecked")
	static SettingsModelColumnFilter2 createColumnFilterModel() {
		return new SettingsModelColumnFilter2("List Columns",
				ListDataValue.class);
	}
}
