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

import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.SettingsModelLongBounded;

/**
 * A simple SettingsModel to store only a date (no time) within a bounded range.
 * Supplied dates are normalised to midnight (00:00:00.000) on the supplied day
 *
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class SettingsModelDateBounded extends SettingsModelLongBounded {

	/**
	 * Constructot
	 * 
	 * @param configName
	 *            The settings model key
	 * @param value
	 *            The initial value
	 * @param minBound
	 *            The minimum bound
	 * @param maxBound
	 *            The maximum bound
	 */
	public SettingsModelDateBounded(String configName, Date value,
			Date minBound, Date maxBound) {
		super(configName, fixDateToMidnight(value).getTime(),
				fixDateToMidnight(minBound).getTime(),
				fixDateToMidnight(maxBound).getTime());

	}

	@SuppressWarnings("deprecation")
	private static Date fixDateToMidnight(Date d) {
		final Calendar c = Calendar.getInstance();
		c.clear();
		c.set(d.getYear() + 1900, d.getMonth(), d.getDate());
		return c.getTime();
	}

	@Override
	protected SettingsModelDateBounded createClone() {
		return new SettingsModelDateBounded(getConfigName(), getDate(),
				getMinBound(), getMaxBound());
	}

	/**
	 * @return The stored {@link Date} value
	 */
	public Date getDate() {
		return new Date(getLongValue());
	}

	/**
	 * Set the stored date, fixed to midnight (00:00:00.000) on the specified
	 * date
	 * 
	 * @param d
	 *            The date
	 */
	public void setDate(Date d) {
		super.setLongValue(fixDateToMidnight(d).getTime());
	}

	/**
	 * @return The minium bound value
	 */
	public Date getMinBound() {
		return new Date(super.getLowerBound());
	}

	/**
	 * @return The maximum bound value
	 */
	public Date getMaxBound() {
		return new Date(super.getUpperBound());
	}

	@Override
	public void prependChangeListener(ChangeListener l) {
		super.prependChangeListener(l);
	}

}
