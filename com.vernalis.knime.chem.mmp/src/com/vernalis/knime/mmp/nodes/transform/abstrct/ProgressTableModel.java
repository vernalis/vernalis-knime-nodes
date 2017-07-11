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
package com.vernalis.knime.mmp.nodes.transform.abstrct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.knime.chem.types.SmartsCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;

/**
 * A simple data container for a progress table with a String 'Transform Name'
 * column, a SMARTS 'Transform' column and a double 'Progress' column
 * 
 * @author s.roughley
 *
 */
class ProgressTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private String[] colNames = new String[] { "Transform Name", "Transform", "Progress" };
	private int numThreads = 0;
	private List<DataCell> transformSmarts = new ArrayList<>();
	private List<Double> transformProgress = new ArrayList<>();
	private List<Long> indices = new ArrayList<>();

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public int getRowCount() {
		return numThreads;
	}

	/**
	 * Method to set the number of threads (i.e. rows in table to view)
	 * 
	 * @param numThreads
	 *            The number of threads
	 */
	public synchronized void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
		while (transformProgress.size() > numThreads) {
			transformProgress.remove(numThreads);
		}
		while (transformSmarts.size() > numThreads) {
			transformSmarts.remove(numThreads);
		}
		while (indices.size() > numThreads) {
			indices.remove(numThreads);
		}
		fireTableStructureChanged();
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (row < indices.size()) {
			switch (col) {
			case 0:
				// Transform name
				return "Transform " + indices.get(row);
			case 1:
				// SMARTS
				return transformSmarts.get(row);
			case 2:
				return (int) (100.0 * transformProgress.get(row));
			default:
				return null;
			}
		} else {
			switch (col) {
			case 0:
				return "";
			case 1:
				return DataType.getMissingCell();
			case 2:
				return -1;
			default:
				return null;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int col) {
		return getValueAt(0, col).getClass();
	}

	/**
	 * Remove a transform (row) from the table
	 * 
	 * @param index
	 *            Index of the transform to remove from the table
	 */
	public void removeTransfrom(Long index) {
		int idx = indices.indexOf(index);
		indices.remove(idx);
		transformProgress.remove(idx);
		transformSmarts.remove(idx);
		fireTableRowsDeleted(idx, idx);
	}

	/**
	 * Clear all the table rows
	 */
	public void clear() {
		int size = indices.size();
		indices.clear();
		transformProgress.clear();
		transformSmarts.clear();
		fireTableRowsDeleted(0, size);
	}

	/**
	 * Add a transform (row) to the table
	 * 
	 * @param index
	 *            The index of the transform
	 * @param rSMARTS
	 *            The transform SMARTS
	 */
	public synchronized void addTransform(Long index, String rSMARTS) {
		indices.add(index);
		Collections.sort(indices);
		int idx = indices.indexOf(index);
		transformProgress.add(idx, 0.0);
		transformSmarts.add(idx, SmartsCellFactory.create(rSMARTS));
		fireTableRowsInserted(idx, idx);
	}

	/**
	 * Method to update the progress of a row
	 * 
	 * @param index
	 *            The index of the progress to update
	 * @param progress
	 *            The progress value
	 */
	public synchronized void updateProgress(Long index, double progress) {
		int idx = indices.indexOf(index);
		transformProgress.set(idx, progress);
		fireTableCellUpdated(idx, 2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		return colNames[column];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

}
