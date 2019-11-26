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
package com.vernalis.knime.plot.nodes.box;

import java.awt.Color;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.BooleanValue;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.LongValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColorChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColor;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.DataValueColumnFilter;
import org.knime.ext.jfc.node.barchart.JfcBarChartNodeDialogPane;

import static com.vernalis.knime.plot.nodes.box.MulticolourNotchedBoxAndWhiskerRenderer.DEFAULT_DEFAULT_BOX;
import static com.vernalis.knime.plot.nodes.box.MulticolourNotchedBoxAndWhiskerRenderer.DEFAULT_DEFAULT_FILL;
import static com.vernalis.knime.plot.nodes.box.MulticolourNotchedBoxAndWhiskerRenderer.DEFAULT_FAROUTLIER_RADIUS;
import static com.vernalis.knime.plot.nodes.box.MulticolourNotchedBoxAndWhiskerRenderer.DEFAULT_MEAN_RADIUS;
import static com.vernalis.knime.plot.nodes.box.MulticolourNotchedBoxAndWhiskerRenderer.DEFAULT_NOTCH_SIZE;
import static com.vernalis.knime.plot.nodes.box.MulticolourNotchedBoxAndWhiskerRenderer.DEFAULT_OUTLIER_RADIUS;
import static com.vernalis.knime.plot.nodes.box.MulticolourNotchedBoxAndWhiskerRenderer.DEFAULT_SHOW_NOTCH;

/**
 * Dialog pane for the notched boxplot node
 * 
 * @author S.Roughley
 *
 */
public class BoxplotNodeDialogPane extends JfcBarChartNodeDialogPane {

	private static final String DEFAULT_FILL_COLOUR = "Default fill colour";
	private static final String DEFAULT_OUTLINE_COLOUR =
			"Default outline colour";
	private static final String EXTREME_OUTLIER_SIZE = "Extreme Outlier Size";
	private static final String OUTLIER_SIZE = "Outlier Size";
	private static final String WHISKER_WIDTH = "Whisker Width";
	private static final String LINE_WIDTH = "Line Width";
	private static final String ENSURE_OUTLIERS_ON_PLOT =
			"Ensure outliers on plot";
	private static final String SHOW_LEGEND = "Show legend";
	private static final String HORIZONTAL_BOXES = "Horizontal boxes";
	private static final String NOTCH_SIZE = "Notch size";
	private static final String SHOW_NOTCH = "Show notch";
	private static final String SHOW_MEDIAN = "Show median";
	private static final String MEAN_SIZE = "Mean size";
	private static final String SHOW_MEAN = "Show mean";
	private static final String DATA_Y_AXIS_VALUES = "Data (Y-Axis) Values";
	private static final String KEEP_CATEGORIES_IN_ORDER_OF_INPUT =
			"Keep categories in order of input";
	private static final String CATEGORICAL_COLUMN = "Categorical column";
	/**
	 * A datavalue column filter for those columns types which might be used as
	 * a catagorical axis label
	 */
	@SuppressWarnings("unchecked")
	static DataValueColumnFilter categoricalColumnFilter =
			new DataValueColumnFilter(StringValue.class, IntValue.class,
					BooleanValue.class, LongValue.class);

	/**
	 * Default constructor
	 */
	public BoxplotNodeDialogPane() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.ext.jfc.node.barchart.JfcBarChartNodeDialogPane#addComponents()
	 */
	@Override
	public void addComponents() {
		createNewGroup("Categorical (X-Axis) Values");
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentColumnNameSelection(
				createCategoricalColumnNameModel(), CATEGORICAL_COLUMN, 0,
				categoricalColumnFilter));
		addDialogComponent(new DialogComponentBoolean(
				createMaintainInputCategoryOrderModel(),
				KEEP_CATEGORIES_IN_ORDER_OF_INPUT));
		setHorizontalPlacement(false);
		closeCurrentGroup();

		createNewGroup(DATA_Y_AXIS_VALUES);
		addDialogComponent(new DialogComponentColumnFilter2(
				createValueColumnFilterModel(), 0));
		closeCurrentGroup();

