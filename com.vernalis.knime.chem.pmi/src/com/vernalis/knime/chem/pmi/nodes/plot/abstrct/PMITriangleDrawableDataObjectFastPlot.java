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
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;

import com.vernalis.knime.jfcplot.core.drawabledataobject.DrawableDataObject;
import com.vernalis.knime.jfcplot.core.drawabledataobject.DrawableDataObjectFastPlot;

/**
 * A modified {@link DrawableDataObjectFastPlot} to also draw PMI Bounds
 * 
 * @author s.roughley
 *
 */
@SuppressWarnings("serial")
public class PMITriangleDrawableDataObjectFastPlot
		extends DrawableDataObjectFastPlot {

	private final PmiTriangleBondsDrawableDataObject pmiTriangle;

	/**
	 * Constructor with axes and data supplied
	 * 
	 * @param data
	 *            The data for the plot
	 * @param xAxis
	 *            The x-axis for the plot
	 * @param yAxis
	 *            The y-axis for the plot
	 * @param vertexLabelColour
	 *            The colour of the triangle vertex labels ('Rod', 'Disc',
	 *            'Sphere')
	 * @param edgeColour
	 *            The colour of the vertex edge labels
	 */
	public PMITriangleDrawableDataObjectFastPlot(
			Collection<DrawableDataObject> data, NumberAxis xAxis,
			NumberAxis yAxis, Color vertexLabelColour, Color edgeColour) {
		super(data, xAxis, yAxis);
		this.pmiTriangle = PmiTriangleBondsDrawableDataObject.get(edgeColour,
				vertexLabelColour);

	}

	/**
	 * Constructor specifying data and axis titles and ranges
	 * 
	 * @param data
	 *            The {@link DrawableDataObject} objects to plot
	 * @param xAxisLabel
	 *            The x-axis label
	 * @param xAxisMin
	 *            The x-axis minimum value
	 * @param xAxisMax
	 *            The x-axis maximum value
	 * @param yAxisLabel
	 *            The y-axis label
	 * @param yAxisMin
	 *            The y-axis minimum value
	 * @param yAxisMax
	 *            The y-axis maximum value
	 * @param vertexLabelColour
	 *            The colour of the triangle vertex labels ('Rod', 'Disc',
	 *            'Sphere')
	 * @param edgeColour
	 *            The colour of the vertex edge labels
	 */
	public PMITriangleDrawableDataObjectFastPlot(
			Collection<DrawableDataObject> data, AttributedString xAxisLabel,
			double xAxisMin, double xAxisMax, AttributedString yAxisLabel,
			double yAxisMin, double yAxisMax, Color vertexLabelColour,
			Color edgeColour) {
		super(data, xAxisLabel, xAxisMin, xAxisMax, yAxisLabel, yAxisMin,
				yAxisMax);
		this.pmiTriangle = PmiTriangleBondsDrawableDataObject.get(edgeColour,
				vertexLabelColour);

	}

	/**
	 * Constructor specifying data and axis titles and
	 * {@link DoubleSummaryStatistics} for the x- and y-coordinates.
	 * 
	 * @param data
	 *            The {@link DrawableDataObject} objects to plot
	 * @param xAxisLabel
	 *            The x-axis label
	 * @param xRangeEndFrac
	 *            The fraction of the x-axis range to use as margins
	 * @param xDataDss
	 *            The {@link DoubleSummaryStatistics} describing the
	 *            x-coordinates of the dataset
	 * @param yAxisLabel
	 *            The y-axis label
	 * @param yRangeEndFrac
	 *            The fraction of the y-axis range to use as margins
	 * @param yDataDss
	 *            The {@link DoubleSummaryStatistics} describing the
	 *            y-coordinates of the dataset
	 * @param vertexLabelColour
	 *            The colour of the triangle vertex labels ('Rod', 'Disc',
	 *            'Sphere')
	 * @param edgeColour
	 *            The colour of the vertex edge labels
	 */
	public PMITriangleDrawableDataObjectFastPlot(
			Collection<DrawableDataObject> data, AttributedString xAxisLabel,
			double xRangeEndFrac, DoubleSummaryStatistics xDataDss,
			AttributedString yAxisLabel, double yRangeEndFrac,
			DoubleSummaryStatistics yDataDss, Color vertexLabelColour,
			Color edgeColour) {
		super(data, xAxisLabel, xRangeEndFrac, xDataDss, yAxisLabel,
				yRangeEndFrac, yDataDss);
		this.pmiTriangle = PmiTriangleBondsDrawableDataObject.get(edgeColour,
				vertexLabelColour);

	}

	/**
	 * Constructor specifying data and axis titles and ranges
	 * 
	 * @param data
	 *            The {@link DrawableDataObject} objects to plot
	 * @param xAxisLabel
	 *            The x-axis label
	 * @param xAxisMin
	 *            The x-axis minimum value
	 * @param xAxisMax
	 *            The x-axis maximum value
	 * @param yAxisLabel
	 *            The y-axis label
	 * @param yAxisMin
	 *            The y-axis minimum value
	 * @param yAxisMax
	 *            The y-axis maximum value
	 * @param vertexLabelColour
	 *            The colour of the triangle vertex labels ('Rod', 'Disc',
	 *            'Sphere')
	 * @param edgeColour
	 *            The colour of the vertex edge labels
	 */
	public PMITriangleDrawableDataObjectFastPlot(
			Collection<DrawableDataObject> data, String xAxisLabel,
			double xAxisMin, double xAxisMax, String yAxisLabel,
			double yAxisMin, double yAxisMax, Color vertexLabelColour,
			Color edgeColour) {
		super(data, xAxisLabel, xAxisMin, xAxisMax, yAxisLabel, yAxisMin,
				yAxisMax);
		this.pmiTriangle = PmiTriangleBondsDrawableDataObject.get(edgeColour,
				vertexLabelColour);

	}

	/**
	 * Constructor specifying data and axis titles and
	 * {@link DoubleSummaryStatistics} for the x- and y-coordinates.
	 * 
	 * @param data
	 *            The {@link DrawableDataObject} objects to plot
	 * @param xAxisLabel
	 *            The x-axis label
	 * @param xRangeEndFrac
	 *            The fraction of the x-axis range to use as margins
	 * @param xDataDss
	 *            The {@link DoubleSummaryStatistics} describing the
	 *            x-coordinates of the dataset
	 * @param yAxisLabel
	 *            The y-axis label
	 * @param yRangeEndFrac
	 *            The fraction of the y-axis range to use as margins
	 * @param yDataDss
	 *            The {@link DoubleSummaryStatistics} describing the
	 *            y-coordinates of the dataset
	 * @param vertexLabelColour
	 *            The colour of the triangle vertex labels ('Rod', 'Disc',
	 *            'Sphere')
	 * @param edgeColour
	 *            The colour of the vertex edge labels
	 */
	public PMITriangleDrawableDataObjectFastPlot(
			Collection<DrawableDataObject> data, String xAxisLabel,
			double xRangeEndFrac, DoubleSummaryStatistics xDataDss,
			String yAxisLabel, double yRangeEndFrac,
			DoubleSummaryStatistics yDataDss, Color vertexLabelColour,
			Color edgeColour) {
		super(data, xAxisLabel, xRangeEndFrac, xDataDss, yAxisLabel,
				yRangeEndFrac, yDataDss);
		this.pmiTriangle = PmiTriangleBondsDrawableDataObject.get(edgeColour,
				vertexLabelColour);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jfree.chart.plot.FastScatterPlot#render(java.awt.Graphics2D,
	 * java.awt.geom.Rectangle2D, org.jfree.chart.plot.PlotRenderingInfo,
	 * org.jfree.chart.plot.CrosshairState)
	 */
	@Override
	public void render(final Graphics2D g2, final Rectangle2D dataArea,
			final PlotRenderingInfo info, final CrosshairState crosshairState) {

		super.render(g2, dataArea, info, crosshairState);
		ValueAxis xAxis = getDomainAxis();
		ValueAxis yAxis = getRangeAxis();
		pmiTriangle.draw(g2, dataArea, xAxis, yAxis, getOrientation());

	}

}
