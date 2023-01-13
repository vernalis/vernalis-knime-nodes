/*******************************************************************************
 * Copyright (c) 2019,2023, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.collection.missing;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionDataValue;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.nodes.collection.abstrct.AbstractMultiCollectionNodeModel;

/**
 * Node Model implementation for the Empty collection to missing cell node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class EmptyToMissingCollectionNodeModel
		extends AbstractMultiCollectionNodeModel {

	/**
	 * Constructor
	 *
	 * @since 1.36.2
	 */
	protected EmptyToMissingCollectionNodeModel() {
		super(true);
	}

	@Override
	protected DataColumnSpec[] createNewColumnSpecs(DataTableSpec spec,
			int[] idx) {
		// Now generate the output spec
		String colNameSuffix =
				isReplaceInputCols() ? "" : " (Empty -> Missing)";
		final DataColumnSpec[] newColSpecs = new DataColumnSpec[idx.length];
		for (int i = 0; i < newColSpecs.length; i++) {
			String newColName =
					spec.getColumnSpec(idx[i]).getName() + colNameSuffix;
			if (!isReplaceInputCols()) {
				newColName =
						DataTableSpec.getUniqueColumnName(spec, newColName);
			}
			newColSpecs[i] = new DataColumnSpecCreator(newColName,
					spec.getColumnSpec(idx[i]).getType()).createSpec();
		}
		return newColSpecs;
	}

	@Override
	protected DataCell[] getCells(int[] idx, DataRow row,
			DataColumnSpec[] newColSpecs) throws RuntimeException {
		DataCell[] retVal = ArrayUtils.fill(new DataCell[newColSpecs.length],
				DataType.getMissingCell());
		int cellIndex = 0;
		for (int col : idx) {
			DataCell colCell = row.getCell(col);
			if (!colCell.isMissing()) {
				retVal[cellIndex] = ((CollectionDataValue) colCell).size() == 0
						? DataType.getMissingCell()
						: colCell;
			}
			cellIndex++;
		}
		return retVal;
	}

}
