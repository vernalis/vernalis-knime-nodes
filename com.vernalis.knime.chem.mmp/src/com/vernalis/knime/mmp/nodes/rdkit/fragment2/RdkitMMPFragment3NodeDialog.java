/*******************************************************************************
 * Copyright (c) 2014, 2015, Vernalis (R&D) Ltd
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
 *******************************************************************************/
package com.vernalis.knime.mmp.nodes.rdkit.fragment2;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.mmp.FragmentationTypes;
import com.vernalis.knime.mmp.MMPConstants;
import com.vernalis.knime.mmp.MolFormats;

/**
 * Node Dialog implementation for the MMP Fragmentation node.
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class RdkitMMPFragment3NodeDialog extends DefaultNodeSettingsPane {
	SettingsModelBoolean m_hasChangingAtoms, m_hasHARatioFilter, m_AddHs,
			m_stripHsAtEnd, m_apFingerprints, m_fpUseBondTypes,
			m_fpUseChirality, m_allowTwoCutsToBondValue;
	SettingsModelIntegerBounded m_maxChangingAtoms, m_NumCuts, m_morganRadius,
			m_fpLength;
	SettingsModelDoubleBounded m_minHARatioFilter;
	SettingsModelString m_fragmentationType, m_customRSMARTS;

	/**
	 * New pane for configuring the MatchedPairsMultipleCuts node.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public RdkitMMPFragment3NodeDialog() {

		addDialogComponent(new DialogComponentColumnNameSelection(
				createMolColumnSettingsModel(), "Select Molecule column", 0,
				MolFormats.m_RDKitmolFormats.toArray(new Class[0])));

		addDialogComponent(new DialogComponentColumnNameSelection(
				createIDColumnSettingsModel(), "Select Molecule IDs column", 0,
				StringValue.class));

		m_fragmentationType = createSMIRKSModel();
		m_customRSMARTS = createCustomSMARTSModel();
		m_fragmentationType.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				m_customRSMARTS.setEnabled(FragmentationTypes
						.valueOf(m_fragmentationType.getStringValue()) == FragmentationTypes.USER_DEFINED);
			}
		});

		m_customRSMARTS
				.setEnabled(FragmentationTypes.valueOf(m_fragmentationType
						.getStringValue()) == FragmentationTypes.USER_DEFINED);

		createNewGroup("Select the fragmentation type");
		addDialogComponent(new DialogComponentButtonGroup(m_fragmentationType,
				null, true, FragmentationTypes.values()));
		addDialogComponent(new DialogComponentString(m_customRSMARTS,
				"User rSMARTS:"));
		closeCurrentGroup();

		m_NumCuts = createCutsModel();
		m_allowTwoCutsToBondValue = createAllowTwoCutsToBondValueModel();
		m_NumCuts.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				updateNumCuts();
				updateCanFp();
			}
		});

		m_allowTwoCutsToBondValue.setEnabled(m_NumCuts.getIntValue() == 2);

		addDialogComponent(new DialogComponentNumber(m_NumCuts,
				"Number of cuts", 1));
		addDialogComponent(new DialogComponentBoolean(
				m_allowTwoCutsToBondValue,
				"Allow 2 cuts along single bond giving a single bond as 'value'?"));

		/*
		 * *****************************
		 * * The ADVANCED SETTINGS TAB * *****************************
		 */
		createNewTab("Advanced Settings");
		createNewGroup("Chirality");
		addDialogComponent(new DialogComponentBoolean(createProchiralModel(),
				"Treat no undefined chiral centres as chiral"));
		closeCurrentGroup();

		createNewGroup("Explicit Hydrogens");
		m_AddHs = createAddHModel();
		m_stripHsAtEnd = createStripHModel();
		m_AddHs.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				updateStripHs();

			}
		});
		addDialogComponent(new DialogComponentBoolean(m_AddHs,
				"Add H's prior to fragmentation (Recommended for n=1)"));
		addDialogComponent(new DialogComponentBoolean(m_stripHsAtEnd,
				"Remove Explicit H's from output"));
		closeCurrentGroup();
		createNewGroup("Variable Heavy Atom filter");
		m_hasChangingAtoms = createHasMaxChangingAtomsModel();
		m_maxChangingAtoms = createMaxChangingAtomsModel();
		m_hasChangingAtoms.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateChangingAtoms();
			}
		});

		addDialogComponent(new DialogComponentBoolean(m_hasChangingAtoms,
				"Filter by maximum number of changing heavy atoms?"));
		addDialogComponent(new DialogComponentNumber(m_maxChangingAtoms,
				"Maximum Number of variable heavy atoms:", 1));

		createNewGroup("Heavy Atom Ratio Filter");
		m_hasHARatioFilter = createHasHARatioFilterModel();
		m_minHARatioFilter = createRatioModel();
		m_hasHARatioFilter.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				updateHARatio();

			}
		});

		addDialogComponent(new DialogComponentBoolean(m_hasHARatioFilter,
				"Filter by ratio of changing / unchanging atoms?"));
		addDialogComponent(new DialogComponentNumber(m_minHARatioFilter,
				"Minimum ratio of changing to unchanging heavy atoms", 0.1));

		/*
		 * ***************************
		 * * The OUTPUT SETTINGS tab * ***************************
		 */
		createNewTab("Output Settings");

		addDialogComponent(new DialogComponentBoolean(
				createOutputChangingHACountsModel(),
				"Show number of changing atoms"));
		addDialogComponent(new DialogComponentBoolean(
				createOutputHARatiosModel(),
				"Show ratio of constant / changing heavy atoms"));

		addDialogComponent(new DialogComponentBoolean(
				createAddFailReasonModel(),
				"Add failure reasons to 2nd output table"));

		/*
		 * Attachment Point Fingerprints Tab
		 */
		createNewTab("Attachment Point Fingerprints");
		m_apFingerprints = createApFingerprintsModel();
		m_fpUseBondTypes = createFpUseBondTypesModel();
		m_fpUseChirality = createFpUseChiralityModel();
		m_morganRadius = createMorganRadiusModel();
		m_fpLength = createFpLengthModel();
		m_apFingerprints.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				updateFingerprintEnabled();
			}
		});
		updateFingerprintEnabled();
		addDialogComponent(new DialogComponentBoolean(m_apFingerprints,
				"Add Attachment Point Fingerprints"));
		addDialogComponent(new DialogComponentNumber(m_fpLength,
				"Fingerprint Length", 128));
		addDialogComponent(new DialogComponentNumber(m_morganRadius,
				"Morgan Radius", 1));
		addDialogComponent(new DialogComponentBoolean(m_fpUseBondTypes,
				"Use Bond Types"));
		addDialogComponent(new DialogComponentBoolean(m_fpUseChirality,
				"Use chirality"));
	}

	/**
	 * Update fingerprints settings
	 */
	protected void updateCanFp() {

		updateFingerprintEnabled();
	}

	/**
	 * Update fingerprints settings
	 */
	protected void updateFingerprintEnabled() {
		m_fpUseBondTypes.setEnabled(m_apFingerprints.isEnabled()
				&& m_apFingerprints.getBooleanValue());
		m_fpLength.setEnabled(m_apFingerprints.isEnabled()
				&& m_apFingerprints.getBooleanValue());
		m_fpUseChirality.setEnabled(m_apFingerprints.isEnabled()
				&& m_apFingerprints.getBooleanValue());
		m_morganRadius.setEnabled(m_apFingerprints.isEnabled()
				&& m_apFingerprints.getBooleanValue());
	}

	/**
	 * Updates the HA Ratio filter setting
	 */
	protected void updateHARatio() {
		m_minHARatioFilter.setEnabled(m_hasHARatioFilter.getBooleanValue());
	}

	/**
	 * Update settings based on the number of cuts
	 */
	protected void updateNumCuts() {
		// We can only add Hs in the RDKit node if numCuts = 1
		m_AddHs.setEnabled(m_NumCuts.getIntValue() == 1);
		m_stripHsAtEnd.setEnabled(m_NumCuts.getIntValue() == 1
				&& m_AddHs.getBooleanValue());
		m_allowTwoCutsToBondValue.setEnabled(m_NumCuts.getIntValue() == 2);
	}

	/**
	 * Update strip H's settings
	 */
	protected void updateStripHs() {
		m_stripHsAtEnd.setEnabled(m_NumCuts.getIntValue() == 1
				&& m_AddHs.getBooleanValue());
	}

	/**
	 * Update the max changing atoms filter
	 */
	protected void updateChangingAtoms() {
		m_maxChangingAtoms.setEnabled(m_hasChangingAtoms.getBooleanValue());

	}

	/**
	 * Create Settings Model for the option to deal with generated chirality for
	 * molecules with no centres
	 */
	public static SettingsModelBoolean createProchiralModel() {
		return new SettingsModelBoolean("Treat Prochiral as Chiral", true);
	}

	/**
	 * Create the track connectivity settings model
	 * 
	 * @deprecated Setting is no longer used - always {@code true}
	 */
	@Deprecated
	public static SettingsModelBoolean createTrackCutConnectivityModel() {
		return new SettingsModelBoolean("Track cut connectivity",
				MMPConstants.DEFAULT_TRACK_CONNECTIVITY);
	}

	/**
	 * Create Settings Model for option to allow 2 cuts to generate a single
	 * bond as a value
	 */
	public static SettingsModelBoolean createAllowTwoCutsToBondValueModel() {
		return new SettingsModelBoolean("Two Cuts to Bond Value",
				MMPConstants.DEFAULT_ALLOW_2_CUTS_TO_SINGLE_BOND);
	}

	/** Create Settings Model for Morgan FP Length */
	public static SettingsModelIntegerBounded createFpLengthModel() {
		return new SettingsModelIntegerBounded("FP Length",
				(int) MMPConstants.DEFAULT_FP_LENGTH,
				MMPConstants.MINIMUM_FINGERPRINT_LENGTH, Integer.MAX_VALUE);
	}

	/** Create Settings Model for Morgan FP Radius */
	public static SettingsModelIntegerBounded createMorganRadiusModel() {
		return new SettingsModelIntegerBounded("FP Morgan Radius",
				(int) MMPConstants.DEFAULT_FP_RADIUS,
				MMPConstants.MINIMUM_MORGAN_RADIUS, Integer.MAX_VALUE);
	}

	/** Create Settings Model for use of chirality in FP generation */
	public static SettingsModelBoolean createFpUseChiralityModel() {
		return new SettingsModelBoolean("FP Use Chirality",
				MMPConstants.DEFAULT_USE_CHIRALITY);
	}

	/** Create Settings Model for use of bond types in FP generation */
	public static SettingsModelBoolean createFpUseBondTypesModel() {
		return new SettingsModelBoolean("FP Use Bond Types",
				MMPConstants.DEFAULT_USE_BOND_TYPES);
	}

	/** Create Settings Model for use of Attachment Point Fingerprints */
	public static SettingsModelBoolean createApFingerprintsModel() {
		return new SettingsModelBoolean("Attachment Point Fingerprints",
				MMPConstants.DEFAULT_ADD_AP_FINGERPRINTS);
	}

	/** Create settings model for extra failure reasons column */
	public static SettingsModelBoolean createAddFailReasonModel() {
		return new SettingsModelBoolean("Add failure reasons",
				MMPConstants.DEFAULT_ADD_FAIL_REASONS);
	}

	/** Create model for customReaction SMARTS */
	public static SettingsModelString createCustomSMARTSModel() {
		return new SettingsModelString("Custom rSMARTS", null);
	}

	/** Create model to include Reaction SMARTS */
	public static SettingsModelBoolean createShowSmartsTransformsModel() {
		return new SettingsModelBoolean("Include Reaction SMARTS",
				MMPConstants.DEFAULT_OUTPUT_REACTION_SMARTS);
	}

	/** Create model to include reverse transforms in outpu */
	public static SettingsModelBoolean createShowReverseTransformsModel() {
		return new SettingsModelBoolean("Show Reverse Transforms",
				MMPConstants.DEFAULT_OUTPUT_REVERSE_TRANSFORMS);
	}

	/**
	 * Create Settings model for the maximum number of changing atoms
	 */
	public static SettingsModelIntegerBounded createMaxChangingAtomsModel() {
		return new SettingsModelIntegerBounded("Max Changing Heavy Atoms",
				MMPConstants.DEFAULT_MAX_CHANGING_HA, 0, Integer.MAX_VALUE);
	}

	/**
	 * Create Settings model for the maximum ration of unchanging to changing
	 * atoms
	 */
	public static SettingsModelDoubleBounded createRatioModel() {
		return new SettingsModelDoubleBounded(
				"Max ratio of unchanging to changing heavy atoms",
				MMPConstants.DEFAULT_MAX_HA_RATIO, 0.0001, Double.MAX_VALUE);
	}

	/**
	 * Create the settings model for the has changing atoms filter
	 */
	public static SettingsModelBoolean createHasMaxChangingAtomsModel() {
		return new SettingsModelBoolean("Has Max Changing Atoms Filter",
				MMPConstants.DEFAULT_HAS_MAX_CHANGING_HA);
	}

	/**
	 * Create the settings model for the has atom ratio filter
	 */
	public static SettingsModelBoolean createHasHARatioFilterModel() {
		return new SettingsModelBoolean("Has Max Changing Atoms Ratio Filter",
				MMPConstants.DEFAULT_HAS_CHANGING_HA_RATIO);
	}

	/**
	 * Create the settings model for the molecule column
	 */
	public static SettingsModelString createMolColumnSettingsModel() {
		return new SettingsModelString("Molecule Column", null);
	}

	/**
	 * Create the settings model the the ID column
	 */
	public static SettingsModelString createIDColumnSettingsModel() {
		return new SettingsModelString("ID Column", null);
	}

	/**
	 * Create the settings model for the number of cuts
	 */
	public static SettingsModelIntegerBounded createCutsModel() {
		return new SettingsModelIntegerBounded("Number of cuts",
				MMPConstants.DEFAULT_NUM_CUTS,
				MMPConstants.MINIMUM_NUMBER_OF_CUTS,
				MMPConstants.MAXIMUM_NUMBER_OF_CUTS);
	}

	/** Create the settings model for the option to add hydrogens */
	public static SettingsModelBoolean createAddHModel() {
		return new SettingsModelBoolean("Add Hs", MMPConstants.DEFAULT_ADD_H);
	}

	/** Create the settings model for the strip H's at the end option */
	public static SettingsModelBoolean createStripHModel() {
		return new SettingsModelBoolean("Remove Explicit H's from output",
				MMPConstants.DEFAULT_REMOVE_H_POST_FRAGMENTATION);
	}

	/** Create settings model for the SMIRKS fragmentation */
	public static SettingsModelString createSMIRKSModel() {
		return new SettingsModelString("Fragmentation SMIRKS",
				FragmentationTypes.getDefaultMethod().getActionCommand());
	}

	/** Create the SM for outputting the fragment key */
	public static SettingsModelBoolean createOutputKeyModel() {
		return new SettingsModelBoolean("Output fragment key",
				MMPConstants.DEFAULT_OUTPUT_FRAGMENT_KEY);
	}

	/** Create the SM for outputting the changing HA count */
	public static SettingsModelBoolean createOutputChangingHACountsModel() {
		return new SettingsModelBoolean("Output changing HA counts",
				MMPConstants.DEFAULT_OUTPUT_DELTA_HAC);
	}

	/** Create the SM for outputting the HAC ratios */
	public static SettingsModelBoolean createOutputHARatiosModel() {
		return new SettingsModelBoolean(
				"Output changing / unchanging HA ratios",
				MMPConstants.DEFAULT_OUTPUT_CHANGING_UNCHANGING_HAC_RATIOS);
	}
}
