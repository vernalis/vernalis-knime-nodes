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
package com.vernalis.knime.fingerprint.nodes.logic.concatenate;

import org.knime.core.data.DataCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCellFactory;
import org.knime.core.data.vector.bitvector.SparseBitVectorCell;
import org.knime.core.data.vector.bitvector.SparseBitVectorCellFactory;
import org.knime.core.data.vector.bytevector.DenseByteVectorCell;
import org.knime.core.data.vector.bytevector.DenseByteVectorCellFactory;
import org.knime.core.data.vector.bytevector.SparseByteVectorCell;
import org.knime.core.data.vector.bytevector.SparseByteVectorCellFactory;

import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractDoubleFingerprintSingleOutNodeModel;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public class FingerprintConcatNodeModel extends AbstractDoubleFingerprintSingleOutNodeModel {

	public FingerprintConcatNodeModel() {
		super(true, true, true, true, null);
	}

	@Override
	protected String getResultColumnName() {
		return "(" + m_firstColName.getStringValue() + ") + (" + m_secondColName.getStringValue()
				+ ")";
	}

	@Override
	protected DataCell getByteVectorResultCell(DataCell fp1, DataCell fp2)
			throws UnsupportedOperationException {
		if (m_fpType == SparseByteVectorCell.TYPE) {
			// Sparse
			return new SparseByteVectorCellFactory(
					(((SparseByteVectorCell) fp1).getByteVectorCopy())
							.concatenate(((SparseByteVectorCell) fp2).getByteVectorCopy()))
									.createDataCell();
		}
		// Dense
		return new DenseByteVectorCellFactory((((DenseByteVectorCell) fp1).getByteVectorCopy())
				.concatenate(((DenseByteVectorCell) fp2).getByteVectorCopy())).createDataCell();
	}

	@Override
	protected DataCell getBitVectorResultCell(DataCell fp1, DataCell fp2)
			throws UnsupportedOperationException {
		if (m_fpType == SparseBitVectorCell.TYPE) {
			// Sparse
			return new SparseBitVectorCellFactory((((SparseBitVectorCell) fp1).getBitVectorCopy())
					.concatenate(((SparseBitVectorCell) fp2).getBitVectorCopy())).createDataCell();
		}
		// Dense
		return new DenseBitVectorCellFactory((((DenseBitVectorCell) fp1).getBitVectorCopy())
				.concatenate(((DenseBitVectorCell) fp2).getBitVectorCopy())).createDataCell();
	}

}
