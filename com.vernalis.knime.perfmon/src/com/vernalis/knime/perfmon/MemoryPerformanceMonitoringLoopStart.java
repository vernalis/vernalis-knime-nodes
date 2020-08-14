/*******************************************************************************
 * Copyright (c) 2016,2020 Vernalis (R&D) Ltd
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

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

/**
 * Type denominator only
 *
 * API breaking changes as of v 1.27.0 due to asynchronous saving bug meaning
 * Loop End node needs to hold and own the memory monitoring table from KNIME
 * 4.2.0 onwards
 *
 * @author s.roughley
 *
 * @param <T> The implementation type of {@link MemoryMonitoringResult}
 */
public interface MemoryPerformanceMonitoringLoopStart<T extends MemoryMonitoringResult>
extends PerformanceMonitoringLoopStart {

	/**
	 * @param msDelay
	 * @param exec
	 * @deprecated Since 1.27.0 - use {@link #startMonitoring(int)}
	 */
	@Deprecated
	default void startMonitoring(int msDelay, ExecutionContext exec) {
		startMonitoring(msDelay);
	}

	/**
	 * Start a memory monitoring thread which adds memory usage stats to a
	 * collection at the indicated intervals
	 *
	 * @param msDelay
	 */
	void startMonitoring(int msDelay);



	/**
	 * Stop the memory monitoring thread
	 */
	void stopMonitoring();

	/**
	 * @return The memory monitoring usage stats for the current iteration.
	 *         Implementations should clear the stored values prior to returning
	 *         them. Implementations should call {@link #pauseMonitoring()}
	 * @since 1.27.0
	 */
	Iterable<T> getIterationMemoryUsage();

	/**
	 * @return The timestamp of the iteration start
	 * @since 1.27.0
	 */
	long getIterationStartTime();

	/**
	 * @throws Exception
	 * @{@link Deprecated} Since 1.27.0 - use
	 */
	@Deprecated
	default BufferedDataTable getTimingTable() throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated Since 1.27.0. The Loop End node should carry out this function
	 *             now
	 */
	@Deprecated
	default DataTableSpec createMonitorTableSpec() {
		return null;
	}
}
