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

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.RDKit.Atom;
import org.RDKit.ROMol;

import com.vernalis.knime.chem.rdkit.scaffoldkeys.abstrct.AbstractScaffoldKey;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.abstrct.AbstractScaffoldKeysFactory;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.Scaffold;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.ScaffoldKeysFactory;

/**
 * A {@link ScaffoldKeysFactory} implementation using the RDKit toolkit and the
 * revised Ertl Scaffold tree key definitions at
 * <a href='https://doi.org/10.26434/chemrxiv.13525457.v2'>ChemRxiv DOI:
 * 10.26434/chemrxiv.13525457.v2</a> (Accessed 17-Feb-2022)
 * 
 * @author S.Roughley knime@vernalis.com
 *
 *
 * @since v1.34.0
 */
public final class ROMolRevisedScaffoldKeysFactory
		extends AbstractScaffoldKeysFactory<ROMol>
		implements ScaffoldKeysFactory<ROMol> {

	/**
	 * Holding class idiom provides automatic lazy instantiation and
	 * synchronization See Joshua Bloch Effective Java Item 71
	 */
	private static final class HoldingClass {

		private static ROMolRevisedScaffoldKeysFactory INSTANCE =
				new ROMolRevisedScaffoldKeysFactory();
	}

	/**
	 * Method to get the singleton instance
	 * 
	 * @return The singleton instance of ROMolRevisedScaffoldKeysFactory
	 */
	public static ROMolRevisedScaffoldKeysFactory getInstance() {
		return HoldingClass.INSTANCE;
	}

	private ROMolRevisedScaffoldKeysFactory() {
		addScaffoldKey(new ROMolSMARTSScaffoldKey(1, "nar",
				"Number of ring atoms", "[*!R0]", true, true, true, true));
		addScaffoldKey(new ROMolSMARTSScaffoldKey(2, "naar",
				"Number of atoms in conjugated rings", "[a!R0]", true, true,
				true, true));
		addScaffoldKey(new ROMolSMARTSScaffoldKey(3, "naal",
				"Number of Atoms not in conjugated rings (i.e. atoms in aliphatic rings and not in rings)",
				"[!a]", true, true, true, true));
		addScaffoldKey(new ROMolSMARTSScaffoldKey(4, "nachain",
				"Number of atoms in chains (not including double-connected exo-chain atoms)",
				"[*R0!D1]", true, true, true, true));
		addScaffoldKey(new ROMolSMARTSScaffoldKey(5, "naexocyc",
				"Number of exocyclic atoms (connected by multiple-bonds to a ring)",
				"[*R0;$(*!-!@[*!R0]),$([*-]-!@[*+!R0])]", true, true, true,
				true));
		addScaffoldKey(new ROMolSMARTSScaffoldKey(6, "nN",
				"Number of nitrogen atoms", "[#7]", true, true, true, false));
		addScaffoldKey(new ROMolSMARTSScaffoldKey(7, "nNr",
				"Number of nitrogen atoms in rings", "[#7!R0]", true, true,
				true, false));
		addScaffoldKey(new ROMolSMARTSScaffoldKey(8, "nO",
				"Number of oxygen atoms", "[#8]", true, true, true, false));
		addScaffoldKey(new ROMolSMARTSScaffoldKey(9, "nOr",
				"Number of oxygen atoms in rings", "[#8!R0]", true, true, true,
				false));
		addScaffoldKey(new ROMolSMARTSScaffoldKey(10, "nS",
				"Number of sulfur atoms", "[#16]", true, true, true, false));
		addScaffoldKey(new ROMolSMARTSScaffoldKey(11, "nX",
				"Number of heteroatoms", "[!#6]", true, true, true, false));
		addScaffoldKey(new ROMolSMARTSScaffoldKey(12, "nXr",
				"Number of heteroatoms in rings", "[!#6!R0]", true, true, true,
				false));
		addScaffoldKey(new ROMolSMARTSScaffoldKey(13, "naspiro",
				"Number of spiro atoms", "[*!R0$(*(~@*)(~@*)(~@*)~@*)]", true,
				true, true, false));
		addScaffoldKey(new ROMolSMARTSScaffoldKey(14, "-X<",
				"Number of heteroatoms with more than 2 connections",
				"[!#6!D0!D1!D2]", true, true, true, false));
		addScaffoldKey(new ROMolSMARTSScaffoldKey(15, "XCX",
				"Number of carbon atoms connected to at least 2 heteroatoms",
				"[#6$([#6](~[!#6])~[!#6])]", true, true, true, false));
		addScaffoldKey(new ROMolSMARTSScaffoldKey(16, ">***<",
				"Number of atoms where at least 2 connected atoms have more than 2 connections",
				"[*$(*(~[*!D0!D1!D2])~[*!D0!D1!D2])]", true, true, true,
				false));
		addScaffoldKey(new AbstractScaffoldKey<ROMol>(17, "nq",
				"Absolute value of the scaffold formal charge", true, true,
				false) {

			@Override
			public int calculateForScaffold(Scaffold<ROMol> scaffold) {
				int charge = 0;
				ROMol mol = scaffold.getScaffoldMolecule();
				for (int i = 0; i < mol.getNumAtoms(); i++) {
					Atom at = mol.getAtomWithIdx(i);
					charge += at.getFormalCharge();
					at.delete();
				}
				return Math.abs(charge);
			}

			@Override
			public int[][] getMatchingAtomIndices(Scaffold<ROMol> scaffold) {
				// We will return a single bitset with all charged atoms
				BitSet chargedAtoms = new BitSet();
				ROMol mol = scaffold.getScaffoldMolecule();
				for (int i = 0; i < mol.getNumAtoms(); i++) {
					Atom at = mol.getAtomWithIdx(i);
					int charge = at.getFormalCharge();
					if (charge != 0) {
						chargedAtoms.set(i);
					}
					at.delete();
				}
				return new int[][] { chargedAtoms.stream().toArray() };
			}

		});
		addScaffoldKey(new AbstractScaffoldKey<ROMol>(18, "nb",
				"Number of bonds", false, false, false) {

			@Override
			public int calculateForScaffold(Scaffold<ROMol> scaffold) {
				return (int) scaffold.getScaffoldMolecule().getNumBonds();
			}

			@Override
			public int[][] getMatchingAtomIndices(Scaffold<ROMol> scaffold) {
				// This would just always highlight the whole molecule
				throw new UnsupportedOperationException(
						"Cannot depict this scaffold key (" + getName() + ")");
			}

		});

		addScaffoldKey(new ROMolSMARTSScaffoldKey(19, "nbrm",
				"Number of multiple, non-conjugated ring bonds", "A!-@A", true,
				true, true, true));

		addScaffoldKey(new ROMolSMARTSScaffoldKey(20, "XX",
				"Number of bonds connecting 2 heteroatoms", "[!#6]~[!#6]", true,
				true, true, true));

		addScaffoldKey(new ROMolSMARTSScaffoldKey(21, "XCCX",
				"Number of carbon-carbon bonds when each carbon contains at least one heteroatom",
				"[#6;$([#6]~[!#6])]~[#6;$([#6]~[!#6])]", true, true, true,
				true));

		addScaffoldKey(new ROMolSMARTSScaffoldKey(22, ">**<",
				"Number of bonds with at least 3 connections on both its atoms",
				"[*!D1!D2]~[*!D1!D2]", true, true, true, true));

		// Match both ends of bond, but only keep ring atom to ensure geminal
		// disubstitution is retained
		// TODO: Check render options
		addScaffoldKey(new ROMolSMARTSScaffoldKey(23, "nCr-",
				"Number of exocyclic single bonds where a ring atom is carbon",
				"[#6!R0]-!@[*]", true, true, true, false, 0));
		// TODO: Check render options
		addScaffoldKey(new ROMolSMARTSScaffoldKey(24, "nNr-",
				"Number of exocyclic single bonds where a ring atom is nitrogen",
				"[#7!R0]-!@[*]", true, true, true, false, 0));
		addScaffoldKey(new ROMolSMARTSScaffoldKey(25, "[R]!@[R]",
				"Number of non-ring bonds connecting 2 non-conjugated rings",
				"[A!R0]~!@[A!R0]", true, true, true, true));
		addScaffoldKey(new ROMolSMARTSScaffoldKey(26, "[r]!@[R]",
				"Number of non-ring bonds connecting 2 rings, one of them conjugated and one non-conjugated",
				"[a!R0]~!@[A!R0]", true, true, true, true));

		addScaffoldKey(new ROMolSMARTSScaffoldKey(27, ">****<",
				"Number of bonds where both atoms have at least one neighbour "
						+ "(not considering the bond atoms) with more than 2 connections",
				"[*$(*~[*!D1!D2])$(*~*~[*!D1!D2])]~[*$(*~[*!D1!D2])$(*~*~[*!D1!D2])]",
				true, true, true, true) {

			@Override
			public int calculateForScaffold(Scaffold<ROMol> scaffold)
					throws IllegalStateException {
				return (int) getAtomMatchStream(scaffold).count();

			}

			@Override
			public int[][] getMatchingAtomIndices(Scaffold<ROMol> scaffold) {
				return getAtomMatchStream(scaffold)
						.map(bs -> bs.stream().toArray()).toArray(int[][]::new);

			}

			private Stream<BitSet> getAtomMatchStream(Scaffold<ROMol> scaffold)
					throws IllegalStateException {
				Collection<BitSet> initialMatches =
						scaffold.getAllQueryMoleculeMatches(getQueryMol(),
								isUniquify(), getIndicesToKeep());
				return initialMatches.stream().filter(match -> match.stream()
						// Get the atom neighbours for each matching
						// bond atom in turn
						.mapToObj(a -> scaffold.getAtomNeighbours(a))
						// Remove the other end of the matching bond -
						// this is the over-permissive part
						.map(nbr -> {
							nbr.andNot(match);
							return nbr;
						})
						// Ensure both ends have at least one neighbour
						// with more than 2 connections
						.allMatch(nbr -> nbr.stream().anyMatch(i -> scaffold
								.getAtomNeighbours(i).cardinality() > 2)));
			}
		});

		addScaffoldKey(new AbstractScaffoldKey<ROMol>(28, "nrings",
				"Number of simple rings", true, false, true) {

			@Override
			public int calculateForScaffold(Scaffold<ROMol> scaffold) {
				return scaffold.getSimpleRings().size();
			}

			@Override
			public int[][] getMatchingAtomIndices(Scaffold<ROMol> scaffold) {
				return scaffold.getSimpleRings().stream()
						.map(bs -> bs.stream().toArray()).toArray(int[][]::new);
			}

		});

		addScaffoldKey(new AbstractScaffoldKey<ROMol>(29, "rlargest",
				"Size of the largest ring", true, false, true) {

			@Override
			public int calculateForScaffold(Scaffold<ROMol> scaffold) {
				return scaffold.getSimpleRings().stream()
						.mapToInt(r -> r.cardinality()).max().orElse(0);
			}

			@Override
			public int[][] getMatchingAtomIndices(Scaffold<ROMol> scaffold) {
				NavigableMap<Integer, Set<BitSet>> rings = new TreeMap<>();
				scaffold.getSimpleRings().stream()
						.forEach(
								bs -> rings
										.computeIfAbsent(bs.cardinality(),
												k -> new LinkedHashSet<>())
										.add(bs));
				return rings.lastEntry().getValue().stream()
						.map(bs -> bs.stream().toArray()).toArray(int[][]::new);
			}
		});

		addScaffoldKey(new AbstractScaffoldKey<ROMol>(30, "nr0X",
				"Number of simple rings with no heteroatoms", true, false,
				true) {

			@Override
			public int calculateForScaffold(Scaffold<ROMol> scaffold) {
				BitSet heteros = scaffold.getHeteroAtoms();
				return (int) scaffold.getSimpleRings().stream()
						.filter(r -> !r.intersects(heteros)).count();
			}

			@Override
			public int[][] getMatchingAtomIndices(Scaffold<ROMol> scaffold) {
				BitSet heteros = scaffold.getHeteroAtoms();
				return scaffold.getSimpleRings().stream()
						.filter(r -> !r.intersects(heteros))
						.map(bs -> bs.stream().toArray()).toArray(int[][]::new);
			}

		});

		addScaffoldKey(new AbstractScaffoldKey<ROMol>(31, "nr1X",
				"Number of simple rings with 1 heteroatom", true, false, true) {

			@Override
			public int calculateForScaffold(Scaffold<ROMol> scaffold) {
				BitSet heteros = scaffold.getHeteroAtoms();
				return (int) scaffold.getSimpleRings().stream()
						.filter(r -> r.intersects(heteros))
						.map(r -> (BitSet) r.clone()).map(r -> {
							r.and(heteros);
							return r;
						}).filter(r -> r.cardinality() == 1).count();

			}

			@Override
			public int[][] getMatchingAtomIndices(Scaffold<ROMol> scaffold) {
				BitSet heteros = scaffold.getHeteroAtoms();
				return scaffold.getSimpleRings().stream()
						.filter(r -> r.intersects(heteros)).filter(r -> {
							BitSet t = (BitSet) r.clone();
							t.and(heteros);
							return t.cardinality() == 1;
						}).map(bs -> bs.stream().toArray())
						.toArray(int[][]::new);
			}

		});

		addScaffoldKey(new AbstractScaffoldKey<ROMol>(32, "nr2X",
				"Number of simple rings with 2 heteroatoms", true, false,
				true) {

			@Override
			public int calculateForScaffold(Scaffold<ROMol> scaffold) {
				BitSet heteros = scaffold.getHeteroAtoms();
				return (int) scaffold.getSimpleRings().stream()
						.filter(r -> r.intersects(heteros))
						.map(r -> (BitSet) r.clone()).map(r -> {
							r.and(heteros);
							return r;
						}).filter(r -> r.cardinality() == 2).count();
			}

			@Override
			public int[][] getMatchingAtomIndices(Scaffold<ROMol> scaffold) {
				BitSet heteros = scaffold.getHeteroAtoms();
				return scaffold.getSimpleRings().stream()
						.filter(r -> r.intersects(heteros)).filter(r -> {
							BitSet t = (BitSet) r.clone();
							t.and(heteros);
							return t.cardinality() == 2;
						}).map(bs -> bs.stream().toArray())
						.toArray(int[][]::new);
			}

		});

		addScaffoldKey(new AbstractScaffoldKey<ROMol>(33, "nr3X",
				"Number of simple rings with 3 or more heteroatoms", true,
				false, true) {

			@Override
			public int calculateForScaffold(Scaffold<ROMol> scaffold) {
				BitSet heteros = scaffold.getHeteroAtoms();
				return (int) scaffold.getSimpleRings().stream()
						.filter(r -> r.intersects(heteros))
						.map(r -> (BitSet) r.clone()).map(r -> {
							r.and(heteros);
							return r;
						}).filter(r -> r.cardinality() > 2).count();
			}

			@Override
			public int[][] getMatchingAtomIndices(Scaffold<ROMol> scaffold) {
				BitSet heteros = scaffold.getHeteroAtoms();
				return scaffold.getSimpleRings().stream()
						.filter(r -> r.intersects(heteros)).filter(r -> {
							BitSet t = (BitSet) r.clone();
							t.and(heteros);
							return t.cardinality() > 2;
						}).map(bs -> bs.stream().toArray())
						.toArray(int[][]::new);
			}

		});

		addScaffoldKey(new AbstractScaffoldKey<ROMol>(34, "nr5A",
				"Number of simple non-conjugated rings with 5 atoms", true,
				false, true) {

			@Override
			public int calculateForScaffold(Scaffold<ROMol> scaffold) {

				return (int) scaffold.getSimpleNonconjugatedRings().stream()
						.filter(r -> r.cardinality() == 5).count();
			}

			@Override
			public int[][] getMatchingAtomIndices(Scaffold<ROMol> scaffold) {
				return scaffold.getSimpleNonconjugatedRings().stream()
						.filter(r -> r.cardinality() == 5)
						.map(bs -> bs.stream().toArray()).toArray(int[][]::new);
			}

		});

		addScaffoldKey(new AbstractScaffoldKey<ROMol>(35, "nr6A",
				"Number of simple non-conjugated rings with 6 atoms", true,
				false, true) {

			@Override
			public int calculateForScaffold(Scaffold<ROMol> scaffold) {

				return (int) scaffold.getSimpleNonconjugatedRings().stream()
						.filter(r -> r.cardinality() == 6).count();
			}

			@Override
			public int[][] getMatchingAtomIndices(Scaffold<ROMol> scaffold) {
				return scaffold.getSimpleNonconjugatedRings().stream()
						.filter(r -> r.cardinality() == 6)
						.map(bs -> bs.stream().toArray()).toArray(int[][]::new);
			}

		});

		addScaffoldKey(new AbstractScaffoldKey<ROMol>(36, "nsystems",
				"Number of ring systems", true, false, true) {

			@Override
			public int calculateForScaffold(Scaffold<ROMol> scaffold) {
				return scaffold.getRingSystems().size();
			}

			@Override
			public int[][] getMatchingAtomIndices(Scaffold<ROMol> scaffold) {
				return scaffold.getRingSystems().keySet().stream()
						.map(bs -> bs.stream().toArray()).toArray(int[][]::new);
			}

		});

		addScaffoldKey(new AbstractScaffoldKey<ROMol>(37, "sys2A",
				"Number of ring systems with 2 non-conjugated simple rings",
				true, false, true) {

			@Override
			public int calculateForScaffold(Scaffold<ROMol> scaffold) {
				Map<BitSet, Set<BitSet>> rSystems = scaffold.getRingSystems();
				Set<BitSet> conjRings = scaffold.getSimpleConjugatedRings();
				// First remove any with other than 2 rings, then remove any
				// which contain a conjugated system
				return (int) rSystems.values().stream()
						.filter(rSys -> rSys.size() == 2)
						.filter(rSys -> Collections.disjoint(rSys, conjRings))
						.count();

			}

			@Override
			public int[][] getMatchingAtomIndices(Scaffold<ROMol> scaffold) {
				Map<BitSet, Set<BitSet>> rSystems = scaffold.getRingSystems();
				Set<BitSet> conjRings = scaffold.getSimpleConjugatedRings();
				// First remove any with other than 2 rings, then remove any
				// which contain a conjugated system
				return rSystems.values().stream()
						.filter(rSys -> rSys.size() == 2)
						.filter(rSys -> Collections.disjoint(rSys, conjRings))
						.map(bss -> bss.stream().flatMapToInt(bs -> bs.stream())
								.toArray())
						.toArray(int[][]::new);
			}

		});

		addScaffoldKey(new AbstractScaffoldKey<ROMol>(38, "sys2a",
				"Number of ring systems with 2 conjugated simple rings", true,
				false, true) {

			@Override
			public int calculateForScaffold(Scaffold<ROMol> scaffold) {
				Map<BitSet, Set<BitSet>> rSystems = scaffold.getRingSystems();
				Set<BitSet> conjRings = scaffold.getSimpleConjugatedRings();
				// First remove any with other than 2 rings, then remove any
				// which contain a non-conjugated system
				return (int) rSystems.values().stream()
						.filter(rSys -> rSys.size() == 2)
						.filter(rSys -> conjRings.containsAll(rSys)).count();

			}

			@Override
			public int[][] getMatchingAtomIndices(Scaffold<ROMol> scaffold) {
				Map<BitSet, Set<BitSet>> rSystems = scaffold.getRingSystems();
				Set<BitSet> conjRings = scaffold.getSimpleConjugatedRings();
				// First remove any with other than 2 rings, then remove any
				// which contain a non-conjugated system
				return rSystems.values().stream()
						.filter(rSys -> rSys.size() == 2)
						.filter(rSys -> conjRings.containsAll(rSys))
						.map(bss -> bss.stream().flatMapToInt(bs -> bs.stream())
								.toArray())
						.toArray(int[][]::new);
			}

		});

		addScaffoldKey(new AbstractScaffoldKey<ROMol>(39, "sys2aA",
				"Number of ring systems with 2 simple rings, one conjugated and one non-conjugated",
				true, false, true) {

			@Override
			public int calculateForScaffold(Scaffold<ROMol> scaffold) {
				Map<BitSet, Set<BitSet>> rSystems = scaffold.getRingSystems();
				Set<BitSet> conjRings = scaffold.getSimpleConjugatedRings();
				// First remove any with other than 2 rings, then remove any
				// which contain a conjugated system
				return (int) rSystems.values().stream()
						.filter(rSys -> rSys.size() == 2)
						.filter(rSys -> rSys.stream()
								.filter(conjRings::contains).count() == 1)
						.count();

			}

			@Override
			public int[][] getMatchingAtomIndices(Scaffold<ROMol> scaffold) {
				Map<BitSet, Set<BitSet>> rSystems = scaffold.getRingSystems();
				Set<BitSet> conjRings = scaffold.getSimpleConjugatedRings();
				// First remove any with other than 2 rings, then remove any
				// which contain a conjugated system
				return rSystems.entrySet().stream()
						.filter(rSys -> rSys.getValue().size() == 2)
						.filter(rSys -> rSys.getValue().stream()
								.filter(conjRings::contains).count() == 1)
						.map(bss -> bss.getKey().stream().toArray())
						.toArray(int[][]::new);
			}
		});

		addScaffoldKey(new AbstractScaffoldKey<ROMol>(40, "sys3a",
				"Number of ring systems with 3 conjugated simple rings", true,
				false, true) {

			@Override
			public int calculateForScaffold(Scaffold<ROMol> scaffold) {
				Map<BitSet, Set<BitSet>> rSystems = scaffold.getRingSystems();
				Set<BitSet> conjRings = scaffold.getSimpleConjugatedRings();
				// First remove any with other than 3 rings, then remove any
				// which contain a non-conjugated system
				return (int) rSystems.values().stream()
						.filter(rSys -> rSys.size() == 3)
						.filter(rSys -> conjRings.containsAll(rSys)).count();

			}

			@Override
			public int[][] getMatchingAtomIndices(Scaffold<ROMol> scaffold) {
				Map<BitSet, Set<BitSet>> rSystems = scaffold.getRingSystems();
				Set<BitSet> conjRings = scaffold.getSimpleConjugatedRings();
				// First remove any with other than 3 rings, then remove any
				// which contain a non-conjugated system
				return rSystems.values().stream()
						.filter(rSys -> rSys.size() == 3)
						.filter(rSys -> conjRings.containsAll(rSys))
						.map(bss -> bss.stream().flatMapToInt(bs -> bs.stream())
								.toArray())
						.toArray(int[][]::new);
			}
		});

		addScaffoldKey(new AbstractScaffoldKey<ROMol>(41, "sys3A",
				"Number of ring systems with 3 non-conjugated simple rings",
				true, false, true) {

			@Override
			public int calculateForScaffold(Scaffold<ROMol> scaffold) {
				Map<BitSet, Set<BitSet>> rSystems = scaffold.getRingSystems();
				Set<BitSet> conjRings = scaffold.getSimpleConjugatedRings();
				// First remove any with other than 3 rings, then remove any
				// which contain a conjugated system
				return (int) rSystems.values().stream()
						.filter(rSys -> rSys.size() == 3)
						.filter(rSys -> Collections.disjoint(rSys, conjRings))
						.count();

			}

			@Override
			public int[][] getMatchingAtomIndices(Scaffold<ROMol> scaffold) {
				Map<BitSet, Set<BitSet>> rSystems = scaffold.getRingSystems();
				Set<BitSet> conjRings = scaffold.getSimpleConjugatedRings();
				// First remove any with other than 3 rings, then remove any
				// which contain a conjugated system
				return rSystems.values().stream()
						.filter(rSys -> rSys.size() == 3)
						.filter(rSys -> Collections.disjoint(rSys, conjRings))
						.map(bss -> bss.stream().flatMapToInt(bs -> bs.stream())
								.toArray())
						.toArray(int[][]::new);
			}
		});

		addScaffoldKey(new AbstractScaffoldKey<ROMol>(42, "sys3aA",
				"Number of ring systems with 3 simple rings, at least one conjugated and one non-conjugated",
				true, false, true) {

			@Override
			public int calculateForScaffold(Scaffold<ROMol> scaffold) {
				Map<BitSet, Set<BitSet>> rSystems = scaffold.getRingSystems();
				Set<BitSet> conjRings = scaffold.getSimpleConjugatedRings();
				// First remove any with other than 3 rings, then remove any
				// which contain a conjugated system
				return (int) rSystems.values().stream()
						.filter(rSys -> rSys.size() == 3).filter(rSys -> {
							// Doesn't modify rSystems values
							long nConj = rSys.stream()
									.filter(conjRings::contains).count();
							// Need at least one to be conjugated, but not all
							return nConj > 0 && nConj < 3;
						}).count();

			}

			@Override
			public int[][] getMatchingAtomIndices(Scaffold<ROMol> scaffold) {
				Map<BitSet, Set<BitSet>> rSystems = scaffold.getRingSystems();
				Set<BitSet> conjRings = scaffold.getSimpleConjugatedRings();
				// First remove any with other than 3 rings, then remove any
				// which contain a conjugated system
				return rSystems.entrySet().stream()
						.filter(rSys -> rSys.getValue().size() == 3)
						.filter(rSys -> {
							// Doesn't modify rSystems values
							long nConj = rSys.getValue().stream()
									.filter(conjRings::contains).count();
							// Need at least one to be conjugated, but not all
							return nConj > 0 && nConj < 3;
						}).map(bss -> bss.getKey().stream().toArray())
						.toArray(int[][]::new);
			}

		});

	}

	@Override
	public Scaffold<ROMol> getScaffoldFromMolecule(ROMol mol,
			boolean isMurckoScaffold) {
		return ROMolScaffold.fromMolecule(mol, isMurckoScaffold);
	}

	@Override
	public boolean canDepict() {
		return getScaffoldKeys().stream().anyMatch(k -> k.canDepict());
	}

}
