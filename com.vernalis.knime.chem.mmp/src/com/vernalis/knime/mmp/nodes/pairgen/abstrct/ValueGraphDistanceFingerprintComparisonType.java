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

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.vector.bytevector.ByteVectorValue;
import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * The method used to comparw the value graph distance similarity fingerprints
 * 
 * @author s.roughley
 *
 */
public enum ValueGraphDistanceFingerprintComparisonType implements ButtonGroupEnumInterface {
	NONE("None", "No filtering") {
		@Override
		public Number calculateDistSim(ByteVectorValue leftFp, ByteVectorValue rightFp) {
			throw new UnsupportedOperationException();
		}
	},

	MAX_TOTAL_DISTANCE_CHANGE("Max. total graph distance change",
			"The maximum total graph distance change between attachment points", true, true,
			Integer.valueOf(0), Integer.valueOf(Integer.MAX_VALUE)) {

		@Override
		protected Integer calculateDistSim(ByteVectorValue leftFp, ByteVectorValue rightFp) {

			int totalDist = 0;
			for (int i = 0; i < leftFp.length(); i++) {
				totalDist += Math.abs(leftFp.get(i) - rightFp.get(i));
			}
			return Integer.valueOf(totalDist);
		}
	},

	MAX_SINGLE_DISTANCE_CHANGE("Max. single graph distance change",
			"The maximum graph distance change between any 2 attachment points", true, true,
			Integer.valueOf(0), Integer.valueOf(Integer.MAX_VALUE)) {

		@Override
		public Integer calculateDistSim(ByteVectorValue leftFp, ByteVectorValue rightFp) {
			int maxDist = 0;
			for (int i = 0; i < leftFp.length(); i++) {
				maxDist = Math.max(maxDist, Math.abs(leftFp.get(i) - rightFp.get(i)));
			}
			return Integer.valueOf(maxDist);
		}
	},

	TANIMOTO_SIM("Tanimoto Similarity", "The minimum Tanimoto Similarity", false, false,
			Double.valueOf(-1.0 / 3.0), Double.valueOf(1.0)) {
		@Override
		public Double calculateDistSim(ByteVectorValue leftFp, ByteVectorValue rightFp) {
			double leftSqSum = 0.0;
			double rightSqSum = 0.0;
			double productSum = 0.0;
			for (int i = 0; i < leftFp.length(); i++) {
				leftSqSum += leftFp.get(i) * leftFp.get(i);
				rightSqSum += rightFp.get(i) * rightFp.get(i);
				productSum += leftFp.get(i) * rightFp.get(i);
			}
			return Double.valueOf(productSum / (leftSqSum + rightSqSum - productSum));
		}
	},

	DICE_SIM("Dice Similarity", "The minimum Dice Similarity") {
		@Override
		public Double calculateDistSim(ByteVectorValue leftFp, ByteVectorValue rightFp) {
			double leftSqSum = 0.0;
			double rightSqSum = 0.0;
			double productSum = 0.0;
			for (int i = 0; i < leftFp.length(); i++) {
				leftSqSum += leftFp.get(i) * leftFp.get(i);
				rightSqSum += rightFp.get(i) * rightFp.get(i);
				productSum += leftFp.get(i) * rightFp.get(i);
			}
			return Double.valueOf(2.0 * productSum / (leftSqSum + rightSqSum));
		}
	},

	COSINE_SIM("Cosine Similarity", "The minimum Cosine Similarity") {
		@Override
		public Double calculateDistSim(ByteVectorValue leftFp, ByteVectorValue rightFp) {
			double leftSqSum = 0.0;
			double rightSqSum = 0.0;
			double productSum = 0.0;
			for (int i = 0; i < leftFp.length(); i++) {
				leftSqSum += leftFp.get(i) * leftFp.get(i);
				rightSqSum += rightFp.get(i) * rightFp.get(i);
				productSum += leftFp.get(i) * rightFp.get(i);
			}
			return Double.valueOf(productSum / Math.sqrt(leftSqSum * rightSqSum));
		}
	},

	EUCLIDEAN_DIST("Euclidean Distance", "The maximum Euclidean Distance", true, false,
			Double.valueOf(0.0), Double.MAX_VALUE) {
		@Override
		public Double calculateDistSim(ByteVectorValue leftFp, ByteVectorValue rightFp) {
			double sumOfSquaredDiffs = 0.0;
			for (int i = 0; i < leftFp.length(); i++) {
				sumOfSquaredDiffs +=
						(leftFp.get(i) - rightFp.get(i)) * (leftFp.get(i) - rightFp.get(i));
			}
			return Double.valueOf(Math.sqrt(sumOfSquaredDiffs));
		}
	},

	HAMMING_DIST("Hamming Distance", "The maximum Hamming (Manhattan, or 'City-block') Distance",
			true, true, Double.valueOf(0.0), Double.MAX_VALUE) {
		@Override
		public Integer calculateDistSim(ByteVectorValue leftFp, ByteVectorValue rightFp) {
			int sumOfAbsDiffs = 0;
			for (int i = 0; i < leftFp.length(); i++) {
				sumOfAbsDiffs += Math.abs(leftFp.get(i) - rightFp.get(i));
			}
			return Integer.valueOf(sumOfAbsDiffs);
		}

	},

