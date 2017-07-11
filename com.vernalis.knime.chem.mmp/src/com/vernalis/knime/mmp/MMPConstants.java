/*******************************************************************************
 * Copyright (c) 2015, 2017, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License, Version 3, as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.mmp;

import java.awt.Color;

import org.RDKit.Bond.BondStereo;

/**
 * A variety of constants and default settings values for the MMP plugin
 * 
 * @author s.roughley knime@vernalis.com
 * 
 */
public class MMPConstants {
	/** Default add attachment point fingerprints */
	public static final boolean DEFAULT_ADD_AP_FINGERPRINTS = true;

	/** Default add reasons to failed rows table */
	public static final boolean DEFAULT_ADD_FAIL_REASONS = true;

	/** Default add hydrogens for 1 cut */
	public static final boolean DEFAULT_ADD_H = true;

	/** Default Allow one bond to be cut twice for 2-cuts */
	public static final boolean DEFAULT_ALLOW_2_CUTS_TO_SINGLE_BOND = true;

	/** Default allow self-transforms */
	public static final boolean DEFAULT_ALLOW_SELF_TRANSFORMS = false;

	/** Default check keys are sortedn */
	public static final boolean DEFAULT_CHECK_SORTED_KEYS = true;

	/** The default fingerprint length */
	public static final long DEFAULT_FP_LENGTH = 2048;

	/** The Default fingerprint radius */
	public static final long DEFAULT_FP_RADIUS = 4;

	/** Default Has changing HAC ratio filter */
	public static final boolean DEFAULT_HAS_CHANGING_HA_RATIO = true;

	/** Default has changing HAC filter */
	public static final boolean DEFAULT_HAS_MAX_CHANGING_HA = true;

	/** Default value for assuming incoming keys are sorted */
	public static final boolean DEFAULT_HAS_SORTED_KEYS = false;

	/** Default ignore IDs in uniqueness check */
	public static final boolean DEFAULT_IGNORE_IDS_FOR_UNIQUENESS = true;

	/** Default maximum changing HAC value */
	public static final int DEFAULT_MAX_CHANGING_HA = 12;

	/** Default changing HAC ratio value */
	public static final double DEFAULT_MAX_HA_RATIO = 1.0;

	/** The default number of cuts */
	public static final int DEFAULT_NUM_CUTS = 1;

	/** Default output HAC changes */
	public static final boolean DEFAULT_OUTPUT_CHANGING_HA_COUNTS = false;

	/** Default output HAC Ratios */
	public static final boolean DEFAULT_OUTPUT_CHANGING_UNCHANGING_HAC_RATIOS = false;

	/** Default output HAC Differences */
	public static final boolean DEFAULT_OUTPUT_DELTA_HAC = false;

	/** Default output the fragment key (i.e. the unchanging portion) */
	public static final boolean DEFAULT_OUTPUT_FRAGMENT_KEY = false;

	/** Default outpu HAC change ratios */
	public static final boolean DEFAULT_OUTPUT_HAC_RATIOS = false;

	/** Default include Reaction SMARTS in output */
	public static final boolean DEFAULT_OUTPUT_REACTION_SMARTS = true;

	/** Default output reverse transforms */
	public static final boolean DEFAULT_OUTPUT_REVERSE_TRANSFORMS = true;

	/** Default remove Hydrogens from transform */
	public static final boolean DEFAULT_REMOVE_H_FROM_TRANSFORM = true;

	/** Default remove H after fragmentation */
	public static final boolean DEFAULT_REMOVE_H_POST_FRAGMENTATION = true;

	/** Default remove non-viable bonds from mark bonds */
	public static final boolean DEFAULT_REMOVE_NONVIABLE_BONDS = true;

	/**
	 * Default for tracking cut connectivity
	 * 
	 * @deprecated No longer used - we always track!
	 */
	@Deprecated
	public static boolean DEFAULT_TRACK_CONNECTIVITY = true;

	/** The default 'Use Bond Types' flag */
	public static boolean DEFAULT_USE_BOND_TYPES = true;

	/** The default 'Use Chirality' flag */
	public static boolean DEFAULT_USE_CHIRALITY = false;

	public static BondStereo[] DOUBLE_BOND_STEREO_OPTIONS =
			new BondStereo[] { BondStereo.STEREOE, BondStereo.STEREOZ };

	/** Maximum number of cuts */
	public static int MAXIMUM_NUMBER_OF_CUTS = 10;

	/** Minimum Fingerprint length */
	public static final int MINIMUM_FINGERPRINT_LENGTH = 32;

	/** The minimum Morgan Radius */
	public static final int MINIMUM_MORGAN_RADIUS = 0;

	/** Minimum number of cuts */
	public static final int MINIMUM_NUMBER_OF_CUTS = 1;

	/**
	 * Key for the Fragmentation SMIRKS option
	 */
	public static final String FRAGMENTATION_SMIRKS = "Fragmentation SMIRKS";

	/**
	 * Key for the Remove Explicit H's option
	 */
	public static final String REMOVE_EXPLICIT_H_S_FROM_OUTPUT = "Remove Explicit H's from output";

