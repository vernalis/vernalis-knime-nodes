/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.iterators;

import java.util.Comparator;

import org.knime.core.util.Pair;

/**
 * A class to wrap two iterables of with the same generic type to be enable to
 * perform pair-wise iteration
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The generic type parameter
 */
public class PairwiseIterable<T> implements Iterable<Pair<T, T>> {
	Iterable<T> queryList, referenceList;
	private Comparator<T> comparator;
	private boolean trackSkippedQueries;

	/**
	 * Overloaded constructor without tracking of skipped queries/references
	 * 
	 * @param queryList
	 *            The query list
	 * @param referenceList
	 *            The reference list
	 * @param comparator
	 *            The comparator, or <code>null</code> to use the default
	 *            comparator for the type T
	 */
	public PairwiseIterable(Iterable<T> queryList, Iterable<T> referenceList,
			Comparator<T> comparator) {
		this(queryList, referenceList, comparator, false);
	}

	/**
	 * Constructor
	 * 
	 * @param queryList
	 *            The query list
	 * @param referenceList
	 *            The reference list
	 * @param comparator
	 *            The comparator, or <code>null</code> to use the default
	 *            comparator for the type T
	 * @param trackSkippedQueries
	 *            Should the iterator track those query items which were skipped
	 *            as they had no corresponding reference items, and vice versa?
	 *            See
	 *            {@link PairIterator#PairIterator(Iterable, Iterable, Comparator, boolean)
	 *            for further details
	 */
	public PairwiseIterable(Iterable<T> queryList, Iterable<T> referenceList,
			Comparator<T> comparator, boolean trackSkippedQueries) {
		super();
		this.queryList = queryList;
		this.referenceList = referenceList;
		this.comparator = comparator;
		this.trackSkippedQueries = trackSkippedQueries;
	}

	@Override
	public PairIterator<T> iterator() {
		return new PairIterator<>(queryList, referenceList, comparator, trackSkippedQueries);
	}

}
