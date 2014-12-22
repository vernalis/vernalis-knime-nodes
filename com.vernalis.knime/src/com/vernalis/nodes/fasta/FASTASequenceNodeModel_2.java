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
package com.vernalis.nodes.fasta;

import java.io.File;
import java.io.IOException;
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

import com.vernalis.helpers.FASTAHelperFunctions_2;

/**
 * This is the model implementation of FASTASequence. Extract the chains and
 * sequences from a FASTA sequence file column
 */
public class FASTASequenceNodeModel_2 extends NodeModel {
	// the logger instance
	private static final NodeLogger logger = NodeLogger
			.getLogger(FASTASequenceNodeModel_2.class);

	// Settings models
	static final String CFG_FASTA_COL_NAME = "FASTA_Column";
	static final String CFG_HEADER = "FASTA_Headers";
	static final String CFG_OVERWRITE = "Overwrite";
	static final String CFG_FASTA_TYPE = "FASTA_Source_Type";
	static final String CFG_SEQUENCE = "Extract_Sequence";

	// Name of the column of FASTA sequences
	private final SettingsModelString m_FASTAcolName = new SettingsModelString(
			CFG_FASTA_COL_NAME, null);

	// Extract the Header Row? (The chain and sequence are always extracted!)
	private final SettingsModelBoolean m_HEADER = new SettingsModelBoolean(
			CFG_HEADER, false);

	// Define the FASTA type
	private final SettingsModelString m_FASTAType = new SettingsModelString(
			CFG_FASTA_TYPE, null);

	// Overwrite the FASTA column
	private final SettingsModelBoolean m_Overwrite = new SettingsModelBoolean(
			CFG_OVERWRITE, false);

	// Extract the actual sequence
	private final SettingsModelBoolean m_ExtractSequence = new SettingsModelBoolean(
			CFG_SEQUENCE, true);

	/**
	 * Constructor for the node model.
	 */
	protected FASTASequenceNodeModel_2() {

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
		final boolean removeFastaCol = m_Overwrite.getBooleanValue();
		final boolean addHeader = m_HEADER.getBooleanValue();

		// Create the new output table spec and a Buffered Data Container for it
		final DataTableSpec newSpec = createTableSpec(table.getSpec(),
				m_FASTAcolName.getStringValue(), removeFastaCol,
				FASTAHelperFunctions_2.ColumnNames(
						m_FASTAType.getStringValue(), addHeader,
						m_ExtractSequence.getBooleanValue()));
		final BufferedDataContainer dc = exec.createDataContainer(newSpec);

		// Handle Empty Tables
		if (table.getRowCount() == 0) {
			dc.close();
			return new BufferedDataTable[] { dc.getTable() };
		}

		final int colIdx = table.getSpec().findColumnIndex(
				m_FASTAcolName.getStringValue());
		final int totalRowCount = table.getRowCount();
		final double progressPerRow = 1.0 / totalRowCount;
		int rowCounter = 0;

		// count of new columns
		int newColCnt = FASTAHelperFunctions_2.ColumnNames(
				m_FASTAType.getStringValue(), addHeader,
				m_ExtractSequence.getBooleanValue()).size();

		// Now loop through the rows of the table
		for (final DataRow row : table) {
			rowCounter++;
			exec.checkCanceled();
			exec.setProgress(rowCounter * progressPerRow, "Processing row "
					+ rowCounter + " of " + totalRowCount);

			DataCell c = row.getCell(colIdx);

			// Firstly, deal with the possibility of an empty FASTA sell
			if (c.isMissing()) {
				final DefaultRow newRow = createClone(row.getKey(), row,
						colIdx, removeFastaCol, new DataCell[newColCnt]);
				dc.addRowToTable(newRow);
				continue;
			}

			String[] FASTAs = FASTAHelperFunctions_2
					.getFASTAs(((StringValue) c).getStringValue());
			// Now deal with the possibility that the FASTA cell couldnt be
			// parsed properly
			// TODO: WHAT IS THE CORRECT WAY OF DEALING WITH THIS SITUATION -
			// HERE WE CARRY ON REGARDLESS
			if (FASTAs.length == 0 || FASTAs == null) {
				final DefaultRow newRow = createClone(row.getKey(), row,
						colIdx, removeFastaCol, new DataCell[newColCnt]);
				dc.addRowToTable(newRow);
				continue;
			}
			int counter = 1;

			// Now we loop through the individual sequences in the FASTA column
			// in each
			for (String FASTA : FASTAs) {
				// Sort out new row keys, based on existing
				final RowKey oldKey = row.getKey();
				final RowKey newKey = new RowKey(oldKey.getString() + "_"
						+ counter++);

				// Extract the properties to be added

				DataCell[] newCells = FASTAHelperFunctions_2.ColumnValues(
						FASTA, m_FASTAType.getStringValue(), addHeader,
						m_ExtractSequence.getBooleanValue());

				final DefaultRow newRow = createClone(newKey, row, colIdx,
						removeFastaCol, newCells);
				dc.addRowToTable(newRow);
			}
		}
		dc.close();
		return new BufferedDataTable[] { dc.getTable() };
	}

