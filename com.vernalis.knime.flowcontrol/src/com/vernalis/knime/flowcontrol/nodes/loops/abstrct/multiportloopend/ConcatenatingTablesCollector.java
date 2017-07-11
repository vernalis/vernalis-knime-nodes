/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.knime.flowcontrol.nodes.loops.abstrct.multiportloopend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.append.AppendedColumnRow;
import org.knime.core.data.append.AppendedRowsTable;
import org.knime.core.data.container.BlobSupportDataRow;
import org.knime.core.data.container.ConcatenateTable;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.util.DuplicateChecker;
import org.knime.core.util.DuplicateKeyException;

/**
 * Class to collect up rows at loop ends. Based closely on that at
 * org.knime.base.meta.looper.ConcatenateTableFactory
 */
public class ConcatenatingTablesCollector {

	/**
	 * Maximum number of tables to be kept. If this threshold is exceeded the so
	 * far created tables are copied into an entire new one.
	 */
	private static final int MAX_NUM_TABLES = 50;

	private ArrayList<BufferedDataContainer> m_tables;

	private BufferedDataContainer m_emptyTable;

	private boolean m_ignoreEmptyTables;

	private boolean m_addIterationColumn;

	private boolean m_tolerateChangingSpecs;

	private Function<RowKey, RowKey> m_rowKeyCreator;

	private DuplicateChecker m_duplicateChecker;

	/**
	 * keeps track of the number of call of the addTable-function.
	 * m_iterationCount <= m_tables.size()
	 */
	private int m_iterationCount = 0;

	/**
	 * The row counter if required by the row key policy
	 */
	private long m_rowCount = 0L;

	/**
	 * Optional port index
	 */
	private Optional<Integer> m_portIndex;

	private boolean m_allowVaryingTypes;

	/**
	 * Creates a new factory that allows to create a {@link ConcatenateTable}.
	 *
	 * @param ignoreEmptyTables
	 *            if empty tables should entirely be skipped
	 * @param addIterationColumn
	 *            if an iteration column should be appended
	 * @param allowVaryingTypes
	 *            Should varying column types be allowed?
	 * @param tolerateChangingSpecs
	 *            Should tables be allowed to have different specs?
	 * @param optRowKeyPolicy
	 *            an optional row key creator. If not provided, the row keys
	 *            remain unmodified.
	 * @param portIndex
	 *            An optional port index for type-failure error reporting
	 */
	public ConcatenatingTablesCollector(final boolean ignoreEmptyTables,
			final boolean addIterationColumn, final boolean allowVaryingTypes,
			final boolean tolerateChangingSpecs, final Optional<RowPolicies> optRowKeyPolicy,
			final Optional<Integer> portIndex) {

		m_ignoreEmptyTables = ignoreEmptyTables;
		m_addIterationColumn = addIterationColumn;
		m_tolerateChangingSpecs = tolerateChangingSpecs;
		m_allowVaryingTypes = allowVaryingTypes;
		m_portIndex = portIndex;
		m_tables = new ArrayList<BufferedDataContainer>();
		if (optRowKeyPolicy.isPresent()) {
			switch (optRowKeyPolicy.get()) {
			case APPEND_SUFFIX:
				m_rowKeyCreator = k -> {
					return new RowKey(k.toString() + "_Iter#" + (m_iterationCount));
				};
				break;
			case GENERATE_NEW:
				m_rowKeyCreator = k -> {
					return new RowKey("Row" + (m_rowCount++));
				};
				break;
			case UNMODIFIED:
			default:
				m_rowKeyCreator = null;
			}
		} else {
			m_rowKeyCreator = null;
		}
		m_duplicateChecker = new DuplicateChecker();
		m_iterationCount = 0;
	}

