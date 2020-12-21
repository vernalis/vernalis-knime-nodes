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

import java.util.Calendar;
import java.util.Date;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * A settings model to store a bounded date range. All dates will be normalised
 * to midnight (00:00:00.000)
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class SettingsModelDateRangeBounded
		extends AbstractSettingsModelRangeBounded<Date> {

	/**
	 * Constructor
	 * 
	 * @param configKey
	 *            The settings model config key
	 * @param minRange
	 *            The minimum value of the range
	 * @param maxRange
	 *            The maximum value of the range
	 * @param lowerBound
	 *            The minimum bound
	 * @param upperBound
	 *            The maxium bound
	 */
	public SettingsModelDateRangeBounded(String configKey, Date minRange,
			Date maxRange, Date lowerBound, Date upperBound) {
		super(configKey, setToMidnight(minRange), setToMidnight(maxRange),
				setToMidnight(lowerBound), setToMidnight(upperBound));

	}

	@SuppressWarnings("deprecation")
	private static Date setToMidnight(Date d) {
		final Calendar c = Calendar.getInstance();
		c.clear();
		c.set(d.getYear() + 1900, d.getMonth(), d.getDate());
		return c.getTime();
	}

	@Override
	protected Date readValue(NodeSettingsRO mySettings, String subKey)
			throws InvalidSettingsException {
		return new Date(mySettings.getLong(subKey));
	}

	@Override
	protected void writeValue(NodeSettingsWO mySettings, String subKey,
			Date value) {
		mySettings.addLong(subKey, value.getTime());

	}

	@SuppressWarnings("unchecked")
	@Override
	protected SettingsModelDateRangeBounded createClone() {
		return new SettingsModelDateRangeBounded(getConfigKey(),
				getLowerValue(), getUpperValue(), getLowerBound(),
				getUpperBound());
	}

	@Override
	protected String getModelTypeID() {
		return "SMID_DateRangeBounded";
	}

}
