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
package com.vernalis.knime.fingerprint.nodes.props.type;

import org.knime.core.data.DataCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bytevector.DenseByteVectorCell;

import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractSingleFingerprintSingleOutputNodeModel;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public class FingerprintTypeNodeModel extends AbstractSingleFingerprintSingleOutputNodeModel {
	/**
	 * Constructor for the NodeModel
	 */
	public FingerprintTypeNodeModel() {
		super(StringCell.TYPE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getResultColumnName() {
		return "Type (" + m_firstColName.getStringValue() + ")";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataCell getByteVectorResultCell(DataCell fp1) {
		return new StringCell(((fp1 instanceof DenseByteVectorCell) ? "Dense " : "Sparse ")
				+ fp1.getType().toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataCell getBitVectorResultCell(DataCell fp1) {
		return new StringCell(((fp1 instanceof DenseBitVectorCell) ? "Dense " : "Sparse ")
				+ fp1.getType().toString());
	}

}
