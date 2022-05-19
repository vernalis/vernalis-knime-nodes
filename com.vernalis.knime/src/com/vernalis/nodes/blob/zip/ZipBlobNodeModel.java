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
package com.vernalis.nodes.blob.zip;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.blob.BinaryObjectCellFactory;
import org.knime.core.data.blob.BinaryObjectDataCell;
import org.knime.core.data.blob.BinaryObjectDataValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.nodes.SettingsModelRegistry;

import static com.vernalis.nodes.blob.zip.ZipBlobNodeDialog.BLOB_COLUMN_FILTER;
import static com.vernalis.nodes.blob.zip.ZipBlobNodeDialog.createBlobColNameModel;
import static com.vernalis.nodes.blob.zip.ZipBlobNodeDialog.createCompressionLevelModel;
import static com.vernalis.nodes.blob.zip.ZipBlobNodeDialog.createKeepDirectoriesModel;
import static com.vernalis.nodes.blob.zip.ZipBlobNodeDialog.createZipCommentModel;
import static com.vernalis.nodes.blob.zip.ZipBlobNodeDialog.createZipPathColumnModel;

/**
 * {@link NodeModel} for the Zip Blobs node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class ZipBlobNodeModel extends NodeModel
		implements SettingsModelRegistry {

	private final Set<SettingsModel> models = new LinkedHashSet<>();

	private final SettingsModelString blobColNameMdl =
			registerSettingsModel(createBlobColNameModel());
	private final SettingsModelColumnName pathColNameMdl =
			registerSettingsModel(createZipPathColumnModel());
	private final SettingsModelBoolean keepEmptyBlobsMdl =
			registerSettingsModel(createKeepDirectoriesModel());
	private final SettingsModelString zipCommentMdl =
			registerSettingsModel(createZipCommentModel());
	private final SettingsModelIntegerBounded compressionLevelMdl =
			registerSettingsModel(createCompressionLevelModel());

	/**
	 * Constructor
	 */
	protected ZipBlobNodeModel() {
		super(1, 1);
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		// Need a blob col...
		getValidatedColumnSelectionModelColumnIndex(blobColNameMdl,
				BLOB_COLUMN_FILTER, inSpecs[0], getLogger());

		// Might need a column, but only if it isnt using the row id..
		if (!pathColNameMdl.useRowID()) {
			if (inSpecs[0]
					.getColumnSpec(pathColNameMdl.getColumnName()) == null) {
				throw new InvalidSettingsException(
						"The column '" + pathColNameMdl.getColumnName()
								+ "' is not in the input table");
			}
		}
		return new DataTableSpec[] { createOutputSpec() };
	}

	private DataTableSpec createOutputSpec() {
		return new DataTableSpecCreator().addColumns(new DataColumnSpecCreator(
				blobColNameMdl.getStringValue() + " (Zipped)",
				BinaryObjectDataCell.TYPE).createSpec()).createSpec();
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {
		final BufferedDataTable inTable = inData[0];
		DataTableSpec inSpec = inTable.getDataTableSpec();

		int blobColIdx =
				inSpec.findColumnIndex(blobColNameMdl.getStringValue());
		if (blobColIdx < 0) {
			throw new InvalidSettingsException(
					String.format("Column '%s' is not present in the table",
							blobColNameMdl.getStringValue()));
		}
		if (!inSpec.getColumnSpec(blobColIdx).getType()
				.isCompatible(BinaryObjectDataValue.class)) {
			throw new InvalidSettingsException(
					String.format("Column '%s' is not a Binary Object column",
							blobColNameMdl.getStringValue()));
		}

		int pathColIdx;
		if (pathColNameMdl.useRowID()) {
			pathColIdx = -1;
		} else {
			pathColIdx = inSpec.findColumnIndex(pathColNameMdl.getColumnName());
			if (pathColIdx < 0) {
				throw new InvalidSettingsException(
						String.format("Column '%s' is not present in the table",
								pathColNameMdl.getStringValue()));
			}
			if (!inSpec.getColumnSpec(pathColIdx).getType()
					.isCompatible(StringValue.class)) {
				throw new InvalidSettingsException(
						String.format("Column '%s' is not a String column",
								pathColNameMdl.getStringValue()));
			}
		}

		PipedOutputStream pipe = new PipedOutputStream();
		PipedInputStream zipped = new PipedInputStream(pipe);

		// Pass exception from the thread below back to the user rather than the
		// otherwise rather cryptic pipe end closed error
		// Based on https://stackoverflow.com/a/45510494/6076839
		AtomicReference<Exception> osException = new AtomicReference<>();
		FilterInputStream fis = new FilterInputStream(zipped) {

			@Override
			public void close() throws IOException {
				try {
					Exception e = osException.get();
					if (e != null) {
						throw new IOException(
								"Error in writer - " + e.getMessage(), e);
					}
				} finally {
					super.close();
				}
			}

		};

		final Thread thread = new Thread(() -> {
			long rows = inTable.size();
			double progPerRow = 1.0 / rows;
			long rowCnt = 0l;
			// We have to create the output stream in the writer thread
			ZipOutputStream deflater = new ZipOutputStream(pipe);
			deflater.setLevel(compressionLevelMdl.getIntValue());
			if (zipCommentMdl.getStringValue() != null
					&& !zipCommentMdl.getStringValue().isEmpty()) {
				deflater.setComment(zipCommentMdl.getStringValue());
			}
			for (DataRow row : inTable) {
				exec.setProgress(rowCnt * progPerRow, "Read " + (rowCnt++)
						+ " of " + rows + " binary objects");
				try {
					exec.checkCanceled();
				} catch (CanceledExecutionException e1) {
					osException.set(e1);
				}
				String path;
				if (pathColNameMdl.useRowID()) {
					path = row.getKey().getString();
				} else {
					DataCell pathCell = row.getCell(pathColIdx);
					if (pathCell.isMissing()) {
						continue;
					}
					path = ((StringValue) pathCell).getStringValue();
				}
				if (path == null || path.isEmpty()) {
					continue;
				}

				DataCell blobCell = row.getCell(blobColIdx);
				if (blobCell.isMissing()) {
					try {
						if (keepEmptyBlobsMdl.getBooleanValue()) {
							if (path.endsWith("/")) {
								deflater.putNextEntry(new ZipEntry(path));
								deflater.closeEntry();
							} else {
								deflater.putNextEntry(new ZipEntry(path + "/"));
								deflater.closeEntry();
							}
						}
					} catch (IOException e) {
						osException.set(e);
					}
					continue;
				}

				try (InputStream is =
						((BinaryObjectDataValue) blobCell).openInputStream()) {
					ZipEntry entry = new ZipEntry(path);
					deflater.putNextEntry(entry);
					IOUtils.copy(is, deflater);
					deflater.flush();
					deflater.closeEntry();
				} catch (IOException e) {
					osException.set(e);
				}

			}
			try {
				deflater.finish();
				deflater.close();
				pipe.close();
			} catch (IOException e) {
				osException.set(e);
			}
		}, "Zipper Thread");
		thread.start();

		BinaryObjectCellFactory cellFact = new BinaryObjectCellFactory(exec);
		BufferedDataContainer bdc =
				exec.createDataContainer(createOutputSpec());
		bdc.addRowToTable(new DefaultRow(RowKey.createRowKey(0L),
				cellFact.create(zipped)));
		fis.close();
		bdc.close();
		return new BufferedDataTable[] { bdc.getTable() };
	}

	@Override
	public Set<SettingsModel> getModels() {
		return models;
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		//

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		//

	}

	@Override
	public void saveSettingsTo(NodeSettingsWO settings) {
		SettingsModelRegistry.super.saveSettingsTo(settings);

	}

	@Override
	public void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		SettingsModelRegistry.super.validateSettings(settings);

	}

	@Override
	public void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		SettingsModelRegistry.super.loadValidatedSettingsFrom(settings);

	}

	@Override
	protected void reset() {
		//
	}

}
