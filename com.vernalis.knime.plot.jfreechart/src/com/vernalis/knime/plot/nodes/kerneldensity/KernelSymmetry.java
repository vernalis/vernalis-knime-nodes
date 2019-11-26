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
 * Enum offering options as to application of the Kernel Estimator in
 * multidimensional space
 * 
 * @author S.Roughley
 *
 */
public enum KernelSymmetry {
	/**
	 * Radial applies the {@link KernelEstimator} multiplicatively in the two
	 * coordinate directions, e.g.
	 * <p>
	 * kEst(U) = kEst(U<sub>x</sub>)•kEst(U<sub>y</sub>)
	 * </p>
	 */
	RADIAL_MULTIPLICATIVE {

		@Override
		public double getEstimate(double[] u, KernelEstimator kEst) {
			return Arrays.stream(u).map(ux -> kEst.getK(ux)).reduce(1.0,
					(a, b) -> a * b);

		}

		@Override
		public String getDescription() {
			return "The kernel estimator is applied multiplicatively across dimensions, "
					+ "e.g. K(u) = K(u(x)) \u2022 K(u(y)), where u(x) is the x-dimension "
					+ "component of u, and u(y) the y-dimension component";
		}
	},

	/**
	 * Spherical applies the {@link KernelEstimator} with 'spherical' symmetry
	 * around the kernel, e.g.
	 * <p>
	 * kEst(U) = kEst(U<sup>T</sup>•U) =
	 * kEst(U<sub>x</sub><sup>2</sup>+U<sub>y</sub> <sup>2</sup>)
	 * </p>
	 */
	SPHERICAL {

		@Override
		public double getEstimate(double[] u, KernelEstimator kEst) {
			double uTu = Math.sqrt(Arrays.stream(u).map(x -> x * x).sum());
			return kEst.getK(uTu);
		}

		@Override
		public String getDescription() {
			return "The kernel estimator is applied spherically symmetrically - "
					+ "i.e. any point of the same distance from the kernel "
					+ "estimator center has the same value. This is equivalent to K(u) =  K(\u221Au\u1D40u)";
		}
	};

	/**
	 * @return A description for the node description
	 */
	public abstract String getDescription();

	/**
	 * @return The names of all opitons
	 */
	public static String[] names() {
		return Arrays.stream(values()).map(x -> x.name())
				.toArray(size -> new String[size]);
	}

	/**
	 * @return The default
	 */
	public static KernelSymmetry getDefault() {
		return RADIAL_MULTIPLICATIVE;
	}

	/**
	 * @param u
	 *            The bandwidth-normalised vector e.g. [(xi-x0)/hx, (yi-y0)/hy]
	 * @param kEst
	 *            The {@link KernelEstimator}
	 * @return The combined kernel estimator
	 */
	public abstract double getEstimate(double[] u, KernelEstimator kEst);
}
