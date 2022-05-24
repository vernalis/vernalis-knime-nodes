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
import java.util.BitSet;

/**
 * An interface defining a set of atoms (and optionally their connecting bonds)
 * which should be highlighted during rendering, along with the highlight colour
 * 
 * @author S.Roughley knime@vernalis.com
 *
 *
 * @since v1.34.0
 */
public interface DepictionAtomSet {

	/**
	 * @return A BitSet of the atom indices to be highlighted in the depiction
	 *
	 * @since v1.34.0
	 */
	public BitSet getHighlightedAtoms();

	/**
	 * @return The colour for the highlight
	 *
	 * @since v1.34.0
	 */
	public Color getHighlightColour();

	/**
	 * @return Whether the bonds connecting highlighted atoms should also be
	 *         highlighted
	 *
	 * @since v1.34.0
	 */
	public boolean highlightConnectingBonds();
}
