/*******************************************************************************
 *  Copyright (c) 2018, Vernalis (R&D) Ltd
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
 *******************************************************************************/
package com.vernalis.knime.chem.speedysmiles.nodes.filter.abstrct;

import java.util.Optional;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.chem.speedysmiles.helpers.SmilesHelpers;
import com.vernalis.knime.nodes.AbstractStreamableParallelisedFilterSplitterNodeModel;

import static com.vernalis.knime.chem.speedysmiles.nodes.abstrct.AbstractSpeedySmilesNodeDialog.createColumnNameModel;

/**
 * This is the base class for Speedy SMILES Filter/Splitter nodes. Nodes using
 * this class may calculate new properties prior to filtering. For filtering
 * based purely on the SMILES string without any changes to the table structure,
 * use the subclass {@link AbstractSpeedySmilesFilterNodeModel}
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The type parameter of an object retrieved from the
 *            {@link DataRow}. See
 *            {@link AbstractStreamableParallelisedFilterSplitterNodeModel} for
 *            further details
 * @see AbstractStreamableParallelisedFilterSplitterNodeModel
 */
public abstract class AbstractSpeedySmilesCalcFilterNodeModel<T>
		extends AbstractStreamableParallelisedFilterSplitterNodeModel<T> {

	protected final SettingsModelString m_colName =
			registerSettingsModel(createColumnNameModel());
	protected int smiColIdx;

	protected AbstractSpeedySmilesCalcFilterNodeModel(boolean isSplitter) {
		super(isSplitter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.chem.speedysmiles.nodes.filter.abstrct.
	 * AbstractStreamableParallelisedFilterSplitterNodeModel#doConfigure(org.
	 * knime.core.data.DataTableSpec[])
	 */
	@Override
	protected String doConfigure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		return SmilesHelpers.findSmilesColumn(inSpecs[0], m_colName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.chem.speedysmiles.nodes.filter.abstrct.
	 * AbstractStreamableParallelisedFilterSplitterNodeModel#doPreExecutionSetup
	 * (org.knime.core.data.DataTableSpec)
	 */
	@Override
	protected void doPreExecutionSetup(DataTableSpec inSpec)
			throws InvalidSettingsException {
		smiColIdx = inSpec.findColumnIndex(m_colName.getStringValue());
	}

	/**
	 * @param row
	 *            The incoming DataRow
	 * @return The SMILES String from the relevant column, or null if the cell
	 *         was missing
	 * @throws Exception
	 *             If there was an exception thrown whilst getting the cell
	 */
	protected final Optional<String> getSmilesFromRow(DataRow row)
			throws Exception {
		return Optional.ofNullable(
				SmilesHelpers.getSmilesFromCell(row.getCell(smiColIdx)));
	}
}
