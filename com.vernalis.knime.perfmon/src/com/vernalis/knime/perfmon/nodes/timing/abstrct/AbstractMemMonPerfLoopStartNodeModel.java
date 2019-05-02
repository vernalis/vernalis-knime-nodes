/*******************************************************************************
 * Copyright (c) 2016, 2019 Vernalis (R&D) Ltd
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
package com.vernalis.knime.perfmon.nodes.timing.abstrct;

import static com.vernalis.knime.core.memory.MemoryUtils.getJVMAllocatedMemory;
import static com.vernalis.knime.core.memory.MemoryUtils.getJVMFreeMemory;
import static com.vernalis.knime.core.memory.MemoryUtils.getJVMMaxAvailableMemory;
import static com.vernalis.knime.core.memory.MemoryUtils.getJVMUsedMemory;
import static com.vernalis.knime.core.memory.MemoryUtils.getSystemProcessMemory;
import static com.vernalis.knime.perfmon.nodes.timing.abstrct.AbstractMemMonPerfLoopStartNodeDialog.getMonitoringDelayModel;

import java.util.Arrays;
import java.util.Date;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.date.DateAndTimeCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortType;
import org.knime.core.util.ThreadUtils;

import com.vernalis.knime.core.os.UnsupportedOperatingSystemException;
import com.vernalis.knime.core.system.CommandExecutionException;
import com.vernalis.knime.perfmon.MemoryPerformanceMonitoringLoopStart;

/**
 * @author s.roughley
 * 
 */
