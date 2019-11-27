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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.stream.DoubleStream;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.ui.RectangleEdge;

import com.vernalis.knime.chem.util.points.Point;

/**
 * A {@link KernelDensityDrawableDataObject} for a simple 1D
 * {@link KernelDensityFunction}. It has an optional colour and identifier
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class KernelDensity1DDrawableDataObject
		implements KernelDensityDrawableDataObject {

	private final KernelDensityFunction kernel;
	private final Color colour;
	private final String id;

	/**
	 * Overloaded constructor with no identifer, and the kernel being drawn in
	 * black
	 * 
	 * @param kernel
	 *            The 1D kernel
	 */
	public KernelDensity1DDrawableDataObject(KernelDensityFunction kernel) {
		this(kernel, Color.BLACK);
	}

	/**
	 * Overloaded constructor with no identifier
	 * 
	 * @param kernel
	 *            The 1D kernel
	 * @param colour
	 *            The colour to draw the density function
	 */
	public KernelDensity1DDrawableDataObject(KernelDensityFunction kernel,
			Color colour) {
		this(kernel, colour, null);
	}

	/**
	 * Constructor offering full control
	 * 
	 * @param kernel
	 *            The 1D kernel
	 * @param colour
	 *            The colour to draw the density function
	 * @param id
	 *            The optional identifier
	 */
	public KernelDensity1DDrawableDataObject(KernelDensityFunction kernel,
			Color colour, String id) {
		if (kernel == null || kernel.getNumDimensions() != 1) {
			throw new IllegalArgumentException("Kernel must be a 1-D kernel");
		}
		this.kernel = kernel;
		this.colour = colour;
		this.id = id;
	}

	@Override
	public void draw(Graphics2D g2, Rectangle2D dataArea, ValueAxis xAxis,
			ValueAxis yAxis, PlotOrientation orientation) {

		// Save incoming stroke and colour
		Color oldcolour = g2.getColor();
		Stroke oldStroke = g2.getStroke();
		Path2D.Float line = new Path2D.Float();

		List<Point<?>> points = kernel.getGridPoints();
		if (!points.isEmpty()) {
			line.moveTo(
					xAxis.valueToJava2D(points.get(0).getCoordinate(0),
							dataArea, RectangleEdge.BOTTOM),
					yAxis.valueToJava2D(points.get(0).getCoordinate(1),
							dataArea, RectangleEdge.LEFT));
			points.stream().skip(1)
					.forEach(pt -> line.lineTo(
							xAxis.valueToJava2D(
									pt.getCoordinate(0),
									dataArea, RectangleEdge.BOTTOM),
							yAxis.valueToJava2D(pt.getCoordinate(1), dataArea,
									RectangleEdge.LEFT)));
		}

		g2.setColor(colour);
		g2.setStroke(new BasicStroke(1.0f));
		g2.draw(line);

		getAdditionalDDOs()
				.forEach(x -> x.draw(g2, dataArea, xAxis, yAxis, orientation));

		// Restore incoming stroke and colour
		g2.setColor(oldcolour);
		g2.setStroke(oldStroke);
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

	/**
	 * @return the colour
	 */
	public final Color getColour() {
		return colour;
	}

	/**
	 * @return the id
	 */
	public final String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("KernelDensity1DDrawableDataObject [");
		if (kernel != null) {
			builder.append("kernel=");
			builder.append(kernel);
			builder.append(", ");
		}
		if (id != null) {
			builder.append("id=");
			builder.append(id);
		}
		builder.append("]");
		return builder.toString();
	}

}
