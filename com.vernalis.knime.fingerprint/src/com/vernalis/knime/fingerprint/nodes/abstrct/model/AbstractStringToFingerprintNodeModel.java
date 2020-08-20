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
package com.vernalis.knime.fingerprint.nodes.abstrct.model;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.StringCell;

import com.vernalis.knime.fingerprint.abstrct.FingerprintStringTypes;

/**
 * Abstract streamable node model implementation for nodes converting string
 * cells to fingerprints
 * 
 * @author s.roughley
 *
 */
public class AbstractStringToFingerprintNodeModel extends AbstractToFingerprintNodeModel {

	/** The String Type */
	protected FingerprintStringTypes m_stringType;

	/**
	 * Constructor which assumes the node has an fp-type (i.e. dense/sparse)
	 * setting
	 * 
	 * @param stringType
	 *            The stringtype to use for the conversion
	 */
	public AbstractStringToFingerprintNodeModel(FingerprintStringTypes stringType) {
		this(stringType, true);
	}

	/**
	 * Constructor
	 * 
	 * @param stringType
	 *            The stringtype to use for the conversion
	 * @param hasFpType
	 *            Does the node have a sparse/dense option?
	 */
	public AbstractStringToFingerprintNodeModel(FingerprintStringTypes stringType,
			boolean hasFpType) {
		super(new DataType[] { StringCell.TYPE }, hasFpType);
		m_stringType = stringType;
	}

	/**
	 * Default implementation for converting the String representation to the
	 * DataCell
	 * 
	 * @param fpString
	 *            The string from the cell, which has already been checked for
	 *            truncation and validated, and is non-null;
	 * @return
	 */
	protected DataCell getResultCell(String fpString) {
		return m_fpType.getDataCellFromSetBits(m_stringType.getLengthFromString(fpString),
				m_stringType.setBitsFromString(fpString));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.fingerprint.bit.nodes.abstrct.
	 * AbstractToFingerprintNodeModel#getFingerprintCell(org.knime.core.data.
	 * DataCell)
	 */
	@Override
	protected DataCell getFingerprintCell(DataCell inCell) {

		String fpString = ((StringValue) inCell).getStringValue();
		if (m_stringType.isTruncated(fpString)) {
			// Truncated string representation
			m_logger.warn("String representation is incomplete due to truncation"
					+ " - returning missing cell (" + fpString + ")");
			return DataType.getMissingCell();
		}
		if (m_stringType.validateString(fpString)) {
			return getResultCell(fpString);
		} else {
			// Error converting the string
			m_logger.warn("Error converting string '" + fpString
					+ "' to fingerprint: String does not match correct "
					+ "pattern for required celltype - returning missing cell");
			return DataType.getMissingCell();
		}
	}

	@Override
	protected String getResultColumnName() {
		return m_ColName.getStringValue() + " (" + m_fpType.getToolTip() + "_from "
				+ m_stringType.getActionCommand() + ")";
	}

}
