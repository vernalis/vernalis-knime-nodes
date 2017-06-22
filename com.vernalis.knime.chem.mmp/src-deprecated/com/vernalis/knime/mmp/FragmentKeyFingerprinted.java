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
import java.util.List;

import org.RDKit.ExplicitBitVect;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.SparseBitVectorCell;

/**
 * This interface provides the API methods required for Attachment point
 * environment fingerprinted {@link FragmentKey}s. Implementations should
 * sub-class {@link FragmentKey}.
 * 
 * @author Steve Roughley knime@vernalis.com
 * 
 */
public interface FragmentKeyFingerprinted {

	/**
	 * Generate the attachment point environment fingerprints rooted on the
	 * attachment point atoms Thie method should be called by methods updating
	 * the object and should not need explicitly to be called publically
	 * 
	 * @param keyComponents
	 *            The list of key components
	 * 
	 */
	public void calculateAttPointEnvironments(List<String> keyComponents);

	/**
	 * Add a component to the Fragment Key
	 * 
	 * @param smiles
	 *            The SMILES string of the component to be added
	 */
	public void addComponent(String smiles);

	/**
	 * Merge the keys from a second {@link FragmentKey} object. Implementations
	 * need to handle different Fingerprinted subtypes
	 * 
	 * @param otherKey
	 *            The other fragement key
	 */
	public void mergeKeys(FragmentKey otherKey);

	/**
	 * Return {@link DenseBitVectorCell} of the fingerprint for the specified
	 * attachment point index
	 * 
	 * @param idx
	 *            The index of the attachment point
	 * @return The required fingerprint, or {@link DataType#getMissingCell()} if
	 *         the index was not found
	 */
	public DataCell getAttachmentPointFingerprintDenseCell(int idx);

	/**
	 * Return {@link SparseBitVectorCell} of the fingerprint for the specified
	 * attachment point index
	 * 
	 * @param idx
	 *            The index of the attachment point
	 * @return The required fingerprint, or {@link DataType#getMissingCell()} if
	 *         the index was not found
	 */
	public DataCell getAttachmentPointFingerprintSparseCell(int idx);

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
	public List<DataCell> getAttachmentPointFingerprintsDenseCellCollection();

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
	public List<DataCell> getAttachmentPointFingerprintsSparseCellCollection();

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
	public DataCell[] getSparseAttachmentPointFingerprints(int numCuts);

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
	public DataCell[] getDenseAttachmentPointFingerprints(int numCuts);

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
	public ExplicitBitVect getAttachmentPointExplicitBitVectorFingerprint(
			int idx);
}
