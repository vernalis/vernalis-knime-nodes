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
package com.vernalis.knime.fingerprint.nodes.props.totalcount;

import com.vernalis.knime.fingerprint.nodes.abstrct.factory.AbstractSingleFingerprintNodeFactory;

public class FingerprintTotalCountNodeFactory
		extends AbstractSingleFingerprintNodeFactory<FingerprintTotalCountNodeModel> {
	public FingerprintTotalCountNodeFactory() {
		super(true, true, true, true, "Total Count", "fp_sigma.png",
				"This node calculates the fingerprint total bit count",
				new String[] { "This node calculates the fingerprint total bit counts",
						"For binary (bitvector) fingerprints, this is the number of "
								+ "set bits (i.e. is the same as cardinality)",
						"For count (bytevector) fingerprints, this is the sum of all "
								+ "the individual counts" },
				"Fingerprints", "Table of fingerprints for total count calculation", "Total counts",
				"Table with calculated fingerprint total counts");

	}

	@Override
	protected FingerprintTotalCountNodeModel createNodeModel(boolean allowSparseBitVector2,
			boolean allowDenseBitVector2, boolean allowSparseByteVector2,
			boolean allowDenseByteVector2) {
		return new FingerprintTotalCountNodeModel();
	}

}
