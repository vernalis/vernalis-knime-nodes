/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.knime.core.node.defaultnodesettings.DialogComponent;

/**
 * Utility class for performing operations on Swing components and
 * {@link DialogComponent}s
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class SwingUtils {

	private SwingUtils() {
		// Don't instantiate
	}

	/**
	 * Method to force the component panel of a KNIME {@link DialogComponent} to
	 * be displayed at its preferred size
	 * 
	 * @param comp
	 *            The component
	 * @return The underlying component panel of the {@link DialogComponent}
	 * @see SwingUtils#forceToPreferredSize(JComponent)
	 */
	public static JPanel forceToPreferredSize(DialogComponent comp) {
		final JPanel retVal = comp.getComponentPanel();
		forceToPreferredSize(retVal);
		return retVal;
	}

	/**
	 * Method to force a Swing {@link JComponent} to be displayed at its
	 * preferred size
	 * 
	 * @param comp
	 *            The component
	 * @see #forceToSize(JComponent, Dimension)
	 */
	public static void forceToPreferredSize(JComponent comp) {
		final Dimension s = comp.getPreferredSize();
		forceToSize(comp, s);
	}

	/**
	 * Method to force a Swing {@link JComponent} to be displayed at a specified
	 * size
	 * 
	 * @param comp
	 *            The component
	 * @param dim
	 *            The size to display the component at
	 */
	public static void forceToSize(JComponent comp, final Dimension dim) {
		comp.setMinimumSize(dim);
		comp.setPreferredSize(dim);
		comp.setMaximumSize(dim);
	}

	/**
	 * Method to force a Swing {@link JComponent} to retain it's height but fill
	 * the available width, by setting the minimum size to the preferred size,
	 * and the maximum size height to the preferred width + 2000 pixels
	 * 
	 * @param comp
	 *            The component
	 */
	public static void keepHeightFillWidth(JComponent comp) {
		final Dimension s = new Dimension(comp.getPreferredSize().width + 2000,
				comp.getPreferredSize().height);
		comp.setMinimumSize(comp.getPreferredSize());
		comp.setMaximumSize(s);
	}

	/**
	 * Method to force a Swing {@link JComponent} to retain it's width but fill
	 * the available height, by setting the minimum size to the preferred size,
	 * and the maximum size height to the preferred height + 2000 pixels
	 * 
	 * @param comp
	 *            The component
	 */
	public static void keepWidthFillHeight(JComponent comp) {
		final Dimension s = new Dimension(comp.getPreferredSize().width,
				comp.getPreferredSize().height + 2000);
		comp.setMinimumSize(comp.getPreferredSize());
		comp.setMaximumSize(s);
	}
}
