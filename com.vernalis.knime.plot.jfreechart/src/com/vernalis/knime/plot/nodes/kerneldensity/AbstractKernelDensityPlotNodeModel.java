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

import java.util.regex.Pattern;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleRange;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.jfcplot.core.nodes.AbstractDrawableSeriesPlotNodeModel;
import com.vernalis.knime.misc.ArrayUtils;

import static com.vernalis.knime.jfcplot.core.nodes.AbstractDrawableSeriesPlotNodeDialogPane.DOUBLE_VALUE_COLUMN_FILTER;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.X;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createAutoGenerateBandwidthModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createAutorangeModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createAxisRangeModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createGridSizeModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createKernelEstimatorModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createManualBandwidthModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createShowHValuesModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createShowLegendModel;

/**
 * Abstract Base Node Model implementation for Kernel Plots, handling 1D and 2D
 * cases
 * 
 * @author S.Roughley knime@vernalis.com
 *
 * @param <T>
 *            The type of the {@link KernelDensity1DDrawableDataObject}
 */
public abstract class AbstractKernelDensityPlotNodeModel<T extends KernelDensityDrawableDataObject>
		extends AbstractDrawableSeriesPlotNodeModel<T> {

	// Kernel Options
	protected final SettingsModelString kernelEstimatorMdl =
			registerSettingsModel(createKernelEstimatorModel());
	protected final SettingsModelString autoHXMdl;
	protected final SettingsModelDoubleBounded hXMdl;

	// Plot Options
	protected final SettingsModelBoolean xAutoRngMdl;
	protected final SettingsModelDoubleRange xRangeMdl;
	protected final SettingsModelIntegerBounded gridSizeMdl;
	protected final SettingsModelBoolean showHonAxesMdl;
	protected final SettingsModelBoolean showLegendMdl =
			registerSettingsModel(createShowLegendModel());

	protected BandwidthEstimationMethod xBandWidthEstimator;
	protected KernelEstimator kernelEstimator;

	/**
	 * Overloaded constructor with no regex patterns and auto-range axes
	 * settings
	 * 
	 * @param columnSelectorNames
	 *            The names of the column selectors
	 */
	public AbstractKernelDensityPlotNodeModel(String[] columnSelectorNames) {
		this(columnSelectorNames, true);
	}

	/**
	 * Overloaded constructor for the node model. This default constructor
	 * creates a node with one input port and one output image port. The node
	 * has no regex patterns for the column selectors.
	 * 
	 * @param columnLabels
	 *            The names of the column selectors
	 * @param hasAutorangeAxes
	 *            Should the node have the auto-range axes settings?
	 */
	public AbstractKernelDensityPlotNodeModel(String[] columnSelectorNames,
			boolean hasAutorangeAxes) {
		this(columnSelectorNames, null, hasAutorangeAxes);
	}

	/**
	 * Full constructor for the node model. This default constructor creates a
	 * node with one input port and one output image port. The node has regex
	 * patterns for the column selectors to guess with
	 * 
	 * @param columnSelectorNames
	 *            The names of the column selectors
	 * @param colNamePatterns
	 *            The preferred regex patterns. If this is {@code null}, or
	 *            contains and {@code null} values, they will be replaced with
	 *            the catch-all {@code .*} regular expression
	 * @param hasAutorangeAxes
	 *            Should the node have the auto-range axes settings?
	 */
	public AbstractKernelDensityPlotNodeModel(String[] columnSelectorNames,
			Pattern[] colNamePatterns, boolean hasAutorangeAxes) {
		super(columnSelectorNames, ArrayUtils.of(DOUBLE_VALUE_COLUMN_FILTER,
				columnSelectorNames.length), colNamePatterns);

		final boolean is2D = columnSelectorNames.length > 1;

		autoHXMdl = registerSettingsModel(
				createAutoGenerateBandwidthModel(columnSelectorNames[0]));
		hXMdl = registerSettingsModel(createManualBandwidthModel(is2D, X));
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

		gridSizeMdl = registerSettingsModel(createGridSizeModel(is2D));
		showHonAxesMdl = registerSettingsModel(createShowHValuesModel(is2D));

		if (hasAutorangeAxes) {
			xAutoRngMdl = registerSettingsModel(createAutorangeModel(X));
			xRangeMdl = registerSettingsModel(createAxisRangeModel(X));
			xRangeMdl.setEnabled(!xAutoRngMdl.getBooleanValue());
			xAutoRngMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					xRangeMdl.setEnabled(!xAutoRngMdl.getBooleanValue());

				}
			});
		} else {
			xRangeMdl = null;
			xAutoRngMdl = null;
		}

	}

	@Override
	public void doConfigure(DataTableSpec inSpec)
			throws InvalidSettingsException {
		super.doConfigure(inSpec);
		xBandWidthEstimator =
				BandwidthEstimationMethod.valueOf(autoHXMdl.getStringValue());
		kernelEstimator =
				KernelEstimator.valueOf(kernelEstimatorMdl.getStringValue());
	}

}
