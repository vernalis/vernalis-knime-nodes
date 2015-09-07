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
/**
 * 
 */
package com.vernalis.knime.flowcontrol.nodes.wait.abstrct;

import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.DateFunctions.getCurrentDate;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.DateFunctions.isNowAfter;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog.createDayModel;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog.createHourModel;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog.createMinuteModel;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog.createRunThruWeekendModel;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog.createRunToTomorrowModel;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog.createSecondModel;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
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
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;

import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.DateFunctions;
import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.TimedNodeType;

/**
 * Abtsract class for the Wait nodes. Subclasses should implement
 * {@link RunForTime} or {@link RunToTime}.
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 * 
 */
public class AbstractWaitNodeModel extends NodeModel {

	// the logger instance - not final so correct class name is used
	/** The NodeLogger instance. */
	protected NodeLogger m_logger;

	// Settings models shared by all subclasses

	/** Settings model for the hour - may be a relative or absolute!. */
	protected SettingsModelIntegerBounded m_Hour;

	/** Settings model for the minute - may be a relative or absolute!. */
	protected SettingsModelIntegerBounded m_Min = createMinuteModel();

	/** Settings model for the day - optional. */
	protected SettingsModelIntegerBounded m_Day = null;

	/** Settings model for the seconds - optional. */
	protected SettingsModelIntegerBounded m_Sec = null;

	/** Settings model for the to tomorrow option - optional. */
	protected SettingsModelBoolean m_ToTomorrow = null;

	/** Settings model for the run through weekend option - optional. */
	protected SettingsModelBoolean m_thruWeekend = null;

	/** The end time for the execution, calculated at the start of execution. */
	protected Date m_endTime;

	/** The Node Type. */
	protected TimedNodeType m_nodeType;

	/**
	 * Default constructor, generates an optional flow variable to flow variable
	 * node.
	 * 
	 * @param nodeType
	 *            the node type
	 */
	public AbstractWaitNodeModel(TimedNodeType nodeType) {
		this(new PortType[] { FlowVariablePortObject.TYPE_OPTIONAL },
				new PortType[] { FlowVariablePortObject.TYPE }, nodeType);
	}

	/**
	 * Constructor for wait nodes allowing specification of number of
	 * {@link BufferedDataTable} ports.
	 * 
	 * @param nrInDataPorts
	 *            the nr in data ports
	 * @param nrOutDataPorts
	 *            the nr out data ports
	 * @param nodeType
	 *            the node type
	 */
	public AbstractWaitNodeModel(int nrInDataPorts, int nrOutDataPorts,
			TimedNodeType nodeType) {
		super(nrInDataPorts, nrOutDataPorts);
		m_nodeType = nodeType;
		m_logger = NodeLogger.getLogger(AbstractWaitNodeModel.class);
		// Initialise the settings models
		switch (m_nodeType) {
		case RUN_FOR_TIME:
			m_Hour = createHourModel(false);
			m_Day = createDayModel();
			m_Sec = createSecondModel();
			break;
		case RUN_TO_TIME:
			m_Hour = createHourModel(true);
			m_ToTomorrow = createRunToTomorrowModel();
			m_thruWeekend = createRunThruWeekendModel();
			break;
		default:
			assert false;
		}
	}

	/**
	 * Constructor for wait nodes allowing specification of varying port types.
	 * 
	 * @param inPortTypes
	 *            the in port types
	 * @param outPortTypes
	 *            the out port types
	 * @param nodeType
	 *            the node type
	 */
	public AbstractWaitNodeModel(PortType[] inPortTypes,
			PortType[] outPortTypes, TimedNodeType nodeType) {
		super(inPortTypes, outPortTypes);
		m_nodeType = nodeType;
		m_logger = NodeLogger.getLogger(AbstractWaitNodeModel.class);
		// Initialise the settings models
		switch (m_nodeType) {
		case RUN_FOR_TIME:
			m_Hour = createHourModel(false);
			m_Day = createDayModel();
			m_Sec = createSecondModel();
			break;
		case RUN_TO_TIME:
			m_Hour = createHourModel(true);
			m_ToTomorrow = createRunToTomorrowModel();
			m_thruWeekend = createRunThruWeekendModel();
			break;
		default:
			assert false;
		}
	}

