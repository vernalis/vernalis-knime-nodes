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

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListDataValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

import static com.vernalis.nodes.collection.missing.MissingToEmptyCollectionNodeDialog.COLLECTION_FILTER;
import static com.vernalis.nodes.collection.missing.MissingToEmptyCollectionNodeDialog.createColumnsModel;
import static com.vernalis.nodes.collection.missing.MissingToEmptyCollectionNodeDialog.createReplaceInputColumnsModel;

/**
 * Node Model implementation for the Missing cell to Empty collection node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class MissingToEmptyCollectionNodeModel
		extends AbstractSimpleStreamableFunctionNodeModel {

	private final SettingsModelColumnFilter2 collColsMdl =
			registerSettingsModel(createColumnsModel());
	private final SettingsModelBoolean replaceInputsMdl =
			registerSettingsModel(createReplaceInputColumnsModel());

	public MissingToEmptyCollectionNodeModel() {

	}

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {

		// Firstly check we have any columns, and any selected
		if (!spec.stream().filter(colSpec -> COLLECTION_FILTER.include(colSpec))
				.findAny().isPresent()) {
			throw new InvalidSettingsException(
					"No collection columns in the incoming table");
		}

		final int[] colIdx =
				Arrays.stream(collColsMdl.applyTo(spec).getIncludes())
						.mapToInt(name -> spec.findColumnIndex(name)).toArray();
		if (colIdx.length == 0) {
			throw new InvalidSettingsException(
					"No collection columns selected!");
		}

		// Now generate the output spec
		String colNameSuffix =
				replaceInputsMdl.getBooleanValue() ? "" : " (Missing -> Empty)";
		final DataColumnSpec[] newColSpecs = new DataColumnSpec[colIdx.length];
		BitSet isList = new BitSet();
		for (int i = 0; i < newColSpecs.length; i++) {
			String newColName =
					spec.getColumnSpec(colIdx[i]).getName() + colNameSuffix;
			if (!replaceInputsMdl.getBooleanValue()) {
				newColName =
						DataTableSpec.getUniqueColumnName(spec, newColName);
			}
			newColSpecs[i] = new DataColumnSpecCreator(newColName,
					spec.getColumnSpec(colIdx[i]).getType()).createSpec();
			if (newColSpecs[i].getType().isCompatible(ListDataValue.class)) {
				isList.set(i);
			}
		}

		// Create the cellfactory
		CellFactory cellFact = new AbstractCellFactory(newColSpecs) {

			@Override
			public DataCell[] getCells(DataRow row) {

				DataCell[] retVal =
						ArrayUtils.fill(new DataCell[newColSpecs.length],
								DataType.getMissingCell());
				int cellIndex = 0;
				for (int col : colIdx) {
					DataCell colCell = row.getCell(col);
					retVal[cellIndex] = colCell.isMissing()
							? isList.get(cellIndex)
									? CollectionCellFactory.createListCell(
											Collections.emptyList())
									: CollectionCellFactory.createSetCell(
											Collections.emptySet())
							: colCell;
					cellIndex++;
				}
				return retVal;
			}
		};

		// Create the rearranger, and apply the cell factory appropriately
		ColumnRearranger rearranger = new ColumnRearranger(spec);
		if (replaceInputsMdl.getBooleanValue()) {
			rearranger.replace(cellFact, colIdx);
		} else {
			rearranger.append(cellFact);
		}
		return rearranger;
	}

}
