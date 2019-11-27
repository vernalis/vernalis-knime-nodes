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
package com.vernalis.knime.misc;

import java.util.DoubleSummaryStatistics;

/**
 * An enhanced {@link DoubleSummaryStatistics} which tracks the sum-of-squares,
 * allowing calculation of standard deviation of inputs. Sum of squares if
 * calculated using the Kahan sumation algorithm, analogously to the calculation
 * of sum in {@link DoubleSummaryStatistics}
 * 
 * @author s.roughley
 *
 */
public class DoubleSummary extends DoubleSummaryStatistics {

	private double sumOfSquares;
	private double sumOfSquaresCompensation; // Low order bits of sum - see
												// https://en.wikipedia.org/wiki/Kahan_summation_algorithm
	private double simpleSumOfSquares; // Used to compute right sum for
										// non-finite inputs

	@Override
	public void accept(double value) {
		super.accept(value);
		double squareVal = value * value;
		simpleSumOfSquares += squareVal;
		squareSumWithCompensation(squareVal);
	}

	private void squareSumWithCompensation(double value) {
		double tmp = value - sumOfSquaresCompensation;
		double velvel = sumOfSquares + tmp; // Little wolf of rounding error
		sumOfSquaresCompensation = (velvel - sumOfSquares) - tmp;
		sumOfSquares = velvel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.DoubleSummaryStatistics#combine(java.util.
	 * DoubleSummaryStatistics)
	 */

	public void combine(DoubleSummary other) {
		super.combine(other);
		simpleSumOfSquares += other.simpleSumOfSquares;
		squareSumWithCompensation(other.sumOfSquares);
		squareSumWithCompensation(other.sumOfSquaresCompensation);
	}

	/**
	 * @return The sum of the squares
	 */
	public final double getSumOfSquares() {
		// Better error bounds to add both terms as the final sum
		double tmp = sumOfSquares + sumOfSquaresCompensation;
		if (Double.isNaN(tmp) && Double.isInfinite(sumOfSquares))
			// If the compensated sum is spuriously NaN from
			// accumulating one or more same-signed infinite values,
			// return the correctly-signed infinity stored in
			// sumOfSquares.
			return sumOfSquares;
		else
			return tmp;
	}

	/**
	 * @return The standard deviation
	 */
	public final double getStandardDeviation() {
		return getCount() == 0 ? 0.0
				: Math.sqrt(getSumOfSquares() / getCount()
						- getAverage() * getAverage());
	}

}
