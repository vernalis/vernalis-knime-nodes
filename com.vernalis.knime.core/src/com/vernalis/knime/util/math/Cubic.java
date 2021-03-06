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
package com.vernalis.knime.util.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class representing a cubic equation in the form x^3 + a * x^2 + b* x + c = 0
 * Real roots are returned in a sorted set; Algorithm is from
 * <a href="http://mathworld.wolfram.com/CubicFormula.html">
 * http://mathworld.wolfram.com/CubicFormula.html</a>
 * 
 * @author s.roughley
 *
 */
public class Cubic {

	private static final double FOUR_PI = 4.0 * Math.PI;
	private static final double TWO_PI = 2.0 * Math.PI;
	private double a; // coefficient of x^2
	private double b; // coefficient of x
	private double c; // constant

	private static final double precision = 1e-5;

	public Cubic() {
	}

	/**
	 * Constructor for x^3 + ax^2 + bx + c = 0
	 * 
	 * @param a
	 *            Coefficient of x^2
	 * @param b
	 *            Coefficient of x
	 * @param c
	 *            Constant
	 */
	public Cubic(double a, double b, double c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	/**
	 * Constructor to allow ax^3 + bx^2 + cx + d = 0
	 * 
	 * @param a
	 *            Coefficient of x^3
	 * @param b
	 *            Coefficient of x^2
	 * @param c
	 *            Coefficient of x
	 * @param d
	 *            Constant
	 */
	public Cubic(double a, double b, double c, double d) {
		// a must be non-zero
		if (a == 0.0) {
			throw new IllegalArgumentException("a=0.0 is not a cubic equation!");
		}
		this.a = b / a;
		this.b = c / a;
		this.c = d / a;
	}

	/**
	 * Setter method to set the coefficients in form ax^3 + bx^2 + cx + d = 0
	 * 
	 * @param a
	 *            Coefficient of x^3
	 * @param b
	 *            Coefficient of x^2
	 * @param c
	 *            Coefficient of x
	 * @param d
	 *            Constant
	 */
	public void setCoefficients(double a, double b, double c, double d) {
		// a must be non-zero
		if (a == 0.0) {
			throw new IllegalArgumentException("a=0.0 is not a cubic equation!");
		}
		this.a = b / a;
		this.b = c / a;
		this.c = d / a;
	}

	/**
	 * Setter method to set the coefficients in form x^3 + ax^2 + bx + c = 0
	 * 
	 * @param a
	 *            Coefficient of x^2
	 * @param b
	 *            Coefficient of x
	 * @param c
	 *            Constant
	 */
	public void setCoefficients(double a, double b, double c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	/**
	 * Method to return the real Root(s) of the cubic.
	 * 
	 * @return Returns the solutions in a set, sorted high to low
	 */
	public List<Double> getRoots() {
		List<Double> roots = new ArrayList<>();
		double Q = ((3.0 * b) - (a * a)) / 9.0;
		double R = ((9.0 * a * b) - (27 * c) - (2 * a * a * a)) / 54.0;
		double D = (Q * Q * Q) + (R * R);
		int comp = compareWithTolerance(D, 0.0);
		final double aOver3 = a / 3.0;
		if (comp > 0 /* D > 0.0 */) {
			// 1 real root
			double S = Math.cbrt(R + Math.sqrt(D));
			double T = Math.cbrt(R - Math.sqrt(D));
			roots.add(S + T - aOver3);
		} else if (comp == 0 /* D == 0.0 */) {
			// 3 real roots, at least 2 are equal
			// NB This is the result of substituting D=0 into equations for 3
			// roots
			// z1 = -a/3+S+T
			// z2 = z3 = -a/3-(S+T)/2 - the imaginary component is S-T, but S
			// and T are degenerate
			// Previously z2 and z3 where erroneously +Math.cbrt(R) rather than
			// -Math.cbrt(R)
			double S = Math.cbrt(R);
			roots.add((2.0 * S) - aOver3);
			roots.add(-S - aOver3);
			roots.add(-S - aOver3);
		} else {
			// D<0.0
			// 3 different real roots
			final double theta = Math.acos(R / Math.sqrt(-1.0 * Q * Q * Q));
			final double twoRootMinusQ = 2.0 * Math.sqrt(-1.0 * Q);
			roots.add((twoRootMinusQ * Math.cos(theta / 3.0)) - aOver3);
			roots.add((twoRootMinusQ * Math.cos((theta + TWO_PI) / 3.0)) - aOver3);
			roots.add((twoRootMinusQ * Math.cos((theta + FOUR_PI) / 3.0)) - aOver3);
		}
		// Sort High-to-low
		Collections.sort(roots, Collections.reverseOrder());
		return roots;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Cubic [x^3 + %.3fx^2 + %.3fx + %.3f = 0]", a, b, c);
	}

	/**
	 * Method to perform double comparison with a tolerance.
	 * 
	 * @param a
	 *            First double
	 * @param b
	 *            Second double
	 * @return 0 if a and b differ by less than {@link #precision}, otherwise
	 *         the result of {@link Double#compare(double, double)}
	 */
	private int compareWithTolerance(double a, double b) {
		if (Math.abs(a - b) < precision) {
			return 0;
		}
		return Double.compare(a, b);
	}
}
