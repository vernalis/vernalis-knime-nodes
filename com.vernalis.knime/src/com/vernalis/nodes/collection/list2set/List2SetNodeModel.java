/*******************************************************************************
 * Copyright (c) 2018,2023, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.collection.list2set;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListDataValue;
import org.knime.core.data.collection.SetCell;
import org.knime.core.data.collection.SparseListDataValue;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.nodes.collection.abstrct.AbstractMultiCollectionNodeModel;

import static com.vernalis.nodes.collection.list2set.List2SetNodeDialog.createSortedModel;

/**
 * Node Model for the List to Set node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class List2SetNodeModel extends AbstractMultiCollectionNodeModel {

	private final SettingsModelBoolean sortedMdl =
			registerSettingsModel(createSortedModel());

	/**
	 * Constructor
	 *
	 * @since 1.36.2
	 */
	protected List2SetNodeModel() {
		super("List Columns", false, true, false);
	}

	@Override
	protected DataColumnSpec[] createNewColumnSpecs(DataTableSpec spec,
			int[] idx) {
		return Arrays.stream(idx).mapToObj(spec::getColumnSpec)
				.map(cSpec -> new DataColumnSpecCreator(cSpec.getName(),
						SetCell.getCollectionType(
								cSpec.getType().getCollectionElementType()))
										.createSpec())
				.toArray(DataColumnSpec[]::new);
	}

	@Override
	protected DataCell[] getCells(int[] idx, DataRow row,
			DataColumnSpec[] newColSpecs) throws RuntimeException {
		DataCell[] retVal = ArrayUtils.fill(new DataCell[newColSpecs.length],
				DataType.getMissingCell());
		int colIdx = 0;
		for (int i : idx) {
			DataCell collCell = row.getCell(i);
			if (collCell.isMissing()) {
				colIdx++;
				continue;
			}
			ListDataValue ldv = (ListDataValue) collCell;
			Set<DataCell> cells = sortedMdl.getBooleanValue()
					? new TreeSet<>(ldv.getElementType().getComparator())
					: new LinkedHashSet<>();
			if (ldv instanceof SparseListDataValue) {
				// Short-cut...
				SparseListDataValue sldv = (SparseListDataValue) ldv;
				if (sldv.getAllIndices().length < sldv.size()) {
					// Only add the default if it appears in the collection
					cells.add(sldv.getDefaultElement());
				}
				for (int j : sldv.getAllIndices()) {
					cells.add(sldv.get(j));
				}
			} else {
				ldv.forEach(cells::add);
			}
			retVal[colIdx++] = CollectionCellFactory.createSetCell(cells);
		}
		return retVal;
	}

}
