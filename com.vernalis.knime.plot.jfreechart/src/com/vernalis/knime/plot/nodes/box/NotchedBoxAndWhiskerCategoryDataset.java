/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.knime.plot.nodes.box;

import java.util.List;

import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

/**
 * A notched box and whisker dataset
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
@SuppressWarnings("serial")
public class NotchedBoxAndWhiskerCategoryDataset
		extends DefaultBoxAndWhiskerCategoryDataset {

	public NotchedBoxAndWhiskerCategoryDataset() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset#add(org.
	 * jfree.data.statistics.BoxAndWhiskerItem, java.lang.Comparable,
	 * java.lang.Comparable)
	 */
	@Override
	public void add(BoxAndWhiskerItem item, Comparable rowKey,
			Comparable columnKey) {
		if (!(item instanceof NotchedBoxAndWhiskerItem)) {
			throw new IllegalArgumentException(
					"Must add Notched Box and whisker items!");
		}
		super.add(item, rowKey, columnKey);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset#add(java.
	 * util.List, java.lang.Comparable, java.lang.Comparable)
	 */
	@Override
	public void add(List list, Comparable rowKey, Comparable columnKey) {
		NotchedBoxAndWhiskerItem item = NotchedBoxAndWhiskerItem.create(list);
		add(item, rowKey, columnKey);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset#getItem(
	 * int, int)
	 */
	@Override
	public NotchedBoxAndWhiskerItem getItem(int row, int column) {
		return (NotchedBoxAndWhiskerItem) this.data.getObject(row, column);
	}

	/**
	 * Get the lower confidence limit of the specified data object
	 * 
	 * @param row
	 *            The row index
	 * @param column
	 *            The column index
	 * @return The lower confidence limit
	 * @see NotchedBoxAndWhiskerItem
	 */
	public double getMinNotchValue(int row, int column) {
		NotchedBoxAndWhiskerItem item =
				(NotchedBoxAndWhiskerItem) this.data.getObject(row, column);
		if (item != null) {
			return item.getMinNotchValue();
		}
		throw new IllegalArgumentException(
				"No data item found for row/column values");
	}

	/**
	 * Get the upper confidence limit of the specified data object
	 * 
	 * @param row
	 *            The row index
	 * @param column
	 *            The column index
	 * @return The upper confidence limit
	 * @see NotchedBoxAndWhiskerItem
	 */
	public double getMaxNotchValue(int row, int column) {
		NotchedBoxAndWhiskerItem item =
				(NotchedBoxAndWhiskerItem) this.data.getObject(row, column);
		if (item != null) {
			return item.getMaxNotchValue();
		}
		throw new IllegalArgumentException(
				"No data item found for row/column values");
	}

}
