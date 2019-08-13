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
package com.vernalis.nodes.collection2string;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.filter.InputFilter;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;

/**
 * @author steve
 *
 */
public class ColToStringNodeDialog extends DefaultNodeSettingsPane {

	private static final String CELL = "Cell ";

	static final String PREFIX = "Prefix";

	static final String SKIP_MISSING_VALUES_IN_COLLECTIONS =
			"Skip missing values in collections?";

	static final String REPLACE_INPUT_COLUMNS = "Replace input columns?";

	static final String COLLECTION_COLUMNS = "Collection Columns";

	static final InputFilter<DataColumnSpec> COLLECTION_FILTER =
			new InputFilter<DataColumnSpec>() {

				@Override
				public boolean include(DataColumnSpec spec) {
					return spec.getType().isCollectionType();

				}
			};

	private static final String JOINER = "Separator";

	private static final String SUFFIX = "Suffix";

	private static final String CELL_PREFIX = CELL + PREFIX;

	private static final String CELL_SUFFIX = CELL + SUFFIX;

	/**
	 * 
	 */
	public ColToStringNodeDialog() {
		createNewGroup(COLLECTION_COLUMNS);
		addDialogComponent(
				new DialogComponentColumnFilter2(createColumnsModel(), 0));
		closeCurrentGroup();
		addDialogComponent(new DialogComponentBoolean(
				createReplaceInputColumnsModel(), REPLACE_INPUT_COLUMNS));
		addDialogComponent(
				new DialogComponentBoolean(createSkipMissingValuesModel(),
						SKIP_MISSING_VALUES_IN_COLLECTIONS));
		addDialogComponent(new DialogComponentString(createPrefixModel(),
				PREFIX, false, 5));
		addDialogComponent(new DialogComponentString(createCellPrefixModel(),
				CELL_PREFIX, false, 5));
		addDialogComponent(new DialogComponentString(createJoinerModel(),
				JOINER, false, 5));
		addDialogComponent(new DialogComponentString(createCellSuffixModel(),
				CELL_SUFFIX, false, 5));
		addDialogComponent(new DialogComponentString(createSuffixModel(),
				SUFFIX, false, 5));

	}

	static SettingsModelString createCellSuffixModel() {
		return new SettingsModelString(CELL_SUFFIX, "");
	}

	static SettingsModelString createCellPrefixModel() {
		return new SettingsModelString(CELL_PREFIX, "");
	}

	static SettingsModelString createSuffixModel() {
		return new SettingsModelString(SUFFIX, "]");
	}

	static SettingsModelString createJoinerModel() {
		return new SettingsModelString(JOINER, ",");
	}

	static SettingsModelString createPrefixModel() {
		return new SettingsModelString(PREFIX, "[");
	}

	static SettingsModelBoolean createSkipMissingValuesModel() {
		return new SettingsModelBoolean(SKIP_MISSING_VALUES_IN_COLLECTIONS,
				false);
	}

	static SettingsModelBoolean createReplaceInputColumnsModel() {
		return new SettingsModelBoolean(REPLACE_INPUT_COLUMNS, true);
	}

	static SettingsModelColumnFilter2 createColumnsModel() {
		return new SettingsModelColumnFilter2(COLLECTION_COLUMNS,
				COLLECTION_FILTER,
				DataColumnSpecFilterConfiguration.FILTER_BY_NAMEPATTERN);
	}

}
