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
package com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart;

import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.DateFunctions.getCurrentDate;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.DateFunctions.isNowAfter;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog.createDayModel;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog.createHourModel;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog.createMinuteModel;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog.createRunThruWeekendModel;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog.createRunToTomorrowModel;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog.createSecondModel;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog.createZerothIterModel;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataContainer;
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
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.workflow.LoopStartNodeTerminator;

import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.DateFunctions;
import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.TimedNodeType;

/**
 * This is an abstract Timed Loop Start NodeModel class. In addition to the
 * Standard {@link NodeModel} implementation, default implementations of the
 * {@link #getInSpec()}, {@link #getUnprocessedRows(ExecutionContext)} and
 * {@link #terminateLoop()} methods are provided.
 * <p>
 * The required Abstract Methods from the NodeModel superclass (
 * {@link #loadInternals(File, ExecutionMonitor)} and
 * {@link #saveInternals(File, ExecutionMonitor)} are provided with default (do
 * nothing) implementations.
 * </p>
 * <p>
 * Provides a default {@link #configure(DataTableSpec[])} method which checks
 * that the endTime has not yet passed
 * </p>
 * <p>
 * Provides a default implementation of the
 * {@link #getUnprocessedRows(ExecutionContext)} method, which does not report
 * the number of rows remaining - should be over-wridden if possible!
 * </p>
 * <p>
 * The {{@link #configure(DataTableSpec[])} or {
 * {@link #configure(org.knime.core.node.port.PortObjectSpec[])} methods need to
 * set m_inspec.
 * </p>
 * 
 * @author "Stephen Roughley  knime@vernalis.com"
 * 
 */
