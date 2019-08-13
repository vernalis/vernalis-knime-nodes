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
package com.vernalis.knime.fingerprint.nodes.props.isEmpty;

import com.vernalis.knime.fingerprint.nodes.abstrct.factory.AbstractSingleFingerprintNodeFactory;

public class FingerprintEmptyNodeFactory
		extends AbstractSingleFingerprintNodeFactory<FingerprintEmptyNodeModel> {

	public FingerprintEmptyNodeFactory() {
		super(true, true, true, true, "Is Empty", "fp_isempty.png",
				"This node calculates the fingerprint 'Emptiness'",
				new String[] { "This node calculates the fingerprint 'Emptiness'",
						"For binary (bitvector) fingerprints, , this is 'true' is there are no set bits",
						"For count (bytevector) fingerprints, this is 'true' if all counts are 0." },
				"Fingerprints", "Table of fingerprints", "Emptiness",
				"Output table with the 'emptiness' of the fingerprint. "
						+ "'True' is no bits set or all counts are 0");
	}

	@Override
	protected FingerprintEmptyNodeModel createNodeModel(boolean allowSparseBitVector2,
			boolean allowDenseBitVector2, boolean allowSparseByteVector2,
			boolean allowDenseByteVector2) {
		return new FingerprintEmptyNodeModel();
	}

}
