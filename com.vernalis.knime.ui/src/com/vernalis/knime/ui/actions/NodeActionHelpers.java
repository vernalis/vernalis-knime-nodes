/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
/**
 * 
 */
package com.vernalis.knime.ui.actions;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A Utility class with helper methods for the Node Actions
 * 
 * @author S.Roughley knime@vernalis.com
 *
 *
 * @since 01-Mar-2022
 */
class NodeActionHelpers {

	private NodeActionHelpers() {
		// Utility Class - Do not Instantiate
		throw new UnsupportedOperationException();
	}

	/**
	 * Helper method to cast a collection to a List<T>
	 * 
	 * @param <T>
	 *            The class of objects to case into the list
	 * @param clazz
	 *            The class to case to
	 * @param c
	 *            The incoming collection
	 * 
	 * @return A typed List
	 *
	 * @since 01-Mar-2022
	 */
	static final <T> List<T> castList(Class<? extends T> clazz,
			Collection<?> c) {
		return c.stream().map(obj -> clazz.cast(obj))
				.collect(Collectors.toList());
	}

}
