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
import org.knime.core.node.streamable.RowOutput;

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
	 * @{@link Deprecated} See
	 *         {@link #addUniqueInts(int, int, long, RowOutput, ExecutionContext)}
	 */
	public static void addUniqueInts(int min, int max, long l, BufferedDataContainer dc,
			ExecutionContext exec) throws CanceledExecutionException {

		// Check the range is sensible
		if (min > max) {
			int t = min;
			min = max;
			max = t;
		}

		// Ensure that we dont get stuck in an infinite effort to create more
		// integers than are in
		// the min max range
		if (l > (max - min + 1)) {
			l = max - min + 1;
		}

		// Now populate the set
		Set<Integer> Numbers = new HashSet<>();
		long numbersAdded = 0;
		Random rand = new Random();
		Integer RandomNum;
		while (numbersAdded < l) {
			RandomNum = (rand.nextInt(max - min + 1) + min);
			if (Numbers.add(RandomNum)) {
				dc.addRowToTable(new DefaultRow("Row_" + (numbersAdded++), new IntCell(RandomNum)));
				exec.setProgress((double) numbersAdded / l,
						"Added " + numbersAdded + " of " + l + " rows");
			}
			exec.checkCanceled();
		}
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
	 */
	public static void addUniqueInts(int min, int max, long l, RowOutput out, ExecutionContext exec)
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
		if (l > (max - min + 1)) {
			l = max - min + 1;
		}

		// Now populate the set
		Set<Integer> Numbers = new HashSet<>();
		long numbersAdded = 0;
		Random rand = new Random();
		Integer RandomNum;
		while (numbersAdded < l) {
			RandomNum = (rand.nextInt(max - min + 1) + min);
			if (Numbers.add(RandomNum)) {
				out.push(new DefaultRow("Row_" + (numbersAdded++), new IntCell(RandomNum)));
				exec.setProgress((double) numbersAdded / l,
						"Added " + numbersAdded + " of " + l + " rows");
			}
			exec.checkCanceled();
		}
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
	 * @{@link Deprecated} See
	 *         {@link #addInts(int, int, long, RowOutput, ExecutionContext)}
	 */
	public static void addInts(int min, int max, final long n, BufferedDataContainer dc,
			ExecutionContext exec) throws CanceledExecutionException {

		// Check the range is sensible
		if (min > max) {
			int t = min;
			min = max;
			max = t;
		}

		// Now populate the set
		Random rand = new Random();
		Integer RandomNum;
		long numbersAdded = 0;
		while (numbersAdded < n) {
			RandomNum = (rand.nextInt(max - min + 1) + min);
			dc.addRowToTable(new DefaultRow("Row_" + (numbersAdded++), new IntCell(RandomNum)));
			exec.setProgress((double) numbersAdded / n,
					"Added " + numbersAdded + " of " + n + " rows");
			exec.checkCanceled();
		}
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
	 * @param out
	 *            The data container to add values to
	 * @param exec
	 * @throws CanceledExecutionException
	 * @throws InterruptedException
	 */
	public static void addInts(int min, int max, final long n, RowOutput out, ExecutionContext exec)
			throws CanceledExecutionException, InterruptedException {

		// Check the range is sensible
		if (min > max) {
			int t = min;
			min = max;
			max = t;
		}

		// Now populate the set
		Random rand = new Random();
		Integer RandomNum;
		long numbersAdded = 0;
		while (numbersAdded < n) {
			RandomNum = (rand.nextInt(max - min + 1) + min);
			out.push(new DefaultRow("Row_" + (numbersAdded++), new IntCell(RandomNum)));
			exec.setProgress((double) numbersAdded / n,
					"Added " + numbersAdded + " of " + n + " rows");
			exec.checkCanceled();
		}
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
	public static Collection<Double> getUniqueDoubles(Double min, Double max, final int n) {

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
	 * @{@link Deprecated} See
	 *         {@link #addUniqueDoubles(Double, Double, long, RowOutput, ExecutionContext)}
	 */
	public static void addUniqueDoubles(Double min, Double max, final long n,
			BufferedDataContainer dc, ExecutionContext exec) throws CanceledExecutionException {

		// Check the range is sensible
		if (min > max) {
			Double t = min;
			min = max;
			max = t;
		}

		// Now populate the set
		Set<Double> Numbers = new HashSet<>();
		long numbersAdded = 0;
		Double RandomNum;
		while (numbersAdded < n) {
			RandomNum = min + (Math.random() * ((max - min) + 1));
			if (Numbers.add(RandomNum)) {
				dc.addRowToTable(
						new DefaultRow("Row_" + (numbersAdded++), new DoubleCell(RandomNum)));
				exec.setProgress((double) numbersAdded / n,
						"Added " + numbersAdded + " of " + n + " rows");
			}
			exec.checkCanceled();
		}

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
	public static void addUniqueDoubles(Double min, Double max, final long n, RowOutput out,
			ExecutionContext exec) throws CanceledExecutionException, InterruptedException {

		// Check the range is sensible
		if (min > max) {
			Double t = min;
			min = max;
			max = t;
		}

		// Now populate the set
		Set<Double> Numbers = new HashSet<>();
		long numbersAdded = 0;
		Double RandomNum;
		while (numbersAdded < n) {
			RandomNum = min + (Math.random() * ((max - min) + 1));
			if (Numbers.add(RandomNum)) {
				out.push(new DefaultRow("Row_" + (numbersAdded++), new DoubleCell(RandomNum)));
				exec.setProgress((double) numbersAdded / n,
						"Added " + numbersAdded + " of " + n + " rows");
			}
			exec.checkCanceled();
		}

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
	public static Collection<Double> getDoubles(Double min, Double max, final int n) {

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
	 * @{@link Deprecated} See
	 *         {@link #addDoubles(Double, Double, long, RowOutput, ExecutionContext)}
	 */
	public static void addDoubles(Double min, Double max, final long n, BufferedDataContainer dc,
			ExecutionContext exec) throws CanceledExecutionException {

		// Check the range is sensible
		if (min > max) {
			Double t = min;
			min = max;
			max = t;
		}

		// Now populate the set
		Double RandomNum;
		long numbersAdded = 0;
		while (numbersAdded < n) {
			RandomNum = min + (Math.random() * ((max - min) + 1));
			dc.addRowToTable(new DefaultRow("Row_" + (numbersAdded++), new DoubleCell(RandomNum)));
			exec.setProgress((double) numbersAdded / n,
					"Added " + numbersAdded + " of " + n + " rows");
			exec.checkCanceled();
		}
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
	public static void addDoubles(Double min, Double max, final long n, RowOutput out,
			ExecutionContext exec) throws CanceledExecutionException, InterruptedException {

		// Check the range is sensible
		if (min > max) {
			Double t = min;
			min = max;
			max = t;
		}

		// Now populate the set
		Double RandomNum;
		long numbersAdded = 0;
		while (numbersAdded < n) {
			RandomNum = min + (Math.random() * ((max - min) + 1));
			out.push(new DefaultRow("Row_" + (numbersAdded++), new DoubleCell(RandomNum)));
			exec.setProgress((double) numbersAdded / n,
					"Added " + numbersAdded + " of " + n + " rows");
			exec.checkCanceled();
		}
	}
}