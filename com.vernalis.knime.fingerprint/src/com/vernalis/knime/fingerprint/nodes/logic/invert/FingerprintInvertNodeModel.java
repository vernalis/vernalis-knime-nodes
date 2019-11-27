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
package com.vernalis.knime.fingerprint.nodes.logic.invert;

import org.knime.core.data.DataCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCellFactory;
import org.knime.core.data.vector.bitvector.SparseBitVectorCell;

import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractSingleFingerprintSingleOutputNodeModel;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public class FingerprintInvertNodeModel extends AbstractSingleFingerprintSingleOutputNodeModel {

	public FingerprintInvertNodeModel() {
		super(false, true, false, false, null);
	}

	@Override
	protected DataCell getBitVectorResultCell(DataCell fp1) {
		// Dense only for invert()
		if (fp1 instanceof SparseBitVectorCell) {
			throw new UnsupportedOperationException(
					"Only Dense Bit Vectors can be inverted - use Sparse-To-Dense node first!");
		}
		return new DenseBitVectorCellFactory(
				(((DenseBitVectorCell) fp1).getBitVectorCopy()).invert()).createDataCell();
	}

	@Override
	protected String getResultColumnName() {
		return "~ (" + m_firstColName.getStringValue() + ")";
	}

}
