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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.RDKit.ExplicitBitVect;
import org.RDKit.RDKFuncs;
import org.RDKit.ROMol;
import org.RDKit.RWMol;
import org.RDKit.UInt_Vect;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCellFactory;
import org.knime.core.data.vector.bitvector.SparseBitVectorCell;
import org.knime.core.data.vector.bitvector.SparseBitVectorCellFactory;

/**
 * This class provides the default implementation of the
 * {@link FragmentKeyFingerprinted} interface. Subclasses need to implement the
 * {@link #getAttachmentPointFingerprint(RWMol, UInt_Vect)} method and provide
 * constructors with the required settings for fingerprint generation, or their
 * default values, which over-ride or call the three provided constructors.
 * 
 * @author Steve Roughley <s.roughley@vernalis.com>
 * 
 */
public abstract class DefaultFragmentKeyFingerprinted extends FragmentKey
		implements FragmentKeyFingerprinted {
	protected HashMap<String, ExplicitBitVect> m_apEnvironments;

	/** The ROMol for the query for an attachment point */
	protected static final ROMol AP_QUERY_MOL = RWMol.MolFromSmarts("[#0]");

	/**
	 * Constructor - initialises an empty key
	 */
	protected DefaultFragmentKeyFingerprinted() {
		super();
	}

	/**
	 * Constructor initialises from an existing object
	 * 
	 * @param existingKey
	 *            The existing object
	 */
	protected DefaultFragmentKeyFingerprinted(FragmentKey existingKey) {
		super(existingKey);
	}

	/**
	 * Constructor initialising from a SMILES string, with one or more
	 * components
	 * 
	 * @param keyAsString
	 *            The key as a SMILES string
	 */
	protected DefaultFragmentKeyFingerprinted(String keyAsString) {
		super(keyAsString);
	}

	/**
	 * This method is the bare-bones requirement for implementing subclasses,
	 * and needs to generate the fingerprint for each component, taking account
	 * of any fingerprint options
	 * 
	 * @param mol
	 *            The component to generate a fingerprint of
	 * @param apIdx
	 *            The attachment point index
	 * @return The rooted fingerprint for the component
	 */
	protected abstract ExplicitBitVect getAttachmentPointFingerprint(RWMol mol,
			UInt_Vect apIdx);

	/** {@inheritDoc} */
	@Override
	public void calculateAttPointEnvironments(List<String> keyComponents) {
		for (String comp : keyComponents) {
			// 'De-isotopize', dealing with un-tracked or 1 cut cases
			comp = comp.replace("[*]", "[1*]");
			String comp2 = comp.replaceAll("\\[\\d+\\*", "[*");
			if (!m_apEnvironments.containsKey(comp2)) {
				RWMol mol = RWMol.MolFromSmiles(comp2, 0, false);
				RDKFuncs.sanitizeMol(mol);
				// Get the atom ID of the attachment point
				UInt_Vect apIdx = new UInt_Vect();
				apIdx.add(mol.getSubstructMatch(AP_QUERY_MOL).get(0)
						.getSecond());

				ExplicitBitVect fp = getAttachmentPointFingerprint(mol, apIdx);
				mol.delete();
				m_apEnvironments.put(comp, fp);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.mmp.FragmentKey#addComponent(java.lang.String
	 * )
	 */
	@Override
	public void addComponent(String smiles) {
		super.addComponent(smiles);
		calculateAttPointEnvironments(m_keyComponents);
	}

	/** {@inheritDoc} */
	@Override
	public void mergeKeys(FragmentKey otherKey) {
		super.mergeKeys(otherKey);
		if (this.getClass() == otherKey.getClass()) {
			// The two objects are the same type, so can copy FPs
			m_apEnvironments
					.putAll(((DefaultFragmentKeyFingerprinted) otherKey).m_apEnvironments);
		} else {
			calculateAttPointEnvironments(otherKey.m_keyComponents);
		}
	}

	/**
	 * Return {@link DenseBitVectorCell} of the fingerprint for the specified
	 * attachment point index
	 * 
	 * @param idx
	 *            The index of the attachment point
	 * @return The required fingerprint, or {@link DataType#getMissingCell()} if
	 *         the index was not found
	 */
	@Override
	public DataCell getAttachmentPointFingerprintDenseCell(int idx) {
		// Need first to find the key
		ExplicitBitVect ebv = getAttachmentPointExplicitBitVectorFingerprint(idx);
		if (ebv == null) {
			return DataType.getMissingCell();
		} else {
			return new DenseBitVectorCellFactory(
					RDKitUtils.rdkitFpToDenseBitVector(ebv)).createDataCell();
		}
	}

	/**
	 * Return {@link SparseBitVectorCell} of the fingerprint for the specified
	 * attachment point index
	 * 
	 * @param idx
	 *            The index of the attachment point
	 * @return The required fingerprint, or {@link DataType#getMissingCell()} if
	 *         the index was not found
	 */
	@Override
	public DataCell getAttachmentPointFingerprintSparseCell(int idx) {
		// Need first to find the key
		ExplicitBitVect ebv = getAttachmentPointExplicitBitVectorFingerprint(idx);
		if (ebv == null) {
			return DataType.getMissingCell();
		} else {
			return new SparseBitVectorCellFactory(
					RDKitUtils.rdkitFpToSparseBitVector(ebv)).createDataCell();
		}
	}

	/**
	 * Method to get a list of all the attachment point fingerprints as
	 * {@link DenseBitVectorCell}s. The list will be in ascending order of
	 * attachment point index, starting at 1, until the same number of
	 * attachment points have been found as there are SMILES components, or 100
	 * indices have failed to return attachment points. Indices not returning
	 * attachment points will retun {@link DataType#getMissingCell()}
	 * 
	 * @return {@link ArrayList} of {@link DenseBitVectorCell} fingerprints
	 */
	@Override
	public List<DataCell> getAttachmentPointFingerprintsDenseCellCollection() {
		ArrayList<DataCell> retVal = new ArrayList<DataCell>();
		int idx = 1;
		int numFound = 0;
		// Countdown to avoid infinite loops if the SMILES components are badly
		// formed
		int failedTries = 100;
		ExplicitBitVect ebv = null;
		while (numFound < m_apEnvironments.size() && failedTries > 0) {
			// Need first to find the key next
			ebv = getAttachmentPointExplicitBitVectorFingerprint(idx++);
			if (ebv != null) {
				// Incrememnt the counter
				numFound++;
				// And add the fp to the output
				retVal.add(new DenseBitVectorCellFactory(RDKitUtils
						.rdkitFpToDenseBitVector(ebv)).createDataCell());
			} else {
				// We already have found some, but this one is missing
				retVal.add(DataType.getMissingCell());
				failedTries--;
			}
		}
		return retVal;
	}

	/**
	 * Method to get a list of all the attachment point fingerprints as
	 * {@link SparseBitVectorCell}s. The list will be in ascending order of
	 * attachment point index, starting at 1, until the same number of
	 * attachment points have been found as there are SMILES components, or 100
	 * indices have failed to return attachment points. Indices not returning
	 * attachment points will retun {@link DataType#getMissingCell()}
	 * 
	 * @return {@link ArrayList} of {@link SparseBitVectorCell} fingerprints
	 */
	@Override
	public List<DataCell> getAttachmentPointFingerprintsSparseCellCollection() {
		ArrayList<DataCell> retVal = new ArrayList<DataCell>();
		int idx = 1;
		int numFound = 0;
		ExplicitBitVect ebv = null;
		while (numFound < m_apEnvironments.size()) {
			// Need first to find the key next
			ebv = getAttachmentPointExplicitBitVectorFingerprint(idx++);
			if (ebv != null) {
				// Incrememnt the counter
				numFound++;
				// And add the fp to the output
				retVal.add(new SparseBitVectorCellFactory(RDKitUtils
						.rdkitFpToSparseBitVector(ebv)).createDataCell());
			} else {
				// this one is missing
				retVal.add(DataType.getMissingCell());
			}
		}
		ebv.delete();
		return retVal;
	}

	/**
	 * Generate an array of {@link SparseBitVectorCell}s containing fingerprints
	 * for attachment points with indices in the range 1 - numCuts
	 * {@link DataType#getMissingCell()} is returned in positions for which a
	 * fingerprint was not found. The fingerprint for attachment point
	 * <code>n</code> is stored in position <code>n-1</code>
	 * 
	 * @param numCuts
	 *            The highest index to return
	 * @return Array of {@link SparseBitVectorCell} fingerprints
	 */
	@Override
	public DataCell[] getSparseAttachmentPointFingerprints(int numCuts) {
		DataCell[] retVal = new DataCell[numCuts];
		Arrays.fill(retVal, DataType.getMissingCell());
		for (int idx = 1; idx <= numCuts; idx++) {
			ExplicitBitVect ebv = getAttachmentPointExplicitBitVectorFingerprint(idx);
			if (ebv != null) {
				retVal[idx - 1] = new SparseBitVectorCellFactory(
						RDKitUtils.rdkitFpToSparseBitVector(ebv))
						.createDataCell();
			}
			ebv.delete();
		}
		return retVal;
	}

	/**
	 * Generate an array of {@link DenseBitVectorCell}s containing fingerprints
	 * for attachment points with indices in the range 1 - numCuts
	 * {@link DataType#getMissingCell()} is returned in positions for which a
	 * fingerprint was not found. The fingerprint for attachment point
	 * <code>n</code> is stored in position <code>n-1</code>
	 * 
	 * @param numCuts
	 *            The highest index to return
	 * @return Array of {@link DenseBitVectorCell} fingerprints
	 */
	@Override
	public DataCell[] getDenseAttachmentPointFingerprints(int numCuts) {
		DataCell[] retVal = new DataCell[numCuts];
		Arrays.fill(retVal, DataType.getMissingCell());
		for (int idx = 1; idx <= numCuts; idx++) {
			ExplicitBitVect ebv = getAttachmentPointExplicitBitVectorFingerprint(idx);
			if (ebv != null) {
				retVal[idx - 1] = new DenseBitVectorCellFactory(
						RDKitUtils.rdkitFpToDenseBitVector(ebv))
						.createDataCell();
			}
			ebv.delete();
		}
		return retVal;
	}

	/**
	 * Get the RDKit {@link ExplicitBitVect} representation of the fingerprint
	 * with the given index
	 * 
	 * @param idx
	 *            The index of the attackment point
	 * @return The {@link ExplicitBitVect} for the attachment point, or
	 *         <code>null</code> if no fingerprint was found for the specified
	 *         index
	 */
	@Override
	public ExplicitBitVect getAttachmentPointExplicitBitVectorFingerprint(
			int idx) {
		ExplicitBitVect ebv = null;
		for (Entry<String, ExplicitBitVect> ent : m_apEnvironments.entrySet()) {
			if (ent.getKey().indexOf("[" + idx + "*") >= 0) {
				ebv = ent.getValue();
				break;
			}
		}
		return ebv;
	}
}
