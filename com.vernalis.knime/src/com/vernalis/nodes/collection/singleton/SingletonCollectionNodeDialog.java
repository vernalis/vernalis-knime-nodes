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
package com.vernalis.nodes.collection.singleton;

import java.util.Arrays;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.filter.InputFilter;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterPanel;

import static org.knime.core.node.util.filter.NameFilterConfiguration.FILTER_BY_NAMEPATTERN;
import static org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration.FILTER_BY_DATATYPE;

/**
 * Node Dialog for tehh Column to Singleton Collection Column Node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class SingletonCollectionNodeDialog extends DefaultNodeSettingsPane {

	private static final int FILTER_FLAGS =
			FILTER_BY_DATATYPE | FILTER_BY_NAMEPATTERN;

	private static final String ALLOW_WRAPPING_COLLECTIONS =
			"Allow wrapping collections";

	private static final String COLLECTION_TYPE = "Collection Type";

	private static final String WRAP_MISSING_CELLS = "Wrap missing cells";

	private static final String SELECT_COLUMNS = "Select Columns";

	/**
	 * {@link InputFilter} for non-collection columns
	 */
	static final InputFilter<DataColumnSpec> COLUMN_FILTER =
			new InputFilter<>() {

				@Override
				public boolean include(DataColumnSpec name) {
					return !name.getType().isCollectionType();
				}
			};

	/**
	 * {@link InputFilter} for any column type
	 */
	static final InputFilter<DataColumnSpec> ANY_COLUMN_FILTER =
			new InputFilter<>() {

				@Override
				public boolean include(DataColumnSpec name) {
					return true;
				}
			};

	private final DataColumnSpecFilterPanel filterPanel;

	private final SettingsModelBoolean allowCollectionsMdl;

	/**
	 * Constructor
	 */
	protected SingletonCollectionNodeDialog() {
		super();

		createNewGroup(SELECT_COLUMNS);

		final SettingsModelColumnFilter2 colFilterMdl =
				createColumnFilterModel();
		final DialogComponentColumnFilter2 diaC =
				new DialogComponentColumnFilter2(colFilterMdl, 0);
		addDialogComponent(diaC);
		filterPanel = Arrays.stream(diaC.getComponentPanel().getComponents())
				.filter(c -> c instanceof DataColumnSpecFilterPanel)
				.map(c -> (DataColumnSpecFilterPanel) c).findFirst()
				.orElse(null);

		allowCollectionsMdl = createAllowCollectionsModel();
		addDialogComponent(new DialogComponentBoolean(allowCollectionsMdl,
				ALLOW_WRAPPING_COLLECTIONS));
		allowCollectionsMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateFilterPanel();

			}
		});
		// updateFilterPanel(); - Dont do here as fails - no input spec!

		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentBoolean(createMissingModel(),
				WRAP_MISSING_CELLS));
		addDialogComponent(
				new DialogComponentButtonGroup(createCollectionTypeModel(),
						false, COLLECTION_TYPE, "Set", "List"));
	}

	@Override
	public void onOpen() {
		updateFilterPanel();
		super.onOpen();
	}

	/**
	 * 
	 */
	private void updateFilterPanel() {
		if (filterPanel != null) {
			filterPanel.updateWithNewConfiguration(
					new DataColumnSpecFilterConfiguration(SELECT_COLUMNS,
							allowCollectionsMdl.getBooleanValue()
									? ANY_COLUMN_FILTER
									: COLUMN_FILTER,
							FILTER_FLAGS));
		}
	}

	/**
	 * @return model for the Allow Wrapping Collections setting
	 */
	static SettingsModelBoolean createAllowCollectionsModel() {
		return new SettingsModelBoolean(ALLOW_WRAPPING_COLLECTIONS, false);
	}

	/**
	 * @return The model for the collection type
	 */
	static SettingsModelString createCollectionTypeModel() {
		return new SettingsModelString(COLLECTION_TYPE, "List");
	}

	/**
	 * @return The model for the wrap missing cells option
	 */
	static SettingsModelBoolean createMissingModel() {
		return new SettingsModelBoolean(WRAP_MISSING_CELLS, false);
	}

	/**
	 * @return The model for the column filter
	 */
	static SettingsModelColumnFilter2 createColumnFilterModel() {
		return new SettingsModelColumnFilter2(SELECT_COLUMNS, null,
				FILTER_FLAGS);
	}

}
