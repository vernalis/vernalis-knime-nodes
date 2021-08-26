/*******************************************************************************
 * Copyright (c) 2021, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.dialogcomponents.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import com.vernalis.pdbconnector2.query.QueryPanel;

/**
 * A {@link MouseListener} to highlight the border of a parent
 * {@link QueryPanel} when the mouse enters the component it is registered to,
 * and restore it when the mouse leaves the component.
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
final class QueryPanelHighlightBorderMouseListener implements MouseListener {

	private final Color highlightColor;
	private final QueryPanel<?> panel;

	/**
	 * @param highlightColor
	 *            The highlight colour for the border when the mouse is over the
	 *            component
	 * @param panel
	 *            The panel to highlight the border
	 */
	QueryPanelHighlightBorderMouseListener(Color highlightColor,
			QueryPanel<?> panel) {
		this.highlightColor = highlightColor;
		this.panel = panel;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// Nothing

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// Nothing

	}

	@Override
	public void mouseExited(MouseEvent e) {
		panel.resetBorder();

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (((Component) e.getSource()).isEnabled()) {
			panel.highlightBorder(highlightColor);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// nothing

	}
}