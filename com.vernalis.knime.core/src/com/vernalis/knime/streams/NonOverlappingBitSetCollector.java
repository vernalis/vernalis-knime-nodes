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
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * A Stream collector to collect a stream of {@link BitSet}s to a {@link Set} of
 * {@link BitSet}s, where any intersecting BitSets are 'OR'd together into a
 * single BitSet in the Set. Therefore, no two BitSets in the resulting set
 * intersect.
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
final class NonOverlappingBitSetCollector
		implements Collector<BitSet, Set<BitSet>, Set<BitSet>> {

	@Override
	public BiConsumer<Set<BitSet>, BitSet> accumulator() {
		return new BiConsumer<>() {

			@Override
			public void accept(Set<BitSet> t, BitSet u) {
				Set<BitSet> intersects =
						t.stream().filter(bs -> bs.intersects(u))
								.collect(Collectors.toSet());
				// Generate a new BitSet from the intersects and the new value
				BitSet newVal =
						intersects.stream().collect(BitSetCollectors.BitSetOr);
				newVal.or(u);
				t.add(newVal);
				// All the separate BitSets here will intersect once they
				// are 'OR'd with the new BitSet, so we remove them...
				t.removeAll(intersects);
			}
		};

	}

	@Override
	public Set<Characteristics> characteristics() {
		return Collections.singleton(Characteristics.UNORDERED);
	}

	@Override
	public BinaryOperator<Set<BitSet>> combiner() {
		return new BinaryOperator<>() {

			@Override
			public Set<BitSet> apply(Set<BitSet> t, Set<BitSet> u) {
				for (BitSet bs : u) {
					accumulator().accept(t, bs);
				}
				return t;
			}
		};
	}

	@Override
	public Function<Set<BitSet>, Set<BitSet>> finisher() {
		return Function.identity();
	}

	@Override
	public Supplier<Set<BitSet>> supplier() {
		return HashSet::new;
	}

}
