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
package com.vernalis.nodes.collection.collection2string;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.filter.InputFilter;

import com.vernalis.nodes.collection.abstrct.AbstractMultiCollectionNodeDialog;

/**
 * Node Dialog for the Collection to String node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class ColToStringNodeDialog extends AbstractMultiCollectionNodeDialog {

	private static final String CELL = "Cell ";

	private static final String PREFIX = "Prefix";

	private static final String SKIP_MISSING_VALUES_IN_COLLECTIONS =
			"Skip missing values in collections?";

	/**
	 * InputFilter for collection columns
	 */
	static final InputFilter<DataColumnSpec> COLLECTION_FILTER =
			new InputFilter<>() {

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
	 * Constructor
	 */
	public ColToStringNodeDialog() {
		super(true);

	}

	@Override
	protected void postpendDialogComponents() {
		addDialogComponent(
				new DialogComponentBoolean(createSkipMissingValuesModel(),
						SKIP_MISSING_VALUES_IN_COLLECTIONS));
		setHorizontalPlacement(true);
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
		setHorizontalPlacement(false);
	}

	/**
	 * @return the model for the cell suffix
	 */
	static SettingsModelString createCellSuffixModel() {
		return new SettingsModelString(CELL_SUFFIX, "");
	}

	/**
	 * @return the model for the cell prefix
	 */
	static SettingsModelString createCellPrefixModel() {
		return new SettingsModelString(CELL_PREFIX, "");
	}

	/**
	 * @return the model for the collection suffix
	 */
	static SettingsModelString createSuffixModel() {
		return new SettingsModelString(SUFFIX, "]");
	}

	/**
	 * @return the model for the collection joiner
	 */
	static SettingsModelString createJoinerModel() {
		return new SettingsModelString(JOINER, ",");
	}

	/**
	 * @return the model for the collection prefix
	 */
	static SettingsModelString createPrefixModel() {
		return new SettingsModelString(PREFIX, "[");
	}

	/**
	 * @return the model for the skip missing values option
	 */
	static SettingsModelBoolean createSkipMissingValuesModel() {
		return new SettingsModelBoolean(SKIP_MISSING_VALUES_IN_COLLECTIONS,
				false);
	}

}
