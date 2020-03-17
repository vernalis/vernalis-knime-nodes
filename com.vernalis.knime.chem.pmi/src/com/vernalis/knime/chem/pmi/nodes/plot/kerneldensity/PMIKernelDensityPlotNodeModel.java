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
package com.vernalis.knime.chem.pmi.nodes.plot.kerneldensity;

import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.Collection;
import java.util.regex.Pattern;

import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColor;

import com.vernalis.knime.chem.pmi.nodes.plot.abstrct.PMITriangleDrawableDataObjectFastPlot;
import com.vernalis.knime.jfcplot.core.drawabledataobject.DrawableDataObject;
import com.vernalis.knime.jfcplot.core.drawabledataobject.DrawableDataObjectFastPlot;
import com.vernalis.knime.plot.nodes.kerneldensity.BandwidthEstimationMethod;
import com.vernalis.knime.plot.nodes.kerneldensity.KernelDensity2DDrawableDataObject;
import com.vernalis.knime.plot.nodes.kerneldensity.KernelDensity2DPlotNodeModel;
import com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityDrawableDataObject;
import com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityFunction;

import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.N_PMI1_I1_I3_COLUMN;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.N_PMI2_I2_I3_COLUMN;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.createShowFullTriangleModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.getTriangleColourModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.getVertexColourModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesPlotNodeModel.NPR1_PATTERN;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesPlotNodeModel.NPR2_PATTERN;

