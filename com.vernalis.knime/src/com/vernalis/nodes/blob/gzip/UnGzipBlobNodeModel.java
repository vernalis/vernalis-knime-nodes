/*******************************************************************************
 * Copyright (c) 2021, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.blob.gzip;

import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.MissingCell;
import org.knime.core.data.blob.BinaryObjectCellFactory;
import org.knime.core.data.blob.BinaryObjectDataCell;
import org.knime.core.data.blob.BinaryObjectDataValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

import static com.vernalis.nodes.blob.gzip.UnGzipBlobNodeDialog.BLOB_COLUMN_FILTER;
import static com.vernalis.nodes.blob.gzip.UnGzipBlobNodeDialog.createBlobColNameModel;
import static com.vernalis.nodes.blob.gzip.UnGzipBlobNodeDialog.createReplaceInputColModel;

/**
 * NodeModel for the Un-GZip Binary Objects node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class UnGzipBlobNodeModel
		extends AbstractSimpleStreamableFunctionNodeModel {

	private final SettingsModelString blobColNameMdl =
			registerSettingsModel(createBlobColNameModel());
	private final SettingsModelBoolean replaceInputColMdl =
			registerSettingsModel(createReplaceInputColModel());
	private ExecutionContext exec;

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {
		this.exec = exec;
		return super.execute(inData, exec);
	}

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {
		int colIdx = getValidatedColumnSelectionModelColumnIndex(blobColNameMdl,
				BLOB_COLUMN_FILTER, spec, getLogger());
		ColumnRearranger rearranger = new ColumnRearranger(spec);
		DataColumnSpec newColSpec =
				replaceInputColMdl.getBooleanValue()
						? spec.getColumnSpec(colIdx)
						: new DataColumnSpecCreator(
								DataTableSpec.getUniqueColumnName(spec,
										spec.getColumnNames()[colIdx]
												+ " (Un-GZip'd)"),
								BinaryObjectDataCell.TYPE).createSpec();
		BinaryObjectCellFactory bocf =
				exec == null ? null : new BinaryObjectCellFactory(exec);
		SingleCellFactory scf = new SingleCellFactory(newColSpec) {

			@Override
			public DataCell getCell(DataRow row) {
				DataCell inCell = row.getCell(colIdx);
				if (inCell.isMissing()) {
					return DataType.getMissingCell();
				}
				BinaryObjectDataValue inBlobVal =
						(BinaryObjectDataValue) inCell;

				try (GZIPInputStream gis =
						new GZIPInputStream(inBlobVal.openInputStream())) {
					return bocf.create(gis);
				} catch (IOException e) {
					setWarningMessage(
							"Errors converting some rows - " + e.getMessage());
					return new MissingCell(e.getMessage());
				}

			}
		};
		if (replaceInputColMdl.getBooleanValue()) {
			rearranger.replace(scf, colIdx);
		} else {
			rearranger.append(scf);
		}
		return rearranger;
	}

}
