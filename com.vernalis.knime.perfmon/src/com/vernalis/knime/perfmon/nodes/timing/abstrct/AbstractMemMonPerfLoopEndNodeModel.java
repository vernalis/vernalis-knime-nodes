/*******************************************************************************
 * Copyright (c) 2016, 2020 Vernalis (R&D) Ltd
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

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.append.AppendedColumnRow;
import org.knime.core.data.date.DateAndTimeCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.time.duration.DurationCellFactory;
import org.knime.core.data.time.zoneddatetime.ZonedDateTimeCellFactory;
import org.knime.core.node.BufferedDataContainer;
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

import com.vernalis.knime.perfmon.KnowsIterationMemoryMonitoringResult;
import com.vernalis.knime.perfmon.MemoryMonitoringResult;
import com.vernalis.knime.perfmon.MemoryMonitoringResultImpl;
import com.vernalis.knime.perfmon.MemoryPerformanceMonitoringLoopEnd;
import com.vernalis.knime.perfmon.MemoryPerformanceMonitoringLoopStart;

/**
 * Node model for memory monitoring benchmarking loop ends
 *
 * Updated v 1.27.0 to allow optional use of new date-time types and to avoid
 * KNIME 4.2.0 asynchronous table writing issues
 *
 * @author s.roughley
 *
 */
