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
package com.vernalis.knime.data.datatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;

import com.vernalis.knime.iterators.PeekingIterator;

/**
 * A DataTable wrapper with Grouping Iterator which will allow iteration over a
 * table in Groups of values in a single column. If all rows in the table with
 * the same value in this column are to be returned at once, then the table
 * should be pre-sorted by that column. If the column index is -1 then the
 * iterator will simply return one row at a time.
 * <p>
 * NB We cannot implement DataTable in this class as that extends
 * Iterable<DataRow>
 * 
 * @author s.roughley
 *
 */
public class GroupIterableDataTable implements Iterable<List<DataRow>> {
	/**
	 * The actual iterator which returns lists of rows which all contain the
	 * same value in the grouping column
	 * 
	 * @author s.roughley
	 *
	 */
	public class GroupRowIterator implements Iterator<List<DataRow>> {
		private PeekingIterator<DataRow> rowIterator = new PeekingIterator<>(table.iterator());

		@Override
		public boolean hasNext() {
			return rowIterator.hasNext();
		}

		@Override
		public List<DataRow> next() {

			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			if (groupColIdx < 0) {
				// -ve index - we group on Row ID which is always unique so we
				// supply one row at a time
				return Collections.singletonList(rowIterator.next());
			}

			List<DataRow> group = new ArrayList<>();
			DataCell currentGroupID = rowIterator.peek().getCell(groupColIdx);
			group.add(rowIterator.next());
			while (hasNext() && rowIterator.peek().getCell(groupColIdx).equals(currentGroupID)) {
				group.add(rowIterator.next());
			}

			return group;
		}

	}

	private final DataTable table;
	private final int groupColIdx;

	/**
	 * Constructor for the table wrapper.
	 * 
	 * @param table
	 *            The datatable to wrap. NB this should be pre-sorted on the
	 *            grouping column
	 * @param groupColumnIndex
	 *            The column index to group on - -1 gives per-row iteration
	 */
	public GroupIterableDataTable(DataTable table, int groupColumnIndex) {
		this.groupColIdx = groupColumnIndex;
		this.table = table;

	}

	@Override
	public GroupRowIterator iterator() {
		return new GroupRowIterator();
	}

	/**
	 * @return The {@link DataTableSpec} of the table
	 */
	public DataTableSpec getDataTableSpec() {
		return table.getDataTableSpec();
	}

}
