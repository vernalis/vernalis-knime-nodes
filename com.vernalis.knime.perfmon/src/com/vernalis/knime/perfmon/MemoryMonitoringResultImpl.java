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

import static com.vernalis.knime.core.memory.MemoryUtils.getJVMAllocatedMemory;
import static com.vernalis.knime.core.memory.MemoryUtils.getJVMFreeMemory;
import static com.vernalis.knime.core.memory.MemoryUtils.getJVMMaxAvailableMemory;
import static com.vernalis.knime.core.memory.MemoryUtils.getJVMUsedMemory;
import static com.vernalis.knime.core.memory.MemoryUtils.getSystemProcessMemory;

import java.util.Date;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.NodeLogger;

import com.vernalis.knime.core.os.UnsupportedOperatingSystemException;
import com.vernalis.knime.core.system.CommandExecutionException;

/**
 * A class to store a memory usage snapshot with a timestamp
 *
 * @author S.Roughley <knime@vernalis.com>
 * @since 1.27.0
 */
public class MemoryMonitoringResultImpl implements MemoryMonitoringResult {

	/**
	 * Static logger instance
	 */
	private static final NodeLogger logger = NodeLogger.getLogger(MemoryMonitoringResultImpl.class);

	private final long timestamp;
	private Double sysProcMem;
	private final double jvmMaxAvailableMem;
	private final double jvmAllocMem;
	private final double jvmUsedMem;
	private final double jvmFreeMem;

	private static final DataTableSpec TABLE_SPEC = new DataTableSpecCreator()
			.addColumns(new DataColumnSpecCreator("System JVM process Memory (MB)", DoubleCell.TYPE).createSpec())
			.addColumns(new DataColumnSpecCreator("Max. Available Memory (MB)", DoubleCell.TYPE).createSpec())
			.addColumns(new DataColumnSpecCreator("Currently Allocated Memory (MB)", DoubleCell.TYPE).createSpec())
			.addColumns(new DataColumnSpecCreator("Used Memory (MB)", DoubleCell.TYPE).createSpec())
			.addColumns(new DataColumnSpecCreator("Free Memory (MB)", DoubleCell.TYPE).createSpec()).createSpec();



	/**
	 * Constructor - creates a new object with the current memory usage values
	 **/
	public MemoryMonitoringResultImpl() {
		timestamp = new Date().getTime();
		try {
			sysProcMem = getSystemProcessMemory();
		} catch (final CommandExecutionException | UnsupportedOperatingSystemException e) {
			logger.info("Unable to retrieve system process memory: " + e.getMessage());
		}
		jvmMaxAvailableMem = getJVMMaxAvailableMemory();
		jvmAllocMem = getJVMAllocatedMemory();
		jvmUsedMem = getJVMUsedMemory();
		jvmFreeMem = getJVMFreeMemory();

	}

	/**
	 * @return The timestamp (UTC) when the snapshot was created
	 */
	@Override
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return The System process memory usage (MB). Will return {@code null} if
	 *         unable to calculate
	 */
	@Override
	public final Double getSysProcMem() {
		return sysProcMem;
	}

	/**
	 * @return The JVM's maximum available memory (MB)
	 */
	@Override
	public final double getJvmMaxAvailableMem() {
		return jvmMaxAvailableMem;
	}

	/**
	 * @return The JVM's allocated memory (MB)
	 */
	@Override
	public final double getJvmAllocMem() {
		return jvmAllocMem;
	}

	/**
	 * @return The JVM's used memory (MB)
	 */
	@Override
	public final double getJvmUsedMem() {
		return jvmUsedMem;
	}

	/**
	 * @return The JVM's free memory (MB)
	 */
	@Override
	public final double getJvmFreeMem() {
		return jvmFreeMem;
	}

	/**
	 * @return An array of datacells in the order corresponding to the columns in
	 *         {@link #getTableSpec()}
	 */
	@Override
	public final DataCell[] getDataCells() {
		return new DataCell[] { getSysProcMem() == null ? DataType.getMissingCell() : new DoubleCell(getSysProcMem()),
				new DoubleCell(getJvmMaxAvailableMem()), new DoubleCell(getJvmAllocMem()),
				new DoubleCell(getJvmUsedMem()), new DoubleCell(getJvmFreeMem()) };
	}

	/**
	 * @return The table spec for the memory use columns
	 */
	public static DataTableSpec getTableSpec() {
		return TABLE_SPEC;
	}
}
