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
package com.vernalis.knime.chem.util.points;

import com.vernalis.knime.dist.Measurable;

/**
 * Interface defining a point which also stores a related property
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The type of the property
 */
public interface Point<T extends Point<T>>
		extends Measurable<T>, Comparable<T> {

	/**
	 * @return The number of dimensions of the point
	 */
	public int getNumDimensions();

	/**
	 * @param dim
	 *            The dimension
	 * @return The coordinate for the dimension
	 */
	public double getCoordinate(int dim);

	/**
	 * @return an array of the point coordinates
	 */
	double[] getCoordinates();

	/**
	 * Treating two points as a pair of vectors, calculate the dot product
	 * between this and o
	 * 
	 * @param o
	 *            The other point
	 * @return The dot product
	 */
	default double dotProduct(Point<?> o) {
		if (getNumDimensions() != o.getNumDimensions()) {
			throw new IllegalArgumentException(
					"Differing number of vector dimensions");
		}
		double retVal = 0.0;
		for (int d = 0; d < getNumDimensions(); d++) {
			retVal += getCoordinate(d) * o.getCoordinate(d);
		}
		return retVal;
	}

	/**
	 * @param o
	 *            The second point
	 * @return An array of the differences of corresponding coordinates, e.g.
	 *         [x-xo,y-yo,z-zo]
	 */
	default double[] getCoordinateDifference(Point<?> o) {
		if (getNumDimensions() != o.getNumDimensions()) {
			throw new IllegalArgumentException(
					"Differing number of vector dimensions");
		}
		double[] retVal = new double[getNumDimensions()];
		for (int d = 0; d < getNumDimensions(); d++) {
			retVal[d] = o.getCoordinate(d) - getCoordinate(d);
		}
		return retVal;
	}

	/**
	 * @return A copy of the point (treated as a vector) with a magnitude of 1.0
	 *         (i.e. distance to the origin is 1.0)
	 */
	T normalise();

	/**
	 * @return The squared length of the vector
	 */
	default double squaredLength() {
		double retVal = 0.0;
		for (int d = 0; d < getNumDimensions(); d++) {
			retVal += getCoordinate(d) * getCoordinate(d);
		}
		return retVal;
	}

	/**
	 * @return The length (or magnitude) of the vector
	 */
	default double length() {
		return Math.sqrt(squaredLength());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.dist.Measurable#getDistance(java.lang.Object)
	 */
	@Override
	public default double getDistance(T o) {
		double dist = 0.0;
		for (int d = 0; d < getNumDimensions(); d++) {
			double delta = getCoordinate(d) - o.getCoordinate(d);
			dist += delta * delta;
		}
		return Math.sqrt(dist);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	default int compareTo(T o) {
		if (getNumDimensions() != o.getNumDimensions()) {
			throw new IllegalArgumentException(
					"Differing number of vector dimensions");
		}
		int retVal = 0;
		for (int d = 0; d < getNumDimensions(); d++) {
			retVal = Double.compare(getCoordinate(d), o.getCoordinate(d));
			if (retVal != 0) {
				break;
			}
		}
		return retVal;
	}

}
