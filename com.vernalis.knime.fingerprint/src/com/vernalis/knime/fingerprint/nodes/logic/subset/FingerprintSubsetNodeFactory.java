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
package com.vernalis.knime.fingerprint.nodes.logic.subset;

import java.util.LinkedHashMap;
import java.util.Map;

import org.knime.core.node.NodeDialogPane;

import com.vernalis.knime.fingerprint.nodes.abstrct.factory.AbstractSingleFingerprintNodeFactory;
import com.vernalis.knime.misc.EitherOr;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 *
 */
public class FingerprintSubsetNodeFactory
		extends AbstractSingleFingerprintNodeFactory<FingerprintSubsetNodeModel> {
	private static final EitherOr<Map<String, String>, Map<String, Map<String, String>>> opts;
	static {
		Map<String, String> tmp = new LinkedHashMap<>(DEFAULT_SINGLE_FP_OPTIONS.getLeft());
		tmp.put("Start", "The first index to be kept (0-indexed)");
		tmp.put("End", "The first index to be lost, or -1 to run to the end of the fingerprint");
		opts = EitherOr.ofLeft(tmp);
	}

	public FingerprintSubsetNodeFactory() {
		super(true, true, true, true, "Fingerprint SUBSET", "fp_subset.png",
				"Perform logic SUBSET operation on a binary (bitvector) or count (bytevector) fingerprint column",
				new String[] {
						"This node performs a logic SUBSET operation on a selected "
								+ "binary (bitvector) or count (bytevector) fingerprint column.",
						"The start value is inclusive, i.e. the value is the first bit or "
								+ "byte included in the subset (0-indexed). The end value is the first "
								+ "bit or byte to be excluded. Use '-1' to retrieve to the end of the "
								+ "fingerprint.",
						"If the start index is beyond the end of the fingerprint, a zero-length "
								+ "fingerprint is returned",
						"Examples", "Fingerprint '01100110' ",
						"Start = 3, End = -1 returns '00110'",
						"Start = 0, " + "End = 3 returns '011'",
						"NB Binary fingerprints are rendered in bigendian format, and "
								+ "so index 0 is the right-most displayed bit, whilst for "
								+ "count fingerprints the counts are in increasing order of index" },

				"Fingerprints", "Input table containing two fingerprint columns of the same type",
				"Subset Fingerprints", "Output table with subset fingerprints", opts);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new FingerprintSubsetNodeDialog();
	}

	@Override
	protected FingerprintSubsetNodeModel createNodeModel(boolean allowSparseBitVector2,
			boolean allowDenseBitVector2, boolean allowSparseByteVector2,
			boolean allowDenseByteVector2) {
		return new FingerprintSubsetNodeModel();

	}
}