		createNewGroup("Plot Options");
		SettingsModelBoolean showMeanMdl = createShowMeanModel();
		SettingsModelIntegerBounded meanSizeMdl = createMeanSizeModel();
		meanSizeMdl.setEnabled(showMeanMdl.getBooleanValue());
		showMeanMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				meanSizeMdl.setEnabled(showMeanMdl.getBooleanValue());

			}
		});

		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentBoolean(showMeanMdl, SHOW_MEAN));
		addDialogComponent(
				new DialogComponentNumberEdit(meanSizeMdl, MEAN_SIZE));
		setHorizontalPlacement(false);

		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentBoolean(createShowMedianModel(),
				SHOW_MEDIAN));

		SettingsModelBoolean showNotchMdl = createShowNotchModel();
		SettingsModelDoubleBounded notchSizeMdl = createNotchSizeModel();
		notchSizeMdl.setEnabled(showNotchMdl.getBooleanValue());
		showNotchMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				notchSizeMdl.setEnabled(showNotchMdl.getBooleanValue());

			}
		});

		addDialogComponent(
				new DialogComponentBoolean(showNotchMdl, SHOW_NOTCH));
		addDialogComponent(
				new DialogComponentNumberEdit(notchSizeMdl, NOTCH_SIZE));

		setHorizontalPlacement(false);
		setHorizontalPlacement(true);

		addDialogComponent(new DialogComponentBoolean(
				createHorizontalBoxesModel(), HORIZONTAL_BOXES));
		addDialogComponent(new DialogComponentBoolean(createShowLegendModel(),
				SHOW_LEGEND));
		addDialogComponent(new DialogComponentBoolean(
				createEnsureOutliersModel(), ENSURE_OUTLIERS_ON_PLOT));

		setHorizontalPlacement(false);
		setHorizontalPlacement(true);

		addDialogComponent(new DialogComponentNumberEdit(createLineWidthModel(),
				LINE_WIDTH));
		addDialogComponent(new DialogComponentNumberEdit(
				createWhiskerWidthModel(), WHISKER_WIDTH));
		addDialogComponent(new DialogComponentNumberEdit(
				createOutlierSizeModel(), OUTLIER_SIZE));
		addDialogComponent(new DialogComponentNumberEdit(
				createFarOutlierSizeModel(), EXTREME_OUTLIER_SIZE));
		setHorizontalPlacement(false);
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentColorChooser(
				createDefaultOutlineColourModel(), DEFAULT_OUTLINE_COLOUR,
				true));
		addDialogComponent(new DialogComponentColorChooser(
				createDefaultFillColourModel(), DEFAULT_FILL_COLOUR, true));
		setHorizontalPlacement(false);
	}

	/**
	 * @return The settings model for horizontal bars
	 */
	static SettingsModelBoolean createHorizontalBoxesModel() {
		return new SettingsModelBoolean(HORIZONTAL_BOXES, false);
	}

	/**
	 * @return The settings model for the size of the notch
	 */
	static SettingsModelDoubleBounded createNotchSizeModel() {
		return new SettingsModelDoubleBounded(NOTCH_SIZE, DEFAULT_NOTCH_SIZE,
				0.05, 0.5);
	}

	/**
	 * @return The settings model for whether the notch is shown
	 */
	static SettingsModelBoolean createShowNotchModel() {
		return new SettingsModelBoolean(SHOW_NOTCH, DEFAULT_SHOW_NOTCH);
	}

	/**
	 * @return The settings model for the default outline colour
	 */
	static SettingsModelColor createDefaultOutlineColourModel() {
		return new SettingsModelColor(DEFAULT_OUTLINE_COLOUR,
				(Color) DEFAULT_DEFAULT_BOX);
	}

	/**
	 * @return The settings model for the default fill colour
	 */
	static SettingsModelColor createDefaultFillColourModel() {
		return new SettingsModelColor(DEFAULT_FILL_COLOUR,
				(Color) DEFAULT_DEFAULT_FILL);
	}

	/**
	 * @return The settings model for the mean size
	 */
	static SettingsModelIntegerBounded createMeanSizeModel() {
		return new SettingsModelIntegerBounded(MEAN_SIZE,
				(int) DEFAULT_MEAN_RADIUS, 0, 20);
	}

	/**
	 * @return The settings model for the outlier size
	 */
	static SettingsModelIntegerBounded createOutlierSizeModel() {
		return new SettingsModelIntegerBounded(OUTLIER_SIZE,
				(int) DEFAULT_OUTLIER_RADIUS, 0, 20);
	}

	/**
	 * @return The settings model for the extreme outlier size
	 */
	static SettingsModelIntegerBounded createFarOutlierSizeModel() {
		return new SettingsModelIntegerBounded(EXTREME_OUTLIER_SIZE,
				(int) DEFAULT_FAROUTLIER_RADIUS, 0, 20);
	}

	/**
	 * @return The settings model to ensure extreme outliers are shown
	 */
	static SettingsModelBoolean createEnsureOutliersModel() {
		return new SettingsModelBoolean(ENSURE_OUTLIERS_ON_PLOT, true);
	}

	/**
	 * @return The settings model for the line width
	 */
	static SettingsModelDoubleBounded createLineWidthModel() {
		return new SettingsModelDoubleBounded(LINE_WIDTH, 1.0, 0.1, 10.0);
	}

	/**
	 * @return The settings model for the whisker width. the maximum value of
	 *         1.0 is hard-coded into one of the renderer superclasses
	 */
	static SettingsModelDoubleBounded createWhiskerWidthModel() {
		return new SettingsModelDoubleBounded(WHISKER_WIDTH, 1.0, 0.1, 1.0);
	}

	/**
	 * @return The settings model for showing a legend on the plot
	 */
	static SettingsModelBoolean createShowLegendModel() {
		return new SettingsModelBoolean(SHOW_LEGEND, true);
	}

	/**
	 * @return The settings model for whether the median is shown
	 */
	static SettingsModelBoolean createShowMedianModel() {
		return new SettingsModelBoolean(SHOW_MEDIAN, true);
	}

	/**
	 * @return The settings model for whether the mean is shown
	 */
	static SettingsModelBoolean createShowMeanModel() {
		return new SettingsModelBoolean(SHOW_MEAN, false);
	}

	/**
	 * @return The settings model for whether the input order of categories is
	 *         maintained
	 */
	static SettingsModelBoolean createMaintainInputCategoryOrderModel() {
		return new SettingsModelBoolean(KEEP_CATEGORIES_IN_ORDER_OF_INPUT,
				true);
	}

	/**
	 * @return The settings model for the category column
	 */
	static SettingsModelString createCategoricalColumnNameModel() {
		return new SettingsModelString(CATEGORICAL_COLUMN, null);
	}

	/**
	 * @return The settings model for the series column filter
	 */
	@SuppressWarnings("unchecked")
	static SettingsModelColumnFilter2 createValueColumnFilterModel() {
		return new SettingsModelColumnFilter2(DATA_Y_AXIS_VALUES,
				DoubleValue.class);
	}
}
