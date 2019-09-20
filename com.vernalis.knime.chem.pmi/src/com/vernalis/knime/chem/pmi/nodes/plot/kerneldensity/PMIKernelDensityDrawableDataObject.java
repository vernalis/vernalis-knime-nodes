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
import org.jfree.chart.renderer.PaintScale;
import org.jfree.ui.RectangleEdge;

import com.vernalis.knime.jfcplot.core.drawabledataobject.DrawableDataObject;
import com.vernalis.knime.plot.nodes.kerneldensity.KernelDensity2DDrawableDataObject;
import com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityFunction;

public class PMIKernelDensityDrawableDataObject
		extends KernelDensity2DDrawableDataObject {

	/**
	 * @param kd
	 * @param c
	 * @param scale
	 */
	public PMIKernelDensityDrawableDataObject(KernelDensityFunction kd,
			Collection<? extends DrawableDataObject> c, PaintScale scale) {
		super(kd, c, scale);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.pmi.util.kerneldensity.KernelDensity2D#
	 * drawRect(java.awt.Graphics2D, java.awt.geom.Rectangle2D, double, double,
	 * java.awt.Paint, org.jfree.chart.axis.ValueAxis,
	 * org.jfree.chart.axis.ValueAxis)
	 */
	@Override
	protected void drawRect(Graphics2D g2, Rectangle2D dataArea,
			ValueAxis xAxis, ValueAxis yAxis, double x, double y, Paint paint) {
		// We need to modify to not draw outside the triangle bounds
		// variable for original composite settings
		Paint oldPaint = g2.getPaint();
		Stroke oldStroke = g2.getStroke();

		g2.setPaint(paint);

		// Calculate the coordinates of the corners in plot units - dont let the
		// corners be beyond the outer rectangle
		// y runs from top of screen increasing downwards - so we need to
		// 'invert' the ytop/ybottom names

		// The coordinates in nPMI space of the rectangle corners
		double xLeft = Math.max(x - getXOffSet(), 0.0);
		double xRight = Math.min(x + getXOffSet(), 1.0);
		double yTop = Math.min(y + getYOffSet(), 1.0);
		double yBottom = Math.max(y - getYOffSet(), 0.5);

		// If the top edge (the wider edge) ends outside the bounding triangle,
		// then truncate it

		if (xLeft + yTop < 1.0) {
			/*
			 * Top left corner outside
			 * @formatter:off
			 *      _\__
			 *      | \|
			 *      |  \
			 *  	|  |\
			 *  	|  | \
			 *      |__|  \
			 *             \
			 *  @formatter:on
			 */
			xLeft = 1 - yTop;
		}
		if (xRight > yTop) {
			/*
			 * Top right corner outside
			 * @formatter:off
			 *      __/_
			 *      |/ |
			 *      /  |
			 *     /|  |
			 *    / |  |
			 *   /  |__|
			 *  /       
			 *  @formatter:on
			 */
			xRight = yTop;
		}
		// If the bottom is outside the bounding triangle, raise it. The
		// possibility of one bottom corner in and one out is handled later
		// during the actual plotting
		if (yBottom < xLeft && yBottom < xRight) {
			/*
			 * @formatter:off
			 *  	___ /
			 *      | |/
			 *      | /
			 *  	|/|
			 *  	/ |
			 *     /|_|
			 *    / 
			 * @formatter:on
			 */
			yBottom = xLeft;
		}
		if (xLeft + yBottom < 1.0 && xRight + yBottom < 1.0) {
			/*
			 * @formatter:off
			 *    \ ___
			 *     \| |
			 *      \ |
			 *  	|\|
			 *  	| \
			 *      |_|\
			 *          \ 
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
		if (xRight < yBottom && xLeft + yBottom > 1.0) {
			// generating and drawing the rectangle - all 4 corners are in the
			// shape bounds

			Rectangle2D rect =
					new Rectangle2D.Double(Math.min(plotXLeft, plotXRight),
							Math.min(plotYBottom, plotYTop),
							Math.abs(plotXRight - plotXLeft),
							Math.abs(plotYTop - plotYBottom));
			g2.fill(rect);
			g2.setStroke(new BasicStroke(1.0f));
			g2.draw(rect);

		} else {
			// At least one of bottom corners is outside of bounding triangle
			Path2D shape = new Path2D.Double();
			// Draw top of shape
			shape.moveTo(plotXLeft, plotYTop);
			shape.lineTo(plotXRight, plotYTop);

			// Draw the RHS of shape
			if (xRight > yBottom) {
				// Bottom right of rectangle is outside triangle
				shape.lineTo(plotXRight, plotXRightAsYBottom);
				shape.lineTo(plotYBottomAsXRight, plotYBottom);
			} else {
				// Bottom right is in
				shape.lineTo(plotXRight, plotYBottom);
			}

			// Draw the bottom of shape
			if (yBottom < 1.0 - xLeft) {
				// Bottom left is outside triangle
				shape.lineTo(plotYBottomAsXLeft, plotYBottom);
				shape.lineTo(plotXLeft, plotXLeftAsYBottom);
			} else {
				shape.lineTo(plotXLeft, plotYBottom);
			}

			// Draw the LHS of shape
			shape.closePath();
			g2.fill(shape);
			g2.setStroke(new BasicStroke(1.0f));
			g2.draw(shape);

		}

		// Restore settings
		g2.setPaint(oldPaint);
		g2.setStroke(oldStroke);
	}

}
