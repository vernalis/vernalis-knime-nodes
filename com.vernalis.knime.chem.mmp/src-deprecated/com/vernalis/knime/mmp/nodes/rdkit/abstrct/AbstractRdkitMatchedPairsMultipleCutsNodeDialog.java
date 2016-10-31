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
package com.vernalis.knime.mmp.nodes.rdkit.abstrct;

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

import com.vernalis.knime.mmp.FragmentKeyMorganFP;
import com.vernalis.knime.mmp.FragmentationTypes;
import com.vernalis.knime.mmp.MolFormats;

/**
 * Node Dialog implementation for the MMP and Fragmentation nodes.
 * 
 * @author "Stephen Roughley  <s.roughley@vernalis.com>"
 * 
 */
public class AbstractRdkitMatchedPairsMultipleCutsNodeDialog extends
		DefaultNodeSettingsPane {
	SettingsModelBoolean m_hasChangingAtoms, m_hasHARatioFilter, m_AddHs,
			m_stripHsAtEnd, m_trackCutConnectivity, m_apFingerprints,
			m_fpUseBondTypes, m_fpUseChirality;
	SettingsModelIntegerBounded m_maxChangingAtoms, m_NumCuts, m_morganRadius,
			m_fpLength;
	SettingsModelDoubleBounded m_minHARatioFilter;
	SettingsModelString m_fragmentationType, m_customRSMARTS;

	/**
	 * New pane for configuring the MatchedPairsMultipleCuts node.
	 * 
	 * @param includeMMPGenerationOptions
	 *            Setting to show optional 'Show unchanging portion' dialog
	 *            option, and options relating to transform output
	 */
	@SuppressWarnings("unchecked")
	public AbstractRdkitMatchedPairsMultipleCutsNodeDialog(
			boolean includeMMPGenerationOptions) {

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

		addDialogComponent(new DialogComponentButtonGroup(m_fragmentationType,
				"Select the fragmentation type", true,
				FragmentationTypes.values()));
		addDialogComponent(new DialogComponentString(m_customRSMARTS,
				"User rSMARTS:"));

		m_NumCuts = createCutsModel();
		m_trackCutConnectivity = createTrackCutConnectivityModel();
		m_NumCuts.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				updateNumCuts();
				updateCanFp();
			}
		});
		m_trackCutConnectivity.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				updateCanFp();

			}
		});

		addDialogComponent(new DialogComponentNumber(m_NumCuts,
				"Number of cuts", 1));
		addDialogComponent(new DialogComponentBoolean(m_trackCutConnectivity,
				"Track connectivity?"));

		/*
		 * *****************************
		 * * The ADVANCED SETTINGS TAB * *****************************
		 */
		createNewTab("Advanced Settings");
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
		if (includeMMPGenerationOptions) {
			addDialogComponent(new DialogComponentBoolean(
					createOutputKeyModel(), "Show unchanging portion"));
		}
		addDialogComponent(new DialogComponentBoolean(
				createOutputChangingHACountsModel(),
				"Show number of changing atoms"));
		addDialogComponent(new DialogComponentBoolean(
				createOutputHARatiosModel(),
				"Show ratio of constant / changing heavy atoms"));
		if (includeMMPGenerationOptions) {
			addDialogComponent(new DialogComponentBoolean(
					createShowReverseTransformsModel(),
					"Show reverse-direction transforms"));

			addDialogComponent(new DialogComponentBoolean(
					createShowSmartsTransformsModel(),
					"Include Reaction SMARTS"));
		}

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

		protected void updateCanFp() {
		m_apFingerprints.setEnabled(m_NumCuts.getIntValue() == 1
				|| m_trackCutConnectivity.getBooleanValue());
		updateFingerprintEnabled();
	}

	/*
	 * Methods for the change listeners
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

	protected void updateHARatio() {
		m_minHARatioFilter.setEnabled(m_hasHARatioFilter.getBooleanValue());
	}

	protected void updateNumCuts() {
		// We can only add Hs in the RDKit node if numCuts = 1
		m_AddHs.setEnabled(m_NumCuts.getIntValue() == 1);
		m_stripHsAtEnd.setEnabled(m_NumCuts.getIntValue() == 1
				&& m_AddHs.getBooleanValue());
		// We only worry about tracking cuts for multiple cuts
		m_trackCutConnectivity.setEnabled(m_NumCuts.getIntValue() > 1);
	}

	protected void updateStripHs() {
		m_stripHsAtEnd.setEnabled(m_NumCuts.getIntValue() == 1
				&& m_AddHs.getBooleanValue());
	}

	protected void updateChangingAtoms() {
		m_maxChangingAtoms.setEnabled(m_hasChangingAtoms.getBooleanValue());

	}

	/*
	 * Methods for Settings Model Creation
	 */
	/** Create Settings Model for Morgan FP Length */
	public static SettingsModelIntegerBounded createFpLengthModel() {
		return new SettingsModelIntegerBounded("FP Length",
				(int) FragmentKeyMorganFP.DEFAULT_FP_LENGTH, 64,
				Integer.MAX_VALUE);
	}

	/** Create Settings Model for Morgan FP Radius */
	public static SettingsModelIntegerBounded createMorganRadiusModel() {
		return new SettingsModelIntegerBounded("FP Morgan Radius",
				(int) FragmentKeyMorganFP.DEFAULT_FP_RADIUS, 1,
				Integer.MAX_VALUE);
	}

	/** Create Settings Model for use of chirality in FP generation */
	public static SettingsModelBoolean createFpUseChiralityModel() {
		return new SettingsModelBoolean("FP Use Chirality",
				FragmentKeyMorganFP.DEFAULT_USE_CHIRALITY);
	}

	/** Create Settings Model for use of bond types in FP generation */
	public static SettingsModelBoolean createFpUseBondTypesModel() {
		return new SettingsModelBoolean("FP Use Bond Types",
				FragmentKeyMorganFP.DEFAULT_USE_BOND_TYPES);
	}

	/** Create Settings Model for use of Attachment Point Fingerprints */
	public static SettingsModelBoolean createApFingerprintsModel() {
		return new SettingsModelBoolean("Attachment Point Fingerprints", true);
	}

	/** Create settings model for extra failure reasons column */
	public static SettingsModelBoolean createAddFailReasonModel() {
		return new SettingsModelBoolean("Add failure reasons", true);
	}

	/** Create model for customReaction SMARTS */
	public static SettingsModelString createCustomSMARTSModel() {
		return new SettingsModelString("Custom rSMARTS", null);
	}

	/** Create model to include Reaction SMARTS */
	public static SettingsModelBoolean createShowSmartsTransformsModel() {
		return new SettingsModelBoolean("Include Reaction SMARTS", true);
	}

	/** Create model to include reverse transforms in outpu */
	public static SettingsModelBoolean createShowReverseTransformsModel() {
		return new SettingsModelBoolean("Show Reverse Transforms", true);
	}

	/*
	 * Static methods for the creation of settings models
	 */
	/** Create the SM for tracking cut connectivity */
	public static SettingsModelBoolean createTrackCutConnectivityModel() {
		return new SettingsModelBoolean("Track Cut Connectivity", true);
	}

	/**
	 * Create Settings model for the maximum number of changing atoms
	 */
	public static SettingsModelIntegerBounded createMaxChangingAtomsModel() {
		return new SettingsModelIntegerBounded("Max Changing Heavy Atoms", 12,
				0, Integer.MAX_VALUE);
	}

	/**
	 * Create Settings model for the maximum ration of unchanging to changing
	 * atoms
	 */
	public static SettingsModelDoubleBounded createRatioModel() {
		return new SettingsModelDoubleBounded(
				"Max ratio of unchanging to changing heavy atoms", 1.0, 0.0001,
				Double.MAX_VALUE);
	}

	/**
	 * Create the settings model for the has changing atoms filter
	 */
	public static SettingsModelBoolean createHasMaxChangingAtomsModel() {
		return new SettingsModelBoolean("Has Max Changing Atoms Filter", true);
	}

	/**
	 * Create the settings model for the has atom ratio filter
	 */
	public static SettingsModelBoolean createHasHARatioFilterModel() {
		return new SettingsModelBoolean("Has Max Changing Atoms Ratio Filter",
				true);
	}

	/**
	 * Create the settings model for the molecule column
	 */
	public static SettingsModelString createMolColumnSettingsModel() {
		return new SettingsModelString("Molecule Column", "");
	}

	/**
	 * Create the settings model the the ID column
	 */
	public static SettingsModelString createIDColumnSettingsModel() {
		return new SettingsModelString("ID Column", "");
	}

	/**
	 * Create the settings model for the number of cuts
	 */
	public static SettingsModelIntegerBounded createCutsModel() {
		return new SettingsModelIntegerBounded("Number of cuts", 1, 1, 10);
	}

	/** Create the settings model for the option to add hydrogens */
	public static SettingsModelBoolean createAddHModel() {
		return new SettingsModelBoolean("Add Hs", false);
	}

	/** Create the settings model for the strip H's at the end option */
	public static SettingsModelBoolean createStripHModel() {
		return new SettingsModelBoolean("Remove Explicit H's from output", true);
	}

	/** Create settings model for the SMIRKS fragmentation */
	public static SettingsModelString createSMIRKSModel() {
		return new SettingsModelString("Fragmentation SMIRKS",
				FragmentationTypes.getDefaultMethod().getActionCommand());
	}

	/** Create the SM for outputting the fragment key */
	public static SettingsModelBoolean createOutputKeyModel() {
		return new SettingsModelBoolean("Output fragment key", false);
	}

	/** Create the SM for outputting the changing HA count */
	public static SettingsModelBoolean createOutputChangingHACountsModel() {
		return new SettingsModelBoolean("Output changing HA counts", false);
	}

	/** Create the SM for outputting the HAC ratios */
	public static SettingsModelBoolean createOutputHARatiosModel() {
		return new SettingsModelBoolean(
				"Output changing / unchanging HA ratios", false);
	}
}
