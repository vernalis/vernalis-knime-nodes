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
package com.vernalis.knime.parallel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataRow;

/**
 * Simple container class containing multiple lists of {@link DataRow}s for the
 * concurrent processor to add to the requisite output tables
 * 
 * @author s.roughley
 * 
 */
public class MultiTableParallelResult {
	protected Map<Integer, List<DataRow>> tables;
	protected int numTables;

	/**
	 * Construct a new result
	 * 
	 * @param numberOfTables
	 *            The number of tables to be supplied
	 */
	public MultiTableParallelResult(int numberOfTables) {
		tables = new HashMap<>();
		for (int i = 0; i < numberOfTables; i++) {
			tables.put(i, new ArrayList<DataRow>());
		}
		numTables = numberOfTables;
	}

	/**
	 * @param row
	 *            The {@link DataRow} to add to the output table
	 * @param index
	 *            The index of the output table
	 * @throws IllegalArgumentException
	 *             if the index is out of range for the tables
	 */
	public void addRowToTable(DataRow row, int index)
			throws IllegalArgumentException {
		if (index < 0 || index >= numTables) {
			throw new IllegalArgumentException("Table index out of range");
		}
		tables.get(index).add(row);
	}

	/**
	 * @param index
	 *            The index of the table to be returned
	 * @return a {@link List} of the {@link DataRow}s for the specified table
	 * @throws IllegalArgumentException
	 *             if the index is out of range for the tables
	 */
	public List<DataRow> getRowsForTable(int index)
			throws IllegalArgumentException {
		if (index < 0 || index >= numTables) {
			throw new IllegalArgumentException("Table index out of range");
		}
		return tables.get(index);
	}


	/**
	 * @return The number of tables
	 */
	public int getNumberTables() {
		return numTables;
	}
}