	private DefaultRow createClone(final RowKey newKey, final DataRow row,
			final int FastaColId, final boolean removeFastaCol,
			final DataCell[] newCells) {
		// Create a clone of the existing row adding the new columns to the end
		// Calculate number of cells
		int cellCount = row.getNumCells();
		cellCount -= (removeFastaCol) ? 1 : 0;
		cellCount += newCells.length;

		final DataCell[] newRowCells = new DataCell[cellCount];
		int cellIdx = 0;

		// First loop through the existing cells in the row, adding them to the
		// new row
		for (int i = 0, length = row.getNumCells(); i < length; i++) {
			if (removeFastaCol && i == FastaColId) {
				// Skip the Fasta column if required
				continue;
			}
			newRowCells[cellIdx++] = row.getCell(i);
		}

		// now add the new cells
		for (int i = 0, length = newCells.length; i < length; i++) {
			newRowCells[cellIdx++] = (newCells[i] == null) ? DataType
					.getMissingCell() : newCells[i];
		}
		return new DefaultRow(newKey, newRowCells);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		// spec is the input table
		final DataTableSpec spec = inSpecs[0];

		// Check the selection for the FASTA column
		int colIndex = -1;
		if (m_FASTAcolName.getStringValue() == null) {
			int i = 0;
			for (DataColumnSpec cs : inSpecs[0]) {
				if (cs.getType().isCompatible(StringValue.class)) {
					if (colIndex != -1) {
						setWarningMessage("No FASTA column selected");
						throw new InvalidSettingsException(
								"No FASTA column selected.");
					}
					colIndex = i;
				}
				i++;
			}

			if (colIndex == -1) {
				setWarningMessage("No FASTA column selected");
				throw new InvalidSettingsException("No FASTA column selected.");
			}
			m_FASTAcolName.setStringValue(inSpecs[0].getColumnSpec(colIndex)
					.getName());
			setWarningMessage("Column '" + m_FASTAcolName.getStringValue()
					+ "' auto selected for FASTA column");
		} else {
			colIndex = inSpecs[0].findColumnIndex(m_FASTAcolName
					.getStringValue());
			if (colIndex < 0) {
				setWarningMessage("No such column: "
						+ m_FASTAcolName.getStringValue());
				throw new InvalidSettingsException("No such column: "
						+ m_FASTAcolName.getStringValue());
			}

			DataColumnSpec colSpec = inSpecs[0].getColumnSpec(colIndex);
			if (!colSpec.getType().isCompatible(StringValue.class)) {
				setWarningMessage("Column \"" + m_FASTAcolName
						+ "\" does not contain string values");
				throw new InvalidSettingsException("Column \""
						+ CFG_FASTA_COL_NAME
						+ "\" does not contain string values: "
						+ colSpec.getType().toString());
			}
		}

		Set<String> newColNames = FASTAHelperFunctions_2.ColumnNames(
				m_FASTAType.getStringValue(), m_HEADER.getBooleanValue(),
				m_ExtractSequence.getBooleanValue());

		// Ensure at least one column will be added to the output
		if (newColNames.size() < 1) {
			setWarningMessage("No new columns created!");
			throw new InvalidSettingsException("No new columns created!");
		}

		final DataTableSpec resultSpec = createTableSpec(spec,
				m_FASTAcolName.getStringValue(), m_Overwrite.getBooleanValue(),
				newColNames);
		return new DataTableSpec[] { resultSpec };
	}

	/**
	 * @param spec
	 *            original spec
	 * @param colName
	 *            the FASTA column name
	 * @param removeFastaCol
	 *            <code>true</code> if the FASTA column should be removed
	 * @param NewColumnNames
	 *            The list of new column names to be added
	 * @return the new spec
	 * @throws InvalidSettinsException
	 *             if an excettion occurs
	 */
	private static DataTableSpec createTableSpec(final DataTableSpec spec,
			final String colName, final boolean removeFastaCol,
			final Collection<String> NewColumnNames)
			throws InvalidSettingsException {
		/*
		 * Method to create a new table spec, optionally retaining the FASTA
		 * column and adding optionally a extra columns along with Chain and
		 * Sequence columns
		 */
		final int index = spec.findColumnIndex(colName);
		if (index < 0) {
			throw new InvalidSettingsException("Invalid column name: "
					+ colName);
		}
		final DataColumnSpec colSpec = spec.getColumnSpec(index);

		// create a collection to put the existing columns into
		final Collection<DataColumnSpec> specs = new LinkedList<DataColumnSpec>();
		final int noOfCols = spec.getNumColumns();
		for (int i = 0; i < noOfCols; i++) {
			final DataColumnSpec currentSpec = spec.getColumnSpec(i);
			if (removeFastaCol && currentSpec.equals(colSpec)) {
				// Skip adding the FASTA column
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
			specCreator = new DataColumnSpecCreator(newColName, StringCell.TYPE);
			specs.add(specCreator.createSpec());
		}

		final DataTableSpec resultSpec = new DataTableSpec(
				specs.toArray(new DataColumnSpec[0]));
		return resultSpec;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		m_FASTAcolName.saveSettingsTo(settings);
		m_Overwrite.saveSettingsTo(settings);
		m_HEADER.saveSettingsTo(settings);
		m_FASTAType.saveSettingsTo(settings);
		m_ExtractSequence.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		m_FASTAcolName.loadSettingsFrom(settings);
		m_Overwrite.loadSettingsFrom(settings);
		m_HEADER.loadSettingsFrom(settings);
		m_FASTAType.loadSettingsFrom(settings);
		m_ExtractSequence.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		m_FASTAcolName.validateSettings(settings);
		m_Overwrite.validateSettings(settings);
		m_HEADER.validateSettings(settings);
		m_FASTAType.validateSettings(settings);
		m_ExtractSequence.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// TODO: generated method stub
	}

}
