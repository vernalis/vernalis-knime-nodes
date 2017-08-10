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
package com.vernalis.knime.fingerprint.nodes.convert.tostring;

import org.knime.core.data.DataCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.SparseBitVectorCell;
import org.knime.core.data.vector.bytevector.DenseByteVectorCell;
import org.knime.core.data.vector.bytevector.SparseByteVectorCell;

import com.vernalis.knime.fingerprint.abstrct.FingerprintStringTypes;
import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractSingleFingerprintSingleOutputNodeModel;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public class FingerprintToStringNodeModel extends AbstractSingleFingerprintSingleOutputNodeModel {

	public FingerprintToStringNodeModel() {
		super(true, true, true, true, StringCell.TYPE);
	}

	@Override
	protected String getResultColumnName() {
		return m_firstColName.getStringValue() + " (String)";
	}

	@Override
	protected DataCell getByteVectorResultCell(DataCell fp1) {
		// TODO: Check that the string validation piece works for ByteVectors
		String retVal;
		if (m_fpType == SparseByteVectorCell.TYPE) {
			// Sparse
			retVal = ((SparseByteVectorCell) fp1).toString();
		} else {
			// Dense
			retVal = ((DenseByteVectorCell) fp1).toString();
		}
		if (FingerprintStringTypes.BITVECTOR_STRING.isTruncated(retVal)) {
			m_logger.warn("Fingerprint has too many set bits for complete "
					+ "string representation - truncated to " + (retVal.split(",").length - 1)
					+ " set bits");
			setWarningMessage("Some Fingerprints were truncated");
		}
		return new StringCell(retVal);
	}

	@Override
	protected DataCell getBitVectorResultCell(DataCell fp1) {
		String retVal;
		if (m_fpType == SparseBitVectorCell.TYPE) {
			// Sparse
			retVal = ((SparseBitVectorCell) fp1).toString();
		} else {
			// Dense
			// Bug in DBVC in which #toString() calls DBV#toBinaryString()
			retVal = ((DenseBitVectorCell) fp1).getBitVectorCopy().toString();
		}
		if (FingerprintStringTypes.BITVECTOR_STRING.isTruncated(retVal)) {
			m_logger.warn("Fingerprint has too many set bits for complete "
					+ "string representation - truncated to " + (retVal.split(",").length - 1)
					+ " set bits");
			setWarningMessage("Some Fingerprints were truncated");
		}
		return new StringCell(retVal);

	}

}
