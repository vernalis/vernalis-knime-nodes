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
package com.vernalis.pdbconnector2.query.text.fields;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * An abstract {@link QueryField} for range values
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 * @param <T>
 *            The type of value for the range
 */
public abstract class AbstractRangeQueryField<T> extends QueryField {

	private final T min, max;

	/**
	 * Constructor
	 * 
	 * @param node
	 *            The JSON for additional parameters
	 * @param min
	 *            The minimum value
	 * @param max
	 *            The maximum value
	 */
	public AbstractRangeQueryField(JsonNode node, T min, T max) {
		super(node, false);
		this.min = min;
		this.max = max;
	}

	/**
	 * Full constructor
	 * 
	 * @param attribute
	 *            The field attribute or id
	 * @param displayName
	 *            The field display name
	 * @param description
	 *            The field description
	 * @param placeholder
	 *            The field placeholder text
	 * @param canHaveMultipleFields
	 *            whether the field can have multiple fields
	 * @param searchGroupName
	 *            The category name
	 * @param searchGroupPriority
	 *            The priority - governs position within the category submenu
	 * @param operators
	 *            The possible operators
	 * @param defaultOperator
	 *            The default operator
	 * @param min
	 *            The minimum value
	 * @param max
	 *            The maximum value
	 */
	protected AbstractRangeQueryField(String attribute, String displayName,
			String description, String placeholder,
			boolean canHaveMultipleFields, String searchGroupName,
			int searchGroupPriority, String[] operators, String defaultOperator,
			T min, T max) {
		super(attribute, displayName, description, placeholder,
				canHaveMultipleFields, searchGroupName, searchGroupPriority,
				operators, defaultOperator);
		this.min = min;
		this.max = max;
	}

	/**
	 * @return The minimum value
	 */
	public T getMin() {
		return min;
	}

	/**
	 * @return The maximum value
	 */
	public T getMax() {
		return max;
	}
}
