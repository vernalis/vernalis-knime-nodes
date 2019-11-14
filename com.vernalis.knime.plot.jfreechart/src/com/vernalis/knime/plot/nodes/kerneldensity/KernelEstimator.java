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
 * This Enum provides a set of kernel density estimators. Taken from
 * http://homepages.inf.ed.ac.uk/rbf/CVonline/LOCAL_COPIES/AV0405/MISHRA/kde.
 * html and from https://en.wikipedia.org/wiki/Kernel_(statistics)
 * 
 * 
 * @author S.Roughley
 *
 */
public enum KernelEstimator {
	UNIFORM("K(u) = 0.5 (|u| \u2264 1), 0 (|u) > 1); aka 'Uniform' or 'Boxcar'") {

		@Override
		public double getK(double u) {
			if (Math.abs(u) <= 1) {
				return 0.5;
			}
			return 0;
		}
	},

	TRIANGLE("K(u) = 1-|u| (|u| \u2264 1), 0 (|u) > 1)") {

		@Override
		public double getK(double u) {
			if (Math.abs(u) <= 1) {
				return 1 - Math.abs(u);
			}
			return 0;
		}
	},

	EPANECHNIKOV("K(u) = 3\u2022(1-u\u00B2)/4 (|u| \u2264 1), 0 (|u) > 1)") {

		@Override
		public double getK(double u) {
			if (Math.abs(u) <= 1) {
				return 0.75 * (1.0 - u * u);
			}
			return 0;
		}
	},

	QUARTIC("K(u) = 15\u2022(1-u\u00B2)\u00B2/16 (|u| \u2264 1), 0 (|u) > 1)") {

		@Override
		public double getK(double u) {
			if (Math.abs(u) <= 1) {
				return 15.0 * (1.0 - u * u) * (1.0 - u * u) / 16.0;
			}
			return 0;
		}
	},

	TRIWEIGHT(
			"K(u) = 35\u2022(1-u\u00B2)\u00B3/32 (|u| \u2264 1), 0 (|u) > 1)") {

		@Override
		public double getK(double u) {
			if (Math.abs(u) <= 1) {
				return 35.0 * (1.0 - u * u) * (1.0 - u * u) * (1.0 - u * u)
						/ 32.0;
			}
			return 0;
		}
	},

	TRICUBE("K(u) = 70\u2022(1-|u|\u00B3)\u00B3/81 (|u| \u2264 1), 0 (|u) > 1)") {

		@Override
		public double getK(double x) {
			double u = Math.abs(x);
			if (u <= 1) {
				return 70 * (1 - u * u * u) * (1 - u * u * u) * (1 - u * u * u)
						/ 81;
			}
			return 0;
		}

	},

	GAUSSIAN("K(u) = e^(-u\u00B2/2) / \u221A(2\u03c0)") {

		@Override
		public double getK(double u) {
			return Math.exp(-u * u / 2.0) / Math.sqrt(2.0 * Math.PI);
		}
	},

	COSINUS("K(u) = (\u03c0/4)\u2022cos(\u03c0u/2) (|u| \u2264 1), 0 (|u) > 1)") {

		@Override
		public double getK(double u) {
			if (Math.abs(u) <= 1) {
				return Math.PI * Math.cos(Math.PI * u / 2.0) / 4.0;
			}
			return 0;
		}
	},

	LOGISTIC("K(u) = 1/(e^u + 2 + e^-u)") {

		@Override
		public double getK(double u) {
			return 1.0 / (Math.exp(u) + 2 + Math.exp(-u));
		}

	},
	SIGMOID("K(u) = 2/(\u03c0\u2022(e^u + e^-u))") {

		@Override
		public double getK(double u) {
			return 2.0 / (Math.PI * (Math.exp(u) + Math.exp(-u)));
		}
	},
	SILVERMAN(
			"K(u) = 0.5\u2022e^(-|u|/\u221A2)\u2022sin((|u|/\u221A2) + (\u03c0/4))") {

		@Override
		public double getK(double x) {
			final double u = Math.abs(x);
			final double root2 = Math.sqrt(2);
			return 0.5 * Math.exp(-u / root2)
					* Math.sin(u / root2 + Math.PI / 4);
		}
	};

	private final String desc;

	private KernelEstimator(String desc) {
		this.desc = desc;
	}

	/**
	 * @param u
	 *            The bandwidth-normalised distance from the datapoint (i.e.
	 *            (x-x0)/h)
	 * @return The kernel intensity
	 */
	public abstract double getK(double u);

	/**
	 * @return A short description for the node description pane
	 */
	public String getDescription() {
		return desc;
	}

	/**
	 * @return The names of all options
	 */
	public static String[] names() {
		return Arrays.stream(values()).map(x -> x.name())
				.toArray(size -> new String[size]);
	}

	/**
	 * @return the default value
	 */
	public static KernelEstimator getDefault() {
		return GAUSSIAN;
	}
}
