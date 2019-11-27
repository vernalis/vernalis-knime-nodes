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
package com.vernalis.knime.chem.pmi.nodes.plot.convexhull;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.DoubleStream;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;
import org.knime.core.data.property.ShapeFactory.Shape;
import org.knime.ext.jfc.node.scatterplot.util.ShapeTranslator;

import com.vernalis.knime.jfcplot.core.drawabledataobject.DrawableDataObject;

/**
 * A class representing a 2D Convex Hull from a set of points. Points can be
 * added by calling {@link #addPoint(double, double)} until a call to
 * {@link #generateHull()} is made, whereupon the hull is closed. Implements the
 * Quick Hull algorithm
 * 
 * @author s.roughley
 *
 */
class ConvexHull implements Iterable<ConvexHull.DataPoint>, DrawableDataObject {

	static final double DEFAULT_PRECISION = 1e-3;

	private double area = 0.0;

	private Set<DataPoint> dataPoints;

	private final String ID;

	private final Color lineColour, fillColour;

	private final double percentPointsThreshold;

	private final double pmiTriangleArea;

	private final double precision;

	private final Shape shape;

	private final ShapeTranslator shapeTrans;

	private final boolean showBondaries;

	private boolean showPoints;

	boolean isClosed = false;

	/**
	 * Overloaded constructor using the default precision
	 * ({@value #DEFAULT_PRECISION})
	 * 
	 * @param ID
	 *            A label for the hull
	 * @param shape
	 *            The shape to use for points when shown
	 * @param colour
	 *            The colour to use for the hull
	 * @param fillAlpha
	 *            The transparency of the fill colour
	 * @param showBoundaries
	 *            Should the exterior hull boundaries be drawn
	 * @param symbolSize
	 *            The plot symbol size
	 * @param percentPointThreshold
	 *            The percentage of the PMI Triangle area in order to determine
	 *            whether this hull should have it's individual boundary point
	 *            plotted in order to make small hulls visible
	 */
	ConvexHull(String ID, Shape shape, Color colour, int fillAlpha,
			boolean showBoundaries, double symbolSize,
			double percentPointThreshold) {
		this(ID, DEFAULT_PRECISION, shape, colour, fillAlpha, showBoundaries,
				symbolSize, percentPointThreshold);
	}

	/**
	 * @param ID
	 *            A label for the hull
	 * @param precision
	 *            The numerical precision to use in double comparisons
	 * @param shape
	 *            The shape to use for points when shown
	 * @param colour
	 *            The colour to use for the hull
	 * @param fillAlpha
	 *            The transparency of the fill colour
	 * @param showBoundaries
	 *            Should the exterior hull boundaries be drawn
	 * @param symbolSize
	 *            The plot symbol size
	 * @param percentPointThreshold
	 *            The percentage of the PMI Triangle area in order to determine
	 *            whether this hull should have it's individual boundary point
	 *            plotted in order to make small hulls visible
	 */
	ConvexHull(String ID, double precision, Shape shape, Color colour,
			int fillAlpha, boolean showBoundaries, double symbolSize,
			double percentPointThreshold) {
		this.precision = precision;
		this.ID = ID;
		lineColour = colour;
		fillColour =
				new Color(colour.getRed(), colour.getGreen(), colour.getBlue(),
						fillAlpha < 0 ? 0 : fillAlpha > 255 ? 255 : fillAlpha);
		this.shape = shape;
		// The points will be de-duplicated and sorted on addition
		dataPoints = new TreeSet<>();
		shapeTrans = new ShapeTranslator((float) symbolSize);
		this.showBondaries = showBoundaries;
		pmiTriangleArea =
				DataPoint.triangleArea(new DataPoint(0.0, 1.0, precision),
						new DataPoint(1.0, 1.0, precision),
						new DataPoint(0.5, 0.5, precision));
		this.percentPointsThreshold = percentPointThreshold;
	}

	@Override
	public void draw(Graphics2D g2, Rectangle2D dataArea, ValueAxis xAxis,
			ValueAxis yAxis, PlotOrientation orientation) {

		boolean isFirst = true;
		g2.setPaint(lineColour);
		Path2D.Double hullShape = new Path2D.Double();
		for (DataPoint point : this) {
			double transX = xAxis.valueToJava2D(point.getXDbl(), dataArea,
					RectangleEdge.BOTTOM);
			double transY = yAxis.valueToJava2D(point.getYDbl(), dataArea,
					RectangleEdge.LEFT);
			if (isFirst) {
				if (orientation == PlotOrientation.HORIZONTAL) {
					hullShape.moveTo(transY, transX);
				} else {
					hullShape.moveTo(transX, transY);
				}
				isFirst = false;
			} else {
				if (orientation == PlotOrientation.HORIZONTAL) {
					hullShape.lineTo(transY, transX);
				} else {
					hullShape.lineTo(transX, transY);
				}
			}
			if (isShowPoints()) {
				if (orientation == PlotOrientation.HORIZONTAL) {
					g2.draw(ShapeUtilities.createTranslatedShape(
							shapeTrans.getAWTShape(shape), transY, transX));
				} else {
					g2.draw(ShapeUtilities.createTranslatedShape(
							shapeTrans.getAWTShape(shape), transX, transY));
				}
			}
		}
		hullShape.closePath();
		if (showBondaries) {
			g2.draw(hullShape);
		}
		g2.setPaint(fillColour);
		g2.fill(hullShape);
	}