	/**
	 * Key for the Add H's option
	 */
	public static final String ADD_HS = "Add Hs";

	/**
	 * Key for the number of cuts option
	 */
	public static final String NUMBER_OF_CUTS = "Number of cuts";

	/**
	 * Key for the Molecule Column option
	 */
	public static final String MOLECULE_COLUMN = "Molecule Column";

	/**
	 * Key for the custom rSMARTS option
	 */
	public static final String CUSTOM_R_SMARTS = "Custom rSMARTS";

	/**
	 * Key for the Two cuts to bond option
	 */
	public static final String TWO_CUTS_TO_BOND_VALUE = "Two Cuts to Bond Value";

	/**
	 * Key for the Incoming explicit H's handling option
	 */
	public static final String INCOMING_EXPLICIT_H_S_OPTION = "Incoming Explicit H's Option";

	/**
	 * The key for the FP use bond types setting
	 */
	public static final String FP_USE_BOND_TYPES = "FP Use Bond Types";

	/**
	 * The key for the FP use chirality types setting
	 */
	public static final String FP_USE_CHIRALITY = "FP Use Chirality";

	/**
	 * The key for the FP length setting
	 */
	public static final String FP_LENGTH = "FP Length";

	/**
	 * The key for the FP radius setting
	 */
	public static final String FP_MORGAN_RADIUS = "FP Morgan Radius";

	/**
	 * The key for the AP FP option
	 */
	public static final String ATTACHMENT_POINT_FINGERPRINTS = "Attachment Point Fingerprints";

	/**
	 * The default value for the graph distance FP setting
	 */
	public static final boolean DEFAULT_VALUE_GRAPH_DIST_FP = true;

	/**
	 * The key for the value graph distance FP setting
	 */
	public static final String VALUE_GRAPH_DISTANCE_FP = "Value Graph Distance FP";

	/**
	 * The key for the kept columns setting
	 */
	public static final String KEPT_COLUMNS = "Kept Columns";

	/**
	 * The key for the add failure reasons setting
	 */
	public static final String ADD_FAILURE_REASONS = "Add failure reasons";

	/**
	 * The key for the Output changing / unchanging HA ratios setting
	 */
	public static final String OUTPUT_CHANGING_UNCHANGING_HA_RATIOS =
			"Output changing / unchanging HA ratios";

	/**
	 * The key for the Output changing HA counts setting
	 */
	public static final String OUTPUT_CHANGING_HA_COUNTS = "Output changing HA counts";

	/**
	 * The key for the Max ratio of unchanging to changing heavy atoms setting
	 */
	public static final String MAX_RATIO_OF_UNCHANGING_TO_CHANGING_HEAVY_ATOMS =
			"Max ratio of unchanging to changing heavy atoms";

	/**
	 * The key for the
	 */
	public static final String HAS_MAX_CHANGING_ATOMS_RATIO_FILTER =
			"Has Max Changing Atoms Ratio Filter";

	/**
	 * The key for the Has Max Changing Atoms Ratio Filter setting
	 */
	public static final String MAX_CHANGING_HEAVY_ATOMS = "Max Changing Heavy Atoms";

	/**
	 * The key for the Has Max Changing Atoms Filter setting
	 */
	public static final String HAS_MAX_CHANGING_ATOMS_FILTER = "Has Max Changing Atoms Filter";

	/**
	 * The default treat-prochiral as chiral setting
	 */
	public static final boolean DEFAULT_TREAT_PROCHIRAL_AS_CHIRAL = true;

	/**
	 * The key for the Treat Prochiral as Chiral setting
	 */
	public static final String TREAT_PROCHIRAL_AS_CHIRAL = "Treat Prochiral as Chiral";

	/**
	 * The default limit by complecity setting
	 */
	public static final boolean DEFAULT_LIMIT_BY_COMPLEXITY = false;

	/**
	 * The maximum allowed value for the maximum fragmentations filter
	 */
	public static final int MAXIMUM_MAX_FRAGMENTATIONS = Integer.MAX_VALUE;

	/**
	 * The minimum allowed value for the maximum fragmentations filter
	 */
	public static final int MINIMUM_MAX_FRAGMENTATIONS = 1;

	/**
	 * The default Max Fragmentations setting
	 */
	public static final int DEFAULT_MAX_FRAGMENTATIONS = 5000;

	/**
	 * The key for the Max fragmentations setting
	 */
	public static final String MAX_FRAGMENTATIONS = "Max Fragmentations";

	/**
	 * The key for the limit by complecit setting
	 */
	public static final String LIMIT_BY_COMPLEXITY = "Limit by Complexity";

	/**
	 * The key for the ID column
	 */
	public static final String ID_COLUMN = "ID Column";

	/**
	 * The default allow hilite setting
	 */
	public static final boolean DEFAULT_ALLOW_HILITE = false;

	/**
	 * The key for the Allow hilite setting
	 */
	public static final String ALLOW_HI_LITING = "Allow HiLiting";

	/**
	 * The default render fragmentation setting
	 */
	public static final boolean DEFAULT_RENDER_FRAGMENTATION = false;

