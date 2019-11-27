/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.knime.jfcplot.core.nodes;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.DataValueColumnFilter;
import org.knime.ext.jfc.node.base.JfcBaseNodeDialogPane;

/**
 * Node Dialog base class for the Plot nodes. This class creates column
 * selectors as defined by the constructor arguments
 * 
 * @author s.roughley knime@vernalis.com
 */
public class AbstractDrawableSeriesPlotNodeDialogPane
		extends JfcBaseNodeDialogPane {

	/**
	 * A column filter for DoubleValue columns
	 */
	@SuppressWarnings("unchecked")
	public static final DataValueColumnFilter DOUBLE_VALUE_COLUMN_FILTER =
			new DataValueColumnFilter(DoubleValue.class);

	/**
	 * A column filter for StringValue columns
	 */
	@SuppressWarnings("unchecked")
	public static final DataValueColumnFilter STRING_VALUE_COLUMN_FILTER =
			new DataValueColumnFilter(StringValue.class);

	/**
	 * Overload constructor with no grouping box around the column selectors
	 * 
	 * @param tabLabel
	 *            The label for the new tab, which will be the first tab in the
	 *            dialog
	 * @param columnLabels
	 *            The labels in the node dialog pane for the column labels
	 * @param classFilters
	 *            The {@link ColumnFilter}s for the column selectors
	 * @throws IllegalArgumentException
	 *             is the number of column labels is different from the class
	 *             filters
	 */
	protected AbstractDrawableSeriesPlotNodeDialogPane(String tabLabel,
			String[] columnLabels, ColumnFilter[] classFilters) {
		this(tabLabel, null, columnLabels, classFilters);
	}

	/**
	 * Full constructor
	 * 
	 * @param tabLabel
	 *            The label for the new tab, which will be the first tab in the
	 *            dialog
	 * @param columnFilterGroupLabel
	 *            Optional label for the column selectors group. If
	 *            {@code null}, no grouping box is provided around the column
	 *            selectors
	 * @param columnLabels
	 *            The labels in the node dialog pane for the column labels
	 * @param classFilters
	 *            The {@link ColumnFilter}s for the column selectors
	 * @throws IllegalArgumentException
	 *             is the number of column labels is different from the class
	 *             filters
	 */
	protected AbstractDrawableSeriesPlotNodeDialogPane(String tabLabel,
			String columnFilterGroupLabel, String[] columnLabels,
			ColumnFilter[] classFilters) {
		if (columnLabels == null || classFilters == null
				|| columnLabels.length != classFilters.length) {
			throw new IllegalArgumentException(
					"Must supply non-null column selection details, with same number of names and filters");
		}
		removeTab("Options");
		createNewTabAt(tabLabel, 0);
		setSelected(tabLabel);
		if (columnFilterGroupLabel != null) {
			createNewGroup(columnFilterGroupLabel);
		}
		for (int i = 0; i < columnLabels.length; i++) {
			if (columnLabels[i] == null || classFilters[i] == null) {
				throw new IllegalArgumentException(
						"Must supply non-null column selection details, with same number of names and filters");
			}
			addDialogComponent(new DialogComponentColumnNameSelection(
					createColumnNameModel(columnLabels[i]), columnLabels[i], 0,
					classFilters[i]));
		}
	}

	/** {@inheritDoc} */
	@Override
	public final void addComponents() {
		// This puts the components in the 'Options' tab, which we remove!
	}

	/**
	 * @param columnLabel
	 *            The column label in the node dialog
	 * @return The column name settings model
	 */
	static SettingsModelString createColumnNameModel(String columnLabel) {
		return new SettingsModelString(columnLabel, null);
	}

}
