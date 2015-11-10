/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2013, Vernalis (R&D) Ltd
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ------------------------------------------------------------------------
 */
package com.vernalis.nodes.io.txt;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.BooleanCell.BooleanCellFactory;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.helpers.FileHelpers;

/**
 * This is the model implementation of SaveTxtLocal. Node to save a String cell
 * column (as string) to a local file with path specified in a second column
 * 
 * @author SDR
 */
public class SaveTxtLocalNodeModel extends NodeModel {
	// the logger instance
	private static final NodeLogger logger = NodeLogger.getLogger(SaveTxtLocalNodeModel.class);

	/**
	 * the settings key which is used to retrieve and store the settings (from
	 * the dialog or from a settings file) (package visibility to be usable from
	 * the dialog).
	 */

	static final String CFG_FILE_COLUMN_NAME = "File_column_name";
	static final String CFG_PATH_COLUMN_NAME = "PATH_column_name";
	static final String CFG_SUCCESS_COLUMN_NAME = "SUCCESS_column_name";
	static final String CFG_OVERWRITE = "Overwrite_flag";

	private final SettingsModelString m_TxtcolumnName = new SettingsModelString(
			CFG_FILE_COLUMN_NAME, null);

	private final SettingsModelString m_PathcolumnName = new SettingsModelString(
			CFG_PATH_COLUMN_NAME, null);

	private final SettingsModelString m_SuccesscolumnName = new SettingsModelString(
			CFG_SUCCESS_COLUMN_NAME, "Success");

	private final SettingsModelBoolean m_Overwrite = new SettingsModelBoolean(CFG_OVERWRITE, true);

	/**
	 * Constructor for the node model.
	 */
	protected SaveTxtLocalNodeModel() {

		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		// the data table spec of the single output table,
		// the table will have three columns:
		ColumnRearranger c = createColumnRearranger(inData[0].getDataTableSpec());
		BufferedDataTable out = exec.createColumnRearrangeTable(inData[0], c, exec);
		return new BufferedDataTable[] { out };
	}

