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
package com.vernalis.knime.fingerprint.nodes.convert.tobin;

import com.vernalis.knime.fingerprint.nodes.abstrct.factory.AbstractSingleFingerprintNodeFactory;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 *
 */
public class FingerprintToBinNodeFactory
		extends AbstractSingleFingerprintNodeFactory<FingerprintToBinNodeModel> {

	public FingerprintToBinNodeFactory() {
		super(true, true, false, false, "Fingerprint to Binary String", "fingerprint_to_BIN.png",
				"This node converts binary (bitvector) fingerprints to a binary String representation",
				new String[] {
						"This node converts binary (bitvector) fingerprints to a binary String representation" },
				"Fingerprints", "Table of fingerprints", "Binary Strings",
				"Table with binary string representation of fingerprints");
	}

	@Override
	protected FingerprintToBinNodeModel createNodeModel(boolean allowSparseBitVector2,
			boolean allowDenseBitVector2, boolean allowSparseByteVector2,
			boolean allowDenseByteVector2) {
		return new FingerprintToBinNodeModel();
	}

}
