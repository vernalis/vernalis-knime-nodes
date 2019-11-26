/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.rcsb.io.nodes.manip;

import static com.vernalis.rcsb.io.nodes.manip.RCSBmultiDownload2NodeDialog.createColumnNameModel;
import static com.vernalis.rcsb.io.nodes.manip.RCSBmultiDownload2NodeDialog.createFiletypesModel;

import java.util.Arrays;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;

import com.vernalis.io.FileDownloadException;
import com.vernalis.io.FileHelpers;
import com.vernalis.rcsb.io.helpers.RCSBFileTypes;

/**
 * This is the model implementation of RCSBmultiDownload. Node to allow download
 * of multiple RCSB PDB filetypes from a column of RCSB Structure IDs
 */
public class RCSBmultiDownload2NodeModel extends SimpleStreamableFunctionNodeModel {

	// the logger instance
	private static final NodeLogger logger =
			NodeLogger.getLogger(RCSBmultiDownload2NodeModel.class);

	private final SettingsModelString m_PDBcolumnName = createColumnNameModel();

	private final SettingsModelStringArray m_fTypes = createFiletypesModel();

	/**
	 * Constructor for the node model.
	 */

	protected RCSBmultiDownload2NodeModel() {
		super();
	}

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec in) {
		// Actually download the files and build the output
		// The column index of the selected column
		final int colIndexPDBID = in.findColumnIndex(m_PDBcolumnName.getStringValue());

		RCSBFileTypes[] fTypes =
				Arrays.stream(m_fTypes.getStringArrayValue()).map(x -> x.replace(" ", "_"))
						.map(x -> RCSBFileTypes.valueOf(x)).toArray(x -> new RCSBFileTypes[x]);

		// column spec of the appended columns
		DataColumnSpec[] newColSpec = new DataColumnSpec[fTypes.length];
		for (int i = 0; i < fTypes.length; i++) {
			newColSpec[i] = new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(in, fTypes[i].getName()),
					fTypes[i].getOutputType()).createSpec();
		}

		ColumnRearranger rearranger = new ColumnRearranger(in);
		rearranger.append(new AbstractCellFactory(true, newColSpec) {
			@Override
			public DataCell[] getCells(final DataRow row) {
				DataCell[] result = new DataCell[fTypes.length];
				Arrays.fill(result, DataType.getMissingCell());
				DataCell c = row.getCell(colIndexPDBID);
				if (c.isMissing()) {
					return result;
				}
				String pdbid = ((StringValue) c).getStringValue();

				for (int i = 0; i < fTypes.length; i++) {
					try {
						String r = FileHelpers.readURLToString(fTypes[i].getURL(pdbid));
						result[i] = fTypes[i].getCellFromContent(r);
					} catch (FileDownloadException e) {
						logger.info(e.getMessage());
					}
				}
				return result;
			}

		});
		return rearranger;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		// Check that a column is selected
		int colIndex = -1;
		if (m_PDBcolumnName.getStringValue() == null) {
			int i = 0;
			for (DataColumnSpec cs : inSpecs[0]) {
				if (cs.getType().isCompatible(StringValue.class)) {
					if (colIndex != -1) {
						setWarningMessage("No PDB ID column selected");
						throw new InvalidSettingsException("No PDB ID column selected.");
					}
					colIndex = i;
				}
				i++;
			}

			if (colIndex == -1) {
				setWarningMessage("No PDB column selected");
				throw new InvalidSettingsException("No PDB column selected.");
			}
			m_PDBcolumnName.setStringValue(inSpecs[0].getColumnSpec(colIndex).getName());
			setWarningMessage("Column '" + m_PDBcolumnName.getStringValue()
					+ "' auto selected for PDB ID column");
		} else {
			colIndex = inSpecs[0].findColumnIndex(m_PDBcolumnName.getStringValue());
			if (colIndex < 0) {
				setWarningMessage("No such column: " + m_PDBcolumnName.getStringValue());
				throw new InvalidSettingsException(
						"No such column: " + m_PDBcolumnName.getStringValue());
			}

			DataColumnSpec colSpec = inSpecs[0].getColumnSpec(colIndex);
			if (!colSpec.getType().isCompatible(StringValue.class)) {
				setWarningMessage(
						"Column \"" + m_PDBcolumnName + "\" does not contain string values");
				throw new InvalidSettingsException("Column \"" + m_PDBcolumnName
						+ "\" does not contain string values: " + colSpec.getType().toString());
			}
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

		m_PDBcolumnName.saveSettingsTo(settings);
		m_fTypes.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_PDBcolumnName.loadSettingsFrom(settings);
		m_fTypes.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_PDBcolumnName.validateSettings(settings);
		m_fTypes.validateSettings(settings);
	}

}
