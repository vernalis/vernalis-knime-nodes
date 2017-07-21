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
package com.vernalis.knime.fingerprint.nodes.convert.fromhex;

import org.knime.core.data.DataCell;
import org.knime.core.data.vector.bitvector.DenseBitVector;
import org.knime.core.data.vector.bitvector.DenseBitVectorCellFactory;
import org.knime.core.data.vector.bitvector.SparseBitVector;
import org.knime.core.data.vector.bitvector.SparseBitVectorCell;
import org.knime.core.data.vector.bitvector.SparseBitVectorCellFactory;

import com.vernalis.knime.fingerprint.abstrct.FingerprintStringTypes;
import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractStringToFingerprintNodeModel;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public class HexStringToFingerprintNodeModel extends AbstractStringToFingerprintNodeModel {

	public HexStringToFingerprintNodeModel() {
		super(FingerprintStringTypes.HEX);

	}

	@Override
	protected DataCell getResultCell(String fpString) {
		DataCell retVal;
		if (m_fpType.getDataType(isOutputCountFp) == SparseBitVectorCell.TYPE) {
			// Sparse
			retVal = (new SparseBitVectorCellFactory(new SparseBitVector(fpString)))
					.createDataCell();
		} else {
			// Dense
			retVal = (new DenseBitVectorCellFactory(new DenseBitVector(fpString))).createDataCell();
		}
		return retVal;
	}

}
