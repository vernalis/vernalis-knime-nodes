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

import javax.swing.event.ChangeListener;

/**
 * An interface to show that a change listener can be registered to the object
 *
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public interface Changeable {

	/**
	 * Add the {@link ChangeListener} to the object
	 * 
	 * @param l
	 *            The change listener to add
	 */
	public void addChangeListener(ChangeListener l);

	/**
	 * Overloaded method redirects to {@link #addChangeListener(ChangeListener)}
	 * 
	 * @param l
	 *            The change listener to register
	 */
	public default void registerChangeListener(ChangeListener l) {
		addChangeListener(l);
	}

	/**
	 * Remove the {@link ChangeListener} from the object
	 * 
	 * @param l
	 *            The change listener to remove
	 */
	public void removeChangeListener(ChangeListener l);

	/**
	 * @return An array containing all registered {@link ChangeListener}s
	 */
	public ChangeListener[] getChangeListeners();
}
