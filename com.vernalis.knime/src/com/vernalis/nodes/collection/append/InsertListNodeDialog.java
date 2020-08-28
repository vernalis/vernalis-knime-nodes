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

import org.knime.core.data.collection.ListDataValue;
import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.DataValueColumnFilter;

/**
 * Node Dialog implementation for the Insert to List node
 * 
 * @author Steve <knime@vernalis.com>
 * @since 1.27.0
 */
public class InsertListNodeDialog extends AppendCollectionNodeDialog {

	private static final String FAIL_EXECUTION_IF_INDEX_IS_OUT_OF_BOUNDS = "Fail execution if index is out of bounds";
	private static final String INSERTION_INDEX = "Insertion Index";
	@SuppressWarnings("unchecked")
	private static final ColumnFilter LIST_FILTER = new DataValueColumnFilter(ListDataValue.class);
	public InsertListNodeDialog() {
		// Dont allow sorting - why would you sort after inserting in set position?
		super(false);

	}

	@Override
	protected ColumnFilter getCollectionFilter() {
		return LIST_FILTER;
	}

	@Override
	protected void doAfterRemoveAppendedColumnsOption() {
		final SettingsModelInteger insertionIndexMdl = createInsertionIndexModel();
		final FlowVariableModel fvm = createFlowVariableModel(insertionIndexMdl);
		addDialogComponent(new DialogComponentNumber(insertionIndexMdl, INSERTION_INDEX, 1, 5, fvm));
		addDialogComponent(new DialogComponentBoolean(createOutOfBoundsBehaviourModel(),
				FAIL_EXECUTION_IF_INDEX_IS_OUT_OF_BOUNDS));
	}

	static SettingsModelBoolean createOutOfBoundsBehaviourModel() {
		return new SettingsModelBoolean(FAIL_EXECUTION_IF_INDEX_IS_OUT_OF_BOUNDS, true);
	}

	static SettingsModelInteger createInsertionIndexModel() {
		return new SettingsModelInteger(INSERTION_INDEX, 0);
	}

}
