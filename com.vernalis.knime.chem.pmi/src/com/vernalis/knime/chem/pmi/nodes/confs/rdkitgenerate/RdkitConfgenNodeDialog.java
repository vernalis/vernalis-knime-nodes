/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.chem.rdkit.RdkitCompatibleColumnFormats;
import com.vernalis.knime.dialog.components.DialogComponentEmpty;
import com.vernalis.knime.dialog.components.DialogComponentGroup;
import com.vernalis.knime.dialog.components.DialogComponentMultilineStringFlowvar;
import com.vernalis.knime.dialog.components.SettingsModelMultilineString;

/**
 * Node Dialog for the Conformer Generation node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class RdkitConfgenNodeDialog extends DefaultNodeSettingsPane {

	static final String ALLOW_HEAVY_ATOM_MISMATCHES =
			"Allow heavy atom mismatches";
	static final String RANDOM_SEED = "Random Seed";
	static final String GEOMETRY_OPTIMISATION = "Geometry Optimisation";
	static final String TEMPLATE_OPTIONS = "Template options";
	static final String REMOVE_H_S = "Remove H's";
	static final String IGNORE_H_S = "Ignore H's";
	static final String MINIMUM_RMSD = "Minimum RMSD";
	static final String FILTER_CONFORMERS_BY_RMSD = "Filter conformers by RMSD";
	static final String USE_TETHERS = "Use tethers";
	static final String MAX_TEMPLATE_RMSD = "Max template RMSD";
	static final String FILTER_BY_TEMPLATE_RMSD = "Filter by Template RMSD";
	static final String CONFORMER_OUTPUT_FORMAT = "Conformer output format";
	static final String SHOW_TEMPLATE_MATCH = "Show template match";
	static final String OUTPUT_ACTUAL_TEMPLATE_FOR_ROW =
			"Output Actual Template for Row";
	static final String ENSURE_H_S_ADDED = "Ensure H's added";
	static final String MAX_RELATIVE_ENERGY = "Max relative energy";
	static final String FILTER_BY_ENERGY = "Filter by energy";
	static final String ITERATIONS = "Iterations";
	static final String USE_UFF_FORCE_FIELD_IF_UNABLE_TO_GENERATE_MMFF =
			"Use UFF force field if unable to generate MMFF?";
	static final String FORCEFIELD = "Forcefield";
	static final String USE_BASIC_KNOWLEDGE_I_E_FLAT_RINGS_ETC =
			"Use 'basic knowledge' - i.e. flat rings etc";
	static final String USE_EXPERIMENTAL_TORSIONS = "Use experimental torsions";
	static final String MAX_NUMBER_OF_TRIES_TO_GENERATE_CONFORMER =
			"Max. Number of tries to generate conformer";
	static final String NUMBER_OF_CONFORMERS = "Number of conformers";
	static final String USE_ROTATABLE_BOND_COUNT_TO_DETERMINE_NUMBER_OF_CONFORMERS =
			"Use Rotatable Bond Count to Determine Number of Conformers";
	static final String MOLECULE_COLUMN = "Molecule column";
	static final String TEMPLATE_MOL_BLOCK = "Template MOL Block";
	static final String TEMPLATE_COLUMN = "Template Column";
	private final SettingsModelIntegerBounded m_numConfs;
	private final SettingsModelBoolean m_useNRotForNConfs, m_filterByEnergy;
	private final SettingsModelDouble m_energyMax;
	private final SettingsModelString templateColNameMdl;
	private final SettingsModelString templateMolBlockMdl;
	private final SettingsModelBoolean outputActualTemplateMdl;
	private final SettingsModelBoolean filterByTemplateRMDSMdl =
			createFilterByTemplateRMSDModel();
	private final SettingsModelDoubleBounded maxTemplateRMSDMdl =
			createMaxTemplateRMSDModel();
	private SettingsModelBoolean useTethersMdl = createUseTethersModel();
	private final SettingsModelBoolean allowHeavyAtomMismatchesMdl =
			createAllowHeavyAtomMismatchesModel();

	private final FlowVariableModel templateMolBlockFvm;

	/**
	 * Constructor
	 */
	public RdkitConfgenNodeDialog() {
		super();

		/*
		 * Molecule selection and pre-processing group
		 */
		createNewGroup("Input Molecules / Preprocessing");
		addDialogComponent(new DialogComponentColumnNameSelection(
				createColumnNameModel(), MOLECULE_COLUMN, 0,
				RdkitCompatibleColumnFormats.MOL_ANY));

		addDialogComponent(new DialogComponentBoolean(createAddHsModel(),
				ENSURE_H_S_ADDED));

		/*
		 * Conformer generation options
		 */
		createNewGroup("Conformer Generation Options");
		m_numConfs = createNumConfsModel();
		m_useNRotForNConfs = createUseNRotModel();
		m_useNRotForNConfs.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				m_numConfs.setEnabled(!m_useNRotForNConfs.getBooleanValue());

			}
		});
		m_numConfs.setEnabled(!m_useNRotForNConfs.getBooleanValue());

		addDialogComponent(new DialogComponentBoolean(m_useNRotForNConfs,
				USE_ROTATABLE_BOND_COUNT_TO_DETERMINE_NUMBER_OF_CONFORMERS));

		addDialogComponent(new DialogComponentNumber(m_numConfs,
				NUMBER_OF_CONFORMERS, 10));

		addDialogComponent(new DialogComponentNumber(createNumTriesModel(),
				MAX_NUMBER_OF_TRIES_TO_GENERATE_CONFORMER, 5));

		addDialogComponent(new DialogComponentBoolean(createExpTorsionModel(),
				USE_EXPERIMENTAL_TORSIONS));

		addDialogComponent(
				new DialogComponentBoolean(createBasicKnowledgeModel(),
						USE_BASIC_KNOWLEDGE_I_E_FLAT_RINGS_ETC));

		final SettingsModelBoolean filterByRMSDMdl = createFilterByRMSDModel();
		final SettingsModelDoubleBounded minRMSDMdl = createMinRMSDModel();
		final SettingsModelBoolean ignoreHMdl = createIgnoreHsRMSDModel();
		filterByRMSDMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				minRMSDMdl.setEnabled(filterByRMSDMdl.getBooleanValue());
				ignoreHMdl.setEnabled(filterByRMSDMdl.getBooleanValue());
			}
		});
		minRMSDMdl.setEnabled(filterByRMSDMdl.getBooleanValue());
		ignoreHMdl.setEnabled(filterByRMSDMdl.getBooleanValue());

		DialogComponentGroup rmsdFilterGroup = new DialogComponentGroup(this,
				"RMSD Filter", new DialogComponentBoolean(filterByRMSDMdl,
						FILTER_CONFORMERS_BY_RMSD),
				false);
		DialogComponentGroup rmsdOptionGroup = rmsdFilterGroup.addSubGroup(
				new DialogComponentNumber(minRMSDMdl, MINIMUM_RMSD, 0.05, 7),
				"Filter Options", true);
		rmsdOptionGroup.addComponent(
				new DialogComponentBoolean(ignoreHMdl, IGNORE_H_S));

		closeCurrentGroup();
		createNewGroup("Output Options");

		addDialogComponent(
				new DialogComponentBoolean(createRemoveHsModel(), REMOVE_H_S));
		// Use a DCG to prevent title truncation
		new DialogComponentGroup(this, CONFORMER_OUTPUT_FORMAT,
				new DialogComponentButtonGroup(createOutputFormatModel(), null,
						false, ConformerOutputFormats.values()),
				true);

		/*
		 * Template options
		 */
		createNewTab(TEMPLATE_OPTIONS);
		templateColNameMdl = createTemplateColNameModel();
		templateMolBlockMdl = createTemplateMolBlockModel();
		templateMolBlockFvm = createFlowVariableModel(templateMolBlockMdl);
		outputActualTemplateMdl = createOutputActualTemplateModel();
		// showTemplateMatchMdl = createShowTemplateMatchModel();

		templateColNameMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateTemplateModels();

			}
		});
		templateMolBlockMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateTemplateModels();

			}
		});
		templateMolBlockFvm.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateTemplateModels();

			}
		});

		filterByTemplateRMDSMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateTemplateModels();

			}
		});
		updateTemplateModels();

		addDialogComponent(new DialogComponentBoolean(outputActualTemplateMdl,
				OUTPUT_ACTUAL_TEMPLATE_FOR_ROW));
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentBoolean(filterByTemplateRMDSMdl,
				FILTER_BY_TEMPLATE_RMSD));
		addDialogComponent(new DialogComponentNumber(maxTemplateRMSDMdl,
				MAX_TEMPLATE_RMSD, 0.05, 7));
		setHorizontalPlacement(false);
		setHorizontalPlacement(true);
		addDialogComponent(
				new DialogComponentBoolean(useTethersMdl, USE_TETHERS));
		addDialogComponent(new DialogComponentBoolean(
				allowHeavyAtomMismatchesMdl, ALLOW_HEAVY_ATOM_MISMATCHES));
		setHorizontalPlacement(false);
		createNewGroup("Template");
		addDialogComponent(new DialogComponentColumnNameSelection(
				templateColNameMdl, TEMPLATE_COLUMN, 0, false, true,
				RdkitCompatibleColumnFormats.MOL_FROM_MOLBLOCK));

		addDialogComponent(new DialogComponentMultilineStringFlowvar(
				templateMolBlockMdl, TEMPLATE_MOL_BLOCK, false, 50, 15,
				templateMolBlockFvm));

		/*
		 * Post-processing options
		 */
		createNewTab(GEOMETRY_OPTIMISATION);
		addDialogComponent(
				new DialogComponentButtonGroup(createForceFieldModel(),
						FORCEFIELD, false, ForceFieldFactory.values()));
		addDialogComponent(new DialogComponentBoolean(createCleanUpModel(),
				USE_UFF_FORCE_FIELD_IF_UNABLE_TO_GENERATE_MMFF));
		addDialogComponent(new DialogComponentNumber(createIterationsModel(),
				ITERATIONS, 100, 8));

		m_filterByEnergy = createEnergyFilterModel();
		m_energyMax = createMaxEnergyModel();
		m_energyMax.setEnabled(m_filterByEnergy.getBooleanValue());
		m_filterByEnergy.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				m_energyMax.setEnabled(m_filterByEnergy.getBooleanValue());

			}
		});

		setHorizontalPlacement(true);
		addDialogComponent(
				new DialogComponentBoolean(m_filterByEnergy, FILTER_BY_ENERGY));
		addDialogComponent(new DialogComponentNumber(m_energyMax,
				MAX_RELATIVE_ENERGY, 0.1));
		setHorizontalPlacement(false);

		/*
		 * Hidden options for testing
		 */
		addDialogComponent(new DialogComponentEmpty(createRandomSeedMdl()));

	}

	/**
	 * Method to ensure template model enablement matches settings
	 */
	protected void updateTemplateModels() {
		templateMolBlockMdl
				.setEnabled(templateColNameMdl.getStringValue() == null
						|| templateColNameMdl.getStringValue().isEmpty());
		boolean hasTemplate = (templateColNameMdl.getStringValue() != null
				&& !templateColNameMdl.getStringValue().isEmpty())
				|| (templateMolBlockMdl.getStringValue() != null
						&& !templateMolBlockMdl.getStringValue().isEmpty())
				|| templateMolBlockFvm.isVariableReplacementEnabled();
		outputActualTemplateMdl.setEnabled(hasTemplate);
		filterByTemplateRMDSMdl.setEnabled(hasTemplate);
		maxTemplateRMSDMdl.setEnabled(
				hasTemplate && filterByTemplateRMDSMdl.getBooleanValue());
		useTethersMdl.setEnabled(hasTemplate);
		allowHeavyAtomMismatchesMdl.setEnabled(hasTemplate);
	}

	/**
	 * @return the settings model for the {@value #ALLOW_HEAVY_ATOM_MISMATCHES}
	 *         option
	 */
	static SettingsModelBoolean createAllowHeavyAtomMismatchesModel() {
		return new SettingsModelBoolean(ALLOW_HEAVY_ATOM_MISMATCHES, true);
	}

	/**
	 * @return The random seed hidden setting to allow the same random seed to
	 *         be used during conformer generation for testing
	 */
	static SettingsModelInteger createRandomSeedMdl() {
		return new SettingsModelInteger(RANDOM_SEED, -1);
	}

	/**
	 * @return The settings model for the {@value #REMOVE_H_S} option
	 */
	static SettingsModelBoolean createRemoveHsModel() {
		return new SettingsModelBoolean(REMOVE_H_S, false);
	}

	/**
	 * @return The settings model for the {@value #IGNORE_H_S} option
	 */
	static SettingsModelBoolean createIgnoreHsRMSDModel() {
		return new SettingsModelBoolean(IGNORE_H_S, true);
	}

	/**
	 * @return The settings model for the {@value #MINIMUM_RMSD} option
	 */
	static SettingsModelDoubleBounded createMinRMSDModel() {
		return new SettingsModelDoubleBounded(MINIMUM_RMSD, 0.35, 0.0, 100.0);
	}

	/**
	 * @return The settings model for the {@value #FILTER_CONFORMERS_BY_RMSD}
	 *         option
	 */
	static SettingsModelBoolean createFilterByRMSDModel() {
		return new SettingsModelBoolean(FILTER_CONFORMERS_BY_RMSD, true);
	}

	/**
	 * @return The settings model for the {@value #USE_TETHERS} option
	 */
	static SettingsModelBoolean createUseTethersModel() {
		return new SettingsModelBoolean(USE_TETHERS, true);
	}

	/**
	 * @return The settings model for the {@value #FILTER_BY_TEMPLATE_RMSD}
	 *         option
	 */
	static SettingsModelBoolean createFilterByTemplateRMSDModel() {
		return new SettingsModelBoolean(FILTER_BY_TEMPLATE_RMSD, true);
	}

	/**
	 * @return The settings model for the {@value #MAX_TEMPLATE_RMSD} option
	 */
	static SettingsModelDoubleBounded createMaxTemplateRMSDModel() {
		return new SettingsModelDoubleBounded(MAX_TEMPLATE_RMSD, 0.05, 0.00,
				100.0);
	}

	/**
	 * @return The settings model for the {@value #CONFORMER_OUTPUT_FORMAT}
	 *         option
	 */
	static SettingsModelString createOutputFormatModel() {
		return new SettingsModelString(CONFORMER_OUTPUT_FORMAT,
				ConformerOutputFormats.getDefault().getActionCommand());
	}

	/**
	 * @return The settings model for the {@value #SHOW_TEMPLATE_MATCH} option
	 */
	static SettingsModelBoolean createShowTemplateMatchModel() {
		return new SettingsModelBoolean(SHOW_TEMPLATE_MATCH, true);
	}

	/**
	 * @return The settings model for the
	 *         {@value #OUTPUT_ACTUAL_TEMPLATE_FOR_ROW} option
	 */
	static SettingsModelBoolean createOutputActualTemplateModel() {
		return new SettingsModelBoolean(OUTPUT_ACTUAL_TEMPLATE_FOR_ROW, true);
	}

	/**
	 * @return The settings model for the {@value #TEMPLATE_MOL_BLOCK} option
	 */
	static SettingsModelMultilineString createTemplateMolBlockModel() {
		return new SettingsModelMultilineString(TEMPLATE_MOL_BLOCK, "");
	}

	/**
	 * @return The settings model for the {@value #TEMPLATE_COLUMN} option
	 */
	static SettingsModelString createTemplateColNameModel() {
		return new SettingsModelString(TEMPLATE_COLUMN, null);
	}

	/**
	 * @return The settings model for the {@value #MAX_RELATIVE_ENERGY} option
	 */
	static SettingsModelDouble createMaxEnergyModel() {
		return new SettingsModelDouble(MAX_RELATIVE_ENERGY, 7.5);
	}

	/**
	 * @return The settings model for the {@value #FILTER_BY_ENERGY} option
	 */
	static SettingsModelBoolean createEnergyFilterModel() {
		return new SettingsModelBoolean(FILTER_BY_ENERGY, false);
	}

	/**
	 * @return The settings model for the {@value #ITERATIONS} option
	 */
	static SettingsModelIntegerBounded createIterationsModel() {
		return new SettingsModelIntegerBounded(ITERATIONS, 1000, 50, 100000);
	}

	/**
	 * @return The settings model for the {@value #FORCEFIELD} option
	 */
	static SettingsModelString createForceFieldModel() {
		return new SettingsModelString(FORCEFIELD,
				ForceFieldFactory.getDefault().getActionCommand());
	}

	/**
	 * @return The settings model for the
	 *         {@value #USE_UFF_FORCE_FIELD_IF_UNABLE_TO_GENERATE_MMFF} option
	 */
	static SettingsModelBoolean createCleanUpModel() {
		return new SettingsModelBoolean(
				USE_UFF_FORCE_FIELD_IF_UNABLE_TO_GENERATE_MMFF, true);
	}

	/**
	 * @return The settings model for the
	 *         {@value #USE_BASIC_KNOWLEDGE_I_E_FLAT_RINGS_ETC} option
	 */
	static SettingsModelBoolean createBasicKnowledgeModel() {
		return new SettingsModelBoolean(USE_BASIC_KNOWLEDGE_I_E_FLAT_RINGS_ETC,
				false);
	}

	/**
	 * @return The settings model for the {@value #USE_EXPERIMENTAL_TORSIONS}
	 *         option
	 */
	static SettingsModelBoolean createExpTorsionModel() {
		return new SettingsModelBoolean(USE_EXPERIMENTAL_TORSIONS, false);
	}

	/**
	 * @return The settings model for the
	 *         {@value #MAX_NUMBER_OF_TRIES_TO_GENERATE_CONFORMER} option
	 */
	static SettingsModelIntegerBounded createNumTriesModel() {
		return new SettingsModelIntegerBounded(
				MAX_NUMBER_OF_TRIES_TO_GENERATE_CONFORMER, 30, 5, 100);
	}

	/**
	 * @return The settings model for the
	 *         {@value #USE_ROTATABLE_BOND_COUNT_TO_DETERMINE_NUMBER_OF_CONFORMERS}
	 *         option
	 */
	static SettingsModelBoolean createUseNRotModel() {
		return new SettingsModelBoolean(
				USE_ROTATABLE_BOND_COUNT_TO_DETERMINE_NUMBER_OF_CONFORMERS,
				true);
	}

	/**
	 * @return The settings model for the {@value #NUMBER_OF_CONFORMERS} option
	 */
	static SettingsModelIntegerBounded createNumConfsModel() {
		return new SettingsModelIntegerBounded(NUMBER_OF_CONFORMERS, 500, 1,
				10000);
	}

	/**
	 * @return The settings model for the {@value #ENSURE_H_S_ADDED} option
	 */
	static SettingsModelBoolean createAddHsModel() {
		return new SettingsModelBoolean(ENSURE_H_S_ADDED, true);
	}

	/**
	 * @return The settings model for the {@value #MOLECULE_COLUMN} option
	 */
	static SettingsModelString createColumnNameModel() {
		return new SettingsModelString(MOLECULE_COLUMN, null);
	}
}
