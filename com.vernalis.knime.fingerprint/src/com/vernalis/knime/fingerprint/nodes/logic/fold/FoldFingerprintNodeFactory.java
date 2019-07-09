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
package com.vernalis.knime.fingerprint.nodes.logic.fold;

import com.vernalis.knime.fingerprint.nodes.abstrct.factory.AbstractSingleFingerprintNodeFactory;

public class FoldFingerprintNodeFactory
		extends AbstractSingleFingerprintNodeFactory<FoldFingerprintNodeModel> {

	/**
	 * Constructor
	 */
	public FoldFingerprintNodeFactory() {
		super(true, true, false, false, "Fingerprint FOLD", "fp_fold.png",
				"This node folds bitvector fingerprints",
				new String[] { "This node folds binary (bitvector) fingerprints",
						"The folding operation splits the fingerprint halfway along its length "
								+ "and 'OR's the two resulting fingeprints to get the result" },
				"Fingerprints", "Table of fingerprints for folding", "Folded fingerprints",
				"Table with folded fingerprints");
	}

	@Override
	protected FoldFingerprintNodeModel createNodeModel(boolean allowSparseBitVector2,
			boolean allowDenseBitVector2, boolean allowSparseByteVector2,
			boolean allowDenseByteVector2) {
		return new FoldFingerprintNodeModel();
	}

}
