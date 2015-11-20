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
package com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart;

import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.DateFunctions.isNowAfter;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog.createChunkSizeModel;

import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.DateFunctions;
import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.TimedNodeType;

/**
 * <p>
 * Extension of the {@link AbstractTimedLoopStartNodeModel} class which provides
 * extra settings and method implementations for chunked timed loop start nodes.
 * </p>
 * <p>
 * Subclasses should implement {@link RunForTime} or {@link RunToTime}.
 * </p>
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 * 
 */
public class AbstractChunkTimedLoopStartNodeModel extends AbstractTimedLoopStartNodeModel {

	/** The number of rows to be processed in each iteration. */
	protected SettingsModelIntegerBounded m_chunkSize;

	/**
	 * The only constructor for a Chunk loop start, provides 1 in and 1 out data
	 * table port.
	 * 
	 * @param nodeType
	 *            Run-to-time ot Run-for-time
	 */
	public AbstractChunkTimedLoopStartNodeModel(TimedNodeType nodeType) {
		super(1, 1, nodeType);
		m_chunkSize = createChunkSizeModel();
		m_logger = NodeLogger.getLogger(AbstractChunkTimedLoopStartNodeModel.class);
	}

	/**
	 * <p>
	 * The implementation for chunked loops, returns the unprocessed rows and
	 * also provides a progress counter.
	 * </p>
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public BufferedDataTable getUnprocessedRows(ExecutionContext exec)
			throws CanceledExecutionException {
		BufferedDataContainer cont = exec.createDataContainer(m_table.getDataTableSpec());
		exec.setMessage("Retrieving unprocessed rows...");
		long rowsToRetrieve = m_table.size() - (m_chunkSize.getIntValue() * (m_iteration));
		long rowcnt = 0;
		while (m_iterator.hasNext()) {
			exec.setProgress((double) rowcnt / rowsToRetrieve,
					"Retrieving unprocessed row " + ++rowcnt + " of " + rowsToRetrieve);
			exec.checkCanceled();
			cont.addRowToTable(m_iterator.next());
		}
		cont.close();
		m_logger.info("Loop execution completed at " + DateFunctions.getCurrentDate().toString());
		return cont.getTable();
	}

	/**
	 * <p>
	 * Default implementation assumes a standard chunk loop start is being
	 * implemented.
	 * </p>
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		BufferedDataTable table = inData[0];
		long rowCount = table.size();
		int rowsPerChunk = m_chunkSize.getIntValue();
		int totalChunkCount = (int) Math.ceil(rowCount / (double) rowsPerChunk);

		if (m_iteration == m_ZerothIteration.getIntValue()) {
			// First iteration - set the end time and tables
			assert getLoopEndNode() == null : "1st iteration but end node set";
			m_table = table;
			m_iterator = table.iterator();
			m_endTime = calculateEndTime();
			pushFlowVariableString("endTime", m_endTime.toString());
			m_logger.info("Loop execution will terminate after " + m_endTime);
			if (isNowAfter(m_endTime)) {
				throw new Exception("No rows executed as end time has already passed");
			}
		} else {
			// Just some assertions for second iteration and beyond
			assert getLoopEndNode() != null : "No end node set";
			assert table == m_table : "Input table changed between iterations";
		}

		// Now generate the chunked output table
		BufferedDataContainer container = exec.createDataContainer(table.getSpec());
		for (int i = 0; i < rowsPerChunk && m_iterator.hasNext(); i++) {
			container.addRowToTable(m_iterator.next());
		}
		container.close();

		// Update the loop counters
		pushFlowVariableInt("currentIteration", m_iteration++);
		pushFlowVariableInt("maxIterations", totalChunkCount + m_ZerothIteration.getIntValue());
		return new BufferedDataTable[] { container.getTable() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		super.saveSettingsTo(settings);
		m_chunkSize.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		super.validateSettings(settings);
		m_chunkSize.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		super.loadValidatedSettingsFrom(settings);
		m_chunkSize.loadSettingsFrom(settings);
	}
}
