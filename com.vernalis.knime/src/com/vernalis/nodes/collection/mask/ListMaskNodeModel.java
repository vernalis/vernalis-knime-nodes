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
package com.vernalis.nodes.collection.mask;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.stream.Collectors;

import org.knime.core.data.BooleanValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.IntValue;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListDataValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

import static com.vernalis.nodes.collection.mask.ListMaskNodeDialog.MASK_COLUMN_FILTER;
import static com.vernalis.nodes.collection.mask.ListMaskNodeDialog.createColumnFilterModel;
import static com.vernalis.nodes.collection.mask.ListMaskNodeDialog.createMaskColumnNameModel;
import static com.vernalis.nodes.collection.mask.ListMaskNodeDialog.createMissingMasksEmptyModel;
import static com.vernalis.nodes.collection.mask.ListMaskNodeDialog.createUseIntMaskAsIndicesModel;

/**
 * NodeModel for the Mask List columns node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class ListMaskNodeModel
		extends AbstractSimpleStreamableFunctionNodeModel {

	private final SettingsModelColumnFilter2 colFilterMdl =
			registerSettingsModel(createColumnFilterModel());
	private final SettingsModelString maskColNameMdl =
			registerSettingsModel(createMaskColumnNameModel());
	private final SettingsModelBoolean missingAsEmptyMaskMdl =
			registerSettingsModel(createMissingMasksEmptyModel());
	private final SettingsModelBoolean intsAsIndicesMdl =
			registerSettingsModel(createUseIntMaskAsIndicesModel());

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {
		int maskColIdx = getValidatedColumnSelectionModelColumnIndex(
				maskColNameMdl, MASK_COLUMN_FILTER, spec, getLogger());
		String[] colNames = colFilterMdl.applyTo(spec).getIncludes();
		if (colNames.length == 0 || (colNames.length == 1
				&& colNames[0].equals(maskColNameMdl.getStringValue()))) {
			throw new InvalidSettingsException("No columns to mask selected!");
		}
		int[] colIdx = Arrays.stream(colNames)
				.mapToInt(colName -> spec.findColumnIndex(colName)).toArray();
		ColumnRearranger rearranger = new ColumnRearranger(spec);
		rearranger.replace(new AbstractCellFactory(
				Arrays.stream(colIdx).mapToObj(idx -> spec.getColumnSpec(idx))
						.toArray(DataColumnSpec[]::new)) {

			@Override
			public DataCell[] getCells(DataRow row) {

				DataCell maskCell = row.getCell(maskColIdx);

				// Handle missing mask according to settings
				if (maskCell.isMissing()) {
					if (missingAsEmptyMaskMdl.getBooleanValue()) {
						return ArrayUtils.of(
								() -> CollectionCellFactory.createListCell(
										Collections.emptyList()),
								colNames.length);
					} else {
						return Arrays.stream(colIdx)
								.mapToObj(idx -> row.getCell(idx))
								.toArray(DataCell[]::new);
					}
				}

				// Create the mask
				BitSet mask = new BitSet();
				if (maskCell instanceof ListDataValue) {
					ListDataValue maskListValue = (ListDataValue) maskCell;
					for (int i = 0; i < maskListValue.size(); i++) {
						DataCell l = maskListValue.get(i);
						if (l.isMissing()) {
							continue;
						}
						if (l instanceof BooleanValue) {
							BooleanValue bv = (BooleanValue) l;
							if (bv.getBooleanValue()) {
								mask.set(i);
							}
						} else if (l instanceof IntValue) {
							IntValue iv = (IntValue) l;
							if (intsAsIndicesMdl.getBooleanValue()) {
								mask.set(iv.getIntValue());
							} else if (iv.getIntValue() > 0) {
								mask.set(i);
							}

						}
					}
				} else if (maskCell instanceof BitVectorValue) {
					BitVectorValue bvv = (BitVectorValue) maskCell;
					for (long i = bvv.nextSetBit(0); i >= 0
							&& i <= Integer.MAX_VALUE; i =
									bvv.nextSetBit(i + 1)) {
						mask.set((int) i);
					}
				}

				final ListDataValue[] lists = Arrays.stream(colIdx)
						.mapToObj(idx -> row.getCell(idx))
						.map(dc -> dc.isMissing() ? null : (ListDataValue) dc)
						.toArray(ListDataValue[]::new);
				return Arrays.stream(lists)
						.map(x -> x == null ? DataType.getMissingCell()
								: CollectionCellFactory.createListCell(
										mask.stream().filter(y -> y < x.size())
												.mapToObj(y -> x.get(y))
												.collect(Collectors.toList())))
						.toArray(DataCell[]::new);
			}
		}, colIdx);
		return rearranger;
	}

}
