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
package com.vernalis.knime.plot.nodes.kerneldensity;

import java.awt.Color;
import java.util.Arrays;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.property.ShapeFactory;
import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColorChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentDoubleRange;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColor;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleRange;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.DefaultStringIconOption;

import com.vernalis.knime.dialog.components.DialogComponentGroup;
import com.vernalis.knime.dialog.components.DialogComponentShapeSelector;
import com.vernalis.knime.dialog.components.SettingsModelShape;
import com.vernalis.knime.jfcplot.core.nodes.AbstractDrawableSeriesPlotNodeDialogPane;
import com.vernalis.knime.misc.ArrayUtils;

import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.BANDWIDTH;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.COLUMN_SELECTION;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.CONTOUR_INTERVAL_SCHEMA;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.DEFAULT_GRID_POINTS;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.DEFAULT_OUTLIERS;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.EXPAND_ALL_KERNELS_TO_WHOLE_RANGE;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.FILL_CONTOURS;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.GROUPING_COLUMN;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.KERNEL_ESTIMATOR;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.KERNEL_OPTIONS;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.KERNEL_SYMMETRY;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.LOWER_BOUND;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.NUMBER_OF_CONTOURS;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.NUMBER_OF_OUTLIERS_OF_DATASET;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.OUTLIER_COLOUR;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.OUTLIER_OPTIONS;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.OUTLIER_SHAPE;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.OUTLIER_SIZE;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.PLOT_OPTIONS;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.SHOW_ALL_DATA_KERNEL;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.SHOW_LEGEND;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.UPPER_BOUND;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.VALUES_COLUMN;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.X;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.X_VALUES_COLUMN;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.Y;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.Y_VALUES_COLUMN;

