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
package com.vernalis.knime.fingerprint.nodes.convert.sparse2dense;

import com.vernalis.knime.fingerprint.nodes.abstrct.factory.AbstractSingleFingerprintNodeFactory;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 *
 */
public class FingerprintSparseToDenseNodeFactory
		extends AbstractSingleFingerprintNodeFactory<FingerprintSparseToDenseNodeModel> {

	/**
	 * Constructor
	 */
	public FingerprintSparseToDenseNodeFactory() {
		// Any sparse input
		super(true, false, true, false, "Sparse To Dense", "fp_sparse2dense.png",
				"This node converts 'Sparse' binary (bitvector) or count (bytevector) fingerprints to "
						+ "the corresponding 'Dense' representation",
				new String[] {
						"This node converts 'Sparse' binary (bitvector) or count (bytevector) fingerprints to "
								+ "the corresponding 'Dense' representation",
						"NB Dense fingerprints are length-restricted to Integer.MAX_VALUE "
								+ "whereas Sparse fingerprints are restricted to Long.MAX_VALUE in length "
								+ "and Integer.MAX_VALUE cardinality" },
				"Sparse Fingerprints", "Table of 'sparse' fingerprints", "Dense Fingerprints",
				"Table with 'dense' representation of fingerprints");
	}

	@Override
	protected FingerprintSparseToDenseNodeModel createNodeModel(boolean allowSparseBitVector2,
			boolean allowDenseBitVector2, boolean allowSparseByteVector2,
			boolean allowDenseByteVector2) {
		return new FingerprintSparseToDenseNodeModel();
	}

}
