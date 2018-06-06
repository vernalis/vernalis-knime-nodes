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

import org.knime.core.data.DataCell;

import com.vernalis.knime.mmp.frags.abstrct.AbstractLeaf;

/**
 * A 'dumb' {@link AbstractLeaf} implementation which does no canonicalisation
 * but simply stores the SMILES String and the fingerprint
 * 
 * @author s.roughley
 *
 */
public class SimpleFragmentLeaf extends AbstractLeaf<String> {

	private DataCell fpCell = null;

	public SimpleFragmentLeaf(String smiles) throws IllegalArgumentException {
		super(smiles);

	}

	/**
	 * Method to store the fingerprint cell
	 * 
	 * @param fpCell
	 *            The fingerprint cell
	 */
	public void setFingerprintCell(DataCell fpCell) throws UnsupportedOperationException {
		if (this.fpCell != null) {
			throw new UnsupportedOperationException("The fingerprint has already been set");
		}
		this.fpCell = fpCell;
	}

	/** {@inheritDoc} */
	@Override
	public DataCell getMorganFingerprintCell(int radius, int numBits, boolean useChirality,
			boolean useBondTypes) {
		return fpCell;
	}

	/** {@inheritDoc} */
	@Override
	protected String toolkitCanonicalize(String smiles) {
		return smiles;
	}

}
