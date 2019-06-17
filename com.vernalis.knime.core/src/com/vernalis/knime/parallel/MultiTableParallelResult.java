/*******************************************************************************
 * Copyright (c) 2016, 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.parallel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.DataContainer;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.streamable.RowOutput;

/**
 * Simple container class containing multiple lists of {@link DataRow}s for the
 * concurrent processor to add to the requisite output tables. If an
 * {@link ExecutionContext} and {@link DataTableSpec}s are supplied then the
 * node will keep in memory unless a table exceeds
 * {@link DataContainer#MAX_CELLS_IN_MEMORY}, when it will be transferred to a
 * {@link BufferedDataContainer} internally
 * 
 * @author s.roughley
 * 
 */
public class MultiTableParallelResult {
	protected Map<Integer, List<DataRow>> tables;
	protected int numTables;
	protected ExecutionContext exec;
	protected Map<Integer, BufferedDataContainer> bufferedTables;
	int[] cellCounts;
	DataTableSpec[] specs;

	/**
	 * Construct a new result. This method will always store all rows in memory
	 * 
	 * @param numberOfTables
	 *            The number of tables to be supplied
	 */
	public MultiTableParallelResult(int numberOfTables) {
		this(null, new DataTableSpec[numberOfTables]);
	}

	/**
	 * Construct a new result with the specified specs. Tables will be stored in
	 * memory until the number of cells exceeds
	 * {@link DataContainer#MAX_CELLS_IN_MEMORY} whereupon they will be
	 * transfered to a temporary {@link BufferedDataContainer}
	 * 
	 * @param exec
	 *            Node {@link ExecutionContext} to allow table creation
	 * @param specs
	 *            Table Specs to allow table creation
	 */
	public MultiTableParallelResult(ExecutionContext exec, DataTableSpec[] specs) {
		tables = new HashMap<>();
		numTables = specs.length;
		for (int i = 0; i < numTables; i++) {
			tables.put(i, new ArrayList<DataRow>());
		}
		this.exec = exec;
		cellCounts = new int[numTables];
		this.specs = specs;
		bufferedTables = new HashMap<>();
	}

	/**
	 * Method to add a row to the stored table
	 * 
	 * @param row
	 *            The {@link DataRow} to add to the output table
	 * @param index
	 *            The index of the output table
	 * @throws IllegalArgumentException
	 *             if the index is out of range for the tables
	 */
	public void addRowToTable(DataRow row, int index) throws IllegalArgumentException {
		if (index < 0 || index >= numTables) {
			throw new IllegalArgumentException("Table index out of range");
		}
		List<DataRow> rows = tables.get(index);
		if (rows != null) {
			// Still in memory
			rows.add(row);
			cellCounts[index] += row.getNumCells();
			if (cellCounts[index] > DataContainer.MAX_CELLS_IN_MEMORY && exec != null) {
				// Need to transfer to bdc
				BufferedDataContainer bdc = exec.createDataContainer(specs[index]);
				for (DataRow row1 : rows) {
					bdc.addRowToTable(row1);
				}
				bufferedTables.put(index, bdc);
				rows.clear();
				tables.remove(index);
			}
		} else {
			bufferedTables.get(index).addRowToTable(row);
		}
	}

	/**
	 * @param index
	 *            The index of the table to be returned
	 * @return a {@link List} of the {@link DataRow}s for the specified table
	 * @throws IllegalArgumentException
	 *             if the index is out of range for the tables
	 * @deprecated Use {@link #addRowsToTable(int, BufferedDataContainer)} -
	 *             this method may return <code>null</code> if a
	 *             {@link BufferedDataContainer} has been created
	 */
	@Deprecated
	public List<DataRow> getRowsForTable(int index) throws IllegalArgumentException {
		if (index < 0 || index >= numTables) {
			throw new IllegalArgumentException("Table index out of range");
		}
		return tables.get(index);
	}

	/**
	 * Method to add the stored rows to a {@link BufferedDataContainer}
	 * 
	 * @param index
	 *            The table index to take the rows from
	 * @param table
	 *            The table to add them to
	 * @throws CanceledExecutionException
	 */
	public void addRowsToTable(int index, BufferedDataContainer table)
			throws CanceledExecutionException {
		if (index < 0 || index >= numTables) {
			throw new IllegalArgumentException("Table index out of range");
		}
		Iterator<DataRow> rowIter = iterator(index);
		while (rowIter.hasNext()) {
			if (exec != null) {
				exec.checkCanceled();
			}
			table.addRowToTable(rowIter.next());
		}
	}

	/**
	 * Method to add the stored rows to a {@link BufferedDataContainer}
	 * 
	 * @param index
	 *            The table index to take the rows from
	 * @param table
	 *            The table to add them to
	 */
	public void addRowsToTable(int index, RowOutput table) throws CanceledExecutionException {
		if (index < 0 || index >= numTables) {
			throw new IllegalArgumentException("Table index out of range");
		}
		Iterator<DataRow> rowIter = iterator(index);
		while (rowIter.hasNext()) {
			try {
				table.push(rowIter.next());
			} catch (InterruptedException e) {
				throw new CanceledExecutionException(e.getMessage());
			}
		}
	}

	/**
	 * @return The number of tables
	 */
	public int getNumberTables() {
		return numTables;
	}

	/**
	 * @param index
	 *            The table index
	 * @return An iterator over the rows in the table
	 */
	public Iterator<DataRow> iterator(int index) {
		if (tables.get(index) != null) {
			return tables.get(index).iterator();
		} else {
			BufferedDataContainer bufferedDataContainer = bufferedTables.get(index);
			bufferedDataContainer.close();
			return bufferedDataContainer.getTable().iterator();
		}
	}

	/**
	 * @param index
	 *            the table index
	 * @return The number of stored rows
	 */
	public long size(int index) {
		if (tables.get(index) != null) {
			return tables.get(index).size();
		} else {
			return bufferedTables.get(index).size();
		}
	}

	/**
	 * @param index
	 *            The table index
	 * @return Is the table empty?
	 */
	public boolean isEmpty(int index) {
		return size(index) == 0;
	}
}