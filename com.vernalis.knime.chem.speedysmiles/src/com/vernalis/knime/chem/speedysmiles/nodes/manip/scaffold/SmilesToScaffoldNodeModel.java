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
package com.vernalis.knime.chem.speedysmiles.nodes.manip.scaffold;

import org.knime.chem.types.SmartsAdapterCell;
import org.knime.chem.types.SmartsCellFactory;
import org.knime.chem.types.SmilesAdapterCell;
import org.knime.chem.types.SmilesCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.knime.chem.speedysmiles.helpers.SmilesHelpers;
import com.vernalis.knime.chem.speedysmiles.nodes.manip.abstrct.AbstractSpeedySmilesSingleCellManipNodeModel;

import static com.vernalis.knime.chem.speedysmiles.nodes.manip.scaffold.SmilesToScaffoldNodeDialog.createAnyAtomModel;
import static com.vernalis.knime.chem.speedysmiles.nodes.manip.scaffold.SmilesToScaffoldNodeDialog.createAromaticModel;
import static com.vernalis.knime.chem.speedysmiles.nodes.manip.scaffold.SmilesToScaffoldNodeDialog.createBondOrdersModel;
import static com.vernalis.knime.chem.speedysmiles.nodes.manip.scaffold.SmilesToScaffoldNodeDialog.createChiralityModel;
import static com.vernalis.knime.chem.speedysmiles.nodes.manip.scaffold.SmilesToScaffoldNodeDialog.createExplHsModel;

public class SmilesToScaffoldNodeModel
		extends AbstractSpeedySmilesSingleCellManipNodeModel {

	private final SettingsModelBoolean anyAtomMdl = createAnyAtomModel();
	private final SettingsModelBoolean keepExplicitHsMdl = createExplHsModel();
	private final SettingsModelBoolean keepBondOrdersMdl =
			createBondOrdersModel();
	private final SettingsModelBoolean keepAromaticityMdl =
			createAromaticModel();
	private final SettingsModelBoolean keepChiralityMdl =
			createChiralityModel();

	/**
	 * Constructor for the node model
	 */
	public SmilesToScaffoldNodeModel() {
		super(true);
		registerSettingsModel(anyAtomMdl);
		registerSettingsModel(keepExplicitHsMdl);
		registerSettingsModel(keepBondOrdersMdl);
		registerSettingsModel(keepAromaticityMdl);
		registerSettingsModel(keepChiralityMdl);
	}

	@Override
	protected DataType getColumnType() {
		return anyAtomMdl.getBooleanValue()
				&& keepAromaticityMdl.getBooleanValue()
						? SmartsAdapterCell.RAW_TYPE
						: SmilesAdapterCell.RAW_TYPE;
	}

	@Override
	protected String getColumnNameSuffix() {
		return "Scaffold";
	}

	@Override
	protected String getColumnNamePrefix() {
		return null;
	}

	@Override
	protected DataCell getResultCell(String SMILES) throws Exception {
		String retVal = SmilesHelpers.convertToScaffold(SMILES,
				anyAtomMdl.getBooleanValue(),
				keepExplicitHsMdl.getBooleanValue(),
				keepBondOrdersMdl.getBooleanValue(),
				keepAromaticityMdl.getBooleanValue(),
				keepChiralityMdl.getBooleanValue());
		return anyAtomMdl.getBooleanValue()
				&& keepAromaticityMdl.getBooleanValue()
						? SmartsCellFactory.createAdapterCell(retVal)
						: SmilesCellFactory.createAdapterCell(retVal);
	}

}
