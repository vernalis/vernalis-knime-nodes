/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColorChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColor;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

import com.vernalis.knime.mmp.MMPConstants;
import com.vernalis.knime.mmp.fragutils.FragmentationUtilsFactory;
import com.vernalis.knime.mmp.nodes.fragutil.abstrct.AbstractMMPFragmentationFactoryNodeDialog;

/**
 * Node dialog pane for the MMP Fragment nodes
 */
public class AbstractMMPFragmentNodeDialog<T, U>
		extends AbstractMMPFragmentationFactoryNodeDialog<T, U> {

	/**
	 * Constructor
	 * 
	 * @param fragUtilityFactory
	 *            The {@link FragmentationUtilsFactory} instance for the node
	 * @param isMulticut
	 *            Is the node multicut?
	 */
	public AbstractMMPFragmentNodeDialog(FragmentationUtilsFactory<T, U> fragUtilityFactory,
			boolean isMulticut) {
		super(fragUtilityFactory, isMulticut);
		/*
		 * @formatter:off
		 * ****************************************** 
		 * The FRAGMENTATION FILTERING SETTINGS TAB *
		 * ******************************************
		 * @formatter:on
		 */
		createNewTab("Fragmentation Filtering Settings");

		createNewGroup("Complexity");
		SettingsModelBoolean limitByComplexityMdl = createLimitByComplexityModel();
		SettingsModelIntegerBounded maxFragmentationsMdl = createMaxFragmentationsModel();
		limitByComplexityMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				maxFragmentationsMdl.setEnabled(limitByComplexityMdl.getBooleanValue());
			}
		});

		addDialogComponent(
				new DialogComponentBoolean(limitByComplexityMdl, MMPConstants.LIMIT_BY_COMPLEXITY));
		addDialogComponent(
				new DialogComponentNumber(maxFragmentationsMdl, "Maximum Fragmentations", 1000));

		createNewGroup("Chirality");
		addDialogComponent(new DialogComponentBoolean(createProchiralModel(),
				"Treat no undefined chiral centres as chiral"));
		closeCurrentGroup();

		createNewGroup("Variable Heavy Atom filter");
		SettingsModelBoolean hasChangingAtomsMdl = createHasMaxChangingAtomsModel();
		SettingsModelIntegerBounded maxChangingAtomsMdl = createMaxChangingAtomsModel();
		hasChangingAtomsMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				maxChangingAtomsMdl.setEnabled(hasChangingAtomsMdl.getBooleanValue());

			}
		});
		maxChangingAtomsMdl.setEnabled(hasChangingAtomsMdl.getBooleanValue());

		addDialogComponent(new DialogComponentBoolean(hasChangingAtomsMdl,
				"Filter by maximum number of changing heavy atoms?"));
		addDialogComponent(new DialogComponentNumber(maxChangingAtomsMdl,
				"Maximum Number of variable heavy atoms:", 1));

		createNewGroup("Heavy Atom Ratio Filter");
		SettingsModelBoolean hasHARatioFilterMdl = createHasHARatioFilterModel();
		SettingsModelDoubleBounded minHARatioFilterMdl = createHARatioModel();
		hasHARatioFilterMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				minHARatioFilterMdl.setEnabled(hasHARatioFilterMdl.getBooleanValue());
			}
		});
		minHARatioFilterMdl.setEnabled(hasHARatioFilterMdl.getBooleanValue());
		addDialogComponent(new DialogComponentBoolean(hasHARatioFilterMdl,
				"Filter by ratio of changing / unchanging atoms?"));
		addDialogComponent(new DialogComponentNumber(minHARatioFilterMdl,
				"Minimum ratio of changing to unchanging heavy atoms", 0.1));

		/*
		 * @formatter:off
		 * ***************************
		 * The OUTPUT SETTINGS tab *
		 * ***************************
		 * @formatter:on
		 */
		createNewTab("Output Settings");

		addDialogComponent(new DialogComponentBoolean(createOutputChangingHACountsModel(),
				"Show number of changing atoms"));
		addDialogComponent(new DialogComponentBoolean(createOutputHARatiosModel(),
				"Show ratio of constant / changing heavy atoms"));

		addDialogComponent(new DialogComponentBoolean(createAddFailReasonModel(),
				"Add failure reasons to 2nd output table"));

		if (fragUtilityFactory.getRendererType() != null) {
			createNewGroup("Fragmentation rendering");
			final SettingsModelBoolean renderFragmentationMdl = createRenderFragmentationModel();
			final SettingsModelBoolean renderBreakingBondMdl = createRenderBreakingBondModel();
			final SettingsModelColor breakingBondColourMdl = createBreakingBondColourModel();
			final SettingsModelBoolean renderKeyBondMdl = createRenderKeyBondModel();
			final SettingsModelColor keyColourMdl = createKeyColourModel();
			final SettingsModelBoolean renderValueBondMdl = createRenderValueBondModel();
			final SettingsModelColor valueColourMdl = createValueColourModel();

			renderFragmentationMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					if (renderFragmentationMdl.getBooleanValue()) {
						renderBreakingBondMdl.setEnabled(true);
						renderKeyBondMdl.setEnabled(true);
						renderValueBondMdl.setEnabled(true);
						breakingBondColourMdl.setEnabled(renderBreakingBondMdl.getBooleanValue());
						keyColourMdl.setEnabled(renderKeyBondMdl.getBooleanValue());
						valueColourMdl.setEnabled(renderValueBondMdl.getBooleanValue());
					} else {
						renderBreakingBondMdl.setEnabled(false);
						renderKeyBondMdl.setEnabled(false);
						renderValueBondMdl.setEnabled(false);
						breakingBondColourMdl.setEnabled(false);
						keyColourMdl.setEnabled(false);
						valueColourMdl.setEnabled(false);
					}

				}
			});

			renderBreakingBondMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					breakingBondColourMdl.setEnabled(renderBreakingBondMdl.getBooleanValue());
				}
			});
			renderKeyBondMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					keyColourMdl.setEnabled(renderKeyBondMdl.getBooleanValue());

				}
			});
			renderValueBondMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					valueColourMdl.setEnabled(renderValueBondMdl.getBooleanValue());
				}
			});

			if (renderFragmentationMdl.getBooleanValue()) {
				renderBreakingBondMdl.setEnabled(true);
				renderKeyBondMdl.setEnabled(true);
				renderValueBondMdl.setEnabled(true);
				breakingBondColourMdl.setEnabled(renderBreakingBondMdl.getBooleanValue());
				keyColourMdl.setEnabled(renderKeyBondMdl.getBooleanValue());
				valueColourMdl.setEnabled(renderValueBondMdl.getBooleanValue());
			} else {
				renderBreakingBondMdl.setEnabled(false);
				renderKeyBondMdl.setEnabled(false);
				renderValueBondMdl.setEnabled(false);
				breakingBondColourMdl.setEnabled(false);
				keyColourMdl.setEnabled(false);
				valueColourMdl.setEnabled(false);
			}

			addDialogComponent(
					new DialogComponentBoolean(renderFragmentationMdl, "Render Fragmentation"));
			setHorizontalPlacement(true);
			addDialogComponent(new DialogComponentBoolean(renderBreakingBondMdl,
					MMPConstants.SHOW_BREAKING_BONDS));
			addDialogComponent(new DialogComponentColorChooser(breakingBondColourMdl,
					"Breaking bond colour", true));
			setHorizontalPlacement(false);
			setHorizontalPlacement(true);
			addDialogComponent(new DialogComponentBoolean(renderKeyBondMdl, "Show key"));
			addDialogComponent(new DialogComponentColorChooser(keyColourMdl, "Key colour", true));
			setHorizontalPlacement(false);
			setHorizontalPlacement(true);
			addDialogComponent(new DialogComponentBoolean(renderValueBondMdl, "Show value"));
			addDialogComponent(
					new DialogComponentColorChooser(valueColourMdl, "Value colour", true));
			setHorizontalPlacement(false);
			closeCurrentGroup();
		}
		createNewGroup("Incoming Columns to Keep");
		addDialogComponent(new DialogComponentColumnFilter2(createKeptColumnsModel(), 0));

		/*
		 * Attachment Point Fingerprints Tab
		 */
		createNewTab("Fingerprints");
		createNewGroup("'Value' fingerprints");
		addDialogComponent(new DialogComponentBoolean(createAddValueGraphDistanceFPModel(),
				"Show 'Value' attachment point graph distances fingerprint"));
		closeCurrentGroup();

		createNewGroup("'Key' fingerprints");
		SettingsModelBoolean apFingerprintsMdl = createApFingerprintsModel();
		SettingsModelIntegerBounded morganRadiusMdl = createMorganRadiusModel();
		SettingsModelIntegerBounded fpLengthMdl = createFpLengthModel();
		SettingsModelBoolean fpUseBondTypesMdl;
		SettingsModelBoolean fpUseChiralityMdl;
		if (fragUtilityFactory.hasExtendedFingerprintOptions()) {
			fpUseBondTypesMdl = createFpUseBondTypesModel();
			fpUseChiralityMdl = createFpUseChiralityModel();
		} else {
			fpUseBondTypesMdl = null;
			fpUseChiralityMdl = null;
		}
		apFingerprintsMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				fpLengthMdl.setEnabled(
						apFingerprintsMdl.isEnabled() && apFingerprintsMdl.getBooleanValue());
				morganRadiusMdl.setEnabled(
						apFingerprintsMdl.isEnabled() && apFingerprintsMdl.getBooleanValue());
				if (fpUseBondTypesMdl != null) {
					fpUseBondTypesMdl.setEnabled(
							apFingerprintsMdl.isEnabled() && apFingerprintsMdl.getBooleanValue());
				}
				if (fpUseChiralityMdl != null) {
					fpUseChiralityMdl.setEnabled(
							apFingerprintsMdl.isEnabled() && apFingerprintsMdl.getBooleanValue());
				}
			}
		});
		// Initialise enabled states
		fpLengthMdl
				.setEnabled(apFingerprintsMdl.isEnabled() && apFingerprintsMdl.getBooleanValue());
		morganRadiusMdl
				.setEnabled(apFingerprintsMdl.isEnabled() && apFingerprintsMdl.getBooleanValue());
		if (fpUseBondTypesMdl != null) {
			fpUseBondTypesMdl.setEnabled(
					apFingerprintsMdl.isEnabled() && apFingerprintsMdl.getBooleanValue());
		}
		if (fpUseChiralityMdl != null) {
			fpUseChiralityMdl.setEnabled(
					apFingerprintsMdl.isEnabled() && apFingerprintsMdl.getBooleanValue());
		}

		// Add dialog components
		addDialogComponent(
				new DialogComponentBoolean(apFingerprintsMdl, "Add Attachment Point Fingerprints"));
		addDialogComponent(new DialogComponentNumber(fpLengthMdl, "Fingerprint Length", 128));
		addDialogComponent(new DialogComponentNumber(morganRadiusMdl, "Morgan Radius", 1));
		if (fragUtilityFactory.hasExtendedFingerprintOptions()) {
			addDialogComponent(new DialogComponentBoolean(fpUseBondTypesMdl, "Use Bond Types"));
			addDialogComponent(new DialogComponentBoolean(fpUseChiralityMdl, "Use chirality"));
		}
	}

	/**
	 * @return The settings model for the value colour
	 */
	static SettingsModelColor createValueColourModel() {
		return new SettingsModelColor(MMPConstants.VALUE_COLOUR, MMPConstants.DEFAULT_VALUE_COLOUR);
	}

	/**
	 * @return The settings model for the 'show value' model
	 */
	static SettingsModelBoolean createRenderValueBondModel() {
		return new SettingsModelBoolean(MMPConstants.SHOW_VALUE, MMPConstants.DEFAULT_RENDER_VALUE);
	}

	/**
	 * @return The settings model for the key colour
	 */
	static SettingsModelColor createKeyColourModel() {
		return new SettingsModelColor(MMPConstants.KEY_COLOUR, MMPConstants.DEFAULT_KEY_COLOUR);
	}

	/**
	 * @return The settings model for the 'show key' model
	 */
	static SettingsModelBoolean createRenderKeyBondModel() {
		return new SettingsModelBoolean(MMPConstants.SHOW_KEY, MMPConstants.DEFAULT_RENDER_KEY);
	}

	/**
	 * @return The settings model for the breaking bond(s) colour
	 */
	static SettingsModelColor createBreakingBondColourModel() {
		return new SettingsModelColor(MMPConstants.BREAKING_BONDS_COLOR,
				MMPConstants.DEFAULT_BREAKING_BONDS_COLOUR);
	}

	/**
	 * @return The settings model for the 'show breaking bonds' model
	 */
	static SettingsModelBoolean createRenderBreakingBondModel() {
		return new SettingsModelBoolean(MMPConstants.SHOW_BREAKING_BONDS,
				MMPConstants.DEFAULT_RENDER_BREAKING_BONDS);
	}

	/**
	 * @return The settings model for the 'Render Fragmentation' option
	 */
	static SettingsModelBoolean createRenderFragmentationModel() {
		return new SettingsModelBoolean(MMPConstants.RENDER_FRAGMENTATION,
				MMPConstants.DEFAULT_RENDER_FRAGMENTATION);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.mmp.nodes.fragment.abstrct.
	 * AbstractMMPFragmentationFactoryNodeDialog#addAdditionalColumnSelectors()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void addAdditionalColumnSelectors() {
		addDialogComponent(new DialogComponentColumnNameSelection(createIDColumnSettingsModel(),
				"Select Molecule IDs column", 0, StringValue.class));
		addDialogComponent(
				new DialogComponentBoolean(createAllowHiliteModel(), MMPConstants.ALLOW_HI_LITING));
	}

	/**
	 * @return The settings model for the hilite mapping
	 */
	static SettingsModelBoolean createAllowHiliteModel() {
		return new SettingsModelBoolean(MMPConstants.ALLOW_HI_LITING,
				MMPConstants.DEFAULT_ALLOW_HILITE);
	}

	/**
	 * Create the settings model the the ID column
	 */
	public static SettingsModelColumnName createIDColumnSettingsModel() {
		return new SettingsModelColumnName(MMPConstants.ID_COLUMN, null);
	}

	/*
	 * Settings Models for the 'Fragmentation Filtering Settings' tab
	 */
	/** Create settings model for limiting fragmentation by complexity */
	public static SettingsModelBoolean createLimitByComplexityModel() {
		return new SettingsModelBoolean(MMPConstants.LIMIT_BY_COMPLEXITY,
				MMPConstants.DEFAULT_LIMIT_BY_COMPLEXITY);
	}

	/** Create settings model for the upper limit of fragmentations */
	public static SettingsModelIntegerBounded createMaxFragmentationsModel() {
		return new SettingsModelIntegerBounded(MMPConstants.MAX_FRAGMENTATIONS,
				MMPConstants.DEFAULT_MAX_FRAGMENTATIONS, MMPConstants.MINIMUM_MAX_FRAGMENTATIONS,
				MMPConstants.MAXIMUM_MAX_FRAGMENTATIONS);
	}

	/**
	 * Create Settings Model for the option to deal with generated chirality for
	 * molecules with no centres
	 */
	public static SettingsModelBoolean createProchiralModel() {
		return new SettingsModelBoolean(MMPConstants.TREAT_PROCHIRAL_AS_CHIRAL,
				MMPConstants.DEFAULT_TREAT_PROCHIRAL_AS_CHIRAL);
	}

	/**
	 * Create the settings model for the has changing atoms filter
	 */
	public static SettingsModelBoolean createHasMaxChangingAtomsModel() {
		return new SettingsModelBoolean(MMPConstants.HAS_MAX_CHANGING_ATOMS_FILTER,
				MMPConstants.DEFAULT_HAS_MAX_CHANGING_HA);
	}

	/**
	 * Create Settings model for the maximum number of changing atoms
	 */
	public static SettingsModelIntegerBounded createMaxChangingAtomsModel() {
		return new SettingsModelIntegerBounded(MMPConstants.MAX_CHANGING_HEAVY_ATOMS,
				MMPConstants.DEFAULT_MAX_CHANGING_HA, 0, MMPConstants.MAXIMUM_MAX_FRAGMENTATIONS);
	}

	/**
	 * Create the settings model for the has atom ratio filter
	 */
	public static SettingsModelBoolean createHasHARatioFilterModel() {
		return new SettingsModelBoolean(MMPConstants.HAS_MAX_CHANGING_ATOMS_RATIO_FILTER,
				MMPConstants.DEFAULT_HAS_CHANGING_HA_RATIO);
	}

	/**
	 * Create Settings model for the maximum ration of unchanging to changing
	 * atoms
	 */
	public static SettingsModelDoubleBounded createHARatioModel() {
		return new SettingsModelDoubleBounded(
				MMPConstants.MAX_RATIO_OF_UNCHANGING_TO_CHANGING_HEAVY_ATOMS,
				MMPConstants.DEFAULT_MAX_HA_RATIO, 0.0001, Double.MAX_VALUE);
	}

	/*
	 * Output Settings Tab Settings Models
	 */
	/** Create the SM for outputting the changing HA count */
	public static SettingsModelBoolean createOutputChangingHACountsModel() {
		return new SettingsModelBoolean(MMPConstants.OUTPUT_CHANGING_HA_COUNTS,
				MMPConstants.DEFAULT_OUTPUT_DELTA_HAC);
	}

	/** Create the SM for outputting the HAC ratios */
	public static SettingsModelBoolean createOutputHARatiosModel() {
		return new SettingsModelBoolean(MMPConstants.OUTPUT_CHANGING_UNCHANGING_HA_RATIOS,
				MMPConstants.DEFAULT_OUTPUT_CHANGING_UNCHANGING_HAC_RATIOS);
	}

	/** Create settings model for extra failure reasons column */
	public static SettingsModelBoolean createAddFailReasonModel() {
		return new SettingsModelBoolean(MMPConstants.ADD_FAILURE_REASONS,
				MMPConstants.DEFAULT_ADD_FAIL_REASONS);
	}

	public static SettingsModelColumnFilter2 createKeptColumnsModel() {
		return new SettingsModelColumnFilter2(MMPConstants.KEPT_COLUMNS);
	}

	/*
	 * Fingerprints Tabs Settings Models
	 */
	/** Create Settings Model for value graph distance counts fingerprints */
	public static SettingsModelBoolean createAddValueGraphDistanceFPModel() {
		return new SettingsModelBoolean(MMPConstants.VALUE_GRAPH_DISTANCE_FP,
				MMPConstants.DEFAULT_VALUE_GRAPH_DIST_FP);
	}

	/** Create Settings Model for use of Attachment Point Fingerprints */
	public static SettingsModelBoolean createApFingerprintsModel() {
		return new SettingsModelBoolean(MMPConstants.ATTACHMENT_POINT_FINGERPRINTS,
				MMPConstants.DEFAULT_ADD_AP_FINGERPRINTS);
	}

	/** Create Settings Model for Morgan FP Radius */
	public static SettingsModelIntegerBounded createMorganRadiusModel() {
		return new SettingsModelIntegerBounded(MMPConstants.FP_MORGAN_RADIUS,
				(int) MMPConstants.DEFAULT_FP_RADIUS, MMPConstants.MINIMUM_MORGAN_RADIUS,
				MMPConstants.MAXIMUM_MAX_FRAGMENTATIONS);
	}

	/** Create Settings Model for Morgan FP Length */
	public static SettingsModelIntegerBounded createFpLengthModel() {
		return new SettingsModelIntegerBounded(MMPConstants.FP_LENGTH,
				(int) MMPConstants.DEFAULT_FP_LENGTH, MMPConstants.MINIMUM_FINGERPRINT_LENGTH,
				MMPConstants.MAXIMUM_MAX_FRAGMENTATIONS);
	}

	/** Create Settings Model for use of chirality in FP generation */
	public static SettingsModelBoolean createFpUseChiralityModel() {
		return new SettingsModelBoolean(MMPConstants.FP_USE_CHIRALITY,
				MMPConstants.DEFAULT_USE_CHIRALITY);
	}

	/** Create Settings Model for use of bond types in FP generation */
	public static SettingsModelBoolean createFpUseBondTypesModel() {
		return new SettingsModelBoolean(MMPConstants.FP_USE_BOND_TYPES,
				MMPConstants.DEFAULT_USE_BOND_TYPES);
	}
}
