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

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * An enum for the various plugin bandwidth estimation methods
 * 
 * @author s.roughley
 *
 */
public enum BandwidthEstimationMethod implements ButtonGroupEnumInterface {

	Silverman {

		@Override
		public String getToolTip() {
			return "Bandwidth is estimated using the Silverman approximation "
					+ "(H = stdDev * [4 / ((d + 2) * n)]^(1 / (d + 4)), "
					+ "where d is thenumber of dimensions and n the number of datapoints)";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.vernalis.knime.internal.pmi.util.kerneldensity.
		 * BandwidthEstimationMethod#calculate(int, double, int)
		 */
		@Override
		public double calculate(int numDimensions, double stdDev, int numPoints)
				throws UnsupportedOperationException {
			double c = Math.pow(4.0 / (numDimensions + 2.0),
					1.0 / (numDimensions + 4.0));
			return c * Scott.calculate(numDimensions, stdDev, numPoints);
		}
	},
	Scott {

		@Override
		public String getToolTip() {
			return "Bandwidth is estimated using the Scott approximation "
					+ "(H = stdDev / n^(1 / (d + 4)), "
					+ "where d is thenumber of dimensions and n the number of datapoints)";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.vernalis.knime.internal.pmi.util.kerneldensity.
		 * BandwidthEstimationMethod#calculate(int, double, int)
		 */
		@Override
		public double calculate(int numDimensions, double stdDev, int numPoints)
				throws UnsupportedOperationException {
			return stdDev * Math.pow(numPoints, -1.0 / (numDimensions + 4.0));
		}
	},

	User_Defined {

		@Override
		public String getToolTip() {
			return "The user specifies the bandwidth (H)";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.vernalis.knime.internal.pmi.util.kerneldensity.
		 * BandwidthEstimationMethod#needsManualValue()
		 */
		@Override
		public boolean needsManualValue() {
			return true;
		}
	};

	/**
	 * @param numDimensions
	 *            The number of dimensions
	 * @param stdDev
	 *            The standard deviation
	 * @param numPoints
	 *            The number of data points
	 * @return The bandwidth estimation
	 * @throws UnsupportedOperationException
	 */
	public double calculate(int numDimensions, double stdDev, int numPoints)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"Calculation not supported for " + name());
	}

	@Override
	public String getText() {
		return name().replace("_", " ");
	}

	@Override
	public String getActionCommand() {
		return name();
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	/**
	 * @return {@code true} if the method needs a manual value of bandwidth
	 *         supplied
	 */
	public boolean needsManualValue() {
		return false;
	}

	/**
	 * @return The default method
	 */
	public static BandwidthEstimationMethod getDefault() {
		return Silverman;
	}
}
