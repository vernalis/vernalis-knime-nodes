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
package com.vernalis.knime.fingerprint.nodes.logic.min;

import com.vernalis.knime.fingerprint.nodes.abstrct.factory.AbstractDoubleFingerprintNodeFactory;

public class FingerprintMinNodeFactory
		extends AbstractDoubleFingerprintNodeFactory<FingerprintMinNodeModel> {

	public FingerprintMinNodeFactory() {
		super(false, false, true, true, "Fingerprint MIN", "fp_min.png",
				"This node calculates the minimum of two count (bytevector) fingerprints",
				new String[] {
						"This node calculates the minimum of two count (bytevector) fingerprints",
						"The minimum is the lowest value at each count position, and is loosely "
								+ "analogous to 'AND' for binary fingerprints" },
				"Count (ByteVector) Fingerprints",
				"Input port containing two fingerprint columns to combine.",
				"Combined 'MIN' fingerprint", "Port containing the calculated 'MIN' fingerprint");

	}

	@Override
	protected FingerprintMinNodeModel createNodeModel(boolean allowSparseBitVector2,
			boolean allowDenseBitVector2, boolean allowSparseByteVector2,
			boolean allowDenseByteVector2) {
		return new FingerprintMinNodeModel();
	}

}
