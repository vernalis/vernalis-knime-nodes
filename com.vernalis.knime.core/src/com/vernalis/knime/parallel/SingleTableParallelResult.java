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

import java.util.Iterator;
import java.util.List;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.streamable.RowOutput;

/**
 * @author s.roughley
 *
 */
public class SingleTableParallelResult extends MultiTableParallelResult
		implements Iterable<DataRow> {

	/**
	 * Constructor for a single parallel processing result container with a
	 * table spec and execution context to allow buffering of larger tables
	 * 
	 * @param exec
	 * @param spec
	 */
	public SingleTableParallelResult(ExecutionContext exec, DataTableSpec spec) {
		super(exec, new DataTableSpec[] { spec });
	}

	/**
	 * Constructor for a single table parallel processing result container
	 */
	public SingleTableParallelResult() {
		super(1);
	}

	/**
	 * Convenience method to add the row to the table
	 * 
	 * @param row
	 *            The {@link DataRow} to be added
	 */
	public void addRowToTable(DataRow row) {
		super.addRowToTable(row, 0);
	}

	/**
	 * Convenience methods to get the rows for the table back
	 * 
	 * @deprecated Use addRowToTable()
	 */
	@Deprecated
	public List<DataRow> getRowsForTable() {
		return super.getRowsForTable(0);
	}

	public void addRowsToTable(BufferedDataContainer table) throws CanceledExecutionException {
		super.addRowsToTable(0, table);
	}

	public void addRowsToTable(RowOutput table) throws CanceledExecutionException {
		super.addRowsToTable(0, table);
	}

	public long size() {
		return super.size(0);
	}

	public boolean isEmpty() {
		return super.isEmpty(0);
	}

	@Override
	public Iterator<DataRow> iterator() {
		return super.iterator(0);
	}
}
