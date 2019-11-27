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
import org.knime.core.node.defaultnodesettings.SettingsModelFlowVariableCompatible;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.FlowVariable.Type;

/**
 * This is a SettingsModel implementation representing a bounded integer. The
 * bounds can be changed
 * 
 * @author s.roughley
 *
 */
public class SettingsModelIntegerBoundedRerangable extends SettingsModelInteger
		implements SettingsModelFlowVariableCompatible {

	private int m_iMinVal, m_iMaxVal;

	/**
	 * Constructor
	 * 
	 * @param configName
	 *            The settings model config name
	 * @param defaultValue
	 *            The default value
	 * @param min
	 *            The initial minimum
	 * @param max
	 *            The initial maximum
	 */
	public SettingsModelIntegerBoundedRerangable(String configName, int defaultValue, int min,
			int max) {
		super(configName, defaultValue);
		if (min > max) {
			throw new IllegalArgumentException(
					"Specified min value must be" + " smaller than the max value.");
		}
		m_iMinVal = min;
		m_iMaxVal = max;

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
		StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append(" - ")
				.append(getConfigName()).append(":[");
		sb.append(m_iMinVal).append(",").append(m_iMaxVal);
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
			setBounds(mySettings.getInt("iMin"), mySettings.getInt("iMax"));
			setIntValue(mySettings.getInt("iVal"));

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
		int iMin = mySettings.getInt("iMin");
		int iMax = mySettings.getInt("iMax");
		if (iMin > iMax) {
			throw new InvalidSettingsException(
					"iMin > iMax in " + getClass().getSimpleName() + " - " + getConfigName());
		}
		int iVal = mySettings.getInt("iVal");
		if (iVal < iMin || iVal > iMax) {
			throw new InvalidSettingsException("iVal outside range [" + iMin + "," + iMax + "] in"
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
			setBounds(mySettings.getInt("iMin"), mySettings.getInt("iMax"));
			setIntValue(mySettings.getInt("iVal"));
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
		mySettings.addInt("iMin", m_iMinVal);
		mySettings.addInt("iMax", m_iMaxVal);
		mySettings.addInt("iVal", getIntValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.defaultnodesettings.SettingsModelDouble#createClone()
	 */
	@Override
	protected SettingsModelIntegerBoundedRerangable createClone() {
		return new SettingsModelIntegerBoundedRerangable(getConfigName(), getIntValue(), m_iMinVal,
				m_iMaxVal);

	}

	@Override
	public void setIntValue(int newValue) {
		checkBounds(newValue);
		super.setIntValue(newValue);
	}

	/**
	 * @return the lower bound of the acceptable values.
	 */
	public int getLowerBound() {
		return m_iMinVal;
	}

	/**
	 * @return the upper bound of the acceptable values.
	 */
	public int getUpperBound() {
		return m_iMaxVal;
	}

	/**
	 * Set the lower bound of the acceptable values. If the stored value is now
	 * illegal then an {@link IllegalArgumentException} is thrown after
	 * notification of change listeners
	 */
	public void setBounds(int min, int max) {
		boolean notify = (m_iMinVal != min || m_iMaxVal != max);
		if (min > max) {
			throw new IllegalArgumentException("min > max");
		}
		m_iMinVal = min;
		m_iMaxVal = max;
		try {
			checkBounds(getIntValue());
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
		return "SM Integer Bounded Reboundable";
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
		return Type.INTEGER;
	}

	private void checkBounds(int value) throws IllegalArgumentException {
		if ((value < m_iMinVal) || (m_iMaxVal < value)) {
			throw new IllegalArgumentException("value (=" + value + ") must be within the range ["
					+ m_iMinVal + "..." + m_iMaxVal + "].");
		}
	}

}
