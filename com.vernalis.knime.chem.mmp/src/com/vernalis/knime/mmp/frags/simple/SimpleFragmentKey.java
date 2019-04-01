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

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.knime.core.data.DataCell;
import org.knime.core.data.vector.bitvector.DenseBitVector;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;

import com.vernalis.knime.mmp.frags.abstrct.AbstractFragmentKey;

/**
 * A 'Dumb' {@link AbstractFragmentKey} implementation which simply stores the
 * SMILES String for use in pair generation. This implementation also adds
 * methods for lazy instantiation and storage (but not <i>de novo</i>
 * generation) of fingerprints and concatenated fingerprints
 * 
 * @author s.roughley
 *
 */
public class SimpleFragmentKey extends AbstractFragmentKey<String> {
	private DenseBitVector[] fps = null;
	private DenseBitVector concatenatedFp = null;
	private DataCell[] fpCells = null;

	/**
	 * {@inheritDoc}
	 */
	public SimpleFragmentKey() {

	}

	/**
	 * {@inheritDoc}
	 */
	public SimpleFragmentKey(SimpleFragmentKey existingKey) {
		super(existingKey);
	}

	/**
	 * {@inheritDoc}
	 */
	public SimpleFragmentKey(String keyAsString) {
		super(keyAsString);
	}

	/** {@inheritDoc} */
	@Override
	protected SimpleFragmentLeaf getLeafFromString(String keyAsString) {
		return new SimpleFragmentLeaf(keyAsString);
	}

	/**
	 * Method to store the AP fingerprint cells
	 * 
	 * @param fpCells
	 *            The cells to store
	 * @throws IllegalArgumentException
	 *             If the wrong number of cells are supplied
	 * @throws UnsupportedOperationException
	 *             If cells have already been stored
	 */
	public void setLeafFingerprints(DataCell[] fpCells)
			throws IllegalArgumentException, UnsupportedOperationException {
		if (fpCells.length != getNumComponents()) {
			throw new IllegalArgumentException("Incorrect number of fingerprint cells ("
					+ fpCells.length + " supplied, " + getNumComponents() + " required)");
		}
		if (this.fpCells != null) {
			throw new UnsupportedOperationException(
					"The fingerprints have already been set and may not be changed");
		}
		// Pass down to the individual Leafs in case those are accessed
		for (int i = 0; i < fpCells.length; i++) {
			((SimpleFragmentLeaf) getLeafWithIdx(i + 1)).setFingerprintCell(fpCells[i]);
		}
		// Store here too for brevity
		this.fpCells = fpCells;
	}

	/**
	 * Method to retrieve the fingerprint cells
	 * 
	 * @return The stored cells
	 * @throws NoSuchElementException
	 *             if the cells have not previously been stored by a call to
	 *             {@link #setLeafFingerprints(DataCell[])}
	 */
	public DataCell[] getLeafFingerprintCells() throws NoSuchElementException {
		if (fpCells == null) {
			throw new NoSuchElementException("The Fingerprints have not been initialised");
		}
		return fpCells;
	}

	/**
	 * Method to retrieve the actual fingerprints
	 * 
	 * @return the fingerprints
	 * @throws NoSuchElementException
	 *             if the cells have not previously been stored by a call to
	 *             {@link #setLeafFingerprints(DataCell[])}
	 */
	public DenseBitVector[] getLeafFingerprints() throws NoSuchElementException {
		if (fps == null) {
			fps = Arrays.stream(getLeafFingerprintCells())
					.map(x -> ((DenseBitVectorCell) x).getBitVectorCopy())
					.toArray(x -> new DenseBitVector[x]);
		}
		return fps;
	}

	/**
	 * Method to calculate and return the concatenated fingerprint
	 * 
	 * @return the concatenated fingerprint
	 * @throws NoSuchElementException
	 *             if the cells have not previously been stored by a call to
	 *             {@link #setLeafFingerprints(DataCell[])}
	 */
	public DenseBitVector getConcatenatedFingerprints() throws NoSuchElementException {
		if (concatenatedFp == null) {
			concatenatedFp = getLeafFingerprints()[0];
			for (int i = 1; i < fps.length; i++) {
				concatenatedFp = concatenatedFp.concatenate(fps[i]);
			}
		}
		return concatenatedFp;
	}
}
