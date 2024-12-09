/*******************************************************************************
 * Copyright (c) 2024, Vernalis (R&D) Ltd
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
package com.vernalis.knime.data.datacolumn;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.node.util.ColumnFilter;

/**
 * An interface for ColumnFilter implementations which involve filtering the
 * column names by a regular expression
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public interface RegexColumnNameColumnFilter extends ColumnFilter {

	/**
	 * @return The regular expression which column names must match
	 */
	public Pattern getRegex();

	/**
	 * @return The regular expression as a predicate which can be used to match
	 *         the column name
	 */
	public default Predicate<String> getRegexPredicate() {
		return getRegex().asPredicate();
	}

	/**
	 * @return A predicate which can be used to test the column spec
	 */
	public default Predicate<DataColumnSpec> getColumnSpecPredicate() {
		return colSpec -> getRegexPredicate().test(colSpec.getName());
	}

	@Override
	default boolean includeColumn(DataColumnSpec colSpec) {
		return getColumnSpecPredicate().test(colSpec);
	}

}
