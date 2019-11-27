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
package com.vernalis.knime.nodes.propcalc;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;

/**
 * Marker Interface for calculated properties. Should be implemented using an
 * {@link Enum} ideally. The description, references, aliases, minimum and
 * maximum values and units will be set as column properties in the output table
 * unless they return {@code null}
 * 
 * @author s.roughley
 * 
 * @param <T>
 *            The type of input object required to calculate the properties
 */
public interface CalculatedPropertyInterface<T extends Object> {

	/**
	 * @return The display name of the property
	 */
	public String getName();

	/**
	 * 
	 * @param inputObj
	 * @return The calculated property as a DataCell, or a missing value cell if
	 *         could not be calculated
	 */
	public DataCell calculate(T inputObj);

	/**
	 * @return The datatype of the column
	 */
	public DataType getType();

	/**
	 * @return A simple text description of the property
	 */
	public String getDescription();

	/**
	 * @return An array of Literature reference(s) for the property
	 */
	public String[] getReferences();

	/**
	 * @return An array of alias(es) for the property
	 */
	public String[] getAliases();

	/**
	 * @return The minimum possible value for the property
	 */
	public Number getMinimum();

	/**
	 * @return The maximum possible value for the property
	 */
	public Number getMaximum();

	/**
	 * @return the units {@code null} will result in the property not being set
	 *         in the column
	 */
	public String getUnits();

}
