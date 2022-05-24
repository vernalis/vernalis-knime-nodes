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

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.carrotsearch.hppc.IntIntMap;
import com.carrotsearch.hppc.IntIntScatterMap;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.Scaffold;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.ScaffoldKeysFactory;
import com.vernalis.knime.streams.BitSetCollectors;

/**
 * An abstract base implementation of the {@link Scaffold} interface, providing
 * some caching and basic implementation
 * 
 * @author S.Roughley knime@vernalis.com
 * 
 * @param <T>
 *            The type of molecule object
 *
 *
 * @since v1.34.0
 */
public abstract class AbstractScaffold<T> implements Scaffold<T> {

	private T mol;
	private IntIntMap keyCache = new IntIntScatterMap();
	private Set<BitSet> rings;
	private Map<BitSet, Set<BitSet>> ringSystems;
	private Set<BitSet> conjugatedRings;
	private BitSet[] atomNeighbours;

	/**
	 * Constructor
	 * 
	 * @param mol
	 *            the wrapped molecule obkect
	 * 
	 * @since v1.34.0
	 */
	protected AbstractScaffold(T mol) {
		super();
		this.mol = mol;
		this.rings = findRings(mol);
	}

	@Override
	public int getKey(int keyIndex, ScaffoldKeysFactory<T> factory) {
		if (!keyCache.containsKey(keyIndex)) {
			keyCache.put(keyIndex, factory.getKey(keyIndex, this));
		}
		return keyCache.get(keyIndex);
	}

	@Override
	public T getScaffoldMolecule() throws IllegalStateException {
		return mol;
	}

	@Override
	public Set<BitSet> getSimpleRings() {
		return Collections.unmodifiableSet(rings);
	}

	@Override
	public Set<BitSet> getSimpleConjugatedRings() {
		if (conjugatedRings == null) {
			// There are two tests
			// 1. All atoms are aromatic
			// 2. All bonds are aromatic
			// The central 8-membered ring of
			// "c12ccccc1c3ccccc3c4ccccc4c5ccccc52" passes only the first
			// test -> Answer should be 4 rings

			// Non-aromatic atoms
			BitSet disAllowedAtoms = getAllAtomsMatchingSMARTS("[A]");
			// Ring atoms which are non-aromatic but between two aromatic atoms
			Set<BitSet> disAllowedBonds = getAllSMARTSMatches("[a]!:@[a]");

			conjugatedRings = getSimpleRings().stream()
					.filter(r -> !r.intersects(disAllowedAtoms))
					.filter(r -> disAllowedBonds.stream()
							.noneMatch(b -> r.get(b.nextSetBit(0))
									&& r.get(b.previousSetBit(b.length()))))
					.collect(BitSetCollectors.cloningBitSetCollector(
							Collectors.toCollection(LinkedHashSet::new)));
		}
		return Collections.unmodifiableSet(conjugatedRings);
	}

	@Override
	public Map<BitSet, Set<BitSet>> getRingSystems() {
		// // We simply cache the result of the default implementation on first
		// // call
		if (ringSystems == null) {
			ringSystems = Scaffold.super.getRingSystems();
		}
		return ringSystems;
	}

	@Override
	public BitSet getHeteroAtoms() {
		return getAllAtomsMatchingSMARTS("[!#6]");
	}

	@Override
	public BitSet getAllAtomsMatchingQueryMolecule(T queryMol,
			int... indicesToKeep) throws IllegalStateException {
		return getAllQueryMoleculeMatches(queryMol, true, indicesToKeep)
				.stream().collect(BitSetCollectors.BitSetOr);
	}

	@Override
	public Set<BitSet> getAllQueryMoleculeMatches(T queryMol,
			int... indicesToKeep) throws IllegalStateException {
		return (Set<BitSet>) getAllQueryMoleculeMatches(queryMol, true,
				indicesToKeep);
	}

	@Override
	public final BitSet getAtomNeighbours(int atomIndex)
			throws IndexOutOfBoundsException {
		if (atomNeighbours == null) {
			atomNeighbours = new BitSet[getAtomCount()];
		}
		if (atomNeighbours[atomIndex] == null) {
			atomNeighbours[atomIndex] = calculateAtomNeighbours(atomIndex);
		}
		return (BitSet) atomNeighbours[atomIndex].clone();
	}

	/**
	 * Method to calculate the atom neighbours for an atom
	 * 
	 * @param atomIndex
	 *            the atom index - guaranteed to be within the range of accepted
	 *            values
	 * 
	 * @return A Bitset of the atom neighbours of the {@code atomIndex} atom. It
	 *         should not include the {@code atomIndex} atom
	 *
	 * @since v1.34.0
	 */
	protected abstract BitSet calculateAtomNeighbours(int atomIndex);

	@Override
	public String toString() {
		final int maxLen = 5;
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append(" [SMILES=");
		builder.append(getCanonicalSMILES());
		builder.append(", rings=");
		builder.append(rings != null ? toString(rings, maxLen) : null);
		builder.append("]");
		return builder.toString();
	}

	private String toString(Collection<?> collection, int maxLen) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		int i = 0;
		for (Iterator<?> iterator = collection.iterator(); iterator.hasNext()
				&& i < maxLen; i++) {
			if (i > 0)
				builder.append(", ");
			builder.append(iterator.next());
		}
		builder.append("]");
		return builder.toString();
	}

	@Override
	public Set<BitSet> getAllSMARTSMatches(final String smarts,
			int... indicesToKeep) throws IllegalStateException {
		return (Set<BitSet>) getAllSMARTSMatches(smarts, true, indicesToKeep);
	}

	/**
	 * This method is called by the constructor, and should rind all the rings
	 * from the molecule object
	 * 
	 * @param mol
	 *            The molecule
	 * 
	 * @return A Set in which each BitSet represents the atom indices of a
	 *         single ring. Each ring of a fused or spiro ring is returned
	 *         separately.
	 *
	 * @since v1.34.0
	 */
	public abstract Set<BitSet> findRings(T mol);

	/**
	 * Dispose of the molecule representation. Subsequent method calls to other
	 * methods are likely to cause problems once this methods has been called.
	 * The default implementation simple sets the mol field to {@code null}
	 *
	 * @since v1.34.0
	 */
	public void delete() {
		mol = null;
	}

}
