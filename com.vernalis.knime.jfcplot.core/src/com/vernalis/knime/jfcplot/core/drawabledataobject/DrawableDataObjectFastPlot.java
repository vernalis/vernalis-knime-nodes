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
 *******************************************************************************/
package com.vernalis.knime.jfcplot.core.drawabledataobject;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.PlotRenderingInfo;

/**
 * A {@link FastScatterPlot} based implementation of a PMI Triangle plot. A
 * collection of {@link DrawableDataObject}s are plotted in iterator order,
 * followed by the bounding triangle and labels
 * 
 * @author s.roughley
 *
 */
@SuppressWarnings("serial")
public class DrawableDataObjectFastPlot extends FastScatterPlot {

	private final Collection<DrawableDataObject> data;

	/**
	 * Constructor specifying data and axis titles. <em>NB This constructor will
	 * iterate over the data twice to determine the axis ranges</em>
	 * 
	 * @param data
	 *            The {@link DrawableDataObject} objects to plot
	 * @param xAxisLabel
	 *            The x-axis label
	 * @param xRangeEndFrac
	 *            The fraction of the x-axis range to use as margins
	 * @param yAxisLabel
	 *            The y-axis label
	 * @param yRangeEndFrac
	 *            The fraction of the y-axis range to use as margins
	 */
	public DrawableDataObjectFastPlot(Collection<DrawableDataObject> data,
			String xAxisLabel, double xRangeEndFrac, String yAxisLabel,
			double yRangeEndFrac) {
		this(data, new AttributedString(xAxisLabel), xRangeEndFrac,
				new AttributedString(yAxisLabel), yRangeEndFrac);
	}

	/**
	 * Constructor specifying data and axis titles. <em>NB This constructor will
	 * iterate over the data twice to determine the axis ranges</em>
	 * 
	 * @param data
	 *            The {@link DrawableDataObject} objects to plot
	 * @param xAxisLabel
	 *            The x-axis label
	 * @param xRangeEndFrac
	 *            The fraction of the x-axis range to use as margins
	 * @param yAxisLabel
	 *            The y-axis label
	 * @param yRangeEndFrac
	 *            The fraction of the y-axis range to use as margins
	 */
	public DrawableDataObjectFastPlot(Collection<DrawableDataObject> data,
			AttributedString xAxisLabel, double xRangeEndFrac,
			AttributedString yAxisLabel, double yRangeEndFrac) {
		this(data, xAxisLabel, xRangeEndFrac,
				data.stream().flatMapToDouble(d -> d.getXStream())
						.summaryStatistics(),
				yAxisLabel, yRangeEndFrac,
				data.stream().flatMapToDouble(d -> d.getYStream())
						.summaryStatistics());
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
	 */
	public DrawableDataObjectFastPlot(Collection<DrawableDataObject> data,
			String xAxisLabel, double xRangeEndFrac,
			DoubleSummaryStatistics xDataDss, String yAxisLabel,
			double yRangeEndFrac, DoubleSummaryStatistics yDataDss) {
		this(data, new AttributedString(xAxisLabel), xRangeEndFrac, xDataDss,
				new AttributedString(yAxisLabel), yRangeEndFrac, yDataDss);
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
	 */
	public DrawableDataObjectFastPlot(Collection<DrawableDataObject> data,
			AttributedString xAxisLabel, double xRangeEndFrac,
			DoubleSummaryStatistics xDataDss, AttributedString yAxisLabel,
			double yRangeEndFrac, DoubleSummaryStatistics yDataDss) {
		this(data, xAxisLabel,
				xDataDss.getMin() - xRangeEndFrac
						* (xDataDss.getMax() - xDataDss.getMin()),
				xDataDss.getMax() + xRangeEndFrac
						* (xDataDss.getMax() - xDataDss.getMin()),
				yAxisLabel,
				yDataDss.getMin() - yRangeEndFrac
						* (yDataDss.getMax() - yDataDss.getMin()),
				yDataDss.getMax() + yRangeEndFrac
						* (yDataDss.getMax() - yDataDss.getMin()));
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
	 */
	public DrawableDataObjectFastPlot(Collection<DrawableDataObject> data,
			String xAxisLabel, double xAxisMin, double xAxisMax,
			String yAxisLabel, double yAxisMin, double yAxisMax) {
		this(data, new AttributedString(xAxisLabel), xAxisMin, xAxisMax,
				new AttributedString(yAxisLabel), yAxisMin, yAxisMax);
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
	 */
	public DrawableDataObjectFastPlot(Collection<DrawableDataObject> data,
			AttributedString xAxisLabel, double xAxisMin, double xAxisMax,
			AttributedString yAxisLabel, double yAxisMin, double yAxisMax) {

		this(data, getNumberAxis(xAxisLabel, xAxisMin, xAxisMax),
				getNumberAxis(yAxisLabel, yAxisMin, yAxisMax));

	}

	/**
	 * Constructor with axes and data supplied
	 * 
	 * @param data
	 *            The {@link DrawableDataObject} objects to plot
	 * @param xAxis
	 *            The x-axis
	 * @param yAxis
	 *            The y-axis
	 */
	public DrawableDataObjectFastPlot(Collection<DrawableDataObject> data,
			NumberAxis xAxis, NumberAxis yAxis) {

		this.data = data;
		this.setDomainAxis(xAxis);
		this.setRangeAxis(yAxis);

		// Turn gridlines off
		setDomainGridlinesVisible(false);
		setRangeGridlinesVisible(false);
	}

	/**
	 * Utility method to create a number axis
	 * 
	 * @param axisLabel
	 *            The axis label
	 * @param axisMin
	 *            The minimum value for the axis
	 * @param axisMax
	 *            The maximum value for the axis
	 */
	private static NumberAxis getNumberAxis(AttributedString axisLabel,
			double axisMin, double axisMax) {
		NumberAxis axis = new NumberAxis();
		axis.setAttributedLabel(axisLabel);
		axis.setRange(axisMin, axisMax);
		return axis;
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

		for (DrawableDataObject series : data) {
			series.draw(g2, dataArea, getDomainAxis(), getRangeAxis(),
					getOrientation());

		}

	}

}
