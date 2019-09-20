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
package com.vernalis.knime.chem.pmi.nodes.plot.abstrct;

import java.awt.Color;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.property.ColorModelRange;
import org.knime.core.data.property.ShapeFactory;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColorChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColor;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.DataValueColumnFilter;
import org.knime.ext.jfc.node.base.JfcBaseNodeDialogPane;

import com.vernalis.knime.dialog.components.DialogComponentShapeSelector;
import com.vernalis.knime.dialog.components.SettingsModelShape;

/**
 * Node Dialog base class for the PMI Plot nodes. This class creates the options
 * for the Scatter port and basic PMI options (e.g. bounds triangle colour,
 * labels etc). If more than one input port is specified, the last, scatter
 * table, port is optional, and if not connected, it's options tab is disabled
 * in the dialog
 */
public class AbstractPMIDrawableSeriesNodeDialogPane
		extends JfcBaseNodeDialogPane {

	static final String SCATTER_OPTIONS = "Scatter Options";
	static final String PMI_OPTIONS = "PMI Options";
	public static final String SHOW_FULL_TRIANGLE = "Show full triangle";
	public static final String TRIANGLE_BOUNDS_COLOUR =
			"Triangle bounds colour";
	public static final String VERTEX_LABEL_COLOUR = "Vertex label colour";
	static final String DEFAULT_SHAPE = "Default Shape";
	static final String ITEM_SIZE = "Item size";
	static final String SHOW_COLOUR_GRADIENT_LEGEND =
			"Show Colour Gradient legend";
	static final String COLOUR_GRADIENT_LEGEND = "Colour Gradient Legend";
	static final String SHOW_SHAPE_NOMINAL_COLOURS_LEGEND =
			"Show Shape/Nominal Colours Legend";
	static final String SHOW_SIZE_LEGEND = "Show Size legend";
	public static final String N_PMI2_I2_I3_COLUMN =
			"nPMI2 (I2/I3, npr2) column";
	public static final String N_PMI1_I1_I3_COLUMN =
			"nPMI1 (I1/I3, npr1) column";
	protected SettingsModelBoolean sizeLegendMdl;
	protected SettingsModelBoolean shapeLegendMdl;
	protected SettingsModelBoolean colourGradLegendMdl;

	final int numPorts;
	boolean scatterConnected = true;

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
	 * Override constructor for 1 input port
	 */
	public AbstractPMIDrawableSeriesNodeDialogPane() {
		this(1);
	}

	/**
	 * Constructor allowing specification of the number of input ports
	 * 
	 * @param numPorts
	 *            The number of input ports
	 */
	public AbstractPMIDrawableSeriesNodeDialogPane(int numPorts) {
		super();
		this.numPorts = numPorts;

		removeTab("Options");
		createNewTabAt(SCATTER_OPTIONS, 0);
		setSelected(SCATTER_OPTIONS);
		this.createNewGroup("PMI Columns");
		SettingsModelString npmi1ColumnModel = getNPMI1ColumnModel();
		this.addDialogComponent(new DialogComponentColumnNameSelection(
				npmi1ColumnModel, N_PMI1_I1_I3_COLUMN, numPorts - 1,
				numPorts == 1, DOUBLE_VALUE_COLUMN_FILTER));
		SettingsModelString npmi2ColumnModel = getNPMI2ColumnModel();
		this.addDialogComponent(new DialogComponentColumnNameSelection(
				npmi2ColumnModel, N_PMI2_I2_I3_COLUMN, numPorts - 1,
				numPorts == 1, DOUBLE_VALUE_COLUMN_FILTER));

		createNewGroup("Plot symbol options");
		// item size
		addDialogComponent(new DialogComponentNumber(getElementSizeModel(),
				ITEM_SIZE, 1, 9));
		this.setHorizontalPlacement(true);
		this.addDialogComponent(new DialogComponentShapeSelector(
				getDefaultShapeModel(), DEFAULT_SHAPE));
		this.setHorizontalPlacement(false);
		this.closeCurrentGroup();

		createNewGroup("Legends");
		sizeLegendMdl = getSizeLegendModel();
		shapeLegendMdl = getShapeLegendModel();
		colourGradLegendMdl = getColourGradientModel();

		addDialogComponent(
				new DialogComponentBoolean(sizeLegendMdl, SHOW_SIZE_LEGEND));
		addDialogComponent(new DialogComponentBoolean(shapeLegendMdl,
				SHOW_SHAPE_NOMINAL_COLOURS_LEGEND));
		addDialogComponent(new DialogComponentBoolean(colourGradLegendMdl,
				SHOW_COLOUR_GRADIENT_LEGEND));
		closeCurrentGroup();

		if (numPorts > 1) {
			createNewTabAt(PMI_OPTIONS, 0);
			setSelected(PMI_OPTIONS);
		}

		createNewGroup("PMI Bounds Triangle");
		addDialogComponent(new DialogComponentColorChooser(
				getVertexColourModel(), VERTEX_LABEL_COLOUR, true));
		addDialogComponent(new DialogComponentColorChooser(
				getTriangleColourModel(), TRIANGLE_BOUNDS_COLOUR, true));
		addDialogComponent(new DialogComponentBoolean(
				createShowFullTriangleModel(), SHOW_FULL_TRIANGLE));
		closeCurrentGroup();

	}

	/** {@inheritDoc} */
	@Override
	public final void addComponents() {
		// This puts the components in the 'Options' tab, which we remove!
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeDialogPane#loadSettingsFrom(org.knime.core.node.
	 * NodeSettingsRO, org.knime.core.data.DataTableSpec[])
	 */
	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings,
			DataTableSpec[] specs) throws NotConfigurableException {
		super.loadAdditionalSettingsFrom(settings, specs);
		scatterConnected = specs[numPorts - 1].getNumColumns() > 0;
		setEnabled(scatterConnected, SCATTER_OPTIONS);
		sizeLegendMdl.setEnabled(false);
		shapeLegendMdl.setEnabled(false);
		colourGradLegendMdl.setEnabled(false);
		int colColIdx = -1;
		int shapeColIdx = -1;
		int sizeColIdx = -1;
		for (int i = 0; i < specs[numPorts - 1].getNumColumns()
				&& (colColIdx < 0 || shapeColIdx < 0 || sizeColIdx < 0); i++) {
			DataColumnSpec colSpec = specs[numPorts - 1].getColumnSpec(i);
			if (colSpec.getColorHandler() != null && colColIdx < 0) {
				colColIdx = i;
				if (colSpec.getColorHandler()
						.getColorModel() instanceof ColorModelRange) {
					colourGradLegendMdl.setEnabled(true);
				} else {
					shapeLegendMdl.setEnabled(true);
				}
			}
			if (colSpec.getShapeHandler() != null && shapeColIdx < 0) {
				shapeColIdx = i;
				shapeLegendMdl.setEnabled(true);
			}
			if (colSpec.getSizeHandler() != null && sizeColIdx < 0) {
				sizeColIdx = i;
				sizeLegendMdl.setEnabled(true);
			}
		}

	}

	/**
	 * @return The Settings model for the scatter port npr2 column
	 */
	static SettingsModelString getNPMI2ColumnModel() {
		return new SettingsModelString(N_PMI2_I2_I3_COLUMN, null);
	}

	/**
	 * @return The Settings model for the scatter port npr1 column
	 */
	static SettingsModelString getNPMI1ColumnModel() {
		return new SettingsModelString(N_PMI1_I1_I3_COLUMN, null);
	}

	/**
	 * @return The Settings model for the {@value #COLOUR_GRADIENT_LEGEND}
	 *         setting
	 */
	static SettingsModelBoolean getColourGradientModel() {
		return new SettingsModelBoolean(COLOUR_GRADIENT_LEGEND, true);
	}

	/**
	 * @return The Settings model for the {@value #SHOW_SIZE_LEGEND} setting
	 */
	static SettingsModelBoolean getSizeLegendModel() {
		return new SettingsModelBoolean(SHOW_SIZE_LEGEND, true);
	}

	/**
	 * @return The Settings model for the {@value #ITEM_SIZE} setting
	 */
	static final SettingsModelDoubleBounded getElementSizeModel() {
		return new SettingsModelDoubleBounded(ITEM_SIZE, 5.0, 0.0,
				Integer.MAX_VALUE);
	}

	/**
	 * @return The Settings model for the {@value #DEFAULT_SHAPE} setting
	 */
	static final SettingsModelShape getDefaultShapeModel() {
		return new SettingsModelShape(DEFAULT_SHAPE,
				ShapeFactory.getShape(ShapeFactory.X_SHAPE));
	}

	/**
	 * @return The Settings model for the {@value #VERTEX_LABEL_COLOUR} setting
	 */
	public static final SettingsModelColor getVertexColourModel() {
		return new SettingsModelColor(VERTEX_LABEL_COLOUR, Color.RED);
	}

	/**
	 * @return The Settings model for the {@value #TRIANGLE_BOUNDS_COLOUR}
	 *         setting
	 */
	public static SettingsModelColor getTriangleColourModel() {
		return new SettingsModelColor(TRIANGLE_BOUNDS_COLOUR, Color.BLACK);
	}

	/**
	 * @return The Settings model for the {@value #SHOW_FULL_TRIANGLE} setting
	 */
	public static SettingsModelBoolean createShowFullTriangleModel() {
		return new SettingsModelBoolean(SHOW_FULL_TRIANGLE, true);
	}

	/**
	 * @return The Settings model for the
	 *         {@value #SHOW_SHAPE_NOMINAL_COLOURS_LEGEND} setting
	 */
	static SettingsModelBoolean getShapeLegendModel() {
		return new SettingsModelBoolean(SHOW_SHAPE_NOMINAL_COLOURS_LEGEND,
				false);
	}

}