public abstract class AbstractTimedLoopStartNodeModel extends NodeModel
		implements LoopStartNodeTerminator, TimedLoopStart {

	// the logger instance - not final so correct class name is used
	/** The NodeLogger instance. */
	protected NodeLogger m_logger;

	// Settings models shared by all subclasses
	/** Settings model for the index of the 1st iteration. */
	protected SettingsModelIntegerBounded m_ZerothIteration = createZerothIterModel();

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

	// loop invariants
	/** The input table once execution has commenced. */
	protected BufferedDataTable m_table;

	/** The iterator for the input table. */
	protected CloseableRowIterator m_iterator;

	/** The end time for the execution, calculated at the start of execution. */
	protected Date m_endTime;

	/** The input datatable spec (we will assume only 1 input table). */
	protected DataTableSpec m_inspec;

	// loop variants
	/** The interation counter. */
	protected int m_iteration;

	/** The Node Type. */
	protected TimedNodeType m_nodeType;

	/**
	 * Creates a new model with the given number of input and output data ports.
	 * Initialises the ChunkSize Model
	 * 
	 * @param nrInDataPorts
	 *            number of input data ports
	 * @param nrOutDataPorts
	 *            number of output data ports
	 * @param nodeType
	 *            'Run-to-time' or 'Run-for-time' node
	 */
	public AbstractTimedLoopStartNodeModel(int nrInDataPorts,
			int nrOutDataPorts, TimedNodeType nodeType) {
		super(nrInDataPorts, nrOutDataPorts);
		m_nodeType = nodeType;
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
	 * Creates a new model with the given number (and types!) of input and
	 * output types.
	 * 
	 * @param inPortTypes
	 *            an array of non-null in-port types
	 * @param outPortTypes
	 *            an array of non-null out-port types
	 * @param nodeType
	 *            'Run-to-time' or 'Run-for-time' node
	 */
	public AbstractTimedLoopStartNodeModel(PortType[] inPortTypes,
			PortType[] outPortTypes, TimedNodeType nodeType) {
		super(inPortTypes, outPortTypes);
		m_nodeType = nodeType;
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
		m_iteration = m_ZerothIteration.getIntValue();
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
			m_logger.info("Loop execution will terminate after " + endTime);
			m_logger.info("NB This based on the current time.  "
					+ "The value will be re-calculated at execution start");
		}
		pushFlowVariableInt("currentIteration", m_iteration);
		pushFlowVariableInt("maxIterations", 0);
		pushFlowVariableString("endTime", endTime.toString());
		m_inspec = inSpecs[0];
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
		m_iteration = m_ZerothIteration.getIntValue();
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
			m_logger.info("Loop execution will terminate after " + endTime);
			m_logger.info("NB This based on the current time.  "
					+ "The value will be re-calculated at execution start");
		}
		pushFlowVariableInt("currentIteration", m_iteration);
		pushFlowVariableInt("maxIterations", 0);
		pushFlowVariableString("endTime", endTime.toString());
		m_inspec = (DataTableSpec) inSpecs[0];
		return inSpecs;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The default implementation returns the unprocessed rows, but is unaware
	 * of how many there might be. This should be over-ridden in subclasses if
	 * at all possible! Provided for backwards compatibility only.
	 * </p>
	 */
	@Override
	public BufferedDataTable getUnprocessedRows(ExecutionContext exec)
			throws CanceledExecutionException {
		BufferedDataContainer cont = exec.createDataContainer(m_table
				.getDataTableSpec());
		exec.setMessage("Retrieving unprocessed rows...");
		int rowcnt = 0;
		while (m_iterator.hasNext()) {
			exec.setProgress("Retrieving unprocessed row " + rowcnt++);
			exec.checkCanceled();
			cont.addRowToTable(m_iterator.next());
		}
		cont.close();
		return cont.getTable();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The default implementation returns the value in {@link #m_inspec}, which
	 * should be set in the {@link #configure} method
	 * </p>
	 */
	@Override
	public DataTableSpec getInSpec() {
		// The default implementation, returns the value in m_inspec
		return m_inspec;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The default implementation checks whether there are any rows remaining
	 * and any time left
	 */
	@Override
	public boolean terminateLoop() {
		boolean moreRows = m_iterator == null || m_iterator.hasNext();
		boolean stillTimeLeft = hasTimeLeft();
		if (!moreRows) {
			m_logger.info("Loop execution completed at " + getCurrentDate());
		}
		if (!stillTimeLeft) {
			m_logger.warn("Loop terminated at " + getCurrentDate()
					+ " as end time passed");
		}
		return !(moreRows && stillTimeLeft);
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getEndTime() {
		return m_endTime;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getIteration() {
		return m_iteration;
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
	 * This default implementation clears the iterator, table, endTime and
	 * iteration counters. It should be called by subclasses
	 * </p>
	 */
	@Override
	protected void reset() {
		m_iteration = 0;// We will overwrite this with the 0th value on
						// configure or execute, but we maynot know it here
		if (m_iterator != null) {
			m_iterator.close();
		}
		m_iterator = null;
		m_table = null;
		m_endTime = null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Default 'do nothing' implementation
	 * </p>
	 */
	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Do nothing

	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Default 'do nothing' implementation
	 * </p>
	 */
	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Do nothing

	}

	/**
	 * <p>
	 * Default implementation, which saves settings provided by abstract
	 * class(es). It should be called by subclasses
	 * </p>
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_Hour.saveSettingsTo(settings);
		m_Min.saveSettingsTo(settings);
		m_ZerothIteration.saveSettingsTo(settings);
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
	 * Default implementation, which validates settings provided by abstract
	 * class(es). It should be called by subclasses
	 * </p>
	 * {@inheritDoc}
	 * 
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_Hour.validateSettings(settings);
		m_Min.validateSettings(settings);
		m_ZerothIteration.validateSettings(settings);
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
	 * Default implementation, which loads settings provided by abstract
	 * class(es). It should be called by subclasses
	 * </p>
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_Hour.loadSettingsFrom(settings);
		m_Min.loadSettingsFrom(settings);
		m_ZerothIteration.loadSettingsFrom(settings);
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
