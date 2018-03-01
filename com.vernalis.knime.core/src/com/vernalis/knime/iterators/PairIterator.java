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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.knime.core.util.Pair;

/**
 * An iterator to return pairs of matching values from two iterables. The
 * iterables must be sorted otherwise unpredictable behaviour may result! A
 * {@link Comparator} can be provided.
 * <p>
 * Example usage:
 * 
 * <pre>
 * List&lt;String&gt; l1 = Arrays.asList(new String[] {"a","B","D","E","g"});
 * List&lt;String&gt; l2 = Arrays.asList(new String[] {"A","B","c","e","F","g"});
 * Comparator&lt;String&gt; comp = new Comparator&lt;String&gt;() {
 * 
 *     &#64;Override
 *     public int compare(String o1, String o2) {
 *         return o1.compareToIgnoreCase(o2);
 *     }
 * };
 * PairIterator&lt;String&gt; iter = new PairIterator(l1,l2, comp);
 * while(iter.hasNext()){
 *     System.out.println(iter.next());
 * }
 * 
 * Gives...
 * &lt;a;A&gt;
 * &lt;B;B&gt;
 * &lt;E;e&gt;
 * &lt;g;g&gt;
 * </pre>
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The type parameter. T must implement {@link Comparable} TODO:
 *            Allow skippedQueries/skippedReferences to be added directly to
 *            BufferedDataContainer instances rather than stored in memory
 */
public class PairIterator<T> implements Iterator<Pair<T, T>> {
	private Pair<T, T> next = null;
	private Iterator<T> queryIterator, referenceIterator;
	private Comparator<T> comparator;
	private T queryItem = null;
	private T referenceItem = null;
	private List<T> skippedQueries, skippedReferences;
	private boolean trackSkippedQueries;

	/**
	 * Constructor
	 * 
	 * @param queryList
	 *            The query list
	 * @param referenceList
	 *            The reference list
	 * @param comparator
	 *            The Compator. If <code>null</code>, then the node will attempt
	 *            to compare using the default comparator for the type T
	 * @param trackSkippedQueries
	 *            Should the call to {@link #next()} track those query items
	 *            which were skipped as they had no corresponding reference
	 *            items, and vice versa? Skipped items can be accessed with
	 *            {@link #skippedQueries()} and {@link #skippedReferences()}
	 */
	public PairIterator(Iterable<T> queryList, Iterable<T> referenceList, Comparator<T> comparator,
			boolean trackSkippedQueries) {
		queryIterator = queryList.iterator();
		referenceIterator = referenceList.iterator();
		this.comparator = comparator;
		if (trackSkippedQueries) {
			skippedQueries = new ArrayList<>();
			skippedReferences = new ArrayList<>();
		} else {
			skippedQueries = null;
			skippedReferences = null;
		}
		this.trackSkippedQueries = trackSkippedQueries;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean hasNext() {
		if (next != null) {
			// We already populate the next value, so yes, we do have a next
			return true;
		}

		// OK, lets figure out whether we have another value, and store it if we
		// do
		while (queryIterator.hasNext()) {
			getNextQueryItem();
			if (referenceItem == null && referenceIterator.hasNext()) {
				getNextReferenceItem();
			}
			int comp = comparator == null ? ((Comparable<T>) queryItem).compareTo(referenceItem)
					: comparator.compare(queryItem, referenceItem);
			if (comp == 0) {
				// they match!
				setNextIteratorValue();
				return true;
			}

			if (comp < 0) {
				// reference is ahead of query so move to next query item

				continue;
			}

			// reference is behind query so move to next reference item
			while (referenceIterator.hasNext()) {
				getNextReferenceItem();
				comp = comparator == null ? ((Comparable<T>) referenceItem).compareTo(queryItem)
						: comparator.compare(queryItem, referenceItem);
				if (comp == 0) {
					// They match
					setNextIteratorValue();
					return true;
				}

				if (comp < 0) {
					// didnt match - next member of query
					break;
				}

			}

		}
		if (trackSkippedQueries) {
			referenceIterator.forEachRemaining(x -> skippedReferences.add(x));
		}
		// We didnt have a next value
		return false;
	}

	/**
	 * 
	 */
	private void setNextIteratorValue() {
		next = new Pair<>(queryItem, referenceItem);
		if (trackSkippedQueries) {
			skippedQueries.remove(queryItem);
			skippedReferences.remove(referenceItem);
		}
	}

	/**
	 * 
	 */
	private void getNextQueryItem() {
		queryItem = queryIterator.next();
		if (trackSkippedQueries) {
			skippedQueries.add(queryItem);
		}
	}

	/**
	 * 
	 */
	private void getNextReferenceItem() {
		referenceItem = referenceIterator.next();
		if (trackSkippedQueries) {
			skippedReferences.add(referenceItem);
		}
	}

	@Override
	public Pair<T, T> next() {
		// Ensure the next is populated if it can be
		if (!hasNext()) {
			// There is no next...
			throw new NoSuchElementException();
		}
		// Get the value
		Pair<T, T> retVal = next;
		// and clear the stored value so that the next #hasNext() call works
		next = null;
		if (trackSkippedQueries) {
			skippedQueries.clear();
			skippedReferences.clear();
		}
		return retVal;
	}

	/**
	 * This returns members of the Queries Iterator which were skipped. It is
	 * populated by a call to {@link #hasNext()}, and cleared by a call to
	 * {@link #next()}, so should be used as follows
	 * 
	 * <pre>
	 * PairIterator<T> iter =
	 * 		new PairWiseIterable<T>(queryList, referenceList, comparator, true).iterator();
	 * while (iter.hasNext()) {
	 * 	List<T> skipped = iter.skippedQueries();
	 * 	// Do something with them...
	 * 	T next = iter.next();
	 * 	// Do something with it...
	 * }
	 * // Finally deal with any skipped without finding another match
	 * List<T> skipped = iter.skippedQueries();
	 * // Do something with them...
	 * 
	 * </pre>
	 * 
	 * @return The skipped queries
	 */
	public List<T> skippedQueries() {
		return skippedQueries;
	}

	/**
	 * @return The members of the References iterator which were skipped. See
	 *         {@link #skippedQueries} for further details
	 */
	public List<T> skippedReferences() {
		return skippedReferences;
	}

}
