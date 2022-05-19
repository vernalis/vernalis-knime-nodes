/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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

import java.util.Arrays;
import java.util.Iterator;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultCellIterator;
import org.knime.core.node.util.CheckUtils;

/**
 * A {@link DataRow} implementation allowing simple removal of 1 or more columns
 * 
 * @author s.roughley
 *
 */
public class RemoveColumnsDataRow implements DataRow {

	private final DataRow row;
	private final RowKey key;
	private int[] removeCols;

	/**
	 * Constructor to keep incoming {@link RowKey}
	 * 
	 * @param row
	 *            The base row
	 * @param colIdx
	 *            The indices of the column(s) to remove
	 */
	public RemoveColumnsDataRow(DataRow row, int... colIdx) {
		this(row.getKey(), row, colIdx);
	}

	/**
	 * Constructor with new {@link RowKey}
	 * 
	 * @param key
	 *            The new {@link RowKey}
	 * @param row
	 *            The base row
	 * @param colIdx
	 *            The indices of the column(s) to remove
	 */
	public RemoveColumnsDataRow(RowKey key, DataRow row, int... colIdx) {
		this.row = CheckUtils.checkNotNull(row);
		this.key = CheckUtils.checkNotNull(key);
		// Ensure that we have a unique list of ids, which are sorted and all
		// within the row - otherwise weird things can happen..
		this.removeCols = Arrays.stream(colIdx).sorted().distinct()
				.filter(x -> x >= 0 && x < row.getNumCells()).toArray();

	}

	@Override
	public Iterator<DataCell> iterator() {
		return new DefaultCellIterator(this);
	}

	@Override
	public int getNumCells() {
		return row.getNumCells() - removeCols.length;
	}

	@Override
	public RowKey getKey() {
		return key;
	}

	@Override
	public DataCell getCell(int index) {
		int offSet = 0;
		while (offSet < removeCols.length && removeCols[offSet] <= index + offSet) {
			offSet++;
		}
		return row.getCell(index + offSet);
	}

}
