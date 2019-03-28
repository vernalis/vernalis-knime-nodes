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
package com.vernalis.knime.perfmon.nodes.loopstart.timing.memory;

import com.vernalis.knime.perfmon.nodes.timing.abstrct.AbstractPerfMonTimingStartNodeFactory;

/**
 * <code>NodeFactory</code> for the "TimingStart" Node. Loop start for execution
 * timing
 *
 * @author S. Roughley
 */
public class PerfMemMonTiming3PortStartNodeFactory
		extends AbstractPerfMonTimingStartNodeFactory {

	public PerfMemMonTiming3PortStartNodeFactory() {
		super(3, true);
	}

}
