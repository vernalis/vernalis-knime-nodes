/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.misc.distance;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.collection.ListDataValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.DataValueColumnFilter;

/**
 * Node Dialog for the distance nodes
 * 
 * @author s.roughley
 *
 */
public class AbstractVectorDistanceNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * A {@link ColumnFilter} for a column containing Lists of Numbers
	 */
	final static ColumnFilter LIST_OF_NUMBERS = new ColumnFilter() {

		@Override
		public boolean includeColumn(DataColumnSpec colSpec) {
			return colSpec.getType().isCompatible(ListDataValue.class)
					&& colSpec.getType().getCollectionElementType()
							.isCompatible(DoubleValue.class);
		}

		@Override
		public String allFilteredMsg() {
			return "No Lists of Numbers found";
		}
	};

	/**
	 * Constructor for a non-collection-based implementation. At least one
	 * dimension name must be supplied
	 * 
	 * @param firstDimension
	 *            The name of the first dimension
	 * @param extraDimensions
	 *            The name of any additional dimensions
	 */
	public AbstractVectorDistanceNodeDialog(char firstDimension,
			char... extraDimensions) {
		this(false, firstDimension, extraDimensions);
	}

	/**
	 * Convenience constructor for implementations requiring only a pair of
	 * columns, which may be collection based
	 * 
	 * @param isCollectionBased
	 *            Are the columns collection columns, in which case the node is
	 *            n-dimensional
	 */
	public AbstractVectorDistanceNodeDialog(boolean isCollectionBased) {
		this(isCollectionBased, isCollectionBased ? 'n' : 'x');
	}

	/**
	 * Full constructor, exposing all options, some combinations of which may be
	 * invalid
	 * 
	 * @param isCollectionBased
	 *            Are the columns collection columns, in which case the node is
	 *            n-dimensional, and only the first dimension can be supplied
	 * @param firstDimension
	 *            The name of the first dimension
	 * @param extraDimensions
	 *            The name of any additional dimensions
	 */
	protected AbstractVectorDistanceNodeDialog(boolean isCollectionBased,
			char firstDimension, char... extraDimensions) {
		if (isCollectionBased && extraDimensions.length > 0) {
			throw new IllegalArgumentException(
					"Collection-based implementations should only supply a single dimension name");
		}

		if (extraDimensions.length > 0) {
			createNewGroup("Start Point");
		}
		setHorizontalPlacement(true);

		@SuppressWarnings("unchecked")
		ColumnFilter colFilter = isCollectionBased ? LIST_OF_NUMBERS
				: new DataValueColumnFilter(DoubleValue.class);
		SettingsModelString mdl = AbstractVectorDistanceNodeDialog
				.createStartModel(firstDimension);
		addDialogComponent(new DialogComponentColumnNameSelection(mdl,
				mdl.getKey(), 0, colFilter));

		for (char dim : extraDimensions) {
			mdl = AbstractVectorDistanceNodeDialog.createStartModel(dim);
			addDialogComponent(new DialogComponentColumnNameSelection(mdl,
					mdl.getKey(), 0, colFilter));
		}

		if (extraDimensions.length > 0) {
			setHorizontalPlacement(false);
			createNewGroup("End Point");
			setHorizontalPlacement(true);
		}

		mdl = AbstractVectorDistanceNodeDialog.createEndModel(firstDimension);
		addDialogComponent(new DialogComponentColumnNameSelection(mdl,
				mdl.getKey(), 0, colFilter));

		for (char dim : extraDimensions) {
			mdl = AbstractVectorDistanceNodeDialog.createEndModel(dim);
			addDialogComponent(new DialogComponentColumnNameSelection(mdl,
					mdl.getKey(), 0, colFilter));
		}
		setHorizontalPlacement(false);
		closeCurrentGroup();

	}

	/**
	 * @param dim
	 *            The name of the dimension
	 * @return The Settings model for the dimension for the 'End' column
	 */
	static SettingsModelString createEndModel(char dim) {
		return new SettingsModelString(String.valueOf(dim).toLowerCase() + "1",
				null);
	}

	/**
	 * @param dim
	 *            The name of the dimension
	 * @return The Settings model for the dimension for the 'Start' column
	 */
	static SettingsModelString createStartModel(char dim) {
		return new SettingsModelString(String.valueOf(dim).toLowerCase() + "0",
				null);
	}

}
