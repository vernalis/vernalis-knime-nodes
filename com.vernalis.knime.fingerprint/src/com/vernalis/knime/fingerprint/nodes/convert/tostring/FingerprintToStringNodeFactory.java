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
package com.vernalis.knime.fingerprint.nodes.convert.tostring;

import com.vernalis.knime.fingerprint.nodes.abstrct.factory.AbstractSingleFingerprintNodeFactory;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 *
 */
public class FingerprintToStringNodeFactory
		extends AbstractSingleFingerprintNodeFactory<FingerprintToStringNodeModel> {

	public FingerprintToStringNodeFactory() {
		super(true, true, true, true, "Fingerprint to String", "fingerprint_to_string.png",
				"This node converts fingerprints to a String representation",
				new String[] { "This node converts fingerprints to a String representation",
						"For binary (bitvector) fingerprints, this is of the form "
								+ "'{length=n, set bits=b1, b2, ...}'",
						"For count (bytevector) fingerprints, this is of the form '{c1, c2, ...}'",
						"where 'n' is the length of the fingerprint, 'b1', 'b2' etc."
								+ " are the indices of the set bits, and 'c1', 'c2' etc are the individual counts" },
				"Fingerprints", "Table of fingerprints", "Fingerprint Strings",
				"Table with string representation of fingerprints");
	}

	@Override
	protected FingerprintToStringNodeModel createNodeModel(boolean allowSparseBitVector2,
			boolean allowDenseBitVector2, boolean allowSparseByteVector2,
			boolean allowDenseByteVector2) {
		return new FingerprintToStringNodeModel();
	}

}
