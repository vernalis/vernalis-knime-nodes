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

import org.RDKit.RDKFuncs;
import org.RDKit.ROMol;
import org.RDKit.RWMol;
import org.knime.chem.types.SmilesCell;
import org.knime.core.data.DataCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;

/**
 * Simple container class to hold an ID string and SMILES String together and
 * allow comparison and sorting for use in HashSet etc
 * 
 * @author "Stephen Roughley  knime@vernalis.com"
 * @deprecated Implementations should use the {@link RDKitFragmentValue} class
 */
@Deprecated
public class FragmentValue implements Comparable<FragmentValue> {
	private final String ID, SMILES;

	/**
	 * Constructor - an ID and SMILES string must be supplied
	 * 
	 * @param smiles
	 *            The SMILES String
	 * @param id
	 *            The ID String
	 */
	public FragmentValue(String smiles, String id) {
		this.ID = id;
		this.SMILES = smiles;
	}

	/** Get the ID */
	public String getID() {
		return this.ID;
	}

	/** Get the ID as a {@link DataCell} */
	public DataCell getIDCell() {
		return new StringCell(this.ID);
	}

	/** Get the SMILES */
	public String getSMILES(boolean removeExplicitHs) {
		if (removeExplicitHs) {

			String smi = this.SMILES;
			smi = smi.replace("[H]", "").replace("()", "");
			// if (smi.equals("[*H]") || smi.equals("[*]")) {
			// smi = "[*][H]";
			// }
			if (smi.matches("^\\[[0-9]*?\\*[H]?\\]$")) {
				smi = smi.replaceAll("^\\[([0-9]*?)\\*.*", "[$1*][H]");
			}
			return smi;
		} else {
			return this.SMILES;
		}
	}

	/** Get the SMILES as a {@link DataCell} */
	public DataCell getSMILESCell(boolean removeExplicitHs) {
		return new SmilesCell(getSMILES(removeExplicitHs));
	}

	/** Get a simple string representation(ID::SMILES) */
	@Override
	public String toString() {
		return ID + "::" + SMILES;
	}

	/** Get the object as a pair (ID then SMILES) of KNIME {@link DataCell}s */
	public DataCell[] toDataCells() {
		DataCell[] retVal = new DataCell[2];
		retVal[0] = new StringCell(ID);
		retVal[1] = new SmilesCell(SMILES);
		return retVal;
	}

	/** Get an RDKit {@link ROMol} object from the SMILES */
	public ROMol getROMol() {
		// This method ensures explicit H present in SMILES are retained
		RWMol rwMol = RWMol.MolFromSmiles(SMILES, 0, false);
		RDKFuncs.sanitizeMol(rwMol);
		return rwMol;
	}

	/**
	 * Get the number of changing atoms (i.e. the number of heavy atoms not '*')
	 * in the {@link FragmentValue} {@link #SMILES}
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
	 * @return The number of heavy atoms
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
		cnt -= (SMILES.indexOf("[H]") >= 0) ? SMILES.split("\\[H\\]").length - 1
				: 0;
		// And correct for attachment points
		cnt -= countAttachmentPoints();
		return cnt;
	}

	/**
	 * Private function to calculate the number of attachment points in the
	 * object
	 * 
	 * @return The number of attachment points
	 */
	private Integer countAttachmentPoints() {
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

	@Override
	public int compareTo(FragmentValue that) {
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
		result = prime * result + ((ID == null) ? 0 : ID.hashCode());
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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FragmentValue other = (FragmentValue) obj;
		if (ID == null) {
			if (other.ID != null)
				return false;
		} else if (!ID.equals(other.ID))
			return false;
		if (SMILES == null) {
			if (other.SMILES != null)
				return false;
		} else if (!SMILES.equals(other.SMILES))
			return false;
		return true;
	}

}
