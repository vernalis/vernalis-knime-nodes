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
package com.vernalis.knime.dist;

/**
 * This interface marks an object as having a meaningful distance to another
 * object of the same type. It could be considered analogous to Comparable<T>
 * 
 * @author s.roughley knime@vernalis.com
 *
 * @param <T>
 *            The type of the objects being measured
 */
public interface Measurable<T> {

	/**
	 * Get the distance to a second object of the same type
	 * 
	 * @param o
	 *            The second object
	 * @return The distance
	 */
	double getDistance(T o);
}
