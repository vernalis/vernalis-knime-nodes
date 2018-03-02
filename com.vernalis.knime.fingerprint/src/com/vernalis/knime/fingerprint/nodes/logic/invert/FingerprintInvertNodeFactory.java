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
package com.vernalis.knime.fingerprint.nodes.logic.invert;

import com.vernalis.knime.fingerprint.nodes.abstrct.factory.AbstractSingleFingerprintNodeFactory;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 *
 */
public class FingerprintInvertNodeFactory
		extends AbstractSingleFingerprintNodeFactory<FingerprintInvertNodeModel> {

	public FingerprintInvertNodeFactory() {
		super(false, true, false, false, "Fingerprint NOT (Invert / Complement)",
				"fingerprint_converse.png",
				"Perform logic NOT (Invert or complement) operation on a binary (bitvector) fingerprint column",
				new String[] {
						"This node performs a logic NOT (Invert or complement) operation on a binary (bitvector) "
								+ "fingerprint column, i.e. all bits with value '1' are set to '0', and those with '0' to '1'. "
								+ "The column must be a DenseBitVector Cell type",
						"NB Types can be interconverted using the "
								+ "Sparse-to-Dense Fingerprint Convertor node" },

				"Fingerprints", "Input table containing a fingerprint column",
				"Inverted (Complement) Fingerprints",
				"Output table with complemented (or Inverted) fingerprints");
	}

	@Override
	protected FingerprintInvertNodeModel createNodeModel(boolean allowSparseBitVector2,
			boolean allowDenseBitVector2, boolean allowSparseByteVector2,
			boolean allowDenseByteVector2) {
		return new FingerprintInvertNodeModel();
	}

}
