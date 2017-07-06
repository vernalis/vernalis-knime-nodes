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

import java.awt.event.MouseEvent;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * A simple {@link JTableHeader} which allows for locking of Transform column
 * aspect ratio and setting the ratio, and catches resizing events to implement
 * 
 * @author s.roughley
 *
 */
public class ProgressTableHeader extends JTableHeader {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	double aspectRatio = 2;
	boolean lockAspectRatio = false;

	/**
	 * Constructur
	 * 
	 * @param columnModel
	 *            The {@link TableColumnModel} for the tables
	 */
	public ProgressTableHeader(TableColumnModel columnModel) {
		super(columnModel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#processMouseEvent(java.awt.event.MouseEvent)
	 */
	@Override
	protected void processMouseEvent(MouseEvent e) {
		// Have to get this before the super call as otherwise it is null
		TableColumn resizingColumn2 = getResizingColumn();
		super.processMouseEvent(e);
		if (resizingColumn2 != null && "Transform".equals(resizingColumn2.getIdentifier())) {

			if (e.getID() == MouseEvent.MOUSE_RELEASED && (lockAspectRatio || e.isShiftDown())) {
				getTable().applyAspectRatio();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.JTableHeader#getTable()
	 */
	@Override
	public ProgressTable getTable() {
		return (ProgressTable) super.getTable();
	}

}
