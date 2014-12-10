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

import java.util.Date;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

/**
 * Interface with the methods required for a timed loop start node. These public
 * methods are required to be implemented so that the timed loop end nodes can
 * use them.
 * 
 * @author Stephen Roughley <s.roughley@vernalis.com>
 * 
 */
public interface TimedLoopStart {

	/**
	 * Call this from the Loop end to retrieve unprocessed rows.
	 * 
	 * @param exec
	 *            The execution context
	 * @return a BufferedDataTable object with the unprocessed rows
	 * @throws CanceledExecutionException
	 *             the canceled execution exception
	 */
	public BufferedDataTable getUnprocessedRows(ExecutionContext exec)
			throws CanceledExecutionException;

	/**
	 * Call this from the Timed Loop end to get the spec for the unused rows
	 * table.
	 * 
	 * @return The input DataTableSpec
	 */
	public DataTableSpec getInSpec();

	/**
	 * Returns the end time of the loop.
	 * 
	 * @return The time at which loop execution is set to terminate
	 */
	public Date getEndTime();

	/**
	 * Returns the iteration counter.
	 * 
	 * @return The iteration counter
	 */
	public int getIteration();

}
