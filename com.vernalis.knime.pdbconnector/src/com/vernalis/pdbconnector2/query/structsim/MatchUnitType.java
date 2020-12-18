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
package com.vernalis.pdbconnector2.query.structsim;

import java.util.Arrays;
import java.util.List;

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * An enum showing the possible match unit types for the structural similarity
 * query type
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public enum MatchUnitType implements ButtonGroupEnumInterface {
	/**
	 * Assembly match
	 *
	 */
	assembly_id("Assembly ID") {

		@Override
		public List<String> getDefaultIDs() {
			return Arrays.asList("1");
		}
	},

	/**
	 * Chain match
	 */
	asym_id("Chain ID") {

		@Override
		public List<String> getDefaultIDs() {
			return Arrays.asList("A");
		}
	};

	private final String text;

	private MatchUnitType(String text) {
		this.text = text;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public String getActionCommand() {
		return name();
	}

	@Override
	public String getToolTip() {
		return null;
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	/**
	 * @return the default match unit
	 */
	public static MatchUnitType getDefault() {
		return assembly_id;
	}

	/**
	 * @return the default ids for the given type
	 */
	public abstract List<String> getDefaultIDs();

	/**
	 * Method to parse a string value to return a corresponding
	 * {@link MatchUnitType}
	 * 
	 * @param str
	 *            The text to parse
	 * @return The corresponding {@link MatchUnitType} where the supplied
	 *         argument matches either {@link #name()} or {@link #getText()}
	 * @throws NullPointerException
	 *             if {@code str} is {@code null}
	 * @throws IllegalArgumentException
	 *             if no match is found
	 */
	public static MatchUnitType fromString(String str) {
		if (str == null) {
			throw new NullPointerException();
		}
		return Arrays.stream(values())
				.filter(x -> str.equals(x.name()) || str.equals(x.getText()))
				.findFirst().orElseThrow(IllegalArgumentException::new);

	}
}
