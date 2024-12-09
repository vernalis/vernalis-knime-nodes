/*******************************************************************************
 * Copyright (c) 2019, 2020, Vernalis (R&D) Ltd
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
package com.vernalis.knime.chem.pmi.nodes.plot.abstrct;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.knime.core.util.Pair;
import org.knime.ext.jfc.node.base.KnimeChartTheme;

import com.vernalis.knime.jfcplot.core.drawabledataobject.BottomCenteredTextDrawableDataObject;
import com.vernalis.knime.jfcplot.core.drawabledataobject.DrawableDataObject;
import com.vernalis.knime.jfcplot.core.drawabledataobject.TopCenteredTextDrawableDataObject;
import com.vernalis.knime.jfcplot.core.drawabledataobject.TriangleDrawableDataObject;

/**
 * A {@link DrawableDataObject} to draw the bounds triangle for the PMI Plot.
 * The objects are cached as singletons by the colour of lines and labels, and
 * are accessed via the static {@link #get(Color, Color)} method
 * 
 * @author s.roughley
 *
 */
public class PmiTriangleBondsDrawableDataObject implements DrawableDataObject {

	@SuppressWarnings("serial")
	private static final Map<Pair<Color, Color>, PmiTriangleBondsDrawableDataObject> CACHE =
			Collections.synchronizedMap(
					new LinkedHashMap<Pair<Color, Color>, PmiTriangleBondsDrawableDataObject>(
							16, 0.75f, true) {

						/*
						 * (non-Javadoc)
						 * 
						 * @see
						 * java.util.LinkedHashMap#removeEldestEntry(java.util.
						 * Map.Entry)
						 */
						@Override
						protected boolean removeEldestEntry(
								Entry<Pair<Color, Color>, PmiTriangleBondsDrawableDataObject> eldest) {
							return size() > 50;
						}

					});

	private final BottomCenteredTextDrawableDataObject rodLabel, sphereLabel;
	private final TopCenteredTextDrawableDataObject discLabel;
	private final TriangleDrawableDataObject boundsTriangle;

	/**
	 * Private constructor
	 * 
	 * @param lineColour
	 *            The colour of the bounding line
	 * @param labelColour
	 *            The colour of the vertex labels
	 */
	private PmiTriangleBondsDrawableDataObject(Color lineColour,
			Color labelColour) {
		this.rodLabel = new BottomCenteredTextDrawableDataObject(0.0, 1.005,
				"Rod", labelColour,
				((KnimeChartTheme) ChartFactory.getChartTheme())
						.getSmallFont());
		this.sphereLabel = new BottomCenteredTextDrawableDataObject(1.0, 1.005,
				"Sphere", labelColour,
				((KnimeChartTheme) ChartFactory.getChartTheme())
						.getSmallFont());
		this.discLabel = new TopCenteredTextDrawableDataObject(0.5, 0.5, "Disc",
				labelColour, ((KnimeChartTheme) ChartFactory.getChartTheme())
						.getSmallFont());
		this.boundsTriangle = new TriangleDrawableDataObject(0.0, 1.0, 1.0, 1.0,
				0.5, 0.5, lineColour, new BasicStroke(1.0f));
	}

	public static PmiTriangleBondsDrawableDataObject get(Color lineColour,
			Color labelColour) {
		return CACHE.computeIfAbsent(new Pair<>(lineColour, labelColour),
				k -> new PmiTriangleBondsDrawableDataObject(k.getFirst(),
						k.getSecond()));
	}

	@Override
	public void draw(Graphics2D g2, Rectangle2D dataArea, ValueAxis xAxis,
			ValueAxis yAxis, PlotOrientation orientation) {
		Stroke oldStroke = g2.getStroke();
		Font oldFont = g2.getFont();
		Paint oldPaint = g2.getPaint();

		// Add the triangle bounds to the plot
		boundsTriangle.draw(g2, dataArea, xAxis, yAxis, orientation);

		// And the vertex labels
		rodLabel.draw(g2, dataArea, xAxis, yAxis, orientation);
		sphereLabel.draw(g2, dataArea, xAxis, yAxis, orientation);
		discLabel.draw(g2, dataArea, xAxis, yAxis, orientation);

		// Restore the settings
		g2.setStroke(oldStroke);
		g2.setFont(oldFont);
		g2.setPaint(oldPaint);

	}

	@Override
	public DoubleStream getXStream() {
		return Stream.of(rodLabel, sphereLabel, discLabel, boundsTriangle)
				.flatMapToDouble(ddo -> ddo.getXStream());
	}

	@Override
	public DoubleStream getYStream() {
		return Stream.of(rodLabel, sphereLabel, discLabel, boundsTriangle)
				.flatMapToDouble(ddo -> ddo.getYStream());

	}

}
