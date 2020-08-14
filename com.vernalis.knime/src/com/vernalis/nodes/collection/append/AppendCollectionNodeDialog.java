/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.collection.append;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.ListCell;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;

/**
 * The node dialog implementation for the Append to Collection node
 *
 * @author Steve <knime@vernalis.com>
 * @since 1.27.0
 *
 */
public class AppendCollectionNodeDialog extends DefaultNodeSettingsPane {

	private static final String SORT_LIST = "Sort List";
	private static final String REMOVE_APPENDED_COLUMNS = "Remove appended columns";
	private static final String COLLECTION_COLUMN = "Collection Column";
	private static final String DON_T_ADD_MISSING_CELLS_TO_COLLECTION = "Don't add missing cells to collection";
	private static final String TREAT_MISSING_COLLECTION_CELLS_AS_EMPTY_COLLECTIONS = "Treat missing collection cells as empty collections";
	private static final String COLUMNS_TO_APPEND = "Columns to append";

	/**
	 * A singleton EmptyColumnFilter - no columns will pass this filter
	 *
	 * @author Steve
	 *
	 */
	private static final class EmptyColumnFilter implements ColumnFilter {

		private static final EmptyColumnFilter INSTANCE = new EmptyColumnFilter();

		static EmptyColumnFilter getInstance() {
			return INSTANCE;
		}

		private EmptyColumnFilter() {

		}

		@Override
		public boolean includeColumn(DataColumnSpec colSpec) {
			return false;
		}

		@Override
		public String allFilteredMsg() {
			return "No Collection column selected";
		}
	}

	/**
	 * A Column filter allow subtypes of the specified column type through
	 * (including that type itself)
	 *
	 * @author Steve
	 *
	 */
	static final class ElementColumnFilter implements ColumnFilter {
		private final DataType elementType;

		ElementColumnFilter(DataType type) {
			this.elementType = type;
		}

		@Override
		public boolean includeColumn(DataColumnSpec colSpec) {
			return elementType != null && elementType.isASuperTypeOf(colSpec.getType());
		}

		@Override
		public String allFilteredMsg() {
			return "No Columns of the appropriate type in the input table";
		}
	}

	/**
	 * A column filter to allow collection columns through
	 */
	static final ColumnFilter COLLECTION_FILTER = new ColumnFilter() {

		@Override
		public boolean includeColumn(DataColumnSpec colSpec) {
			return colSpec.getType().isCollectionType();
		}

		@Override
		public String allFilteredMsg() {
			return "No collection columns found in the input table";
		}
	};

	private DataTableSpec inSpec;
	private final SettingsModelFilterString filterColModel;
	private final DialogComponentColumnFilter filterPanel;
	private final SettingsModelString collectionColumnNameMdl;
	private final SettingsModelBoolean sortListMdl;

	AppendCollectionNodeDialog() {
		this(true);
	}

	AppendCollectionNodeDialog(boolean allowListSorting) {
		super();

		collectionColumnNameMdl = createCollectionColumnNameModel();
		filterColModel = createFilterColumnModel();
		filterPanel = new DialogComponentColumnFilter(filterColModel, 0, true, EmptyColumnFilter.getInstance(), true);
		sortListMdl = allowListSorting ? createSortListModel() : null;

		collectionColumnNameMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (inSpec != null) {
					updateAfterCollColChange();
				}
			}
		});

		addDialogComponent(new DialogComponentColumnNameSelection(collectionColumnNameMdl, COLLECTION_COLUMN, 0,
				getCollectionFilter()));
		if (allowListSorting) {
			addDialogComponent(new DialogComponentBoolean(sortListMdl, SORT_LIST));
		}

		createNewGroup(COLUMNS_TO_APPEND);
		addDialogComponent(filterPanel);
		doBeforeRemoveAppendedColumnsOption();
		addDialogComponent(new DialogComponentBoolean(createRemoveAppendedColumnsModel(), REMOVE_APPENDED_COLUMNS));
		doAfterRemoveAppendedColumnsOption();
		closeCurrentGroup();

		addDialogComponent(new DialogComponentBoolean(createTreatMissingCollectionCellsAsEmptyCollectionsModel(),
				TREAT_MISSING_COLLECTION_CELLS_AS_EMPTY_COLLECTIONS));
		addDialogComponent(
				new DialogComponentBoolean(createDontAddMissingCellsModel(), DON_T_ADD_MISSING_CELLS_TO_COLLECTION));

	}

	protected ColumnFilter getCollectionFilter() {
		return COLLECTION_FILTER;
	}

	protected void doAfterRemoveAppendedColumnsOption() {
		// Hook

	}

	protected void doBeforeRemoveAppendedColumnsOption() {
		// Hook

	}

	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs)
			throws NotConfigurableException {
		inSpec = specs[0];
		updateAfterCollColChange();

		super.loadAdditionalSettingsFrom(settings, specs);
	}

	private void updateAfterCollColChange() {
		final DataColumnSpec collColSpec = inSpec.getColumnSpec(collectionColumnNameMdl.getStringValue());
		if (collColSpec != null) {
			filterPanel.setColumnFilter(new ElementColumnFilter(collColSpec.getType().getCollectionElementType()));
			if (sortListMdl != null) {
				sortListMdl.setEnabled(collColSpec.getType().getCellClass().equals(ListCell.class));
			}
		} else {
			filterPanel.setColumnFilter(EmptyColumnFilter.getInstance());
		}
	}

	static SettingsModelBoolean createSortListModel() {
		return new SettingsModelBoolean(SORT_LIST, false);
	}

	static SettingsModelBoolean createRemoveAppendedColumnsModel() {
		return new SettingsModelBoolean(REMOVE_APPENDED_COLUMNS, false);
	}

	static SettingsModelBoolean createDontAddMissingCellsModel() {
		return new SettingsModelBoolean(DON_T_ADD_MISSING_CELLS_TO_COLLECTION, false);
	}

	static SettingsModelBoolean createTreatMissingCollectionCellsAsEmptyCollectionsModel() {
		return new SettingsModelBoolean(TREAT_MISSING_COLLECTION_CELLS_AS_EMPTY_COLLECTIONS, true);
	}

	static SettingsModelFilterString createFilterColumnModel() {
		return new SettingsModelFilterString(COLUMNS_TO_APPEND);
	}

	static SettingsModelString createCollectionColumnNameModel() {
		return new SettingsModelString(COLLECTION_COLUMN, null);
	}
}
