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

import static com.vernalis.knime.perfmon.nodes.timing.abstrct.AbstractPerfMonTimingEndNodeDialog.createUseLegacyDateTimeFieldsModel;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.RowKey;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.date.DateAndTimeCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.time.zoneddatetime.ZonedDateTimeCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeFactory.NodeType;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.FlowVariable.Scope;
import org.knime.core.node.workflow.NodeContainer;
import org.knime.core.node.workflow.NodeContext;
import org.knime.core.node.workflow.SingleNodeContainer;
import org.knime.core.node.workflow.SubNodeContainer;
import org.knime.core.node.workflow.WorkflowManager;

import com.vernalis.knime.perfmon.PerformanceMonitoringLoopEnd;
import com.vernalis.knime.perfmon.PerformanceMonitoringLoopStart;

/**
 * This is the abstract Performance monitoring loop end node model class.
 *
 * v1.27.0 - Updated to allow optional use of new Date-Time types
 *
 * @author S. Roughley knime@vernalis.com
 */
@SuppressWarnings("deprecation")
public class AbstractPerfMonTimingEndNodeModel extends NodeModel implements PerformanceMonitoringLoopEnd {

	protected final SettingsModelBoolean useLegacyDateTimeMdl = createUseLegacyDateTimeFieldsModel();
	/**
	 * Separator for sub-nodes/metanodes in the optional loop body timings output
	 */
	private static final String WORKFLOW_PATH_SEPARATOR = " --> ";

	/**
	 * The {@link NodeLogger} instance
	 */
	protected static NodeLogger m_logger = NodeLogger.getLogger(AbstractPerfMonTimingEndNodeModel.class);

	/** The m_result container. */
	protected BufferedDataContainer m_resultContainer = null;

	protected DataTableSpec m_resultSpec;

	/** The m_current iteration. */
	protected int m_currentIteration = 0;

	protected double m_bestTime = Double.MAX_VALUE;
	protected double m_worstTime = 0.0;
	protected double m_runningTotal = 0.0;

	/**
	 * Constructor for the node model.
	 */
	public AbstractPerfMonTimingEndNodeModel(PortType portType, Integer numPorts) {
		super(createInputPortArray(portType, numPorts), createOutputPortArray(portType, numPorts));

	}

