/*******************************************************************************
 * Copyright (c) 2025, Vernalis (R&D) Ltd
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
package com.vernalis.knime.misc.blobs.nodes.props;

import static com.vernalis.knime.misc.blobs.nodes.BlobConstants.BLOB_COLUMN_FILTER;
import static com.vernalis.knime.misc.blobs.nodes.props.BlobPropertiesNodeDialog.createBlobColNameModel;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.blob.BinaryObjectDataCell;
import org.knime.core.data.blob.BinaryObjectDataValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.BooleanCell.BooleanCellFactory;
import org.knime.core.data.def.LongCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

/**
 * @author S.Roughley knime@vernalis.com
 *
 *
 * @since 1.38.0
 */
public class BlobPropertiesNodeModel
		extends AbstractSimpleStreamableFunctionNodeModel {

	private final SettingsModelString blobColNameMdl =
			registerSettingsModel(createBlobColNameModel());

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {

		int colIdx = getValidatedColumnSelectionModelColumnIndex(blobColNameMdl,
				BLOB_COLUMN_FILTER, spec, getLogger());
		DataColumnSpec[] newColSpecs = new DataColumnSpec[2];
		newColSpecs[0] = new DataColumnSpecCreator(
				DataTableSpec.getUniqueColumnName(spec,
						"Binary Objects Size ("
								+ blobColNameMdl.getStringValue() + ")"),
				LongCell.TYPE).createSpec();
		newColSpecs[1] = new DataColumnSpecCreator(
				DataTableSpec.getUniqueColumnName(spec,
						"Binary Objects Is In Memory ("
								+ blobColNameMdl.getStringValue() + ")"),
				BooleanCellFactory.TYPE).createSpec();

		ColumnRearranger rearranger = new ColumnRearranger(spec);
		rearranger.append(new AbstractCellFactory(newColSpecs) {

			@Override
			public DataCell[] getCells(DataRow row) {
				DataCell[] retVal = ArrayUtils.fill(new DataCell[2],
						DataType.getMissingCell());
				DataCell inCell = row.getCell(colIdx);
				if (inCell.isMissing()) {
					return retVal;
				}
				retVal[0] =
						new LongCell(((BinaryObjectDataValue) inCell).length());
				retVal[1] = BooleanCellFactory
						.create(inCell instanceof BinaryObjectDataCell);
				return retVal;
			}
		});
		return rearranger;
	}

}