	/**
	 * The key for the render fragmentation setting
	 */
	public static final String RENDER_FRAGMENTATION = "Render fragmentation";

	/**
	 * The default for the render breaking bonds setting
	 */
	public static final boolean DEFAULT_RENDER_BREAKING_BONDS = true;

	/**
	 * The key for the show breaking bonds setting
	 */
	public static final String SHOW_BREAKING_BONDS = "Show breaking bonds";

	/**
	 * The default colour for breaking bonds
	 */
	public static final Color DEFAULT_BREAKING_BONDS_COLOUR = Color.LIGHT_GRAY;

	/**
	 * The key for the breaking bonds colour
	 */
	public static final String BREAKING_BONDS_COLOR = "Breaking bonds color";

	/**
	 * The default for the render key setting
	 */
	public static final boolean DEFAULT_RENDER_KEY = true;

	/**
	 * The key for the show key setting
	 */
	public static final String SHOW_KEY = "Show Key";

	/**
	 * The default key colour
	 */
	public static final Color DEFAULT_KEY_COLOUR = Color.MAGENTA;

	/**
	 * The default for the render value setting
	 */
	public static final boolean DEFAULT_RENDER_VALUE = true;

	/**
	 * The default value colour
	 */
	public static final Color DEFAULT_VALUE_COLOUR = Color.ORANGE;

	/**
	 * The key for the key colour setting
	 */
	public static final String KEY_COLOUR = "Key Colour";

	/**
	 * The key for the show value setting
	 */
	public static final String SHOW_VALUE = "Show Value";

	/**
	 * The key for the value colour setting
	 */
	public static final String VALUE_COLOUR = "Value Colour";

	/**
	 * The default bond hilite colour
	 */
	public static final Color DEFAULT_BOND_COLOUR = Color.RED;

	/**
	 * The bond colour setting key
	 */
	public static final String BOND_COLOUR = "Bond Colour";

	/**
	 * Default value for Tversky alpha value
	 */
	public static final double DEFAULT_TVERSKY_ALPHA = 0.5;

	/**
	 * Key for Tversky alpha setting
	 */
	public static final String ALPHA = "Alpha";

	/**
	 * The maximum for Tversky alpha/beta values
	 */
	public static final double MAX_TVERSKY_PARAM = 1.0;

	/**
	 * The minimum for Tversky alpha/beta values
	 */
	public static final double MIN_TVERSKY_PARAM = 0.0;

	/**
	 * Default value for Tversky beta value
	 */
	public static final double DEFAULT_TVERSKY_BETA = 0.5;

	/**
	 * Key for Tversky beta setting
	 */
	public static final String BETA = "Beta";

	/**
	 * Key for similarity type
	 */
	public static final String SIMILARITY_TYPE = "Similarity Type";

	/**
	 * Key for comparison type
	 */
	public static final String COMPARISON_TYPE = "Comparison Type";

	/**
	 * Minimum for similarity threshold
	 */
	public static final double MAXIMUM_DOUBLE_SIMILARITY_THRESHOLD = 1.0;

	/**
	 * Maximum for similarity threshold
	 */
	public static final double MINIMUM_DOUBLE_SIMILARITY_THRESHOLD = 0.05;

	/**
	 * Default for similarity threshold
	 */
	public static final double DEFAULT_DOUBLE_SIMILARITY_THRESHOLD = 0.75;

	/**
	 * Key for similarity threshold setting
	 */
	public static final String SIMILARITY_THRESHOLD = "Similarity Threshold";

	/**
	 * Key for first AP fingerprint column name setting
	 */
	public static final String FIRST_AP_FINGERPRINT_COLUMN = "First AP Fingerprint Column";

	/**
	 * Key for rSMARTS column setting
	 */
	public static final String R_SMARTS_COLUMN = "rSMARTS Column";

	/**
	 * Key for molecule table pass through columns
	 */
	public static final String MOLECULE_TABLE_PASS_THROUGH_COLUMNS =
			"Molecule table Pass-through columns";

	/**
	 * Default for filter by AP environment setting
	 */
	public static final boolean DEFAULT_FILTER_BY_ENVIRONMENT_SETTING = false;

	/**
	 * Key for filter by transform environment setting
	 */
	public static final String FILTER_BY_TRANSFROM_ENVIRONMENT = "Filter by Transfrom Environment";

	/**
	 * Key for transform pass through columns
	 */
	public static final String TRANSFORM_TABLE_PASS_THROUGH_COLUMNS =
			"Transform table Pass-through columns";

	/**
	 * Default for are transforms sorted
	 */
	public static final boolean DEFAULT_TRANSFORMS_SORTED = false;

	/**
	 * Key for are transforms sorted
	 */
	public static final String TRANSFORMS_SORTED = "Transforms sorted";

	/**
	 * Default for create chiral products setting
	 */
	public static final boolean DEFAULT_CREATE_CHIRAL_PRODUCTS = true;

	/**
	 * Key for create chiral products setting
	 */
	public static final String CREATE_CHIRAL_PRODUCTS = "Create chiral products";

	private MMPConstants() {
		// Dont instantiate!
	}
}
