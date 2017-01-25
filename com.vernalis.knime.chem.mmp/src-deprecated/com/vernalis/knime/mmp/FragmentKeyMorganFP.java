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
package com.vernalis.knime.mmp;

import java.util.HashMap;

import org.RDKit.ExplicitBitVect;
import org.RDKit.RDKFuncs;
import org.RDKit.RWMol;
import org.RDKit.UInt_Vect;

/**
 * This class extends the {@link FragmentKey} class, adding a Morgan fingerprint
 * rooted at each attachment point. It will still appear to function if
 * attachment points were not tracked during fragmentation, but the results will
 * not be meaningful.
 * 
 * @author Steve Roughley knime@vernalis.com
 * 
 */
public class FragmentKeyMorganFP extends DefaultFragmentKeyFingerprinted {
	
	private long m_radius;
	private long m_numBits;
	private boolean m_useChirality;
	private boolean m_useBondTypes;

	/** The Default fingerprint radius */
	public static long DEFAULT_FP_RADIUS = 4;
	/** The default fingerprint length */
	public static long DEFAULT_FP_LENGTH = 2048;
	/** The default 'Use Chirality' flag */
	public static boolean DEFAULT_USE_CHIRALITY = false;
	/** The default 'Use Bond Types' flag */
	public static boolean DEFAULT_USE_BOND_TYPES = true;

	/**
	 * Default constructor. Default properties are applied for fingerprint
	 * generation
	 */
	public FragmentKeyMorganFP() {
		super();
		m_apEnvironments = new HashMap<>();
		m_radius = DEFAULT_FP_RADIUS;
		m_numBits = DEFAULT_FP_LENGTH;
		m_useChirality = DEFAULT_USE_CHIRALITY;
		m_useBondTypes = DEFAULT_USE_BOND_TYPES;
	}

	/**
	 * Constructor with no fragments and user-supplied fingerprint properties
	 * 
	 * @param radius
	 *            The fingerprint radius
	 * @param numBits
	 *            The fingerprint length
	 * @param useChirality
	 *            Use chirality flag
	 * @param useBondTypes
	 *            Use Bond Types flag
	 */
	public FragmentKeyMorganFP(long radius, long numBits,
			boolean useChirality, boolean useBondTypes) {
		super();
		m_radius = radius;
		m_numBits = numBits;
		m_useChirality = useChirality;
		m_useBondTypes = useBondTypes;
	}

	/**
	 * Generate a new {@link FragmentKeyMorganFP} from an existing
	 * {@link FragmentKey}.
	 * 
	 * @param existingKey
	 *            The exisiting key. If this is not fingeprinted, then the
	 *            default fingerprint options will be applied, otherwise those
	 *            from the argument will be inherited.
	 */
	public FragmentKeyMorganFP(FragmentKey existingKey) {
		super(existingKey);
		if (existingKey instanceof FragmentKeyMorganFP) {
			m_apEnvironments = ((FragmentKeyMorganFP) existingKey).m_apEnvironments;
			m_radius = ((FragmentKeyMorganFP) existingKey).m_radius;
			m_numBits = ((FragmentKeyMorganFP) existingKey).m_numBits;
			m_useChirality = ((FragmentKeyMorganFP) existingKey).m_useChirality;
			m_useBondTypes = ((FragmentKeyMorganFP) existingKey).m_useBondTypes;
		} else {
			m_apEnvironments = new HashMap<>();
			m_radius = DEFAULT_FP_RADIUS;
			m_numBits = DEFAULT_FP_LENGTH;
			m_useChirality = DEFAULT_USE_CHIRALITY;
			m_useBondTypes = DEFAULT_USE_BOND_TYPES;
			calculateAttPointEnvironments(m_keyComponents);
		}
	}

