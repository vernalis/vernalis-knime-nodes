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
package com.vernalis.knime.fingerprint.nodes.props.cardinality;

import com.vernalis.knime.fingerprint.nodes.abstrct.factory.AbstractSingleFingerprintNodeFactory;

public class FingerprintCardinalityNodeFactory
		extends AbstractSingleFingerprintNodeFactory<FingerprintCardinalityNodeModel> {

	/**
	 */
	public FingerprintCardinalityNodeFactory() {
		super("Cardinality", "fp_cardinality.png", "Returns the cardinality of a fingerprint",
				new String[] { "This node calculates the cardinality of a fingerprint.",
						"For binary (BitVector) fingerprints, this is the number of set bits.",
						"For count (ByteVector) fingerprints, this is the number of bits with a non-zero count." },
				"Fingerprints", "Input table containing a fingerprint column", "Cardinality",
				"Output table with the cardinalities of the fingerprints");
		init();

	}

	@Override
	protected FingerprintCardinalityNodeModel createNodeModel(boolean allowSparseBitVector2,
			boolean allowDenseBitVector2, boolean allowSparseByteVector2,
			boolean allowDenseByteVector2) {
		return new FingerprintCardinalityNodeModel();
	}

}
