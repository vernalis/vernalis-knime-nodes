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
package com.vernalis.nodes.collection.size;

import java.util.Arrays;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;

import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

import static com.vernalis.nodes.collection.size.CollectionSizeNodeDialog.createColumnsModel;

/**
 * Node model implementation for the Collection Size node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class CollectionSizeNodeModel
		extends AbstractSimpleStreamableFunctionNodeModel {

	private final SettingsModelColumnFilter2 colsMdl =
			registerSettingsModel(createColumnsModel());

	public CollectionSizeNodeModel() {
	}

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {
		int[] idx = spec.columnsToIndices(colsMdl.applyTo(spec).getIncludes());
		if (idx.length == 0) {
			throw new InvalidSettingsException("No columns selected");
		}
		DataColumnSpec[] newColSpecs = new DataColumnSpec[idx.length];
		for (int i = 0; i < newColSpecs.length; i++) {
			newColSpecs[i] = new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(spec,
							spec.getColumnSpec(idx[i]).getName() + " Size"),
					IntCell.TYPE).createSpec();
		}
		ColumnRearranger rearranger = new ColumnRearranger(spec);
		rearranger.append(new AbstractCellFactory(newColSpecs) {

			@Override
			public DataCell[] getCells(DataRow row) {

				return Arrays.stream(idx)
						.mapToObj(colIdx -> row.getCell(colIdx))
						.map(cell -> cell.isMissing()
								? DataType.getMissingCell()
								: new IntCell(
										((CollectionDataValue) cell).size()))
						.toArray(DataCell[]::new);
			}
		});
		return rearranger;
	}

}
