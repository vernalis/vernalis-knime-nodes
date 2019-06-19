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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListDataValue;
import org.knime.core.data.collection.SetCell;
import org.knime.core.data.collection.SparseListDataValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

import static com.vernalis.nodes.list2set.List2SetNodeDialog.createColumnFilterModel;
import static com.vernalis.nodes.list2set.List2SetNodeDialog.createSortedModel;

public class List2SetNodeModel
		extends AbstractSimpleStreamableFunctionNodeModel {

	private final SettingsModelColumnFilter2 colFiltMdl =
			registerSettingsModel(createColumnFilterModel());
	private final SettingsModelBoolean sortedMdl =
			registerSettingsModel(createSortedModel());

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {
		String[] colNames = colFiltMdl.applyTo(spec).getIncludes();
		int[] colIdxs = Arrays.stream(colNames)
				.mapToInt(x -> spec.findColumnIndex(x)).toArray();
		DataColumnSpec[] newColSpecs = Arrays.stream(colNames)
				.map(x -> spec.getColumnSpec(x))
				.map(cSpec -> new DataColumnSpecCreator(cSpec.getName(),
						SetCell.getCollectionType(
								cSpec.getType().getCollectionElementType()))
										.createSpec())
				.toArray(DataColumnSpec[]::new);
		ColumnRearranger rearranger = new ColumnRearranger(spec);
		rearranger.replace(new AbstractCellFactory(newColSpecs) {

			@Override
			public DataCell[] getCells(DataRow row) {
				DataCell[] retVal =
						ArrayUtils.fill(new DataCell[colNames.length],
								DataType.getMissingCell());
				int colIdx = 0;
				for (int i : colIdxs) {
					DataCell collCell = row.getCell(i);
					if (collCell.isMissing()) {
						colIdx++;
						continue;
					}
					ListDataValue ldv = (ListDataValue) collCell;
					Set<DataCell> cells = sortedMdl.getBooleanValue()
							? new TreeSet<>(
									ldv.getElementType().getComparator())
							: new LinkedHashSet<>();
					if (ldv instanceof SparseListDataValue) {
						// Short-cut...
						SparseListDataValue sldv = (SparseListDataValue) ldv;
						cells.add(sldv.getDefaultElement());
						for (int j : sldv.getAllIndices()) {
							cells.add(sldv.get(j));
						}
					} else {
						ldv.forEach(cells::add);
					}
					retVal[colIdx++] =
							CollectionCellFactory.createSetCell(cells);
				}
				return retVal;
			}
		}, colIdxs);
		return rearranger;
	}

}
