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
package com.vernalis.knime.chem.pmi.nodes.plot.convexhull;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane;

/**
 * Node dialog for the PMI Hull plot
 */
public class PMIHullPlotNodeDialogPane
		extends AbstractPMIDrawableSeriesNodeDialogPane {

	static final String HULL_NPMI2_COLUMN = "Hull " + N_PMI2_I2_I3_COLUMN;
	static final String HULL_NPMI1_COLUMN = "Hull " + N_PMI1_I1_I3_COLUMN;
	static final String HULL_OPTIONS = "Hull Options";
	static final String THRESHOLD_FOR_AREA_TO_SHOW_HULL_POINTS =
			"Threshold for area to show hull points";
	static final String SHOW_RELATIVE_HULL_AREA_ON_LEGEND =
			"Show relative hull area on legend";
	static final String SHOW_LEGEND = "Show legend";
	static final String USE_GREYSCALE_COLOUR_PALETTE_IF_NO_COLOUR_MODEL_AVAILABLE =
			"Use greyscale colour palette if no colour model available";
	static final String HULL_TRANSPARENCY = "Hull Transparency";
	static final String SHOW_HULL_BOUNDRIES = "Show hull boundries";
	static final String GROUPING_COLUMN = "Grouping Column";

	/**
	 * Constructor
	 */
	protected PMIHullPlotNodeDialogPane() {
		super(2);
		createNewTabAt(HULL_OPTIONS, 0);
		setSelected(HULL_OPTIONS);

		addDialogComponent(new DialogComponentColumnNameSelection(
				createHullNpr1ColumnModel(), HULL_NPMI1_COLUMN, 0,
				DOUBLE_VALUE_COLUMN_FILTER));
		addDialogComponent(new DialogComponentColumnNameSelection(
				createHullNpr2ColumnModel(), HULL_NPMI2_COLUMN, 0,
				DOUBLE_VALUE_COLUMN_FILTER));
		final SettingsModelString groupColumnMdl = createGroupColumnModel();
		addDialogComponent(new DialogComponentColumnNameSelection(
				groupColumnMdl, GROUPING_COLUMN, 0, false, true,
				STRING_VALUE_COLUMN_FILTER));

		addDialogComponent(new DialogComponentBoolean(
				createShowHullBoundriesModel(), SHOW_HULL_BOUNDRIES));
		addDialogComponent(new DialogComponentNumber(
				createHullAlphaValueModel(), HULL_TRANSPARENCY, 5));
		addDialogComponent(new DialogComponentBoolean(
				createUseDefaultGreyscale(),
				USE_GREYSCALE_COLOUR_PALETTE_IF_NO_COLOUR_MODEL_AVAILABLE));
		SettingsModelBoolean legendMdl = getShowLegendModel();
		SettingsModelBoolean relativeAreaModel = createShowRelativeAreaModel();
		groupColumnMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				legendMdl.setEnabled(groupColumnMdl.getStringValue() != null
						&& !groupColumnMdl.getStringValue().isEmpty());

			}
		});
		legendMdl.setEnabled(groupColumnMdl.getStringValue() != null
				&& !groupColumnMdl.getStringValue().isEmpty());
		legendMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				relativeAreaModel.setEnabled(
						legendMdl.isEnabled() && legendMdl.getBooleanValue());
			}
		});
		relativeAreaModel.setEnabled(
				legendMdl.isEnabled() && legendMdl.getBooleanValue());

		addDialogComponent(new DialogComponentBoolean(legendMdl, SHOW_LEGEND));

		addDialogComponent(new DialogComponentBoolean(relativeAreaModel,
				SHOW_RELATIVE_HULL_AREA_ON_LEGEND));
		addDialogComponent(new DialogComponentNumber(createAreaThresholdModel(),
				THRESHOLD_FOR_AREA_TO_SHOW_HULL_POINTS, 5));
		closeCurrentGroup();
	}

	/**
	 * @return The Settings Model for the {@value #HULL_NPMI1_COLUMN} setting
	 */
	static final SettingsModelString createHullNpr1ColumnModel() {
		return new SettingsModelString(HULL_NPMI1_COLUMN, null);// X-axis
	}

	/**
	 * @return The Settings Model for the {@value #HULL_NPMI2_COLUMN} setting
	 */
	static final SettingsModelString createHullNpr2ColumnModel() {
		return new SettingsModelString(HULL_NPMI2_COLUMN, null);// Y-axis
	}

	/**
	 * @return The Settings Model for the {@value #SHOW_LEGEND} setting
	 */
	static SettingsModelBoolean getShowLegendModel() {
		return new SettingsModelBoolean(SHOW_LEGEND, false);
	}

	/**
	 * @return The Settings Model for the {@value #HULL_TRANSPARENCY} setting
	 */
	static SettingsModelIntegerBounded createHullAlphaValueModel() {
		return new SettingsModelIntegerBounded(HULL_TRANSPARENCY, 175, 0, 255);
	}

	/**
	 * @return The Settings Model for the {@value #GROUPING_COLUMN} setting
	 */
	static SettingsModelString createGroupColumnModel() {
		return new SettingsModelString(GROUPING_COLUMN, null);
	}

	/**
	 * @return The Settings Model for the
	 *         {@value #THRESHOLD_FOR_AREA_TO_SHOW_HULL_POINTS} setting
	 */
	static SettingsModelDoubleBounded createAreaThresholdModel() {
		return new SettingsModelDoubleBounded(
				THRESHOLD_FOR_AREA_TO_SHOW_HULL_POINTS, 5.0, 0.0, 100.0);
	}

	/**
	 * @return The Settings Model for the
	 *         {@value #USE_GREYSCALE_COLOUR_PALETTE_IF_NO_COLOUR_MODEL_AVAILABLE}
	 *         setting
	 */
	static SettingsModelBoolean createUseDefaultGreyscale() {
		return new SettingsModelBoolean(
				USE_GREYSCALE_COLOUR_PALETTE_IF_NO_COLOUR_MODEL_AVAILABLE,
				false);
	}

	/**
	 * @return The Settings Model for the
	 *         {@value #SHOW_RELATIVE_HULL_AREA_ON_LEGEND} setting
	 */
	static SettingsModelBoolean createShowRelativeAreaModel() {
		return new SettingsModelBoolean(SHOW_RELATIVE_HULL_AREA_ON_LEGEND,
				false);
	}

	/**
	 * @return The Settings Model for the {@value #SHOW_HULL_BOUNDRIES} setting
	 */
	static SettingsModelBoolean createShowHullBoundriesModel() {
		return new SettingsModelBoolean(SHOW_HULL_BOUNDRIES, true);
	}

}
