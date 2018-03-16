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

/**
 * <p>
 * Enum for the similarity metrics available for fingerprint comparison.
 * </p>
 * <p>
 * In all cases:
 * <ul>
 * <li>{@code a} - The number of 'left' set bits</li>
 * <li>{@code b} - The number of 'right' set bits</li>
 * <li>{@code c} - The number of set bits common to both</li>
 * </ul>
 * </p>
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public enum AttachmentPointFingerprintSimilarityType implements ButtonGroupEnumInterface {
	/**
	 * Tanimoto Similarity = c / (a+b-c)
	 */
	TANIMOTO {
		@Override
		protected double calculateSimilarity(DenseBitVector dbv0, DenseBitVector dbv1,
				Double object, Double object2) {
			long a = dbv0.cardinality();
			long b = dbv1.cardinality();
			if (a == 0 && b == 0) {
				return 1.0;
			}
			long c = dbv0.and(dbv1).cardinality();
			return (double) c / (double) (a + b - c);
		}

	},

	/**
	 * Cosine Similarity = c / ((a•b)^(1/2))
	 */
	COSINE {
		@Override
		protected double calculateSimilarity(DenseBitVector dbv0, DenseBitVector dbv1,
				Double object, Double object2) {
			long a = dbv0.cardinality();
			long b = dbv1.cardinality();
			if (a == 0 && b == 0) {
				return 1.0;
			}
			long c = dbv0.and(dbv1).cardinality();
			return c / Math.sqrt(a * b);
		}

	},

	/**
	 * Dice similarity = 2*c / (a+b)
	 */
	DICE {
		@Override
		protected double calculateSimilarity(DenseBitVector dbv0, DenseBitVector dbv1,
				Double object, Double object2) {
			long a = dbv0.cardinality();
			long b = dbv1.cardinality();
			if (a == 0 && b == 0) {
				return 1.0;
			}
			long c = dbv0.and(dbv1).cardinality();
			return 2.0 * c / (a + b);
		}

	},

	/**
	 * Tversky Similarity = c / (alpha*(a-c) + beta*(b-c) + c)
	 */
	TVERSKY {
		@Override
		protected double calculateSimilarity(DenseBitVector dbv0, DenseBitVector dbv1, Double alpha,
				Double beta) {
			long a = dbv0.cardinality();
			long b = dbv1.cardinality();
			if (a == 0 && b == 0) {
				return 1.0;
			}
			long c = dbv0.and(dbv1).cardinality();
			return c / (alpha * (a - c) + beta * (b - c) + c);
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
	 * @return The default value
	 */
	public static AttachmentPointFingerprintSimilarityType getDefault() {
		return TANIMOTO;
	}

	/**
	 * Calculate the similarity between two {@link DenseBitVector}s
	 * 
	 * @param dbv0
	 *            The left fingeprint
	 * @param dbv1
	 *            The right fingerprint
	 * @param alpha
	 *            The Tversky 'alpha' parameter (can be {@code null} for others)
	 * @param beta
	 *            The Tversky 'beta' parameter (can be {@code null} for others)
	 * @return The calculated similarity
	 */
	public double calculate(DenseBitVector dbv0, DenseBitVector dbv1, Double alpha, Double beta) {
		if (this.equals(TVERSKY) && (alpha == null || beta == null)) {
			throw new IllegalArgumentException("Tversky needs alpha and beta coefficients");
		}
		return calculateSimilarity(dbv0, dbv1, alpha, beta);
	}

	/**
	 * Overloaded method when alpha and beta are not required.
	 * 
	 * @throws UnsupportedOperationException
	 *             If the Similarity Type is Tversky
	 * 
	 * @see #calculate(DenseBitVector, DenseBitVector, Double, Double)
	 */
	public double calculate(DenseBitVector dbv0, DenseBitVector dbv1)
			throws UnsupportedOperationException {
		if (this.equals(TVERSKY)) {
			throw new UnsupportedOperationException(
					"TVERSKY cannot be calculated without alpha and beta");
		}
		return calculateSimilarity(dbv0, dbv1, null, null);
	}

	/**
	 * This is the actual workhorse method which does the similarity
	 * calculation, and is called by the public methods. Calculate the
	 * similarity between two {@link DenseBitVector}s
	 * 
	 * @param dbv0
	 *            The left fingeprint
	 * @param dbv1
	 *            The right fingerprint
	 * @param alpha
	 *            The Tversky 'alpha' parameter (can be {@code null} for others)
	 * @param beta
	 *            The Tversky 'beta' parameter (can be {@code null} for others)
	 * @return The calculated similarity
	 */
	protected abstract double calculateSimilarity(DenseBitVector dbv0, DenseBitVector dbv1,
			Double alpha, Double beta);
}