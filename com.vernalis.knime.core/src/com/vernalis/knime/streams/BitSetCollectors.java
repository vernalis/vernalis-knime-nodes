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
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A class of collector methods to operate on Streams of BitSets
 * 
 * @author S.Roughley knime@vernalis.com
 *
 *
 * @since v1.34.0
 */
public class BitSetCollectors {

	private BitSetCollectors() {
		// Utility Class - Do not Instantiate
	}

	/**
	 * A Stream Collector to collect a stream of bitsets by logical 'or'
	 * operations
	 */
	public static final Collector<BitSet, BitSet, BitSet> BitSetOr =
			Collector.of(BitSet::new, BitSet::or, (a, b) -> {
				a.or(b);
				return a;
			});

	/**
	 * A Stream Collector to collect a stream of bitsets by logical 'and'
	 * operations
	 */
	public static final Collector<BitSet, BitSet, BitSet> BitSetAnd =
			Collector.of(BitSet::new, BitSet::and, (a, b) -> {
				a.and(b);
				return a;
			});

	/**
	 * A Stream collector to collect a stream of {@link BitSet}s to a
	 * {@link Set} of {@link BitSet}s, where any intersecting BitSets are 'OR'd
	 * together into a single BitSet in the Set. Therefore, no two BitSets in
	 * the resulting set intersect. The incoming bitsets are not modified
	 * 
	 * @return a collector to collect the stream to a Set of non-intersecting
	 *         BitSets, 'OR'ing any intersecting sets to combine them
	 */
	public static final NonOverlappingBitSetCollector
			toNonOverlappingBitSetSet() {
		return new NonOverlappingBitSetCollector();
	}

	/**
	 * Method to create a grouping collector to act on a Stream of
	 * {@link BitSet}s.
	 * 
	 * <p>
	 * The key of each group is the intersection of the individual group
	 * members.
	 * </p>
	 * <p>
	 * The values are a {@link Set} of BitSets which are mutually intersecting.
	 * No two keys in the map will intersect each other, and accordingly, no two
	 * values will contain BitSets which will overlap. The value {@link Set}s
	 * are mutable. If immutable set values are required, then use
	 * {@link #toOverlappingBitSetGroups(boolean)}
	 * </p>
	 * <p>
	 * <strong>NB</strong> The values are made up of the original BitSets, and
	 * so any further operations which modifies these will in turn modify the
	 * incoming Stream BitSets, must call {@link BitSet#clone()} to not cause
	 * side-effects, or wrap this collector with a call to
	 * {@link BitSetCollectors#cloningBitSetCollector(Collector)}
	 * 
	 * @return A grouping Collector which will group a Stream of BitSets into
	 *         groups of overlapping BitSets.
	 *
	 * @since v1.34.0
	 */
	public static final OverlappingBitSetGroupingCollector
			toOverlappingBitSetGroups() {
		return new OverlappingBitSetGroupingCollector();
	}

	/**
	 * Method to create a grouping collector to act on a Stream of
	 * {@link BitSet}s.
	 * 
	 * <p>
	 * The key of each group is the intersection of the individual group
	 * members.
	 * </p>
	 * <p>
	 * The values are a {@link Set} of BitSets which are mutually intersecting.
	 * No two keys in the map will intersect each other, and accordingly, no two
	 * values will contain BitSets which will overlap. The value {@link Set}s
	 * are unmodifiable if the parameter {@code unmodifiableSets} is
	 * {@code true}
	 * </p>
	 * <p>
	 * <strong>NB</strong> The values are made up of the original BitSets, and
	 * so any further operations which modifies these will in turn modify the
	 * incoming Stream BitSets, must call {@link BitSet#clone()} to not cause
	 * side-effects, or wrap this collector with a call to
	 * {@link BitSetCollectors#cloningBitSetCollector(Collector)}
	 * 
	 * @param unmodifiableSets
	 *            whether the Map values resulting from the collector are
	 *            unmodifiable sets
	 * 
	 * @return A grouping Collector which will group a Stream of BitSets into
	 *         groups of overlapping BitSets.
	 *
	 * @since v1.34.0
	 */
	public static final OverlappingBitSetGroupingCollector
			toOverlappingBitSetGroups(boolean unmodifiableSets) {
		return new OverlappingBitSetGroupingCollector(unmodifiableSets);
	}

	/**
	 * A method to wrap a BitSet {@link Collector} into a collector which
	 * creates a clone of each BitSet prior to Collecting
	 * 
	 * @param <A>
	 *            The mutable accumulation type of the wrapped collector
	 * @param <R>
	 *            The result type of the wrapped collector
	 * @param collector
	 *            The collector to wrap, which must accept {@link BitSet} as its
	 *            input elements
	 * 
	 * @return A wrapped collector
	 *
	 * @since v1.34.0
	 */
	public static <A, R> Collector<BitSet, A, R>
			cloningBitSetCollector(Collector<BitSet, A, R> collector) {
		return new Collector<>() {

			@Override
			public Supplier<A> supplier() {
				return collector.supplier();
			}

			@Override
			public BiConsumer<A, BitSet> accumulator() {
				return new BiConsumer<>() {

					@Override
					public void accept(A t, BitSet u) {
						collector.accumulator().accept(t,
								u == null ? null : (BitSet) u.clone());

					}
				};
			}

			@Override
			public BinaryOperator<A> combiner() {
				return collector.combiner();
			}

			@Override
			public Function<A, R> finisher() {
				return collector.finisher();
			}

			@Override
			public Set<Characteristics> characteristics() {
				return collector.characteristics();
			}
		};

	}
}
