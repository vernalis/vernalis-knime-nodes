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
package com.vernalis.knime.plot.nodes.kerneldensity;

import java.util.Arrays;

/**
 * Enum for the types of contour interval schema
 * 
 * @author s.roughley
 *
 */
public enum ContourIntervalSchema {
	LINEAR {

		@Override
		public double[] getContourIntervals(double min, double max,
				int numberOfIntervals) {
			double[] retVal = new double[numberOfIntervals];
			double stepSize = (max - min) / numberOfIntervals;
			double level = min;
			for (int i = 0; i < numberOfIntervals; i++) {
				retVal[i] = level;
				level += stepSize;
			}
			return retVal;
		}
	},
	/*
	 * LOGARITHMIC {
	 * 
	 * @Override public double[] getContourIntervals(double min, double max, int
	 * numberOfIntervals) { double[] retVal = new double[numberOfIntervals];
	 * double stepSize = Math.log(max - min) / (numberOfIntervals + 1.0); double
	 * level = min; for (int i = 0; i < numberOfIntervals; i++) { retVal[i] =
	 * Math.exp(level); level += stepSize; } return retVal; } }, SQUARE {
	 * 
	 * @Override public double[] getContourIntervals(double min, double max, int
	 * numberOfIntervals) { double[] retVal = new double[numberOfIntervals];
	 * double stepSize = Math.sqrt(max - min) / numberOfIntervals; double level
	 * = min; for (int i = 0; i < numberOfIntervals; i++) { retVal[i] = level *
	 * level; level += stepSize; } return retVal; } }, CUBIC {
	 * 
	 * @Override public double[] getContourIntervals(double min, double max, int
	 * numberOfIntervals) { double[] retVal = new double[numberOfIntervals];
	 * double stepSize = Math.cbrt(max - min) / numberOfIntervals; double level
	 * = min; for (int i = 0; i < numberOfIntervals; i++) { retVal[i] = level *
	 * level * level; level += stepSize; } return retVal; } }, QUARTIC {
	 * 
	 * @Override public double[] getContourIntervals(double min, double max, int
	 * numberOfIntervals) { double[] retVal = new double[numberOfIntervals];
	 * double stepSize = Math.pow((max - min), 0.25) / numberOfIntervals; double
	 * level = min; for (int i = 0; i < numberOfIntervals; i++) { retVal[i] =
	 * level * level * level * level; level += stepSize; } return retVal; } },
	 */
	QUANTILE {

		@Override
		public double[] getContourIntervals(double min, double max,
				int numberOfIntervals) {
			// This method needs to figure out from the Kernel Density object
			// directly
			return null;
		}

	};

	/**
	 * @return The names of all options
	 */
	public static String[] names() {
		return Arrays.stream(values()).map(x -> x.name())
				.toArray(size -> new String[size]);
	}

	/**
	 * @return the default method
	 */
	public static ContourIntervalSchema getDefault() {
		return LINEAR;
	}

	/**
	 * Method to calculate an array of contour values between two value with a
	 * given number of intervals
	 * 
	 * @param min
	 *            The minimum value
	 * @param max
	 *            The maximum value
	 * @param numberOfIntervals
	 *            The number of intervals
	 * @return The contour levels
	 */
	public abstract double[] getContourIntervals(double min, double max,
			int numberOfIntervals);
}