/**
 * Node Dialog pane to handle 1- and 2-D Node settings for Kernel Density Plot
 * nodes
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class KernelDensityPlotNodeDialogPane
		extends AbstractDrawableSeriesPlotNodeDialogPane {

	protected final boolean is2D;
	protected final SettingsModelBoolean showHMdl;
	protected final SettingsModelBoolean showLegendMdl =
			createShowLegendModel();
	protected final SettingsModelString groupColNameMdl;
	protected final SettingsModelBoolean expandKernelsMdl;

	/**
	 * Convenience constructor for 1D case with default label
	 * 
	 * @param hasAutorangeAxes
	 *            Does the node dialog have the automatic axis range options?
	 */
	public KernelDensityPlotNodeDialogPane(boolean hasAutorangeAxes) {
		this(false, hasAutorangeAxes);
	}

	/**
	 * Convenience constructor case with default labels
	 * 
	 * @param is2D
	 *            Is the node a 2D kernel node?
	 * @param hasAutorangeAxes
	 *            Does the node dialog have the automatic axis range options?
	 */
	public KernelDensityPlotNodeDialogPane(boolean is2D,
			boolean hasAutorangeAxes) {
		this(is2D ? X_VALUES_COLUMN : VALUES_COLUMN,
				is2D ? Y_VALUES_COLUMN : null, hasAutorangeAxes);
	}

	/**
	 * @param xColLabel
	 *            The label for the x-column selector
	 * @param yColLabel
	 *            The label for the y-column selector
	 * @param hasAutorangeAxes
	 *            Does the node dialog have the automatic axis range options?
	 */
	public KernelDensityPlotNodeDialogPane(String xColLabel, String yColLabel,
			boolean hasAutorangeAxes) {

		// Add the column selector(s) to a new dialog tab
		super(KERNEL_OPTIONS, COLUMN_SELECTION,
				yColLabel == null || yColLabel.isEmpty()
						? new String[] { xColLabel }
						: new String[] { xColLabel, yColLabel },
				ArrayUtils.of(DOUBLE_VALUE_COLUMN_FILTER,
						yColLabel == null || yColLabel.isEmpty() ? 1 : 2));

		this.is2D = yColLabel != null && !yColLabel.isEmpty();
		showHMdl = createShowHValuesModel(is2D);

		if (!is2D) {
			SettingsModelBoolean totalDataMdl =
					createShowTotalDataKernelModel();
			expandKernelsMdl = createExpandKernelsModel();
			groupColNameMdl = createGroupColNameModel();
			groupColNameMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					showHMdl.setEnabled(is2D
							|| groupColNameMdl.getStringValue() == null
							|| groupColNameMdl.getStringValue().isEmpty()
							|| (showLegendMdl.isEnabled()
									&& showLegendMdl.getBooleanValue()));
					showLegendMdl.setEnabled(is2D || (groupColNameMdl
							.getStringValue() != null
							&& !groupColNameMdl.getStringValue().isEmpty()));
					totalDataMdl.setEnabled(groupColNameMdl
							.getStringValue() != null
							&& !groupColNameMdl.getStringValue().isEmpty());
					expandKernelsMdl.setEnabled(groupColNameMdl
							.getStringValue() != null
							&& !groupColNameMdl.getStringValue().isEmpty());
				}
			});
			showHMdl.setEnabled(is2D || groupColNameMdl.getStringValue() == null
					|| groupColNameMdl.getStringValue().isEmpty()
					|| (showLegendMdl.isEnabled()
							&& showLegendMdl.getBooleanValue()));
			showLegendMdl.setEnabled(
					is2D || (groupColNameMdl.getStringValue() != null
							&& !groupColNameMdl.getStringValue().isEmpty()));
			totalDataMdl.setEnabled(groupColNameMdl.getStringValue() != null
					&& !groupColNameMdl.getStringValue().isEmpty());
			expandKernelsMdl.setEnabled(groupColNameMdl.getStringValue() != null
					&& !groupColNameMdl.getStringValue().isEmpty());
			setHorizontalPlacement(true);
			addDialogComponent(new DialogComponentColumnNameSelection(
					groupColNameMdl, GROUPING_COLUMN, 0, false, true,
					STRING_VALUE_COLUMN_FILTER));
			addDialogComponent(new DialogComponentBoolean(totalDataMdl,
					SHOW_ALL_DATA_KERNEL));
			setHorizontalPlacement(false);
		} else {
			groupColNameMdl = null;
			expandKernelsMdl = null;
		}
		closeCurrentGroup();

		addOptionalDialogs();

		createNewGroup(KERNEL_OPTIONS);
		setHorizontalPlacement(true);
		final SettingsModelString kernelEstimatorMdl =
				createKernelEstimatorModel();
		FlowVariableModel kEstFvm = createFlowVariableModel(kernelEstimatorMdl);
		addDialogComponent(
				new DialogComponentStringSelection(kernelEstimatorMdl,
						KERNEL_ESTIMATOR,
						DefaultStringIconOption.createOptionArray(
								Arrays.asList(KernelEstimator.names())),
						kEstFvm));
		if (is2D) {
			final SettingsModelString kernelSymmMdl = createKernelSymmModel();
			FlowVariableModel kSymmFvm = createFlowVariableModel(kernelSymmMdl);
			addDialogComponent(
					new DialogComponentStringSelection(kernelSymmMdl,
							KERNEL_SYMMETRY,
							DefaultStringIconOption.createOptionArray(
									Arrays.asList(KernelSymmetry.names())),
							kSymmFvm));
		}
		setHorizontalPlacement(false);

		SettingsModelString autoHXMdl =
				createAutoGenerateBandwidthModel(xColLabel);
		SettingsModelNumber hXMdl = createManualBandwidthModel(is2D, X);
		autoHXMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				hXMdl.setEnabled(BandwidthEstimationMethod
						.valueOf(autoHXMdl.getStringValue())
						.needsManualValue());
			}
		});
		hXMdl.setEnabled(BandwidthEstimationMethod
				.valueOf(autoHXMdl.getStringValue()).needsManualValue());

		DialogComponentGroup autoH0Grp = new DialogComponentGroup(this,
				getDialogBandwidthBoxTitle(xColLabel),
				new DialogComponentButtonGroup(autoHXMdl, null, false,
						BandwidthEstimationMethod.values()),
				true);
		autoH0Grp.addComponent(new DialogComponentNumberEdit(hXMdl,
				getDialogManualBandwidthName(is2D, X)));

		if (is2D) {
			SettingsModelString autoHYMdl =
					createAutoGenerateBandwidthModel(yColLabel);
			SettingsModelNumber hYMdl = createManualBandwidthModel(is2D, Y);
			autoHYMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					hYMdl.setEnabled(BandwidthEstimationMethod
							.valueOf(autoHYMdl.getStringValue())
							.needsManualValue());
				}
			});
			hYMdl.setEnabled(BandwidthEstimationMethod
					.valueOf(autoHYMdl.getStringValue()).needsManualValue());

			DialogComponentGroup autoH1Grp =
					new DialogComponentGroup(this,
							getDialogBandwidthBoxTitle(yColLabel),
							new DialogComponentButtonGroup(autoHYMdl, null,
									false, BandwidthEstimationMethod.values()),
							true);
			autoH1Grp.addComponent(new DialogComponentNumberEdit(hYMdl,
					getDialogManualBandwidthName(is2D, Y)));
		}

		addDialogComponent(new DialogComponentNumber(createGridSizeModel(is2D),
				getDialogGridPointsName(is2D), 50));

		if (!is2D) {
			addDialogComponent(new DialogComponentBoolean(expandKernelsMdl,
					EXPAND_ALL_KERNELS_TO_WHOLE_RANGE));
		}
		closeCurrentGroup();

		if (is2D) {
			createNewGroup(OUTLIER_OPTIONS);
			addDialogComponent(
					new DialogComponentNumber(createNumOutliersModel(),
							NUMBER_OF_OUTLIERS_OF_DATASET, 5));
			setHorizontalPlacement(true);
			addDialogComponent(new DialogComponentNumberEdit(
					createOutlierSizeModel(), OUTLIER_SIZE));
			addDialogComponent(new DialogComponentShapeSelector(
					createOutlierShapeModel(), OUTLIER_SHAPE));
			addDialogComponent(new DialogComponentColorChooser(
					createOutlierColorModel(), OUTLIER_COLOUR, true));
			setHorizontalPlacement(false);
			closeCurrentGroup();
		}

		createNewGroup(PLOT_OPTIONS);
		if (hasAutorangeAxes) {
			setHorizontalPlacement(true);
			SettingsModelBoolean xAutoRngMdl = createAutorangeModel(X);
			SettingsModelDoubleRange xRangeMdl = createAxisRangeModel(X);
			xAutoRngMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					xRangeMdl.setEnabled(!xAutoRngMdl.getBooleanValue());

				}
			});
			xRangeMdl.setEnabled(!xAutoRngMdl.getBooleanValue());

			addDialogComponent(new DialogComponentBoolean(xAutoRngMdl,
					getDialogAutoRangeAxisName(X)));
			addDialogComponent(
					new DialogComponentDoubleRange(xRangeMdl, -Double.MAX_VALUE,
							Double.MAX_VALUE, 10.0, getDialogAxisRangeName(X)));
			setHorizontalPlacement(false);

			if (is2D) {
				setHorizontalPlacement(true);
				SettingsModelBoolean yAutoRngMdl = createAutorangeModel(Y);
				SettingsModelDoubleRange yRangeMdl = createAxisRangeModel(Y);
				yAutoRngMdl.addChangeListener(new ChangeListener() {

					@Override
					public void stateChanged(ChangeEvent e) {
						yRangeMdl.setEnabled(!yAutoRngMdl.getBooleanValue());

					}
				});
				yRangeMdl.setEnabled(!yAutoRngMdl.getBooleanValue());

				addDialogComponent(new DialogComponentBoolean(yAutoRngMdl,
						getDialogAutoRangeAxisName(Y)));
				addDialogComponent(new DialogComponentDoubleRange(yRangeMdl,
						-Double.MAX_VALUE, Double.MAX_VALUE, 10.0,
						getDialogAxisRangeName(Y)));
				setHorizontalPlacement(false);
			}
		}

		setHorizontalPlacement(true);
		showLegendMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				showHMdl.setEnabled(
						is2D || groupColNameMdl.getStringValue() == null
								|| groupColNameMdl.getStringValue().isEmpty()
								|| (showLegendMdl.isEnabled()
										&& showLegendMdl.getBooleanValue()));

			}
		});
		showHMdl.setEnabled(is2D || groupColNameMdl.getStringValue() == null
				|| groupColNameMdl.getStringValue().isEmpty()
				|| (showLegendMdl.isEnabled()
						&& showLegendMdl.getBooleanValue()));
		showLegendMdl
				.setEnabled(is2D || groupColNameMdl.getStringValue() == null
						|| groupColNameMdl.getStringValue().isEmpty());
		addDialogComponent(
				new DialogComponentBoolean(showLegendMdl, SHOW_LEGEND));
		addDialogComponent(new DialogComponentBoolean(showHMdl,
				getDialogShowBandwidthName(is2D)));
		setHorizontalPlacement(false);

		if (is2D) {
			setHorizontalPlacement(true);
			addDialogComponent(new DialogComponentColorChooser(
					createUpperBoundColorModel(), UPPER_BOUND, true));

			addDialogComponent(new DialogComponentColorChooser(
					createLowerBoundColorModel(), LOWER_BOUND, true));
			setHorizontalPlacement(false);

			final SettingsModelIntegerBounded numContoursMdl =
					createNumContoursModel();
			final SettingsModelBoolean filledContoursMdl =
					createFilledContoursModel();
			final SettingsModelString contourIntervalSchemaMdl =
					createContourIntervalSchemaModel();
			numContoursMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					filledContoursMdl
							.setEnabled(numContoursMdl.getIntValue() > 0);
					contourIntervalSchemaMdl
							.setEnabled(numContoursMdl.getIntValue() > 0);
				}
			});

			filledContoursMdl.setEnabled(numContoursMdl.getIntValue() > 0);
			contourIntervalSchemaMdl
					.setEnabled(numContoursMdl.getIntValue() > 0);
			setHorizontalPlacement(true);
			addDialogComponent(new DialogComponentNumber(numContoursMdl,
					NUMBER_OF_CONTOURS, 1));
			addDialogComponent(new DialogComponentBoolean(filledContoursMdl,
					FILL_CONTOURS));
			addDialogComponent(new DialogComponentStringSelection(
					contourIntervalSchemaMdl, CONTOUR_INTERVAL_SCHEMA,
					ContourIntervalSchema.names()));
			setHorizontalPlacement(true);
		}
		addAdditionalPlotOptions();
		closeCurrentGroup();
	}

	/**
	 * Hook to add additional options to the 'Plot Options' part of the dialog
	 */
	protected void addAdditionalPlotOptions() {

	}

	/**
	 * Method to optionally add additional dialog components between the column
	 * selectors and the Kernel options
	 */
	protected void addOptionalDialogs() {

	}

	/**
	 * @return The settings model for the expand kernels model
	 */
	static SettingsModelBoolean createExpandKernelsModel() {
		return new SettingsModelBoolean(EXPAND_ALL_KERNELS_TO_WHOLE_RANGE,
				false);
	}

	/**
	 * @return The settings model to show the overall data kernel
	 */
	static SettingsModelBoolean createShowTotalDataKernelModel() {
		return new SettingsModelBoolean(SHOW_ALL_DATA_KERNEL, true);
	}

	/**
	 * @param is2D
	 *            Is the node 2D?
	 * @return The dialog option name for the option to show the bandwidth on
	 *         the axis labels
	 */
	static String getDialogShowBandwidthName(boolean is2D) {
		return "Show bandwidth" + (is2D ? "s" : "") + " (H) on axis label"
				+ (is2D ? "s" : " or legend");
	}

	/**
	 * @param axis
	 *            The name of the axis (e.g. x or y)
	 * @return The axis range dialog caption
	 */
	static String getDialogAxisRangeName(String axis) {
		return axis + "-Axis Range";
	}

	/**
	 * @param axis
	 *            The name of the axis (e.g. x or y)
	 * @return the auto-range axis dialog caption
	 */
	static String getDialogAutoRangeAxisName(String axis) {
		return "Auto-range " + axis + "-Axis";
	}

	/**
	 * @param is2D
	 *            Is the node 2D
	 * @param suffix
	 *            Any optional suffix after 'H' in the caption - only applied if
	 *            2D is {@code true}
	 * @return The caption for the manual bandwidth setting
	 */
	static String getDialogManualBandwidthName(boolean is2D, String suffix) {
		return "Bandwidth (H" + (is2D ? (" " + suffix.trim()) : "") + ")";
	}

	/**
	 * @param colLabel
	 *            The column label for the bandwidth
	 * @return The dialog caption for the box surrounding the bandwidth options
	 *         for the given column selector
	 */
	static String getDialogBandwidthBoxTitle(String colLabel) {
		return colLabel + BANDWIDTH;
	}

	/**
	 * @param is2D
	 *            Is the node 2D
	 * @return The dialog caption for the number of grid points option
	 */
	static String getDialogGridPointsName(boolean is2D) {
		return "Number of grid points along ax" + (is2D ? "es" : "is");
	}

	/**
	 * @return The settings model for the grouping column option
	 */
	static SettingsModelString createGroupColNameModel() {
		return new SettingsModelString(GROUPING_COLUMN, null);
	}

	/**
	 * @return The settings model for the outlier shape option
	 */
	static SettingsModelShape createOutlierShapeModel() {
		return new SettingsModelShape(OUTLIER_SHAPE,
				ShapeFactory.getShape(ShapeFactory.X_SHAPE));
	}

	/**
	 * @return The settings model for the contour interval schema option
	 */
	static SettingsModelString createContourIntervalSchemaModel() {
		return new SettingsModelString(CONTOUR_INTERVAL_SCHEMA,
				ContourIntervalSchema.getDefault().name());
	}

	/**
	 * @return The settings model for the fill contours option
	 */
	static SettingsModelBoolean createFilledContoursModel() {
		return new SettingsModelBoolean(FILL_CONTOURS, false);
	}

	/**
	 * @return The settings model for the number of contours option
	 */
	static SettingsModelIntegerBounded createNumContoursModel() {
		return new SettingsModelIntegerBounded(NUMBER_OF_CONTOURS, 0, 0, 25);
	}

	/**
	 * @return The settings model for the Kernel Symmetry option
	 */
	static SettingsModelString createKernelSymmModel() {
		return new SettingsModelString(KERNEL_SYMMETRY,
				KernelSymmetry.getDefault().name());
	}

	/**
	 * @return The settings model for the outlier colour option
	 */
	static SettingsModelColor createOutlierColorModel() {
		return new SettingsModelColor(OUTLIER_COLOUR, Color.red);
	}

	/**
	 * @return The settings model for the outlier size option
	 */
	static SettingsModelIntegerBounded createOutlierSizeModel() {
		return new SettingsModelIntegerBounded(OUTLIER_SIZE, 5, 1, 20);
	}

	/**
	 * @return The settings model for the number of outliers option
	 */
	static SettingsModelIntegerBounded createNumOutliersModel() {
		return new SettingsModelIntegerBounded(NUMBER_OF_OUTLIERS_OF_DATASET,
				DEFAULT_OUTLIERS, 0, 100);
	}

	/**
	 * @return The settings model for the grid size option
	 */
	static SettingsModelIntegerBounded createGridSizeModel(boolean is2D) {
		return new SettingsModelIntegerBounded(getDialogGridPointsName(is2D),
				DEFAULT_GRID_POINTS, 5, 10000);
	}

	/**
	 * @param is2D
	 *            Is the node 2D?
	 * @return The settings model for the show bandwidths on axis labels option
	 */
	static SettingsModelBoolean createShowHValuesModel(boolean is2D) {
		return new SettingsModelBoolean(getDialogShowBandwidthName(is2D), true);
	}

	/**
	 * @param axis
	 *            The axis name
	 * @return The settings model for the manual axis range option
	 */
	static SettingsModelDoubleRange createAxisRangeModel(String axis) {
		return new SettingsModelDoubleRange(getDialogAxisRangeName(axis), 0.0,
				10.0);
	}

	/**
	 * @param axis
	 *            The axis name
	 * @return The settings model for the autorange axis option
	 */
	static SettingsModelBoolean createAutorangeModel(String axis) {
		return new SettingsModelBoolean(getDialogAutoRangeAxisName(axis), true);
	}

	/**
	 * @param The
	 *            column selector label corresponding to the bandwidth
	 * @return The settings model for the bandwidth estimation method option
	 */
	static SettingsModelString createAutoGenerateBandwidthModel(
			String colLabel) {
		return new SettingsModelString(getDialogBandwidthBoxTitle(colLabel),
				BandwidthEstimationMethod.getDefault().getActionCommand());
	}

	/**
	 * @param is2D
	 *            Is the node 2D?
	 * @return The settings model for the manual bandwith option
	 */
	static SettingsModelDoubleBounded createManualBandwidthModel(boolean is2D,
			String suffix) {
		return new SettingsModelDoubleBounded(
				getDialogManualBandwidthName(is2D, suffix), 1.0, 0.0,
				Double.MAX_VALUE);
	}

	/**
	 * @return The settings model for the Kernel Estimator option
	 */
	static SettingsModelString createKernelEstimatorModel() {
		return new SettingsModelString(KERNEL_ESTIMATOR,
				KernelEstimator.getDefault().name());
	}

	/**
	 * @return The settings model for the show legend option
	 */
	static SettingsModelBoolean createShowLegendModel() {
		return new SettingsModelBoolean(SHOW_LEGEND, true);
	}

	/**
	 * @return the model for the color used to represent the lower bound value
	 */
	static SettingsModelColor createLowerBoundColorModel() {
		return new SettingsModelColor(LOWER_BOUND, Color.white);
	}

	/**
	 * @return the model for the color used to represent the upper bound value
	 */
	static SettingsModelColor createUpperBoundColorModel() {
		return new SettingsModelColor(UPPER_BOUND, Color.black);
	}

}
