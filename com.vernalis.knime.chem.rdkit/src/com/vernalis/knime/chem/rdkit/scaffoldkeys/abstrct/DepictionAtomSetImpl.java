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
package com.vernalis.knime.chem.rdkit.scaffoldkeys.abstrct;

import java.awt.Color;
import java.util.BitSet;
import java.util.Objects;

import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.DepictionAtomSet;

/**
 * Simple immutable implementation of a {@link DepictionAtomSet}
 * 
 * @author S.Roughley knime@vernalis.com
 *
 *
 * @since v1.34.0
 */
public class DepictionAtomSetImpl implements DepictionAtomSet {

	private final BitSet atomIDs;
	private final Color highlightColour;
	private final boolean highlightConnectingBonds;

	/**
	 * @param atomIDs
	 *            the highlighted atom IDs. The supplied argument is cloned
	 * @param highlightColour
	 *            The highlight colour for highlighted atoms
	 * @param highlightConnectingBonds
	 *            Whether bonds connecting atoms of this
	 *            {@link DepictionAtomSet} should also be highlighted
	 *
	 * @since v1.34.0
	 */
	public DepictionAtomSetImpl(BitSet atomIDs, Color highlightColour,
			boolean highlightConnectingBonds) {
		this.atomIDs = (BitSet) atomIDs.clone();
		this.highlightColour = highlightColour;
		this.highlightConnectingBonds = highlightConnectingBonds;
	}

	@Override
	public BitSet getHighlightedAtoms() {
		return (BitSet) atomIDs.clone();
	}

	@Override
	public Color getHighlightColour() {
		return highlightColour;
	}

	@Override
	public boolean highlightConnectingBonds() {
		return highlightConnectingBonds;
	}

	@Override
	public int hashCode() {
		return Objects.hash(atomIDs, highlightColour, highlightConnectingBonds);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DepictionAtomSetImpl)) {
			return false;
		}
		DepictionAtomSetImpl other = (DepictionAtomSetImpl) obj;
		return Objects.equals(atomIDs, other.atomIDs)
				&& Objects.equals(highlightColour, other.highlightColour)
				&& highlightConnectingBonds == other.highlightConnectingBonds;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DepictionAtomSetImpl [atomIDs=");
		builder.append(atomIDs);
		builder.append(", highlightColour=");
		builder.append(highlightColour);
		builder.append(", highlightConnectingBonds=");
		builder.append(highlightConnectingBonds);
		builder.append("]");
		return builder.toString();
	}

}
