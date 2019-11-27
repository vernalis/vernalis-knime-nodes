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
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.RowOutput;

import com.vernalis.knime.misc.DoubleSummary;

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
	 * @deprecated {@link #addUniqueInts(int, int, long, BufferedDataContainer)}
	 *             should be used in place as allows more than
	 *             {@value Integer#MAX_VALUE} values to be added
	 */
	@Deprecated
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
		Set<Integer> Numbers = new LinkedHashSet<>();
		Random rand = new Random();
		Integer RandomNum;
		while (Numbers.size() < n) {
			RandomNum = (rand.nextInt(max - min + 1) + min);
			Numbers.add(RandomNum);
		}
		return Numbers;
	}

	/**
	 * Create a collection object of unique random integers
	 * 
	 * @param min
	 *            The minimum value in the collection
	 * @param max
	 *            The maximum value in the collections
	 * @param l
	 *            The number of values to return (will be reduced if greater
	 *            then max-min)
	 * @param dc
	 *            {@link BufferedDataContainer} to add the numbers too
	 * @param exec
	 * @throws CanceledExecutionException
	 * @throws InterruptedException
	 * @{@link Deprecated} See
	 *         {@link #addUniqueInts(int, int, long, RowOutput, ExecutionContext)}
	 */
	@Deprecated
	public static void addUniqueInts(int min, int max, long l,
			BufferedDataContainer dc, ExecutionContext exec)
			throws CanceledExecutionException, InterruptedException {
		addUniqueInts(min, max, l, new BufferedDataTableRowOutput(dc), exec);
	}

	/**
	 * Create a collection object of unique random integers
	 * 
	 * @param min
	 *            The minimum value in the collection
	 * @param max
	 *            The maximum value in the collections
	 * @param l
	 *            The number of values to return (will be reduced if greater
	 *            then max-min)
	 * @param out
	 *            {@link BufferedDataContainer} to add the numbers too
	 * @param exec
	 * @throws CanceledExecutionException
	 * @throws InterruptedException
	 * @deprecated
	 */
	@Deprecated
	public static void addUniqueInts(int min, int max, long l, RowOutput out,
			ExecutionContext exec)
			throws CanceledExecutionException, InterruptedException {
		addInts(min, max, l, -1, out, exec, true);
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
	 * @deprecated {@link #addInts(int, int, long, BufferedDataContainer)}
	 *             should be used in place as allows more than
	 *             {@value Integer#MAX_VALUE} values to be added
	 */
	@Deprecated
	public static Collection<Integer> getInts(int min, int max, final int n) {

		// Check the range is sensible
		if (min > max) {
			int t = min;
			min = max;
			max = t;
		}

		// Now populate the set
		List<Integer> Numbers = new ArrayList<>();
		Random rand = new Random();
		Integer RandomNum;
		while (Numbers.size() < n) {
			RandomNum = (rand.nextInt(max - min + 1) + min);
			Numbers.add(RandomNum);
		}
		return Numbers;
	}

	/**
	 * Add a collection object of random integers to a Data Container
	 * 
	 * @param min
	 *            The minimum value
	 * @param max
	 *            The maximum value
	 * @param n
	 *            The number of values
	 * @param dc
	 *            The data container to add values to
	 * @param exec
	 * @throws CanceledExecutionException
	 * @throws InterruptedException
	 * @{@link Deprecated} See
	 *         {@link #addInts(int, int, long, RowOutput, ExecutionContext)}
	 */
	@Deprecated
	public static void addInts(int min, int max, final long n,
			BufferedDataContainer dc, ExecutionContext exec)
			throws CanceledExecutionException, InterruptedException {
		addInts(min, max, n, -1, new BufferedDataTableRowOutput(dc), exec,
				false);
	}

	/**
	 * Add a collection object of random integers to a Data Container
	 * 
	 * @param min
	 *            The minimum value
	 * @param max
	 *            The maximum value
	 * @param n
	 *            The number of values
	 * @param dc
	 *            The data container to add values to
	 * @param exec
	 * @throws CanceledExecutionException
	 * @throws InterruptedException
	 * @{@link Deprecated} See
	 *         {@link #addInts(int, int, long, RowOutput, ExecutionContext)}
	 */
	@Deprecated
	public static void addInts(int min, int max, final long n, long seed,
			BufferedDataContainer dc, ExecutionContext exec)
			throws CanceledExecutionException, InterruptedException {

		addInts(min, max, n, -1, new BufferedDataTableRowOutput(dc), exec,
				false);
	}

	@Deprecated
	public static void addInts(int min, int max, final long n, RowOutput out,
			ExecutionContext exec)
			throws CanceledExecutionException, InterruptedException {
		addInts(min, max, min, -1, out, exec, false);
	}

	/**
	 * Add a collection object of random integers to a Data Container
	 * 
	 * @param min
	 *            The minimum value
	 * @param max
	 *            The maximum value
	 * @param n
	 *            The number of values
	 * @param seed
	 *            The random seed (-1 for random)
	 * @param out
	 *            The data container to add values to
	 * @param exec
	 * @param unique
	 *            TODO
	 * @throws CanceledExecutionException
	 * @throws InterruptedException
	 * @since 1.25.0
	 */
	public static DoubleSummary addInts(int min, int max, long n,
			final long seed, RowOutput out, ExecutionContext exec,
			boolean unique)
			throws CanceledExecutionException, InterruptedException {

		// Check the range is sensible
		if (min > max) {
			int t = min;
			min = max;
			max = t;
		}

		// Ensure that we dont get stuck in an infinite effort to create more
		// integers than are in
		// the min max range
		if (unique && n > (max - min + 1)) {
			n = max - min + 1;
		}
		// Now populate the set
		Random rand = new Random();
		if (seed >= 0) {
			rand.setSeed(seed);
		}

		DoubleSummary retVal = new DoubleSummary();
		BitSet seen = new BitSet();

		long numbersAdded = 0;
		while (numbersAdded < n) {
			int randomNum = (rand.nextInt(max - min + 1) + min);
			if (unique) {
				if (seen.get(randomNum)) {
					continue;
				}
				seen.set(randomNum);
			}
			retVal.accept(randomNum);
			out.push(new DefaultRow("Row_" + (numbersAdded++),
					new IntCell(randomNum)));
			exec.setProgress((double) numbersAdded / n,
					"Added " + numbersAdded + " of " + n + " rows");
			exec.checkCanceled();
		}
		return retVal;
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
	 * @deprecated {@link #addUniqueDoubles(int, int, long, BufferedDataContainer)}
	 *             should be used in place as allows more than
	 *             {@value Integer#MAX_VALUE} values to be added
	 */
	@Deprecated
	public static Collection<Double> getUniqueDoubles(Double min, Double max,
			final int n) {

		// Check the range is sensible
		if (min > max) {
			Double t = min;
			min = max;
			max = t;
		}

		// Now populate the set
		Set<Double> Numbers = new LinkedHashSet<>();
		Double RandomNum;
		while (Numbers.size() < n) {
			RandomNum = min + (Math.random() * ((max - min) + 1));
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
	 * @param dc
	 *            The {@link BufferedDataContainer} to add the values to
	 * @param exec
	 * @return A Set object of random doubles
	 * @throws CanceledExecutionException
	 * @throws InterruptedException
	 * @{@link Deprecated} See
	 *         {@link #addUniqueDoubles(Double, Double, long, RowOutput, ExecutionContext)}
	 */
	@Deprecated
	public static void addUniqueDoubles(Double min, Double max, final long n,
			BufferedDataContainer dc, ExecutionContext exec)
			throws CanceledExecutionException, InterruptedException {
		addDoubles(min, max, n, -1, new BufferedDataTableRowOutput(dc), exec,
				true);
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
	 * @param out
	 *            The {@link BufferedDataContainer} to add the values to
	 * @param exec
	 * @return A Set object of random doubles
	 * @throws CanceledExecutionException
	 * @throws InterruptedException
	 */
	@Deprecated
	public static void addUniqueDoubles(Double min, Double max, final long n,
			RowOutput out, ExecutionContext exec)
			throws CanceledExecutionException, InterruptedException {
		addDoubles(min, max, n, -1, out, exec, true);
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
	 * @deprecated {@link #addDoubles(int, int, long, BufferedDataContainer)}
	 *             should be used in place as allows more than
	 *             {@value Integer#MAX_VALUE} values to be added
	 */
	@Deprecated
	public static Collection<Double> getDoubles(Double min, Double max,
			final int n) {

		// Check the range is sensible
		if (min > max) {
			Double t = min;
			min = max;
			max = t;
		}

		// Now populate the set
		List<Double> Numbers = new ArrayList<>();
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
	 * @param dc
	 *            The {@link BufferedDataContainer} to add the values to
	 * @param exec
	 * @throws CanceledExecutionException
	 * @throws InterruptedException
	 * @{@link Deprecated} See
	 *         {@link #addDoubles(Double, Double, long, RowOutput, ExecutionContext)}
	 */
	@Deprecated
	public static void addDoubles(Double min, Double max, final long n,
			BufferedDataContainer dc, ExecutionContext exec)
			throws CanceledExecutionException, InterruptedException {
		addDoubles(min, max, n, new BufferedDataTableRowOutput(dc), exec);
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
	 * @param out
	 *            The {@link BufferedDataContainer} to add the values to
	 * @param exec
	 * @throws CanceledExecutionException
	 * @throws InterruptedException
	 */
	@Deprecated
	public static void addDoubles(Double min, Double max, final long n,
			RowOutput out, ExecutionContext exec)
			throws CanceledExecutionException, InterruptedException {
		addDoubles(min, max, n, -1, out, exec, false);
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
	 * @param out
	 *            The {@link BufferedDataContainer} to add the values to
	 * @param exec
	 * @throws CanceledExecutionException
	 * @throws InterruptedException
	 * @return {@link DoubleSummary} of the available parameters
	 * @since 1.25.0
	 */
	public static DoubleSummary addDoubles(Double min, Double max, long n,
			long seed, RowOutput out, ExecutionContext exec, boolean unique)
			throws CanceledExecutionException, InterruptedException {

		// Check the range is sensible
		if (min > max) {
			Double t = min;
			min = max;
			max = t;
		}

		// Now populate the set
		Set<Double> seen = new HashSet<>();
		DoubleSummary retVal = new DoubleSummary();
		long numbersAdded = 0;
		Random rand = new Random();
		if (seed >= 0) {
			rand.setSeed(numbersAdded);
		}

		while (numbersAdded < n) {
			double randomNum = min + rand.nextDouble() * (max - min);
			if (randomNum > max) {
				randomNum = Math.nextDown(randomNum);
			}
			if (unique && !seen.add(randomNum)) {
				continue;
			}
			retVal.accept(randomNum);
			out.push(new DefaultRow("Row_" + (numbersAdded++),
					new DoubleCell(randomNum)));
			exec.setProgress((double) numbersAdded / n,
					"Added " + numbersAdded + " of " + n + " rows");
			exec.checkCanceled();
		}
		return retVal;
	}
}