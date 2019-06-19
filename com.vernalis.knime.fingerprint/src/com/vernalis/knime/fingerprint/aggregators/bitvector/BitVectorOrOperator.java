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
package com.vernalis.knime.fingerprint.aggregators.bitvector;

import org.knime.base.data.aggregation.AggregationOperator;
import org.knime.base.data.aggregation.GlobalSettings;
import org.knime.base.data.aggregation.OperatorColumnSettings;
import org.knime.base.data.aggregation.OperatorData;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.data.vector.bitvector.DenseBitVector;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCellFactory;
import org.knime.core.data.vector.bitvector.SparseBitVector;
import org.knime.core.data.vector.bitvector.SparseBitVectorCell;
import org.knime.core.data.vector.bitvector.SparseBitVectorCellFactory;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public class BitVectorOrOperator extends AggregationOperator {
	SparseBitVector sbv = null;
	DenseBitVector dbv = null;
	DataType colType;

	public BitVectorOrOperator() {
		this(new OperatorData("Bitvector OR", "Bitvector OR", "OR", false, true,
				BitVectorValue.class, false), GlobalSettings.DEFAULT,
				OperatorColumnSettings.DEFAULT_EXCL_MISSING);
	}

	public BitVectorOrOperator(GlobalSettings globalSettings,
			OperatorColumnSettings opColSettings) {
		this(new OperatorData("Bitvector OR", "Bitvector OR", "OR", false, true,
				BitVectorValue.class, false), globalSettings, opColSettings);
	}

	public BitVectorOrOperator(OperatorData operatorData, GlobalSettings globalSettings,
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
		return "Combines Bitvector fingerprints in group using a logic OR operation";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AggregationOperator createInstance(GlobalSettings globalSettings,
			OperatorColumnSettings opColSettings) {
		return new BitVectorOrOperator(getOperatorData(), globalSettings, opColSettings);
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
		if (colType == DenseBitVectorCell.TYPE) {
			DenseBitVector dbv1 = ((DenseBitVectorCell) cell).getBitVectorCopy();
			if (dbv == null) {
				dbv = new DenseBitVector(dbv1);
			} else {
				dbv = dbv.or(dbv1);
			}
		} else {
			SparseBitVector sbv1 = ((SparseBitVectorCell) cell).getBitVectorCopy();
			if (sbv == null) {
				sbv = new SparseBitVector(sbv1);
			} else {
				sbv = sbv.or(sbv1);
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
		if (colType == DenseBitVectorCell.TYPE) {
			if (dbv == null) {
				return DataType.getMissingCell();
			}
			return (new DenseBitVectorCellFactory(dbv)).createDataCell();
		} else {
			if (sbv == null) {
				return DataType.getMissingCell();
			}
			return (new SparseBitVectorCellFactory(sbv)).createDataCell();
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
