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
package com.vernalis.knime.fingerprint.nodes.props.isEmpty;

import org.knime.core.data.DataCell;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.BooleanCell.BooleanCellFactory;
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.data.vector.bytevector.ByteVectorValue;

import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractSingleFingerprintSingleOutputNodeModel;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public class FingerprintEmptyNodeModel extends AbstractSingleFingerprintSingleOutputNodeModel {

	/**
	 * Constructor for the NodeModel
	 */
	public FingerprintEmptyNodeModel() {
		super(BooleanCell.TYPE);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getResultColumnName() {
		return "Is Empty? (" + m_firstColName.getStringValue() + ")";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataCell getByteVectorResultCell(DataCell fp1) {
		return BooleanCellFactory.create(((ByteVectorValue) fp1).isEmpty());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataCell getBitVectorResultCell(DataCell fp1) {
		return BooleanCellFactory.create(((BitVectorValue) fp1).isEmpty());
	}

}
