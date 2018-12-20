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
 *******************************************************************************/
package com.vernalis.knime.chem.speedysmiles.nodes.manip.dummy;

import org.knime.chem.types.SmilesAdapterCell;
import org.knime.chem.types.SmilesCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.chem.speedysmiles.nodes.manip.abstrct.AbstractSpeedySmilesSingleCellManipNodeModel;

import static com.vernalis.knime.chem.speedysmiles.helpers.SmilesHelpers.dummyAtomToElementSymbol;
import static com.vernalis.knime.chem.speedysmiles.nodes.manip.dummy.ReplaceDummyAtomNodeDialog.createReplacementModel;

public class ReplaceDummyAtomNodeModel
		extends AbstractSpeedySmilesSingleCellManipNodeModel {

	private final SettingsModelString replacementMdl = createReplacementModel();
	private String replacement;

	/**
	 * @param hasRemoveInputCol
	 */
	public ReplaceDummyAtomNodeModel() {
		super(true);
		registerSettingsModel(replacementMdl);
	}

	@Override
	protected DataType getColumnType() {
		return SmilesAdapterCell.RAW_TYPE;
	}

	@Override
	protected String getColumnNameSuffix() {
		return "";
	}

	@Override
	protected String getColumnNamePrefix() {
		return "Replaced";
	}

	@Override
	protected DataCell getResultCell(String SMILES) throws Exception {
		return SmilesCellFactory.createAdapterCell(
				dummyAtomToElementSymbol(SMILES, replacement));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.chem.speedysmiles.nodes.manip.abstrct.
	 * AbstractSpeedySmilesManipNodeModel#createColumnRearranger(org.knime.core.
	 * data.DataTableSpec)
	 */
	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {
		setReplacement();

		return super.createColumnRearranger(spec);
	}

	/**
	 * @throws InvalidSettingsException
	 * 
	 */
	private void setReplacement() throws InvalidSettingsException {

		replacement = replacementMdl.getStringValue();
		if (replacement == null) {
			throw new InvalidSettingsException(
					"A null replacement was specified");
		}
		if (replacement.matches("\\[(.*)\\]")) {
			replacement = replacement.replaceAll("\\[(.*)\\]", "$1");
			m_logger.info(
					"Surrounding '[]' removed from replacement, will be reapplied where needed");
			setWarningMessage(
					"Surrounding '[]' removed from replacement, will be reapplied where needed");
		}
		if (replacement.isEmpty()) {
			throw new InvalidSettingsException(
					"An empty replacement was specified");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.chem.speedysmiles.nodes.manip.abstrct.
	 * AbstractSpeedySmilesManipNodeModel#configure(org.knime.core.data.
	 * DataTableSpec[])
	 */
	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		setReplacement();
		return super.configure(inSpecs);
	}

}
