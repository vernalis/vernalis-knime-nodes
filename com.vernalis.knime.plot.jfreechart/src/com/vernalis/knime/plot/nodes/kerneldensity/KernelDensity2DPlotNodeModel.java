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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.title.Title;
import org.jfree.ui.RectangleEdge;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.property.ShapeFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColor;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleRange;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.jfc.node.heatmap.util.TwoValuePaintScale;

import com.vernalis.knime.core.collections.LimitedTreeSet;
import com.vernalis.knime.dialog.components.SettingsModelShape;
import com.vernalis.knime.jfcplot.core.drawabledataobject.DrawableDataObject;
import com.vernalis.knime.jfcplot.core.drawabledataobject.DrawableDataObjectFastPlot;
import com.vernalis.knime.jfcplot.core.drawabledataobject.SimpleShapeDrawableDataObject;
import com.vernalis.knime.jfcplot.core.legend.TitledLegend;
import com.vernalis.knime.jfcplot.core.legenditemsources.ColourLegendItemSource;

import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.NUMBER_OF_OUTLIERS;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.X_BANDWIDTH_HX;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.X_VALUES_COLUMN;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.Y;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.Y_BANDWIDTH_HY;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.Y_VALUES_COLUMN;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createAutoGenerateBandwidthModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createAutorangeModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createAxisRangeModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createContourIntervalSchemaModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createFilledContoursModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createKernelSymmModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createLowerBoundColorModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createManualBandwidthModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createNumContoursModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createNumOutliersModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createOutlierColorModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createOutlierShapeModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createOutlierSizeModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.createUpperBoundColorModel;

