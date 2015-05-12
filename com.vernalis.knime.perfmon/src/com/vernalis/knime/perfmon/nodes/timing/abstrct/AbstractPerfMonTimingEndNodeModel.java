/*******************************************************************************
 * Copyright (c) 2015, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License, Version 3, as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.perfmon.nodes.timing.abstrct;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.RowKey;
import org.knime.core.data.date.DateAndTimeCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
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
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.FlowVariable.Scope;

import com.vernalis.knime.perfmon.PerformanceMonitoringLoopEnd;
import com.vernalis.knime.perfmon.PerformanceMonitoringLoopStart;

/**
 * This is the abstract Performance monitoring loop end node model class.
 * 
 * @author S. Roughley <s.roughley@vernalis.com>
 */
public class AbstractPerfMonTimingEndNodeModel extends NodeModel implements
		PerformanceMonitoringLoopEnd {

	/**
	 * The {@link NodeLogger} instance
	 */
	private static NodeLogger m_logger = NodeLogger
			.getLogger(AbstractPerfMonTimingEndNodeModel.class);

	/** The m_result container. */
	private BufferedDataContainer m_resultContainer = null;

	private DataTableSpec m_resultSpec;

	/** The m_current iteration. */
	private int m_currentIteration = 0;

	private double m_bestTime = Double.MAX_VALUE;
	private double m_worstTime = 0.0;
	private double m_runningTotal = 0.0;

	/**
	 * Constructor for the node model.
	 */
	public AbstractPerfMonTimingEndNodeModel(PortType portType,
			Integer numPorts) {
		super(createInputPortArray(portType, numPorts), createOutputPortArray(
				portType, numPorts));

	}

	/**
	 * Create the input PortTypes Array. This is simple an array with the given
	 * number of ports of the given type
	 */
	protected static PortType[] createInputPortArray(PortType portType,
			Integer numPorts) {
		PortType[] retVal = new PortType[numPorts];
		Arrays.fill(retVal, portType);
		return retVal;
	}

	/**
	 * Create the output {@link PortType}s Array. This is the input ports,
	 * prepended with a flow variable port and a {@link BufferedDataTable} port
	 */
	protected static PortType[] createOutputPortArray(PortType portType,
			Integer numPorts) {
		PortType[] retVal = new PortType[numPorts + 2];
		Arrays.fill(retVal, portType);
		retVal[0] = FlowVariablePortObject.TYPE;
		retVal[1] = BufferedDataTable.TYPE;
		return retVal;
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

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#reset()
	 */
	@Override
	protected void reset() {
		m_currentIteration = 0;
		m_bestTime = Double.MAX_VALUE;
		m_worstTime = 0.0;
		m_runningTotal = 0.0;
		m_resultContainer = null;
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

		// Get the loop start node - we've already checked the type in the
		// configure method
		PerformanceMonitoringLoopStart loopStartNode = (PerformanceMonitoringLoopStart) this
				.getLoopStartNode();

		// Get the end time
		Date endTime = new Date();
		// And get the start time
		Date startTime = loopStartNode.getStartDate();

		// Process the current time
		Double durationSeconds = (endTime.getTime() - startTime.getTime()) / 1000.0;
		m_runningTotal += durationSeconds;
		m_bestTime = Math.min(m_bestTime, durationSeconds);
		m_worstTime = Math.max(m_worstTime, durationSeconds);

		if (m_currentIteration == 0) {
			m_resultContainer = exec.createDataContainer(m_resultSpec);
		}



		// Log progress
		m_logger.info("Iteration " + m_currentIteration + " completed in "
				+ durationSeconds + " secs.");
		m_logger.info("Cumulative total execution time ("
				+ (++m_currentIteration)
				+ " iterations): " + m_runningTotal + " secs.");
		m_logger.info("Current Mean execution time (" + m_currentIteration
				+ " iterations): " + m_runningTotal / m_currentIteration);

		// pushFlowVariableInt("Iteration", m_currentIteration);
		DataRow newOutRow = createOutputRow(startTime, endTime, durationSeconds);
		m_resultContainer.addRowToTable(newOutRow);
		pushFlowVariableDouble("Mean Execution Time (s)", m_runningTotal
				/ (m_currentIteration));

		if (loopStartNode.terminateLoop()
				|| (loopStartNode.hasTimeoutEnabled() && loopStartNode
						.getTimeoutDuration() < m_runningTotal)) {
			// Update the logger
			if (!loopStartNode.terminateLoop()) {
				m_logger.info("Loop terminated as cumulative running time ("
						+ m_runningTotal + ") exceded timeout ("
						+ loopStartNode.getTimeoutDuration() + ")");
			}

			// Only do these on the last iteration
			pushFlowVariableString("Overall End Time", endTime.toString());
			pushFlowVariableInt("Total number of iterations",
					m_currentIteration);
			// Pass on all the variables except the global constant types and
			// the loop iteration counter
			Map<String, FlowVariable> inFlowVars = getAvailableInputFlowVariables();
			for (Entry<String, FlowVariable> flowVar : inFlowVars.entrySet()) {
				FlowVariable fvar = flowVar.getValue();
				String fvName = flowVar.getKey();
				if (fvar.getScope() == Scope.Flow
						&& !fvName.equals("Iteration")) {
					switch (fvar.getType()) {
					case DOUBLE:
						pushFlowVariableDouble(fvName, fvar.getDoubleValue());
						break;
					case INTEGER:
						pushFlowVariableInt(fvName, fvar.getIntValue());
						break;
					case STRING:
						pushFlowVariableString(fvName, fvar.getStringValue());
						break;
					default:
						break;
					}
				}
			}

			// Update the flow variables (do this last to ensure the current
			// values for these are real, and not overwritten e.g. from nested
			// or preceding loops
			pushFlowVariableString("Overall Start Time", startTime.toString());
			pushFlowVariableDouble("Last Execution time (s)", durationSeconds);
			pushFlowVariableDouble("Total Execution Time (s)", m_runningTotal);
			pushFlowVariableDouble("Best Execution Time (s)", m_bestTime);
			pushFlowVariableDouble("Worst Execution Time (s)", m_worstTime);

			m_resultContainer.close();
			BufferedDataTable table = m_resultContainer.getTable();
			// Finally, do a reset to restore the interation counter and result
			// container
			reset();
			// Just pass through the table with the added summary table
			return getOutputObjects(table, inObjects);
		} else {
			continueLoop();
			return new PortObject[inObjects.length + 2];
		}
	}

	/**
	 * @param table
	 * @param inObjects
	 * @return
	 */
	private PortObject[] getOutputObjects(BufferedDataTable table,
			PortObject[] inObjects) {
		PortObject[] retVal = new PortObject[inObjects.length + 2];
		int portIdx = 0;
		// 1st port is a flow variable port
		retVal[portIdx++] = FlowVariablePortObject.INSTANCE;

		// 2nd port is a new BDT with 4 columns to summarise loop executions
		retVal[portIdx++] = table;
		// And the rest are straight from the input
		for (; portIdx < inObjects.length + 2; portIdx++) {
			retVal[portIdx] = inObjects[portIdx - 2];
		}
		return retVal;
	}

	/**
	 * @param durationSeconds
	 * @param endTime
	 * @param startTime
	 * @return
	 */
	private DataRow createOutputRow(Date startTime, Date endTime,
			Double durationSeconds) {
		RowKey rKey = new RowKey("Row_" + m_currentIteration);
		DataCell[] rCells = new DataCell[4];
		int colIdx = 0;
		rCells[colIdx++] = new IntCell(m_currentIteration);
		rCells[colIdx++] = new DateAndTimeCell(startTime.getTime(), true, true,
				true);
		rCells[colIdx++] = new DateAndTimeCell(endTime.getTime(), true, true,
				true);
		rCells[colIdx++] = new DoubleCell(durationSeconds);
		return new DefaultRow(rKey, rCells);
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
		// Check that the loop start is valid
		NodeModel loopStart = (NodeModel) this.getLoopStartNode();
		if (loopStart != null
				&& !(loopStart instanceof PerformanceMonitoringLoopStart)) {
			throw new InvalidSettingsException(
					"Loop Start must be a 'Performance monitoring Loop Start'; "
							+ loopStart.getClass().getName()
							+ " is not a valid loop start");
		}

		// Initialise flowVar outputs
		pushFlowVariableString("Overall Start Time", "");
		pushFlowVariableString("Overall End Time", "");
		pushFlowVariableDouble("Last Execution time (s)", 0.0);
		pushFlowVariableDouble("Total Execution Time (s)", 0.0);
		pushFlowVariableDouble("Best Execution Time (s)", 0.0);
		pushFlowVariableDouble("Worst Execution Time (s)", 0.0);
		pushFlowVariableDouble("Mean Execution Time (s)", 0.0);
		pushFlowVariableInt("Total number of iterations", -1);
		// Just pass through
		return getOutputSpecs(inSpecs);
	}

	/**
	 * @param inSpecs
	 * @return
	 */
	private PortObjectSpec[] getOutputSpecs(PortObjectSpec[] inSpecs) {
		PortObjectSpec[] retVal = new PortObjectSpec[inSpecs.length + 2];
		int portIdx = 0;
		// 1st port is a flow variable port
		retVal[portIdx++] = FlowVariablePortObjectSpec.INSTANCE;

		// 2nd port is a new BDT with 4 columns to summarise loop executions
		DataTableSpecCreator summarySpecCreator = new DataTableSpecCreator();
		DataColumnSpec[] colSpecs = new DataColumnSpec[4];
		int colIdx = 0;
		colSpecs[colIdx++] = new DataColumnSpecCreator("Iteration",
				IntCell.TYPE).createSpec();
		colSpecs[colIdx++] = new DataColumnSpecCreator("Start Time",
				DateAndTimeCell.TYPE).createSpec();
		colSpecs[colIdx++] = new DataColumnSpecCreator("End Time",
				DateAndTimeCell.TYPE).createSpec();
		colSpecs[colIdx++] = new DataColumnSpecCreator("Execution Time (s)",
				DoubleCell.TYPE).createSpec();
		summarySpecCreator.addColumns(colSpecs);
		m_resultSpec = summarySpecCreator.createSpec();
		retVal[portIdx++] = m_resultSpec;
		for (; portIdx < inSpecs.length + 2; portIdx++) {
			retVal[portIdx] = inSpecs[portIdx - 2];
		}
		return retVal;
	}

	@Override
	public double getRunningTotalTime() {
		return m_runningTotal;
	}
}