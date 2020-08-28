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
package com.vernalis.knime.fingerprint.nodes.convert.tosetbitslist;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.SetCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.data.vector.bitvector.DenseBitVector;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.SparseBitVectorCell;

import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractSingleFingerprintNodeModel;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public class FingerprintToSetBitsListNodeModel extends AbstractSingleFingerprintNodeModel {

	public FingerprintToSetBitsListNodeModel() {
		super(true, true, false, false,
				new DataType[] { LongCell.TYPE, SetCell.getCollectionType(LongCell.TYPE) });

	}

	protected DataCell getSetBits(DataCell fp1) {
		// Firstly, we need to get an array of set bits

		if (m_fpType == SparseBitVectorCell.TYPE) {
			// Sparse
			return CollectionCellFactory.createSetCell(
					Arrays.stream(((SparseBitVectorCell) fp1).getBitVectorCopy().getAllOneIndices())
							.boxed().map(l -> new LongCell(l))
							.collect(Collectors.toCollection(LinkedHashSet::new)));
		} else {
			// Dense - more difficult
			DenseBitVector dbv = ((DenseBitVectorCell) fp1).getBitVectorCopy();

			Set<LongCell> cells = new LinkedHashSet<>();
			long lastSetBit = dbv.nextSetBit(0);
			while (lastSetBit >= 0) {
				cells.add(new LongCell(lastSetBit++));
				lastSetBit = dbv.nextSetBit(lastSetBit);
			}
			return CollectionCellFactory.createSetCell(cells);
		}

	}

	@Override
	protected String[] getResultColumnNames() {
		return new String[] { m_firstColName.getStringValue() + " (Length)",
				m_firstColName.getStringValue() + " (Set bits)" };
	}

	@Override
	protected void getBitVectorResultCells(DataCell fp1, DataCell[] result)
			throws UnsupportedOperationException {
		result[0] = new LongCell(((BitVectorValue) fp1).length());
		result[1] = getSetBits(fp1);
	}

}
