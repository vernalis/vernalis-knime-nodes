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
 * Enum representing the chemical match types available
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public enum ChemicalMatchType implements ButtonGroupEnumInterface {

	/**
	 * Relaxed molecular graph match
	 */
	Graph_Relaxed,

	/**
	 * Relaxed molecular graph match including stereochemistry
	 */
	Graph_Relaxed_Stereo,

	/**
	 * Strict molecular graph match
	 */
	Graph_Strict,

	/**
	 * Fingerprint similarity match
	 */
	Fingerprint_Similarity,

	/**
	 * Strict Substructure Graph Query
	 * @since 1.28.3
	 */
	Sub_Struct_Graph_Strict,

	/**
	 * Relaxed Substructure Graph Query
	 * @since 1.28.3
	 */
	Sub_Struct_Graph_Relaxed,

	/**
	 * Relaxed Substructure Stereo Graph Query
	 * @since 1.28.3
	 */
	Sub_Struct_Graph_Relaxed_Stereo;

	@Override
	public String getText() {
		return name().replace('_', ' ');
	}

	@Override
	public String getActionCommand() {
		return name().toLowerCase().replace('_', '-');
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
	 * @return The default match type
	 */
	public static ChemicalMatchType getDefault() {
		return Graph_Relaxed;
	}

	/**
	 * Method to attempt to return a {@link ChemicalMatchType} from a string
	 * representation
	 * 
	 * @param str
	 *            The string to parse
	 * @return The {@link ChemicalMatchType} which either
	 *         {@link #getActionCommand()} or {@link #getText()} matches the
	 *         supplied string parameter
	 * @throws IllegalArgumentException
	 *             if no match is found
	 */
	public static ChemicalMatchType getFromText(String str) {
		return Arrays.stream(values())
				.filter(x -> x.getActionCommand().equals(str)
						|| x.getText().equals(str))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
						"No Chemical Match Type found for action command '"
								+ str + "'"));
	}

}
