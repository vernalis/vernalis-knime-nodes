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
 *******************************************************************************/
package com.vernalis.knime.swiggc;

/**
 * This interface defines a simple unique wave index supplier for parallel
 * applications
 * 
 * @author s.roughley knime@vernalis.om
 * 
 */
public interface UniqueWaveIdSupplier {
	/**
	 * @return The next free wave index. Implementations should be thread-safe
	 */
	public long getNextWaveIndex();
}
