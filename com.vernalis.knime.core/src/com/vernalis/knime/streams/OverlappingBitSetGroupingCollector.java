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
package com.vernalis.knime.streams;

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * A grouping collector to collect a {@link java.util.stream.Stream} of
 * {@link BitSet}s to a {@link Map}. The Map keys are a BitSet which is the
 * intersection of the BitSets which form its value. The values are a
 * {@link Set} of BitSets which are mutually intersecting. No two keys in the
 * map will intersect each other, and accordingly, no two values will contain
 * BitSets which will overlap. The values are made up of the original BitSets,
 * and so any further operations which modifies these will in turn modify the
 * incoming Stream BitSets, must call {@link BitSet#clone()} to not cause
 * side-effects, or wrap this collector with a call to
 * {@link BitSetCollectors#cloningBitSetCollector(Collector)}
 * 
 * Instances of this class should be accessed by the static method in
 * {@link BitSetCollectors}
 * 
 * @see BitSetCollectors#toOverlappingBitSetGroups()
 * 
 * @author S.Roughley knime@vernalis.com
 *
 *
 * @since v1.34.0
 */
final class OverlappingBitSetGroupingCollector implements
		Collector<BitSet, Map<BitSet, Set<BitSet>>, Map<BitSet, Set<BitSet>>> {

	private final boolean unmodifiableSets;

	/**
	 * Constructor without unmodifiable sets
	 *
	 * @since v1.34.0
	 */
	OverlappingBitSetGroupingCollector() {
		this(false);
	}

	/**
	 * Constructor
	 * 
	 * @param unmodifiableSets
	 *            whether the Set<BitSet> Map values are unmodifiable
	 *
	 * @since v1.34.0
	 */
	OverlappingBitSetGroupingCollector(boolean unmodifiableSets) {
		this.unmodifiableSets = unmodifiableSets;
	}

	@Override
	public Supplier<Map<BitSet, Set<BitSet>>> supplier() {
		return HashMap::new;
	}

	@Override
	public BiConsumer<Map<BitSet, Set<BitSet>>, BitSet> accumulator() {
		return new BiConsumer<>() {

			@Override
			public void accept(Map<BitSet, Set<BitSet>> t, BitSet u) {
				// Find any existing groups that we intersect
				Set<BitSet> intersects =
						t.keySet().stream().filter(bs -> bs.intersects(u))
								.collect(Collectors.toSet());
				// Create a new key
				BitSet newKey =
						intersects.stream().collect(BitSetCollectors.BitSetOr);
				newKey.or(u);
				// And a new value
				Set<BitSet> newSet = intersects.stream().map(bs -> t.get(bs))
						.flatMap(s -> s.stream()).collect(Collectors.toSet());
				newSet.add(u);

				// Remove the old intersecting groups - we do this before adding
				// the new k, v pair - see #164
				t.keySet().removeAll(intersects);

				// Put them in the accumulator
				t.put(newKey, newSet);

			}
		};
	}

	@Override
	public BinaryOperator<Map<BitSet, Set<BitSet>>> combiner() {
		return new BinaryOperator<>() {

			@Override
			public Map<BitSet, Set<BitSet>> apply(Map<BitSet, Set<BitSet>> t,
					Map<BitSet, Set<BitSet>> u) {
				for (Set<BitSet> s : u.values()) {
					for (BitSet bs : s) {
						accumulator().accept(t, bs);
					}
				}
				return t;
			}
		};
	}

	@Override
	public Function<Map<BitSet, Set<BitSet>>, Map<BitSet, Set<BitSet>>>
			finisher() {
		return unmodifiableSets
				? m -> m.entrySet().stream()
						.collect(Collectors.toMap(ent -> ent.getKey(),
								ent -> Collections
										.unmodifiableSet(ent.getValue())))
				: Function.identity();
	}

	@Override
	public Set<Characteristics> characteristics() {
		return Collections.singleton(Characteristics.UNORDERED);
	}

}
