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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.FlowVariable.Scope;

import com.vernalis.knime.perfmon.MemoryPerformanceMonitoringLoopEnd;
import com.vernalis.knime.perfmon.MemoryPerformanceMonitoringLoopStart;

/**
 * Node model for memory monitoring benchmarking loop ends
 * 
 * @author s.roughley
 *
 */
public class AbstractMemMonPerfLoopEndNodeModel
		extends AbstractPerfMonTimingEndNodeModel
		implements MemoryPerformanceMonitoringLoopEnd {

	/**
	 * @param portType
	 *            The type of port to use
	 * @param numPorts
	 *            The number of ports
	 */
	public AbstractMemMonPerfLoopEndNodeModel(PortType portType,
			Integer numPorts) {
		super(createInputPortArray(portType, numPorts),
				createOutputPortArray(portType, numPorts));
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
		PortType[] retVal = new PortType[numPorts + 3];
		Arrays.fill(retVal, portType);
		retVal[0] = FlowVariablePortObject.TYPE;
		retVal[1] = BufferedDataTable.TYPE;
		retVal[2] = BufferedDataTable.TYPE;
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.perfmon.nodes.timing.abstrct.
	 * AbstractPerfMonTimingEndNodeModel
	 * #execute(org.knime.core.node.port.PortObject[],
	 * org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {

		// Get the loop start node - we've already checked the type in the
		// configure method
		MemoryPerformanceMonitoringLoopStart loopStartNode =
				(MemoryPerformanceMonitoringLoopStart) this.getLoopStartNode();

		// Get the end time
		Date endTime = new Date();
		// And get the start time
		Date startTime = loopStartNode.getStartDate();

		// Process the current time
		Double durationSeconds =
				(endTime.getTime() - startTime.getTime()) / 1000.0;
		m_runningTotal += durationSeconds;
		m_bestTime = Math.min(m_bestTime, durationSeconds);
		m_worstTime = Math.max(m_worstTime, durationSeconds);

		if (m_currentIteration == 0) {
			m_resultContainer = exec.createDataContainer(m_resultSpec);
		}

		// Log progress
		m_logger.info("Iteration " + m_currentIteration + " completed in "
				+ String.format("%.3f secs.", durationSeconds));
		m_logger.info("Cumulative total execution time ("
				+ (++m_currentIteration) + " iterations): "
				+ String.format("%.3f secs.", m_runningTotal));
		m_logger.info("Current Mean execution time (" + m_currentIteration
				+ " iterations): " + String.format("%.3f secs.",
						m_runningTotal / m_currentIteration));

		// pushFlowVariableInt("Iteration", m_currentIteration);
		DataRow newOutRow = createOutputRow(startTime, endTime, durationSeconds,
				loopStartNode.getReportNodeTimes(),
				loopStartNode.getProbeSubnodeTimes());
		m_resultContainer.addRowToTable(newOutRow);
		pushFlowVariableDouble("Mean Execution Time (s)",
				m_runningTotal / (m_currentIteration));

		if (loopStartNode.terminateLoop() || (loopStartNode.hasTimeoutEnabled()
				&& loopStartNode.getTimeoutDuration() < m_runningTotal)) {
			// Update the logger
			if (!loopStartNode.terminateLoop()) {
				m_logger.info("Loop terminated as cumulative running time ("
						+ m_runningTotal + ") exceded timeout ("
						+ loopStartNode.getTimeoutDuration() + ")");
			}

			// We need to tell the loop start node to stop monitoring.
			loopStartNode.stopMonitoring();

			// Only do these on the last iteration
			pushFlowVariableString("Overall End Time", endTime.toString());
			pushFlowVariableInt("Total number of iterations",
					m_currentIteration);

			// Pass on all the variables except the global constant types and
			// the loop iteration counter
			Map<String, FlowVariable> inFlowVars =
					getAvailableInputFlowVariables();
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
			pushFlowVariableDouble("Mean Execution Time (s)",
					m_runningTotal / m_currentIteration);

			m_resultContainer.close();
			BufferedDataTable table = m_resultContainer.getTable();
			// Finally, do a reset to restore the interation counter and result
			// container
			reset();
			// Just pass through the table with the added summary table
			return getOutputObjects(table, inObjects);
		} else {
			continueLoop();
			return new PortObject[inObjects.length + 3];
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.perfmon.nodes.timing.abstrct.
	 * AbstractPerfMonTimingEndNodeModel
	 * #configure(org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		// Check that the loop start is valid
		NodeModel loopStart = (NodeModel) this.getLoopStartNode();
		if (loopStart != null
				&& !(loopStart instanceof MemoryPerformanceMonitoringLoopStart)) {
			throw new InvalidSettingsException(
					"Loop Start must be a 'Memory Performance monitoring Loop Start'; "
							+ loopStart.getClass().getSimpleName()
							+ " is not a valid loop start");
		}
		return super.configure(inSpecs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.perfmon.nodes.timing.abstrct.
	 * AbstractPerfMonTimingEndNodeModel
	 * #getOutputObjects(org.knime.core.node.BufferedDataTable,
	 * org.knime.core.node.port.PortObject[])
	 */
	@Override
	protected PortObject[] getOutputObjects(BufferedDataTable table,
			PortObject[] inObjects) throws Exception {
		List<PortObject> retVal = new ArrayList<>(
				Arrays.asList(super.getOutputObjects(table, inObjects)));
		retVal.add(2,
				((MemoryPerformanceMonitoringLoopStart) getLoopStartNode())
						.getTimingTable());

		return retVal.toArray(new PortObject[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.perfmon.nodes.timing.abstrct.
	 * AbstractPerfMonTimingEndNodeModel
	 * #getOutputSpecs(org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	protected PortObjectSpec[] getOutputSpecs(PortObjectSpec[] inSpecs,
			boolean reportNodeTimes) {
		List<PortObjectSpec> retVal = new ArrayList<>(
				Arrays.asList(super.getOutputSpecs(inSpecs, reportNodeTimes)));
		retVal.add(2,
				((MemoryPerformanceMonitoringLoopStart) getLoopStartNode())
						.createMonitorTableSpec());
		return retVal.toArray(new PortObjectSpec[0]);
	}

}
