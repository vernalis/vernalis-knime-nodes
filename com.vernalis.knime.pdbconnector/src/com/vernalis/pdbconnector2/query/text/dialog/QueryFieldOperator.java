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
package com.vernalis.pdbconnector2.query.text.dialog;

import java.util.Arrays;

/**
 * An enum listing all possible query field operators. Not all operators are
 * valid for all fields
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public enum QueryFieldOperator {
	@SuppressWarnings("javadoc")
	equals, @SuppressWarnings("javadoc")
	contains_phrase, @SuppressWarnings("javadoc")
	contains_words(true), @SuppressWarnings("javadoc")
	in, @SuppressWarnings("javadoc")
	exact_match, @SuppressWarnings("javadoc")
	exists, @SuppressWarnings("javadoc")
	greater(">"), @SuppressWarnings("javadoc")
	greater_or_equal(">="), @SuppressWarnings("javadoc")
	less("<"), @SuppressWarnings("javadoc")
	less_or_equal("<="), @SuppressWarnings("javadoc")
	range, @SuppressWarnings("javadoc")
	range_closed;

	private final String displayName;
	private final boolean needsGroupQuery;

	private QueryFieldOperator() {
		this(null);
	}

	private QueryFieldOperator(String displayName) {
		this(displayName, false);
	}

	private QueryFieldOperator(boolean needsGroupQuery) {
		this(null, needsGroupQuery);
	}

	private QueryFieldOperator(String displayName, boolean needsGroupQuery) {
		this.displayName = displayName;
		this.needsGroupQuery = needsGroupQuery;
	}

	/**
	 * @return The name to display in the dropdown
	 */
	public String getDisplayName() {
		return displayName == null ? name().replace("_", " ") : displayName;
	}

	/**
	 * @return The text to use in the JSON query
	 */
	public String getQueryString() {
		return name();
	}

	/**
	 * @return {@code true} if each value needs to go in a separate query group
	 *         with 'OR' logic
	 */
	public boolean needsGroupQuery() {
		return needsGroupQuery;
	}

	/**
	 * Method to return a {@link QueryFieldOperator} from its corresponding
	 * display name
	 * 
	 * @param displayName
	 *            The display name
	 * @return the matching {@link QueryFieldOperator}
	 * @throws IllegalArgumentException
	 *             if no match is found
	 */
	public static QueryFieldOperator fromDisplayName(String displayName) {
		return Arrays.stream(values())
				.filter(x -> x.getDisplayName().equals(displayName)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
						"No operator found for '" + displayName + "'"));
	}

	/**
	 * @return The default field operator
	 */
	public static QueryFieldOperator getDefault() {
		return equals;
	}
}
