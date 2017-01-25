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
package com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct;

/**
 * An Enum to allow designation of node types for timed loop nodes and other
 * nodes which are set to run based on a time constraint.
 * 
 * @author S.Roughley knime@vernalis.com
 */
public enum TimedNodeType {

	/** Node is to run to a set time. */
	RUN_TO_TIME,

	/** Node is to run for a set time. */
	RUN_FOR_TIME;
}
