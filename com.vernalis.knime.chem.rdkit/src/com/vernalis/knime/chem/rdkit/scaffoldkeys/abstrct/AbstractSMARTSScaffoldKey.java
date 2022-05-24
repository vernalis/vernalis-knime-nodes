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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.DepictionAtomSet;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.Scaffold;

/**
 * An abstract SMARTS query string-based ScaffoldKey definition. Most likely,
 * implementations pass the SMARTS querying in
 * {@link #calculateForScaffold(Scaffold)} to the {@link Scaffold} query methods
 * 
 * @author S.Roughley knime@vernalis.com
 * 
 * @param <T>
 *            The type of molecule object
 *
 *
 * @since v1.34.0
 */
public abstract class AbstractSMARTSScaffoldKey<T>
		extends AbstractScaffoldKey<T> {

	private final String smarts;
	private final int[] indicesToKeep;
	private final boolean uniquify;

	/**
	 * Constructor
	 * 
	 * @param index
	 *            The key index
	 * @param name
	 *            The name of the key
	 * @param description
	 *            The description of the key
	 * @param canDepict
	 *            Can the key be rendered
	 * @param isSingletonDepict
	 *            Should all matches of the key be depicted on a single canvas
	 * @param highlightConnectingBonds
	 *            Should bonds between highlighted atoms be highlighted
	 * @param smarts
	 *            The SMARTS string
	 * @param uniquify
	 *            Should the query be performed on the scaffold with
	 *            uniquification of hits?
	 * @param indicesToKeep
	 *            the indices of the SMARTS query atoms to keep matches for.
	 *            Almost certainly passed to the {@link Scaffold} query methods
	 *            during the implementation's
	 *            {@link #calculateForScaffold(Scaffold)} method, via a call to
	 *            {@link #getIndicesToKeep()}. If no indices are supplied, then
	 *            all indices are kept
	 * 
	 * @throws NullPointerException
	 *             if the description, name or SMARTS strings are {@code null}
	 * 
	 * @since v1.34.0
	 */

	protected AbstractSMARTSScaffoldKey(int index, String name,
			String description, boolean canDepict, boolean isSingletonDepict,
			boolean highlightConnectingBonds, String smarts, boolean uniquify,
			int... indicesToKeep) throws NullPointerException {
		super(index, name, description, canDepict, isSingletonDepict,
				highlightConnectingBonds);
		this.smarts = Objects.requireNonNull(smarts,
				"The SMARTS string may not be null!");
		this.indicesToKeep = indicesToKeep == null ? new int[0] : indicesToKeep;
		this.uniquify = uniquify;
	}

	/**
	 * @return the indicesToKeep
	 *
	 * @since v1.34.0
	 */
	protected final int[] getIndicesToKeep() {
		return indicesToKeep;
	}

	/**
	 * @return the SMARTS string
	 *
	 * @since v1.34.0
	 */
	protected final String getSMARTS() {
		return smarts;
	}

	/**
	 * @return whether the SMARTS query should be performed on the scaffold with
	 *         uniquification
	 *
	 * @since v1.34.0
	 */
	protected final boolean isUniquify() {
		return uniquify;
	}

	@Override
	public String[] depictForScaffold(Scaffold<T> scaffold,
			Color highlightColour) throws UnsupportedOperationException {
		if (!scaffold.canDepict() || !canDepict()) {
			throw new UnsupportedOperationException(
					"Unable to depict scaffold key (" + getName() + ")");
		}
		int[][] atomIndices = getMatchingAtomIndices(scaffold);
		String[] retVal =
				new String[isSingletonDepiction() ? 1 : atomIndices.length];
		List<DepictionAtomSet> depictionAtomSets = new ArrayList<>();

		for (int i = 0; i < atomIndices.length; i++) {
			BitSet indices = Arrays.stream(atomIndices[i]).collect(BitSet::new,
					BitSet::set, BitSet::or);
			DepictionAtomSet das = new DepictionAtomSetImpl(indices,
					highlightColour, highlightConnectingBonds());
			if (!isSingletonDepiction()) {
				retVal[i] = scaffold.depict(Collections.singleton(das));
			} else {
				depictionAtomSets.add(das);
			}
		}

		if (isSingletonDepiction()) {
			retVal[0] = scaffold.depict(depictionAtomSets);
		}
		return retVal;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append(" [index=");
		builder.append(getIndex());
		builder.append(", description=");
		builder.append(getDescription());
		builder.append(", SMARTS=");
		builder.append(smarts);
		builder.append(", Uniquify=");
		builder.append(uniquify);
		builder.append("]");
		return builder.toString();
	}

}