	@Override
	public DoubleStream getXStream() {
		return getHullPoints().stream().mapToDouble(x -> x.getXDbl());
	}

	@Override
	public DoubleStream getYStream() {
		return getHullPoints().stream().mapToDouble(x -> x.getYDbl());
	}

	@Override
	public Iterator<ConvexHull.DataPoint> iterator()
			throws IllegalStateException {
		checkClosed();
		return dataPoints.iterator();
	}

	private void checkClosed() throws IllegalStateException {
		if (!isClosed) {
			throw new IllegalStateException(
					"The hull has not been calculated. Call #generateHull() first");

		}
	}

	/**
	 * This is the recursive partitioning method
	 * 
	 * @param A
	 *            the datapoint at the start of the line
	 * @param B
	 *            the datapoint at the end of the line
	 * 
	 */
	private synchronized void hullSet(DataPoint A, DataPoint B,
			List<DataPoint> points, List<DataPoint> hull) {

		if (points.size() == 0) {
			// We are done
			return;
		}

		// Find the correct place in the
		int insertPos = hull.indexOf(B);

		if (points.size() == 1) {
			// Just need to add the 1 remaining point and we are done
			// We can add it safely because we never include in 'points' and
			// which are colinear with the line
			hull.add(insertPos, points.remove(0));
			return;
		}

		// Now we need to find the furthest point from line AB
		long maxDist = Long.MIN_VALUE;
		int furthestPt = -1;
		for (int i = 0; i < points.size(); i++) {
			DataPoint P = points.get(i);
			long distP = P.distFromLine(A, B);
			if (distP > maxDist) {
				maxDist = distP;
				furthestPt = i;
			}
		}

		// We remove it from the test points, and add it to the hull
		DataPoint P = points.remove(furthestPt);
		hull.add(insertPos, P);

		// Now we partition the remaining points into those above AP and those
		// above PB, and discard the others (they are inside triangle ABP, and
		// so inside the hull)
		List<DataPoint> leftAP = new ArrayList<>();
		List<DataPoint> leftPB = new ArrayList<>();
		for (int i = 0; i < points.size(); i++) {
			DataPoint M = points.get(i);
			if (M.isAbove(A, P) == 1) {
				leftAP.add(M);
			} else if (M.isAbove(P, B) == 1) {
				leftPB.add(M);
			}
			// M Can't be above AP and above PB because it would be further from
			// AB then P, and so would be P
		}

		// Now recurse on the two sets on either side of the new point, P
		hullSet(A, P, leftAP, hull);
		hullSet(P, B, leftPB, hull);

	}

	/**
	 * Add a new point to the set of points to be considered for hulling
	 * 
	 * @param x
	 *            The x coordinate
	 * @param y
	 *            The y coordinate
	 * @throws IllegalStateException
	 *             If the hull has been closed
	 */
	void addPoint(double x, double y) throws IllegalStateException {
		if (!isClosed) {
			// We let the datapoint take care of the precision rounding
			dataPoints.add(new DataPoint(x, y, precision));
		} else {
			throw new IllegalStateException(
					"The hull has been closed.  No further points can be added");
		}
	}

	/**
	 * Method to generate the convex hull once all points are added. Once
	 * called, the hull is closed and no further points can be added
	 */
	synchronized void generateHull() {
		if (isClosed || dataPoints.size() <= 2) {
			// We already have a hull (3 points could be co-linear!)
			isClosed = true;
			return;
		}

		// We close the hull now to stop any further points being added from a
		// different thread
		isClosed = true;

		// The points are sorted in ascending x value
		List<DataPoint> hull = new ArrayList<>();

		// Points are sorted by x, so min and max points are first and last -
		// add
		// them to the hull and remove from remaining points
		DataPoint A = ((TreeSet<DataPoint>) dataPoints).first();
		hull.add(A);
		DataPoint B = ((TreeSet<DataPoint>) dataPoints).last();
		hull.add(B);
		dataPoints.remove(A);
		dataPoints.remove(B);

		// Now partition remaining points between above and below the line AB
		List<DataPoint> below = new ArrayList<>();
		List<DataPoint> above = new ArrayList<>();
		for (DataPoint P : dataPoints) {
			int side = P.isAbove(A, B);
			if (side == -1) {
				below.add(P);
			} else if (side == 1) {
				above.add(P);
			}
			// Otherwise it is colinear with A and B and we ignore it
		}
		hullSet(A, B, above, hull);
		hullSet(B, A, below, hull);

		// hull is now the points on the hull - keeping them in order!
		dataPoints = new LinkedHashSet<>(hull);

		// Now calculate the area - the sum of all the triangles formed from an
		// arbitrary 'first' point
		area = 0.0;
		for (int i = 1; i < hull.size() - 1; i++) {
			area += DataPoint.triangleArea(hull.get(0), hull.get(i),
					hull.get(i + 1));
		}

		showPoints = getHullPoints().size() <= 2 || getArea()
				/ getPmiTriangleArea() < (getPercentPointsThreshold() / 100.0);

	}

