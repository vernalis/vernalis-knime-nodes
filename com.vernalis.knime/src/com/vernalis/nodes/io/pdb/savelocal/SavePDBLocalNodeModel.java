/**************************************************************************
 * ------------------------------------------------------------------------
 *  Copyright (C) 2013, 2019 Vernalis (R&D) Ltd
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
 **************************************************************************/
package com.vernalis.nodes.io.pdb.savelocal;

import java.io.File;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.BooleanCell.BooleanCellFactory;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;

import com.vernalis.io.FileHelpers;

import static com.vernalis.nodes.io.pdb.savelocal.SavePDBLocalNodeDialog.createFolderNameModel;
import static com.vernalis.nodes.io.pdb.savelocal.SavePDBLocalNodeDialog.createUseParentFolderModel;

/**
 * This is the model implementation of SavePDBLocal. Node to save a PDB cell
 * column (as string) to a local file with path specified in a second column
 */
public class SavePDBLocalNodeModel extends SimpleStreamableFunctionNodeModel {

	// the logger instance
	private static final NodeLogger logger =
			NodeLogger.getLogger(SavePDBLocalNodeModel.class);

	/**
	 * the settings key which is used to retrieve and store the settings (from
	 * the dialog or from a settings file) (package visibility to be usable from
	 * the dialog).
	 */

	static final String CFG_PDB_COLUMN_NAME = "PDB_column_name";
	static final String CFG_PATH_COLUMN_NAME = "PATH_column_name";
	static final String CFG_SUCCESS_COLUMN_NAME = "SUCCESS_column_name";
	static final String CFG_OVERWRITE = "Overwrite_flag";

	private final SettingsModelString m_PDBcolumnName =
			new SettingsModelString(CFG_PDB_COLUMN_NAME, null);

	private final SettingsModelString m_PathcolumnName =
			new SettingsModelString(CFG_PATH_COLUMN_NAME, null);

	private final SettingsModelString m_SuccesscolumnName =
			new SettingsModelString(CFG_SUCCESS_COLUMN_NAME, "Success");

	private final SettingsModelBoolean m_Overwrite =
			new SettingsModelBoolean(CFG_OVERWRITE, true);

	private final SettingsModelBoolean userParentFolderMdl =
			createUseParentFolderModel();
	private final SettingsModelString parentFolderMdl = createFolderNameModel();

