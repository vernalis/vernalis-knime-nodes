/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.knime.chem.speedysmiles.helpers;

/**
 * An Interface to indicate that the class provides a flag mask option to use
 * parts of a long as bitsets or counters
 * 
 * @author S.Roughley
 *
 */
public interface FlagProvider {

	/**
	 * @return The bitmask to apply to a long to obtain the (value-1)
	 */
	public abstract long mask();

	/**
	 * @return The maximum value
	 */
	public abstract int max();

	/**
	 * @return The number of bits required to fully enumerate
	 */
	public abstract int bits();
}
