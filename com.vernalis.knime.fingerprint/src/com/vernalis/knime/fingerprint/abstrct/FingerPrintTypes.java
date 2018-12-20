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
package com.vernalis.knime.fingerprint.abstrct;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.vector.bitvector.DenseBitVector;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCellFactory;
import org.knime.core.data.vector.bitvector.SparseBitVector;
import org.knime.core.data.vector.bitvector.SparseBitVectorCell;
import org.knime.core.data.vector.bitvector.SparseBitVectorCellFactory;
import org.knime.core.data.vector.bytevector.DenseByteVector;
import org.knime.core.data.vector.bytevector.DenseByteVectorCell;
import org.knime.core.data.vector.bytevector.DenseByteVectorCellFactory;
import org.knime.core.data.vector.bytevector.SparseByteVector;
import org.knime.core.data.vector.bytevector.SparseByteVectorCell;
import org.knime.core.data.vector.bytevector.SparseByteVectorCellFactory;
import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * An enum representing the types of fingerprint, and providing methods for
 * generating the correct datacell representation
 * 
 * @author s.roughley
 *
 */
public enum FingerPrintTypes implements ButtonGroupEnumInterface {
	SPARSE(SparseBitVectorCell.TYPE, SparseByteVectorCell.TYPE, "Sparse", Long.MAX_VALUE,
			Integer.MAX_VALUE) {
		@Override
		public DataCell getDataCellFromSetBits(long length, long[] setBits) {
			return (new SparseBitVectorCellFactory(new SparseBitVector(length, setBits)))
					.createDataCell();
		}

		@Override
		public DataCell getDataCellFromCounts(byte[] counts) {
			SparseByteVector sbv = new SparseByteVector(counts.length);
			for (int i = 0; i < counts.length; i++) {
				sbv.set(i, counts[i]);
			}
			return new SparseByteVectorCellFactory(sbv).createDataCell();
		}
	},

	DENSE(DenseBitVectorCell.TYPE, DenseByteVectorCell.TYPE, "Dense",
			((long) Integer.MAX_VALUE - 1) * 64, ((long) Integer.MAX_VALUE - 1) * 64) {
		@Override
		public DataCell getDataCellFromSetBits(long length, long[] setBits) {
			DenseBitVector dbv = new DenseBitVector(length);
			for (long l : setBits) {
				dbv.set(l);
			}
			return (new DenseBitVectorCellFactory(dbv)).createDataCell();
		}

		@Override
		public DataCell getDataCellFromCounts(byte[] counts) {
			DenseByteVector dbv = new DenseByteVector(counts);
			return new DenseByteVectorCellFactory(dbv).createDataCell();
		}
	};

	private final String m_name;
	private final long m_maxLength;
	private final long m_maxCardinality;
	public final DataType BIT_TYPE, COUNT_TYPE;

	private FingerPrintTypes(DataType bitType, DataType countType, String name, long maxLength,
			long maxCardinality) {
		this.BIT_TYPE = bitType;
		this.COUNT_TYPE = countType;
		this.m_name = name;
		this.m_maxLength = maxLength;
		this.m_maxCardinality = maxCardinality;
	}

	@Override
	public String getText() {
		return m_name;
	}

	@Override
	public String getActionCommand() {
		return this.name();
	}

	@Override
	public String getToolTip() {
		return this.getActionCommand();
	}

	@Override
	public boolean isDefault() {
		return this.equals(FingerPrintTypes.getDefaultMethod());
	}

	public static FingerPrintTypes getDefaultMethod() {
		return SPARSE;
	}

	/**
	 * @param isCount
	 *            is the fingerprint a count or binary type?
	 * @return The correct {@link DataType}
	 */
	public DataType getDataType(boolean isCount) {
		return isCount ? COUNT_TYPE : BIT_TYPE;
	}

	/**
	 * @return the maximum length of the fingerprint
	 */
	public long getMaxLength() {
		return m_maxLength;
	}

	/**
	 * @return the maximum cardinality of the fingerprint
	 */
	public long getMaxCardinality() {
		return m_maxCardinality;

	}

	/**
	 * Method to return a binary bitvector fingerprint from an array of the set
	 * bit indices
	 * 
	 * @param length
	 *            the length of the fingerprint
	 * @param setBits
	 *            the set bit indices
	 * @return The fingerprint cell
	 */
	public abstract DataCell getDataCellFromSetBits(long length, long[] setBits);

	/**
	 * Method to return a count bytevector fingerprint from an array of set bit
	 * indices
	 * 
	 * @param counts
	 * @return
	 */
	public abstract DataCell getDataCellFromCounts(byte[] counts);

}