	/**
	 * * Generate a new {@link FragmentKeyMorganFP} from an existing
	 * {@link FragmentKey}, supplying new fingerprint settings.
	 * 
	 * @param existingKey
	 *            The parent {@link FragmentKey}. If this is fingerprinted, then
	 *            new fingerprints will be generated if the specified settings
	 *            are different from the parent
	 * @param radius
	 *            The fingerprint radius
	 * @param numBits
	 *            The fingerprint length
	 * @param useChirality
	 *            Use chirality flag
	 * @param useBondTypes
	 *            Use Bond Types flag
	 */
	public FragmentKeyMorganFP(FragmentKey existingKey, long radius,
			long numBits, boolean useChirality, boolean useBondTypes) {
		super(existingKey);
		if (existingKey instanceof FragmentKeyMorganFP) {
			if (radius != ((FragmentKeyMorganFP) existingKey).m_radius
					|| numBits != ((FragmentKeyMorganFP) existingKey).m_numBits
					|| useChirality != ((FragmentKeyMorganFP) existingKey).m_useChirality
					|| useBondTypes != ((FragmentKeyMorganFP) existingKey).m_useBondTypes) {
				// We use the supplied parameters and re-calculate
				m_radius = radius;
				m_numBits = numBits;
				m_useChirality = useChirality;
				m_useBondTypes = useBondTypes;
				m_apEnvironments = new HashMap<>();
				calculateAttPointEnvironments(m_keyComponents);
			} else {
				// Parameters are the same to transfer everything
				m_apEnvironments = ((FragmentKeyMorganFP) existingKey).m_apEnvironments;
				m_radius = ((FragmentKeyMorganFP) existingKey).m_radius;
				m_numBits = ((FragmentKeyMorganFP) existingKey).m_numBits;
				m_useChirality = ((FragmentKeyMorganFP) existingKey).m_useChirality;
				m_useBondTypes = ((FragmentKeyMorganFP) existingKey).m_useBondTypes;
			}
		} else {
			// Create them from new
			m_radius = radius;
			m_numBits = numBits;
			m_useChirality = useChirality;
			m_useBondTypes = useBondTypes;
			m_apEnvironments = new HashMap<>();
			calculateAttPointEnvironments(m_keyComponents);
		}
	}

	/**
	 * <p>
	 * Generated a {@link FragmentKeyMorganFP} from a (possibly
	 * multi-component) SMILES string, using default properties for fingerprint
	 * generation
	 * </p>
	 * 
	 * @param keyAsString
	 *            The SMILES string
	 */
	public FragmentKeyMorganFP(String keyAsString) {
		super(keyAsString);
		m_apEnvironments = new HashMap<>();
		m_apEnvironments = new HashMap<>();
		m_radius = DEFAULT_FP_RADIUS;
		m_numBits = DEFAULT_FP_LENGTH;
		m_useChirality = DEFAULT_USE_CHIRALITY;
		m_useBondTypes = DEFAULT_USE_BOND_TYPES;
		calculateAttPointEnvironments(m_keyComponents);
	}

	/**
	 * Generated a {@link FragmentKeyMorganFP} from a (possibly
	 * multi-component) SMILES string, using non-default properties for
	 * fingerprint generation
	 * 
	 * @param keyAsString
	 *            The SMILES string
	 * @param radius
	 *            The fingerprint radius
	 * @param numBits
	 *            The fingerprint length
	 * @param useChirality
	 *            Use chirality flag
	 * @param useBondTypes
	 *            Use Bond Types flag
	 */
	public FragmentKeyMorganFP(String keyAsString, long radius,
			long numBits, boolean useChirality, boolean useBondTypes) {
		super(keyAsString);
		m_radius = radius;
		m_numBits = numBits;
		m_useChirality = useChirality;
		m_useBondTypes = useBondTypes;
	}

	/* (non-Javadoc)
	 * @see com.vernalis.knime.internal.mmp.DefaultFragmentKeyFingerprinted#getAttachmentPointFingerprint(org.RDKit.RWMol, org.RDKit.UInt_Vect)
	 */
	@Override
	protected ExplicitBitVect getAttachmentPointFingerprint(RWMol mol,
			UInt_Vect apIdx) {
		return RDKFuncs.getMorganFingerprintAsBitVect(mol, m_radius, m_numBits,
				null, apIdx, m_useChirality, m_useBondTypes);
	}
}
