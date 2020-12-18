/*******************************************************************************
 * Copyright (c) 2016,2020 Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector.nodes.describehet;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "PdbDescribeHet" Node. Node to retrieve
 * Heterogen details from the PDB "Describe Heterogen" webservice
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author S.Roughley
 */
public class PdbDescribeHetNodeDialog extends DefaultNodeSettingsPane {

	private static final String PAGE_SIZE = "Page Size";

	/**
	 * New pane for configuring the PdbDescribeHet node.
	 */
	@SuppressWarnings("unchecked")
	protected PdbDescribeHetNodeDialog() {
		super();
		// Rename the default tab
		renameTab("Options", "Search Options");

		createNewGroup("Heterogen IDs");
		addDialogComponent(new DialogComponentColumnNameSelection(
				createHetIdColumnNameModel(), "Het ID Column", 0,
				StringValue.class));

		createNewGroup("Properties");
		addDialogComponent(new DialogComponentBoolean(createTypeModel(),
				"Heterogen Type"));
		addDialogComponent(new DialogComponentBoolean(createMolWtModel(),
				"Molecular Weight"));
		addDialogComponent(new DialogComponentBoolean(createChemicalNameModel(),
				"Chemical Name"));
		addDialogComponent(
				new DialogComponentBoolean(createFormulaModel(), "Formula"));
		addDialogComponent(
				new DialogComponentBoolean(createInchiKeyModel(), "InChI Key"));
		addDialogComponent(
				new DialogComponentBoolean(createInchiModel(), "InChI"));
		addDialogComponent(
				new DialogComponentBoolean(createSmilesModel(), "SMILES"));

		closeCurrentGroup();
		addDialogComponent(new DialogComponentNumber(createPageSizeModel(),
				PAGE_SIZE, 100));

		// createNewTab("Connection Properties");
		// addDialogComponent(new
		// DialogComponentNumber(createMaxUrlLengthModel(),
		// "Maximum URL Length", 100));
	}

	/**
	 * @return The model for the 'Page Size' setting
	 */
	static SettingsModelIntegerBounded createPageSizeModel() {
		return new SettingsModelIntegerBounded(PAGE_SIZE, 1000, 10, 10000);
	}

	/**
	 * @return The model for the 'Het ID Column' setting
	 */
	// Methods for creating settings models
	static SettingsModelString createHetIdColumnNameModel() {
		return new SettingsModelString(
				PdbDescribeHetNodeModel.HETID_COL_NAME_KEY, null);
	}

	/**
	 * @return The model for the 'Type' setting
	 */
	static SettingsModelBoolean createTypeModel() {
		return new SettingsModelBoolean(PdbDescribeHetNodeModel.TYPE_KEY, true);
	}

	/**
	 * @return The model for the 'MWt' setting
	 */
	static SettingsModelBoolean createMolWtModel() {
		return new SettingsModelBoolean(PdbDescribeHetNodeModel.MWT_KEY, true);
	}

	/**
	 * @return The model for the 'Chemical Name' setting
	 */
	static SettingsModelBoolean createChemicalNameModel() {
		return new SettingsModelBoolean(PdbDescribeHetNodeModel.CHEM_NAME_KEY,
				true);
	}

	/**
	 * @return The model for the 'Formula' setting
	 */
	static SettingsModelBoolean createFormulaModel() {
		return new SettingsModelBoolean(PdbDescribeHetNodeModel.FORMULA_KEY,
				true);
	}

	/**
	 * @return The model for the 'InChI Key' setting
	 */
	static SettingsModelBoolean createInchiKeyModel() {
		return new SettingsModelBoolean(PdbDescribeHetNodeModel.INCHI_KEY_KEY,
				true);
	}

	/**
	 * @return The model for the 'InChI' setting
	 */
	static SettingsModelBoolean createInchiModel() {
		return new SettingsModelBoolean(PdbDescribeHetNodeModel.INCHI_KEY,
				true);
	}

	/**
	 * @return The model for the 'SMILES' setting
	 */
	static SettingsModelBoolean createSmilesModel() {
		return new SettingsModelBoolean(PdbDescribeHetNodeModel.SMILES_KEY,
				true);
	}

}
