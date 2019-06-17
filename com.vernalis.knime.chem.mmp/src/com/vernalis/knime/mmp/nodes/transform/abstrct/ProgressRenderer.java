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

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A simple table cell renderer to render a column of progress bars, based on
 * the solution at
 * http://stackoverflow.com/questions/7036036/adding-multiple-jprogressbar-to-tablecolumn-of-jtable
 * 
 * @author s.roughley
 *
 */
class ProgressRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	private final JProgressBar b = new JProgressBar(0, 100);

	/**
	 * Constructor
	 */
	public ProgressRenderer() {
		super();
		setOpaque(true);
		b.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		Integer i = (Integer) value;
		if (i >= 0 && i <= 100) {
			b.setIndeterminate(false);
			b.setValue(i);
			return b;
		}
		super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
		return this;
	}
}
