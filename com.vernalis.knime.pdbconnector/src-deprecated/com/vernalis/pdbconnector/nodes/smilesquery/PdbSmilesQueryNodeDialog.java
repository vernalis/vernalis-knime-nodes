/*******************************************************************************
 * Copyright (c) 2016, 2024, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.pdbconnector.nodes.smilesquery;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;

import javax.swing.JLabel;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentLabel;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "PdbSmilesQuery" Node. Node to perfrom SMILES
 * string queries using the PDB smilesQuery webservice
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * Changes
 * v1.38.0 - disabled all models and added a warning that the node is no longer
 * functional
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

        final DialogComponentLabel diaC = new DialogComponentLabel(
                "THIS NODE NO LONGER WORKS DUE TO THE REMOTE REST WEBSERVICE BEING TURNED OFF");
        JLabel lbl = (JLabel) Arrays
                .stream(diaC.getComponentPanel().getComponents())
                .filter(JLabel.class::isInstance).findFirst().orElseGet(null);
        lbl.setForeground(Color.RED);
        lbl.setFont(
                lbl.getFont().deriveFont(lbl.getFont().getStyle() | Font.BOLD));

        // lbl.setFont(Font.);
        addDialogComponent(diaC);

		createNewGroup("Query Settings");
		addDialogComponent(new DialogComponentString(createSmilesQueryModel(),
				"SMILES Query:"));

		m_queryType = new DialogComponentButtonGroup(createQueryTypeModel(),
				false, "Select Query Type", PdbSmilesQueryNodeModel.QUERY_TYPES);

		addDialogComponent(m_queryType);

		m_similarity = new DialogComponentNumber(createSimilarityQueryModel(),
				"Similarity Threshold", 0.05);
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


	// Methods for creating settings models

	static SettingsModelBoolean createTypeModel() {

        return disableModel(new SettingsModelBoolean(
                PdbSmilesQueryNodeModel.TYPE_KEY, true));
	}

	static SettingsModelBoolean createMolWtModel() {

        return disableModel(new SettingsModelBoolean(
                PdbSmilesQueryNodeModel.MWT_KEY, true));
	}

	static SettingsModelBoolean createChemicalNameModel() {

        return disableModel(new SettingsModelBoolean(
                PdbSmilesQueryNodeModel.CHEM_NAME_KEY, true));
	}

	static SettingsModelBoolean createFormulaModel() {

        return disableModel(new SettingsModelBoolean(
                PdbSmilesQueryNodeModel.FORMULA_KEY, true));
	}

	static SettingsModelBoolean createInchiKeyModel() {

        return disableModel(
                new SettingsModelBoolean(PdbSmilesQueryNodeModel.INCHI_KEY_KEY,
                        true));
	}

	static SettingsModelBoolean createInchiModel() {

        return disableModel(new SettingsModelBoolean(
                PdbSmilesQueryNodeModel.INCHI_KEY, true));
	}

	static SettingsModelBoolean createSmilesModel() {

        return disableModel(new SettingsModelBoolean(
                PdbSmilesQueryNodeModel.SMILES_KEY, true));
	}

	static SettingsModelString createSmilesQueryModel() {

        return disableModel(new SettingsModelString(
                PdbSmilesQueryNodeModel.SMILES_STRING_KEY, ""));
	}

	static SettingsModelDoubleBounded createSimilarityQueryModel() {

        return disableModel(new SettingsModelDoubleBounded(
                PdbSmilesQueryNodeModel.SIMILARITY_KEY, 0.7, 0.0, 1.0));
	}

	static SettingsModelString createQueryTypeModel() {

        return disableModel(
                new SettingsModelString(PdbSmilesQueryNodeModel.QUERY_TYPE_KEY,
        PdbSmilesQueryNodeModel.QUERY_TYPES[0]));

	}

    static <T extends SettingsModel> T disableModel(final T retVal) {

        retVal.setEnabled(false);
        return retVal;
    }
}
