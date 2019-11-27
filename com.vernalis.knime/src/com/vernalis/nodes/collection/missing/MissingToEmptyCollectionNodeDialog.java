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
package com.vernalis.nodes.collection.missing;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.util.filter.InputFilter;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;

/**
 * Node Dialog implementation for the Empty collection to missing cell node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class MissingToEmptyCollectionNodeDialog
		extends DefaultNodeSettingsPane {

	static final String REPLACE_INPUT_COLUMNS = "Replace input columns?";

	static final String COLLECTION_COLUMNS = "Collection Columns";

	/**
	 * {@link InputFilter} for collection columns
	 */
	static final InputFilter<DataColumnSpec> COLLECTION_FILTER =
			new InputFilter<DataColumnSpec>() {

				@Override
				public boolean include(DataColumnSpec spec) {
					return spec.getType().isCollectionType();

				}
			};

	/**
	 * Constructor
	 */
	public MissingToEmptyCollectionNodeDialog() {
		createNewGroup(COLLECTION_COLUMNS);
		addDialogComponent(
				new DialogComponentColumnFilter2(createColumnsModel(), 0));
		closeCurrentGroup();
		addDialogComponent(new DialogComponentBoolean(
				createReplaceInputColumnsModel(), REPLACE_INPUT_COLUMNS));
	}

	/**
	 * @return Settings model for the {@value #REPLACE_INPUT_COLUMNS} option
	 */
	static SettingsModelBoolean createReplaceInputColumnsModel() {
		return new SettingsModelBoolean(REPLACE_INPUT_COLUMNS, true);
	}

	/**
	 * @return Settings model for the {@value #COLLECTION_COLUMNS} option
	 */
	static SettingsModelColumnFilter2 createColumnsModel() {
		return new SettingsModelColumnFilter2(COLLECTION_COLUMNS,
				COLLECTION_FILTER,
				DataColumnSpecFilterConfiguration.FILTER_BY_NAMEPATTERN);
	}

}
