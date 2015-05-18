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
package com.vernalis.knime.flowcontrol.nodes.timedloops.loopend;

import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopend.AbstractMultiPortTimedLoopEndNodeFactory;

/**
 * <code>NodeFactory</code> for the "LoopEnd3LoopToTime" Node. Loop End node for
 * timed loops, exposing unprocessed rows
 * 
 * @author "Stephen Roughley  <s.roughley@vernalis.com>"
 */
public class LoopEndUpto4LoopToTimeNodeFactory extends
		AbstractMultiPortTimedLoopEndNodeFactory {

	/**
	 * Instantiates a new loop end upto4 loop to time node factory.
	 */
	public LoopEndUpto4LoopToTimeNodeFactory() {
		super(4, true);
	}
}
