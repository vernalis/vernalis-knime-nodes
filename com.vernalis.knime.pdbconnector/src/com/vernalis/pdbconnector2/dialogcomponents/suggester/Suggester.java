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
package com.vernalis.pdbconnector2.dialogcomponents.suggester;

import java.util.List;
import java.util.function.Function;

/**
 * A functional interface to supply a list of suggestions for autocompletion
 * fields
 *
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 */
public interface Suggester extends Function<String, List<String>> {

	@Override
	default List<String> apply(String t) {
		return suggest(t);
	}

	/**
	 * @param t
	 *            The string to suggest from
	 * @return The suggestions
	 */
	List<String> suggest(String t);
}
