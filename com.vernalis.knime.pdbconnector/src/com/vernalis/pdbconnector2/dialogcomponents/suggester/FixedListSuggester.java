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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link Suggester} which suggests from a list of options defined at
 * instantiation. The suggestions may or may not be case-sensitive
 * 
 * @author Steve
 *
 */
public class FixedListSuggester implements Suggester {
	private final List<String> values;
	private final boolean caseSensitive;

	/**
	 * Case sensitive constructor
	 * 
	 * @param values The possible values
	 */
	public FixedListSuggester(String... values) {
		this(true, values);
	}

	/**
	 * Full constructor
	 * 
	 * @param caseSensitive Are suggestions case-sensitive?
	 * @param values        The possible values
	 */
	public FixedListSuggester(boolean caseSensitive, String... values) {
		this.caseSensitive = caseSensitive;
		this.values = Arrays.asList(values);
	}

	@Override
	public List<String> suggest(String t) {
		return values.stream()
				.filter(x -> caseSensitive ? x.startsWith(t) : t.toUpperCase().startsWith(x.toUpperCase())).distinct()
				.sorted().collect(Collectors.toList());
	}

}
