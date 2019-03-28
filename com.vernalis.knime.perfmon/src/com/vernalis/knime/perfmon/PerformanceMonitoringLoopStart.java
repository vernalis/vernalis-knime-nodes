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
package com.vernalis.knime.perfmon;

import java.util.Date;

import org.knime.core.node.workflow.LoopStartNodeTerminator;

/**
 * Type denominator only
 * 
 * @author s.roughley
 * 
 */
public interface PerformanceMonitoringLoopStart
		extends LoopStartNodeTerminator {

	/** Get the Start of execution time */
	public Date getStartDate();

	/** Get the enabled status of the timeout setting */
	public boolean hasTimeoutEnabled();

	/** Get the timeout. Should return -1 if the enabled status is false */
	public int getTimeoutDuration();

	/** Should the loop end report individual node timings in loop body */
	public boolean getReportNodeTimes();

	/**
	 * If the loop end is reporting individual node timings, should it probe
	 * subnodes (i.e. wrapped metanodes)?
	 */
	public boolean getProbeSubnodeTimes();
}
