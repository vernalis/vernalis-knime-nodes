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
package com.vernalis.knime.perfmon.nodes.timing.abstrct;

import static com.vernalis.knime.perfmon.nodes.timing.abstrct.AbstractMemMonPerfLoopStartNodeDialog.getMonitoringDelayModel;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortType;
import org.knime.core.util.ThreadUtils;

import com.vernalis.knime.perfmon.KnowsIterationMemoryMonitoringResult;
import com.vernalis.knime.perfmon.MemoryPerformanceMonitoringLoopStart;

/**
 * @author s.roughley
 *
 */
public class AbstractMemMonPerfLoopStartNodeModel extends AbstractPerfMonTimingStartNodeModel
implements MemoryPerformanceMonitoringLoopStart<KnowsIterationMemoryMonitoringResult> {

	boolean isMonitoring = false;
	boolean isMonitoringPaused = false;

	private Thread m_monitorThread = null;

	// protected Integer m_previousIteration = null;
	protected SettingsModelIntegerBounded m_monitoringIntervalMS = getMonitoringDelayModel();
	protected Queue<KnowsIterationMemoryMonitoringResult> monitoringData = null;

	/**
	 * @param portType
	 * @param numPorts
	 */
	public AbstractMemMonPerfLoopStartNodeModel(PortType portType, int numPorts) {
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
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		// Start the monitoring thread
		startMonitoring(m_monitoringIntervalMS.getIntValue());
		try {
			return super.execute(inObjects, exec);
		} catch (final Exception e) {
			stopMonitoring();
			throw e;
		}
	}

	@Override
	public void startMonitoring(int msDelay) {
		if (!isMonitoring) {
			isMonitoring = true;
			isMonitoringPaused = false;
			monitoringData = new ConcurrentLinkedQueue<>();
			m_monitorThread = ThreadUtils.threadWithContext(new MonitorMemory(msDelay),
					"Performance Monitoring Memory Monitor Thread");
			m_monitorThread.setDaemon(true);
			m_monitorThread.start();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.vernalis.knime.internal.perfmon.MemoryPerformanceMonitoringLoopStart
	 * #stopMonitoring()
	 */
	@Override
	public void stopMonitoring() {
		isMonitoring = false;
		// Now retrieve the monitoring thread and wait for it to complete...
		if (m_monitorThread != null) {
			try {
				m_monitorThread.join();
			} catch (final InterruptedException e) {
				//
			}
			m_monitorThread = null;
		}
	}

	@Override
	public Iterable<KnowsIterationMemoryMonitoringResult> getIterationMemoryUsage() {
		if (monitoringData == null) {
			throw new RuntimeException("Monitoring not Started - no table available");
		}

		final ArrayList<KnowsIterationMemoryMonitoringResult> retVal = new ArrayList<>();
		while (monitoringData.peek() != null) {
			retVal.add(monitoringData.poll());
		}
		return retVal;
	}

	@Override
	public long getIterationStartTime() {
		return super.getStartDate().getTime();
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
		monitoringData = null;
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
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
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
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		m_monitoringIntervalMS.loadSettingsFrom(settings);
		super.loadValidatedSettingsFrom(settings);
	}

	private class MonitorMemory implements Runnable {

		private final int msDelay;

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
			while (isMonitoring) {
				try {
					Thread.sleep(msDelay);
				} catch (final InterruptedException e) {
					m_logger.warn("Monitoring thread interrupted: " + e.getMessage());
					break;
				}
				// The loop counter increments on the execute() method of loop start, and so is
				// 1 ahead during actual loop body iteration
				monitoringData.add(new KnowsIterationMemoryMonitoringResult(getIterationStartTime(), m_iteration - 1));
			}
		}

	}
}
