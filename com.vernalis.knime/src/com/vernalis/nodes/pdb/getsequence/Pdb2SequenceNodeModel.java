/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2013, 2014, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.pdb.getsequence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * This is the model implementation of Pdb2Sequence. Node to extract sequence(s)
 * from PDB Cell column n
 *
 * @author SDR
 */
public class Pdb2SequenceNodeModel extends NodeModel {
	// the logger instance
	private static final NodeLogger logger = NodeLogger.getLogger(Pdb2SequenceNodeModel.class);

	static final String CFG_PDB_COL_NAME = "PDB_Column_Name";
	static final String CFG_PDB_DEL = "Remove_PDB_Column";
	static final String CFG_SEQRES1 = "Extract_SEQRES_1-letter_Sequences";
	static final String CFG_SEQRES3 = "Extract_SEQRES_3-letter_Sequences";
	static final String CFG_COORDS1 = "Extract_Co-ords_1-letter_Sequences";
	static final String CFG_COORDS3 = "Extract_Co-ords_3-letter_Sequences";
	static final String CFG_INC_HETATM = "Include_HETATM_in_Co-ords";

	private final SettingsModelString m_PdbColName = new SettingsModelString(CFG_PDB_COL_NAME,
			null);
	private final SettingsModelBoolean m_DelPdb = new SettingsModelBoolean(CFG_PDB_DEL, false);
	private final SettingsModelBoolean m_Seq1 = new SettingsModelBoolean(CFG_SEQRES1, true);
	private final SettingsModelBoolean m_Seq3 = new SettingsModelBoolean(CFG_SEQRES3, false);
	private final SettingsModelBoolean m_Coord1 = new SettingsModelBoolean(CFG_COORDS1, true);
	private final SettingsModelBoolean m_Coord3 = new SettingsModelBoolean(CFG_COORDS3, false);
	private final SettingsModelBoolean m_IncHet = new SettingsModelBoolean(CFG_INC_HETATM, false);
	private final Boolean m_IgnMod = false;

