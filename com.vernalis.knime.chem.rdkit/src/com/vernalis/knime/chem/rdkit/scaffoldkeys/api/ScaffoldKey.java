/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
/**
 * 
 */
package com.vernalis.knime.chem.rdkit.scaffoldkeys.api;

import java.awt.Color;

/**
 * A definition and calculator for an individual key
 * 
 * @author S.Roughley knime@vernalis.com
 * 
 * @param <T>
 *            The type of molecule representation
 *
 * 
 * @since v1.34.0
 */
public interface ScaffoldKey<T> {

	/**
	 * Method to perform the calculation of the key on a supplied
	 * {@link Scaffold}
	 * 
	 * @param scaffold
	 *            The scaffold
	 * 
	 * @return The calculated value for the key
	 *
	 * @since v1.34.0
	 */
	public int calculateForScaffold(Scaffold<T> scaffold);

	/**
	 * @return The index of the key (1-based)
	 *
	 * @since v1.34.0
	 */
	public int getIndex();

	/**
	 * @return A short name for the key
	 *
	 * @since v1.34.0
	 */
	public String getName();

	/**
	 * @return The key description
	 *
	 * @since v1.34.0
	 */
	public String getDescription();

	/**
	 * @return {@code true} if the scaffold key contributing substructure can
	 *         meaningfully be depicted
	 *
	 * @since v1.34.0
	 */
	public boolean canDepict();

	/**
	 * @return whether all contributions to the total key count for a scaffold
	 *         are shown on a single depiction, or whether each is shown in it's
	 *         own separate depiction. If this method returns {@code true} then
	 *         {@link #getMatchingAtomIndices(Scaffold)} should ideally return a
	 *         single member array (where this is not the case, that fact should
	 *         be explicitly documented) and
	 *         {@link #depictForScaffold(Scaffold, Color)} <em>must</em> return
	 *         a single member array
	 *
	 * @since v1.34.0
	 */
	public boolean isSingletonDepiction();

	/**
	 * @param scaffold
	 *            the scaffold
	 * @param highlightColour
	 *            the colour to use for the highlighted atoms
	 * 
	 * @return an array of SVG string depictions
	 * 
	 * @throws UnsupportedOperationException
	 *             if the {@link Scaffold} implementation does not support
	 *             depiction or {@link #canDepict()} returns {@code false}
	 *
	 * @since v1.34.0
	 * 
	 * @see #isSingletonDepiction()
	 */
	public String[] depictForScaffold(Scaffold<T> scaffold,
			Color highlightColour) throws UnsupportedOperationException;

	/**
	 * @param scaffold
	 *            the scaffold
	 * 
	 * @return a nested array of matching atom indices. The outer array should
	 *         have the same number of members as the count returned by
	 *         {@link #calculateForScaffold(Scaffold)} in most cases, but this
	 *         is not guaranteed
	 * 
	 * @throws UnsupportedOperationException
	 *             if {@link #canDepict()} returns {@code false}
	 *
	 * @since v1.34.0
	 * 
	 * @see #isSingletonDepiction()
	 */
	public int[][] getMatchingAtomIndices(Scaffold<T> scaffold)
			throws UnsupportedOperationException;

}
