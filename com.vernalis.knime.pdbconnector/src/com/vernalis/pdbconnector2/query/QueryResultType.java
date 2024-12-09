/*******************************************************************************
 * Copyright (c) 2020, 2024, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.query;

import java.util.Arrays;

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * Enum representing the type of object to return as query execution result
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public enum QueryResultType implements ButtonGroupEnumInterface {
	/**
	 * Structure ('entries')
	 */
	entry("Entries"),
	/**
	 * Polymer entities (proteins, nucleic acid chains etc)
	 */
	polymer_entity("Polymer Entities"),
	/**
	 * Non-polymer entities - ligands, cofactors etc
	 */
	non_polymer_entity("Non-polymer Entities"),
	/**
	 * Instances of polymer entities
	 */
	polymer_instance("Polymer Instances"),
	/**
	 * Biological assemblies
	 */
	assembly("Assemblies"),
	/**
	 * Molecular Definitions
	 * @since 1.37.0
	 */
	mol_definition("Molecular Definitions");

	private String text;

	private QueryResultType(String text) {
		this.text = text;
	}

	/**
	 * @return The default value
	 */
	public static QueryResultType getDefault() {
		return entry;
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
	 * Static method to return the appropriate value from a string which may be
	 * either the result of {@link #name()} or {@link #getText()}
	 * 
	 * @param stringValue
	 *            The string to parse
	 * @return The value
	 * @throws NullPointerException
	 *             If the supplied value was {@code null}
	 * @throws IllegalArgumentException
	 *             If no corresponding enum value was found
	 */
	public static QueryResultType fromText(String stringValue)
			throws NullPointerException, IllegalArgumentException {
		if (stringValue == null) {
			throw new NullPointerException();
		}
		return Arrays.stream(values())
				.filter(x -> stringValue.equals(x.name())
						|| stringValue.equals(x.getText()))
				.findFirst().orElseThrow(IllegalArgumentException::new);

	}

}
