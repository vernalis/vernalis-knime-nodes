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
package com.vernalis.knime.data.datarow;

import java.util.Iterator;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;

/**
 * A simple DataRow implementation which wraps a {@link DataRow} and stores a
 * 'Left' and a 'Right' parent RowKey
 * 
 * @author s.roughley
 *
 */
public class KnowsParentRowIDsDataRow implements DataRow {

	private final DataRow row;
	private final RowKey lKey, rKey;

	/**
	 * Constructor for the object, requiring a row to store, and the parent keys
	 * 
	 * @param row
	 *            The DataRow to wrap
	 * @param leftKey
	 *            The left parent {@link RowKey}
	 * @param rightKey
	 *            The right parent {@link RowKey}
	 */
	public KnowsParentRowIDsDataRow(DataRow row, RowKey leftKey, RowKey rightKey) {
		super();
		this.row = row;
		this.lKey = leftKey;
		this.rKey = rightKey;
	}

	@Override
	public Iterator<DataCell> iterator() {
		return getRow().iterator();
	}

	@Override
	public int getNumCells() {
		return getRow().getNumCells();
	}

	@Override
	public RowKey getKey() {
		return getRow().getKey();
	}

	@Override
	public DataCell getCell(int index) {
		return getRow().getCell(index);
	}

	/**
	 * @return the right parent Key
	 */
	public RowKey getRightKey() {
		return rKey;
	}

	/**
	 * @return the left parent Key
	 */
	public RowKey getLeftKey() {
		return lKey;
	}

	/**
	 * @return the original {@link DataRow}
	 */
	public DataRow getRow() {
		return row;
	}

}
