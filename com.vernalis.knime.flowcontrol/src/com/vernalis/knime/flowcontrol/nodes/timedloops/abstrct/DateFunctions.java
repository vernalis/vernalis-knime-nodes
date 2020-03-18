/*******************************************************************************
 * Copyright (c) 2014, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Utility functions for handling date comparisons and generating end dates.
 * 
 * @author Stephen Roughley <s.roughey@vernalis.com>
 */
public class DateFunctions {

	/**
	 * @return The current date/time
	 */
	public static Date getCurrentDate() {
		GregorianCalendar gc = new GregorianCalendar();
		return gc.getTime();
	}

	/**
	 * Checks whether the current date/time is after a specified date/time.
	 * 
	 * @param endTime
	 *            The specified end time/date
	 * @return true if now is after endTime
	 */
	public static boolean isNowAfter(Date endTime) {
		Date currentTime = getCurrentDate();
		return currentTime.after(endTime);
	}

	/**
	 * Creates an end time, based on an hour and minute. OPtions allow for
	 * running until that time on the following day if it has already passed on
	 * the current day, and for running until the following Monday if the
	 * resulting time is on a Saturday or Sunday.
	 * 
	 * @param hour
	 *            The hour (0 - 23)
	 * @param min
	 *            The minute (0 - 59)
	 * @param runTillTomorrow
	 *            If set, then the time will have 1 day added if the specified
	 *            time is already in the past
	 * @param runTillMonday
	 *            If set, then the time will be changed to the following Monday
	 *            if it is on a Saturday or Sunday (taking account of the
	 *            runTillTomorrow setting, which is applied first)
	 * @return A Date representing the required end time
	 */
	public static Date getEndTime(int hour, int min, boolean runTillTomorrow,
			boolean runTillMonday) {
		GregorianCalendar gc = new GregorianCalendar();// Now, today!
		gc.set(Calendar.HOUR_OF_DAY, hour);
		gc.set(Calendar.MINUTE, min);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);

		// Now set the date to tomorrow if the time has already passed today and
		// the option is set
		if (runTillTomorrow && gc.getTime().before(getCurrentDate())) {
			gc.add(Calendar.DATE, 1);
		}

		// And now keep incrementing until we are not ending on a Sat or Sun if
		// the option is set
		if (runTillMonday) {
			int dayOfWeek = gc.get(Calendar.DAY_OF_WEEK);
			while (dayOfWeek == Calendar.SATURDAY
					|| dayOfWeek == Calendar.SUNDAY) {
				gc.add(Calendar.DATE, 1);
				dayOfWeek = gc.get(Calendar.DAY_OF_WEEK);
			}
		}

		return gc.getTime();
	}

	/**
	 * Creates an end time/date based on an elapsing time interval from the
	 * current time/date.
	 * 
	 * @param day
	 *            The number of days to add
	 * @param hour
	 *            The number of hours to add
	 * @param min
	 *            The number of minutes to add
	 * @param sec
	 *            The number of seconds to add
	 * @return A Date representing the required end time
	 */
	public static Date getEndTime(int day, int hour, int min, int sec) {
		GregorianCalendar gc = new GregorianCalendar();// Now, today!
		gc.add(Calendar.DATE, day);
		gc.add(Calendar.HOUR, hour);
		gc.add(Calendar.MINUTE, min);
		gc.add(Calendar.SECOND, sec);
		return gc.getTime();
	}
}