	/**
	 * <p>
	 * A default implementation of the configure method, which checks whether
	 * the end Time has already passed.
	 * </p>
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		// Calculate the endTime at config, but NB this may not be that used at
		// execution
		Date endTime = calculateEndTime();
		if (isNowAfter(endTime)) {
			// throw new
			// InvalidSettingsException("End time has already passed!");
			// Warn but dont throw exception as user may want to configure now
			// for later execution
			m_logger.warn("End time has already passed - node may fail on execution!");
			setWarningMessage("End time has already passed - node may fail on execution!");
		} else {
			m_logger.info("Node execution will complete after " + endTime);
			m_logger.info("NB This based on the current time.  "
					+ "The value will be re-calculated at execution start");
		}
		pushFlowVariableString("endTime", endTime.toString());
		return inSpecs;
	}

	/**
	 * <p>
	 * A default implementation of the configure method, which checks whether
	 * the end Time has already passed.
	 * </p>
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {

		// Calculate the endTime at config, but NB this may not be that used at
		// execution
		Date endTime = calculateEndTime();
		if (isNowAfter(endTime)) {
			// throw new
			// InvalidSettingsException("End time has already passed!");
			// Warn but dont throw exception as user may want to configure now
			// for later execution
			m_logger.warn("End time has already passed - node may fail on execution!");
			setWarningMessage("End time has already passed - node may fail on execution!");
		} else {
			m_logger.info("Node execution will complete after " + endTime);
			m_logger.info("NB This based on the current time.  "
					+ "The value will be re-calculated at execution start");
		}
		pushFlowVariableString("endTime", endTime.toString());
		return inSpecs;
	}

	/**
	 * <p>
	 * Default implementation of execute method, which waits for time to pass
	 * and then supplies input data to out port
	 * </p>
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec)
			throws Exception {
		m_endTime = calculateEndTime();
		m_logger.info("Node execution will complete after " + m_endTime);
		long milliSecToWait = m_endTime.getTime() - new Date().getTime();
		if (milliSecToWait < 0) {
			throw new Exception(
					"Node not executed as end time has already passed");

		}
		exec.setMessage("Waiting until " + m_endTime.toString());
		while (hasTimeLeft()) {
			double progress = (double) (milliSecToWait - (m_endTime.getTime() - new Date()
					.getTime())) / milliSecToWait;
			exec.checkCanceled();
			exec.setProgress(progress);
		}
		pushFlowVariableString("endTime", m_endTime.toString());
		m_logger.info("Wait node execution completed at " + getCurrentDate());
		if (inObjects[0] != null) {
			return inObjects;
		}
		return new PortObject[] { FlowVariablePortObject.INSTANCE };
	}

	/**
	 * <p>
	 * Default implementation of execute method, which waits for time to pass
	 * and then supplies input data to out port
	 * </p>
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {
		m_endTime = calculateEndTime();
		m_logger.info("Node execution will complete after " + m_endTime);
		long milliSecToWait = m_endTime.getTime() - new Date().getTime();
		exec.setMessage("Waiting until " + m_endTime.toString());
		while (hasTimeLeft()) {
			double progress = (double) (milliSecToWait - (m_endTime.getTime() - new Date()
					.getTime())) / milliSecToWait;
			exec.checkCanceled();
			exec.setProgress(progress);
		}
		pushFlowVariableString("endTime", m_endTime.toString());
		return inData;
	}

	/**
	 * Checks whether there is time remaining in the execution.
	 * 
	 * @return true, if successful
	 */
	protected boolean hasTimeLeft() {
		return m_endTime == null || !isNowAfter(m_endTime);
	}

	/**
	 * Calculates the end time based on the node type and settings.
	 * 
	 * @return The end time
	 */
	protected final Date calculateEndTime() {
		switch (m_nodeType) {
		case RUN_FOR_TIME:
			return DateFunctions.getEndTime(m_Day.getIntValue(),
					m_Hour.getIntValue(), m_Min.getIntValue(),
					m_Sec.getIntValue());
		case RUN_TO_TIME:
			return DateFunctions.getEndTime(m_Hour.getIntValue(),
					m_Min.getIntValue(), m_ToTomorrow.getBooleanValue(),
					m_thruWeekend.getBooleanValue());
		default:
			assert false;
			return null;
		}

	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation clears the endTime. It should be called by
	 * subclasses
	 * </p>
	 */
	@Override
	protected void reset() {
		m_endTime = null;
	}

	/**
	 * Over-rides default method with 'do nothing' method.
	 * 
	 * @param nodeInternDir
	 *            the node intern dir
	 * @param exec
	 *            the exec
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws CanceledExecutionException
	 *             the canceled execution exception
	 */
	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Do nothing
	}

	/**
	 * Over-rides default method with 'do nothing' method.
	 * 
	 * @param nodeInternDir
	 *            the node intern dir
	 * @param exec
	 *            the exec
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws CanceledExecutionException
	 *             the canceled execution exception
	 */
	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Do nothing
	}

	/**
	 * <p>
	 * Default implementation saves implemented settings models. Should be
	 * called in over-riding subclasses.
	 * </p>
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_Hour.saveSettingsTo(settings);
		m_Min.saveSettingsTo(settings);
		switch (m_nodeType) {
		case RUN_FOR_TIME:
			m_Day.saveSettingsTo(settings);
			m_Sec.saveSettingsTo(settings);
			break;
		case RUN_TO_TIME:
			m_ToTomorrow.saveSettingsTo(settings);
			m_thruWeekend.saveSettingsTo(settings);
			break;
		default:
			assert false;
		}

	}

	/**
	 * <p>
	 * Default implementation validates implemented settings models. Should be
	 * called in over-riding subclasses.
	 * </p>
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_Hour.validateSettings(settings);
		m_Min.validateSettings(settings);
		switch (m_nodeType) {
		case RUN_FOR_TIME:
			m_Day.validateSettings(settings);
			m_Sec.validateSettings(settings);
			break;
		case RUN_TO_TIME:
			m_ToTomorrow.validateSettings(settings);
			m_thruWeekend.validateSettings(settings);
			break;
		default:
			assert false;
		}
	}

	/**
	 * <p>
	 * Default implementation loads implemented settings models. Should be
	 * called in over-riding subclasses.
	 * </p>
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_Hour.loadSettingsFrom(settings);
		m_Min.loadSettingsFrom(settings);
		switch (m_nodeType) {
		case RUN_FOR_TIME:
			m_Day.loadSettingsFrom(settings);
			m_Sec.loadSettingsFrom(settings);
			break;
		case RUN_TO_TIME:
			m_ToTomorrow.loadSettingsFrom(settings);
			m_thruWeekend.loadSettingsFrom(settings);
			break;
		default:
			assert false;
		}
	}

}