	SOERGEL_DIST("Soergel Distance", "The maximum Soergel Distance", true, false,
			Double.valueOf(0.0), Double.valueOf(1.0)) {

		@Override
		public Double calculateDistSim(ByteVectorValue leftFp, ByteVectorValue rightFp) {
			int sumOfAbsDiffs = 0;
			int sumOfMaxes = 0;
			for (int i = 0; i < leftFp.length(); i++) {
				sumOfAbsDiffs += Math.abs(leftFp.get(i) - rightFp.get(i));
				sumOfMaxes += Math.max(leftFp.get(i), rightFp.get(i));
			}
			return Double.valueOf(1.0 * sumOfAbsDiffs / sumOfMaxes);
		}
	};

	private String text;
	private String tooltip;
	private boolean isDistance, isInteger;

	/**
	 * @return the isInteger
	 */
	public boolean isInteger() {
		return isInteger;
	}

	private Number min, max;

	private ValueGraphDistanceFingerprintComparisonType(String text, String tooltip,
			boolean isDistance, boolean isInteger) {
		this(text, tooltip, isDistance, isInteger, Double.valueOf(-1.0), Double.valueOf(1.0));
	}

	private ValueGraphDistanceFingerprintComparisonType(String text, String tooltip,
			boolean isDistance, boolean isInteger, Number min, Number max) {
		this.text = text;
		this.tooltip = text + (tooltip.equals(text) ? "" : (" - " + tooltip));
		this.isDistance = isDistance;
		this.isInteger = isInteger;
		this.min = min;
		this.max = max;
	}

	private ValueGraphDistanceFingerprintComparisonType(String text, String tooltip) {
		this(text, tooltip, false, false);
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public String getActionCommand() {
		return this.name();
	}

	@Override
	public String getToolTip() {
		return tooltip;
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	/**
	 * @return Whether the method returns a distance (<code>true</code>) or a
	 *         similarity (<code>false</code>)
	 */
	public boolean isDistance() {
		return isDistance;
	}

	/**
	 * @return The default comparison method
	 */
	public static ValueGraphDistanceFingerprintComparisonType getDefault() {
		return NONE;
	}

	/**
	 * Method to get the distance or similarity between the fingerprints. This
	 * method is a wrapper which checks that the FPs are the same length (an
	 * {@link IllegalArgumentException} is thrown if not), and returns the
	 * minimum distance for the metric (or maximum similarity) if the length is
	 * <code>0</code>. The method calls
	 * {@link #calculateDistSim(ByteVectorValue, ByteVectorValue)} to perform
	 * the actual calculation after these initial checks
	 * 
	 * @param leftFp
	 *            The 'left' fingerprint
	 * @param rightFp
	 *            The 'right' fingerprint
	 * @return The distance or similarity for the two fingerprints
	 * @see #isDistance
	 */
	public Number getDistanceSimilarity(ByteVectorValue leftFp, ByteVectorValue rightFp) {
		if (leftFp.length() != rightFp.length()) {
			throw new IllegalArgumentException("Fingeprints must be of same length");
		}
		if (leftFp.length() == 0) {
			return isDistance() ? getMinimum() : getMaximum();
		}
		return calculateDistSim(leftFp, rightFp);
	}

	/**
	 * @param leftFp
	 *            The 'left' fingerprint
	 * @param rightFp
	 *            The 'right' fingerprint
	 * @return The distance or similarity for the two fingerprints
	 * @see #getDistanceSimilarity(ByteVectorValue, ByteVectorValue)
	 */
	protected abstract Number calculateDistSim(ByteVectorValue leftFp, ByteVectorValue rightFp);

	/**
	 * @param leftFp
	 *            The 'left' fingerprint
	 * @param rightFp
	 *            The 'right' fingerprint
	 * @return The distance or similarity for the two fingerprints as a
	 *         {@link DataCell} (either {@link IntCell} or {@link DoubleCell})
	 * @see #getDistanceSimilarity(ByteVectorValue, ByteVectorValue)
	 */
	public DataCell getDistanceSimilarityCell(ByteVectorValue leftFp, ByteVectorValue rightFp) {

		return getDataType() == IntCell.TYPE
				? new IntCell(calculateDistSim(leftFp, rightFp).intValue())
				: new DoubleCell(calculateDistSim(leftFp, rightFp).doubleValue());
	}

	/**
	 * @return The {@link DataType} for the distance/similarity output cell
	 */
	public DataType getDataType() {
		return isInteger ? IntCell.TYPE : DoubleCell.TYPE;
	}

	/**
	 * @return The minimum possible value for the metric
	 */
	public Number getMinimum() {
		return min;
	}

	/**
	 * @return The maximum possible value for the metric
	 */
	public Number getMaximum() {
		return max;
	}
}
