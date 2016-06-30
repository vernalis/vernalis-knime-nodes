/*******************************************************************************
 * Copyright (c) 2015, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License, Version 3, as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.parallel;

import java.util.List;

import org.knime.core.data.DataRow;

/**
 * @author s.roughley
 *
 */
public class SingleTableParallelResult extends MultiTableParallelResult {

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
	 */
	public List<DataRow> getRowsForTable() {
		return super.getRowsForTable(0);
	}
}
