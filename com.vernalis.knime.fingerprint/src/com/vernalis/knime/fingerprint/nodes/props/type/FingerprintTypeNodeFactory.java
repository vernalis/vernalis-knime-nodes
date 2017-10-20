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
package com.vernalis.knime.fingerprint.nodes.props.type;

import com.vernalis.knime.fingerprint.nodes.abstrct.factory.AbstractSingleFingerprintNodeFactory;

public class FingerprintTypeNodeFactory
		extends AbstractSingleFingerprintNodeFactory<FingerprintTypeNodeModel> {

	public FingerprintTypeNodeFactory() {
		super(true, true, true, true, "Type", "fp_type.png",
				"This node calculates the fingerprint type",
				new String[] {
						"This node calculates the Type of a fingerprint. Both "
								+ "BitVector ('binary') and ByteVector ('count') fingerprints are"
								+ "supported",
						"Fingerprints are stored as Sparse or Dense representations. For "
								+ "most purposes, these are interchangeable. Sparse can be longer, "
								+ "but has limited cardinality, whereas Dense is shorter, but in all "
								+ "bits can be set." },
				"Fingerprints", "Table of fingerprints for total typing", "Fingerprint Types",
				"Table with calculated fingerprint types");
	}

	@Override
	protected FingerprintTypeNodeModel createNodeModel(boolean allowSparseBitVector2,
			boolean allowDenseBitVector2, boolean allowSparseByteVector2,
			boolean allowDenseByteVector2) {
		return new FingerprintTypeNodeModel();
	}

}
