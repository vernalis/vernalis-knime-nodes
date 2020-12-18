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
 * values. Implementations should typically extend SettingsModel. A reference
 * base implementation is provided as {@link AbstractSettingsModelRangeBounded}.
 * 
 * This interface provides a default implementation of
 * {@link #compareValues(Comparable, Comparable)} using
 * {@link Comparable#compareTo(Object)}
 * 
 * @author S.Roughley knime@vernalis.com
 *
 * @param <T>
 *            The type of the value stored, which must implement
 *            {@link Comparable}
 * @see AbstractSettingsModelRangeBounded
 * 
 * @since 1.28.0
 */
public interface SettingsModelComparableRangeBounded<T extends Comparable<T>>
		extends SettingsModelRangeBounded<T> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * SettingsModelRangeBounded#compareValues(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	default int compareValues(T t0, T t1) {
		return t0.compareTo(t1);
	}

}
