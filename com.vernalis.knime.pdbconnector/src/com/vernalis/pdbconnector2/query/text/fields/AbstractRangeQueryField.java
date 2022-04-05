/*******************************************************************************
 * Copyright (c) 2020, 2021 Vernalis (R&D) Ltd
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vernalis.pdbconnector2.RcsbJSONConstants;

/**
 * An abstract {@link QueryField} for range values
 * 
 * @author S.Roughley knime@vernalis.com
 * 
 * @since 1.28.0
 *
 * @param <T>
 *            The type of value for the range
 */
public abstract class AbstractRangeQueryField<T> extends QueryField {

	/**
	 * JSON Range key for including the upper bound of the range
	 *
	 * @since 1.31.0
	 */
	protected static final String INCLUDE_UPPER = "include_upper";
	/**
	 * JSON Range key for the upper bound of the range
	 *
	 * @since 1.31.0
	 */
	protected static final String TO = "to";
	/**
	 * JSON Range key for including the lower bound of the range
	 *
	 * @since 1.31.0
	 */
	protected static final String INCLUDE_LOWER = "include_lower";
	/**
	 * JSON Range key for the lower bound of the range
	 *
	 * @since 1.31.0
	 */
	protected static final String FROM = "from";

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
	 * 
	 * @deprecated Use new full constructor with service name
	 */
	@Deprecated
	protected AbstractRangeQueryField(String attribute, String displayName,
			String description, String placeholder,
			boolean canHaveMultipleFields, String searchGroupName,
			int searchGroupPriority, String[] operators, String defaultOperator,
			T min, T max) {
		this(attribute, displayName, description, placeholder,
				canHaveMultipleFields, searchGroupName, searchGroupPriority,
				operators, defaultOperator, null, min, max);
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
	 * @param serviceName
	 *            The service name ({@code null} uses the default
	 *            {@link RcsbJSONConstants#SERVICE_TEXT})
	 * @param min
	 *            The minimum value
	 * @param max
	 *            The maximum value
	 */
	protected AbstractRangeQueryField(String attribute, String displayName,
			String description, String placeholder,
			boolean canHaveMultipleFields, String searchGroupName,
			int searchGroupPriority, String[] operators, String defaultOperator,
			String serviceName, T min, T max) {

		super(attribute, displayName, description, placeholder,
				canHaveMultipleFields, searchGroupName, searchGroupPriority,
				operators, defaultOperator, serviceName);
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

	/**
	 * @param includeUpper
	 *            should the upper bound be included in the range (i.e. formerly
	 *            'range_closed' queries)
	 * 
	 * @return an {@link ObjectNode} with the include upper / lower fields set.
	 *         Implementations need to add to/from fields
	 *
	 * @since 1.31.0
	 */
	protected final ObjectNode createRangeJSONObject(boolean includeUpper) {
		final ObjectNode retVal = new ObjectMapper().createObjectNode();
		retVal.put(INCLUDE_LOWER, true);
		retVal.put(INCLUDE_UPPER, includeUpper);
		return retVal;
	}
}
