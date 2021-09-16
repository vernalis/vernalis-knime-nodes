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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.vernalis.pdbconnector2.query.RemovableQueryPanel;

/**
 * A JButton which handles removal of the {@link RemovableQueryPanel} it is part
 * of, and highlights the border of that panel when the mouse is within the
 * button
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
@SuppressWarnings("serial")
public class RemoveMeButton extends JButton {

	private final RemovableQueryPanel<?, ?> component;

	/**
	 * Constructor to create a button with the label 'X'
	 * 
	 * @param component
	 *            The component panel which is to be removed by the button
	 */
	public RemoveMeButton(RemovableQueryPanel<?, ?> component) {
		this("X", component);
	}

	/**
	 * Constructor to create a button with a custom label
	 * 
	 * @param label
	 *            The button label
	 * @param component
	 *            The component panel which is to be removed by the button
	 */
	public RemoveMeButton(String label, RemovableQueryPanel<?, ?> component) {
		super(label);
		this.component = component;
		addMouseListener(new QueryPanelHighlightBorderMouseListener(Color.RED,
				this.component));
		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				component.removeMe();
			}
		});
	}

}