	/**
	 * @return The area covered by the hull
	 * @throws IllegalStateException
	 *             is the hull has not been closed by a completed call to
	 *             {@link #generateHull()}
	 */
	double getArea() throws IllegalStateException {
		checkClosed();
		return area;
	}

	/**
	 * @return the fillColour
	 */
	final Color getFillColour() {
		return fillColour;
	}

	/**
	 * @return The points on the perimeter of the hull
	 * @throws IllegalStateException
	 *             is the hull has not been closed by a completed call to
	 *             {@link #generateHull()}
	 */
	Set<DataPoint> getHullPoints() throws IllegalStateException {
		checkClosed();
		return dataPoints;
	}

	/**
	 * @return the iD
	 */
	final String getID() {
		return ID;
	}

	/**
	 * @return the lineColour
	 */
	final Color getLineColour() {
		return lineColour;
	}

	/**
	 * @return the percentPointsThreshold
	 */
	final double getPercentPointsThreshold() {
		return percentPointsThreshold;
	}

	/**
	 * @return the PMI triangle area
	 */
	final double getPmiTriangleArea() {
		return pmiTriangleArea;
	}

	/**
	 * @return the precision
	 */
	final double getPrecision() {
		return precision;
	}

	/**
	 * @return the shape
	 */
	final Shape getShape() {
		return shape;
	}

	/**
	 * @return the shapeTrans
	 */
	final ShapeTranslator getShapeTrans() {
		return shapeTrans;
	}

	/**
	 * @return the showBondaries
	 */
	final boolean isShowBondaries() {
		return showBondaries;
	}

	/**
	 * @return Should the hull points be shown when drawing? Yes if 2 or fewer
	 *         points, of the area is below the
	 *         {@link #getPercentPointsThreshold()}
	 * @throws IllegalStateException
	 *             is the hull has not been closed by a completed call to
	 *             {@link #generateHull()}
	 * 
	 */
	final boolean isShowPoints() throws IllegalStateException {
		checkClosed();
		return showPoints;
	}

	/**
	 * A simple class to hold a point for hull determination
	 * 
	 * @author s.roughley
	 *
	 */
	static class DataPoint implements Comparable<DataPoint> {

		double precision;
		long x, y;

		DataPoint(double x, double y, double precision) {
			this.x = (long) (x / precision);
			this.y = (long) (y / precision);
			this.precision = precision;
		}

		double getXDbl() {
			return x * precision;
		}

		double getYDbl() {
			return y * precision;
		}

		/**
		 * Determine whether this point is above or below a line between two
		 * points A and B
		 * 
		 * @return 1 if above, -1 if below, 0 if co-linear
		 */
		int isAbove(DataPoint lineA, DataPoint lineB) {
			long comp = ((lineB.x - lineA.x) * (this.y - lineA.y)
					- (lineB.y - lineA.y) * (this.x - lineA.x));
			if (comp > 0L) {
				return 1;
			} else if (comp < 0L) {
				return -1;
			}
			return 0;
		}

		/**
		 * Determine the area of a triangle between three points
		 * 
		 * @param point0
		 * @param point1
		 * @param point2
		 * @return
		 */
		static double triangleArea(DataPoint point0, DataPoint point1,
				DataPoint point2) {
			return Math.abs((point0.x * (point1.y - point2.y)
					+ point1.x * (point2.y - point0.y)
					+ point2.x * (point0.y - point1.y)) / 2.0);
		}

		/**
		 * Determine the distance of this point to a line between two points A
		 * and B
		 * 
		 * @return the distance
		 */
		long distFromLine(DataPoint lineA, DataPoint lineB) {
			return Math.abs((lineB.x - lineA.x) * (this.y - lineA.y)
					- (lineB.y - lineA.y) * (this.x - lineA.x));
		}

		@Override
		public int compareTo(DataPoint o) {
			int retVal = Long.compare(x, o.x);
			if (retVal != 0) {
				return retVal;
			}
			return Long.compare(y, o.y);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DataPoint other = (DataPoint) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
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
			result = prime * result + (int) (x ^ (x >>> 32));
			result = prime * result + (int) (y ^ (y >>> 32));
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("DataPoint [x=");
			builder.append(x);
			builder.append(", y=");
			builder.append(y);
			builder.append("]");
			return builder.toString();
		}
	}
}
