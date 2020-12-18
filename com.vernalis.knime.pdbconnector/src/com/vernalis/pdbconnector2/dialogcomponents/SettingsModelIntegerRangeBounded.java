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

/**
 * Settings model for an bounded integer range
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class SettingsModelIntegerRangeBounded
		extends AbstractSettingsModelRangeBounded<Integer> {

	/**
	 * Constructor
	 * 
	 * @param configName
	 *            The setting model key
	 * @param minRange
	 *            The minimum value
	 * @param maxRange
	 *            The maximum value
	 * @param lowerBound
	 *            The lower bound
	 * @param upperBound
	 *            The upper bound
	 */
	public SettingsModelIntegerRangeBounded(String configName, int minRange,
			int maxRange, int lowerBound, int upperBound) {
		super(configName, minRange, maxRange, lowerBound, upperBound);
	}

	@Override
	protected Integer readValue(NodeSettingsRO mySettings, String subKey)
			throws InvalidSettingsException {
		return mySettings.getInt(subKey);
	}

	@Override
	protected void writeValue(NodeSettingsWO mySettings, String subKey,
			Integer value) {
		mySettings.addInt(subKey, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected SettingsModelIntegerRangeBounded createClone() {
		return new SettingsModelIntegerRangeBounded(getConfigKey(),
				getLowerValue(), getUpperValue(), getLowerBound(),
				getUpperBound());
	}

	@Override
	protected String getModelTypeID() {
		return "SMID_IntegerRangeBounded";
	}

}
