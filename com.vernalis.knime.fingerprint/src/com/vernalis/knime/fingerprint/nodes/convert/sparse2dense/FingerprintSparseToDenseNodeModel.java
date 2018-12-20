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
package com.vernalis.knime.fingerprint.nodes.convert.sparse2dense;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.vector.bitvector.DenseBitVector;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCellFactory;
import org.knime.core.data.vector.bitvector.SparseBitVector;
import org.knime.core.data.vector.bitvector.SparseBitVectorCell;
import org.knime.core.data.vector.bytevector.DenseByteVector;
import org.knime.core.data.vector.bytevector.DenseByteVectorCell;
import org.knime.core.data.vector.bytevector.DenseByteVectorCellFactory;
import org.knime.core.data.vector.bytevector.SparseByteVector;
import org.knime.core.data.vector.bytevector.SparseByteVectorCell;

import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractSingleFingerprintSingleOutputNodeModel;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public class FingerprintSparseToDenseNodeModel
		extends AbstractSingleFingerprintSingleOutputNodeModel {

	public FingerprintSparseToDenseNodeModel() {
		// Any sparse input
		super(true, false, true, false, null);
	}

	@Override
	protected String getResultColumnName() {
		return m_firstColName.getStringValue() + " (Dense)";
	}

	@Override
	protected DataCell getByteVectorResultCell(DataCell fp1) {
		if (m_fpType == DenseByteVectorCell.TYPE) {
			// Return a Dense Input unchanged
			return fp1;
		}

		SparseByteVector sbv = ((SparseByteVectorCell) fp1).getByteVectorCopy();
		DenseByteVector dbv = new DenseByteVector((int) sbv.length());
		int lastSetBit = (int) sbv.nextCountIndex(0);
		while (lastSetBit >= 0) {
			int count = sbv.get(lastSetBit);
			dbv.set(lastSetBit, count);
			lastSetBit = (int) sbv.nextCountIndex(lastSetBit + 1);
		}
		return new DenseByteVectorCellFactory(dbv).createDataCell();
	}

	@Override
	protected DataCell getBitVectorResultCell(DataCell fp1) {
		if (m_fpType == DenseBitVectorCell.TYPE) {
			// Return a Dense Input unchanged
			return fp1;
		}
		SparseBitVector sbv = ((SparseBitVectorCell) fp1).getBitVectorCopy();
		DenseBitVector dbv = new DenseBitVector(sbv.length());
		long lastSetBit = sbv.nextSetBit(0);
		while (lastSetBit >= 0) {
			dbv.set(lastSetBit);
			lastSetBit = sbv.nextSetBit(lastSetBit + 1);
		}
		return new DenseBitVectorCellFactory(dbv).createDataCell();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.fingerprint.all.nodes.abstrct.
	 * AbstractSingleFingerprintNodeModel#getResultColumnType()
	 */
	@Override
	protected DataType getResultColumnType() {
		return m_fpType == SparseBitVectorCell.TYPE ? DenseBitVectorCell.TYPE
				: DenseByteVectorCell.TYPE;
	}

}