	/**
	 * Constructor for the node model.
	 */
	protected Pdb2SequenceNodeModel() {

		// TODO: Specify the amount of input and output ports needed.
		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		if (inData == null || inData.length != 1) {
			throw new InvalidSettingsException("Invalid input data");
		}
		final BufferedDataTable table = inData[0];
		final boolean removePdbCol = m_DelPdb.getBooleanValue();
		Set<String> newColNames = Pdb2SeqHelpers.ColumnNames(m_Seq1.getBooleanValue(),
				m_Seq3.getBooleanValue(), m_Coord1.getBooleanValue(), m_Coord3.getBooleanValue(),
				m_IgnMod);

		// Create the new output table spec and a Buffered Data Container for it
		final DataTableSpec newSpec = createTableSpec(table.getDataTableSpec(),
				m_PdbColName.getStringValue(), removePdbCol, newColNames);
		final BufferedDataContainer dc = exec.createDataContainer(newSpec);

		// Handle Empty Tables
		if (table.size() == 0) {
			dc.close();
			return new BufferedDataTable[] { dc.getTable() };
		}

		final int colIdx = table.getSpec().findColumnIndex(m_PdbColName.getStringValue());
		final long totalRowCount = table.size();
		final double progressPerRow = 1.0 / totalRowCount;
		long rowCounter = 0;

		// count of new columns
		int newColCnt = newColNames.size();

		// And an ArrayList of empty cells for missing PDB Cells
		ArrayList<DataCell> Empties = new ArrayList<DataCell>();
		for (int i = 0; i < newColCnt; i++) {
			Empties.add(DataType.getMissingCell());
		}

		// Now loop through the rows of the table
		for (final DataRow row : table) {
			rowCounter++;
			exec.checkCanceled();
			exec.setProgress(rowCounter * progressPerRow,
					"Processing row " + rowCounter + " of " + totalRowCount);
			DataCell c = row.getCell(colIdx);

			// Firstly, deal with the possibility of an empty PDB sell
			if (c.isMissing()) {
				final DefaultRow newRow = createClone(row.getKey(), row, colIdx, removePdbCol,
						Empties);
				dc.addRowToTable(newRow);
				continue;
			}

			String PDB = ((StringValue) c).getStringValue();
			ArrayList<ArrayList<DataCell>> newCells;
			try {
				newCells = Pdb2SeqHelpers.getResults(PDB, m_Seq1.getBooleanValue(),
						m_Seq3.getBooleanValue(), m_Coord1.getBooleanValue(),
						m_Coord3.getBooleanValue(), m_IncHet.getBooleanValue(), m_IgnMod);
			} catch (Exception e) {
				logger.warn("Error processing row " + row.getKey() + " - Returning missing values");
				newCells = null;
			}
			// Now deal with the possibility that the PCB cell couldnt be parsed
			// properly
			// TODO: WHAT IS THE CORRECT WAY OF DEALING WITH THIS SITUATION -
			// HERE WE CARRY ON REGARDLESS
			if (newCells == null) {
				final DefaultRow newRow = createClone(row.getKey(), row, colIdx, removePdbCol,
						Empties);
				dc.addRowToTable(newRow);
				// skip to the next row
				continue;
			}

			// Deal with the possibility that the PDB cell didnt contain any
			// sequence info and retain the row
			if (newCells.size() == 0) {
				final DefaultRow newRow = createClone(row.getKey(), row, colIdx, removePdbCol,
						Empties);
				dc.addRowToTable(newRow);
				logger.info("Row " + row.getKey() + " returned no sequences...");
				// skip to the next row
				continue;
			}
			int counter = 1;

			// Now we loop through the individual sequences in the PDB column
			Iterator<ArrayList<DataCell>> iter = newCells.iterator();
			while (iter.hasNext()) {
				// Sort out new row keys, based on existing
				final RowKey oldKey = row.getKey();
				final RowKey newKey = new RowKey(oldKey.getString() + "_" + counter++);

				final DefaultRow newRow = createClone(newKey, row, colIdx, removePdbCol,
						iter.next());
				dc.addRowToTable(newRow);
			}
		}
		dc.close();
		return new BufferedDataTable[] { dc.getTable() };
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

		// spec is the input table
		final DataTableSpec spec = inSpecs[0];

		// Check the selection for the PDB column
		int colIndex = -1;
		if (m_PdbColName.getStringValue() == null) {
			int i = 0;
			for (DataColumnSpec cs : inSpecs[0]) {
				if (cs.getType().isCompatible(StringValue.class)) {
					if (colIndex != -1) {
						setWarningMessage("No PDB column selected");
						throw new InvalidSettingsException("No PDB column selected.");
					}
					colIndex = i;
				}
				i++;
			}

			if (colIndex == -1) {
				setWarningMessage("No PDB column selected");
				throw new InvalidSettingsException("No PDB column selected.");
			}
			m_PdbColName.setStringValue(inSpecs[0].getColumnSpec(colIndex).getName());
			setWarningMessage(
					"Column '" + m_PdbColName.getStringValue() + "' auto selected for PDB column");
		} else {
			colIndex = inSpecs[0].findColumnIndex(m_PdbColName.getStringValue());
			if (colIndex < 0) {
				setWarningMessage("No such column: " + m_PdbColName.getStringValue());
				throw new InvalidSettingsException(
						"No such column: " + m_PdbColName.getStringValue());
			}

			DataColumnSpec colSpec = inSpecs[0].getColumnSpec(colIndex);
			if (!colSpec.getType().isCompatible(StringValue.class)) {
				setWarningMessage("Column \"" + m_PdbColName + "\" does not contain string values");
				throw new InvalidSettingsException("Column \"" + CFG_PDB_COL_NAME
						+ "\" does not contain string values: " + colSpec.getType().toString());
			}
		}

		Set<String> newColNames = Pdb2SeqHelpers.ColumnNames(m_Seq1.getBooleanValue(),
				m_Seq3.getBooleanValue(), m_Coord1.getBooleanValue(), m_Coord3.getBooleanValue(),
				m_IgnMod);

		// Ensure at least one column will be added to the output
		if (newColNames.size() < 1) {
			setWarningMessage("No new columns created!");
			throw new InvalidSettingsException("No new columns created!");
		}

		final DataTableSpec resultSpec = createTableSpec(spec, m_PdbColName.getStringValue(),
				m_DelPdb.getBooleanValue(), newColNames);
		return new DataTableSpec[] { resultSpec };
	}

