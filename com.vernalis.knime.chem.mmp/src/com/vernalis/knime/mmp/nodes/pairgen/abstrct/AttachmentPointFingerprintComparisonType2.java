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
package com.vernalis.knime.mmp.nodes.pairgen.abstrct;

import org.knime.core.data.vector.bitvector.DenseBitVector;
import org.knime.core.node.util.ButtonGroupEnumInterface;

import com.vernalis.knime.mmp.frags.simple.SimpleFragmentKey;

/**
 * Enum containing the different comparison methods for attachment point
 * fingerprints
 * 
 * @author s.roughley
 * 
 */
public enum AttachmentPointFingerprintComparisonType2 implements ButtonGroupEnumInterface {
	/**
	 * All fingerprints must pass the filter
	 */
	ALL {
		@Override
		public boolean checkPasses(SimpleFragmentKey leftKey, SimpleFragmentKey rightKey,
				AttachmentPointFingerprintSimilarityType similarityType, double threshold,
				Double alpha, Double beta) {
			return this.checkPasses(leftKey.getLeafFingerprints(), rightKey.getLeafFingerprints(),
					similarityType, threshold, alpha, beta);
		}

		@Override
		public boolean checkPasses(DenseBitVector[] leftFPs, DenseBitVector[] rightFPs,
				AttachmentPointFingerprintSimilarityType similarityType, double threshold,
				Double alpha, Double beta) {
			if (leftFPs.length != rightFPs.length) {
				throw new IllegalArgumentException(
						"Both molecules need the same number of attachments points");
			}
			for (int i = 0; i < leftFPs.length; i++) {
				double sim = similarityType.calculate(leftFPs[i], rightFPs[i], alpha, beta);
				if (sim < threshold) {
					return false;
				}
			}
			return true;
		}
	},

	/**
	 * Any fingerprint must pass the filter
	 */
	ANY {
		@Override
		public boolean checkPasses(SimpleFragmentKey leftKey, SimpleFragmentKey rightKey,
				AttachmentPointFingerprintSimilarityType similarityType, double threshold,
				Double alpha, Double beta) {
			return this.checkPasses(leftKey.getLeafFingerprints(), rightKey.getLeafFingerprints(),
					similarityType, threshold, alpha, beta);
		}

		@Override
		public boolean checkPasses(DenseBitVector[] leftFPs, DenseBitVector[] rightFPs,
				AttachmentPointFingerprintSimilarityType similarityType, double threshold,
				Double alpha, Double beta) {
			if (leftFPs.length != rightFPs.length) {
				throw new IllegalArgumentException(
						"Both molecules need the same number of attachments points");
			}
			for (int i = 0; i < leftFPs.length; i++) {
				double sim = similarityType.calculate(leftFPs[i], rightFPs[i], alpha, beta);
				if (sim >= threshold) {
					return true;
				}
			}
			return false;
		}
	},

	/**
	 * The concatenation of all fingerprints must pass the filter
	 */
	CONCATENATED {
		@Override
		public boolean checkPasses(SimpleFragmentKey leftKey, SimpleFragmentKey rightKey,
				AttachmentPointFingerprintSimilarityType similarityType, double threshold,
				Double alpha, Double beta) {
			if (leftKey.getNumComponents() != rightKey.getNumComponents()) {
				throw new IllegalArgumentException(
						"Both molecules need the same number of attachements points");
			}
			return similarityType.calculate(leftKey.getConcatenatedFingerprints(),
					rightKey.getConcatenatedFingerprints(), alpha, beta) >= threshold;
		}

		@Override
		public boolean checkPasses(DenseBitVector[] leftFPs, DenseBitVector[] rightFPs,
				AttachmentPointFingerprintSimilarityType similarityType, double threshold,
				Double alpha, Double beta) {
			if (leftFPs.length != 1 || leftFPs.length != rightFPs.length) {
				throw new IllegalArgumentException(
						"Both molecules need the same number of attachements points");
			}
			return similarityType.calculate(leftFPs[0], rightFPs[0], alpha, beta) >= threshold;
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.util.ButtonGroupEnumInterface#getText()
	 */
	@Override
	public String getText() {
		return this.name();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.util.ButtonGroupEnumInterface#getActionCommand()
	 */
	@Override
	public String getActionCommand() {
		return this.name();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.util.ButtonGroupEnumInterface#getToolTip()
	 */
	@Override
	public String getToolTip() {
		return this.name();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.util.ButtonGroupEnumInterface#isDefault()
	 */
	@Override
	public boolean isDefault() {
		return this.equals(getDefault());
	}

	/**
	 * @return The default comparison type
	 */
	public static AttachmentPointFingerprintComparisonType2 getDefault() {
		return ALL;
	}

	/**
	 * Method to check whether two sets fingerprints pass the comparison filter
	 * 
	 * @param leftKey
	 *            The left fingerprint(s)
	 * @param rightKey
	 *            The right fingerprint(s)
	 * @param similarityType
	 *            The {@link AttachmentPointFingerprintSimilarityType} for the
	 *            comparison
	 * @param threshold
	 *            The similarity threshold
	 * @param alpha
	 *            The Tversky alpha parameter
	 * @param beta
	 *            The Tversky beta parameter
	 * @return {@code true} if the comparison was a 'pass'
	 */
	public abstract boolean checkPasses(SimpleFragmentKey leftKey, SimpleFragmentKey rightKey,
			AttachmentPointFingerprintSimilarityType similarityType, double threshold, Double alpha,
			Double beta);

	/**
	 * Method to check whether two sets fingerprints pass the comparison filter
	 * 
	 * @param leftFPs
	 *            The left fingerprint(s)
	 * @param rightFPs
	 *            The right fingerprint(s)
	 * @param similarityType
	 *            The {@link AttachmentPointFingerprintSimilarityType} for the
	 *            comparison
	 * @param threshold
	 *            The similarity threshold
	 * @param alpha
	 *            The Tversky alpha parameter
	 * @param beta
	 *            The Tversky beta parameter
	 * @return {@code true} if the comparison was a 'pass'
	 */
	public abstract boolean checkPasses(DenseBitVector[] leftFPs, DenseBitVector[] rightFPs,
			AttachmentPointFingerprintSimilarityType similarityType, double threshold, Double alpha,
			Double beta);
}
