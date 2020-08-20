/*******************************************************************************
 * Copyright (c) 2017,2020 Vernalis (R&D) Ltd
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
package com.vernalis.knime.dialog.components;

import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObjectSpec;

/**
 * Settings model for a int range [min, max]. It stores two integer numbers. It
 * ensures that the minimum is smaller than the maximum at any time.
 *
 * @author s.roughley
 */
public class SettingsModelIntegerRange extends SettingsModel {

	private int min, max;

	private final String configName;

	/**
	 * Create setting object.
	 *
	 * @param configName
	 *            identifier in the config file.
	 * @param minRange
	 *            minimum default minimum value
	 * @param maxRange
	 *            maximum default maximum value
	 * @throws IllegalArgumentException
	 *             if the specified configName or range is invalid
	 */
	public SettingsModelIntegerRange(final String configName, final int minRange,
			final int maxRange) throws IllegalArgumentException {
		if (configName == null || "".equals(configName)) {
			throw new IllegalArgumentException("The configName must be a " + "non-empty string");
		}
		this.configName = configName;

		// ensures min<max
		setRange(minRange, maxRange);

	}

	/**
	 * @return the current min value of the range
	 */
	public int getMinRange() {
		return min;
	}

	/**
	 * @return the current max value of the range.
	 */
	public int getMaxRange() {
		return max;
	}

	/**
	 * Sets a new min and a new max value.
	 *
	 * @param newMin
	 *            the new min value
	 * @param newMax
	 *            the new max value
	 * @throws IllegalArgumentException
	 *             if the min is larger than the max or those numbers are not
	 *             really numbers (NaN).
	 */
	public void setRange(final int newMin, final int newMax) throws IllegalArgumentException {

		if (newMin > newMax) {
			throw new IllegalArgumentException(configName + ": The specified minimum (" + newMin
					+ ") is larger than the specified maximum (" + newMax + ").");
		}
		min = newMin;
		max = newMax;
	}

	/**
	 * Sets a new min value of the range.
	 *
	 * @param minRange
	 *            the new min vale of the range
	 * @throws IllegalArgumentException
	 *             if the new min is larger than the current max
	 * @see #setRange(double, double)
	 */
	public void setMinRange(final int minRange) throws IllegalArgumentException {
		setRange(minRange, max);
	}

	/**
	 * Sets a new max value of the range.
	 *
	 * @param maxRange
	 *            the new max value of the range
	 * @throws IllegalArgumentException
	 *             if the current min is larger than the new max
	 * @see #setRange(double, double)
	 */
	public void setMaxRange(final int maxRange) throws IllegalArgumentException {
		setRange(min, maxRange);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected SettingsModelIntegerRange createClone() {
		try {
			return new SettingsModelIntegerRange(configName, min, max);
		} catch (IllegalArgumentException ise) {
			// can't happen.
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getConfigName() {
		return configName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getModelTypeID() {
		return "SMID_intRange";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadSettingsForDialog(final NodeSettingsRO settings,
			final PortObjectSpec[] specs) throws NotConfigurableException {
		try {
			NodeSettingsRO mySettings = settings.getNodeSettings(configName);
			int min = mySettings.getInt("MIN");
			int max = mySettings.getInt("MAX");
			setRange(min, max);
		} catch (IllegalArgumentException | InvalidSettingsException e) {
			// ignore, keep the old values
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadSettingsForModel(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		try {
			NodeSettingsRO mySettings = settings.getNodeSettings(configName);
			setRange(mySettings.getInt("MIN"), mySettings.getInt("MAX"));
		} catch (InvalidSettingsException | IllegalArgumentException e) {
			throw new InvalidSettingsException(
					getClass().getSimpleName() + " - " + configName + ": " + e.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsForDialog(final NodeSettingsWO settings)
			throws InvalidSettingsException {
		saveSettingsForModel(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsForModel(final NodeSettingsWO settings) {
		NodeSettingsWO mySettings = settings.addNodeSettings(configName);
		mySettings.addInt("MIN", min);
		mySettings.addInt("MAX", max);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + " - " + configName + ":[" + min + "," + max + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettingsForModel(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		int min;
		int max;
		try {
			NodeSettingsRO mySettings = settings.getNodeSettings(configName);
			min = mySettings.getInt("MIN");
			max = mySettings.getInt("MAX");
		} catch (InvalidSettingsException e) {
			throw new InvalidSettingsException(
					getClass().getSimpleName() + " - " + configName + ": " + e.getMessage());
		}
		if (min > max) {
			throw new InvalidSettingsException(
					"min>max in " + getClass().getSimpleName() + " - " + configName);
		}
	}

	@Override
	public void prependChangeListener(ChangeListener l) {
		super.prependChangeListener(l);
	}
}
