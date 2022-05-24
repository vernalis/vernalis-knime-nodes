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

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vernalis.knime.streams.BitSetCollectors;

/**
 * A scaffold for Ertl Scaffold Keys calculation.
 *
 * <p>
 * A scaffold is a hydrogen-removed Murcko-Bemis scaffold
 * </p>
 *
 * @author S.Roughley knime@vernalis.com
 *
 * @param <T>
 *            The type of molecule object
 *
 *
 * @since v1.34.0
 */
public interface Scaffold<T> {

	/**
	 * @return The canonical SMILES string representation of the scaffold
	 *
	 * @since v1.34.0
	 */
	public String getCanonicalSMILES();

	/**
	 * @return the number of atoms in the scaffold
	 *
	 * @since v1.34.0
	 */
	public int getAtomCount();

	/**
	 * Method to return the value of the given key for the scaffold.
	 * Implementations may simply delegate to the supplied factory, but should
	 * consider caching results
	 *
	 * @param index
	 *            the index of the key
	 * @param factory
	 *            the key factory
	 *
	 * @return The value for the key for the scaffold
	 *
	 * @since v1.34.0
	 */
	public int getKey(int index, ScaffoldKeysFactory<T> factory);

	/**
	 * @return The molecule representation of the scaffold
	 *
	 * @since v1.34.0
	 */
	public T getScaffoldMolecule();

	/**
	 * A 'simple ring' is any single ring, not including any exo atoms. Indole
	 * comprises 2 simple rings, with fusion atoms included in both simple rings
	 *
	 * @return an set, where each bitset represents the atom indices of a single
	 *         simple ring
	 *
	 * @since v1.34.0
	 */
	public Set<BitSet> getSimpleRings();

	/**
	 * A 'conjugated ring' is a ring in which all atoms and bonds are aromatic
	 * 
	 * @return an array where each bitset represents the atom indices of a
	 *         single fully-conjugated aromatic ring
	 *
	 * @since v1.34.0
	 */
	public Set<BitSet> getSimpleConjugatedRings();

	/**
	 * @return an array where each bitset represents the atom indices of a
	 *         single non-fully-conjugated ring
	 *
	 * @since v1.34.0
	 */
	public default Set<BitSet> getSimpleNonconjugatedRings() {
		Set<BitSet> retVal = new HashSet<>(getSimpleRings());
		retVal.removeAll(getSimpleConjugatedRings());

		return retVal;
	}

	/**
	 * @return an map, where each key bitset represents a ring system, which is
	 *         defined as a single ring, fused or spiro rings and any exocyclic
	 *         atoms multiply bonded to them. The value is a set of the BitSets
	 *         for the individual constituent ring(s). The default
	 *         implementation provides an unmodifiable view of a HashMap with
	 *         unmodifiable sets as values
	 *
	 * @since v1.34.0
	 */
	public default Map<BitSet, Set<BitSet>> getRingSystems() {
		return Collections.unmodifiableMap(getSimpleRings().stream()
				.collect(BitSetCollectors.cloningBitSetCollector(
						BitSetCollectors.toOverlappingBitSetGroups(true))));
	}

	/**
	 * @return a bitset with set bits representing the indices of heteroatoms
	 *
	 * @since v1.34.0
	 */
	public BitSet getHeteroAtoms();

	/**
	 * A method to perform a query molecule search returning matching atom
	 * indices for each match as a BitSet. Implementations should likely call
	 * {@link #getAllQueryMoleculeMatches(Object, boolean, int...)}, with the
	 * {@code uniquify} parameter set to {@code true}
	 * 
	 * @param queryMol
	 *            The query molecule to match
	 * @param indicesToKeep
	 *            optional set of atom indices for the query molecule to keep as
	 *            part of the returned matches. If not supplied (or is
	 *            {@code null}) then all atom match indices will be returned
	 *
	 * @return a set of match indices as BitSets for a query. Each BitSet
	 *         corresponds to a single match
	 *
	 *
	 * @since v1.34.0
	 */
	Set<BitSet> getAllQueryMoleculeMatches(T queryMol, int... indicesToKeep);

