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
package com.vernalis.knime.fingerprint.nodes.logic.intersects;

import org.knime.core.data.DataCell;
import org.knime.core.data.def.BooleanCell.BooleanCellFactory;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.SparseBitVectorCell;

import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractDoubleFingerprintSingleOutNodeModel;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public class FingerprintIntersectsNodeModel extends AbstractDoubleFingerprintSingleOutNodeModel {

	public FingerprintIntersectsNodeModel() {
		super(true, true, false, false, BooleanCellFactory.TYPE);
	}

	/**
	 * Return the new column name. The abstract superclass will handle ensuring
	 * uniqueness
	 */
	@Override
	protected String getResultColumnName() {
		return "(" + m_firstColName.getStringValue() + ") \u2229 ("
				+ m_secondColName.getStringValue() + ")";
	}

	@Override
	protected DataCell getBitVectorResultCell(DataCell fp1, DataCell fp2)
			throws UnsupportedOperationException {
		boolean intersects;
		if (m_fpType == SparseBitVectorCell.TYPE) {
			// Sparse
			intersects = (((SparseBitVectorCell) fp1).getBitVectorCopy())
					.intersects(((SparseBitVectorCell) fp2).getBitVectorCopy());
		} else {
			// Dense
			intersects = (((DenseBitVectorCell) fp1).getBitVectorCopy())
					.intersects(((DenseBitVectorCell) fp2).getBitVectorCopy());
		}
		return BooleanCellFactory.create(intersects);
	}

}
