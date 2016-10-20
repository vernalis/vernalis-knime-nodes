/*******************************************************************************
 * Copyright (c) 2015, Vernalis (R&D) Ltd
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

/**
 * This method finds the number of combinations of objects in a list, returning
 * them as a Set of Sets
 * 
 * Adapted from
 * http://codereview.stackexchange.com/questions/26854/recursive-method
 * -to-return-a-set-of-all-combinations
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 */
public class CombinationFinder {
	private CombinationFinder() {
		// Dont instantiate
	}

	/**
	 * @param group
	 *            The list of all possible members
	 * @param subsetSize
	 *            The number of members required in each combination
	 * @return The Set of Sets of members
	 */
	public static <T> Set<Set<T>> getCombinationsFor(Collection<T> group,
			int subsetSize) {
		Set<Set<T>> resultingCombinations = new HashSet<Set<T>>();
		int totalSize = group.size();
		if (subsetSize == 0) {
			emptySet(resultingCombinations);
		} else if (subsetSize <= totalSize) {
			List<T> remainingElements = new ArrayList<T>(group);
			T X = popLast(remainingElements);

			Set<Set<T>> combinationsExclusiveX = getCombinationsFor(
					remainingElements, subsetSize);
			Set<Set<T>> combinationsInclusiveX = getCombinationsFor(
					remainingElements, subsetSize - 1);
			for (Set<T> combination : combinationsInclusiveX) {
				combination.add(X);
			}
			resultingCombinations.addAll(combinationsExclusiveX);
			resultingCombinations.addAll(combinationsInclusiveX);
		}
		return resultingCombinations;
	}

	private static <T> void emptySet(Set<Set<T>> resultingCombinations) {
		resultingCombinations.add(new HashSet<T>());
	}

	private static <T> T popLast(List<T> remainingElements) {
		return remainingElements.remove(remainingElements.size() - 1);
	}
}
