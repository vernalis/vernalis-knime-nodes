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
package com.vernalis.knime.fingerprint.nodes.convert.tohex;

import org.knime.core.data.DataCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.SparseBitVectorCell;

import com.vernalis.knime.fingerprint.abstrct.FingerprintStringTypes;
import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractSingleFingerprintSingleOutputNodeModel;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public class FingerprintToHexNodeModel extends AbstractSingleFingerprintSingleOutputNodeModel {

	public FingerprintToHexNodeModel() {
		// Only bitvectors
		super(true, true, false, false, StringCell.TYPE);
	}

	@Override
	protected String getResultColumnName() {
		return m_firstColName.getStringValue() + " (Hex)";
	}

	@Override
	protected DataCell getBitVectorResultCell(DataCell fp1) {
		String retVal;
		if (m_fpType == SparseBitVectorCell.TYPE) {
			// Sparse
			retVal = ((SparseBitVectorCell) fp1).toHexString();
		} else {
			// Dense
			retVal = ((DenseBitVectorCell) fp1).toHexString();
		}
		if (FingerprintStringTypes.HEX.isTruncated(retVal)) {
			m_logger.warn(
					"Fingerprint too long for complete hexadecimal representation - truncated to "
							+ ((retVal.length() - 4) * 4) + " bits");
			setWarningMessage("Some Fingerprints were truncated");
		}
		return new StringCell(retVal);
	}

}