	/**
	 * Table is added and rows are copied to a new data container. Creates a new
	 * data container if this data table spec differs from the previous table.
	 * This method call checks for row keys duplicates and throws a
	 * {@link DuplicateKeyException}.
	 *
	 * @param table
	 *            the table to be added
	 * @param exec
	 *            the execution context to possibly create a new data container
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws DuplicateKeyException
	 * @throws CanceledExecutionException
	 */
	public void appendTable(final BufferedDataTable table, final ExecutionContext exec)
			throws InterruptedException, DuplicateKeyException, IOException,
			CanceledExecutionException {
		// check if last container has been closed (i.e. createTable was called)
		if (m_tables.size() > 0 && m_tables.get(m_tables.size() - 1).isClosed()) {
			throw new IllegalStateException(
					"No more tables can be added! ConcatenateTable has already been created.");
		}

		// Handle empty tables
		boolean newTableEmpty = table.size() == 0;
		if (newTableEmpty) {
			if (m_ignoreEmptyTables && m_tables.size() > 0) {
				// Just ignore this one
				m_iterationCount++;
				return;
			} else if (m_tables.size() == 0) {
				// if this is the first table we receive and its empty, create
				// an empty one and keep it
				m_emptyTable = exec.createDataContainer(
						createSpec(table.getDataTableSpec(), m_addIterationColumn));
				m_iterationCount++;
				return;
			}
		}

		// compare spec of the current table with the spec of the first table if
		// changing specs are not tolerated
		if (!m_tolerateChangingSpecs && (m_tables.size() > 0 || m_emptyTable != null)) {
			// don't fail if table is empty and to be ignored
			if (!(m_ignoreEmptyTables && (newTableEmpty || m_emptyTable != null))) {
				// create spec for comparision -> set the most common column
				// type for both table spec, if altered column types
				// are to be tolerated
				DataTableSpec tmpSpec1;
				if (m_tables.size() == 0 && m_emptyTable != null) {
					// false as these already have the iteration column!
					tmpSpec1 = createSpec(m_emptyTable.getTableSpec(), false);
				} else {
					tmpSpec1 = createSpec(m_tables.get(0).getTableSpec(), false);
				}
				DataTableSpec tmpSpec2 = createSpec(table.getDataTableSpec(), m_addIterationColumn);
				// fail if specs has been changed
				String comparisonMessage = getComparisonMessage(tmpSpec1, tmpSpec2);
				if (comparisonMessage != null) {
					throw new IllegalArgumentException(comparisonMessage);
				}
			}
		}

		// if table is empty and they are not to be ignored, nothing else to do
		// -> return now
		if (newTableEmpty) {
			m_iterationCount++;
			return;
		}

		// too many tables - consolidate
		if (m_tables.size() > MAX_NUM_TABLES) {
			copyTablesIntoOneTable(exec);
		}

		// create a new data container except the previously added has the same
		// data table spec -> problem: if in each iteration a new row is added
		// we
		// end up with quite many data containers

		DataTableSpec newTableSpec = createSpec(table.getDataTableSpec(), m_addIterationColumn);
		if (m_tables.size() == 0) {
			// No containers - create one!
			m_tables.add(exec.createDataContainer(newTableSpec));
		} else if (m_tables.size() > 0
				&& !newTableSpec.equalStructure(m_tables.get(m_tables.size() - 1).getTableSpec())) {
			// Different spec - close last container and create new one
			m_tables.get(m_tables.size() - 1).close();
			m_tables.add(exec.createDataContainer(newTableSpec));
		}

		BufferedDataContainer con = m_tables.get(m_tables.size() - 1);

		// add rows of the table to the newly created data container
		for (DataRow row : table) {
			exec.checkCanceled();
			// change row key if desired
			if (m_rowKeyCreator != null) {
				// change row key
				row = new BlobSupportDataRow(m_rowKeyCreator.apply(row.getKey()), row);
			}
			m_duplicateChecker.addKey(row.getKey().toString());

			// add additional iteration column if desired
			if (m_addIterationColumn) {
				IntCell currIterCell = new IntCell(m_iterationCount);
				row = new AppendedColumnRow(row, currIterCell);
			}
			con.addRowToTable(row);
		}

		m_iterationCount++;
	}

