/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2013, Vernalis (R&D) Ltd
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ------------------------------------------------------------------------
 */
package com.vernalis.nodes.misc.randomnos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Utility functions to generate random numbers according to various conditions
 * 
 * @author Stephen
 * 
 */
public class RandomNumbers {

	/**
	 * Create a collection object of unique random integers
	 * 
	 * @param min
	 *            The minimum value in the collection
	 * @param max
	 *            The maximum value in the collections
	 * @param n
	 *            The number of values to return (will be reduced if greater
	 *            then max-min)
	 * @return A set of unique integers
	 */
	public static Collection<Integer> getUniqueInts(int min, int max, int n) {

		// Check the range is sensible
		if (min > max) {
			int t = min;
			min = max;
			max = t;
		}

		// Ensure that we dont get stuck in an infinite effort to create more
		// integers than are in
		// the min max range
		if (n > (max - min + 1)) {
			n = max - min + 1;
		}

		// Now populate the set
		Set<Integer> Numbers = new LinkedHashSet<Integer>();
		Random rand = new Random();
		Integer RandomNum;
		while (Numbers.size() < n) {
			RandomNum = (rand.nextInt(max - min + 1) + min);
			Numbers.add(RandomNum);
		}
		return Numbers;
	}

	/**
	 * Create a collection object of random integers
	 * 
	 * @param min
	 *            The minimum value
	 * @param max
	 *            The maximum value
	 * @param n
	 *            The number of values
	 * @return A List object containing the random integers
	 */
	public static Collection<Integer> getInts(int min, int max, final int n) {

		// Check the range is sensible
		if (min > max) {
			int t = min;
			min = max;
			max = t;
		}

		// Now populate the set
		List<Integer> Numbers = new ArrayList<Integer>();
		Random rand = new Random();
		Integer RandomNum;
		while (Numbers.size() < n) {
			RandomNum = (rand.nextInt(max - min + 1) + min);
			Numbers.add(RandomNum);
		}
		return Numbers;
	}

	/**
	 * Create a collection of unique random doubles
	 * 
	 * @param min
	 *            The minimum value in the set
	 * @param max
	 *            The maximum value in the set
	 * @param n
	 *            The number of values to include
	 * @return A Set object of random doubles
	 */
	public static Collection<Double> getUniqueDoubles(Double min, Double max,
			final int n) {

		// Check the range is sensible
		if (min > max) {
			Double t = min;
			min = max;
			max = t;
		}

		// Now populate the set
		Set<Double> Numbers = new LinkedHashSet<Double>();
		Double RandomNum;
		while (Numbers.size() < n) {
			RandomNum = min + (Math.random() * ((max - min) + 1));
			Numbers.add(RandomNum);
		}
		return Numbers;
	}

	/**
	 * Create a collection of random doubles
	 * 
	 * @param min
	 *            The minimum value in the set
	 * @param max
	 *            The maximum value in the set
	 * @param n
	 *            The number of values to include
	 * @return A List object of random doubles
	 */
	public static Collection<Double> getDoubles(Double min, Double max,
			final int n) {

		// Check the range is sensible
		if (min > max) {
			Double t = min;
			min = max;
			max = t;
		}

		// Now populate the set
		List<Double> Numbers = new ArrayList<Double>();
		Double RandomNum;
		while (Numbers.size() < n) {
			RandomNum = min + (Math.random() * ((max - min) + 1));
			Numbers.add(RandomNum);
		}
		return Numbers;
	}

}