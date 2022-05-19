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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
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
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

import static com.vernalis.nodes.blob.gzip.UnGzipBlobNodeDialog.BLOB_COLUMN_FILTER;
import static com.vernalis.nodes.blob.gzip.UnGzipBlobNodeDialog.createBlobColNameModel;
import static com.vernalis.nodes.blob.gzip.UnGzipBlobNodeDialog.createReplaceInputColModel;;

/**
 * NodeModel for the GZip Blob node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class GzipBlobNodeModel
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
												+ " (GZip'd)"),
								BinaryObjectDataCell.TYPE).createSpec();
		DataColumnSpec compressionColSpec = new DataColumnSpecCreator(
				DataTableSpec.getUniqueColumnName(spec,
						String.format("Compression Ratio (%s)",
								blobColNameMdl.getStringValue())),
				DoubleCell.TYPE).createSpec();
		// We dont have an exec at configure time, but we dont need it then
		// either
		BinaryObjectCellFactory bocf =
				exec == null ? null : new BinaryObjectCellFactory(exec);
		AbstractCellFactory acf =
				new AbstractCellFactory(newColSpec, compressionColSpec) {

					@Override
					public DataCell[] getCells(DataRow row) {
						DataCell inCell = row.getCell(colIdx);
						DataCell[] retVal = ArrayUtils.fill(new DataCell[2],
								DataType.getMissingCell());
						if (inCell.isMissing()) {
							return retVal;
						}
						BinaryObjectDataValue inBlobVal =
								(BinaryObjectDataValue) inCell;
						double compressionRatio = 1.0 / inBlobVal.length();
						try (InputStream is = inBlobVal.openInputStream()) {
							// These cannot go in the try-with-resources block,
							// or they
							// break!
							PipedInputStream gzipped = new PipedInputStream();
							PipedOutputStream pipe =
									new PipedOutputStream(gzipped);
							new Thread(() -> {
								try (OutputStream deflater =
										new GZIPOutputStream(pipe)) {
									IOUtils.copy(is, deflater);
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							}).start();

							retVal[0] = bocf.create(gzipped);
							retVal[1] = new DoubleCell(
									((BinaryObjectDataValue) retVal[0]).length()
											* compressionRatio);
						} catch (IOException | RuntimeException e) {
							setWarningMessage("Errors converting some rows - "
									+ e.getMessage());
							retVal[0] = new MissingCell(e.getMessage());
						}
						return retVal;
					}
				};
		if (replaceInputColMdl.getBooleanValue()) {
			rearranger.remove(colIdx);
			rearranger.insertAt(colIdx, acf);
		} else {
			rearranger.append(acf);
		}
		return rearranger;
	}

	@Override
	protected void reset() {
		exec = null;
		super.reset();
	}

}
