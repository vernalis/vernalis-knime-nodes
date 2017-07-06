/*******************************************************************************
 * Copyright (c) 2015, 2017,  Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License, Version 3, as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.mmp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class contains methods relating to finding all combinations of a
 * incoming collection
 * 
 * Adapted from
 * http://codereview.stackexchange.com/questions/26854/recursive-method
 * -to-return-a-set-of-all-combinations
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 */
public class CombinationFinder {

	/**
	 * Instantiates a new combination finder. DO NOT INSTANTIATE!
	 */
	private CombinationFinder() {
		// Dont instantiate
	}

	/**
	 * Gets the combinations for the incoming collection
	 *
	 * @param <T>
	 *            the type of the collection element
	 * @param group
	 *            The list of all possible members
	 * @param subsetSize
	 *            The number of members required in each combination
	 * @return The Set of Sets of members
	 */
	public static <T> Set<Set<T>> getCombinationsFor(Collection<T> group, int subsetSize) {
		Set<Set<T>> resultingCombinations = new HashSet<>();
		int totalSize = group.size();
		if (subsetSize == 0) {
			emptySet(resultingCombinations);
		} else if (subsetSize <= totalSize) {
			List<T> remainingElements = new ArrayList<>(group);
			T X = popLast(remainingElements);

			Set<Set<T>> combinationsExclusiveX = getCombinationsFor(remainingElements, subsetSize);
			Set<Set<T>> combinationsInclusiveX =
					getCombinationsFor(remainingElements, subsetSize - 1);
			for (Set<T> combination : combinationsInclusiveX) {
				combination.add(X);
			}
			resultingCombinations.addAll(combinationsExclusiveX);
			resultingCombinations.addAll(combinationsInclusiveX);
		}
		return resultingCombinations;
	}

	/**
	 * Method to add a new Empty set to the argument
	 *
	 * @param <T>
	 *            the type of the collection element
	 * @param resultingCombinations
	 *            the resulting combinations
	 */
	private static <T> void emptySet(Set<Set<T>> resultingCombinations) {
		resultingCombinations.add(new TreeSet<T>());
	}

	/**
	 * Method to pop the last element off the incoming collection argument
	 *
	 * @param <T>
	 *            the type of the collection element
	 * @param remainingElements
	 *            the remaining elements
	 * @return the last element, which has been removed
	 */
	private static <T> T popLast(List<T> remainingElements) {
		return remainingElements.remove(remainingElements.size() - 1);
	}
}
