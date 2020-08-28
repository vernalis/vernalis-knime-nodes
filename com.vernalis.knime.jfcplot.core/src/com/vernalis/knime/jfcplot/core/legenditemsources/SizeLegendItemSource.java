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
package com.vernalis.knime.jfcplot.core.legenditemsources;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.property.ShapeFactory.Shape;
import org.knime.core.data.property.SizeHandler;
import org.knime.ext.jfc.node.scatterplot.util.ShapeTranslator;

/**
 * A simple legend item source for a size. The minimum and maximum size are
 * shown with the indicated KNIME {@link Shape}, along with the values.
 * Optionally, a half-way point is also added
 * 
 * @author s.roughley
 *
 */
public class SizeLegendItemSource implements LegendItemSource {

	private final LegendItemCollection lic = new LegendItemCollection();

	/**
	 * @param shape
	 *            The shape to use
	 * @param size
	 *            The base size to use
	 * @param handler
	 *            The {@link SizeHandler}
	 * @param minSize
	 *            The minimum size
	 * @param maxSize
	 *            The maximum size
	 * @param addMidPoint
	 *            Should a mid-point be added?
	 */
	public SizeLegendItemSource(Shape shape, double size, SizeHandler handler,
			double minSize, double maxSize, boolean addMidPoint) {

		float minSizeScaled =
				(float) (size * handler.getSizeFactor(new DoubleCell(minSize)));
		float maxSizeScaled =
				(float) (size * handler.getSizeFactor(new DoubleCell(maxSize)));
		ShapeTranslator shapeTrans = new ShapeTranslator(minSizeScaled);
		// The Minimum size shape has to occupy the same space as the maximum
		// size shape
		lic.add(new LegendItem(String.format("%.3f     ", minSize), null, null,
				null, true, shapeTrans.getAWTShape(shape), true, Color.BLACK,
				false, Color.BLACK, new BasicStroke(1.0f), false,
				new Rectangle2D.Double(-maxSizeScaled / 2.0,
						-maxSizeScaled / 2.0, maxSizeScaled, maxSizeScaled),
				new BasicStroke(1.0f), Color.BLACK));

		if (addMidPoint) {
			double midPoint = minSize + maxSize / 2.0;
			float midPointScaled = (float) (size
					* handler.getSizeFactor(new DoubleCell(midPoint)));
			shapeTrans = new ShapeTranslator(midPointScaled);
			lic.add(new LegendItem(String.format("%.3f     ", midPoint), null,
					null, null, true, shapeTrans.getAWTShape(shape), true,
					Color.BLACK, false, Color.BLACK, new BasicStroke(1.0f),
					false,
					new Rectangle2D.Double(-maxSizeScaled / 2.0,
							-maxSizeScaled / 2.0, maxSizeScaled, maxSizeScaled),
					new BasicStroke(1.0f), Color.BLACK));
		}

		shapeTrans = new ShapeTranslator(maxSizeScaled);
		lic.add(new LegendItem(String.format("%.3f", maxSize), null, null, null,
				true, shapeTrans.getAWTShape(shape), true, Color.BLACK, false,
				Color.BLACK, new BasicStroke(1.0f), false,
				new Rectangle2D.Double(-maxSizeScaled / 2.0,
						-maxSizeScaled / 2.0, maxSizeScaled, maxSizeScaled),
				new BasicStroke(1.0f), Color.BLACK));
	}

	@Override
	public LegendItemCollection getLegendItems() {
		return lic;
	}

}
