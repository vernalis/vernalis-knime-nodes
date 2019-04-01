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
package com.vernalis.knime.fingerprint.nodes.convert.dense2sparse;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.vector.bitvector.DenseBitVector;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.SparseBitVector;
import org.knime.core.data.vector.bitvector.SparseBitVectorCell;
import org.knime.core.data.vector.bitvector.SparseBitVectorCellFactory;
import org.knime.core.data.vector.bytevector.DenseByteVector;
import org.knime.core.data.vector.bytevector.DenseByteVectorCell;
import org.knime.core.data.vector.bytevector.SparseByteVector;
import org.knime.core.data.vector.bytevector.SparseByteVectorCell;
import org.knime.core.data.vector.bytevector.SparseByteVectorCellFactory;

import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractSingleFingerprintSingleOutputNodeModel;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public class FingerprintDenseToSparseNodeModel
		extends AbstractSingleFingerprintSingleOutputNodeModel {

	public FingerprintDenseToSparseNodeModel() {
		super(false, true, false,
				false/* TODO: Re-enable when 0-length SBV bug is fixed! */, null);
	}

	@Override
	protected String getResultColumnName() {
		return m_firstColName.getStringValue() + " (Sparse)";
	}

	@Override
	protected DataCell getByteVectorResultCell(DataCell fp1) {
		if (m_fpType == SparseByteVectorCell.TYPE) {
			// Return a Sparse Input unchanged
			return fp1;
		}
		DenseByteVector dbv = ((DenseByteVectorCell) fp1).getByteVectorCopy();
		SparseByteVector sbv = new SparseByteVector(dbv.length());
		int lastSetBit = dbv.nextCountIndex(0);
		while (lastSetBit >= 0) {
			int count = dbv.get(lastSetBit);
			sbv.set(lastSetBit, count);
			lastSetBit = dbv.nextCountIndex(lastSetBit + 1);
		}
		final SparseByteVectorCell createDataCell =
				new SparseByteVectorCellFactory(sbv).createDataCell();
		return createDataCell;
	}

	@Override
	protected DataCell getBitVectorResultCell(DataCell fp1) {
		if (m_fpType == SparseBitVectorCell.TYPE) {
			// Return a Sparse Input unchanged
			return fp1;
		}

		DenseBitVector dbv = ((DenseBitVectorCell) fp1).getBitVectorCopy();
		SparseBitVector sbv = new SparseBitVector(dbv.length());
		long lastSetBit = dbv.nextSetBit(0);
		while (lastSetBit >= 0) {
			sbv.set(lastSetBit);
			lastSetBit = dbv.nextSetBit(lastSetBit + 1);
		}
		return new SparseBitVectorCellFactory(sbv).createDataCell();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.fingerprint.all.nodes.abstrct.
	 * AbstractSingleFingerprintNodeModel#getResultColumnType()
	 */
	@Override
	protected DataType getResultColumnType() {
		return m_fpType == DenseBitVectorCell.TYPE ? SparseBitVectorCell.TYPE
				: SparseByteVectorCell.TYPE;
	}

}