@SuppressWarnings("deprecation")
public class AbstractMemMonPerfLoopStartNodeModel
		extends AbstractPerfMonTimingStartNodeModel
		implements MemoryPerformanceMonitoringLoopStart {

	boolean isMonitoring = false;
	BufferedDataContainer m_monitoringContainer = null;
	private Thread m_monitorThread = null;

	protected Integer m_previousIteration = null;
	protected SettingsModelIntegerBounded m_monitoringIntervalMS =
			getMonitoringDelayModel();
	protected Long m_iterationStartTime = null;

	/**
	 * @param portType
	 * @param numPorts
	 */
	public AbstractMemMonPerfLoopStartNodeModel(PortType portType,
			int numPorts) {
		super(portType, numPorts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.perfmon.nodes.timing.abstrct.
	 * AbstractPerfMonTimingStartNodeModel
	 * #execute(org.knime.core.node.port.PortObject[],
	 * org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {
		startMonitoring(m_monitoringIntervalMS.getIntValue(), exec);
		try {
			return super.execute(inObjects, exec);
		} catch (Exception e) {
			stopMonitoring();
			throw new Exception(e.getMessage(), e.getCause());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.perfmon.MemoryPerformanceMonitoringLoopStart
	 * #startMonitoring(int)
	 */
	@Override
	public void startMonitoring(int msDelay, ExecutionContext exec) {
		if (!isMonitoring) {
			isMonitoring = true;
			m_monitoringContainer =
					exec.createDataContainer(createMonitorTableSpec());
			m_monitorThread =
					ThreadUtils.threadWithContext(new MonitorMemory(msDelay),
							"Performance Monitoring Memory Monitor Thread");
			m_monitorThread.setDaemon(true);
			m_monitorThread.start();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.perfmon.MemoryPerformanceMonitoringLoopStart
	 * #stopMonitoring()
	 */
	@Override
	public void stopMonitoring() {
		isMonitoring = false;
		// Now retrieve the monitoring thread and wait for it to complete...
		if (m_monitorThread != null) {
			try {
				m_monitorThread.join();
			} catch (InterruptedException e) {
				//
			}
			m_monitorThread = null;
		}
		if (m_monitoringContainer != null) {
			synchronized (m_monitoringContainer) {
				if (m_monitoringContainer.isOpen()) {
					m_monitoringContainer.close();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.perfmon.MemoryPerformanceMonitoringLoopStart
	 * #getTimingTable()
	 */
	@Override
	public BufferedDataTable getTimingTable() throws Exception {
		if (isMonitoring) {
			stopMonitoring();
		}
		if (m_monitoringContainer == null) {
			throw new Exception("Monitoring not Started - no table available");
		}
		return m_monitoringContainer.getTable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.perfmon.MemoryPerformanceMonitoringLoopStart
	 * #createMonitorTableSpec()
	 */
	@Override
	public DataTableSpec createMonitorTableSpec() {
		DataColumnSpec[] colSpecs = new DataColumnSpec[8];
		int colIdx = 0;
		colSpecs[colIdx++] =
				new DataColumnSpecCreator("Date and Time", DateAndTimeCell.TYPE)
						.createSpec();
		colSpecs[colIdx++] =
				new DataColumnSpecCreator("Time since start of iteratoin",
						DateAndTimeCell.TYPE).createSpec();
		colSpecs[colIdx++] =
				new DataColumnSpecCreator("Iteration", IntCell.TYPE)
						.createSpec();
		colSpecs[colIdx++] =
				new DataColumnSpecCreator("System JVM process Memory (MB)",
						DoubleCell.TYPE).createSpec();
		colSpecs[colIdx++] =
				new DataColumnSpecCreator("Max. Available Memory (MB)",
						DoubleCell.TYPE).createSpec();
		colSpecs[colIdx++] =
				new DataColumnSpecCreator("Currently Allocated Memory (MB)",
						DoubleCell.TYPE).createSpec();
		colSpecs[colIdx++] =
				new DataColumnSpecCreator("Used Memory (MB)", DoubleCell.TYPE)
						.createSpec();
		colSpecs[colIdx++] =
				new DataColumnSpecCreator("Free Memory (MB)", DoubleCell.TYPE)
						.createSpec();

		return new DataTableSpec(colSpecs);
	}

	/**
	 * Return a datarow containing the current memory state
	 * 
	 * @throws UnsupportedOperatingSystemException
	 * @throws CommandExecutionException
	 */
	protected DataCell[] getDataRow()
			throws UnsupportedOperatingSystemException {
		DataCell[] cells = new DataCell[8];
		Arrays.fill(cells, DataType.getMissingCell());
		int colIdx = 0;
		long now = new Date().getTime();
		if (m_previousIteration == null || m_previousIteration != m_iteration) {
			m_iterationStartTime = now;
			m_previousIteration = m_iteration;
		}
		cells[colIdx++] =
				new DateAndTimeCell(new Date().getTime(), true, true, true);
		cells[colIdx++] = new DateAndTimeCell(now - m_iterationStartTime, false,
				true, true);
		cells[colIdx++] = new IntCell(m_iteration);
		Double mem;
		try {
			mem = getSystemProcessMemory();
			cells[colIdx++] = new DoubleCell(mem);
		} catch (CommandExecutionException e) {
			m_logger.info("Unable to retrieve system process memory: "
					+ e.getMessage());
		}
		mem = getJVMMaxAvailableMemory();
		cells[colIdx++] = new DoubleCell(mem);
		mem = getJVMAllocatedMemory();
		cells[colIdx++] = new DoubleCell(mem);
		mem = getJVMUsedMemory();
		cells[colIdx++] = new DoubleCell(mem);
		mem = getJVMFreeMemory();
		cells[colIdx++] = new DoubleCell(mem);
		return cells;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.perfmon.nodes.timing.abstrct.
	 * AbstractPerfMonTimingStartNodeModel#reset()
	 */
	@Override
	protected void reset() {
		stopMonitoring();
		m_monitoringContainer = null;
		m_previousIteration = null;
		m_iterationStartTime = null;
		super.reset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#onDispose()
	 */
	@Override
	protected void onDispose() {
		// Make sure we clean up the monitoring thread
		reset();
		super.onDispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.perfmon.nodes.timing.abstrct.
	 * AbstractPerfMonTimingStartNodeModel
	 * #saveSettingsTo(org.knime.core.node.NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_monitoringIntervalMS.saveSettingsTo(settings);
		super.saveSettingsTo(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.perfmon.nodes.timing.abstrct.
	 * AbstractPerfMonTimingStartNodeModel
	 * #validateSettings(org.knime.core.node.NodeSettingsRO)
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_monitoringIntervalMS.validateSettings(settings);
		super.validateSettings(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.perfmon.nodes.timing.abstrct.
	 * AbstractPerfMonTimingStartNodeModel
	 * #loadValidatedSettingsFrom(org.knime.core.node.NodeSettingsRO)
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_monitoringIntervalMS.loadSettingsFrom(settings);
		super.loadValidatedSettingsFrom(settings);
	}

	private class MonitorMemory implements Runnable {

		private final int msDelay;
		private int rowIdx = 0;

		private MonitorMemory(int msDelay) {
			if (msDelay < 0) {
				msDelay *= -1;
			}
			this.msDelay = msDelay;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (isMonitoring) {
				try {
					Thread.sleep(msDelay);
				} catch (InterruptedException e) {
					m_logger.warn(
							"Monitoring thread interrupted: " + e.getMessage());
					break;
				}
				synchronized (m_monitoringContainer) {
					try {
						m_monitoringContainer.addRowToTable(new DefaultRow(
								new RowKey("Row_" + rowIdx++), getDataRow()));
					} catch (UnsupportedOperatingSystemException e) {
						;
					}
				}
			}
		}

	}
}
