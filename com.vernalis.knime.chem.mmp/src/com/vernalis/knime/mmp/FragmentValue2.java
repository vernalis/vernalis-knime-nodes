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
package com.vernalis.knime.mmp;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.RDKit.ROMol;
import org.RDKit.RWMol;
import org.knime.chem.types.SmilesCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;

/**
 * Simple container class to hold an ID string and SMILES String together and
 * allow comparison and sorting for use in HashSet etc
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class FragmentValue2 implements Comparable<FragmentValue2> {
	private String SMILES;
	private final String ID;
	boolean ignoreIDsForComparisons = false;

	/**
	 * Constructor - a SMILES string must be supplied. The ID is null, and IDs
	 * are therefore ignored for comparisons({@link #equals(Object)},
	 * {@link #compareTo(FragmentValue2)})
	 * 
	 * @param smiles
	 *            The SMILES String
	 */
	public FragmentValue2(String smiles) {
		this(smiles, null, false);
	}

	/**
	 * Alternative constructor allowing the supplying of both SMILES String and
	 * ID. IDs are not ignored for comparisons({@link #equals(Object)},
	 * {@link #compareTo(FragmentValue2)})
	 * 
	 * @param smiles
	 *            The SMILES String
	 * @param ID
	 *            The ID
	 */
	public FragmentValue2(String smiles, String ID) {
		this(smiles, ID, false);
	}

	/**
	 * Alternative constructor allowing the supplying of both SMILES String and
	 * ID. IDs may be ignored for comparisons({@link #equals(Object)},
	 * {@link #compareTo(FragmentValue2)})
	 * 
	 * @param smiles
	 *            The SMILES String
	 * @param ID
	 *            The ID
	 * @param ignoreIDsForComparisons
	 *            {@code true} if IDs should be ignored when comparing objects
	 */
	public FragmentValue2(String smiles, String ID, boolean ignoreIDsForComparisons) {
		this.SMILES = smiles;
		this.ID = ID;
		this.ignoreIDsForComparisons = ignoreIDsForComparisons;
		// canonicalizeBondOnlyValue();
	}

	/**
	 * We always want *-* to canonicalize with the highest index first
	 */
	private void canonicalizeBondOnlyValue() {
		if (SMILES.matches("\\[\\d+\\*\\](-)?\\[\\d+\\*\\]")) {
			// Bond
			Pattern p = Pattern.compile("\\[([0-9]+)\\*\\]");
			Matcher m = p.matcher(SMILES);
			m.find();
			int firstIdx = Integer.parseInt(m.group(1));
			m.find();
			int secondIdx = Integer.parseInt(m.group(1));
			if (secondIdx > firstIdx) {
				SMILES = "[" + secondIdx + "*][" + firstIdx + "*]";
			}
		}
	}

	/**
	 * Get the SMILES
	 * 
	 * @param removeExplicitHs
	 *            Should explicit hydrogen atoms be removed from the SMILES
	 *            string?
	 * @param trackConnectivity
	 *            Should the attachment points be indexed - only used for
	 *            smilesparser canonicalisation
	 * @see #getSMILES(boolean)
	 * @return The SMILES string
	 */
	public String getSMILES(boolean removeExplicitHs, boolean trackConnectivity) {

		String smi = this.SMILES;
		if (removeExplicitHs) {
			smi = RDKitFragmentationUtils.removeHydrogens(smi);
			if (smi.matches("^\\[[0-9]*?\\*[H]?\\]$")) {
				smi = smi.replaceAll("^\\[([0-9]*?)\\*.*", "[$1*][H]");
			}
		}
		if (!trackConnectivity) {
			smi = smi.replaceAll("\\[[0-9]+\\*\\]", "[*]");
		}
		return smi;
	}

	/**
	 * Overloaded method to get the SMILES, with connectivity tracked
	 * 
	 * @param removeExplicitHs
	 *            Should explicit hydrogen atoms be removed from the SMILES
	 *            string?
	 * @return The SMILES String
	 */
	public String getSMILES(boolean removeExplicitHs) {
		return getSMILES(removeExplicitHs, true);
	}

	/**
	 * Canonicalisation of high-symmetry molecules via the
	 * {@link MulticomponentSmilesFragmentParser} requires an attachment point
	 * index naive canonical form of the SMILES
	 * 
	 * @return SMILES for canonicalisation of parser
	 */
	public String getCanonicalIndexNaiveSMILES() {
		String smi = getSMILES(false, false);
		RWMol rwMol = RWMol.MolFromSmiles(smi, 0, false);
		smi = rwMol.MolToSmiles(true);
		rwMol.delete();
		return smi;
	}

	/**
	 * Get the SMILES as a {@link DataCell}
	 * 
	 * @see #getSMILES(boolean, boolean)
	 * @deprecated The track cut connectivity option is deprecated
	 */
	@Deprecated
	public DataCell getSMILESCell(boolean removeExplicitHs, boolean trackConnectivity) {
		return SmilesCellFactory.create(getSMILES(removeExplicitHs, trackConnectivity));
	}

	/**
	 * Get the SMILES as a {@link DataCell}, with cut connectivity tracked
	 * 
	 * @see #getSMILES(boolean)
	 */
	public DataCell getSMILESCell(boolean removeExplicitHs) {
		return getSMILESCell(removeExplicitHs, true);
	}

	/**
	 * @return The ID of the molecule. May be {@code null}
	 */
	public String getID() {
		return ID;
	}

	/**
	 * @return Return the ID as a {@link DataCell} (may be
	 *         {@link DataType#getMissingCell()})
	 */
	public DataCell getIDCell() {
		StringCell retVal;
		if (ID == null) {
			retVal = (StringCell) DataType.getMissingCell();
		} else {
			retVal = new StringCell(ID);
		}
		return retVal;
	}

	/** Get a simple string representation(ID::SMILES) */
	@Override
	public String toString() {
		if (ID == null) {
			return SMILES;
		}
		if (ignoreIDsForComparisons) {
			return SMILES;
		}
		return ID + "::" + SMILES;
	}

	/**
	 * Get an RDKit {@link ROMol} object from the SMILES . It is the
	 * responsibility of the downstream caller to destroy the resulting
	 * {@link ROMol} object
	 */
	public ROMol getROMol() {
		// This method ensures explicit H present in SMILES are retained
		RWMol rwMol = RWMol.MolFromSmiles(SMILES, 0, false);
		// RDKFuncs.sanitizeMol(rwMol);
		return rwMol;
	}

	/**
	 * Get the number of changing atoms (i.e. the number of heavy atoms not '*')
	 * in the {@link FragmentValue2} {@link #SMILES}
	 */
	public int getNumberChangingAtoms() {
		return this.calcHAC();
	}

	/** Get the number of changing atoms as an {@link IntCell} */
	public DataCell getNumberChangingAtomsCell() {
		return new IntCell(getNumberChangingAtoms());
	}

	/**
	 * Private function to calculate the number of heavy atoms in this object-
	 * allows calculation for multiple components
	 * 
	 * @return
	 */
	private Integer calcHAC() {
		int cnt = 0;
		// NB define length as l as otherwise smiles.length
		// is re-calculated on each iteration, apparently
		for (int i = 0, l = SMILES.length(); i < l; i++) {
			char x = SMILES.charAt(i);
			if (x == '.')
				// skip - but keep counting!
				continue;
			if (x == '[') {
				cnt++;
				// skip to ]
				while (SMILES.charAt(i) != ']')
					i++;
				continue;
			}

			// Deal with aromatic atoms without []
			if (x == 'c' || x == 'o' || x == 'n' || x == 's') {
				cnt++;
				continue;
			}

			// Deal with other atoms
			if (Character.isUpperCase(x))
				cnt++;
		}
		// Now correct for [H]
		cnt -= (SMILES.indexOf("[H]") >= 0) ? SMILES.split("\\[H\\]").length - 1 : 0;
		// And correct for attachment points
		cnt -= countAttachmentPoints();
		return cnt;
	}

	/**
	 * Method to set the attachment point indices. a {@link FragmentKey2}
	 * argument is required in order to perform the correct mapping. The SMILES
	 * string is canonicalised afterwards by a call to {@link #canonicalize()}
	 * 
	 * @param key
	 *            The {@link FragmentKey2} argument for the mapping
	 */
	public void setAttachmentPointIndices(FragmentKey2 key) {
		HashMap<Integer, Integer> apLookup = key.getAttachmentPointIndexLookup();
		for (Entry<Integer, Integer> ent : apLookup.entrySet()) {
			SMILES = SMILES.replace("[" + ent.getValue() + "*]", "[" + ent.getKey() + "*]");
		}
		canonicalizeBondOnlyValue();
		// canonicalize();
	}

	/**
	 * Canonicalise the SMILES String. This round-trips through an RDKit
	 * {@link ROMol} object.
	 */
	public void canonicalize() {
		ROMol mol = RWMol.MolFromSmiles(SMILES, 0, false);
		SMILES = mol.MolToSmiles(true);
		mol.delete();
	}

	/**
	 * Private function to calculate the number of attachment points in the
	 * object
	 * 
	 * @return The number of attachment points
	 */
	public Integer countAttachmentPoints() {
		int result;
		try {
			result = SMILES.split("\\[[0-9]*?\\*").length;
		} catch (Exception e) {
			try {
				result = SMILES.split("\\*").length;
			} catch (Exception e1) {
				result = 1;
			}
		}
		// Result will hold one more than it should
		result--;
		return result;
	}

	/**
	 * @param other
	 *            Check if are the same SMILES string
	 * @return {@code true} if the two values have the same SMILES string
	 */
	public boolean isSameSMILES(FragmentValue2 other) {
		return this.SMILES.equals(other.SMILES);
	}

	@Override
	public int compareTo(FragmentValue2 that) {
		// Comparison is lexicographical of the string representations
		return this.toString().compareTo(that.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if (!ignoreIDsForComparisons) {
			result = prime * result + ((ID == null) ? 0 : ID.hashCode());
		}
		result = prime * result + ((SMILES == null) ? 0 : SMILES.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FragmentValue2 other = (FragmentValue2) obj;
		if (!ignoreIDsForComparisons) {
			if (ID == null) {
				if (other.ID != null) {
					return false;
				}
			} else if (!ID.equals(other.ID)) {
				return false;
			}
		}
		if (SMILES == null) {
			if (other.SMILES != null) {
				return false;
			}
		} else if (!SMILES.equals(other.SMILES)) {
			return false;
		}
		return true;
	}
}
