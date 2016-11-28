/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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

import static com.vernalis.knime.perfmon.nodes.timing.abstrct.AbstractPerfMonTimingStartNodeDialog.createIterationsModel;
import static com.vernalis.knime.perfmon.nodes.timing.abstrct.AbstractPerfMonTimingStartNodeDialog.createMaxTimeModel;
import static com.vernalis.knime.perfmon.nodes.timing.abstrct.AbstractPerfMonTimingStartNodeDialog.createTimeCutoutModel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import com.vernalis.knime.perfmon.PerformanceMonitoringLoopStart;

/**
 * This is the model implementation of Performance TimingStart. Loop start for
 * execution timing
 * 
 * @author S. Roughley <s.roughley@vernalis.com>
 */
public class AbstractPerfMonTimingStartNodeModel extends NodeModel implements
		PerformanceMonitoringLoopStart {
	private Date m_StartTime;
	protected Integer m_iteration;
	protected static NodeLogger m_logger = NodeLogger
			.getLogger(AbstractPerfMonTimingStartNodeModel.class);

	// Settings Models
	protected final SettingsModelIntegerBounded m_maxIterations = createIterationsModel();
	protected final SettingsModelBoolean m_useTimeout = createTimeCutoutModel();
	protected final SettingsModelIntegerBounded m_timeOut = createMaxTimeModel();


	/**
	 * Constructor for the node model.
	 * 
	 * @param portType
	 *            The type of port
	 * @param numPorts
	 *            The number of ports
	 */
	public AbstractPerfMonTimingStartNodeModel(PortType portType, int numPorts) {
		super(createPorts(portType, numPorts), createPorts(portType, numPorts));
		m_timeOut.setEnabled(m_useTimeout.getBooleanValue());
	}

	/**
	 * @param portType
	 *            The type of port
	 * @param numPorts
	 *            The number of ports
	 * @return An array with the correct type and number of ports
	 */
	private static PortType[] createPorts(PortType portType, int numPorts) {
		PortType[] retVal = new PortType[numPorts];
		Arrays.fill(retVal, portType);
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.workflow.LoopStartNodeTerminator#terminateLoop()
	 */
	@Override
	public boolean terminateLoop() {
		// Need to check whether the iteration counter has passed the maximum or
		// whether we are using a timeout and if so, whether it hasbeen exceeded
		if (m_iteration >= m_maxIterations.getIntValue()) {
			m_logger.info("Loop terminated as all loop executions completed");
			return true;
		}

		// if (m_timeOut.isEnabled()
		// && m_timeOut.getIntValue() > m_loopEndNodeModel
		// .getRunningTotalTime()) {
		// m_logger.info("Loop terminated after " + m_iteration
		// + " iterations due to timeout being exceeded");
		// return true;
		// }
		return false;
	}

	@Override
	public Date getStartDate() {
		return m_StartTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.perfmon.PerformanceMonitoringLoopStart#
	 * hasTimeoutEnabled()
	 */
	@Override
	public boolean hasTimeoutEnabled() {
		return m_timeOut.isEnabled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.perfmon.PerformanceMonitoringLoopStart#
	 * getTimeoutDuration()
	 */
	@Override
	public int getTimeoutDuration() {
		if (m_useTimeout.isEnabled()) {
			return m_timeOut.getIntValue();
		} else {
			return -1;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#loadInternals(java.io.File,
	 * org.knime.core.node.ExecutionMonitor)
	 */
	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#saveInternals(java.io.File,
	 * org.knime.core.node.ExecutionMonitor)
	 */
	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#saveSettingsTo(org.knime.core.node.
	 * NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_maxIterations.saveSettingsTo(settings);
		m_useTimeout.saveSettingsTo(settings);
		m_timeOut.saveSettingsTo(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#validateSettings(org.knime.core.node.
	 * NodeSettingsRO)
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_maxIterations.validateSettings(settings);
		m_useTimeout.validateSettings(settings);
		m_timeOut.validateSettings(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#loadValidatedSettingsFrom(org.knime.core
	 * .node.NodeSettingsRO)
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_maxIterations.loadSettingsFrom(settings);
		m_useTimeout.loadSettingsFrom(settings);
		m_timeOut.loadSettingsFrom(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#reset()
	 */
	@Override
	protected void reset() {
		m_iteration = 0;
		m_StartTime = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#execute(org.knime.core.node.port.PortObject
	 * [], org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec)
			throws Exception {
		// if (m_loopEndNodeModel == null) {
		// NodeModel lEnd = (NodeModel) this.getLoopEndNode();
		// if (lEnd != null && lEnd instanceof PerformanceMonitoringLoopEnd) {
		// m_loopEndNodeModel = (PerformanceMonitoringLoopEnd) lEnd;
		// } else {
		// m_logger.error("Loop must end with Performance monitoring loop end node");
		// throw new Exception(
		// "Loop must end with Performance monitoring loop end node");
		// }
		// }
		// Set the start time
		m_StartTime = new Date();
		pushFlowVariableInt("currentIteration", m_iteration++);
		pushFlowVariableInt("maxIterations", m_maxIterations.getIntValue());
		// Just pass through
		return inObjects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#configure(org.knime.core.node.port.
	 * PortObjectSpec[])
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		m_iteration = 0;
		pushFlowVariableInt("currentIteration", m_iteration);
		pushFlowVariableInt("maxIterations", m_maxIterations.getIntValue());
		pushFlowVariableInt("timeout (s)",
				(m_timeOut.isEnabled()) ? m_timeOut.getIntValue() : -1);
		// Just pass through
		return inSpecs;
	}
}
