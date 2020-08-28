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
import java.util.stream.DoubleStream;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;

/**
 * An interface representing an object which may be drawn onto the plot canvas
 * in JFreeChart
 * 
 * @author s.roughley
 *
 */
public interface DrawableDataObject {

	/**
	 * Draw the data series represented by the object onto the JFreeChart
	 * object. The method should finish with all {@link Graphics2D} settings
	 * unchanged
	 * 
	 * @param g2
	 *            The graphics object to draw onto
	 * @param dataArea
	 *            The data area of the chart
	 * @param xAxis
	 *            The x-axis object of the chart
	 * @param yAxis
	 *            The y-axis object of the chart
	 * @param orientation
	 *            The plot orientation
	 */
	public void draw(final Graphics2D g2, final Rectangle2D dataArea,
			ValueAxis xAxis, ValueAxis yAxis, PlotOrientation orientation);

	/**
	 * @return A stream for all the x-coordinate values of the data series
	 */
	public DoubleStream getXStream();

	/**
	 * @return A stream for all the y-coordinate values of the data series
	 */
	public DoubleStream getYStream();

}
