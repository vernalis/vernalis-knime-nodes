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

import static com.vernalis.nodes.collection.append.AppendCollectionNodeDialog.COLLECTION_FILTER;
import static com.vernalis.nodes.collection.append.AppendCollectionNodeDialog.createCollectionColumnNameModel;
import static com.vernalis.nodes.collection.append.AppendCollectionNodeDialog.createDontAddMissingCellsModel;
import static com.vernalis.nodes.collection.append.AppendCollectionNodeDialog.createFilterColumnModel;
import static com.vernalis.nodes.collection.append.AppendCollectionNodeDialog.createRemoveAppendedColumnsModel;
import static com.vernalis.nodes.collection.append.AppendCollectionNodeDialog.createSortListModel;
import static com.vernalis.nodes.collection.append.AppendCollectionNodeDialog.createTreatMissingCollectionCellsAsEmptyCollectionsModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

/**
 * Node Model implementation for the Append to collection node
 * 
 * @author Steve
 * @since 1.27.0
 */
public class AppendCollectionNodeModel extends AbstractSimpleStreamableFunctionNodeModel {

	private final SettingsModelString collColNameMdl = registerSettingsModel(createCollectionColumnNameModel());
	private final SettingsModelFilterString filterModel = registerSettingsModel(createFilterColumnModel());
	private final SettingsModelBoolean removeAppendedModel = registerSettingsModel(createRemoveAppendedColumnsModel());
	private final SettingsModelBoolean treatMissingAsEmptyCollMdl = registerSettingsModel(
			createTreatMissingCollectionCellsAsEmptyCollectionsModel());
	private final SettingsModelBoolean skipMissingCellsMdl = registerSettingsModel(createDontAddMissingCellsModel());
	private final SettingsModelBoolean sortListMdl;

	public AppendCollectionNodeModel() {
		this(true);

	}

	public AppendCollectionNodeModel(boolean allowListSorting) {
		super();
		sortListMdl = allowListSorting ? registerSettingsModel(createSortListModel()) : null;
	}

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec) throws InvalidSettingsException {
		final int collColIdx = getValidatedColumnSelectionModelColumnIndex(collColNameMdl, COLLECTION_FILTER, spec,
				getLogger());

		final int[] colsToAppend = spec.columnsToIndices(filterModel.getIncludeList().toArray(new String[0]));
		if (colsToAppend.length == 0) {
			throw new InvalidSettingsException("No columns selected to append");
		}

		final ColumnRearranger rearranger = new ColumnRearranger(spec);
		final DataColumnSpec colSpec = spec.getColumnSpec(collColIdx);
		final boolean isList = colSpec.getType().getCellClass().equals(ListCell.class);
		final DataValueComparator comparator = isList && sortListMdl != null && sortListMdl.getBooleanValue()
				? colSpec.getType().getCollectionElementType().getComparator()
						: null;
				rearranger.replace(new SingleCellFactory(colSpec) {

					@Override
					public DataCell getCell(DataRow row) {

						final DataCell collectionCell = row.getCell(collColIdx);
						if (collectionCell.isMissing() && !treatMissingAsEmptyCollMdl.getBooleanValue()) {
							return DataType.getMissingCell();
						}

						final Collection<DataCell> cells = isList ? new ArrayList<>() : new HashSet<>();
						if (!collectionCell.isMissing()) {
							((CollectionDataValue) collectionCell).forEach(c -> cells.add(c));
						}
						final DataCell[] toAdd = Arrays.stream(colsToAppend).mapToObj(idx -> row.getCell(idx))
								.filter(c -> !c.isMissing() || !skipMissingCellsMdl.getBooleanValue()).toArray(DataCell[]::new);
						doAdd(cells, toAdd);
						if (isList && sortListMdl != null && sortListMdl.getBooleanValue() && !cells.isEmpty()) {
							Collections.sort((List<DataCell>) cells, comparator);
						}

						return cells.isEmpty() ? DataType.getMissingCell()
								: isList ? CollectionCellFactory.createListCell(cells)
										: CollectionCellFactory.createSetCell(cells);
					}
				}, collColNameMdl.getStringValue());
				if (removeAppendedModel.getBooleanValue()) {
					rearranger.remove(colsToAppend);
				}
				return rearranger;
	}

	protected void doAdd(Collection<DataCell> cells, DataCell[] toAdd) {
		Collections.addAll(cells, toAdd);

	}

}