	/**
	 * Finally creates the {@link ConcatenateTable}. All data containers will be
	 * closed and no more tables can be added to the factory after this method
	 * call.
	 *
	 * @return creates and returns a table that wraps all the previously added
	 *         tables.
	 * @throws CanceledExecutionException
	 * @throws IOException
	 * @throws DuplicateKeyException
	 */
	public BufferedDataTable createTable(final ExecutionContext exec)
			throws CanceledExecutionException, DuplicateKeyException, IOException {

		// return at least the empty table if thats the only one that is
		// available
		if (m_tables.size() == 0) {
			if (m_emptyTable != null) {
				m_emptyTable.close();
				return m_emptyTable.getTable();
			} else {
				// Probably a non-connected port so add an empty, columnless
				// table
				m_tables.add(exec.createDataContainer(new DataTableSpec()));
			}

		}

		m_duplicateChecker.checkForDuplicates();

		// close last used table
		m_tables.get(m_tables.size() - 1).close();
		BufferedDataTable[] res = new BufferedDataTable[m_tables.size()];
		for (int i = 0; i < res.length; i++) {
			res[i] = m_tables.get(i).getTable();
		}
		// don't check for duplicates since this already has been done
		return exec.createConcatenateTable(exec, Optional.empty(), false, res);
	}

