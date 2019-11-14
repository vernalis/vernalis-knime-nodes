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
package com.vernalis.knime.mmp.frags.simple;

import org.knime.core.data.vector.bytevector.DenseByteVector;

import com.vernalis.knime.mmp.frags.abstrct.AbstractFragmentValue;

/**
 * A 'dumb' {@link AbstractFragmentValue} implementation which does no
 * canonicalisation, but simply stores the SMILES string and graph distance
 * fingerprint
 * 
 * @author s.roughley
 *
 */
public class SimpleFragmentValue extends AbstractFragmentValue<String> {

	/**
	 * Full constructor
	 * 
	 * @param smiles
	 *            The SMILES String
	 * @param ID
	 *            The ID
	 * @param ignoreIDsForComparisons
	 *            Is the ID ignored in comparisons?
	 */
	public SimpleFragmentValue(String smiles, String ID, boolean ignoreIDsForComparisons) {
		super(smiles, ID, ignoreIDsForComparisons);
	}

	/**
	 * Constructor from a SMILES string and ID, which is not ignored in
	 * comparisons
	 * 
	 * @param smiles
	 *            The SMILES string
	 * @param ID
	 *            The ID
	 */
	public SimpleFragmentValue(String smiles, String ID) {
		super(smiles, ID);
	}

	/**
	 * Constructor from a SMILES string, with no ID and IDs not ignored in
	 * comparisons
	 * 
	 * @param smiles
	 *            The SMILES String
	 */
	public SimpleFragmentValue(String smiles) {
		super(smiles);
	}

	@Override
	protected void toolkitCanonicalize() {
		// Do nothing!

	}

	/**
	 * Method to store a fingerprint
	 * 
	 * @param fp
	 *            The fingerprint to store
	 */
	public final void setGraphDistanceFingerprint(DenseByteVector fp) {
		this.graphDistFP = fp;
	}

	@Override
	protected DenseByteVector initialistGraphDistanceFingerprint(int numBytes) {
		return graphDistFP;
	}

}
