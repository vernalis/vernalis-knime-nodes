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
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.ui.RectangleEdge;

import com.vernalis.knime.chem.util.points.Point;
import com.vernalis.knime.jfcplot.core.drawabledataobject.DrawableDataObject;

/**
 * A {@link DrawableDataObject} implementation based on
 * {@link KernelDensity2DDrawableDataObject} which draws contour lines along the
 * edge of each {@link KernelDensityFunction} grid cell in the colour for the
 * cell intensity, when the adjacent cell would be colour differently and has a
 * lower intensity
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class KernelDensity2DContourLinesDrawableDataObject
		extends KernelDensity2DDrawableDataObject {

	protected final Point<?>[][] pointGrid;

	/**
	 * Constructor
	 * 
	 * @param kd
	 *            The {@link KernelDensityFunction} to plot
	 * @param c
	 *            Any additional {@link DrawableDataObject}s which are to be
	 *            overlaid
	 * @param scale
	 *            The paint scale for the contour levels
	 */
	public KernelDensity2DContourLinesDrawableDataObject(
			KernelDensityFunction kd,
			Collection<? extends DrawableDataObject> c,
			LookupPaintScale scale) {
		super(kd, c, scale);
		// We need to reshape the data for convenient access to neighbours
		double[] xLookup = getXStream().distinct().sorted().toArray();
		double[] yLookup = getYStream().distinct().sorted().toArray();
		pointGrid = new Point<?>[xLookup.length][yLookup.length];
		for (Point<?> pt : kd.getGridPoints()) {
			pointGrid[Arrays.binarySearch(xLookup, pt.getCoordinate(0))][Arrays
					.binarySearch(yLookup, pt.getCoordinate(1))] = pt;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.plot.nodes.kerneldensity.
	 * KernelDensity2DDrawableDataObject#draw(java.awt.Graphics2D,
	 * java.awt.geom.Rectangle2D, org.jfree.chart.axis.ValueAxis,
	 * org.jfree.chart.axis.ValueAxis, org.jfree.chart.plot.PlotOrientation)
	 */
	@Override
	public void draw(Graphics2D g2, Rectangle2D dataArea, ValueAxis xAxis,
			ValueAxis yAxis, PlotOrientation orientation) {
		Stroke stroke0 = g2.getStroke();
		Paint paint = g2.getPaint();
		g2.setStroke(new BasicStroke(1.0f));

		for (int xIdx = 0; xIdx < pointGrid.length; xIdx++) {
			for (int yIdx = 0; yIdx < pointGrid[xIdx].length; yIdx++) {
				Point<?> point = pointGrid[xIdx][yIdx];
				final double z0 = point
						.getCoordinate(this.getKernel().getNumDimensions());
				Paint pointColor = getPaintScale().getPaint(z0);
				g2.setPaint(pointColor);
				Path2D.Float line = new Path2D.Float();

				// Draw top edge?
				if (yIdx > 0) {
					double z1 = pointGrid[xIdx][yIdx - 1]
							.getCoordinate(this.getKernel().getNumDimensions());
					if (Double.compare(z1, z0) < 0 && !getPaintScale()
							.getPaint(z1).equals(pointColor)) {
						line.moveTo(
								xAxis.valueToJava2D(
										point.getCoordinate(0) - getXOffSet(),
										dataArea, RectangleEdge.BOTTOM),
								yAxis.valueToJava2D(
										point.getCoordinate(1) - getYOffSet(),
										dataArea, RectangleEdge.LEFT));
						line.lineTo(
								xAxis.valueToJava2D(
										point.getCoordinate(0) + getXOffSet(),
										dataArea, RectangleEdge.BOTTOM),
								yAxis.valueToJava2D(
										point.getCoordinate(1) - getYOffSet(),
										dataArea, RectangleEdge.LEFT));
					}
				}

				// Draw bottom edge?
				if (yIdx < pointGrid[xIdx].length - 1) {
					double z1 = pointGrid[xIdx][yIdx + 1]
							.getCoordinate(this.getKernel().getNumDimensions());
					if (Double.compare(z1, z0) < 0 && !getPaintScale()
							.getPaint(z1).equals(pointColor)) {
						line.moveTo(
								xAxis.valueToJava2D(
										point.getCoordinate(0) - getXOffSet(),
										dataArea, RectangleEdge.BOTTOM),
								yAxis.valueToJava2D(
										point.getCoordinate(1) + getYOffSet(),
										dataArea, RectangleEdge.LEFT));
						line.lineTo(
								xAxis.valueToJava2D(
										point.getCoordinate(0) + getXOffSet(),
										dataArea, RectangleEdge.BOTTOM),
								yAxis.valueToJava2D(
										point.getCoordinate(1) + getYOffSet(),
										dataArea, RectangleEdge.LEFT));
					}
				}

				// Draw left edge?
				if (xIdx > 0) {
					double z1 = pointGrid[xIdx - 1][yIdx]
							.getCoordinate(this.getKernel().getNumDimensions());
					if (Double.compare(z1, z0) < 0 && !getPaintScale()
							.getPaint(z1).equals(pointColor)) {
						line.moveTo(
								xAxis.valueToJava2D(
										point.getCoordinate(0) - getXOffSet(),
										dataArea, RectangleEdge.BOTTOM),
								yAxis.valueToJava2D(
										point.getCoordinate(1) + getYOffSet(),
										dataArea, RectangleEdge.LEFT));
						line.lineTo(
								xAxis.valueToJava2D(
										point.getCoordinate(0) - getXOffSet(),
										dataArea, RectangleEdge.BOTTOM),
								yAxis.valueToJava2D(
										point.getCoordinate(1) - getYOffSet(),
										dataArea, RectangleEdge.LEFT));
					}
				}

				// Draw bottom edge?
				if (xIdx < pointGrid.length - 1) {
					double z1 = pointGrid[xIdx + 1][yIdx]
							.getCoordinate(this.getKernel().getNumDimensions());
					if (Double.compare(z1, z0) < 0 && !getPaintScale()
							.getPaint(z1).equals(pointColor)) {
						line.moveTo(
								xAxis.valueToJava2D(
										point.getCoordinate(0) + getXOffSet(),
										dataArea, RectangleEdge.BOTTOM),
								yAxis.valueToJava2D(
										point.getCoordinate(1) + getYOffSet(),
										dataArea, RectangleEdge.LEFT));
						line.lineTo(
								xAxis.valueToJava2D(
										point.getCoordinate(0) + getXOffSet(),
										dataArea, RectangleEdge.BOTTOM),
								yAxis.valueToJava2D(
										point.getCoordinate(1) - getYOffSet(),
										dataArea, RectangleEdge.LEFT));
					}
				}
				g2.draw(line);
			}
		}
		getAdditionalDDOs().forEach(
				ddo -> ddo.draw(g2, dataArea, xAxis, yAxis, orientation));
		g2.setStroke(stroke0);
		g2.setPaint(paint);
	}

}
