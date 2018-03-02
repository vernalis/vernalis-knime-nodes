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
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.data.vector.bytevector.ByteVectorValue;

/**
 * An abstract streamable node model implementation for nodes generating a
 * single output column from a single input. Implementations should override at
 * least one of {@link #getBitVectorResultCell(DataCell)} or
 * {@link #getByteVectorResultCell(DataCell)}, and possibly
 * {@link #getResultColumnType()}
 * 
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public abstract class AbstractSingleFingerprintSingleOutputNodeModel
		extends AbstractSingleFingerprintNodeModel {

	/**
	 * Overloaded constructor for a node accepting any input fingerprint type
	 * 
	 * @param resultType
	 *            The output column type, or <code>null</code> if it is the same
	 *            as the selected input column type
	 */
	public AbstractSingleFingerprintSingleOutputNodeModel(DataType resultType) {
		this(true, true, true, true, resultType);
	}

	/**
	 * Fully featured constructor
	 * 
	 * @param allowSparseBitVector
	 *            Does the node accept Sparse Bit Vector columns
	 * @param allowDenseBitVector
	 *            Does the node accept Dense Bit Vector columns
	 * @param allowSparseByteVector
	 *            Does the node accept Sparse Byte Vector columns
	 * @param allowDenseByteVector
	 *            Does the node accept Dense Byte Vector columns
	 * @param resultType
	 *            The output column type, or <code>null</code> if it is the same
	 *            as the selected input column type
	 */
	public AbstractSingleFingerprintSingleOutputNodeModel(boolean allowSparseBitVector,
			boolean allowDenseBitVector, boolean allowSparseByteVector,
			boolean allowDenseByteVector, DataType resultType) {
		super(allowSparseBitVector, allowDenseBitVector, allowSparseByteVector,
				allowDenseByteVector, resultType == null ? null : new DataType[] { resultType });

	}

	/**
	 * Return the new column name. The abstract superclass will handle ensuring
	 * uniqueness
	 */
	abstract protected String getResultColumnName();

	/**
	 * Return the new column {@link DataType}. If the resultType supplied to the
	 * constructor was null, then the output will be the same as the input type.
	 * Nodes with variable types depending on other factors should override this
	 * method
	 */
	protected DataType getResultColumnType() {
		return resultTypes == null ? m_fpType : resultTypes[0] == null ? m_fpType : resultTypes[0];
	}

	/**
	 * Return the result cell for BitVectors - need to check for sparse or dense
	 * byte vector and handle accordingly. {@link #m_fpType} contains the
	 * {@link DataType} of the selected columns
	 * 
	 * @param fp1
	 *            The cell containing the first fingerprint. Guaranteed to be
	 *            {@link ByteVectorValue}, non-<code>null</code> and not
	 *            {@link DataType#getMissingCell()}
	 * @return The result cell
	 */
	protected DataCell getByteVectorResultCell(DataCell fp1) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Byte Vector results not implemented");
	}

	/**
	 * Return the result cell for BitVectors - need to check for sparse or dense
	 * byte vector and handle accordingly. {@link #m_fpType} contains the
	 * {@link DataType} of the selected columns
	 * 
	 * @param fp1
	 *            The cell containing the first fingerprint. Guaranteed to be
	 *            {@link BitVectorValue}, non-<code>null</code> and not
	 *            {@link DataType#getMissingCell()}
	 * @return The result cell
	 */
	protected DataCell getBitVectorResultCell(DataCell fp1) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Bit Vector results not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.fingerprint.all.nodes.abstrct.
	 * AbstractSingleFingerprintNodeModel#getResultColumnNames()
	 */
	@Override
	protected String[] getResultColumnNames() {
		return new String[] { getResultColumnName() };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.fingerprint.all.nodes.abstrct.
	 * AbstractSingleFingerprintNodeModel#getResultColumnTypes()
	 */
	@Override
	protected final DataType[] getResultColumnTypes() {
		return new DataType[] { getResultColumnType() };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.fingerprint.all.nodes.abstrct.
	 * AbstractSingleFingerprintNodeModel#getByteVectorResultCells(org.knime.
	 * core.data.DataCell, org.knime.core.data.DataCell[])
	 */
	@Override
	protected final void getByteVectorResultCells(DataCell fp1, DataCell[] result)
			throws UnsupportedOperationException {
		result[0] = getByteVectorResultCell(fp1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.fingerprint.all.nodes.abstrct.
	 * AbstractSingleFingerprintNodeModel#getBitVectorResultCells(org.knime.core
	 * .data.DataCell, org.knime.core.data.DataCell[])
	 */
	@Override
	protected final void getBitVectorResultCells(DataCell fp1, DataCell[] result)
			throws UnsupportedOperationException {
		result[0] = getBitVectorResultCell(fp1);
	}

}
