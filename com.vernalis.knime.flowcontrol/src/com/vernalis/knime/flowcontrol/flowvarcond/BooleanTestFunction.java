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
package com.vernalis.knime.flowcontrol.flowvarcond;

/**
 * A test function to compare a value with a reference
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 * @param <T>
 *            the type of object to compare
 */
public interface BooleanTestFunction<T> {

	/**
	 * The test method
	 * 
	 * @param value
	 *            the value to test
	 * @param reference
	 *            the reference value
	 * 
	 * @return the result of the comparison
	 */
	boolean apply(T value, T reference);
}
