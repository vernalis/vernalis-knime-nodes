/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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

import org.knime.core.node.defaultnodesettings.SettingsModelFlowVariableCompatible;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * A SettingsModel which can be used to supply a String Array directly to / from
 * a single flow variable.
 * <p>
 * The array is serialised as a string by concatenating all entries with a
 * deliminator. This is the value which is then stored, enabling e.g. lists of
 * files to be passed as an array, but supplied from a single Flow Variable.
 * </p>
 * <p>
 * <b>NB</b> It is important to ensure that the chosen deliminator does not
 * appear in any of the array entries. If it does, then the array will become
 * corrupted on serialisation. It is also important to note that the deliminator
 * must be a valid regex, as it is used during deserialisation in
 * {@link String#split(String)}
 * 
 * @author "Steve Roughley knime@vernalis.com"
 * 
 */
public class SettingsModelStringArrayFlowVarReplacable extends
		SettingsModelString implements SettingsModelFlowVariableCompatible {

	/** The default deliminator */
	public static final String DELIMINATOR = ";";

	protected String m_deliminator;

	/**
	 * Default constructor, where the settings model is created with the given
	 * parameters, and the default deliminator
	 * {@link SettingsModelStringArrayFlowVarReplacable#DELIMINATOR} is used. If
	 * an alternative deliminator is required, then
	 * {@link #SettingsModelStringArrayFlowVarReplacable(String, String[], String)}
	 * should be used.
	 * 
	 * @param configName
	 *            The name of the settings model configuration
	 * @param defaultValue
	 *            The default value for the settings model.
	 */
	public SettingsModelStringArrayFlowVarReplacable(String configName,
			String[] defaultValue) {
		super(configName, "");
		m_deliminator = DELIMINATOR;
		setStringArrayValue(defaultValue);
	}

	/**
	 * Constructor in which a non-default string deliminator is supplied. If the
	 * default deliminator is used, then
	 * {@link #SettingsModelStringArrayFlowVarReplacable(String, String[])}
	 * should be used.
	 * 
	 * @param configName
	 *            The name of the settings model configuration
	 * @param defaultValue
	 *            The default value for the settings model.
	 * @param deliminator
	 *            A deliminator to separate items in the array when the array is
	 *            serialised to / from a flow variable string. This character
	 *            sequence must not appear in the possible array entries, else
	 *            corruption will result
	 */
	public SettingsModelStringArrayFlowVarReplacable(String configName,
			String[] defaultValue, String deliminator) {
		super(configName, "");
		m_deliminator = deliminator;
		setStringArrayValue(defaultValue);
	}

	/**
	 * @return The stored values array. May return {@code null}
	 */
	public String[] getStringArrayValue() {
		String stringValue = super.getStringValue();
		if (stringValue == null) {
			return null;
		}
		if ("".equals(stringValue)) {
			return new String[0];
		}
		String[] retVal;
		if (stringValue.indexOf(m_deliminator) >= 0) {
			retVal = stringValue.split(m_deliminator);
			for (int i = 0; i < retVal.length; i++) {
				retVal[i] = retVal[i].trim();
			}
		} else {
			// Only a single value
			retVal = new String[1];
			retVal[0] = stringValue.trim();
		}
		return retVal;
	}

	/**
	 * Set the value of the stored array
	 * 
	 * @param values
	 *            The values to be used (can be {@code null})
	 */
	public void setStringArrayValue(String[] values) {
		String stringVal;
		if (values == null) {
			stringVal = null;
		} else if (values.length == 0) {
			stringVal = "";
		} else {
			// We have at least one value...
			StringBuilder retVal = new StringBuilder(values[0]);
			for (int i = 1; i < values.length; i++) {
				retVal.append(m_deliminator).append(values[i]);
			}
			stringVal = retVal.toString();
		}
		super.setStringValue(stringVal);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.defaultnodesettings.SettingsModelString#getModelTypeID
	 * ()
	 */
	@Override
	protected String getModelTypeID() {
		return "SMID_StringArray_FlowVarReplacable";
	}

	/**
	 * @return The deliminator used for serialization of Arrays to Strings and
	 *         vice versa
	 */
	public String getDeliminator() {
		return m_deliminator;
	}

}
