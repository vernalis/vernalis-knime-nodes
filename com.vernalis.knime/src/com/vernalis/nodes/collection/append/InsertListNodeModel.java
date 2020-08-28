/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.collection.append;

import static com.vernalis.nodes.collection.append.InsertListNodeDialog.createInsertionIndexModel;
import static com.vernalis.nodes.collection.append.InsertListNodeDialog.createOutOfBoundsBehaviourModel;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;

/**
 * Node Model implementation for the Insert to List node
 * 
 * @author Steve <knime@vernalis.com>
 * @since 1.27.0
 *
 */
public class InsertListNodeModel extends AppendCollectionNodeModel {

	private final SettingsModelInteger insertIndexMdl = registerSettingsModel(createInsertionIndexModel());

	private final SettingsModelBoolean indexOutOfBoundsFailMdl = registerSettingsModel(
			createOutOfBoundsBehaviourModel());

	public InsertListNodeModel() {
		super(false);

	}

	@Override
	protected void doAdd(Collection<DataCell> cells, DataCell[] toAdd) {
		if (!(cells instanceof List<?>)) {
			// We should never be able to get here!
			throw new RuntimeException("Cells have been parsed as set");
		}
		final List<DataCell> c = (List<DataCell>) cells;
		int insertIndex = insertIndexMdl.getIntValue();
		if (insertIndex < 0) {
			insertIndex += cells.size();
		}

		if (!indexOutOfBoundsFailMdl.getBooleanValue()) {
			insertIndex = Math.max(0, insertIndex);
			insertIndex = Math.min(cells.size(), insertIndex);
		}
		c.addAll(insertIndex, Arrays.asList(toAdd));
	}

}
