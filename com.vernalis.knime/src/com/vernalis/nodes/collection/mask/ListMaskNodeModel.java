/*******************************************************************************
 * Copyright (c) 2022,2023, Vernalis (R&D) Ltd
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

import org.knime.core.data.BooleanValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.IntValue;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListDataValue;
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.nodes.collection.abstrct.AbstractMultiCollectionNodeModel;

import static com.vernalis.nodes.collection.mask.ListMaskNodeDialog.LIST_COLUMNS_TO_MASK;
import static com.vernalis.nodes.collection.mask.ListMaskNodeDialog.MASK_COLUMN_FILTER;
import static com.vernalis.nodes.collection.mask.ListMaskNodeDialog.createMaskColumnNameModel;
import static com.vernalis.nodes.collection.mask.ListMaskNodeDialog.createMissingMasksEmptyModel;
import static com.vernalis.nodes.collection.mask.ListMaskNodeDialog.createUseIntMaskAsIndicesModel;

/**
 * NodeModel for the Mask List columns node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class ListMaskNodeModel extends AbstractMultiCollectionNodeModel {

	private final SettingsModelString maskColNameMdl =
			registerSettingsModel(createMaskColumnNameModel());
	private final SettingsModelBoolean missingAsEmptyMaskMdl =
			registerSettingsModel(createMissingMasksEmptyModel());
	private final SettingsModelBoolean intsAsIndicesMdl =
			registerSettingsModel(createUseIntMaskAsIndicesModel());
	private int maskColIdx;

	/**
	 * Constructor
	 *
	 * @since 1.36.2
	 */
	protected ListMaskNodeModel() {
		super(LIST_COLUMNS_TO_MASK, false, true, false);
	}

	@Override
	protected void doConfigure(DataTableSpec spec)
			throws InvalidSettingsException {
		maskColIdx = getValidatedColumnSelectionModelColumnIndex(maskColNameMdl,
				MASK_COLUMN_FILTER, spec, getLogger());
	}

	@Override
	protected void reset() {
		super.reset();
		maskColIdx = -1;
	}

	@Override
	protected DataColumnSpec[] createNewColumnSpecs(DataTableSpec spec,
			int[] idx) {
		return Arrays.stream(idx).mapToObj(spec::getColumnSpec)
				.toArray(DataColumnSpec[]::new);
	}

	@Override
	protected DataCell[] getCells(int[] idx, DataRow row,
			DataColumnSpec[] newColSpecs) throws RuntimeException {
		DataCell maskCell = row.getCell(maskColIdx);

		// Handle missing mask according to settings
		if (maskCell.isMissing()) {
			if (missingAsEmptyMaskMdl.getBooleanValue()) {
				return ArrayUtils.of(
						() -> CollectionCellFactory
								.createListCell(Collections.emptyList()),
						idx.length);
			} else {
				return Arrays.stream(idx).mapToObj(row::getCell)
						.toArray(DataCell[]::new);
			}
		}

		// Create the mask
		BitSet mask = generateMask(maskCell);

		final ListDataValue[] lists = Arrays.stream(idx).mapToObj(row::getCell)
				.map(dc -> dc.isMissing() ? null : (ListDataValue) dc)
				.toArray(ListDataValue[]::new);
		return Arrays.stream(lists).map(x -> x == null
				? DataType.getMissingCell()
				: CollectionCellFactory.createListCell(mask.stream()
						.filter(y -> y < x.size()).mapToObj(x::get).toList()))
				.toArray(DataCell[]::new);
	}

	private final BitSet generateMask(DataCell maskCell) {
		BitSet mask = new BitSet();
		if (maskCell instanceof ListDataValue maskListValue) {
			for (int i = 0; i < maskListValue.size(); i++) {
				DataCell l = maskListValue.get(i);
				if (l.isMissing()) {
					continue;
				}
				if (l instanceof BooleanValue bv) {
					if (bv.getBooleanValue()) {
						mask.set(i);
					}
				} else if (l instanceof IntValue iv) {
					if (intsAsIndicesMdl.getBooleanValue()) {
						mask.set(iv.getIntValue());
					} else if (iv.getIntValue() > 0) {
						mask.set(i);
					}

				}
			}
		} else if (maskCell instanceof BitVectorValue bvv) {
			for (long i = bvv.nextSetBit(0); i >= 0
					&& i <= Integer.MAX_VALUE; i = bvv.nextSetBit(i + 1)) {
				mask.set((int) i);
			}
		}
		return mask;
	}

}