	private ColumnRearranger createColumnRearranger(DataTableSpec in) {
		ColumnRearranger c = new ColumnRearranger(in);
		// The column index of the selected column
		final int colIndexTxt = in.findColumnIndex(m_TxtcolumnName.getStringValue());
		final int colIndexPath = in.findColumnIndex(m_PathcolumnName.getStringValue());

		// column spec of the appended column
		DataColumnSpec newColSpec = new DataColumnSpecCreator(m_SuccesscolumnName.getStringValue(),
				BooleanCell.TYPE).createSpec();

		// utility object that performs the calculation
		SingleCellFactory factory = new SingleCellFactory(true, newColSpec) {
			public DataCell getCell(DataRow row) {
				DataCell txtcell = row.getCell(colIndexTxt);
				DataCell pathcell = row.getCell(colIndexPath);

				if (txtcell.isMissing() || !(txtcell instanceof StringValue)) {
					// If there is nothing in the stringcell, we return a
					// missing
					return DataType.getMissingCell();
				}

				if (pathcell.isMissing() || !(pathcell instanceof StringValue)) {
					// Return missing if the path is not present
					return DataType.getMissingCell();
				}
				// Here we actually do the meat of the work and save the txt
				// file
				// 1. Check if the directory exist and create it if not

				String pathToFile = ((StringValue) pathcell).getStringValue();
				if (!(FileHelpers.isPath(pathToFile))) {
					// Well it doesnt look like a path so return fail!
					logger.info("Path '" + pathToFile + "' does not look like a path!");
					return BooleanCell.FALSE;
				}
				if (!(FileHelpers.checkContainerFolderExists(pathToFile))
						&& !(FileHelpers.createContainerFolder(pathToFile))) {
					// Folder doesnt exist and for unknown reason we fail to
					// make it
					logger.info("Container folder for file '" + pathToFile
							+ "' does not exist and could not be created");
					return BooleanCell.FALSE;
				}
				// Now we actually write the file..
				String stringText = ((StringValue) txtcell).getStringValue();
				return BooleanCellFactory.create(FileHelpers.saveStringToPath(stringText,
						pathToFile, m_Overwrite.getBooleanValue()));
			}
		};
		c.append(factory);
		return c;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		// Check the selection for the txt column
		int colIndex = -1;
		if (m_TxtcolumnName.getStringValue() == null) {
			int i = 0;
			for (DataColumnSpec cs : inSpecs[0]) {
				if (cs.getType().isCompatible(StringValue.class)) {
					if (colIndex != -1) {
						setWarningMessage("No String cell column selected");
						throw new InvalidSettingsException("No String cell column selected.");
					}
					colIndex = i;
				}
				i++;
			}

			if (colIndex == -1) {
				setWarningMessage("No String cell column selected");
				throw new InvalidSettingsException("No String cell column selected.");
			}
			m_TxtcolumnName.setStringValue(inSpecs[0].getColumnSpec(colIndex).getName());
			setWarningMessage("Column '" + m_TxtcolumnName.getStringValue()
					+ "' auto selected for String column");
		} else {
			colIndex = inSpecs[0].findColumnIndex(m_TxtcolumnName.getStringValue());
			if (colIndex < 0) {
				setWarningMessage("No such column: " + m_TxtcolumnName.getStringValue());
				throw new InvalidSettingsException(
						"No such column: " + m_TxtcolumnName.getStringValue());
			}

			DataColumnSpec colSpec = inSpecs[0].getColumnSpec(colIndex);
			if (!colSpec.getType().isCompatible(StringValue.class)) {
				setWarningMessage(
						"Column \"" + m_TxtcolumnName + "\" does not contain string values");
				throw new InvalidSettingsException("Column \"" + m_TxtcolumnName
						+ "\" does not contain string values: " + colSpec.getType().toString());
			}
		}

		// Check the selection for the Filepath column
		// NB No checking is done that the cells contain viable paths
		colIndex = -1;
		if (m_PathcolumnName.getStringValue() == null) {
			int i = 0;
			for (DataColumnSpec cs : inSpecs[0]) {
				if (cs.getType().isCompatible(StringValue.class)) {
					if (colIndex != -1) {
						setWarningMessage("No String cell column selected.");
						throw new InvalidSettingsException("No String cell column selected.");
					}
					colIndex = i;
				}
				i++;
			}

			if (colIndex == -1) {
				throw new InvalidSettingsException("No String cell column selected.");
			}
			m_PathcolumnName.setStringValue(inSpecs[0].getColumnSpec(colIndex).getName());
			setWarningMessage("Column '" + m_PathcolumnName.getStringValue()
					+ "' auto selected for String column");
		} else {
			colIndex = inSpecs[0].findColumnIndex(m_PathcolumnName.getStringValue());
			if (colIndex < 0) {
				setWarningMessage("No such column: " + m_PathcolumnName.getStringValue());
				throw new InvalidSettingsException(
						"No such column: " + m_PathcolumnName.getStringValue());
			}

			DataColumnSpec colSpec = inSpecs[0].getColumnSpec(colIndex);
			if (!colSpec.getType().isCompatible(StringValue.class)) {
				setWarningMessage(
						"Column \"" + m_PathcolumnName + "\" does not contain string values");
				throw new InvalidSettingsException("Column \"" + m_PathcolumnName
						+ "\" does not contain string values: " + colSpec.getType().toString());
			}
		}

		if (m_SuccesscolumnName.getStringValue().equals("") || m_SuccesscolumnName == null) {
			setWarningMessage("Success column name cannot be empty");
			throw new InvalidSettingsException("Success column name cannot be empty");
		}

		// everything seems to fine
		ColumnRearranger c = createColumnRearranger(inSpecs[0]);
		return new DataTableSpec[] { c.createSpec() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		m_TxtcolumnName.saveSettingsTo(settings);
		m_PathcolumnName.saveSettingsTo(settings);
		m_Overwrite.saveSettingsTo(settings);
		m_SuccesscolumnName.saveSettingsTo(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		m_TxtcolumnName.loadSettingsFrom(settings);
		m_PathcolumnName.loadSettingsFrom(settings);
		m_Overwrite.loadSettingsFrom(settings);
		m_SuccesscolumnName.loadSettingsFrom(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		m_TxtcolumnName.validateSettings(settings);
		m_PathcolumnName.validateSettings(settings);
		m_Overwrite.validateSettings(settings);
		m_SuccesscolumnName.validateSettings(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

}
