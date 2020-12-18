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

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * The logical query conjunction operator for query parts within a query group
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public enum QueryGroupConjunction implements ButtonGroupEnumInterface {
	/**
	 * Logical 'AND' operator
	 */
	AND,
	/**
	 * Logical 'OR' operator
	 */
	OR;

	@Override
	public String getText() {
		return name();
	}

	@Override
	public String getActionCommand() {
		return name().toLowerCase();
	}

	@Override
	public String getToolTip() {
		return name();
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	/**
	 * @return The default operator
	 */
	public static QueryGroupConjunction getDefault() {
		return AND;
	}

	/**
	 * Method to obtain a {@link QueryGroupConjunction} from a text
	 * representation
	 * 
	 * @param stringValue
	 *            the text to parse
	 * @return a {@link QueryGroupConjunction} equivalent to calling
	 *         {@code valueOf(stringValue.toUpperCase()}
	 * @throws NullPointerException
	 *             if the argument was {@code null}
	 * @throws IllegalArgumentException
	 *             if no matching value was found
	 * @see #valueOf(String)
	 */
	public static QueryGroupConjunction fromText(String stringValue) {
		return valueOf(stringValue.toUpperCase());
	}
}
