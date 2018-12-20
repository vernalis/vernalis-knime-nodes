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
package com.vernalis.knime.fingerprint.nodes.logic.max;

import com.vernalis.knime.fingerprint.nodes.abstrct.factory.AbstractDoubleFingerprintNodeFactory;

public class FingerprintMaxNodeFactory
		extends AbstractDoubleFingerprintNodeFactory<FingerprintMaxNodeModel> {

	public FingerprintMaxNodeFactory() {
		super(false, false, true, true, "Fingerprint MAX", "fp_max.png",
				"This node calculates the maximum of two count (bytevector) fingerprints",
				new String[] {
						"This node calculates the maximum of two count (bytevector) fingerprints",
						"The maximum is the highest value at each count position, and is loosely "
								+ "analogous to 'OR' for binary fingerprints" },
				"Count (ByteVector) Fingerprints",
				"Input port containing two fingerprint columns to combine.",
				"Combined 'MAX' fingerprint", "Port containing the calculated 'MAX' fingerprint");

	}

	@Override
	protected FingerprintMaxNodeModel createNodeModel(boolean allowSparseBitVector2,
			boolean allowDenseBitVector2, boolean allowSparseByteVector2,
			boolean allowDenseByteVector2) {
		return new FingerprintMaxNodeModel();
	}

}