	/**
	 * function to create the new output table spec based on user inputs
	 * 
	 * @param spec
	 *            The input table spec
	 * @param colName
	 *            The name of the PDB column which may be removed
	 * @param removePdbCol
	 *            Is the PDB column removed?
	 * @param NewColumnNames
	 *            Collection object containing the new column names
	 * @return DataTableSpec with the new columns added, and optionally the PDB
	 *         column deleted
	 * @throws InvalidSettingsException
	 */
	private static DataTableSpec createTableSpec(final DataTableSpec spec, final String colName,
			final boolean removePdbCol, final Collection<String> NewColumnNames)
					throws InvalidSettingsException {
		/*
		 * Method to create a new table spec, optionally retaining the PDB
		 * column and adding optionally a extra columns along with Chain and
		 * Sequence columns
		 */
		final int index = spec.findColumnIndex(colName);
		if (index < 0) {
			throw new InvalidSettingsException("Invalid column name: " + colName);
		}
		final DataColumnSpec colSpec = spec.getColumnSpec(index);

		// create a collection to put the existing columns into
		final Collection<DataColumnSpec> specs = new LinkedList<DataColumnSpec>();
		final int noOfCols = spec.getNumColumns();
		for (int i = 0; i < noOfCols; i++) {
			final DataColumnSpec currentSpec = spec.getColumnSpec(i);
			if (removePdbCol && currentSpec.equals(colSpec)) {
				// Skip adding the PDB column
				continue;
			}
			specs.add(currentSpec);
		}

		// New we add the new column names - note we cant just use list add as
		// We need to check each one for uniqueness
		String newColName;
		DataColumnSpecCreator specCreator;

		Iterator<String> itr = NewColumnNames.iterator();
		while (itr.hasNext()) {
			newColName = DataTableSpec.getUniqueColumnName(spec, itr.next());
			if (newColName.toLowerCase().startsWith("model")) {
				specCreator = new DataColumnSpecCreator(newColName, IntCell.TYPE);
			} else {
				specCreator = new DataColumnSpecCreator(newColName, StringCell.TYPE);
			}
			specs.add(specCreator.createSpec());
		}

		final DataTableSpec resultSpec = new DataTableSpec(specs.toArray(new DataColumnSpec[0]));
		return resultSpec;
	}

	/**
	 * Function to create a clone of a row and add new cells to it
	 * 
	 * @param newKey
	 *            The new RowKey
	 * @param row
	 *            The existing data row
	 * @param PDBColId
	 *            The Id of the PDB column
	 * @param removePDBCol
	 *            Is thePDB column to be removed?
	 * @param newCells
	 *            ArrayList containing the new DataCells
	 * @return
	 */
	private DefaultRow createClone(final RowKey newKey, final DataRow row, final int PDBColId,
			final boolean removePDBCol, final ArrayList<DataCell> newCells) {
		// Create a clone of the existing row adding the new columns to the end
		// Calculate number of cells
		int cellCount = row.getNumCells();
		cellCount -= (removePDBCol) ? 1 : 0;
		cellCount += newCells.size();

		final DataCell[] newRowCells = new DataCell[cellCount];
		int cellIdx = 0;

		// First loop through the existing cells in the row, adding them to the
		// new row
		for (int i = 0, length = row.getNumCells(); i < length; i++) {
			if (removePDBCol && i == PDBColId) {
				// Skip the PDB column if required
				continue;
			}
			newRowCells[cellIdx++] = row.getCell(i);
		}

		// now add the new cells
		Iterator<DataCell> iter = newCells.iterator();
		while (iter.hasNext()) {
			DataCell temp = iter.next();
			newRowCells[cellIdx++] = (temp == null) ? DataType.getMissingCell() : temp;
		}
		return new DefaultRow(newKey, newRowCells);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_Coord1.saveSettingsTo(settings);
		m_Coord3.saveSettingsTo(settings);
		m_DelPdb.saveSettingsTo(settings);
		m_IncHet.saveSettingsTo(settings);
		m_PdbColName.saveSettingsTo(settings);
		m_Seq1.saveSettingsTo(settings);
		m_Seq3.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_Coord1.loadSettingsFrom(settings);
		m_Coord3.loadSettingsFrom(settings);
		m_DelPdb.loadSettingsFrom(settings);
		m_IncHet.loadSettingsFrom(settings);
		m_PdbColName.loadSettingsFrom(settings);
		m_Seq1.loadSettingsFrom(settings);
		m_Seq3.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_Coord1.validateSettings(settings);
		m_Coord3.validateSettings(settings);
		m_DelPdb.validateSettings(settings);
		m_IncHet.validateSettings(settings);
		m_PdbColName.validateSettings(settings);
		m_Seq1.validateSettings(settings);
		m_Seq3.validateSettings(settings);
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
