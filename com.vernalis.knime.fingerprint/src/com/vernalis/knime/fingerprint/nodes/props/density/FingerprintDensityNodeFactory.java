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
package com.vernalis.knime.fingerprint.nodes.props.density;

import com.vernalis.knime.fingerprint.nodes.abstrct.factory.AbstractSingleFingerprintNodeFactory;

public class FingerprintDensityNodeFactory
		extends AbstractSingleFingerprintNodeFactory<FingerprintDensityNodeModel> {

	/**
	 * 
	 */
	public FingerprintDensityNodeFactory() {
		super(true, true, true, true, "Density", "fp_density.png",
				"This node calculates the fingerprint density",
				new String[] { "This node calculates the fingerprint densities",
						"For binary (bitvector) fingerprints, this is the proportion of set bits",
						"For count (bytevector) fingerprints, this is the ratio of the "
								+ "total of all counts to the maximum possible total (255 * length)" },
				"Fingerprints", "Table of fingerprints for density calculation", "Densities",
				"Table with calculated fingerprint densities");
	}

	@Override
	protected FingerprintDensityNodeModel createNodeModel(boolean allowSparseBitVector2,
			boolean allowDenseBitVector2, boolean allowSparseByteVector2,
			boolean allowDenseByteVector2) {
		return new FingerprintDensityNodeModel();
	}

}
