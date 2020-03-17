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

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.ui.RectangleEdge;

import com.vernalis.knime.chem.util.points.Point;
import com.vernalis.knime.jfcplot.core.drawabledataobject.DrawableDataObject;
import com.vernalis.knime.plot.nodes.kerneldensity.KernelDensity2DContourLinesDrawableDataObject;
import com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityFunction;

public class PMIContourLinesKernelDensityDrawableDataObject
		extends KernelDensity2DContourLinesDrawableDataObject {

	/**
	 * @param kd
	 * @param c
	 * @param scale
	 */
	public PMIContourLinesKernelDensityDrawableDataObject(
			KernelDensityFunction kd,
			Collection<? extends DrawableDataObject> c,
			LookupPaintScale scale) {
		super(kd, c, scale);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.pmi.nodes.plot.kerneldensity.
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
				if (point == null) {
					// Outside the PMI Bounds triangle
					continue;
				}
				final double z0 = point
						.getCoordinate(this.getKernel().getNumDimensions());
				Paint pointColor = getPaintScale().getPaint(z0);

				// Draw bottom edge?
				boolean drawBottom = false;
				if (yIdx > 0 && pointGrid[xIdx][yIdx - 1] != null) {
					double z1 = pointGrid[xIdx][yIdx - 1]
							.getCoordinate(this.getKernel().getNumDimensions());
					if (Double.compare(z1, z0) < 0 && !getPaintScale()
							.getPaint(z1).equals(pointColor)) {
						drawBottom = true;
					}
				}

				// Draw top edge?
				boolean drawTop = false;
				if (yIdx < pointGrid[xIdx].length - 1
						&& pointGrid[xIdx][yIdx + 1] != null) {
					double z1 = pointGrid[xIdx][yIdx + 1]
							.getCoordinate(this.getKernel().getNumDimensions());
					if (Double.compare(z1, z0) < 0 && !getPaintScale()
							.getPaint(z1).equals(pointColor)) {
						drawTop = true;

					}
				}

				// Draw left edge?
				boolean drawLeft = false;
				if (xIdx > 0 && pointGrid[xIdx - 1][yIdx] != null) {
					double z1 = pointGrid[xIdx - 1][yIdx]
							.getCoordinate(this.getKernel().getNumDimensions());
					if (Double.compare(z1, z0) < 0 && !getPaintScale()
							.getPaint(z1).equals(pointColor)) {
						drawLeft = true;

					}
				}

				// Draw right edge?
				boolean drawRight = false;
				if (xIdx < pointGrid.length - 1
						&& pointGrid[xIdx + 1][yIdx] != null) {
					double z1 = pointGrid[xIdx + 1][yIdx]
							.getCoordinate(this.getKernel().getNumDimensions());
					if (Double.compare(z1, z0) < 0 && !getPaintScale()
							.getPaint(z1).equals(pointColor)) {
						drawRight = true;

					}
				}
				if (drawTop || drawLeft || drawRight || drawBottom) {
					drawRect(g2, dataArea, xAxis, yAxis, point.getCoordinate(0),
							point.getCoordinate(1), drawTop, drawLeft,
							drawRight, drawBottom, pointColor);
				}

			}
		}
		getAdditionalDDOs().forEach(
				ddo -> ddo.draw(g2, dataArea, xAxis, yAxis, orientation));
		g2.setStroke(stroke0);
		g2.setPaint(paint);
	}

	protected void drawRect(Graphics2D g2, Rectangle2D dataArea,
			ValueAxis xAxis, ValueAxis yAxis, double x, double y,
			boolean drawTop, boolean drawLeft, boolean drawRight,
			boolean drawBottom, Paint paint) {
		// We need to modify to not draw outside the triangle bounds
		// variable for original composite settings
		Paint oldPaint = g2.getPaint();
		Stroke oldStroke = g2.getStroke();

		g2.setPaint(paint);

		// Calculate the coordinates of the corners in plot units - dont let the
		// corners be beyond the outer rectangle
		// y runs from top of screen increasing downwards - so we need to
		// 'invert' the ytop/ybottom names

		// The coordinates in nPMI space of the rectangle corners, truncated to
		// the x and y ranges of the triangle
		double xLeft = Math.max(x - getXOffSet(), 0.0);
		double xRight = Math.min(x + getXOffSet(), 1.0);
		double yTop = Math.min(y + getYOffSet(), 1.0);
		double yBottom = Math.max(y - getYOffSet(), 0.5);

		// If the top (wider) edge ends outside the bounding triangle,
		// then truncate its width
		if (xLeft + yTop < 1.0) {
			/*
			 * Top left corner outside
			 * @formatter:off
			 * 
			 *      _\__              ..\__
			 *     |  \ |             .  \ |
			 *     |   \|             .  |\|
			 *     |    \    =====>   .->| \
			 *     |    |\            .  | |\
			 *     |____| \           ...|_| \
			 *             \y=1-x             \
			 *             
			 *  @formatter:on
			 */
			xLeft = 1 - yTop;
		}
		if (xRight > yTop) {
			/*
			 * Top right corner outside
			 * @formatter:off
			 * 
			 *         /y=x
			 *      __/_               __/..
			 *     | /  |             | /  .
			 *     |/   |             |/|  .
			 *     /    |    =====>   / |<-.
			 *    /|    |            /| |  .
			 *   / |____|           / |_|...
			 *  /                  /  
			 *  
			 *  @formatter:on
			 */
			xRight = yTop;
		}

		// If the bottom edge is outside the bounding triangle, raise it to the
		// triangle. The
		// possibility of one bottom corner in and one out is handled later
		// during the actual plotting
		if (yBottom < xLeft && yBottom < xRight) {
			/*
			 * @formatter:off
			 * 
			 *            /y=x              /
			 *  	 ___ /             ___ /
			 *      |   /             |   /
			 *      |  /|             |  /|
			 *  	| / |    =====>   | / |
			 *  	|/  |             |/__|
			 *      /   |             / ^ .
			 *     /|___|            /..|..
			 *    /                 /    
			 *    
			 * @formatter:on
			 */
			yBottom = xLeft;
		}
		if (xLeft + yBottom < 1.0 && xRight + yBottom < 1.0) {
			/*
			 * @formatter:off
			 * 
			 *    \ ___             \ ___
			 *     \   |             \   |
			 *     |\  |             |\  |
			 *     | \ |    =====>   | \ |
			 *     |  \|             |__\|
			 *     |   \             . ^ \
			 *     |___|\            ..|..\
			 *           \y=1-x            \
			 *           
			 *  @formatter:on
			 */
			yBottom = 1.0 - xRight;
		}

		// The coordinates in plot space
		double plotXLeft =
				xAxis.valueToJava2D(xLeft, dataArea, RectangleEdge.BOTTOM);
		double plotYBottom =
				yAxis.valueToJava2D(yBottom, dataArea, RectangleEdge.LEFT);
		double plotXRight =
				xAxis.valueToJava2D(xRight, dataArea, RectangleEdge.BOTTOM);
		double plotYTop =
				yAxis.valueToJava2D(yTop, dataArea, RectangleEdge.LEFT);

		// The derived coordinates in plot space
		double plotXRightAsYBottom =
				yAxis.valueToJava2D(xRight, dataArea, RectangleEdge.LEFT);
		double plotYBottomAsXRight =
				xAxis.valueToJava2D(yBottom, dataArea, RectangleEdge.BOTTOM);
		double plotYBottomAsXLeft = xAxis.valueToJava2D(1.0 - yBottom, dataArea,
				RectangleEdge.BOTTOM);
		double plotXLeftAsYBottom =
				yAxis.valueToJava2D(1.0 - xLeft, dataArea, RectangleEdge.LEFT);

		Path2D shape = new Path2D.Double();
		/* Draw top of shape - always a horizontal line
		* @formatter:off
		* 
		*    ____
		*  
		* @formatter:on
		*/
		shape.moveTo(plotXLeft, plotYTop);
		if (drawTop) {
			shape.lineTo(plotXRight, plotYTop);
		} else {
			shape.moveTo(plotXRight, plotYTop);
		}
		// Draw the RHS of shape
		if (xRight > yBottom) {
			/* Bottom right of rectangle is outside triangle
			* @formatter:off
			*
			*     |
			*     |
			*    /
			*   /
			*
			* @formatter:on
			* */
			if (drawRight) {
				shape.lineTo(plotXRight, plotXRightAsYBottom);
			} else {
				shape.moveTo(plotXRight, plotXRightAsYBottom);
			}
			// Dont draw the '/' edge
			shape.moveTo(plotYBottomAsXRight, plotYBottom);
		} else {
			/* Bottom right is in - '|'
			* @formatter:off
			*
			*     |
			*     |
			*     |
			*     |
			*
			* @formatter:on
			*/
			if (drawRight) {
				shape.lineTo(plotXRight, plotYBottom);
			} else {
				shape.moveTo(plotXRight, plotYBottom);
			}
		}

		// Draw the bottom of shape
		if (yBottom < 1.0 - xLeft) {
			/* Bottom left is outside triangle
			* * @formatter:off
			* 
			*    \
			*     \
			*      \____
			*      
			* @formatter:on
			*/
			if (drawBottom) {
				shape.lineTo(plotYBottomAsXLeft, plotYBottom);
			} else {
				shape.moveTo(plotYBottomAsXLeft, plotYBottom);
			}
			// Dont draw the '\' edge
			shape.moveTo(plotXLeft, plotXLeftAsYBottom);
		} else {
			/*
			 * @formatter:off
			 * 
			 *    ______
			 *    
			 * @formatter:on
			 */
			if (drawBottom) {
				shape.lineTo(plotXLeft, plotYBottom);
			} else {
				shape.moveTo(plotXLeft, plotYBottom);
			}
		}

		/* Draw the LHS of shape - NB last side so dont need to move cursor if not drawing
		* @formatter:off
		*
		*     |
		*     |
		*     |
		*     |
		*
		* @formatter:on
		*/
		if (drawLeft) {
			shape.lineTo(plotXLeft, plotYTop);
		}
		g2.setStroke(new BasicStroke(1.0f));
		g2.draw(shape);

		// Restore settings
		g2.setPaint(oldPaint);
		g2.setStroke(oldStroke);
	}

}
