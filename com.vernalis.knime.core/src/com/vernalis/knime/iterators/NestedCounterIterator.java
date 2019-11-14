/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.vernalis.knime.misc.ArrayUtils;

/**
 * An iterator which systematically enumerates a set of counters from 0 to their
 * respective maxima. This is broadly equivalent to a set of nested loops, for
 * example:
 * 
 * <pre>
 * NestedCounterIterator iter = new NestedCounterIterator(5,3,6);
 * while (iter.hasNext()){
 *     int[] counters = iter.next();
 *     ...
 * }
 * </pre>
 * 
 * is equivalent to:
 * 
 * <pre>
 * for (int i = 0; i < 5; i++){
 *     for (int j = 0; j < 3; j++){
 *         for (int k = 0; i < 6; k++){
 *             // The next line to show the equivalence only - nb order of 'nesting'
 *             int[] counters = new int[] {k, j, i};
 *             ...
 *         }
 *     }
 * }
 * </pre>
 * 
 * NB The counters in the {@code int[]} returned by {@link #next()} are
 * incremented starting with the first array member
 * 
 * @author S.Roughley
 *
 */
public class NestedCounterIterator implements Iterator<int[]> {

	private final int[] maxima;
	private int[] next;

	/**
	 * @param maxima
	 *            The maximum values for each counter
	 */
	public NestedCounterIterator(int... maxima) {
		this.maxima = ArrayUtils.copy(maxima);
		// Initialise next - either all 0, or null if any maxima are <=0
		next = Arrays.stream(maxima).anyMatch(max -> max < 1) ? null
				: new int[maxima.length];

	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public int[] next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		int[] retVal = ArrayUtils.copy(next);
		incrementIterators();
		return retVal;
	}

	private void incrementIterators() {
		for (int i = 0; i < next.length; i++) {
			if (next[i] < maxima[i] - 1) {
				next[i]++;
				return;
			}
			// Reset current iterator
			next[i] = 0;
		}
		// If we are here, then we have exhausted all values...
		next = null;
	}

}
