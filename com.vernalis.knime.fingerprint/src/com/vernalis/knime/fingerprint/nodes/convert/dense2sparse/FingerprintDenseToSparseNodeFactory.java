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
package com.vernalis.knime.fingerprint.nodes.convert.dense2sparse;

import com.vernalis.knime.fingerprint.nodes.abstrct.factory.AbstractSingleFingerprintNodeFactory;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 *
 */
public class FingerprintDenseToSparseNodeFactory
		extends AbstractSingleFingerprintNodeFactory<FingerprintDenseToSparseNodeModel> {

	/**
	 * Constructor
	 */
	public FingerprintDenseToSparseNodeFactory() {
		// Allow either Dense input format
		super(false, true, false,
				false/* TODO: Re-enable when 0-length SBV bug is fixed! */, "Dense To Sparse",
				"fp_dense2sparse.png",
				"This node converts 'Dense' binary (bitvector) or count (bytevector) fingerprints to "
						+ "the corresponding 'Sparse' representation",
				new String[] {
						"This node converts 'Dense' binary (bitvector) or count (bytevector) fingerprints to "
								+ "the corresponding 'Sparse' representation",
						"NB Sparse fingerprints are restricted to Long.MAX_VALUE in length "
								+ "and Integer.MAX_VALUE cardinality whereas Dense fingerprints "
								+ "are length-restricted to Integer.MAX_VALUE " },
				"Dense Fingerprints", "Table of 'dense' fingerprints", "Sparse Fingerprints",
				"Table with 'sparse' representation of fingerprints");
	}

	@Override
	protected FingerprintDenseToSparseNodeModel createNodeModel(boolean allowSparseBitVector2,
			boolean allowDenseBitVector2, boolean allowSparseByteVector2,
			boolean allowDenseByteVector2) {
		return new FingerprintDenseToSparseNodeModel();
	}

}
