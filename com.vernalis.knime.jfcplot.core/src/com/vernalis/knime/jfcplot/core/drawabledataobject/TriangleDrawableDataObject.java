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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.stream.DoubleStream;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.ui.RectangleEdge;

public class TriangleDrawableDataObject implements DrawableDataObject {

	private final double x0, x1, x2, y0, y1, y2;
	private final Color lineColour, fillColour;
	private final Stroke lineStroke;

	/**
	 * Overloaded constructor using with no fill and existing stroke)
	 * 
	 * @param x0
	 *            The x-coordinate of the 1st vertex
	 * @param y0
	 *            The y-coordinate of the 1st vertex
	 * @param x1
	 *            The x-coordinate of the 2nd vertex
	 * @param y1
	 *            The y-coordinate of the 2nd vertex
	 * @param x2
	 *            The x-coordinate of the 3rd vertex
	 * @param y2
	 *            The y-coordinate of the 3rd vertex
	 * @param lineColour
	 *            The line colour - {@code null} means the outside line is not
	 *            drawn
	 * @param lineStroke
	 *            The line Stroke - {@code null} means the existing stroke is
	 *            used
	 */
	public TriangleDrawableDataObject(double x0, double y0, double x1,
			double y1, double x2, double y2, Color lineColour) {
		this(x0, y0, x1, y1, x2, y2, lineColour, null, null);
	}

	/**
	 * Overloaded constructor using with no fill
	 * 
	 * @param x0
	 *            The x-coordinate of the 1st vertex
	 * @param y0
	 *            The y-coordinate of the 1st vertex
	 * @param x1
	 *            The x-coordinate of the 2nd vertex
	 * @param y1
	 *            The y-coordinate of the 2nd vertex
	 * @param x2
	 *            The x-coordinate of the 3rd vertex
	 * @param y2
	 *            The y-coordinate of the 3rd vertex
	 * @param lineColour
	 *            The line colour - {@code null} means the outside line is not
	 *            drawn
	 * @param lineStroke
	 *            The line Stroke - {@code null} means the existing stroke is
	 *            used
	 */
	public TriangleDrawableDataObject(double x0, double y0, double x1,
			double y1, double x2, double y2, Color lineColour,
			Stroke lineStroke) {
		this(x0, y0, x1, y1, x2, y2, lineColour, null, lineStroke);
	}

	/**
	 * Overloaded constructor using existing stroke
	 * 
	 * @param x0
	 *            The x-coordinate of the 1st vertex
	 * @param y0
	 *            The y-coordinate of the 1st vertex
	 * @param x1
	 *            The x-coordinate of the 2nd vertex
	 * @param y1
	 *            The y-coordinate of the 2nd vertex
	 * @param x2
	 *            The x-coordinate of the 3rd vertex
	 * @param y2
	 *            The y-coordinate of the 3rd vertex
	 * @param lineColour
	 *            The line colour - {@code null} means the outside line is not
	 *            drawn
	 * @param fillColour
	 *            The fill colour - {@code null} means the shape is not filled
	 * 
	 */
	public TriangleDrawableDataObject(double x0, double y0, double x1,
			double y1, double x2, double y2, Color lineColour,
			Color fillColour) {
		this(x0, y0, x1, y1, x2, y2, lineColour, fillColour, null);
	}

	/**
	 * Full constructor
	 * 
	 * @param x0
	 *            The x-coordinate of the 1st vertex
	 * @param y0
	 *            The y-coordinate of the 1st vertex
	 * @param x1
	 *            The x-coordinate of the 2nd vertex
	 * @param y1
	 *            The y-coordinate of the 2nd vertex
	 * @param x2
	 *            The x-coordinate of the 3rd vertex
	 * @param y2
	 *            The y-coordinate of the 3rd vertex
	 * @param lineColour
	 *            The line colour - {@code null} means the outside line is not
	 *            drawn
	 * @param fillColour
	 *            The fill colour - {@code null} means the shape is not filled
	 * @param lineStroke
	 *            The line Stroke - {@code null} means the existing stroke is
	 *            used
	 */
	public TriangleDrawableDataObject(double x0, double y0, double x1,
			double y1, double x2, double y2, Color lineColour, Color fillColour,
			Stroke lineStroke) {
		this.x0 = x0;
		this.y0 = y0;
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.lineColour = lineColour;
		this.fillColour = fillColour;
		this.lineStroke = lineStroke;
	}

	@Override
	public void draw(Graphics2D g2, Rectangle2D dataArea, ValueAxis xAxis,
			ValueAxis yAxis, PlotOrientation orientation) {

		// Get the incoming properties
		Stroke oldStroke = g2.getStroke();
		Paint oldPaint = g2.getPaint();

		Path2D triangle = new Path2D.Double();
		triangle.moveTo(xAxis.valueToJava2D(x0, dataArea, RectangleEdge.BOTTOM),
				yAxis.valueToJava2D(y0, dataArea, RectangleEdge.LEFT));
		triangle.lineTo(xAxis.valueToJava2D(x1, dataArea, RectangleEdge.BOTTOM),
				yAxis.valueToJava2D(y1, dataArea, RectangleEdge.LEFT));
		triangle.lineTo(xAxis.valueToJava2D(x2, dataArea, RectangleEdge.BOTTOM),
				yAxis.valueToJava2D(y2, dataArea, RectangleEdge.LEFT));
		triangle.closePath();

		if (lineStroke != null) {
			g2.setStroke(new BasicStroke(1.0f));
		}
		if (lineColour != null) {
			g2.setColor(lineColour);
			g2.draw(triangle);
		}
		if (fillColour != null) {
			g2.fill(triangle);
		}

		// Restore the settings
		g2.setStroke(oldStroke);
		g2.setPaint(oldPaint);
	}

	@Override
	public DoubleStream getXStream() {
		return DoubleStream.of(x0, x1, x2);
	}

	@Override
	public DoubleStream getYStream() {
		return DoubleStream.of(y0, y1, y2);
	}

}