/**
 * NodeModel implementation for the 2D Kernel Density Plot node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class KernelDensity2DPlotNodeModel extends
		AbstractKernelDensityPlotNodeModel<KernelDensity2DDrawableDataObject> {

	// Kernel Options
	protected final SettingsModelString kernelSymmetryMdl =
			registerSettingsModel(createKernelSymmModel());
	protected final SettingsModelString autoHYMdl;
	protected final SettingsModelDoubleBounded hYMdl;

	// Outlier Options
	protected final SettingsModelIntegerBounded numOutliersMdl =
			registerSettingsModel(createNumOutliersModel());
	protected final SettingsModelIntegerBounded outlierSizeMdl =
			registerSettingsModel(createOutlierSizeModel());
	protected final SettingsModelShape outlierShapeMdl =
			registerSettingsModel(createOutlierShapeModel());
	protected final SettingsModelColor outlierColourMdl =
			registerSettingsModel(createOutlierColorModel());

	// Plot Options
	protected final SettingsModelBoolean yAutoRngMdl;
	protected final SettingsModelDoubleRange yRangeMdl;
	protected final SettingsModelColor lowBoundColorMdl =
			registerSettingsModel(createLowerBoundColorModel());
	protected final SettingsModelColor upBoundColorMdl =
			registerSettingsModel(createUpperBoundColorModel());
	protected final SettingsModelIntegerBounded numContoursMdl =
			registerSettingsModel(createNumContoursModel());
	protected final SettingsModelBoolean fillContoursMdl =
			registerSettingsModel(createFilledContoursModel());
	protected final SettingsModelString contourIntervalSchemaMdl =
			registerSettingsModel(createContourIntervalSchemaModel());

	protected BandwidthEstimationMethod xBandWidthEstimator,
			yBandWidthEstimator;
	protected KernelEstimator kernelEstimator;
	protected KernelSymmetry kernelSymmetry;
	protected ContourIntervalSchema contourSchema;
	private final int numDimensions = 2;
	private double[] contourIntervals;

	public KernelDensity2DPlotNodeModel() {
		this(new String[] { X_VALUES_COLUMN, Y_VALUES_COLUMN });
	}

	public KernelDensity2DPlotNodeModel(String[] columnSelectorNames) {
		this(columnSelectorNames, true);
	}

	public KernelDensity2DPlotNodeModel(String[] columnSelectorNames,
			boolean hasAutorangeAxes) {
		this(columnSelectorNames, null, hasAutorangeAxes);
	}

	public KernelDensity2DPlotNodeModel(String[] columnSelectorNames,
			Pattern[] colNamePatterns, boolean hasAutorangeAxes) {
		super(columnSelectorNames, colNamePatterns, hasAutorangeAxes);

		autoHYMdl = registerSettingsModel(
				createAutoGenerateBandwidthModel(columnSelectorNames[1]));
		hYMdl = registerSettingsModel(createManualBandwidthModel(true, Y));
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

		if (hasAutorangeAxes) {
			yRangeMdl = registerSettingsModel(createAxisRangeModel(Y));
			yAutoRngMdl = registerSettingsModel(createAutorangeModel(Y));
			yRangeMdl.setEnabled(!yAutoRngMdl.getBooleanValue());
			yAutoRngMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					yRangeMdl.setEnabled(!yAutoRngMdl.getBooleanValue());

				}
			});
		} else {
			yRangeMdl = null;
			yAutoRngMdl = null;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doReset() {

	}

	@Override
	public void doConfigure(DataTableSpec inSpec)
			throws InvalidSettingsException {
		super.doConfigure(inSpec);
		yBandWidthEstimator =
				BandwidthEstimationMethod.valueOf(autoHYMdl.getStringValue());
		kernelSymmetry =
				KernelSymmetry.valueOf(kernelSymmetryMdl.getStringValue());
		contourSchema = ContourIntervalSchema
				.valueOf(contourIntervalSchemaMdl.getStringValue());
		pushFlowVariableDouble(X_BANDWIDTH_HX, 0.0);
		pushFlowVariableDouble(Y_BANDWIDTH_HY, 0.0);
		pushFlowVariableInt(NUMBER_OF_OUTLIERS, -1);
	}

	@Override
	protected DrawableDataObjectFastPlot createPlotFromChartData(
			ChartData<DrawableDataObject> dataPoints,
			AttributedString xAxisLabel, AttributedString yAxisLabel) {
		// No margin
		return new DrawableDataObjectFastPlot(dataPoints.getData(), xAxisLabel,
				dataPoints.getxDss().getMin(), dataPoints.getxDss().getMax(),
				yAxisLabel, dataPoints.getyDss().getMin(),
				dataPoints.getyDss().getMax());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.jfcplot.core.nodes.
	 * AbstractDrawableSeriesPlotNodeModel#getXAxisLabel()
	 */
	@Override
	protected AttributedString getXAxisLabel(
			ChartData<? extends DrawableDataObject> chartData) {
		return new AttributedString(
				columnNameMdls[0]
						.getStringValue()
						+ (showHonAxesMdl.getBooleanValue()
								? String.format(" (Hx=%.2f)",
										chartData.getData().stream()
												.filter(x -> x instanceof KernelDensityDrawableDataObject)
												.map(x -> (KernelDensityDrawableDataObject) x)
												.findFirst().get().getKernel()
												.getBandwidthMatrix()[0][0])
								: ""));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.jfcplot.core.nodes.
	 * AbstractDrawableSeriesPlotNodeModel#getYAxisLabel()
	 */
	@Override
	protected AttributedString getYAxisLabel(
			ChartData<? extends DrawableDataObject> chartData) {
		return new AttributedString(
				columnNameMdls[1]
						.getStringValue()
						+ (showHonAxesMdl.getBooleanValue()
								? String.format(" (Hy=%.2f)",
										chartData.getData().stream()
												.filter(x -> x instanceof KernelDensityDrawableDataObject)
												.map(x -> (KernelDensityDrawableDataObject) x)
												.findFirst().get().getKernel()
												.getBandwidthMatrix()[1][1])
								: ""));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.jfcplot.core.nodes.
	 * AbstractDrawableSeriesPlotNodeModel#createLegendsFromInputPort(org.knime.
	 * core.data.DataTableSpec, java.util.Collection, int, int, int,
	 * org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected Collection<? extends Title> createLegendsFromInputPort(
			DataTableSpec tableSpec,
			Collection<KernelDensity2DDrawableDataObject> dataFromTable,
			int colorcolumn, int shapecolumn, int sizecolumn,
			ExecutionContext exec) throws CanceledExecutionException {
		if (showLegendMdl.getBooleanValue()) {
			Iterator<KernelDensity2DDrawableDataObject> iter =
					dataFromTable.iterator();
			if (!iter.hasNext()) {
				return Collections.emptySet();
			}
			// There should be 1 kernel only in 2D case
			KernelDensity2DDrawableDataObject kernelDdo =
					dataFromTable.iterator().next();

			final PaintScale paintScale = kernelDdo.getPaintScale();
			// creating the paint legend

			if (numContoursMdl.getIntValue() == 0
					|| fillContoursMdl.getBooleanValue()) {
				// creating axes for the scale
				NumberAxis scaleAxis = new NumberAxis("Kernel Density");
				scaleAxis.setRange(0.0,
						kernelDdo.getKernel().getMaximumIntensity());

				PaintScaleLegend psLegend =
						new PaintScaleLegend(paintScale, scaleAxis);
				psLegend.setAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
				// offset between the axis and the paint strip
				psLegend.setAxisOffset(5D);
				// setting the width of the color strip bar
				psLegend.setStripWidth(10D);
				// position of the paint legend on the final image
				psLegend.setPosition(RectangleEdge.RIGHT);
				// set the padding around the legend
				psLegend.setPadding(5D, 5D, 40D, 5D);
				return Collections.singleton(psLegend);
			} else {
				return Collections.singleton(new TitledLegend("Contours",
						new ColourLegendItemSource(
								Arrays.stream(contourIntervals).boxed().collect(
										LinkedHashMap<String, Color>::new,
										(m, d) -> m.put(
												String.format("%.3e", d),
												(Color) paintScale.getPaint(d)),
										(m, u) -> {
										}),
								10.0, ShapeFactory.getShape(
										ShapeFactory.HORIZONTAL_LINE))));

			}
		} else {
			return Collections.emptySet();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.jfcplot.core.nodes.
	 * AbstractDrawableSeriesPlotNodeModel#addDataSeriesFromInputPort(org.knime.
	 * core.node.BufferedDataTable, int, int, int,
	 * com.vernalis.knime.internal.jfcplot.core.nodes.
	 * AbstractDrawableSeriesPlotNodeModel.ChartData,
	 * org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected void addDataSeriesFromInputPort(BufferedDataTable table,
			int colorcolumn, int shapecolumn, int sizecolumn,
			ChartData<KernelDensity2DDrawableDataObject> chartData,
			ExecutionContext exec)
			throws CanceledExecutionException, Exception {

		xBandWidthEstimator =
				BandwidthEstimationMethod.valueOf(autoHXMdl.getStringValue());
		yBandWidthEstimator =
				BandwidthEstimationMethod.valueOf(autoHYMdl.getStringValue());
		kernelEstimator =
				KernelEstimator.valueOf(kernelEstimatorMdl.getStringValue());
		kernelSymmetry =
				KernelSymmetry.valueOf(kernelSymmetryMdl.getStringValue());
		contourSchema = ContourIntervalSchema
				.valueOf(contourIntervalSchemaMdl.getStringValue());

		KernelDensityFunction kd = getKernelDensity();

		final long numRows = table.size();
		double progPerRow = 1.0 / numRows;
		long currRow = 0L;

		int numOutliers = (int) (numOutliersMdl.getIntValue() == 100
				? table.size()
				: (numOutliersMdl.getIntValue() * table.size() / 100.0));
		exec.setMessage("Generating kernel");
		ExecutionContext exec0 = exec.createSubExecutionContext(1.0 / (2.0
				+ gridSizeMdl.getIntValue() * gridSizeMdl.getIntValue()));
		for (DataRow row : table) {
			exec0.checkCanceled();
			exec0.setProgress(currRow * progPerRow,
					"Read " + (currRow++) + " of " + numRows);
			DataCell xCell = row.getCell(colIdx[0]);
			DataCell yCell = row.getCell(colIdx[1]);
			if (xCell.isMissing() || yCell.isMissing()) {
				continue;
			}
			kd.acceptPoint(((DoubleValue) xCell).getDoubleValue(),
					((DoubleValue) yCell).getDoubleValue());
		}

		if (xAutoRngMdl != null && !xAutoRngMdl.getBooleanValue()) {
			kd.setKernelRange(0, xRangeMdl.getMinRange(),
					xRangeMdl.getMaxRange());
		}
		if (yAutoRngMdl != null && !yAutoRngMdl.getBooleanValue()) {
			kd.setKernelRange(1, yRangeMdl.getMinRange(),
					yRangeMdl.getMaxRange());
		}
		exec.setMessage("Generating Kernel Grid");
		ExecutionContext exec1 = exec.createSubExecutionContext(
				gridSizeMdl.getIntValue() * gridSizeMdl.getIntValue()
						/ (2.0 + gridSizeMdl.getIntValue()
								* gridSizeMdl.getIntValue()));
		kd.generateKDEGrid(getKernelRangeFactor(), exec1);

		// Now look for the outliers
		Set<SimpleShapeDrawableDataObject> outliers = new LimitedTreeSet<>(
				numOutliers, new Comparator<SimpleShapeDrawableDataObject>() {

					@Override
					public int compare(SimpleShapeDrawableDataObject o1,
							SimpleShapeDrawableDataObject o2) {
						// We are going to use the size store the
						// intensity of the kernel at the point.
						// We need the points with the lowest
						// intensities
						return Double.compare(o1.getSize(), o2.getSize());
					}
				});

		exec.setMessage("Finding outliers");
		ExecutionContext exec2 = exec.createSubExecutionContext(1.0 / (2.0
				+ gridSizeMdl.getIntValue() * gridSizeMdl.getIntValue()));
		currRow = 0;
		if (numOutliers > 0) {
			for (DataRow row : table) {
				exec2.checkCanceled();
				exec2.setProgress(currRow * progPerRow,
						"Read " + (currRow++) + " of " + numRows);
				DataCell xCell = row.getCell(colIdx[0]);
				DataCell yCell = row.getCell(colIdx[1]);
				if (xCell.isMissing() || yCell.isMissing()) {
					continue;
				}
				double x = ((DoubleValue) xCell).getDoubleValue();
				double y = ((DoubleValue) yCell).getDoubleValue();
				outliers.add(new SimpleShapeDrawableDataObject(x, y,
						outlierShapeMdl.getShapeValue(),
						kd.getDensityAtPoint(x, y),
						outlierColourMdl.getColorValue(), null, null, null));
			}
		} else {
			exec2.setProgress(1.0);
		}

		// Change the sizes of the outliers to the size for
		// drawing
		outliers = outliers.stream()
				.map(x -> new SimpleShapeDrawableDataObject(x.getX(), x.getY(),
						x.getShape(), outlierSizeMdl.getIntValue(),
						x.getColour(), null, null, null))
				.collect(Collectors.toSet());

		final PaintScale paintScale = createPaintScale(kd);
		chartData
				.addData(getKernelDrawableDataObject(kd, outliers, paintScale));

		pushFlowVariableDouble(X_BANDWIDTH_HX, kd.getBandwidthMatrix()[0][0]);
		pushFlowVariableDouble(Y_BANDWIDTH_HY, kd.getBandwidthMatrix()[1][1]);
		pushFlowVariableInt(NUMBER_OF_OUTLIERS, outliers.size());
	}

	/**
	 * @return
	 */
	protected double getKernelRangeFactor() {
		return 0.05;
	}

	/**
	 * Method to wrap the {@link KernelDensityFunction} in the appropriate
	 * {@link KernelDensity2DDrawableDataObject} according to the contours
	 * setting
	 * 
	 * @param kd
	 *            The kernel
	 * @param outliers
	 *            The outliers
	 * @param paintScale
	 *            The paintscale
	 * @return The {@link KernelDensity2DDrawableDataObject}
	 */
	protected KernelDensity2DDrawableDataObject getKernelDrawableDataObject(
			KernelDensityFunction kd,
			Collection<? extends DrawableDataObject> outliers,
			final PaintScale paintScale) {
		return numContoursMdl.getIntValue() == 0
				|| fillContoursMdl.getBooleanValue()
						? new KernelDensity2DDrawableDataObject(kd, outliers,
								paintScale)
						: new KernelDensity2DContourLinesDrawableDataObject(kd,
								outliers, (LookupPaintScale) paintScale);
	}

	/**
	 * @return The empty {@link KernelDensityFunction} for datapoints from the
	 *         incoming table to be added to
	 */
	protected KernelDensityFunction getKernelDensity() {
		return new KernelDensityFunction(numDimensions,
				gridSizeMdl.getIntValue(), kernelEstimator, kernelSymmetry,
				new BandwidthEstimationMethod[] { xBandWidthEstimator,
						yBandWidthEstimator },
				new Double[] {
						xBandWidthEstimator.needsManualValue()
								? hXMdl.getDoubleValue() : null,
						yBandWidthEstimator.needsManualValue()
								? hYMdl.getDoubleValue() : null });
	}

	/**
	 * @param kd
	 *            The kernel
	 * @return The {@link PaintScale}
	 */
	protected PaintScale createPaintScale(KernelDensityFunction kd) {
		// All schema are based on a continuous colour spectrum
		final TwoValuePaintScale referencePaintScale = new TwoValuePaintScale(
				0.0, lowBoundColorMdl.getColorValue(), kd.getMaximumIntensity(),
				upBoundColorMdl.getColorValue());

		if (numContoursMdl.getIntValue() == 0) {
			// Continuous spectrum - job done
			return referencePaintScale;
		} else {
			// We have contours, so need a LookupPaintScale
			contourIntervals = getContourIntervals(kd);
			LookupPaintScale ps = new LookupPaintScale(0.0,
					kd.getMaximumIntensity(), new Color(255, 255, 255, 255));
			for (double contour : contourIntervals) {
				ps.add(contour, referencePaintScale.getPaint(contour));
			}
			return ps;
		}
	}

	/**
	 * @param kernel
	 *            The kernel
	 * @return The countour intervals
	 */
	protected double[] getContourIntervals(KernelDensityFunction kernel) {
		if (contourSchema == ContourIntervalSchema.QUANTILE) {
			int interval = (int) (1.0
					+ (gridSizeMdl.getIntValue() * gridSizeMdl.getIntValue()
							/ (double) numContoursMdl.getIntValue()));
			double[] sortedZ = kernel.getGridPoints().stream()
					.mapToDouble(
							pt -> pt.getCoordinate(kernel.getNumDimensions()))
					.sorted().toArray();
			return IntStream.range(0, sortedZ.length)
					.filter(x -> x % interval == 0).mapToDouble(x -> sortedZ[x])
					.toArray();
		}
		return contourSchema.getContourIntervals(0.0,
				kernel.getMaximumIntensity(), numContoursMdl.getIntValue());
	}

}