	/**
	 * Extends the given table spec by optionally appending another iteration
	 * column
	 *
	 * @param inSpec
	 * @param addIterationCol
	 *            adds the column spec of the appended iteration column
	 * @return the data table spec possibly modified (e.g. an appended iteration
	 *         column)
	 */
	static DataTableSpec createSpec(final DataTableSpec inSpec, final boolean addIterationCol) {

		if (addIterationCol) {
			return new DataTableSpecCreator(inSpec).addColumns(new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(inSpec, "Iteration"), IntCell.TYPE)
							.createSpec())
					.createSpec();
		} else {
			return inSpec;
		}
	}

	/**
	 * Copies all tables, except the last still not-closed table, into an entire
	 * new table
	 */
	private void copyTablesIntoOneTable(final ExecutionContext exec)
			throws CanceledExecutionException {
		BufferedDataTable[] tables = new BufferedDataTable[m_tables.size() - 1];
		for (int i = 0; i < tables.length; i++) {
			tables[i] = m_tables.get(i).getTable();
		}
		AppendedRowsTable wrapper = new AppendedRowsTable(
				org.knime.core.data.append.AppendedRowsTable.DuplicatePolicy.Fail, null, tables);
		BufferedDataContainer con = exec.createDataContainer(wrapper.getDataTableSpec());
		RowIterator rowIt = wrapper.iterator();
		exec.setProgress("Too many tables. Copy tables into one table.");
		while (rowIt.hasNext()) {
			exec.checkCanceled();
			con.addRowToTable(rowIt.next());
		}
		con.close();
		BufferedDataContainer last = m_tables.get(m_tables.size() - 1);
		m_tables.clear();
		m_tables.add(con);
		m_tables.add(last);
		exec.setProgress("Tables copied into one.");
	}

	/**
	 * Utility function to generate meaningful description of the nature of a
	 * mis-match between two {@link DataTableSpec}s, account for whether
	 * changing column types are allowed
	 * 
	 * @param dataTableSpec0
	 *            The first {@link DataTableSpec}
	 * @param dataTableSpec1
	 *            The second {@link DataTableSpec}
	 * @param portIndex
	 *            the port index
	 * @return Description of the mis-match or <code>null</code> if the tables
	 *         match
	 */
	@SuppressWarnings("unchecked")
	private String getComparisonMessage(final DataTableSpec dataTableSpec0,
			final DataTableSpec dataTableSpec1) {
		if (dataTableSpec0 == null || dataTableSpec1 == null) {
			return "At least one spec not initialised - strange!";
		}
		if (dataTableSpec0.equalStructure(dataTableSpec1)) {
			return null;
		}
		int colcnt0 = dataTableSpec0.getNumColumns();
		int colcnt1 = dataTableSpec1.getNumColumns();

		ArrayList<String> colNames0 = new ArrayList<String>(
				Arrays.asList(dataTableSpec0.getColumnNames()));
		ArrayList<String> colNames1 = new ArrayList<String>(
				Arrays.asList(dataTableSpec1.getColumnNames()));

		if (m_allowVaryingTypes) {
			// just compare the names - they just need to be the same
			boolean areIdentical = true;
			if (colcnt0 == colcnt1) {
				for (int i = 0; i < colcnt0; i++) {
					if (!colNames0.get(i).equals(colNames1.get(i))) {
						areIdentical = false;
						break;
					}
				}
				if (areIdentical) {
					return null;
				}
			}
		}

		StringBuilder sb = new StringBuilder("Tables have different specs; Iteration #")
				.append(m_iterationCount);
		if (m_portIndex.isPresent()) {
			sb.append(", Port ").append(m_portIndex.get());
		}
		sb.append("; ");

		if (colcnt0 != colcnt1) {
			sb.append("Tables have different number of columns: ").append(colcnt0).append(" vs. ")
					.append(colcnt1).append("; ");
		}

		boolean typesChanged = false;
		int movedCols = 0;
		// List moved columns including type changes different types and
		// re-typed columns
		for (String colName : colNames0) {
			// Index of colName in 1st table
			int colIndex0 = colNames0.indexOf(colName);
			// Index of colName in 2nd table - -1 if not present
			int colIndex1 = colNames1.indexOf(colName);
			if (colIndex0 != colIndex1 && colIndex1 >= 0) {
				// column has moved
				movedCols++;
				sb.append("Column moved: [").append(colName).append("] from ").append(colIndex0);
				sb.append(getOrdinalSuffix(colIndex0)).append(" to ");
				sb.append(colIndex1).append(getOrdinalSuffix(colIndex1)).append(" position");
				if (!m_allowVaryingTypes) {
					// Now check the types are the same for the moved columns
					if (!dataTableSpec0.getColumnSpec(colName)
							.equalStructure(dataTableSpec1.getColumnSpec(colName))) {
						sb.append(" (types also changed - ")
								.append(dataTableSpec0.getColumnSpec(colName).getType())
								.append(" to ")
								.append(dataTableSpec1.getColumnSpec(colName).getType())
								.append(")); ");
						typesChanged = true;
					} else {
						sb.append(" (types unchanged); ");
					}
				}
			} else if (!m_allowVaryingTypes
					&& colNames0.indexOf(colName) == colNames1.indexOf(colName)) {
				// Check that the type is unchanged if the column has not moved
				if (!dataTableSpec0.getColumnSpec(colName)
						.equalStructure(dataTableSpec1.getColumnSpec(colName))) {
					sb.append("Column re-typed: [").append(colName).append("] - ")
							.append(dataTableSpec0.getColumnSpec(colName).getType()).append(" to ")
							.append(dataTableSpec1.getColumnSpec(colName).getType()).append("; ");
					typesChanged = true;
				}
			}

		}

		if (colNames0.containsAll(colNames1) && colNames1.containsAll(colNames0)) {
			// Simple cases....
			sb.append("Summary: ");
			if (movedCols > 0) {
				// Just a re-arrangement
				sb.append("Columns re-ordered");
				if (typesChanged) {
					// with or without re-typing
					sb.append(" with type changes");
				}
			} else {
				// Just a re-typing
				sb.append("Column type change(s)");
			}
			return sb.toString();
		}

		// Now we have something more complicated - which will be some added or
		// lost columns. We've already listed columns which have moved or been
		// re-typed
		// so now we need to deal with those lost or added

		// List all lost columns
		ArrayList<String> temp = (ArrayList<String>) colNames0.clone();
		temp.removeAll(colNames1);
		for (String colName : temp) {
			sb.append("Column lost: ").append(colName).append("; ");
		}

		// Now list added columns
		temp = (ArrayList<String>) colNames1.clone();
		temp.removeAll(colNames0);
		for (String colName : temp) {
			sb.append("Column added: ").append(colName).append(" (")
					.append(dataTableSpec1.getColumnSpec(colName).getType()).append("); ");
		}
		sb.append("Summary: Complex changes to table structure");
		return sb.toString();
	}

	/**
	 * Routine to return correct ordinal number text suffix (th, st, nd, rd) for
	 * a given integer.
	 * 
	 * @param number
	 *            The number to process
	 * @return th, st, nd or rd, depending on the last digit of number
	 */
	private String getOrdinalSuffix(final int number) {
		if (number >= 11 & number <= 13) {
			// These are irregular and all are 'th'
			return "th";
		}
		switch (number % 10) {
		case 1:
			return "st";
		case 2:
			return "nd";
		case 3:
			return "rd";
		default:
			return "th";
		}
	}

	public DataTableSpec getTableSpec() {
		if (m_tables.size() == 0 && m_emptyTable != null) {
			// false as these already have the iteration column!
			return createSpec(m_emptyTable.getTableSpec(), false);
		} else if (m_tables.size() > 0) {
			return createSpec(m_tables.get(0).getTableSpec(), false);
		} else {
			return null;
		}
	}
}