public class PMIKernelDensityPlotNodeModel
		extends KernelDensity2DPlotNodeModel {

	protected final SettingsModelColor m_vertexColour =
			registerSettingsModel(getVertexColourModel());
	protected final SettingsModelColor m_triangleColour =
			registerSettingsModel(getTriangleColourModel());
	protected final SettingsModelBoolean showFullTriangleModel =
			registerSettingsModel(createShowFullTriangleModel());

	public PMIKernelDensityPlotNodeModel() {
		super(new String[] { N_PMI1_I1_I3_COLUMN, N_PMI2_I2_I3_COLUMN },
				new Pattern[] { NPR1_PATTERN, NPR2_PATTERN }, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.pmi.nodes.plot.kerneldensity.
	 * KernelDensity2DPlotNodeModel#getKernelRangeFactor()
	 */
	@Override
	protected double getKernelRangeFactor() {
		return showFullTriangleModel.getBooleanValue() ? 0.0 : 0.05;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.pmi.nodes.plot.kerneldensity.
	 * KernelDensity2DPlotNodeModel#getXAxisLabel(com.vernalis.knime.internal.
	 * jfcplot.core.nodes.AbstractDrawableSeriesPlotNodeModel.ChartData)
	 */
	@Override
	protected AttributedString getXAxisLabel(
			ChartData<? extends DrawableDataObject> chartData) {
		AttributedString xAxisLabel =
				new AttributedString(
						"I1/I3" + (showHonAxesMdl.getBooleanValue()
								? String.format(" (Hx=%.2f)",
										chartData.getData().stream()
												.filter(x -> x instanceof KernelDensityDrawableDataObject)
												.map(x -> (KernelDensityDrawableDataObject) x)
												.findFirst().get().getKernel()
												.getBandwidthMatrix()[0][0])
								: ""));
		xAxisLabel.addAttribute(TextAttribute.SUPERSCRIPT,
				TextAttribute.SUPERSCRIPT_SUB, 1, 2);
		xAxisLabel.addAttribute(TextAttribute.SUPERSCRIPT,
				TextAttribute.SUPERSCRIPT_SUB, 4, 5);
		return xAxisLabel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.pmi.nodes.plot.kerneldensity.
	 * KernelDensity2DPlotNodeModel#getYAxisLabel(com.vernalis.knime.internal.
	 * jfcplot.core.nodes.AbstractDrawableSeriesPlotNodeModel.ChartData)
	 */
	@Override
	protected AttributedString getYAxisLabel(
			ChartData<? extends DrawableDataObject> chartData) {
		AttributedString yAxisLabel =
				new AttributedString(
						"I2/I3" + (showHonAxesMdl.getBooleanValue()
								? String.format(" (Hx=%.2f)",
										chartData.getData().stream()
												.filter(x -> x instanceof KernelDensityDrawableDataObject)
												.map(x -> (KernelDensityDrawableDataObject) x)
												.findFirst().get().getKernel()
												.getBandwidthMatrix()[1][1])
								: ""));
		yAxisLabel.addAttribute(TextAttribute.SUPERSCRIPT,
				TextAttribute.SUPERSCRIPT_SUB, 1, 2);
		yAxisLabel.addAttribute(TextAttribute.SUPERSCRIPT,
				TextAttribute.SUPERSCRIPT_SUB, 4, 5);
		return yAxisLabel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.pmi.nodes.plot.kerneldensity.
	 * KernelDensity2DPlotNodeModel#getKernelDensity()
	 */
	@Override
	protected KernelDensityFunction getKernelDensity() {
		KernelDensityFunction kd = new PmiKernelDensityFunction(
				gridSizeMdl.getIntValue(), kernelEstimator, kernelSymmetry,
				new BandwidthEstimationMethod[] { xBandWidthEstimator,
						yBandWidthEstimator },
				new Double[] {
						xBandWidthEstimator.needsManualValue()
								? hXMdl.getDoubleValue() : null,
						yBandWidthEstimator.needsManualValue()
								? hYMdl.getDoubleValue() : null },
				showFullTriangleModel.getBooleanValue());
		return kd;
	}

	/**
	 * @param kd
	 * @param outliers
	 * @param paintScale
	 * @return
	 */
	@Override
	protected KernelDensity2DDrawableDataObject getKernelDrawableDataObject(
			KernelDensityFunction kd,
			Collection<? extends DrawableDataObject> outliers,
			final PaintScale paintScale) {
		return numContoursMdl.getIntValue() == 0
				|| fillContoursMdl.getBooleanValue()
						? new PMIKernelDensityDrawableDataObject(kd, outliers,
								paintScale)
						: new PMIContourLinesKernelDensityDrawableDataObject(kd,
								outliers, (LookupPaintScale) paintScale);
	}

	@Override
	protected DrawableDataObjectFastPlot createPlotFromChartData(
			ChartData<DrawableDataObject> dataPoints,
			AttributedString xAxisLabel, AttributedString yAxisLabel) {

		return showFullTriangleModel.getBooleanValue()
				? new PMITriangleDrawableDataObjectFastPlot(
						dataPoints.getData(), xAxisLabel, -0.05, 1.05,
						yAxisLabel, 0.475, 1.025,
						m_vertexColour.getColorValue(),
						m_triangleColour.getColorValue())
				: new PMITriangleDrawableDataObjectFastPlot(
						dataPoints.getData(), xAxisLabel, 0.0,
						dataPoints.getxDss(), yAxisLabel, 0.0,
						dataPoints.getyDss(), m_vertexColour.getColorValue(),
						m_triangleColour.getColorValue());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.pmi.nodes.plot.kerneldensity.
	 * KernelDensity2DPlotNodeModel#doConfigure(org.knime.core.data.
	 * DataTableSpec)
	 */
	@Override
	public void doConfigure(DataTableSpec inSpec)
			throws InvalidSettingsException {
		super.doConfigure(inSpec);

		// Finally, check the column bounds, if they exist, are within the
		// appropriate range
		if (colIdx[0] >= 0
				&& inSpec.getColumnSpec(colIdx[0]).getDomain().hasBounds()
				&& ((((DoubleValue) inSpec.getColumnSpec(colIdx[0]).getDomain()
						.getLowerBound()).getDoubleValue() < 0.0)
						|| ((DoubleValue) inSpec.getColumnSpec(colIdx[0])
								.getDomain().getUpperBound())
										.getDoubleValue() > 1.0)) {
			throw new InvalidSettingsException(
					"nPMI1 column domain is outside the bounds 0.0 - 1.0");
		}

		if (colIdx[1] >= 0
				&& inSpec.getColumnSpec(colIdx[1]).getDomain().hasBounds()
				&& ((((DoubleValue) inSpec.getColumnSpec(colIdx[1]).getDomain()
						.getLowerBound()).getDoubleValue() < 0.5)
						|| ((DoubleValue) inSpec.getColumnSpec(colIdx[1])
								.getDomain().getUpperBound())
										.getDoubleValue() > 1.0)) {
			throw new InvalidSettingsException(
					"nPMI2 column domain is outside the bounds 0.5 - 1.0");
		}
	}

}
