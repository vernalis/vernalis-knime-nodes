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
package com.vernalis.knime.chem.speedysmiles.nodes.manip.cyclise;

import org.knime.chem.types.SmilesAdapterCell;
import org.knime.chem.types.SmilesCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.knime.chem.speedysmiles.helpers.SmilesHelpers;
import com.vernalis.knime.chem.speedysmiles.nodes.manip.abstrct.AbstractSpeedySmilesSingleCellManipNodeModel;

import static com.vernalis.knime.chem.speedysmiles.nodes.manip.cyclise.SmilesCycliseNodeDialog.createRemoveFirstAtomModel;
import static com.vernalis.knime.chem.speedysmiles.nodes.manip.cyclise.SmilesCycliseNodeDialog.createRemoveLastAtomModel;

public class SmilesCycliseNodeModel
		extends AbstractSpeedySmilesSingleCellManipNodeModel {

	private final SettingsModelBoolean removeFirstAtomMdl =
			createRemoveFirstAtomModel();
	private final SettingsModelBoolean removeLastAtomMdl =
			createRemoveLastAtomModel();

	/**
	 * Constructor for the node model
	 */
	public SmilesCycliseNodeModel() {
		super(true);
		registerSettingsModel(removeFirstAtomMdl);
		registerSettingsModel(removeLastAtomMdl);
	}

	@Override
	protected DataType getColumnType() {
		return SmilesAdapterCell.RAW_TYPE;
	}

	@Override
	protected String getColumnNameSuffix() {
		return null;
	}

	@Override
	protected String getColumnNamePrefix() {
		return "cyclo-";
	}

	@Override
	protected DataCell getResultCell(String SMILES) throws Exception {
		String retVal = SMILES;
		if (removeFirstAtomMdl.getBooleanValue()) {
			retVal = SmilesHelpers.removeFirstAtom(retVal);
		}
		if (removeLastAtomMdl.getBooleanValue()) {
			retVal = SmilesHelpers.removeLastAtom(retVal);
		}
		retVal = SmilesHelpers.cycliseEndAtoms(retVal);
		return retVal == null || retVal.isEmpty() ? DataType.getMissingCell()
				: SmilesCellFactory.createAdapterCell(retVal);
	}

}
