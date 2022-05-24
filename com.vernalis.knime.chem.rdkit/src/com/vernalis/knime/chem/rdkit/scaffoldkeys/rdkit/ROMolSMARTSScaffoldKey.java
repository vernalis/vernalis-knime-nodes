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
package com.vernalis.knime.chem.rdkit.scaffoldkeys.rdkit;

import org.RDKit.ROMol;
import org.RDKit.RWMol;

import com.vernalis.knime.chem.rdkit.scaffoldkeys.abstrct.AbstractSMARTSScaffoldKey;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.Scaffold;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.ScaffoldKey;

/**
 * A {@link ScaffoldKey} definition using the RDKit toolkit which returns the
 * number of matches for a SMARTS pattern (NB not the number of atoms matching
 * the SMARTS pattern, but the number of unique matches)
 * 
 * @author S.Roughley knime@vernalis.com
 *
 *
 * @since v1.34.0
 */
public class ROMolSMARTSScaffoldKey extends AbstractSMARTSScaffoldKey<ROMol>
		implements ScaffoldKey<ROMol> {

	private ROMol queryMol;

	/**
	 * @param index
	 *            The key index
	 * @param name
	 *            The name of the key
	 * @param description
	 *            The description of the key
	 * @param SMARTS
	 *            The SMARTS string
	 * @param canDepict
	 *            Can the key be depicted?
	 * @param uniquify
	 *            Should the query be performed on the scaffold with
	 *            uniquification of hits?
	 * @param singletonDepict
	 *            Should all matches be depicted on a single output, or one
	 *            output per match
	 * @param highlightConnectingBonds
	 *            should bonds between highlighted atoms be highlighted?
	 * @param indicesToKeep
	 *            the indices of the SMARTS query atoms to keep matches for.
	 *            Almost certainly passed to the {@link Scaffold} query methods
	 *            during the implementation's
	 *            {@link #calculateForScaffold(Scaffold)} method, via a call to
	 *            {@link #getIndicesToKeep()}. If no indices are supplied, then
	 *            all indices are kept
	 * 
	 * @since v1.34.0
	 */
	protected ROMolSMARTSScaffoldKey(int index, String name, String description,
			String SMARTS, boolean canDepict, boolean singletonDepict,
			boolean highlightConnectingBonds, boolean uniquify,
			int... indicesToKeep) {
		super(index, name, description, canDepict, singletonDepict,
				highlightConnectingBonds, SMARTS, uniquify, indicesToKeep);
		queryMol = RWMol.MolFromSmarts(SMARTS);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * <p>
	 * The default implementation here counts the number of matched of the
	 * SMARTS substructure in the scaffold according to the
	 * {@link #isUniquify()} state
	 * </p>
	 * <p>
	 * Implementations may override this method from the default behaviour, but
	 * should ensure they call {@link #checkQueryMolState()} prior to doing so,
	 * or use {@link #getQueryMol()} to obtain the molecule (which also performs
	 * the check implicitly) otherwise unspecified behaviour may result
	 * </p>
	 * 
	 * @see #getQueryMol()
	 * @see #getIndicesToKeep()
	 * 
	 * @throws IllegalStateException
	 *             if the query molecule has been disposed by a call to
	 *             {@link #delete()}
	 */
	@Override
	public int calculateForScaffold(Scaffold<ROMol> scaffold)
			throws IllegalStateException {
		return scaffold.getAllQueryMoleculeMatches(getQueryMol(), isUniquify(),
				getIndicesToKeep()).size();
	}

	/**
	 * Method to check for a valid non-disposed query molecule
	 * 
	 * @throws IllegalStateException
	 *             if the query molecule has been disposed
	 *
	 * @since v1.34.0
	 */
	public final void checkQueryMolState() throws IllegalStateException {
		if (queryMol == null) {
			throw new IllegalStateException(
					"The query molecule has been disposed!");
		}
	}

	/**
	 * @return the queryMol
	 * 
	 * @throws IllegalStateException
	 *             if the query molecule has been disposed
	 *
	 * @since v1.34.0
	 */
	protected ROMol getQueryMol() throws IllegalStateException {
		checkQueryMolState();
		return queryMol;
	}

	/**
	 * Method to dispose of the stored query molecule and release native object
	 * resources
	 *
	 * @since v1.34.0
	 */
	public final void delete() {
		if (getQueryMol() != null) {
			getQueryMol().delete();
		}
		queryMol = null;
	}

	@Override
	public int[][] getMatchingAtomIndices(Scaffold<ROMol> scaffold) {
		return scaffold
				.getAllQueryMoleculeMatches(getQueryMol(), isUniquify(),
						getIndicesToKeep())
				.stream().map(bs -> bs.stream().toArray())
				.toArray(int[][]::new);

	}

}
