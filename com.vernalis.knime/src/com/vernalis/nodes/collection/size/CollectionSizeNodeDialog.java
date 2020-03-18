/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.collection.size;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.util.filter.InputFilter;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;

/**
 * Node dialog implementation for the Collection Size node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class CollectionSizeNodeDialog extends DefaultNodeSettingsPane {

	private static final String COLLECTION_COLUMNS = "Collection Columns";
	/**
	 * {@link InputFilter} for collection columns
	 */
	private static final InputFilter<DataColumnSpec> COLLECTION_FILTER =
			new InputFilter<DataColumnSpec>() {

				@Override
				public boolean include(DataColumnSpec name) {
					return name.getType().isCollectionType();
				}
			};

	public CollectionSizeNodeDialog() {
		createNewGroup(COLLECTION_COLUMNS);
		addDialogComponent(
				new DialogComponentColumnFilter2(createColumnsModel(), 0));
	}

	/**
	 * @return the Settings Model for the {@value #COLLECTION_COLUMNS} option
	 */
	static SettingsModelColumnFilter2 createColumnsModel() {
		return new SettingsModelColumnFilter2(COLLECTION_COLUMNS,
				COLLECTION_FILTER,
				DataColumnSpecFilterConfiguration.FILTER_BY_NAMEPATTERN);
	}

}
