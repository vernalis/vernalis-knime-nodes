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
package com.vernalis.knime.chem.speedysmiles.nodes.count.abstrct;

import com.vernalis.exceptions.RowExecutionException;
import com.vernalis.knime.chem.speedysmiles.nodes.manip.abstrct.AbstractSpeedySmilesManipNodeModel;

/**
 * Abstract Node Model for SpeedySMILES nodes calculating only a single integer
 * (count) result
 * <p>
 * This is a convenience implementation. This is a convenience implementation to
 * only require passing of Integer result to the supertype
 * </p>
 * 
 * @author S.Roughley
 *
 */
public abstract class AbstractSpeedySmilesSingleCountNodeModel
		extends AbstractSpeedySmilesCountNodeModel {

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
	protected Integer[] getResultCounts(String SMILES, int numCols) throws RowExecutionException {
		return new Integer[] { getResultCount(SMILES) };
	}

	/**
	 * Return the result count for the SMILES String. See the link below for
	 * details on Exception throwing and handling
	 * 
	 * @param SMILES
	 *            The non-null SMILES string
	 * @return The Result Count
	 * @throws RowExecutionException
	 * @see {@link AbstractSpeedySmilesManipNodeModel#getResultColumns(String, int)
	 */
	protected abstract Integer getResultCount(String SMILES) throws RowExecutionException;

}
