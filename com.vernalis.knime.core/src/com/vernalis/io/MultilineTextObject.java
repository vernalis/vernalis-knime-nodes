/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.io;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;

/**
 * Interface defining an object represented by multiple lines of a String (e.g.
 * a text file)
 * 
 * @author s.roughley
 *
 */
public interface MultilineTextObject {

	/**
	 * @return The {@link DataColumnSpec}s for the output table
	 */
	DataColumnSpec[] getNewColumnSpecs();

	/**
	 * @param lineSeparator
	 *            The lineseparator to use to join the lines back together for
	 *            the 'whole object' cell
	 * @return The cells for the output table from the object
	 * 
	 */
	DataCell[] getNewCells(String lineSeparator);
}
