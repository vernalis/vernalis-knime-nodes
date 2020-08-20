/*******************************************************************************
 * Copyright (c) 2020 Vernalis (R&D) Ltd
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
 ******************************************************************************/
package com.vernalis.knime.perfmon;

import java.util.Date;

/**
 * A {@link MemoryMonitoringResult} implementation which tracks the loop
 * iteration counter and start time
 *
 * @author Steve <knime@vernalis.com>
 *
 */
public class KnowsIterationMemoryMonitoringResult extends MemoryMonitoringResultImpl {
	private final int iteration;
	private final long baseTime;

	/**
	 * @param baseTime
	 */
	public KnowsIterationMemoryMonitoringResult(Date baseTime, int iteration) {
		this(baseTime.getTime(), iteration);
	}

	/**
	 * @param baseTime
	 */
	public KnowsIterationMemoryMonitoringResult(long baseTime, int iteration) {
		super();
		this.baseTime = baseTime;
		this.iteration = iteration;
	}

	/**
	 * @return the iteration
	 */
	public int getIteration() {
		return iteration;
	}

	/**
	 * @return The base time (UTC) for the monitoring
	 */
	public long getBaseTime() {
		return baseTime;
	}

}
