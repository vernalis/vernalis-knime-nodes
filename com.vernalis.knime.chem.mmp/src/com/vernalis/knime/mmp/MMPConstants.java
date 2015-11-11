/*******************************************************************************
 * Copyright (c) 2015, Vernalis (R&D) Ltd
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
/**
 * 
 */
package com.vernalis.knime.mmp;

import org.RDKit.Bond.BondStereo;


/**
 * A variety of constants and default settings values for the MMP plugin
 * 
 * @author s.roughley <knime@vernalis.com>
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

	public static BondStereo[] DOUBLE_BOND_STEREO_OPTIONS = new BondStereo[] {
			BondStereo.STEREOE, BondStereo.STEREOZ };

	/** Maximum number of cuts */
	public static int MAXIMUM_NUMBER_OF_CUTS = 10;

	/** Minimum Fingerprint length */
	public static final int MINIMUM_FINGERPRINT_LENGTH = 32;

	/** The minimum Morgan Radius */
	public static final int MINIMUM_MORGAN_RADIUS = 0;

	/** Minimum number of cuts */
	public static final int MINIMUM_NUMBER_OF_CUTS = 1;

	private MMPConstants() {
	}
}
