/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart;

import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.DateFunctions.getCurrentDate;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.DateFunctions.isNowAfter;

import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;

import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.TimedNodeType;

public class AbstractGenericTimedLoopStartNodeModel extends AbstractTimedLoopStartNodeModel {

	public AbstractGenericTimedLoopStartNodeModel(TimedNodeType nodeType) {
		super(1, 1, nodeType);
		m_logger = NodeLogger.getLogger(getClass());
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
		if (m_iteration == m_ZerothIteration.getIntValue()) {
			// Set things up on first iteration
			m_endTime = calculateEndTime();
			pushFlowVariableString("endTime", m_endTime.toString());
			m_logger.info("Loop execution will terminate after " + m_endTime);
			if (isNowAfter(m_endTime)) {
				throw new Exception("No rows executed as end time has already passed");
			}
		}
		exec.checkCanceled();
		// Update the loop counters
		pushFlowVariableInt("maxIterations", -1);// can go on for any number...
		pushFlowVariableInt("currentIteration", m_iteration++);
		// We just pass through the input table each time
		return inData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.
	 * AbstractTimedLoopStartNodeModel#getUnprocessedRows(org.knime.core.node.
	 * ExecutionContext)
	 */
	@Override
	public BufferedDataTable getUnprocessedRows(ExecutionContext exec)
			throws CanceledExecutionException {
		// There are never unprocessed rows in this implementation, so we just
		// return an empty table
		BufferedDataContainer table = exec.createDataContainer(m_inspec);
		table.close();
		return table.getTable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.
	 * AbstractTimedLoopStartNodeModel#terminateLoop()
	 */
	@Override
	public boolean terminateLoop() {
		if (!hasTimeLeft()) {
			m_logger.warn("Loop terminated at " + getCurrentDate() + " as end time passed");
			return true;
		}
		return false;
	}
}
