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
package com.vernalis.knime.fingerprint.nodes.logic.sum;

import com.vernalis.knime.fingerprint.nodes.abstrct.factory.AbstractDoubleFingerprintNodeFactory;

public class FingerprintSumNodeFactory
		extends AbstractDoubleFingerprintNodeFactory<FingerprintSumNodeModel> {

	public FingerprintSumNodeFactory() {
		super(false, false, true, true, "Fingerprint SUM", "fp_sum.png",
				"This node calculates the sum of two count (bytevector) fingerprints",
				new String[] {
						"This node calculates the sum of two count (bytevector) fingerprints",
						"The sum is the total sum of the values at each count position, and is loosely "
								+ "analogous to 'OR' for binary fingerprints. If "
								+ "the sum exceeds the maximum count value (255), then "
								+ "it stays at 255" },
				"Count (ByteVector) Fingerprints",
				"Input port containing two fingerprint columns to combine.",
				"Combined 'SUM' fingerprint", "Port containing the calculated 'SUM' fingerprint");

	}

	@Override
	protected FingerprintSumNodeModel createNodeModel(boolean allowSparseBitVector2,
			boolean allowDenseBitVector2, boolean allowSparseByteVector2,
			boolean allowDenseByteVector2) {
		return new FingerprintSumNodeModel();
	}

}
