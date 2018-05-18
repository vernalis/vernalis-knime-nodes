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
package com.vernalis.knime.fingerprint.nodes.logic.xor;

import org.knime.core.data.DataCell;
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCellFactory;
import org.knime.core.data.vector.bitvector.SparseBitVectorCellFactory;

import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractDoubleFingerprintSingleOutNodeModel;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public class FingerprintXorNodeModel extends AbstractDoubleFingerprintSingleOutNodeModel {

	public FingerprintXorNodeModel() {
		super(true, true, false, false, null);
	}

	@Override
	protected String getResultColumnName() {
		return "(" + m_firstColName.getStringValue() + ") XOR (" + m_secondColName.getStringValue()
				+ ")";
	}

	@Override
	protected DataCell getBitVectorResultCell(DataCell fp1, DataCell fp2)
			throws UnsupportedOperationException {
		if (m_fpType == DenseBitVectorCell.TYPE) {
			return DenseBitVectorCellFactory.xor((BitVectorValue) fp1, (BitVectorValue) fp2);
		}
		return SparseBitVectorCellFactory.xor((BitVectorValue) fp1, (BitVectorValue) fp2);
	}
}
