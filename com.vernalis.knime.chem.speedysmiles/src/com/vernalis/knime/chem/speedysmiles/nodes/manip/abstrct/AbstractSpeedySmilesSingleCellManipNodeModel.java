/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
package com.vernalis.knime.chem.speedysmiles.nodes.manip.abstrct;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;

/**
 * Abstract Node Model for SpeedySMILES nodes calculating a single output cell
 * <p>
 * This is a convenience implementation. This is a convenience implementation to
 * allow passing of single non-array values to the super type.
 * 
 * @author S.Roughley
 *
 */
public abstract class AbstractSpeedySmilesSingleCellManipNodeModel
		extends AbstractSpeedySmilesManipNodeModel {

	/**
	 * Constructor with no 'Remove Input Column' option
	 */
	public AbstractSpeedySmilesSingleCellManipNodeModel() {
		super();
	}

	/**
	 * Constructor for the node model
	 * 
	 * @param hasRemoveInputCol
	 *            Should the node handle a 'Remove Input Column' setting
	 */
	public AbstractSpeedySmilesSingleCellManipNodeModel(boolean hasRemoveInputCol) {
		super(hasRemoveInputCol);
	}

	@Override
	protected DataType[] getColumnTypes() {
		return new DataType[] { getColumnType() };
	}

	protected abstract DataType getColumnType();

	@Override
	protected String[] getColumnNameSuffixes() {
		return getColumnNameSuffix() == null ? null : new String[] { getColumnNameSuffix() };
	}

	/**
	 * @return the suffix part of the column name. If null, then the prefix is
	 *         used as the entire name
	 * @see AbstractSpeedySmilesManipNodeModel#getColumnNameSuffixes()
	 */
	protected abstract String getColumnNameSuffix();

	@Override
	protected String[] getColumnNamePrefixes() {
		return new String[] { getColumnNamePrefix() };
	}

	/**
	 * @return the prefix part of the column name
	 * @see AbstractSpeedySmilesManipNodeModel#getColumnNamePrefixes()
	 */
	protected abstract String getColumnNamePrefix();

	@Override
	protected DataCell[] getResultColumns(String SMILES, int numCols) throws Exception {
		return new DataCell[] { getResultCell(SMILES) };
	}

	/**
	 * Return the result cell for the SMILES String. See the link below for
	 * details on Exception throwing and handling
	 * 
	 * @param SMILES
	 *            The non-null SMILES string
	 * @return The Result Cell.
	 * @see {@link AbstractSpeedySmilesManipNodeModel#getResultColumns(String, int)
	 */
	protected abstract DataCell getResultCell(String SMILES) throws Exception;

}
