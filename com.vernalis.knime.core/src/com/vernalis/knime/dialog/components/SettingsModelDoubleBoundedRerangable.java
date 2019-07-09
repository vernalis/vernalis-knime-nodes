/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelFlowVariableCompatible;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.FlowVariable.Type;

/**
 * This is a SettingsModel implementation representing a bounded double for
 * which the bounds can be changed
 * 
 * @author s.roughley
 *
 */
public class SettingsModelDoubleBoundedRerangable extends SettingsModelDouble
		implements SettingsModelFlowVariableCompatible {

	private double m_dMinVal, m_dMaxVal;

	/**
	 * The constructor
	 * 
	 * @param configName
	 *            The settings config name
	 * @param defaultValue
	 *            The default value
	 * @param min
	 *            The initial minimum value
	 * @param max
	 *            The initial maximum value
	 */
	public SettingsModelDoubleBoundedRerangable(String configName, double defaultValue, double min,
			double max) {
		super(configName, defaultValue);
		if (min > max) {
			throw new IllegalArgumentException(
					"Specified min value must be" + " smaller than the max value.");
		}
		m_dMinVal = min;
		m_dMaxVal = max;

		// the actual value is the specified default value
		try {
			checkBounds(defaultValue);
		} catch (IllegalArgumentException iae) {
			throw new IllegalArgumentException("InitialValue:" + iae.getMessage());
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder sb =
				new StringBuilder(getClass().getSimpleName()).append(" - ").append(getConfigName())
						.append(":[").append(m_dMinVal).append(",").append(m_dMaxVal);
		return sb.append("]").toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModelDouble#
	 * loadSettingsForDialog(org.knime.core.node.NodeSettingsRO,
	 * org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	protected void loadSettingsForDialog(NodeSettingsRO settings, PortObjectSpec[] specs)
			throws NotConfigurableException {
		try {
			NodeSettingsRO mySettings = settings.getNodeSettings(getConfigName());
			setBounds(mySettings.getDouble("dMin"), mySettings.getDouble("dMax"));
			setDoubleValue(mySettings.getDouble("dVal"));
		} catch (IllegalArgumentException | InvalidSettingsException e) {
			// ignore, keep the old values

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModelDouble#
	 * saveSettingsForDialog(org.knime.core.node.NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsForDialog(NodeSettingsWO settings) throws InvalidSettingsException {
		saveSettingsForModel(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModelDouble#
	 * validateSettingsForModel(org.knime.core.node.NodeSettingsRO)
	 */
	@Override
	protected void validateSettingsForModel(NodeSettingsRO settings)
			throws InvalidSettingsException {

		NodeSettingsRO mySettings = settings.getNodeSettings(getConfigName());

		double dMin = mySettings.getDouble("dMin");
		double dMax = mySettings.getDouble("dMax");
		if (dMin > dMax) {
			throw new InvalidSettingsException(
					"dMin > dMax in " + getClass().getSimpleName() + " - " + getConfigName());
		}
		double dVal = mySettings.getDouble("dVal");
		if (dVal < dMin || dVal > dMax) {
			throw new InvalidSettingsException("dVal outside range [" + dMin + "," + dMax + "] in"
					+ getClass().getSimpleName() + " - " + getConfigName());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModelDouble#
	 * loadSettingsForModel(org.knime.core.node.NodeSettingsRO)
	 */
	@Override
	protected void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		try {
			NodeSettingsRO mySettings = settings.getNodeSettings(getConfigName());
			setBounds(mySettings.getDouble("dMin"), mySettings.getDouble("dMax"));
			setDoubleValue(mySettings.getDouble("dVal"));
		} catch (InvalidSettingsException | IllegalArgumentException e) {
			throw new InvalidSettingsException(
					getClass().getSimpleName() + " - " + getConfigName() + ": " + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModelDouble#
	 * saveSettingsForModel(org.knime.core.node.NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsForModel(NodeSettingsWO settings) {
		NodeSettingsWO mySettings = settings.addNodeSettings(getConfigName());
		mySettings.addDouble("dMin", m_dMinVal);
		mySettings.addDouble("dMax", m_dMaxVal);
		mySettings.addDouble("dVal", getDoubleValue());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.defaultnodesettings.SettingsModelDouble#createClone()
	 */
	@Override
	protected SettingsModelDoubleBoundedRerangable createClone() {
		return new SettingsModelDoubleBoundedRerangable(getConfigName(), super.getDoubleValue(),
				m_dMinVal, m_dMaxVal);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModelDouble#
	 * setDoubleValue(double)
	 */
	@Override
	public void setDoubleValue(double newValue) {
		checkBounds(newValue);
		super.setDoubleValue(newValue);
	}

	/**
	 * @return the lower bound of the acceptable values.
	 */
	public double getLowerBound() {
		return m_dMinVal;
	}

	/**
	 * @return the upper bound of the acceptable values.
	 */
	public double getUpperBound() {
		return m_dMaxVal;
	}

	/**
	 * Set the lower bound of the acceptable values. If the stored value is now
	 * illegal then an {@link IllegalArgumentException} is thrown after
	 * notification of change listeners
	 */
	public void setBounds(double min, double max) {
		boolean notify = (m_dMinVal != min || m_dMaxVal != max);
		if (min > max) {
			throw new IllegalArgumentException("min > max");
		}
		m_dMinVal = min;
		m_dMaxVal = max;
		try {
			checkBounds(getDoubleValue());
		} finally {
			if (notify) {
				notifyChangeListeners();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModelDouble#
	 * getModelTypeID()
	 */
	@Override
	protected String getModelTypeID() {
		return "SM Double Bounded Reboundable";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModelDouble#getKey()
	 */
	@Override
	public String getKey() {
		return super.getKey();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModelDouble#
	 * getFlowVariableType()
	 */
	@Override
	public Type getFlowVariableType() {
		return Type.DOUBLE;
	}

	private void checkBounds(double value) throws IllegalArgumentException {
		if ((value < m_dMinVal) || (m_dMaxVal < value)) {
			throw new IllegalArgumentException("value (=" + value + ") must be within the range ["
					+ m_dMinVal + "..." + m_dMaxVal + "].");
		}
	}

}
