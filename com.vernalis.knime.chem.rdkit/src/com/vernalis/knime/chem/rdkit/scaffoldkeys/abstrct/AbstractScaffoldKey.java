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
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Objects;

import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.DepictionAtomSet;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.Scaffold;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.ScaffoldKey;

/**
 * An abstract base class implementation for {@link ScaffoldKey} which stores
 * the index and description.
 * <p>
 * The default implementation of the method {@link #canDepict()} returns
 * {@code true}
 * </p>
 * <p>
 * The default implementation of the method {@link #isSingletonDepiction()}
 * returns {@code false}
 * </p>
 * <p>
 * The default implementation of the method
 * {@link #depictForScaffold(Scaffold, Color)} will work for any implementation
 * which returns a single inner array of atom indices for
 * {@link #getMatchingAtomIndices(Scaffold)}
 * </p>
 * 
 * @author S.Roughley knime@vernalis.com
 * 
 * @param <T>
 *            The type of molecule representation
 *
 * 
 * @since v1.34.0
 */
public abstract class AbstractScaffoldKey<T> implements ScaffoldKey<T> {

	private final int index;
	private final String description;
	private final String name;
	private final boolean highlightConnectingBonds;
	private final boolean canDepict;
	private final boolean isSingletonDepiction;

	/**
	 * Full constructor
	 * 
	 * @param index
	 *            The key index
	 * @param name
	 *            The name of the key
	 * @param description
	 *            The description of the key
	 * @param canDepict
	 *            can the key be rendered? (Default {@code true})
	 * @param isSingletonDepict
	 *            whether all contributions to the total key count for a
	 *            scaffold are shown on a single depiction (Default:
	 *            {@code false}
	 * @param highlightConnectingBonds
	 *            should bonds connecting highlighted atoms be highlighted too?
	 *            (Default: {@code true})
	 * 
	 * @throws NullPointerException
	 *             if the name or description are {@code null}
	 * 
	 * @since v1.34.0
	 */
	protected AbstractScaffoldKey(int index, String name, String description,
			boolean canDepict, boolean isSingletonDepict,
			boolean highlightConnectingBonds) throws NullPointerException {
		this.index = index;
		this.description = Objects.requireNonNull(description,
				"The description may not be null");
		this.name = Objects.requireNonNull(name, "The name may not be null");
		this.canDepict = canDepict;
		this.isSingletonDepiction = isSingletonDepict;
		this.highlightConnectingBonds = highlightConnectingBonds;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean canDepict() {
		return canDepict;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AbstractScaffoldKey [index=");
		builder.append(index);
		builder.append(", description=");
		builder.append(description);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public boolean isSingletonDepiction() {
		return isSingletonDepiction;
	}

	/**
	 * @return should connecting bonds within highlights be highlighted
	 *
	 * @since v1.34.0
	 */
	protected boolean highlightConnectingBonds() {
		return highlightConnectingBonds;
	}

	@Override
	public String[] depictForScaffold(Scaffold<T> scaffold,
			Color highlightColour) throws UnsupportedOperationException {
		if (!scaffold.canDepict() || !canDepict()) {
			throw new UnsupportedOperationException(
					"Unable to depict scaffold key (" + getName() + ")");
		}
		int[][] atomIndices = getMatchingAtomIndices(scaffold);
		String[] retVal = new String[atomIndices.length];
		for (int i = 0; i < atomIndices.length; i++) {
			BitSet indices = Arrays.stream(atomIndices[i]).collect(BitSet::new,
					BitSet::set, BitSet::or);
			DepictionAtomSet das = new DepictionAtomSetImpl(indices,
					highlightColour, highlightConnectingBonds());
			retVal[i] = scaffold.depict(Collections.singleton(das));
		}
		return retVal;
	}

}
