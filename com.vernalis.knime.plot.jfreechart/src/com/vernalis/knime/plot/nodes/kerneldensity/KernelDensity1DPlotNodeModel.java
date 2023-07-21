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
import java.text.AttributedString;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.title.Title;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.property.ColorAttr;
import org.knime.core.data.property.ColorModel;
import org.knime.core.data.property.ColorModelNominal;
import org.knime.core.data.property.ShapeFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.jfcplot.core.drawabledataobject.DrawableDataObject;
import com.vernalis.knime.jfcplot.core.drawabledataobject.DrawableDataObjectFastPlot;
import com.vernalis.knime.jfcplot.core.legend.TitledLegend;
import com.vernalis.knime.jfcplot.core.legenditemsources.ColourLegendItemSource;

import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.BANDWIDTH_H;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.VALUES_COLUMN;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createExpandKernelsModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createGroupColNameModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createShowTotalDataKernelModel;

/**
 * NodeModel implementation for the 1D Kernel Density Plot node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class KernelDensity1DPlotNodeModel extends
		AbstractKernelDensityPlotNodeModel<KernelDensity1DDrawableDataObject> {

	private final SettingsModelString groupColNameMdl =
			registerSettingsModel(createGroupColNameModel());
	private final SettingsModelBoolean totalDataMdl =
			registerSettingsModel(createShowTotalDataKernelModel());
	private final SettingsModelBoolean expandKernelsMdl =
			registerSettingsModel(createExpandKernelsModel());

	private int groupColIdx;

	/**
	 * Constructor
	 */
	public KernelDensity1DPlotNodeModel() {
		super(new String[] { VALUES_COLUMN });
		groupColNameMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				showHonAxesMdl
						.setEnabled(groupColNameMdl.getStringValue() == null
								|| groupColNameMdl.getStringValue().isEmpty()
								|| (showLegendMdl.isEnabled()
										&& showLegendMdl.getBooleanValue()));
				showLegendMdl
						.setEnabled(groupColNameMdl.getStringValue() != null
								&& !groupColNameMdl.getStringValue().isEmpty());
				totalDataMdl.setEnabled(groupColNameMdl.getStringValue() != null
						&& !groupColNameMdl.getStringValue().isEmpty());
				expandKernelsMdl
						.setEnabled(groupColNameMdl.getStringValue() != null
								&& !groupColNameMdl.getStringValue().isEmpty());
			}
		});
		showHonAxesMdl.setEnabled(groupColNameMdl.getStringValue() == null
				|| groupColNameMdl.getStringValue().isEmpty()
				|| showLegendMdl.getBooleanValue());
		showLegendMdl.setEnabled(groupColNameMdl.getStringValue() != null
				&& !groupColNameMdl.getStringValue().isEmpty());
		totalDataMdl.setEnabled(groupColNameMdl.getStringValue() != null
				&& !groupColNameMdl.getStringValue().isEmpty());
		expandKernelsMdl.setEnabled(groupColNameMdl.getStringValue() != null
				&& !groupColNameMdl.getStringValue().isEmpty());
	}

	@Override
	public void doConfigure(DataTableSpec inSpec)
			throws InvalidSettingsException {
		super.doConfigure(inSpec);
		// Now the optional grouping column
		if (groupColNameMdl.getStringValue() != null
				&& !"".equals(groupColNameMdl.getStringValue())) {

			groupColIdx =
					inSpec.findColumnIndex(groupColNameMdl.getStringValue());
		} else {
			// All are in single group - no column was selected
			groupColIdx = -1;
		}
		if (groupColNameMdl.getStringValue() == null) {
			// We can only do this at configure if we dont have a group column
			pushFlowVariableDouble(
					BANDWIDTH_H + " " + columnNameMdls[0].getStringValue(),
					0.0);
		}

	}

	@Override
	protected DrawableDataObjectFastPlot createPlotFromChartData(
			ChartData<DrawableDataObject> dataPoints,
			AttributedString xAxisLabel, AttributedString yAxisLabel) {
		// We add a 5% margin to the y axis
		final double yMin = Math.min(0.0, dataPoints.getyDss().getMin());
		double margin = 0.05 * (dataPoints.getyDss().getMax() - yMin);
		return new DrawableDataObjectFastPlot(dataPoints.getData(), xAxisLabel,
				dataPoints.getxDss().getMin(), dataPoints.getxDss().getMax(),
				yAxisLabel, yMin - margin,
				dataPoints.getyDss().getMax() + margin);
	}

	@Override
	protected AttributedString
			getXAxisLabel(ChartData<? extends DrawableDataObject> chartData) {
		return new AttributedString(columnNameMdls[0].getStringValue()
				+ (showHonAxesMdl.getBooleanValue() && groupColNameMdl
						.getStringValue() == null ? String.format(
								" (H=%.2f)",
								chartData.getData().stream().filter(
										x -> x instanceof KernelDensityDrawableDataObject)
										.map(x -> (KernelDensityDrawableDataObject) x)
										.findFirst().get().getKernel()
										.getBandwidthMatrix()[0][0])
								: ""));
	}

	@Override
	protected AttributedString
			getYAxisLabel(ChartData<? extends DrawableDataObject> chartData) {
		return new AttributedString("Kernel Intensity");
	}

	@Override
	protected Collection<? extends Title> createLegendsFromInputPort(
			DataTableSpec tableSpec,
			Collection<KernelDensity1DDrawableDataObject> dataFromTable,
			int colorcolumn, int shapecolumn, int sizecolumn,
			ExecutionContext exec) throws CanceledExecutionException {

		if (groupColNameMdl.getStringValue() != null
				&& showLegendMdl.getBooleanValue()) {
			return Collections.singleton(new TitledLegend(
					groupColNameMdl.getStringValue(),
					new ColourLegendItemSource(dataFromTable.stream().collect(
							/*
							 * Use this rather than Collectors.toMap() to retain
							 * order
							 */
							LinkedHashMap<String, Color>::new,
							(m, kd) -> m.put(kd.getId(), kd.getColour()),
							(m, u) -> {
							}), 10.0, ShapeFactory
									.getShape(ShapeFactory.HORIZONTAL_LINE))));
		} else {
			return Collections.emptySet();
		}
	}

	@Override
	protected void addDataSeriesFromInputPort(BufferedDataTable table,
			int colorcolumn, int shapecolumn, int sizecolumn,
			ChartData<KernelDensity1DDrawableDataObject> chartData,
			ExecutionContext exec)
			throws CanceledExecutionException, Exception {
		xBandWidthEstimator =
				BandwidthEstimationMethod.valueOf(autoHXMdl.getStringValue());
		kernelEstimator =
				KernelEstimator.valueOf(kernelEstimatorMdl.getStringValue());

		Map<DataCell, KernelDensityFunction> kernels =
				new TreeMap<>(new Comparator<DataCell>() {

					@Override
					public int compare(DataCell o1, DataCell o2) {

						return ((StringValue) o1).getStringValue()
								.compareTo(((StringValue) o2).getStringValue());
					}
				});

		KernelDensityFunction totalKernel =
				totalDataMdl.isEnabled() && totalDataMdl.getBooleanValue()
						? new KernelDensityFunction(gridSizeMdl.getIntValue(),
								kernelEstimator, xBandWidthEstimator,
								xBandWidthEstimator.needsManualValue()
										? hXMdl.getDoubleValue()
										: null)
						: null;

		final long numRows = table.size();
		double progPerRow = 1.0 / numRows;
		long currRow = 0L;

		exec.setMessage("Reading data table");
		ExecutionContext exec0 = exec.createSubExecutionContext(
				1.0 / (1.0 + gridSizeMdl.getIntValue()));
		for (DataRow row : table) {
			exec0.checkCanceled();
			exec0.setProgress(currRow * progPerRow,
					"Read +" + (currRow++) + " of " + numRows);
			DataCell valueCell = row.getCell(colIdx[0]);
			if (valueCell.isMissing()) {
				continue;
			}

			if (totalKernel != null) {
				// Will include datapoints with missing value in group column
				totalKernel.acceptPoint(
						((DoubleValue) valueCell).getDoubleValue());
			}

			DataCell groupCell = groupColIdx < 0
					? new StringCell(columnNameMdls[0].getStringValue())
					: row.getCell(groupColIdx);
			if (groupCell.isMissing()) {
				continue;
			}

			kernels.computeIfAbsent(groupCell,
					k -> new KernelDensityFunction(gridSizeMdl.getIntValue(),
							kernelEstimator, xBandWidthEstimator,
							xBandWidthEstimator.needsManualValue()
									? hXMdl.getDoubleValue()
									: null))
					.acceptPoint(((DoubleValue) valueCell).getDoubleValue());

		}

		if (!xAutoRngMdl.getBooleanValue()) {
			kernels.values().forEach(kd -> kd.setKernelRange(0,
					xRangeMdl.getMinRange(), xRangeMdl.getMaxRange()));
			if (totalKernel != null) {
				totalKernel.setKernelRange(0, xRangeMdl.getMinRange(),
						xRangeMdl.getMaxRange());
			}
		} else if (expandKernelsMdl.isEnabled()
				&& expandKernelsMdl.getBooleanValue()) {
			DoubleSummaryStatistics totalRange = new DoubleSummaryStatistics();
			kernels.values().forEach(kd -> {
				kd.autoRangeKernel(0.05);
				totalRange.accept(kd.getRangeMin(0));
				totalRange.accept(kd.getRangeMax(0));
			});
			if (totalKernel != null) {
				totalKernel.autoRangeKernel(0.05);
				totalRange.accept(totalKernel.getRangeMin(0));
				totalRange.accept(totalKernel.getRangeMax(0));
			}
			kernels.values().forEach(kd -> kd.setKernelRange(0,
					totalRange.getMin(), totalRange.getMax()));
			if (totalKernel != null) {
				totalKernel.setKernelRange(0, totalRange.getMin(),
						totalRange.getMax());
			}
		}

		// If the grouping column doesnt provide a color model, then we make
		// one up...
		ColorModel colorModel =
				colorcolumn < 0 || colorcolumn != groupColIdx ? null
						: table.getDataTableSpec().getColumnSpec(colorcolumn)
								.getColorHandler().getColorModel();
		if (colorModel == null) {
			// Generate a colour model on the fly
			exec.setMessage("Creating group colours");
			colorModel = createColorMapping(kernels.keySet(), false);
		}

		exec.setMessage("Generating kernel grids");
		// Do the total first so that it appears at the top of the legend.
		// Others are in alphabetical order
		if (totalKernel != null) {
			ExecutionContext exec1 = exec.createSubExecutionContext(
					gridSizeMdl.getIntValue() / ((kernels.size() + 1)
							* (1.0 + gridSizeMdl.getIntValue())));
			totalKernel.generateKDEGrid(0.05, exec1);
			chartData.addData(new KernelDensity1DDrawableDataObject(totalKernel,
					Color.BLACK,
					groupColNameMdl.getStringValue()
							+ (showHonAxesMdl.getBooleanValue()
									? String.format(" (Total; %.3f)",
											totalKernel
													.getBandwidthMatrix()[0][0])
									: " (Total)")));
		}
		for (Entry<DataCell, KernelDensityFunction> kd : kernels.entrySet()) {
			ExecutionContext exec1 = exec.createSubExecutionContext(
					gridSizeMdl.getIntValue() / ((kernels.size() + 1)
							* (1.0 + gridSizeMdl.getIntValue())));
			kd.getValue().generateKDEGrid(0.05, exec1);
			final String id = ((StringValue) kd.getKey()).getStringValue();
			pushFlowVariableDouble(BANDWIDTH_H + " " + id,
					kd.getValue().getBandwidthMatrix()[0][0]);
			chartData.addData(
					new KernelDensity1DDrawableDataObject(kd.getValue(),
							colorModel.getColorAttr(kd.getKey()).getColor(),
							id + (showHonAxesMdl.getBooleanValue()
									? String.format(" (%.3f)",
											kd.getValue()
													.getBandwidthMatrix()[0][0])
									: "")));
		}

	}

	/**
	 * Create default color mapping for the given set of possible
	 * <code>DataCell</code> values.
	 * 
	 * @param set
	 *            possible values
	 * @param useGreyScale
	 *            Should the colour palette be a greyscale pallete?
	 * 
	 * @return a map of possible value to color
	 */
	final ColorModelNominal createColorMapping(
			final Set<? extends DataCell> set, boolean useGreyScale) {
		if (set == null || set.isEmpty()) {
			return new ColorModelNominal(Collections.emptyMap(),
					new ColorAttr[0]);
		}

		Map<DataCell, ColorAttr> map = new LinkedHashMap<>();
		if (set.size() == 1) {
			// Single series, so probably no grouping column, but either way we
			// want it black - otherwise it becomes red!
			map.put(set.stream().findFirst().get(),
					ColorAttr.getInstance(Color.BLACK));
		} else {
			int idx = 0;
			for (DataCell cell : set) {
				// use Color, half saturated, half bright for base color
				float value = (float) idx++ / (float) set.size();
				map.put(cell, ColorAttr.getInstance(useGreyScale
						? new Color(value, value, value)
						: new Color(Color.HSBtoRGB(value, 1.0f, 1.0f))));
			}
		}

		return new ColorModelNominal(map,
				map.values().toArray(ColorAttr[]::new));
	}

}
