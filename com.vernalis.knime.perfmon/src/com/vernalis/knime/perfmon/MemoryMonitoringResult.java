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

import org.knime.core.data.DataCell;

/**
 * Interface describing a memory monitoring result
 * 
 * @author Steve <knime@vernalis.com>
 *
 */
public interface MemoryMonitoringResult {

	/**
	 * @return The timestamp (UTC) when the snapshot was created
	 */
	long getTimestamp();

	/**
	 * @return The System process memory usage (MB). Will return {@code null} if
	 *         unable to calculate
	 */
	Double getSysProcMem();

	/**
	 * @return The JVM's maximum available memory (MB)
	 */
	double getJvmMaxAvailableMem();

	/**
	 * @return The JVM's allocated memory (MB)
	 */
	double getJvmAllocMem();

	/**
	 * @return The JVM's used memory (MB)
	 */
	double getJvmUsedMem();

	/**
	 * @return The JVM's free memory (MB)
	 */
	double getJvmFreeMem();

	/**
	 * @return An array of datacells in the order corresponding to the columns in
	 *         {@link #getTableSpec()}
	 */
	DataCell[] getDataCells();

}