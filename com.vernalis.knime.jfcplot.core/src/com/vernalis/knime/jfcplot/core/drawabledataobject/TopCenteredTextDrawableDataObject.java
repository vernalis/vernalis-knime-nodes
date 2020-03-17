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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.stream.DoubleStream;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.ui.RectangleEdge;

/**
 * A {@link DrawableDataObject} to add a text label to a plot, with the top
 * centre of the text at the coordinates (x,y)
 * 
 * @author s.roughley
 *
 */
public class TopCenteredTextDrawableDataObject implements DrawableDataObject {

	private final double x, y;
	private final String text;
	private final Color colour;
	private final Font font;

	/**
	 * Overloaded constructor to use existing font and colour
	 * 
	 * @param x
	 *            The x coordinate
	 * @param y
	 *            The y coordinate
	 * @param text
	 *            The text to draw
	 */
	public TopCenteredTextDrawableDataObject(double x, double y, String text) {
		this(x, y, text, null);
	}

	/**
	 * Overloaded constructor to use existing font
	 * 
	 * @param x
	 *            The x coordinate
	 * @param y
	 *            The y coordinate
	 * @param text
	 *            The text to draw
	 * @param colour
	 *            The colour of the text - {@code null} uses existing graphics
	 *            colour
	 */
	public TopCenteredTextDrawableDataObject(double x, double y, String text,
			Color colour) {
		this(x, y, text, colour, null);
	}

	/**
	 * Constructor
	 * 
	 * @param x
	 *            The x coordinate
	 * @param y
	 *            The y coordinate
	 * @param text
	 *            The text to draw
	 * @param colour
	 *            The colour of the text - {@code null} uses existing graphics
	 *            colour
	 * @param font
	 *            The font - {@code null} uses existing graphics font
	 */
	public TopCenteredTextDrawableDataObject(double x, double y, String text,
			Color colour, Font font) {
		this.x = x;
		this.y = y;
		this.text = text;
		this.colour = colour;
		this.font = font;
	}

	@Override
	public void draw(Graphics2D g2, Rectangle2D dataArea, ValueAxis xAxis,
			ValueAxis yAxis, PlotOrientation orientation) {

		Stroke oldStroke = g2.getStroke();
		Font oldFont = g2.getFont();
		Paint oldPaint = g2.getPaint();

		g2.setStroke(new BasicStroke(1.0f));
		if (colour != null) {
			g2.setColor(colour);
		}
		if (font != null) {
			g2.setFont(font);
		}
		// Use FontMetrics to ensure position stays correct regardless of
		// shifting around, e.g.
		// http://www.java2s.com/Tutorial/Java/0261__2D-Graphics/Centertext.htm
		FontMetrics fm = g2.getFontMetrics();
		g2.drawString(text,
				(float) (xAxis.valueToJava2D(0.5, dataArea,
						RectangleEdge.BOTTOM) - fm.stringWidth(text) / 2.0),
				(float) yAxis.valueToJava2D(0.5, dataArea, RectangleEdge.LEFT)
						+ fm.getAscent());

		// Restore the settings
		g2.setStroke(oldStroke);
		g2.setFont(oldFont);
		g2.setPaint(oldPaint);

	}

	@Override
	public DoubleStream getXStream() {
		return DoubleStream.of(x);
	}

	@Override
	public DoubleStream getYStream() {
		return DoubleStream.of(y);
	}

}
