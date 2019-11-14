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
package com.vernalis.knime.fingerprint.nodes.convert.tocountslist;

import com.vernalis.knime.fingerprint.nodes.abstrct.factory.AbstractSingleFingerprintNodeFactory;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 *
 */
public class FingerprintToCountsListNodeFactory
		extends AbstractSingleFingerprintNodeFactory<FingerprintToCountsListNodeModel> {

	public FingerprintToCountsListNodeFactory() {
		super(false, false, true, true, "Fingerprint to Counts List", "fingerprint_to_listcell.png",
				"This node converts a count (bytevector) fingerprints column to a column of List cells "
						+ "containing all the individual counts",
				new String[] {
						"This node converts a count (bytevector) fingerprints column to a column of List cells "
								+ "containing all the individual counts" },
				"Fingerprints", "Table of fingerprints", "Fingerprint counts",
				"Output table with a list of counts");
	}

	@Override
	protected FingerprintToCountsListNodeModel createNodeModel(boolean allowSparseBitVector2,
			boolean allowDenseBitVector2, boolean allowSparseByteVector2,
			boolean allowDenseByteVector2) {
		return new FingerprintToCountsListNodeModel();
	}

}
