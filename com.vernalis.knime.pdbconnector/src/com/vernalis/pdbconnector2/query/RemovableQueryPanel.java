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
package com.vernalis.pdbconnector2.query;

/**
 * A {@link QueryPanel} which can be removed from a parent {@link QueryPanel}
 * 
 * @author S.Roughley knime@vernalis.com
 *
 * @param <T>
 *            The type of the {@link QueryModel} for this panel
 * @param <U>
 *            The type of the parent {@link QueryPanel} from which this panel
 *            will be removed
 * @since 1.30.3
 */
public interface RemovableQueryPanel<T extends QueryModel, U extends QueryPanel<?>>
		extends QueryPanel<T> {

	/**
	 * Method to remove this panel from the parent
	 */
	public void removeMe();

	/**
	 * @return The parent panel (or {@code null} if this is the root panel with
	 *         no further parents)
	 */
	public U getParentGroup();

	/**
	 * @return {@code true} if there are no further parents in this tree
	 */
	public default boolean isRoot() {
		return getParentGroup() == null;
	}
}
