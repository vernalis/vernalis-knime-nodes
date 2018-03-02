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
package com.vernalis.knime.fingerprint.aggregators.bytevector;

import org.knime.base.data.aggregation.AggregationOperator;
import org.knime.base.data.aggregation.GlobalSettings;
import org.knime.base.data.aggregation.OperatorColumnSettings;
import org.knime.base.data.aggregation.OperatorData;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.vector.bytevector.ByteVectorValue;
import org.knime.core.data.vector.bytevector.DenseByteVector;
import org.knime.core.data.vector.bytevector.DenseByteVectorCell;
import org.knime.core.data.vector.bytevector.DenseByteVectorCellFactory;
import org.knime.core.data.vector.bytevector.SparseByteVector;
import org.knime.core.data.vector.bytevector.SparseByteVectorCell;
import org.knime.core.data.vector.bytevector.SparseByteVectorCellFactory;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public class ByteVectorConcatOperator extends AggregationOperator {
	SparseByteVector sbv = null;
	DenseByteVector dbv = null;
	DataType colType;

	public ByteVectorConcatOperator() {
		this(new OperatorData("ByteVector Concatenate", "ByteVector Concatenate", "Concatenate",
				false, true, ByteVectorValue.class, false), GlobalSettings.DEFAULT,
				OperatorColumnSettings.DEFAULT_EXCL_MISSING);
	}

	public ByteVectorConcatOperator(GlobalSettings globalSettings,
			OperatorColumnSettings opColSettings) {
		this(new OperatorData("ByteVector Concatenate", "ByteVector Concatenate", "Concatenate",
				false, true, ByteVectorValue.class, false), globalSettings, opColSettings);
	}

	public ByteVectorConcatOperator(OperatorData operatorData, GlobalSettings globalSettings,
			OperatorColumnSettings opColSettings) {
		super(operatorData, globalSettings, opColSettings);
		if (opColSettings.getOriginalColSpec() == null) {
			colType = null;
		} else {
			colType = opColSettings.getOriginalColSpec().getType();
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return "Concatenates ByteVector Fingerprints";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AggregationOperator createInstance(GlobalSettings globalSettings,
			OperatorColumnSettings opColSettings) {
		return new ByteVectorConcatOperator(getOperatorData(), globalSettings, opColSettings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean computeInternal(DataCell cell) {
		if (cell.isMissing()) {
			return false;
		}
		if (colType == null) {
			colType = cell.getType();
		}
		if (colType == DenseByteVectorCell.TYPE) {
			DenseByteVector dbv1 = ((DenseByteVectorCell) cell).getByteVectorCopy();
			if (dbv == null) {
				dbv = dbv1;
			} else {
				dbv = dbv.concatenate(dbv1);
			}
		} else {
			SparseByteVector sbv1 = ((SparseByteVectorCell) cell).getByteVectorCopy();
			if (sbv == null) {
				sbv = sbv1;
			} else {
				sbv = sbv.concatenate(sbv1);
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataType getDataType(DataType origType) {
		return origType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataCell getResultInternal() {
		if (colType == DenseByteVectorCell.TYPE) {
			if (dbv == null) {
				return DataType.getMissingCell();
			}
			return (new DenseByteVectorCellFactory(dbv)).createDataCell();
		} else {
			if (sbv == null) {
				return DataType.getMissingCell();
			}
			return (new SparseByteVectorCellFactory(sbv)).createDataCell();
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void resetInternal() {
		sbv = null;
		dbv = null;

	}

}
