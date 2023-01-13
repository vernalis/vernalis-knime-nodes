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
package com.vernalis.nodes.collection.set2list;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.collection.SetDataValue;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.nodes.collection.abstrct.AbstractMultiCollectionNodeModel;

import static com.vernalis.nodes.collection.set2list.Set2ListNodeDialog.createSortedModel;

/**
 * Node Model for the Set to List node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class Set2ListNodeModel extends AbstractMultiCollectionNodeModel {

	private final SettingsModelBoolean sortedMdl =
			registerSettingsModel(createSortedModel());

	/**
	 * Constructor
	 *
	 * @since 1.36.2
	 */
	protected Set2ListNodeModel() {
		super("Set Columns", false, false, true);
	}

	@Override
	protected DataColumnSpec[] createNewColumnSpecs(DataTableSpec spec,
			int[] idx) {
		return Arrays.stream(idx).mapToObj(spec::getColumnSpec)
				.map(cSpec -> new DataColumnSpecCreator(cSpec.getName(),
						ListCell.getCollectionType(
								cSpec.getType().getCollectionElementType()))
										.createSpec())
				.toArray(DataColumnSpec[]::new);
	}

	@Override
	protected DataCell[] getCells(int[] idx, DataRow row,
			DataColumnSpec[] newColSpecs) throws RuntimeException {
		DataCell[] retVal = ArrayUtils.fill(new DataCell[idx.length],
				DataType.getMissingCell());
		int colIdx = 0;
		for (int i : idx) {
			DataCell collCell = row.getCell(i);
			if (collCell.isMissing()) {
				colIdx++;
				continue;
			}
			final SetDataValue sdv = (SetDataValue) collCell;
			Stream<DataCell> cellStream = sdv.stream();

			if (sortedMdl.getBooleanValue()) {
				DataValueComparator comp = sdv.getElementType().getComparator();
				cellStream = cellStream.sorted(comp);
			}
			List<DataCell> cells = cellStream.toList();
			retVal[colIdx++] = CollectionCellFactory.createListCell(cells);
		}
		return retVal;
	}

}
