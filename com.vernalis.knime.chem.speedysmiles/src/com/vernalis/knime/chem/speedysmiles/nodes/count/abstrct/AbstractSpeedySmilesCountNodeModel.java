/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
package com.vernalis.knime.chem.speedysmiles.nodes.count.abstrct;

import java.util.Arrays;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.IntCell;

import com.vernalis.exceptions.RowExecutionException;
import com.vernalis.knime.chem.speedysmiles.nodes.manip.abstrct.AbstractSpeedySmilesManipNodeModel;

/**
 * Abstract Node Model for SpeedySMILES nodes calculating only integer (counts)
 * results
 * <p>
 * This is a convenience implementation. This is a convenience implementation to
 * only require passing of Integer results to the supertype
 * </p>
 * 
 * @author S.Roughley
 *
 */
public abstract class AbstractSpeedySmilesCountNodeModel
		extends AbstractSpeedySmilesManipNodeModel {

	/**
	 * Constructor for the node model.
	 */
	protected AbstractSpeedySmilesCountNodeModel() {
		this(false);

	}

	protected AbstractSpeedySmilesCountNodeModel(boolean hasRemoveInputCol) {
		super();

	}

	/**
	 * Method to actually return the result counts
	 * 
	 * @param SMILES
	 *            The SMILES String (non-null)
	 * @param numCols
	 *            The number of properties to calculate
	 * @return The counts. <code>null</code> will return a missing cell
	 * @throws RowExecutionException
	 *             If the row could not be computed but execution should
	 *             continue
	 */
	protected abstract Integer[] getResultCounts(String SMILES, int numCols)
			throws RowExecutionException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.chem.molprops.nodes.manip.abstrct.
	 * AbstractSpeedySmilesManipNodeModel#getColumnTypes()
	 */
	@Override
	protected DataType[] getColumnTypes() {
		DataType[] retVal = new DataType[getColumnNamePrefixes().length];
		Arrays.fill(retVal, IntCell.TYPE);
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.chem.molprops.nodes.manip.abstrct.
	 * AbstractSpeedySmilesManipNodeModel#getResultColumns(java.lang.String,
	 * int)
	 */
	@Override
	protected DataCell[] getResultColumns(String SMILES, int numCols)
			throws Exception {
		DataCell[] retVal = new DataCell[numCols];
		int colIdx = 0;
		for (Integer val : getResultCounts(SMILES, numCols)) {
			if (val == null) {
				retVal[colIdx++] = DataType.getMissingCell();
			} else {
				retVal[colIdx++] = new IntCell(val);
			}
		}
		return retVal;
	}

}
