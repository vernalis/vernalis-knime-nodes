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
package com.vernalis.knime.misc.blobs.nodes.zip;

import static com.vernalis.knime.misc.blobs.nodes.BlobConstants.BLOB_COLUMN_FILTER;
import static com.vernalis.knime.misc.blobs.nodes.zip.UnZipBlobNodeDialog.createBlobColNameModel;
import static com.vernalis.knime.misc.blobs.nodes.zip.UnZipBlobNodeDialog.createKeepDirectoriesModel;
import static com.vernalis.knime.misc.blobs.nodes.zip.UnZipBlobNodeDialog.createRemoveInputColumnModel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.MissingCell;
import org.knime.core.data.RowKey;
import org.knime.core.data.append.AppendedColumnRow;
import org.knime.core.data.blob.BinaryObjectCellFactory;
import org.knime.core.data.blob.BinaryObjectDataCell;
import org.knime.core.data.blob.BinaryObjectDataValue;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.util.NonClosableInputStream.Zip;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.data.datarow.RemoveColumnsDataRow;
import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.nodes.SettingsModelRegistry;

/**
 * NodeModel for the UnZip blobs node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class UnZipBlobNodeModel extends NodeModel
		implements SettingsModelRegistry {

	private final Set<SettingsModel> models = new LinkedHashSet<>();
	private final SettingsModelString blobColNameMdl =
			registerSettingsModel(createBlobColNameModel());
	private final SettingsModelBoolean keepDirsMdl =
			registerSettingsModel(createKeepDirectoriesModel());
	private final SettingsModelBoolean removeInputColMdl =
			registerSettingsModel(createRemoveInputColumnModel());

	/**
	 * Constructor
	 */
	protected UnZipBlobNodeModel() {
		super(1, 1);
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		return new DataTableSpec[] { createOutputSpec(inSpecs[0],
				getValidatedColumnSelectionModelColumnIndex(blobColNameMdl,
						BLOB_COLUMN_FILTER, inSpecs[0], getLogger())) };
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {

		final BufferedDataTable inTable = inData[0];
		DataTableSpec spec = inTable.getDataTableSpec();

		int colIdx = getValidatedColumnSelectionModelColumnIndex(blobColNameMdl,
				BLOB_COLUMN_FILTER, spec, getLogger());

		DataTableSpec outSpec = createOutputSpec(spec, colIdx);

		BinaryObjectCellFactory bocf = new BinaryObjectCellFactory(exec);

		long length = inTable.size();
		double progPerRow = 1.0 / length;
		long rowCnt = 0l;
		BufferedDataContainer bdc = exec.createDataContainer(outSpec);

		CloseableRowIterator iter = inTable.iterator();
		while (iter.hasNext()) {
			exec.setProgress(progPerRow * rowCnt,
					"Processing row " + (rowCnt++) + " of " + length);
			exec.checkCanceled();
			DataRow row = iter.next();
			DataRow baseOutRow = removeInputColMdl.getBooleanValue()
					? new RemoveColumnsDataRow(row, colIdx)
					: row;
			DataCell inCell = row.getCell(colIdx);
			if (inCell.isMissing()) {
				DataRow outRow = new AppendedColumnRow(baseOutRow, ArrayUtils
						.fill(new DataCell[2], DataType.getMissingCell()));
				bdc.addRowToTable(outRow);
				continue;
			}

			BinaryObjectDataValue bdv = (BinaryObjectDataValue) inCell;
			try (InputStream is = bdv.openInputStream();
					ZipInputStream zis = new ZipInputStream(is)) {
				int subRow = 0;
				// We need this to prevent the first ZipEntry closing the input
				// stream when it is used in the cell factory
				Zip zip = new Zip(zis);
				ZipEntry zipEntry = zis.getNextEntry();
				while (zipEntry != null) {
					if (zipEntry.isDirectory()
							&& !keepDirsMdl.getBooleanValue()) {
						zipEntry = zis.getNextEntry();
						continue;
					} else {
						DataCell[] newCells = new DataCell[2];
						newCells[0] = new StringCell(zipEntry.getName());
						if (zipEntry.isDirectory()) {
							newCells[1] = DataType.getMissingCell();
						} else {
							try {
								newCells[1] = bocf.create(zip);
							} catch (IOException e) {
								newCells[1] = new MissingCell(e.getMessage());
								setWarningMessage(
										"Not all Zip Entry processed successfully");
							}
						}
						bdc.addRowToTable(
								new AppendedColumnRow(
										new RowKey(String.format("%s_%s",
												baseOutRow.getKey().getString(),
												subRow++)),
										baseOutRow, newCells));
					}
					zipEntry = zis.getNextEntry();
				}
			}
		}
		bdc.close();
		return new BufferedDataTable[] { bdc.getTable() };

	}

	/**
	 * @param spec
	 * @param colIdx
	 * @return
	 */
	private DataTableSpec createOutputSpec(DataTableSpec spec, int colIdx) {
		DataColumnSpec[] newColSpecs =
				new DataColumnSpec[] {
						new DataColumnSpecCreator(DataTableSpec
								.getUniqueColumnName(spec, "Zip Path"),
								StringCell.TYPE).createSpec(),
						new DataColumnSpecCreator(
								DataTableSpec.getUniqueColumnName(spec,
										spec.getColumnNames()[colIdx]
												+ " (Un-Zip'd)"),
								BinaryObjectDataCell.TYPE).createSpec() };

		DataTableSpecCreator outSpecFact;
		if (removeInputColMdl.getBooleanValue()) {
			outSpecFact = new DataTableSpecCreator();
			spec.stream().filter(
					x -> !x.getName().equals(blobColNameMdl.getStringValue()))
					.forEachOrdered(x -> outSpecFact.addColumns(x));
		} else {
			outSpecFact = new DataTableSpecCreator(spec);

		}

		outSpecFact.addColumns(newColSpecs);
		return outSpecFact.createSpec();
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
	public Set<SettingsModel> getModels() {
		return models;
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
