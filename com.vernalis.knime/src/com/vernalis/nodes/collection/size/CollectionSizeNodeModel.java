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
package com.vernalis.nodes.collection.size;

import java.util.Arrays;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.data.def.IntCell;

import com.vernalis.nodes.collection.abstrct.AbstractMultiCollectionNodeModel;

/**
 * NodeModel for the Collection Size node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class CollectionSizeNodeModel extends AbstractMultiCollectionNodeModel {

	/**
	 * Constructor
	 *
	 * @since 1.36.2
	 */
	protected CollectionSizeNodeModel() {
		super(false);
	}

	@Override
	protected DataCell[] getCells(int[] idx, DataRow row,
			DataColumnSpec[] newColSpecs) throws RuntimeException {
		return Arrays.stream(idx).mapToObj(row::getCell)
				.map(cell -> cell.isMissing() ? DataType.getMissingCell()
						: new IntCell(((CollectionDataValue) cell).size()))
				.toArray(DataCell[]::new);
	}

	@Override
	protected DataColumnSpec[] createNewColumnSpecs(DataTableSpec spec,
			int[] idx) {

		return Arrays.stream(idx)
				.mapToObj(i -> new DataColumnSpecCreator(
						DataTableSpec.getUniqueColumnName(spec,
								spec.getColumnSpec(i).getName() + " Size"),
						IntCell.TYPE).createSpec())
				.toArray(DataColumnSpec[]::new);
	}

	@Override
	protected boolean isReplaceInputCols() {
		// We never replace the input columns for this node
		return false;
	}

}
