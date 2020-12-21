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
package com.vernalis.pdbconnector2.query.sequence;

import java.util.Arrays;

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * The polymer type to perform sequence searched agains
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public enum SequenceTarget implements ButtonGroupEnumInterface {
	/**
	 * Protein sequences
	 */
	Protein,
	/**
	 * DNA sequences
	 */
	DNA,
	/**
	 * RNA sequences
	 */
	RNA;

	@Override
	public String getText() {
		return name();
	}

	@Override
	public String getActionCommand() {
		return String.format("pdb_%s_sequence", getText().toLowerCase());
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
	 * @return the default sequence type
	 */
	public static SequenceTarget getDefault() {
		return Protein;
	}

	/**
	 * Method to parse a string value to attempt to return a target
	 * 
	 * @param str
	 *            The string to parse
	 * @return A {@link SequenceTarget} for which {@link #getActionCommand()}
	 *         matches the supplied argument
	 * @throws IllegalArgumentException
	 *             if no match is found
	 */
	public static SequenceTarget getFromActionCommand(String str) {
		return Arrays.stream(values())
				.filter(st -> st.getActionCommand().equals(str)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException(String.format(
						"No Sequence target found for action command '%s'",
						str)));
	}
}
