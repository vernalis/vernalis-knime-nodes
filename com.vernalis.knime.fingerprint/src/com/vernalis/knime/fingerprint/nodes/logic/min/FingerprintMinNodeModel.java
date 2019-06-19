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
package com.vernalis.knime.fingerprint.nodes.logic.min;

import org.knime.core.data.DataCell;
import org.knime.core.data.vector.bytevector.ByteVectorValue;
import org.knime.core.data.vector.bytevector.DenseByteVectorCell;
import org.knime.core.data.vector.bytevector.DenseByteVectorCellFactory;
import org.knime.core.data.vector.bytevector.SparseByteVectorCellFactory;

import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractDoubleFingerprintSingleOutNodeModel;

public class FingerprintMinNodeModel extends AbstractDoubleFingerprintSingleOutNodeModel {

	public FingerprintMinNodeModel() {
		super(false, false, true, true, null);
	}

	@Override
	protected String getResultColumnName() {
		return "MIN (" + m_firstColName.getStringValue() + ", " + m_secondColName.getStringValue()
				+ ")";
	}

	@Override
	protected DataCell getByteVectorResultCell(DataCell fp1, DataCell fp2)
			throws UnsupportedOperationException {
		ByteVectorValue bvv1 = (ByteVectorValue) fp1;
		ByteVectorValue bvv2 = (ByteVectorValue) fp2;
		if (m_fpType == DenseByteVectorCell.TYPE) {
			return DenseByteVectorCellFactory.min(bvv1, bvv2);
		} else {
			return SparseByteVectorCellFactory.min(bvv1, bvv2);
		}
	}

}
