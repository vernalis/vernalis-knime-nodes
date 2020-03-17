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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.stream.DoubleStream;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;
import org.knime.core.data.property.ShapeFactory.Shape;

/**
 * A simple {@link DrawableDataObject} representing a single point on a
 * scatterplot. The point has a {@link Shape}, size and {@link Color} associated
 * with it, as well as (x,y) coordinates, and labels for each of the
 * shape/size/colour attributes
 * 
 * @author s.roughley
 *
 */
public class SimpleShapeDrawableDataObject implements DrawableDataObject,
		Comparable<SimpleShapeDrawableDataObject> {

	private final Shape shape;
	private final double x, y, size;
	private final Color colour;
	private final String colourLabel, shapeLabel, sizeLabel;

	/**
	 * @param x
	 *            The x coordinate
	 * @param y
	 *            The y coordinate
	 * @param shape
	 *            The KNIME shape
	 * @param size
	 *            The size
	 * @param colour
	 *            The colour
	 * @param shapeLabel
	 *            The shape label for use in a legend
	 * @param sizeLabel
	 *            The size label for use in a legend
	 * @param colourLabel
	 *            The colour label for use in a legend
	 */
	public SimpleShapeDrawableDataObject(double x, double y, Shape shape,
			double size, Color colour, String shapeLabel, String sizeLabel,
			String colourLabel) {
		this.shape = shape;
		this.x = x;
		this.y = y;
		this.size = size;
		this.colour = colour;
		this.colourLabel = colourLabel;
		this.shapeLabel = shapeLabel;
		this.sizeLabel = sizeLabel;
	}

	@Override
	public void draw(Graphics2D g2, Rectangle2D dataArea, ValueAxis xAxis,
			ValueAxis yAxis, PlotOrientation orientation) {
		// We need to translate from KNIME shapes to AWT shapes...
		ScaleableShapeTranslator sst = new ScaleableShapeTranslator();

		java.awt.Shape shape = sst.getAWTShape(getShape(), getSize());
		double transX =
				xAxis.valueToJava2D(getX(), dataArea, RectangleEdge.BOTTOM);
		double transY =
				yAxis.valueToJava2D(getY(), dataArea, RectangleEdge.LEFT);
		Color shapeColour = getColour();
		if (orientation == PlotOrientation.HORIZONTAL) {
			shape = ShapeUtilities.createTranslatedShape(shape, transY, transX);
		} else {
			shape = ShapeUtilities.createTranslatedShape(shape, transX, transY);
		}
		Paint oldPaint = g2.getPaint();
		g2.setPaint(shapeColour);
		g2.draw(shape);
		g2.fill(shape);
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

	/**
	 * @return the shape
	 */
	public final Shape getShape() {
		return shape;
	}

	/**
	 * @return the x
	 */
	public final double getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public final double getY() {
		return y;
	}

	/**
	 * @return the size
	 */
	public final double getSize() {
		return size;
	}

	/**
	 * @return the colour
	 */
	public final Color getColour() {
		return colour;
	}

	/**
	 * @return the colourLabel
	 */
	public final String getColourLabel() {
		return colourLabel;
	}

	/**
	 * @return the shapeLabel
	 */
	public final String getShapeLabel() {
		return shapeLabel;
	}

	/**
	 * @return the sizeLabel
	 */
	public final String getSizeLabel() {
		return sizeLabel;
	}

	@Override
	public int compareTo(SimpleShapeDrawableDataObject o) {
		int retVal = shape.toString().compareTo(o.shape.toString());
		if (retVal == 0) {
			retVal = Double.compare(x, o.x);
		}
		if (retVal == 0) {
			retVal = Double.compare(y, o.y);
		}
		if (retVal == 0) {
			retVal = Double.compare(size, o.size);
		}
		if (retVal == 0) {
			retVal = Integer.compare(colour.getRGB(), o.colour.getRGB());
		}
		if (retVal == 0) {
			retVal = Integer.compare(colour.getTransparency(),
					o.colour.getTransparency());
		}
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((colour == null) ? 0 : colour.hashCode());
		result = prime * result
				+ ((shape == null) ? 0 : shape.toString().hashCode());
		long temp;
		temp = Double.doubleToLongBits(size);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SimpleShapeDrawableDataObject)) {
			return false;
		}
		SimpleShapeDrawableDataObject other =
				(SimpleShapeDrawableDataObject) obj;
		if (colour == null) {
			if (other.colour != null) {
				return false;
			}
		} else if (!colour.equals(other.colour)) {
			return false;
		}
		if (shape == null) {
			if (other.shape != null) {
				return false;
			}
		} else if (!shape.toString().equals(other.shape.toString())) {
			return false;
		}
		if (Double.doubleToLongBits(size) != Double
				.doubleToLongBits(other.size)) {
			return false;
		}
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
			return false;
		}
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
			return false;
		}
		return true;
	}

}
