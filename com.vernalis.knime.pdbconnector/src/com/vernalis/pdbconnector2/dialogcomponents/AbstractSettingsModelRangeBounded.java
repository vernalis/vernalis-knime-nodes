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

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObjectSpec;

/**
 * An abstract base implementaion class {@link SettingsModel} for the
 * {@link SettingsModelComparableRangeBounded} interface. Implementing classes
 * must implement the {@link #readValue(NodeSettingsRO, String)} and
 * {@link #writeValue(NodeSettingsWO, String, Comparable)} methods
 * 
 * @author S.Roughley knime@vernalis.com
 *
 * @param <T>
 *            The type of the value to store
 * @since 1.28.0
 */
public abstract class AbstractSettingsModelRangeBounded<T extends Comparable<T>>
		extends SettingsModel
		implements SettingsModelComparableRangeBounded<T> {

	private static final String ALLOW_EQUAL_MAXMIN_KEY = "ALLOW_EQUAL_MAXMIN";
	private static final String MAX_KEY = "MAX";
	private static final String MIN_KEY = "MIN";
	private final String configKey;
	private final T minBound, maxBound;
	private T min, max;
	private boolean allowEqualUpperLower = true;

	/**
	 * Constructor
	 * 
	 * @param configKey
	 *            The settings config key
	 * @param minRange
	 *            The initial minimum value
	 * @param maxRange
	 *            The initial maximum value
	 * @param lowerBound
	 *            The lower bound value
	 * @param upperBound
	 *            The upper bound value
	 */
	public AbstractSettingsModelRangeBounded(String configKey, T minRange,
			T maxRange, T lowerBound, T upperBound) {
		super();
		this.configKey = configKey;
		this.minBound = lowerBound;
		this.maxBound = upperBound;
		this.min = minRange;
		this.max = maxRange;
	}

	@Override
	public String getConfigKey() {
		return configKey;
	}

	@Override
	protected String getConfigName() {
		return getConfigKey();
	}

	@Override
	protected void loadSettingsForDialog(NodeSettingsRO settings,
			PortObjectSpec[] specs) throws NotConfigurableException {
		try {
			loadSettingsForModel(settings);
		} catch (final Exception e) {
			// Ignore the exceptions in the dialog
		}

	}

	@Override
	protected void saveSettingsForDialog(NodeSettingsWO settings)
			throws InvalidSettingsException {
		saveSettingsForModel(settings);

	}

	@Override
	protected void validateSettingsForModel(NodeSettingsRO settings)
			throws InvalidSettingsException {

		try {
			final NodeSettingsRO mySettings =
					settings.getNodeSettings(getConfigKey());
			validateRange(readValue(mySettings, MIN_KEY),
					readValue(mySettings, MAX_KEY));
			settings.getBoolean(ALLOW_EQUAL_MAXMIN_KEY);
		} catch (final InvalidSettingsException | IllegalArgumentException e) {
			throw new InvalidSettingsException(getClass().getSimpleName()
					+ " - " + getConfigKey() + ": " + e.getMessage());
		}

	}

	/**
	 * Abstract method to read a value from the settings model
	 * 
	 * @param mySettings
	 *            The settings object
	 * @param subKey
	 *            The key of the value in the settings object.
	 * @return The value
	 * @throws InvalidSettingsException
	 *             if there was an error reading the value from the settings
	 */
	protected abstract T readValue(NodeSettingsRO mySettings, String subKey)
			throws InvalidSettingsException;

	@Override
	protected void loadSettingsForModel(NodeSettingsRO settings)
			throws InvalidSettingsException {
		try {
			final NodeSettingsRO mySettings =
					settings.getNodeSettings(getConfigKey());
			final T newMin = readValue(mySettings, MIN_KEY);
			final T newMax = readValue(mySettings, MAX_KEY);
			final boolean equalMaxMin =
					mySettings.getBoolean(ALLOW_EQUAL_MAXMIN_KEY);
			setRange(newMin, newMax);

			setAllowEqualUpperLower(equalMaxMin);
		} catch (final InvalidSettingsException | IllegalArgumentException e) {
			throw new InvalidSettingsException(getClass().getSimpleName()
					+ " - " + getConfigKey() + ": " + e.getMessage());
		}

	}

	@Override
	protected void saveSettingsForModel(NodeSettingsWO settings) {
		final NodeSettingsWO mySettings =
				settings.addNodeSettings(getConfigKey());
		writeValue(mySettings, MIN_KEY, min);
		writeValue(mySettings, MAX_KEY, max);
		mySettings.addBoolean(ALLOW_EQUAL_MAXMIN_KEY, allowEqualUpperLower());

	}

	/**
	 * Method to write a value to the settings.
	 * 
	 * @param mySettings
	 *            The settings object to write the value to
	 * @param subKey
	 *            The key to use to store the value in the settings object
	 * @param value
	 *            The value to store
	 */
	protected abstract void writeValue(NodeSettingsWO mySettings, String subKey,
			T value);

	@Override
	public String toString() {
		return "[" + getClass().getName() + ": " + minBound.toString() + " - "
				+ maxBound.toString() + " (" + min.toString() + " - "
				+ max.toString() + ")]";
	}

	@Override
	public T getUpperBound() {
		return maxBound;
	}

	@Override
	public T getLowerBound() {
		return minBound;
	}

	@Override
	public T getLowerValue() {
		return min;
	}

	@Override
	public T getUpperValue() {
		return max;
	}

	@Override
	public void setRange(T lowerVal, T upperVal) {
		validateRange(lowerVal, upperVal);
		min = lowerVal;
		max = upperVal;

	}

	@Override
	public boolean allowEqualUpperLower() {
		return allowEqualUpperLower;
	}

	@Override
	public void setAllowEqualUpperLower(boolean allowEqualUpperLower) {
		this.allowEqualUpperLower = allowEqualUpperLower;

	}

}
