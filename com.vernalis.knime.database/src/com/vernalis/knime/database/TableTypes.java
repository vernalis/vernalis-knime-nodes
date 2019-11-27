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
package com.vernalis.knime.database;

import java.util.Arrays;

/**
 * An enum listing the table types indicated in the Database metadata class
 * javadoc
 * 
 * @author s.roughley
 *
 */
public enum TableTypes {
	TABLE, VIEW, SYSTEM_TABLE, GLOBAL_TEMPORARY, LOCAL_TEMPORARY, ALIAS,
	SYNONYM;

	/**
	 * @return A cleaned-up version of the name for GUI presentaiton
	 */
	public String getTypeName() {
		return name().replace("_", " ");
	}

	/**
	 * @return The default selections for a multiple-selection
	 */
	public static String[] getDefaults() {
		return convertToNames(new TableTypes[] { TABLE, VIEW });
	}

	/**
	 * @return The default selection for a single selection
	 */
	public static TableTypes getDefault() {
		return TABLE;
	}

	/**
	 * @param name
	 *            The cleaned-up name as returned by {@link #getTypeName()}
	 * @return The enum member
	 */
	public static TableTypes getFromTypeName(String name) {
		return valueOf(name.replace(" ", "_"));
	}

	/**
	 * @return An array containing all the names returned by
	 *         {@link #getTypeName()}
	 */
	public static String[] getAllNames() {
		return convertToNames(TableTypes.values());
	}

	/**
	 * @return An array of the {@link #getTypeName()}s for an array of members
	 */
	private static String[] convertToNames(TableTypes[] types) {
		return Arrays.stream(types).map(x -> x.getTypeName())
				.toArray(x -> new String[x]);
	}

}
