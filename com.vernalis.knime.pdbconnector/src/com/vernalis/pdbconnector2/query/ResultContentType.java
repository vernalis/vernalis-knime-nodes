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
package com.vernalis.pdbconnector2.query;

import java.util.Arrays;
import java.util.Objects;

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * Enum of options for result type(s) to be returned by a query execution
 * 
 * @author S.Roughley knime@vernalis.com
 * @since v1.37.0
 *
 */
public enum ResultContentType implements ButtonGroupEnumInterface {

	/** Experimental Results */
	Experimental,

	/** Computational results */
	Computational;

	@Override
	public String getText() {
		return name();
	}

	@Override
	public String getActionCommand() {
		return getText().toLowerCase();
	}

	@Override
	public String getToolTip() {
		return null;
	}

	@Override
	public boolean isDefault() {
		return this == Experimental;
	}

	/**
	 * @return an array of the default option(s)
	 */
	public static ResultContentType[] getDefaults() {
		return Arrays.stream(values()).filter(v -> v.isDefault())
				.toArray(ResultContentType[]::new);
	}

	/**
	 * Method to return an enum member from a supplied string. Matches are tried
	 * in the following order:
	 * <ol>
	 * <li>The string matches the result of {@link #getText()}</li>
	 * <li>The string matches the result of {@link #getActionCommand()}</li>
	 * <li>The string matches the result of {@link #name()}</li>
	 * </ol>
	 * 
	 * @param str
	 *            text to attempt to parse to an enum value
	 * @return the corresponding value
	 * @throws NullPointerException
	 *             if the supplied String is {@code null}
	 * @throws IllegalArgumentException
	 *             if the supplied String does not map to a value
	 */
	public static ResultContentType fromText(String str)
			throws NullPointerException, IllegalArgumentException {
		Objects.requireNonNull(str);
		return Arrays.stream(values()).filter(rct -> rct.getText().equals(str)
				|| rct.getActionCommand().equals(str) || rct.name().equals(str))
				.findFirst().orElseThrow(IllegalArgumentException::new);
	}
}
