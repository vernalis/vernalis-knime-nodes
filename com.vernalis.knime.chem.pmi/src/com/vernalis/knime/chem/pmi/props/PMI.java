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
package com.vernalis.knime.chem.pmi.props;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.vernalis.knime.chem.util.points.AbstractPoint;
import com.vernalis.knime.util.math.Cubic;

/**
 * A simple container class for principle moments of inertia (PMIs)
 * 
 * @author s.roughley
 *
 */
public class PMI {

	private final double[] pmis;
	private final Double MWt;

	/**
	 * @param pmis
	 *            A list containing 1-3 PMIs. When the list has less than 3
	 *            PMIs, the maximum value is duplicated until all 3 are
	 *            populated
	 */
	public PMI(List<Double> pmis) {
		this(pmis, null);
	}

	public PMI(List<Double> pmis, Double MWt) {
		if (pmis.isEmpty() || pmis.size() > 3) {
			throw new IllegalArgumentException("Must be 1-3 PMIs");
		}
		if (pmis.size() < 3) {
			double max = pmis.stream().mapToDouble(x -> x.doubleValue()).max()
					.getAsDouble();
			while (pmis.size() < 3) {
				pmis.add(max);
			}
		}
		this.pmis = new double[3];
		for (int i = 0; i < pmis.size(); ++i) {
			this.pmis[i] = pmis.get(i);

		}
		Arrays.sort(this.pmis);
		this.MWt = MWt;
	}

	public static PMI fromPoints(Collection<AbstractPoint<Double>> points) {
		AbstractPoint<Double> cOfGravity =
				AbstractPoint.getCentreOfGravity(points);
		double[][] inertialTensor =
				AbstractPoint.getInertialTensor(points, cOfGravity);
		Double a = -1.0 * (inertialTensor[0][0] + inertialTensor[1][1]
				+ inertialTensor[2][2]);
		Double b = inertialTensor[0][0] * inertialTensor[1][1]
				+ inertialTensor[0][0] * inertialTensor[2][2]
				+ inertialTensor[1][1] * inertialTensor[2][2]
				- inertialTensor[0][1] * inertialTensor[0][1]
				- inertialTensor[0][2] * inertialTensor[0][2]
				- inertialTensor[1][2] * inertialTensor[1][2];
		Double c = inertialTensor[0][0] * inertialTensor[1][2]
				* inertialTensor[1][2]
				+ inertialTensor[0][1] * inertialTensor[0][1]
						* inertialTensor[2][2]
				+ inertialTensor[0][2] * inertialTensor[0][2]
						* inertialTensor[1][1]
				- 2 * inertialTensor[0][1] * inertialTensor[1][2]
						* inertialTensor[0][2]
				- inertialTensor[0][0] * inertialTensor[1][1]
						* inertialTensor[2][2];
		Cubic cubic = new Cubic(a, b, c);
		return new PMI(cubic.getRoots(), cOfGravity.getProperty());
	}

	public double I1() {
		return pmis[0];
	}

	public double I2() {
		return pmis[1];
	}

	public double I3() {
		return pmis[2];
	}

	public Double MWt() {
		return MWt;
	}
}
