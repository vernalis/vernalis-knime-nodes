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
package com.vernalis.knime.fingerprint.nodes.convert.tosetbitslist;

import com.vernalis.knime.fingerprint.nodes.abstrct.factory.AbstractSingleFingerprintNodeFactory;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 *
 */
public class FingerprintToSetBitsListNodeFactory
		extends AbstractSingleFingerprintNodeFactory<FingerprintToSetBitsListNodeModel> {

	public FingerprintToSetBitsListNodeFactory() {
		super(true, true, false, false, "Fingerprint to Set Bits List",
				"fingerprint_to_listcell.png",
				"This node converts bitvector fingerprints to a List of Set bits",
				new String[] { "This node converts a fingerprint column to a List Cell containing "
						+ "a list of the set bits. A cell containing the fingerprint length is "
						+ "also returned." },
				"Fingerprints", "Table of fingerprints", "Fingerprint Set Bits",
				"Output table with the list of set bits and length added");
	}

	@Override
	protected FingerprintToSetBitsListNodeModel createNodeModel(boolean allowSparseBitVector2,
			boolean allowDenseBitVector2, boolean allowSparseByteVector2,
			boolean allowDenseByteVector2) {
		return new FingerprintToSetBitsListNodeModel();
	}

}