	/**
	 * Constructor for the node model.
	 */
	protected SavePDBLocalNodeModel() {
		super();

		userParentFolderMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				parentFolderMdl
						.setEnabled(userParentFolderMdl.getBooleanValue());

			}
		});
		parentFolderMdl.setEnabled(userParentFolderMdl.getBooleanValue());
	}

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec inSpec)
			throws InvalidSettingsException {
		ColumnRearranger c = new ColumnRearranger(inSpec);

		// Check the selection for the pdb column
		int colIndex = -1;
		if (m_PDBcolumnName.getStringValue() == null) {
			int i = 0;
			for (DataColumnSpec cs : inSpec) {
				if (cs.getType().isCompatible(StringValue.class)) {
					if (colIndex != -1) {
						setWarningMessage("No PDB cell column selected");
						throw new InvalidSettingsException(
								"No PDB cell column selected.");
					}
					colIndex = i;
				}
				i++;
			}

			if (colIndex == -1) {
				setWarningMessage("No PDB cell column selected");
				throw new InvalidSettingsException(
						"No PDB cell column selected.");
			}
			m_PDBcolumnName
					.setStringValue(inSpec.getColumnSpec(colIndex).getName());
			setWarningMessage("Column '" + m_PDBcolumnName.getStringValue()
					+ "' auto selected for PDB column");
		} else {
			colIndex = inSpec.findColumnIndex(m_PDBcolumnName.getStringValue());
			if (colIndex < 0) {
				setWarningMessage(
						"No such column: " + m_PDBcolumnName.getStringValue());
				throw new InvalidSettingsException(
						"No such column: " + m_PDBcolumnName.getStringValue());
			}

			DataColumnSpec colSpec = inSpec.getColumnSpec(colIndex);
			if (!colSpec.getType().isCompatible(StringValue.class)) {
				setWarningMessage("Column \"" + m_PDBcolumnName
						+ "\" does not contain string values");
				throw new InvalidSettingsException("Column \"" + m_PDBcolumnName
						+ "\" does not contain string values: "
						+ colSpec.getType().toString());
			}
		}

		final int colIndexPDB = colIndex;

		// Check the selection for the Filepath column
		// NB No checking is done that the cells contain viable paths
		colIndex = -1;
		if (m_PathcolumnName.getStringValue() == null) {
			int i = 0;
			for (DataColumnSpec cs : inSpec) {
				if (cs.getType().isCompatible(StringValue.class)) {
					if (colIndex != -1) {
						setWarningMessage("No PDB cell column selected.");
						throw new InvalidSettingsException(
								"No PDB cell column selected.");
					}
					colIndex = i;
				}
				i++;
			}

			if (colIndex == -1) {
				throw new InvalidSettingsException(
						"No PDB cell column selected.");
			}
			m_PathcolumnName
					.setStringValue(inSpec.getColumnSpec(colIndex).getName());
			setWarningMessage("Column '" + m_PathcolumnName.getStringValue()
					+ "' auto selected for PDB column");
		} else {
			colIndex =
					inSpec.findColumnIndex(m_PathcolumnName.getStringValue());
			if (colIndex < 0) {
				setWarningMessage(
						"No such column: " + m_PathcolumnName.getStringValue());
				throw new InvalidSettingsException(
						"No such column: " + m_PathcolumnName.getStringValue());
			}

			DataColumnSpec colSpec = inSpec.getColumnSpec(colIndex);
			if (!colSpec.getType().isCompatible(StringValue.class)) {
				setWarningMessage("Column \"" + m_PathcolumnName
						+ "\" does not contain string values");
				throw new InvalidSettingsException(
						"Column \"" + m_PathcolumnName
								+ "\" does not contain string values: "
								+ colSpec.getType().toString());
			}
		}

		if (m_SuccesscolumnName.getStringValue().equals("")
				|| m_SuccesscolumnName == null) {
			setWarningMessage("Success column name cannot be empty");
			throw new InvalidSettingsException(
					"Success column name cannot be empty");
		}

		final int colIndexPath = colIndex;

		// column spec of the appended column
		DataColumnSpec newColSpec =
				new DataColumnSpecCreator(m_SuccesscolumnName.getStringValue(),
						BooleanCell.TYPE).createSpec();

		File parentFolder = userParentFolderMdl.getBooleanValue()
				? new File(parentFolderMdl.getStringValue()) : null;

		// utility object that performs the calculation
		SingleCellFactory factory = new SingleCellFactory(true, newColSpec) {

			@Override
			public DataCell getCell(DataRow row) {
				DataCell pdbcell = row.getCell(colIndexPDB);
				DataCell pathcell = row.getCell(colIndexPath);

				if (pdbcell.isMissing() || !(pdbcell instanceof StringValue)) {
					// If there is nothing in the pdbcell, we return a missing
					return DataType.getMissingCell();
				}

				if (pathcell.isMissing()
						|| !(pathcell instanceof StringValue)) {
					// Return missing if the path is not present
					return DataType.getMissingCell();
				}
				// Here we actually do the meat of the work and save the pdb
				// file
				// 1. Check if the directory exist and create it if not

				String pathToFile = ((StringValue) pathcell).getStringValue();
				if (userParentFolderMdl.getBooleanValue()) {
					if (!pathToFile.toLowerCase().endsWith(".pdb")) {
						pathToFile += ".pdb";
					}
					pathToFile = new File(parentFolder, pathToFile).getPath();
				}
				if (!(FileHelpers.checkContainerFolderExists(pathToFile))) {
					// If the folder doesnt exist we need to make it exist
					if (!(FileHelpers.createContainerFolder(pathToFile))) {
						// Folder doesnt exist and for unknown reason we
						// fail to
						// make it
						return BooleanCell.FALSE;
					}
				}
				// Now we actually write the file..
				String pdbtext = ((StringValue) pdbcell).getStringValue();
				return BooleanCellFactory.create(FileHelpers.saveStringToPath(
						pdbtext, pathToFile, m_Overwrite.getBooleanValue()));
			}
		};
		c.append(factory);
		return c;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		m_PDBcolumnName.saveSettingsTo(settings);
		m_PathcolumnName.saveSettingsTo(settings);
		m_Overwrite.saveSettingsTo(settings);
		m_SuccesscolumnName.saveSettingsTo(settings);
		userParentFolderMdl.saveSettingsTo(settings);
		parentFolderMdl.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		m_PDBcolumnName.loadSettingsFrom(settings);
		m_PathcolumnName.loadSettingsFrom(settings);
		m_Overwrite.loadSettingsFrom(settings);
		m_SuccesscolumnName.loadSettingsFrom(settings);
		try {
			userParentFolderMdl.loadSettingsFrom(settings);
			parentFolderMdl.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			logger.info("Using legacy compatible settings");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		m_PDBcolumnName.validateSettings(settings);
		m_PathcolumnName.validateSettings(settings);
		m_Overwrite.validateSettings(settings);
		m_SuccesscolumnName.validateSettings(settings);
		// Dont validate new settings
	}

}
