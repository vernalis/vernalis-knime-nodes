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

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * Enum representing the possible structure similarity query types
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public enum StructureSimilarityType implements ButtonGroupEnumInterface {
	/**
	 * Strict similarity query
	 */
	Strict,
	/**
	 * Relaxed similarity query
	 */
	Relaxed;

	@Override
	public String getText() {
		return name();
	}

	@Override
	public String getActionCommand() {
		return String.format("%s_shape_match", getText().toLowerCase());
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
	public static StructureSimilarityType getDefault() {
		return Strict;
	}

	/**
	 * Method to attempt to get a {@link StructureSimilarityType} from a string
	 * representation
	 * 
	 * @param str
	 *            The string to parse
	 * @return A {@link StructureSimilarityType} for which {@link #getText()}
	 *         matches str in a case insenstive manner, or
	 *         {@link #getActionCommand()} matches exactly
	 * @throws NullPointerException
	 *             if {@code str} is {@code null}
	 * @throws IllegalArgumentException
	 *             if no suitable match is founf
	 */
	public static StructureSimilarityType fromString(String str) {
		if (str == null) {
			throw new NullPointerException();
		}
		return Arrays.stream(values())
				.filter(x -> str.equals(x.getText())
						|| str.equals(x.getText().toLowerCase())
						|| str.equals(x.getActionCommand()))
				.findFirst().orElseThrow(IllegalArgumentException::new);
	}
}
