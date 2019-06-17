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
package com.vernalis.knime.fingerprint.nodes.logic.concatenate;

import com.vernalis.knime.fingerprint.nodes.abstrct.factory.AbstractDoubleFingerprintNodeFactory;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 *
 */
public class FingerprintConcatNodeFactory
		extends AbstractDoubleFingerprintNodeFactory<FingerprintConcatNodeModel> {

	public FingerprintConcatNodeFactory() {
		super(true, true, true, true, "Fingerprint CONCATENATE", "fingerprint_concat.png",
				"Perform logic CONCATENATE operation on a pair of binary (bitvector) or "
						+ "count (bytevector) fingerprint columns",
				new String[] {
						"This node performs a logic CONCATENATE operation on a pair of binary (bitvector) or "
								+ "count (bytevector) fingerprint columns. The two columns must be of the same type",
						"NB Types can be interconverted using the "
								+ "Sparse-to-Dense Fingerprint Convertor node" },
				"Fingerprints", "Input table containing two fingerprint columns of the same type",
				"Concatenated Fingerprints", "Output table with combined fingerprints");
	}

	@Override
	protected FingerprintConcatNodeModel createNodeModel(boolean allowSparseBitVector2,
			boolean allowDenseBitVector2, boolean allowSparseByteVector2,
			boolean allowDenseByteVector2) {
		return new FingerprintConcatNodeModel();
	}

}
