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

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.DoubleStream;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.ui.RectangleEdge;

import com.vernalis.knime.jfcplot.core.drawabledataobject.DrawableDataObject;

/**
 * {@link KernelDensityDrawableDataObject} implementation for a 2D
 * {@link KernelDensityFunction} with either continuous intensity colour or
 * filled contours, depending on the supplied {@link PaintScale}
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class KernelDensity2DDrawableDataObject
		implements KernelDensityDrawableDataObject {

	private final KernelDensityFunction kernel;
	private final List<DrawableDataObject> additionals = new ArrayList<>();
	private final PaintScale paintScale;
	private double xOffSet;
	private double yOffSet;

	/**
	 * @param kd
	 *            The {@link KernelDensityFunction} to plot
	 * @param c
	 *            Any additional {@link DrawableDataObject}s to overlay on the
	 *            plot
	 * @param scale
	 *            The {@link PaintScale} - TwoValuePaintScale will give a
	 *            continuous colour gradient, whereas LookupPaintScale will give
	 *            filled contours
	 */
	public KernelDensity2DDrawableDataObject(KernelDensityFunction kd,
			Collection<? extends DrawableDataObject> c, PaintScale scale) {
		this.kernel = kd;
		additionals.addAll(c);
		this.paintScale = scale;
		this.xOffSet = Math.abs(
				getXStream().distinct().limit(2).reduce(0, (a, b) -> b - a))
				/ 2;
		this.yOffSet = Math.abs(
				getYStream().distinct().limit(2).reduce(0, (a, b) -> b - a))
				/ 2;
	}

	@Override
	public void draw(Graphics2D g2, Rectangle2D dataArea, ValueAxis xAxis,
			ValueAxis yAxis, PlotOrientation orientation) {
		Stroke stroke0 = g2.getStroke();
		Paint paint = g2.getPaint();
		kernel.getGridPoints()
				.forEach(pt -> drawRect(g2, dataArea, xAxis, yAxis,
						pt.getCoordinate(0), pt.getCoordinate(1),
						paintScale.getPaint(pt.getCoordinate(2))));
		getAdditionalDDOs().forEach(
				ddo -> ddo.draw(g2, dataArea, xAxis, yAxis, orientation));
		g2.setStroke(stroke0);
		g2.setPaint(paint);
	}

	/**
	 * Drawing the single rectangular block on the graphics device, with the
	 * color depending on the specified value. If the specified value equals to
	 * Double.NaN the drawn rectangle is transparent.
	 *
	 * @param g2
	 *            the {@link Graphics2D} object on which the drawing is done
	 * @param dataArea
	 *            The plotting area
	 * @param x
	 *            the coordinate on the X axis of the block's center
	 * @param y
	 *            the coordinate on the Y axis of the block's center
	 * @param color
	 *            the double value for the block's height
	 */
	protected void drawRect(final Graphics2D g2, Rectangle2D dataArea,
			ValueAxis xAxis, ValueAxis yAxis, final double x, final double y,
			final Paint paint) {
		Paint oldPaint = g2.getPaint();
		Stroke oldStroke = g2.getStroke();

		g2.setPaint(paint);
		// Calculate the coordinates of the corners in plot units
		double bLeft = xAxis.valueToJava2D(x - getXOffSet(), dataArea,
				RectangleEdge.BOTTOM);
		double bTop = yAxis.valueToJava2D(y - getYOffSet(), dataArea,
				RectangleEdge.LEFT);
		double bRight = xAxis.valueToJava2D(x + getXOffSet(), dataArea,
				RectangleEdge.BOTTOM);
		double bLow = yAxis.valueToJava2D(y + getYOffSet(), dataArea,
				RectangleEdge.LEFT);

		// generating and drawing the rectangle
		Rectangle2D rect = new Rectangle2D.Double(Math.min(bLeft, bRight),
				Math.min(bLow, bTop), Math.abs(bRight - bLeft),
				Math.abs(bTop - bLow));
		g2.fill(rect);
		g2.setStroke(new BasicStroke(1.0f));
		g2.draw(rect);

		g2.setPaint(oldPaint);
		g2.setStroke(oldStroke);
	}

	public double getYOffSet() {
		return yOffSet;
	}

	public double getXOffSet() {
		return xOffSet;
	}

	@Override
	public DoubleStream getXStream() {
		return kernel.getGridPoints().stream()
				.mapToDouble(pt -> pt.getCoordinate(0));
	}

	@Override
	public DoubleStream getYStream() {
		return kernel.getGridPoints().stream()
				.mapToDouble(pt -> pt.getCoordinate(1));
	}

	@Override
	public KernelDensityFunction getKernel() {
		return kernel;
	}

	@Override
	public Collection<DrawableDataObject> getAdditionalDDOs() {
		return Collections.unmodifiableList(additionals);
	}

	@Override
	public PaintScale getPaintScale() {
		return paintScale;
	}

}
