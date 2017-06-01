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
package com.vernalis.pdbconnector.nodes.smilesquery;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "PdbSmilesQuery" Node. Node to perfrom SMILES
 * string queries using the PDB smilesQuery webservice
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author S.Roughley
 */
public class PdbSmilesQueryNodeDialog extends DefaultNodeSettingsPane {
	DialogComponent m_queryType;
	DialogComponent m_similarity;

	/**
	 * New pane for configuring the PdbSmilesQuery node.
	 */
	protected PdbSmilesQueryNodeDialog() {
		super();
		// Rename the default tab
		renameTab("Options", "Query Options");

		createNewGroup("Query Settings");
		addDialogComponent(new DialogComponentString(createSmilesQueryModel(),
				"SMILES Query:"));

		m_queryType = new DialogComponentButtonGroup(createQueryTypeModel(),
				false, "Select Query Type", PdbSmilesQueryNodeModel.QUERY_TYPES);
		m_queryType.getModel().addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				setSimEnabledStatus();
			}
		});
		addDialogComponent(m_queryType);

		m_similarity = new DialogComponentNumber(createSimilarityQueryModel(),
				"Similarity Threshold", 0.05);
		setSimEnabledStatus();
		addDialogComponent(m_similarity);

		createNewGroup("Returned Properties");
		addDialogComponent(new DialogComponentBoolean(createTypeModel(),
				"Heterogen Type"));
		addDialogComponent(new DialogComponentBoolean(createMolWtModel(),
				"Molecular Weight"));
		addDialogComponent(new DialogComponentBoolean(
				createChemicalNameModel(), "Chemical Name"));
		addDialogComponent(new DialogComponentBoolean(createFormulaModel(),
				"Formula"));
		addDialogComponent(new DialogComponentBoolean(createInchiKeyModel(),
				"InChI Key"));
		addDialogComponent(new DialogComponentBoolean(createInchiModel(),
				"InChI"));
		addDialogComponent(new DialogComponentBoolean(createSmilesModel(),
				"SMILES"));

	}

	protected void setSimEnabledStatus() {
		m_similarity.getModel().setEnabled("Similarity"
				.equals(((SettingsModelString) m_queryType.getModel())
						.getStringValue()));
	}

	// Methods for creating settings models

	static SettingsModelBoolean createTypeModel() {
		return new SettingsModelBoolean(PdbSmilesQueryNodeModel.TYPE_KEY, true);
	}

	static SettingsModelBoolean createMolWtModel() {
		return new SettingsModelBoolean(PdbSmilesQueryNodeModel.MWT_KEY, true);
	}

	static SettingsModelBoolean createChemicalNameModel() {
		return new SettingsModelBoolean(PdbSmilesQueryNodeModel.CHEM_NAME_KEY,
				true);
	}

	static SettingsModelBoolean createFormulaModel() {
		return new SettingsModelBoolean(PdbSmilesQueryNodeModel.FORMULA_KEY,
				true);
	}

	static SettingsModelBoolean createInchiKeyModel() {
		return new SettingsModelBoolean(PdbSmilesQueryNodeModel.INCHI_KEY_KEY,
				true);
	}

	static SettingsModelBoolean createInchiModel() {
		return new SettingsModelBoolean(PdbSmilesQueryNodeModel.INCHI_KEY, true);
	}

	static SettingsModelBoolean createSmilesModel() {
		return new SettingsModelBoolean(PdbSmilesQueryNodeModel.SMILES_KEY,
				true);
	}

	static SettingsModelString createSmilesQueryModel() {
		return new SettingsModelString(
				PdbSmilesQueryNodeModel.SMILES_STRING_KEY, "");
	}

	static SettingsModelDoubleBounded createSimilarityQueryModel() {
		return new SettingsModelDoubleBounded(
				PdbSmilesQueryNodeModel.SIMILARITY_KEY, 0.7, 0.0, 1.0);
	}

	static SettingsModelString createQueryTypeModel() {
		return new SettingsModelString(PdbSmilesQueryNodeModel.QUERY_TYPE_KEY,
				PdbSmilesQueryNodeModel.QUERY_TYPES[0]);
	}
}