	/**
	 * A method to perform a query molecule search returning matching atom
	 * indices for each match as a BitSet. Implementations should return either
	 * a List or Set depending on the value of the {@code uniquify} parameter
	 * 
	 * @param queryMol
	 *            The query molecule to match
	 * @param uniquify
	 *            Should the results be uniquified? If true, the implementation
	 *            will return a Set, otherwise a List will returned, which may
	 *            contain duplicate {@link BitSet}s if the same atom indices can
	 *            be matched in a different order
	 * @param indicesToKeep
	 *            optional set of atom indices for the query SMARTS to keep as
	 *            part of the returned matches. If not supplied (or is
	 *            {@code null}) then all atom match indices will be returned
	 * 
	 * @return a collection of Bitsets of matching atom indices
	 *
	 * @since v1.34.0
	 */
	Collection<BitSet> getAllQueryMoleculeMatches(T queryMol, boolean uniquify,
			int... indicesToKeep);

	/**
	 * A method to perform a SMARTS search returning matching atom indices for
	 * each match as a BitSet. Implementations should likely call
	 * {@link #getAllQueryMoleculeMatches(Object, boolean, int...)}, with the
	 * {@code uniquify} parameter set to {@code true}
	 * 
	 * @param smarts
	 *            The valid SMARTS to match
	 * @param indicesToKeep
	 *            optional set of atom indices for the query SMARTS to keep as
	 *            part of the returned matches. If not supplied (or is
	 *            {@code null}) then all atom match indices will be returned
	 *
	 * @return a set of match indices as BitSets for a query. Each BitSet
	 *         corresponds to a single match
	 * 
	 * @see #getAllSMARTSMatches(String, boolean, int...)
	 *
	 * @since v1.34.0
	 */
	Set<BitSet> getAllSMARTSMatches(final String smarts, int... indicesToKeep);

	/**
	 * A method to perform a SMARTS search returning matching atom indices for
	 * each match as a BitSet. Implementations should return either a List or
	 * Set depending on the value of the {@code uniquify} parameter
	 * 
	 * @param smarts
	 *            The valid SMARTS to match
	 * @param uniquify
	 *            Should the results be uniquified? If true, the implementation
	 *            will return a Set, otherwise a List will returned, which may
	 *            contain duplicate {@link BitSet}s if the same atom indices can
	 *            be matched in a different order
	 * @param indicesToKeep
	 *            optional set of atom indices for the query SMARTS to keep as
	 *            part of the returned matches. If not supplied (or is
	 *            {@code null}) then all atom match indices will be returned
	 * 
	 * @return a collection of Bitsets of matching atom indices
	 *
	 * @since v1.34.0
	 */
	Collection<BitSet> getAllSMARTSMatches(final String smarts,
			boolean uniquify, int... indicesToKeep);

	/**
	 * @param queryMol
	 *            The query molecule to match
	 * @param indicesToKeep
	 *            optional set of atom indices for the query molecule to keep as
	 *            part of the returned matches. If not supplied (or is
	 *            {@code null}) then all atom match indices will be returned
	 *
	 * @return a BitSet with set bits corresponding to all atoms matching the
	 *         supplied query
	 *
	 *
	 * @since v1.34.0
	 */
	BitSet getAllAtomsMatchingQueryMolecule(T queryMol, int... indicesToKeep);

	/**
	 * @param smarts
	 *            The valid SMARTS to match
	 * @param indicesToKeep
	 *            optional set of atom indices for the query molecule to keep as
	 *            part of the returned matches. If not supplied (or is
	 *            {@code null}) then all atom match indices will be returned
	 * 
	 * @return a BitSet with set bits corresponding to all atoms matching the
	 *         supplied query
	 *
	 *
	 * @since v1.34.0
	 */
	BitSet getAllAtomsMatchingSMARTS(final String smarts, int... indicesToKeep);

	/**
	 * @param atomIndex
	 *            The index of the atom
	 * 
	 * @return A BitSet with the set bits representing atoms bonded to the atom
	 *         with the given index
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the atom index is out of the bounds of possible values
	 *
	 * @since v1.34.0
	 */
	BitSet getAtomNeighbours(int atomIndex) throws IndexOutOfBoundsException;

	/**
	 * @return {@code true} if the toolkit scaffold implementation can render
	 *         the wrapped molecule with highlighted atoms and bonds
	 *
	 * @since v1.34.0
	 */
	boolean canDepict();

	/**
	 * @param highlights
	 *            The group or groups of atoms to highlight
	 * 
	 * @return an SVG string of the depiction
	 * 
	 * @throws UnsupportedOperationException
	 *             if {@link #canDepict()} returns false
	 *
	 * @since v1.34.0
	 */
	String depict(Collection<DepictionAtomSet> highlights)
			throws UnsupportedOperationException;

}
