/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.dialogcomponents;

/**
 * An interface defining the methods needed for a settings model defining a
 * bounded range - ie a range with limits on the possible upper and lower
 * values. Implementations should typically extend SettingsModel.
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 * @param <T>
 *            The type of the value stored. If {@code T} implements Comparable,
 *            then the related {@link SettingsModelComparableRangeBounded}
 *            interface should be used
 * @see SettingsModelComparableRangeBounded
 * @see AbstractSettingsModelRangeBounded
 */
public interface SettingsModelRangeBounded<T> {

	/**
	 * Set the enabled status of the model
	 * 
	 * @param b
	 *            The enabled value
	 */
	void setEnabled(boolean b);

	/**
	 * @return The upper bound value
	 */
	T getUpperBound();

	/**
	 * Optional method to set the upper bound. By default this method throws
	 * {@link UnsupportedOperationException}. Implementations allowing this
	 * operation should ensure that an invalidated range does not result
	 * 
	 * @param upperBound
	 *            The new upper bound value
	 */
	public default void setUpperBound(T upperBound) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return The lower bound value
	 */
	T getLowerBound();

	/**
	 * Optional method to set the lower bound. By default this method throws
	 * {@link UnsupportedOperationException}. Implementations allowing this
	 * operation should ensure that an invalidated range does not result
	 * 
	 * @param lowerBound
	 *            The new lower bound value
	 */
	public default void setLowerBound(T lowerBound) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return The lower value of the actual range
	 */
	T getLowerValue();

	/**
	 * Method to set the lower value of the range. Throws
	 * {@link IllegalArgumentException} if the value is not between the upper
	 * value and the lower bound
	 * 
	 * @param lowerVal
	 *            The new lower value
	 * @see #setRange(Object, Object)
	 */
	public default void setLowerValue(T lowerVal) {
		setRange(lowerVal, getUpperValue());
	}

	/**
	 * @return The upper value of the actual range
	 */
	T getUpperValue();

	/**
	 * Method to set the upper value of the range. Throws
	 * {@link IllegalArgumentException} if the value is not between the lower
	 * value and the upper bound
	 * 
	 * @param upperVal
	 *            The new lower value
	 * @see #setRange(Object, Object)
	 */
	public default void setUpperValue(T upperVal) {
		setRange(getLowerValue(), upperVal);
	}

	/**
	 * Method to to set the range. The method should user
	 * {@link #validateRange(Object, Object)} to ensure a valid range is being
	 * set
	 * 
	 * @param lowerVal
	 *            The new lower value
	 * @param upperVal
	 *            The new upper value
	 */
	void setRange(T lowerVal, T upperVal);

	/**
	 * Method to validate the range supplied. The values should be in the order
	 * lower bound <= lower value <(=*) upper value <= upper bound. *Equal
	 * values may be allowed according to the result of
	 * {@link #allowEqualUpperLower()}. The default implementation validates
	 * each value and then the actual range
	 * 
	 * @param lowerVal
	 *            The lower value of the range
	 * @param upperVal
	 *            The upper value of the range
	 * @throws IllegalArgumentException
	 *             if the range is not valid
	 * @see #validateValue(Object)
	 */
	public default void validateRange(T lowerVal, T upperVal) {
		validateValue(lowerVal);
		validateValue(upperVal);
		final int comp = compareValues(lowerVal, upperVal);
		if (comp > 0 || !allowEqualUpperLower() && comp == 0) {
			throw new IllegalArgumentException(
					lowerVal.toString() + " must be less than "
							+ (allowEqualUpperLower() ? "or equal to " : "")
							+ upperVal.toString());
		}
	}

	/**
	 * Method to validate a value. In this case, the check is whether the value
	 * is within the specified bounds as it is unknown whether this is an upper
	 * or lower value
	 * 
	 * @param val
	 *            The value to check
	 * @throws IllegalArgumentException
	 *             if the value is outside the allowed bounds
	 * @see #validateRange(Object, Object)
	 */
	public default void validateValue(T val) {
		int comp = compareValues(getLowerBound(), val);
		if (comp > 0) {
			throw new IllegalArgumentException(val + " must be in range "
					+ getLowerBound() + " ... " + getUpperBound());
		}
		comp = compareValues(val, getUpperBound());
		if (comp > 0 || !allowEqualUpperLower() && comp == 0) {
			throw new IllegalArgumentException(val + " must be in range "
					+ getLowerBound() + " ... " + getUpperBound());
		}
	}

	/**
	 * @return Whether the range is allowed to have the same upper and lower
	 *         value
	 */
	boolean allowEqualUpperLower();

	/**
	 * Method to set whether the range is allowed to have the same upper and
	 * lower value
	 * 
	 * @param allowEqualUpperLower
	 *            The value to set
	 */
	void setAllowEqualUpperLower(boolean allowEqualUpperLower);

	/**
	 * @return Whether the model is enabled
	 */
	boolean isEnabled();

	/**
	 * @return The config key of the model
	 */
	String getConfigKey();

	/**
	 * Method to compare a pair of values. Should return a value <0 when t0 <
	 * t1, 0 then t0 and t1 are equal and >0 when t0 > t1
	 * 
	 * @param t0
	 *            The first value to compare
	 * @param t1
	 *            The second value to compare
	 * @return The result of the comparison as described above
	 */
	public int compareValues(T t0, T t1);

}