@SuppressWarnings("deprecation")
public class AbstractMemMonPerfLoopEndNodeModel extends AbstractPerfMonTimingEndNodeModel
implements MemoryPerformanceMonitoringLoopEnd {

	/** The m_result container. */
	protected BufferedDataContainer memUseTable = null;

	protected DataTableSpec memUseSpec = null;
	protected long memUseRowId = 0;

	/**
	 * @param portType The type of port to use
	 * @param numPorts The number of ports
	 */
	public AbstractMemMonPerfLoopEndNodeModel(PortType portType, Integer numPorts) {
		super(createInputPortArray(portType, numPorts), createOutputPortArray(portType, numPorts));
	}

	/**
	 * Create the input PortTypes Array. This is simple an array with the given
	 * number of ports of the given type
	 */
	protected static PortType[] createInputPortArray(PortType portType, Integer numPorts) {
		final PortType[] retVal = new PortType[numPorts];
		Arrays.fill(retVal, portType);
		return retVal;
	}

	/**
	 * Create the output {@link PortType}s Array. This is the input ports, prepended
	 * with a flow variable port and a {@link BufferedDataTable} port
	 */
	protected static PortType[] createOutputPortArray(PortType portType, Integer numPorts) {
		final PortType[] retVal = new PortType[numPorts + 3];
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
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {

		// Get the loop start node - we've already checked the type in the
		// configure method
		final MemoryPerformanceMonitoringLoopStart<?> loopStartNode = (MemoryPerformanceMonitoringLoopStart<?>) this
				.getLoopStartNode();
		final Iterable<? extends MemoryMonitoringResult> iterationMemoryUsage = loopStartNode.getIterationMemoryUsage();

		// Get the end time
		final Date endTime = new Date();
		// And get the start time
		final Date startTime = loopStartNode.getStartDate();

		// Process the current time
		final Double durationSeconds = (endTime.getTime() - startTime.getTime()) / 1000.0;
		m_runningTotal += durationSeconds;
		m_bestTime = Math.min(m_bestTime, durationSeconds);
		m_worstTime = Math.max(m_worstTime, durationSeconds);

		if (m_currentIteration == 0) {
			m_resultContainer = exec.createDataContainer(m_resultSpec);
			memUseTable = exec.createDataContainer(memUseSpec);
			memUseRowId = 0;
		}

		// Add the iteration to the summary table
		m_resultContainer.addRowToTable(createOutputRow(startTime, endTime, durationSeconds,
				loopStartNode.getReportNodeTimes(), loopStartNode.getProbeSubnodeTimes()));

		// Add the latest iteration rows to the memUseTable
		for (final MemoryMonitoringResult memuse : iterationMemoryUsage) {
			final DataCell[] cells = new DataCell[3];
			Arrays.fill(cells, DataType.getMissingCell());
			int colIdx = 0;
			final long baseTime = memuse instanceof KnowsIterationMemoryMonitoringResult
					? ((KnowsIterationMemoryMonitoringResult) memuse).getBaseTime()
							: loopStartNode.getIterationStartTime();
					cells[colIdx++] = useLegacyDateTimeMdl.getBooleanValue()
							? new DateAndTimeCell(memuse.getTimestamp(), true, true, true)
									: ZonedDateTimeCellFactory
									.create(Instant.ofEpochMilli(memuse.getTimestamp()).atZone(ZoneId.systemDefault()));
							cells[colIdx++] = useLegacyDateTimeMdl.getBooleanValue()
									? new DateAndTimeCell(memuse.getTimestamp() - baseTime, false, true, true)
											: DurationCellFactory.create(Duration.ofMillis(memuse.getTimestamp() - baseTime));
									cells[colIdx++] = new IntCell(memuse instanceof KnowsIterationMemoryMonitoringResult
											? ((KnowsIterationMemoryMonitoringResult) memuse).getIteration()
													: m_currentIteration);
									DataRow row = new DefaultRow(RowKey.createRowKey(memUseRowId++), cells);
									row = new AppendedColumnRow(row, memuse.getDataCells());
									memUseTable.addRowToTable(row);
		}

		// Log progress
		m_logger.info(
				"Iteration " + m_currentIteration + " completed in " + String.format("%.3f secs.", durationSeconds));
		m_logger.info("Cumulative total execution time (" + (++m_currentIteration) + " iterations): "
				+ String.format("%.3f secs.", m_runningTotal));
		m_logger.info("Current Mean execution time (" + m_currentIteration + " iterations): "
				+ String.format("%.3f secs.", m_runningTotal / m_currentIteration));
		pushFlowVariableDouble("Mean Execution Time (s)", m_runningTotal / m_currentIteration);

		if (loopStartNode.terminateLoop()
				|| loopStartNode.hasTimeoutEnabled() && loopStartNode.getTimeoutDuration() < m_runningTotal) {
			// Update the logger
			if (!loopStartNode.terminateLoop()) {
				m_logger.info("Loop terminated as cumulative running time (" + m_runningTotal + ") exceded timeout ("
						+ loopStartNode.getTimeoutDuration() + ")");
			}

			// We need to tell the loop start node to stop monitoring.
			loopStartNode.stopMonitoring();

			// Only do these on the last iteration
			pushFlowVariableString("Overall End Time", endTime.toString());
			pushFlowVariableInt("Total number of iterations", m_currentIteration);

			// Pass on all the variables except the global constant types and
			// the loop iteration counter
			final Map<String, FlowVariable> inFlowVars = getAvailableInputFlowVariables();
			for (final Entry<String, FlowVariable> flowVar : inFlowVars.entrySet()) {
				final FlowVariable fvar = flowVar.getValue();
				final String fvName = flowVar.getKey();
				if (fvar.getScope() == Scope.Flow && !fvName.equals("Iteration")) {
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
			pushFlowVariableDouble("Mean Execution Time (s)", m_runningTotal / m_currentIteration);

			m_resultContainer.close();
			final BufferedDataTable table = m_resultContainer.getTable();
			// Finally, do a reset to restore the interation counter and result
			// container
			reset();
			// Just pass through the table with the added summary table
			final List<PortObject> retVal = new ArrayList<>(Arrays.asList(super.getOutputObjects(table, inObjects)));
			memUseTable.close();

			retVal.add(2, memUseTable.getTable());

			return retVal.toArray(new PortObject[0]);
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
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		// Check that the loop start is valid
		final NodeModel loopStart = (NodeModel) this.getLoopStartNode();
		if (loopStart != null && !(loopStart instanceof MemoryPerformanceMonitoringLoopStart)) {
			throw new InvalidSettingsException("Loop Start must be a 'Memory Performance monitoring Loop Start'; "
					+ loopStart.getClass().getSimpleName() + " is not a valid loop start");
		}
		return super.configure(inSpecs);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.vernalis.knime.internal.perfmon.nodes.timing.abstrct.
	 * AbstractPerfMonTimingEndNodeModel
	 * #getOutputSpecs(org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	protected PortObjectSpec[] getOutputSpecs(PortObjectSpec[] inSpecs, boolean reportNodeTimes) {
		final List<PortObjectSpec> retVal = new ArrayList<>(
				Arrays.asList(super.getOutputSpecs(inSpecs, reportNodeTimes)));
		memUseSpec = createMemoryUseTableSpec();
		retVal.add(2, memUseSpec);
		return retVal.toArray(new PortObjectSpec[0]);
	}

	protected DataTableSpec createMemoryUseTableSpec() {
		final DataTableSpecCreator specFact = new DataTableSpecCreator();

		specFact.addColumns(new DataColumnSpecCreator("Date and Time",
				useLegacyDateTimeMdl.getBooleanValue() ? DateAndTimeCell.TYPE : ZonedDateTimeCellFactory.TYPE)
				.createSpec());
		specFact.addColumns(new DataColumnSpecCreator("Time since start of iteratoin",
				useLegacyDateTimeMdl.getBooleanValue() ? DateAndTimeCell.TYPE : DurationCellFactory.TYPE).createSpec());
		specFact.addColumns(new DataColumnSpecCreator("Iteration", IntCell.TYPE).createSpec());
		specFact.addColumns(MemoryMonitoringResultImpl.getTableSpec());
		return specFact.createSpec();
	}

}
