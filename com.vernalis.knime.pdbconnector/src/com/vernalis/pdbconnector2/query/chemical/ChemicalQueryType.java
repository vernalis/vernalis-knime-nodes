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
package com.vernalis.pdbconnector2.query.chemical;

import java.util.Arrays;

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * Enum describing the different chemical query types
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public enum ChemicalQueryType implements ButtonGroupEnumInterface {
	/**
	 * A chemical formula query
	 */
	Formula,

	/**
	 * A chemical descriptor query
	 */
	Descriptor;

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
		return null;
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	/**
	 * @return The default query type
	 */
	public static ChemicalQueryType getDefault() {
		return Formula;
	}

	/**
	 * @return whether the query type has additional descriptors
	 */
	public boolean hasDescriptorType() {
		return this == Descriptor;
	}

	/**
	 * Method to return a {@link ChemicalQueryType} from a string representation
	 * 
	 * @param str
	 *            The string to parse
	 * @return The parsed value, with either {@link #getActionCommand()} or
	 *         {@link #getText()} matching the supplied argument
	 * @throws IllegalArgumentException
	 *             if no suitable match is found
	 */
	public static ChemicalQueryType fromActionCommand(String str) {
		return Arrays.stream(values())
				.filter(x -> x.getActionCommand().equals(str)
						|| x.getText().equals(str))
				.findAny().orElseThrow(() -> new IllegalArgumentException(
						"No ChemicalQueryType '" + str + "'"));
	}

}