	/**
	 * Constructor for subtypes to supply their own port types
	 */
	protected AbstractPerfMonTimingEndNodeModel(PortType[] inTypes, PortType[] outTypes) {
		super(inTypes, outTypes);
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
		final PortType[] retVal = new PortType[numPorts + 2];
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
		useLegacyDateTimeMdl.saveSettingsTo(settings);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.knime.core.node.NodeModel#validateSettings(org.knime.core.node.
	 * NodeSettingsRO)
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// Don't validate date-time format setting for backwards compatibility
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.knime.core.node.NodeModel#loadValidatedSettingsFrom(org.knime.core
	 * .node.NodeSettingsRO)
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		try {
			useLegacyDateTimeMdl.loadSettingsFrom(settings);
		} catch (final InvalidSettingsException e) {
			// Legacy behaviour
			useLegacyDateTimeMdl.setBooleanValue(true);
		}
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
	 * org.knime.core.node.NodeModel#execute(org.knime.core.node.port.PortObject [],
	 * org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {

		// Get the loop start node - we've already checked the type in the
		// configure method
		final PerformanceMonitoringLoopStart loopStartNode = (PerformanceMonitoringLoopStart) this.getLoopStartNode();

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
		}

		// Log progress
		m_logger.info(
				"Iteration " + m_currentIteration + " completed in " + String.format("%.3f secs.", durationSeconds));
		m_logger.info("Cumulative total execution time (" + (++m_currentIteration) + " iterations): "
				+ String.format("%.3f secs.", m_runningTotal));
		m_logger.info("Current Mean execution time (" + m_currentIteration + " iterations): "
				+ String.format("%.3f secs.", m_runningTotal / m_currentIteration));

		// pushFlowVariableInt("Iteration", m_currentIteration);
		final DataRow newOutRow = createOutputRow(startTime, endTime, durationSeconds,
				loopStartNode.getReportNodeTimes(), loopStartNode.getProbeSubnodeTimes());
		m_resultContainer.addRowToTable(newOutRow);
		pushFlowVariableDouble("Mean Execution Time (s)", m_runningTotal / m_currentIteration);

		if (loopStartNode.terminateLoop()
				|| loopStartNode.hasTimeoutEnabled() && loopStartNode.getTimeoutDuration() < m_runningTotal) {
			// Update the logger
			if (!loopStartNode.terminateLoop()) {
				m_logger.info("Loop terminated as cumulative running time (" + m_runningTotal + ") exceded timeout ("
						+ loopStartNode.getTimeoutDuration() + ")");
			}

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
	 * @throws Exception
	 */
	protected PortObject[] getOutputObjects(BufferedDataTable table, PortObject[] inObjects) throws Exception {
		final PortObject[] retVal = new PortObject[inObjects.length + 2];
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
	 * Method to generate the output row
	 *
	 * @param startTime       The start time of the current loop iteration
	 * @param endTime         The end time of the current loop iteration
	 * @param durationSeconds The duration of the loop iteration in seconds
	 * @param reportNodeTimes Should individual node times in the loop body be
	 *                        reported?
	 * @param probeSubnodes   Should the node times within subnodes (i.e. wrapped
	 *                        metanodes) be reported?
	 * @return The {@link DataRow} for the current loop iteration for the timing
	 *         table
	 */
	protected DataRow createOutputRow(Date startTime, Date endTime, Double durationSeconds, boolean reportNodeTimes,
			boolean probeSubnodes) {
		final RowKey rKey = new RowKey("Row_" + m_currentIteration);
		final DataCell[] rCells = new DataCell[4 + (reportNodeTimes ? 2 : 0)];
		int colIdx = 0;
		rCells[colIdx++] = new IntCell(m_currentIteration);

		rCells[colIdx++] = useLegacyDateTimeMdl.getBooleanValue()
				? new DateAndTimeCell(startTime.getTime(), true, true, true)
						: ZonedDateTimeCellFactory
						.create(Instant.ofEpochMilli(startTime.getTime()).atZone(ZoneId.systemDefault()));
				rCells[colIdx++] = useLegacyDateTimeMdl.getBooleanValue()
						? new DateAndTimeCell(endTime.getTime(), true, true, true)
								: ZonedDateTimeCellFactory
								.create(Instant.ofEpochMilli(endTime.getTime()).atZone(ZoneId.systemDefault()));
						rCells[colIdx++] = new DoubleCell(durationSeconds);

						if (reportNodeTimes) {
							final NodeContext ctx = NodeContext.getContext();
							final WorkflowManager wfm = ctx.getWorkflowManager();
							List<NodeContainer> loop = wfm.getNodesInScope((SingleNodeContainer) ctx.getNodeContainer());
							// Remove the loop start/end
							loop = loop.subList(1, loop.size() - 1);

							final Map<String, Long> times = new LinkedHashMap<>();
							for (final NodeContainer cont : loop) {
								// System.out.println(cont.getNameWithID() + ":\t"
								// + cont.getClass().getCanonicalName());
								if (cont instanceof WorkflowManager) {
									// metanode
									times(cont.getNameWithID(), (WorkflowManager) cont, probeSubnodes, times);
								} else {
									times.put(cont.getNameWithID(), cont.getNodeTimer().getLastExecutionDuration());
									if (probeSubnodes && cont instanceof SubNodeContainer) {
										times(cont.getNameWithID(), (SubNodeContainer) cont, probeSubnodes, times);
									}
								}
							}

							rCells[colIdx++] = CollectionCellFactory
									.createListCell(times.keySet().stream().map(x -> new StringCell(x)).collect(Collectors.toList()));
							rCells[colIdx++] = CollectionCellFactory
									.createListCell(times.values().stream().map(x -> new LongCell(x)).collect(Collectors.toList()));
						}
						return new DefaultRow(rKey, rCells);
	}

	/**
	 * Get the times for a {@link WorkflowManager} (i.e. metanode)
	 *
	 * @param prefix        The path to the metanode
	 * @param wfm           The {@link WorkflowManager} representing the metanode
	 * @param probeSubnodes Should the contents of any contained wrapped metanode /
	 *                      subnode be probed for detailed timings?
	 * @param times         The map of node names - times
	 */
	protected void times(String prefix, WorkflowManager wfm, boolean probeSubnodes, Map<String, Long> times) {
		for (final NodeContainer cont : wfm.getNodeContainers()) {
			if (cont instanceof WorkflowManager) {
				// metanode - recurse
				times(prefix + WORKFLOW_PATH_SEPARATOR + cont.getNameWithID(), (WorkflowManager) cont, probeSubnodes,
						times);
			} else {
				times.put(prefix + WORKFLOW_PATH_SEPARATOR + cont.getNameWithID(),
						cont.getNodeTimer().getLastExecutionDuration());
				if (probeSubnodes && cont instanceof SubNodeContainer) {
					final SubNodeContainer snc = (SubNodeContainer) cont;
					// System.out.println(snc.getNameWithID() + "\t::\t"
					// + snc.getNodeContainers());
					times(prefix + WORKFLOW_PATH_SEPARATOR + snc.getNameWithID(), snc, probeSubnodes, times);
				}
			}
		}
	}

	/**
	 * Get the times for a {@link SubNodeContainer} (i.e. wrapped metanode or
	 * subnode)
	 *
	 * @param prefix        The path to the metanode
	 * @param snc           The {@link SubNodeContainer} representing the metanode
	 * @param probeSubnodes Should the contents of any contained wrapped metanode /
	 *                      subnode be probed for detailed timings?
	 * @param times         The map of node names - times
	 */
	protected void times(String prefix, SubNodeContainer snc, boolean probeSubnodes, Map<String, Long> times) {
		for (final NodeContainer cont : snc.getNodeContainers()) {
			if (cont.getType() == NodeType.VirtualIn || cont.getType() == NodeType.VirtualOut) {
				continue;
			}
			if (cont instanceof WorkflowManager) {
				// metanode - recurse
				times(prefix + WORKFLOW_PATH_SEPARATOR + cont.getNameWithID(), (WorkflowManager) cont, probeSubnodes,
						times);
			} else {
				times.put(prefix + WORKFLOW_PATH_SEPARATOR + cont.getNameWithID(),
						cont.getNodeTimer().getLastExecutionDuration());
				if (probeSubnodes && cont instanceof SubNodeContainer) {
					final SubNodeContainer snc1 = (SubNodeContainer) cont;
					// System.out.println(snc.getNameWithID() + "\t::\t"
					// + snc.getNodeContainers());
					times(prefix + WORKFLOW_PATH_SEPARATOR + snc1.getNameWithID(), snc1, probeSubnodes, times);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.knime.core.node.NodeModel#configure(org.knime.core.node.port.
	 * PortObjectSpec[])
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		// Check that the loop start is valid
		final NodeModel loopStart = (NodeModel) this.getLoopStartNode();
		if (loopStart == null) {
			throw new InvalidSettingsException(
					"This loop needs a corresponding 'Performance monitoring loop start node' - non found!");
		} else if (!(loopStart instanceof PerformanceMonitoringLoopStart)) {
			throw new InvalidSettingsException("Loop Start must be a 'Performance monitoring Loop Start'; "
					+ loopStart.getClass().getSimpleName() + " is not a valid loop start");
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
		return getOutputSpecs(inSpecs, ((PerformanceMonitoringLoopStart) loopStart).getReportNodeTimes());
	}

	/**
	 * Method to return the output port specs
	 *
	 * @param inSpecs         The incoming port specs
	 * @param reportNodeTimes Should individual node execution times in the loop
	 *                        body be reported?
	 * @return The output port specs
	 */
	protected PortObjectSpec[] getOutputSpecs(PortObjectSpec[] inSpecs, boolean reportNodeTimes) {
		final PortObjectSpec[] retVal = new PortObjectSpec[inSpecs.length + 2];
		int portIdx = 0;
		// 1st port is a flow variable port
		retVal[portIdx++] = FlowVariablePortObjectSpec.INSTANCE;

		// 2nd port is a new BDT with 4 columns to summarise loop executions
		final DataTableSpecCreator summarySpecCreator = new DataTableSpecCreator();
		final DataColumnSpec[] colSpecs = new DataColumnSpec[4 + (reportNodeTimes ? 2 : 0)];
		int colIdx = 0;
		colSpecs[colIdx++] = new DataColumnSpecCreator("Iteration", IntCell.TYPE).createSpec();
		colSpecs[colIdx++] = new DataColumnSpecCreator("Start Time",
				useLegacyDateTimeMdl.getBooleanValue() ? DateAndTimeCell.TYPE : ZonedDateTimeCellFactory.TYPE)
				.createSpec();
		colSpecs[colIdx++] = new DataColumnSpecCreator("End Time",
				useLegacyDateTimeMdl.getBooleanValue() ? DateAndTimeCell.TYPE : ZonedDateTimeCellFactory.TYPE)
				.createSpec();
		colSpecs[colIdx++] = new DataColumnSpecCreator("Execution Time (s)", DoubleCell.TYPE).createSpec();
		if (reportNodeTimes) {
			colSpecs[colIdx++] = new DataColumnSpecCreator("Node name / ID",
					ListCell.getCollectionType(StringCell.TYPE)).createSpec();
			colSpecs[colIdx++] = new DataColumnSpecCreator("Node Execution time (ms)",
					ListCell.getCollectionType(LongCell.TYPE)).createSpec();
		}
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
