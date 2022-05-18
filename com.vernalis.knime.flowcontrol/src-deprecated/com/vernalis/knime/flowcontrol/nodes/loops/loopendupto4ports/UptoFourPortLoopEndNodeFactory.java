/*******************************************************************************
 * Copyright (c) 2014,2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.flowcontrol.nodes.loops.loopendupto4ports;

import com.vernalis.knime.flowcontrol.nodes.loops.abstrct.multiportloopend.AbstractMultiPortLoopEndNodeFactory;

/**
 * <code>NodeFactory</code> for the UptoFour Port Loop End Node. Loop end node
 * to handle upto 4 optional input ports
 * 
 * 
 * @author S. Roughley
 */
@Deprecated
public class UptoFourPortLoopEndNodeFactory extends
		AbstractMultiPortLoopEndNodeFactory {

	/**
	 * Instantiates a new upto four port loop end node factory.
	 */
	public UptoFourPortLoopEndNodeFactory() {
		super(4, true);
	}

}
