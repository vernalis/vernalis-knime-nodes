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

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.vernalis.knime.util.math.TransformUtil;

/**
 * This class contains a point in 3D cartesian space which also may have a
 * property associated with it. Methods are provided which also treat the point
 * as a coordinate vector
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The type of the property
 */
public class AbstractPoint<T> implements Point<AbstractPoint<T>> {

	private final double x, y, z;
	private final T property;

	/**
	 * Constructor
	 * 
	 * @param x
	 *            The x coordinate
	 * @param y
	 *            The y coordinate
	 * @param z
	 *            The z coordinate
	 * @param property
	 *            The property of the point
	 */
	public AbstractPoint(double x, double y, double z, T property) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.property = property;
	}

	/**
	 * @return The x coordinate
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return The y coordinate
	 */
	public double getY() {
		return y;
	}

	/**
	 * @return The z coordinate
	 */
	public double getZ() {
		return z;
	}

	/**
	 * @return The property
	 */
	public T getProperty() {
		return property;
	}

	/**
	 * Method to subtract the coordinates of the other point from this point.
	 * The type parameter and property are retained from this point
	 * 
	 * @param other
	 *            The other point
	 * @return The subtracted point (i.e. the vector from other to this)
	 */
	public <U> AbstractPoint<T> minus(AbstractPoint<U> other) {
		return minus(other, new BiFunction<T, U, T>() {

			@Override
			public T apply(T t, U u) {
				return t;
			}
		});
	}

	/**
	 * Method to subtract the coordinates of the other point from this point.
	 * The type parameter is retained from this point and the new property is
	 * calculated according to the supplied {@link BiFunction}
	 * 
	 * @param other
	 *            The other point
	 * @param propertyCombineFunction
	 *            The function to combine the properties of the two points
	 * @return The subtracted point (i.e. the vector from other to this)
	 */
	public <U> AbstractPoint<T> minus(AbstractPoint<U> other,
			BiFunction<T, U, T> propertyCombineFunction) {
		return new AbstractPoint<>(x - other.x, y - other.y, z - other.z,
				propertyCombineFunction.apply(property, other.property));
	}

	/**
	 * Method to subtract the coordinates of the other point from this point.
	 * The type parameter is the same for both points and the new property is
	 * calculated according to the supplied {@link BiFunction}
	 * 
	 * @param other
	 *            The other point
	 * @param propertyCombineFunction
	 *            The function to combine the properties of the two points
	 * @return The subtracted point (i.e. the vector from other to this)
	 */
	public AbstractPoint<T> minus(AbstractPoint<T> other,
			BinaryOperator<T> propertyCombineFunction) {
		return new AbstractPoint<>(x - other.x, y - other.y, z - other.z,
				propertyCombineFunction.apply(property, other.property));
	}

	/**
	 * Method to add the coordinates of the other point from this point. The
	 * type parameter and property are retained from this point
	 * 
	 * @param other
	 *            The other point
	 * @return The added point
	 */
	public <U> AbstractPoint<T> plus(AbstractPoint<U> other) {
		return plus(other, new BiFunction<T, U, T>() {

			@Override
			public T apply(T t, U u) {
				return t;
			}
		});
	}

	/**
	 * Method to add the coordinates of the other point from this point. The
	 * type parameter is retained from this point and the new property is
	 * calculated according to the supplied {@link BiFunction}
	 * 
	 * @param other
	 *            The other point
	 * @param propertyCombineFunction
	 *            The function to combine the properties of the two points
	 * @return The added point
	 */
	public AbstractPoint<T> plus(AbstractPoint<T> other,
			BinaryOperator<T> propertyCombineFunction) {
		return new AbstractPoint<>(x + other.x, y + other.y, z + other.z,
				propertyCombineFunction.apply(property, other.property));
	}

	/**
	 * Method to add the coordinates of the other point from this point. The
	 * type parameter is the same for both points and the new property is
	 * calculated according to the supplied {@link BiFunction}
	 * 
	 * @param other
	 *            The other point
	 * @param propertyCombineFunction
	 *            The function to combine the properties of the two points
	 * @return The added point
	 */
	public <U> AbstractPoint<T> plus(AbstractPoint<U> other,
			BiFunction<T, U, T> propertyCombineFunction) {
		return new AbstractPoint<>(x + other.x, y + other.y, z + other.z,
				propertyCombineFunction.apply(property, other.property));
	}

	/**
	 * Method to normalise the point (i.e. treat it as a vector and divide the
	 * coordinates such that it has magnitude of 1)
	 * 
	 * @param propertyFunction
	 *            The property function to determine the new property
	 * @return Normalised point
	 */
	public AbstractPoint<T> normalise(UnaryOperator<T> propertyFunction) {
		double mag = length();
		return divideBy(mag, propertyFunction);
	}

	/**
	 * @param divisor
	 *            Value to divide coordinates by
	 * @return A new point with the same property and coordinates divided by
	 *         divisor
	 */
	public AbstractPoint<T> divideBy(double divisor) {
		return divideBy(divisor, UnaryOperator.identity());
	}

	/**
	 * @param divisor
	 *            Value to divide coordinates by
	 * @param propertyFunction
	 *            Function to determine the behaviour of the property
	 * @return A new point with the coordinates divided by divisor and property
	 *         modified according to the supplied {@link UnaryOperator}
	 */
	public AbstractPoint<T> divideBy(double divisor,
			UnaryOperator<T> propertyFunction) {
		return new AbstractPoint<>(x / divisor, y / divisor, z / divisor,
				propertyFunction.apply(property));
	}

	/**
	 * @param multiplier
	 *            Value to multiply coordinates by
	 * @return A new point with the same property and coordinates multiplied by
	 *         multiplier
	 */
	public AbstractPoint<T> multiplyBy(double multiplier) {
		return multiplyBy(multiplier, UnaryOperator.identity());
	}

	/**
	 * @param multiplier
	 *            Value to multiply coordinates by
	 * @param propertyFunction
	 *            Function to determine the behaviour of the property
	 * @return A new point with the coordinates multiplied by multiplier and
	 *         property modified according to the supplied {@link UnaryOperator}
	 */
	public AbstractPoint<T> multiplyBy(double multiplier,
			UnaryOperator<T> propertyFunction) {
		return new AbstractPoint<>(x * multiplier, y * multiplier,
				z * multiplier, propertyFunction.apply(property));
	}

	/**
	 * @param propertyFunction
	 *            Function to convert existing property to a new property
	 * @return A new point with the same coordinates as the current point, and
	 *         new property
	 */
	public <U> AbstractPoint<U> mapProperty(Function<T, U> propertyFunction) {
		return new AbstractPoint<>(x, y, z, propertyFunction.apply(property));

	}

	/**
	 * {@link BinaryOperator} to add two double values
	 */
	private static final BinaryOperator<Double> SUM =
			new BinaryOperator<Double>() {

				@Override
				public Double apply(Double t, Double u) {
					return t.doubleValue() + u.doubleValue();
				}
			};

	/**
	 * @param points
	 *            The points to find the centre of gravity of. The point
	 *            properties are the weights
	 * @return The Centre of gravity
	 */
	public static AbstractPoint<Double> getCentreOfGravity(
			Collection<AbstractPoint<Double>> points) {
		AbstractPoint<Double> retVal = new AbstractPoint<>(0.0, 0.0, 0.0, 0.0);
		for (AbstractPoint<Double> point : points) {
			retVal = retVal.plus(point.multiplyBy(point.getProperty()), SUM);
		}
		retVal = retVal.divideBy(retVal.getProperty());
		return retVal;
	}

	/**
	 * @param points
	 *            The points to find the inertial tensor for. The point
	 *            properties are the weights
	 * @return The inertial tensor
	 */
	public static double[][] getInertialTensor(
			Collection<AbstractPoint<Double>> points) {
		return getInertialTensor(points, getCentreOfGravity(points));
	}

	/**
	 * @param points
	 *            The points to find the inertial tensor for. The point
	 *            properties are the weights
	 * @param centreOfGravity
	 *            The centre of gravity of the points
	 * @return The inertial tensor
	 */
	public static <T> double[][] getInertialTensor(
			Collection<AbstractPoint<Double>> points,
			AbstractPoint<T> centreOfGravity) {
		List<AbstractPoint<Double>> centredPoints =
				points.stream().map(pt -> pt.minus(centreOfGravity))
						.collect(Collectors.toList());
		double Ixx = 0, Iyy = 0, Izz = 0, Ixy = 0, Ixz = 0, Iyz = 0;
		for (AbstractPoint<Double> point : centredPoints) {
			Double atomMass = point.property;
			Ixx += atomMass * (point.y * point.y + point.z * point.z);
			Iyy += atomMass * (point.x * point.x + point.z * point.z);
			Izz += atomMass * (point.x * point.x + point.y * point.y);
			Ixy -= atomMass * point.x * point.y;
			Ixz -= atomMass * point.x * point.z;
			Iyz -= atomMass * point.y * point.z;
		}
		return new double[][] { { Ixx, Ixy, Ixz }, { Ixy, Iyy, Iyz },
				{ Ixz, Iyz, Izz } };
	}

	/**
	 * @param points
	 *            The points to align to the principle axes
	 * @return The aligned points
	 */
	public static List<AbstractPoint<Double>> alignToPrincipleAxes(
			Collection<AbstractPoint<Double>> points) {
		return alignToPrincipleAxes(points, getCentreOfGravity(points));
	}

	/**
	 * @param points
	 *            The points to align to the principle axes
	 * @param centreOfGravity
	 *            The centre of gravity
	 * @return The aligned points
	 */
	public static <T> List<AbstractPoint<Double>> alignToPrincipleAxes(
			Collection<AbstractPoint<Double>> points,
			AbstractPoint<T> centreOfGravity) {

		return points.stream()
				.map(pt -> pt.minus(centreOfGravity)
						.transform(TransformUtil.makeRotationMatrix(
								getInertialTensor(points, centreOfGravity))))
				.collect(Collectors.toList());
	}

	/**
	 * @param rotMatrix
	 *            A rotation matrix
	 * @return the transformed point, transformed according to the rotation
	 *         matrix. The point property is unchanged
	 */
	public AbstractPoint<T> transform(double[][] rotMatrix) {
		return transform(rotMatrix, UnaryOperator.identity());
	}

	/**
	 * @param rotMatrix
	 *            A rotation matrix
	 * @param propertyFunction
	 *            Function to govern how the point property is modified
	 * @return The transformed point according to the rotation matrix
	 */
	public AbstractPoint<T> transform(double[][] rotMatrix,
			UnaryOperator<T> propertyFunction) {
		return new AbstractPoint<>(
				rotMatrix[0][0] * x + rotMatrix[0][1] * y + rotMatrix[0][2] * z,
				rotMatrix[1][0] * x + rotMatrix[1][1] * y + rotMatrix[1][2] * z,
				rotMatrix[2][0] * x + rotMatrix[2][1] * y + rotMatrix[2][2] * z,
				propertyFunction.apply(property));
	}

	/**
	 * @param rotMatrix
	 *            A rotation matrix in single array form
	 * @return the transformed point, transformed according to the rotation
	 *         matrix. The point property is unchanged
	 */
	public AbstractPoint<T> transform(double[] rotMatrix) {
		return transform(rotMatrix, UnaryOperator.identity());
	}

	/**
	 * @param rotMatrix
	 *            A rotation matrix in single array form
	 * @param propertyFunction
	 *            Function to govern how the point property is modified
	 * @return the transformed point, transformed according to the rotation
	 *         matrix. The point property is unchanged
	 */
	public AbstractPoint<T> transform(double[] rotMatrix,
			UnaryOperator<T> propertyFunction) {
		return new AbstractPoint<>(
				rotMatrix[0] * x + rotMatrix[1] * y + rotMatrix[2] * z,
				rotMatrix[3] * x + rotMatrix[4] * y + rotMatrix[5] * z,
				rotMatrix[6] * x + rotMatrix[7] * y + rotMatrix[8] * z,
				propertyFunction.apply(property));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AbstractPoint [x=");
		builder.append(x);
		builder.append(", y=");
		builder.append(y);
		builder.append(", z=");
		builder.append(z);
		builder.append(", ");
		if (property != null) {
			builder.append("property=");
			builder.append(property);
		}
		builder.append("]");
		return builder.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.chem.util.points.Point#getDistance(com.
	 * vernalis.knime.internal.chem.util.points.Point)
	 */
	@Override
	public double getDistance(AbstractPoint<T> o) {
		double delta = (getX() - o.getX()) * (getX() - o.getX());
		delta += (getY() - o.getY()) * (getY() - o.getY());
		delta += (getZ() - o.getZ()) * (getZ() - o.getZ());
		return Math.sqrt(delta);
	}

	/**
	 * @return A {@link SimplePoint} with the same coordinates as the current
	 *         point
	 */
	public SimplePoint toSimplePoint() {
		return new SimplePoint(x, y, z);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.chem.util.points.Point#getNumDimensions()
	 */
	@Override
	public int getNumDimensions() {
		return 3;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.chem.util.points.Point#getCoordinate(int)
	 */
	@Override
	public double getCoordinate(int dim) {
		switch (dim) {
		case 0:
			return getX();
		case 1:
			return getY();
		case 2:
			return getZ();
		default:
			throw new IllegalArgumentException("Dim must be in range 0-2");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.chem.util.points.Point#getCoordinates()
	 */
	@Override
	public double[] getCoordinates() {
		return new double[] { getX(), getY(), getZ() };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.chem.util.points.Point#normalise()
	 */
	@Override
	public AbstractPoint<T> normalise() {
		double l = length();
		return new AbstractPoint<>(getX() / l, getY() / l, getZ() / l,
				property);
	}

}
