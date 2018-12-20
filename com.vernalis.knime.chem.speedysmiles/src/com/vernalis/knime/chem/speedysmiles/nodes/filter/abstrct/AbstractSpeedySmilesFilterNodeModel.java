/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *  This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.knime.chem.speedysmiles.nodes.filter.abstrct;

import org.knime.core.data.DataRow;

/**
 * This is the base node model class for the SpeedySMILES filter/splitter nodes
 * when no changes to table structure take place. It implements the KNIME
 * Streaming API to allow streaming execution where available, and runs
 * parallelised otherwise.
 * 
 * @author S Roughley
 */
public abstract class AbstractSpeedySmilesFilterNodeModel
		extends AbstractSpeedySmilesCalcFilterNodeModel<String> {

	/**
	 * Constructor for the node model.
	 * 
	 * @param isSplitter
	 *            if <code>true</code> then 2 outputs, otherwise 1
	 */
	protected AbstractSpeedySmilesFilterNodeModel(boolean isSplitter) {
		super(isSplitter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.chem.speedysmiles.nodes.filter.abstrct.
	 * AbstractStreamableParallelisedFilterSplitterNodeModel#getObjectFromRow(
	 * org.knime.core.data.DataRow)
	 */
	@Override
	protected String getObjectFromRow(DataRow row) throws Exception {
		return getSmilesFromRow(row).orElse(null);
	}

}
