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
package com.vernalis.knime.fingerprint.nodes.logic.fold;

import org.knime.core.data.DataCell;
import org.knime.core.data.vector.bitvector.DenseBitVector;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCellFactory;
import org.knime.core.data.vector.bitvector.SparseBitVector;
import org.knime.core.data.vector.bitvector.SparseBitVectorCell;
import org.knime.core.data.vector.bitvector.SparseBitVectorCellFactory;

import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractSingleFingerprintSingleOutputNodeModel;

public class FoldFingerprintNodeModel extends AbstractSingleFingerprintSingleOutputNodeModel {

	public FoldFingerprintNodeModel() {
		super(true, true, false, false, null);
	}

	@Override
	protected String getResultColumnName() {
		return "FOLD (" + m_firstColName.getStringValue() + ")";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.fingerprint.nodes.abstrct.model.
	 * AbstractDoubleFingerprintSingleOutNodeModel#getBitVectorResultCell(org.
	 * knime.core.data.DataCell, org.knime.core.data.DataCell)
	 */
	@Override
	protected DataCell getBitVectorResultCell(DataCell fp1) throws UnsupportedOperationException {
		if (fp1 instanceof DenseBitVectorCell) {
			DenseBitVector dbv = ((DenseBitVectorCell) fp1).getBitVectorCopy();
			long midPoint;
			if (dbv.length() % 2 == 0) {
				midPoint = dbv.length() / 2;
			} else {
				midPoint = (1 + dbv.length()) / 2;
			}
			DenseBitVector ldbv = dbv.subSequence(0, midPoint);
			DenseBitVector rdbv = dbv.subSequence(midPoint, dbv.length());
			return new DenseBitVectorCellFactory(ldbv.or(rdbv)).createDataCell();
		} else {
			SparseBitVector sbv = ((SparseBitVectorCell) fp1).getBitVectorCopy();
			long midPoint;
			if (sbv.length() % 2 == 0) {
				midPoint = sbv.length() / 2;
			} else {
				midPoint = (1 + sbv.length()) / 2;
			}
			SparseBitVector lsbv = sbv.subSequence(0, midPoint);
			SparseBitVector rsbv = sbv.subSequence(midPoint, sbv.length());
			return new SparseBitVectorCellFactory(lsbv.or(rsbv)).createDataCell();
		}
	}

}
