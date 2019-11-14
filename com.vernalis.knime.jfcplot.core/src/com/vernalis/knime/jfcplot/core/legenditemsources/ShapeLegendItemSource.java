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
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.knime.core.data.property.ShapeFactory.Shape;
import org.knime.ext.jfc.node.scatterplot.util.ShapeTranslator;

import com.vernalis.knime.jfcplot.core.drawabledataobject.SimpleShapeDrawableDataObject;

/**
 * A simple legend for a shape handler series.
 * 
 * @author s.roughley
 *
 */
public class ShapeLegendItemSource implements LegendItemSource {

	private final LegendItemCollection lic = new LegendItemCollection();

	/**
	 * Constructor, with black symbols in the legend
	 * 
	 * @param dataSeries
	 *            The data series to extract shapes from
	 * @param size
	 *            The shape size to use
	 */
	public ShapeLegendItemSource(
			Collection<SimpleShapeDrawableDataObject> dataSeries, double size) {
		this(dataSeries, size, Color.BLACK);
	}

	/**
	 * Constructor allowing setting of size and colour
	 * 
	 * @param dataSeries
	 *            The data series to extract shapes from
	 * @param size
	 *            The shape size to use
	 * @param color
	 *            The color to use for the plot symbols
	 */
	public ShapeLegendItemSource(
			Collection<SimpleShapeDrawableDataObject> dataSeries, double size,
			Color color) {

		// We need a unique mapping of labels to shape
		Map<String, Shape> shapes = dataSeries.stream()
				.collect(Collectors.toMap(
						SimpleShapeDrawableDataObject::getShapeLabel,
						SimpleShapeDrawableDataObject::getShape,
						(shape0, shape1) -> shape0));
		ShapeTranslator shapeTrans = new ShapeTranslator((float) size);

		for (Entry<String, Shape> series : shapes.entrySet()) {
			lic.add(new LegendItem(series.getKey(), null, null, null, true,
					shapeTrans.getAWTShape(series.getValue()), true, color,
					true, color, new BasicStroke(1.0f), false,
					new Rectangle2D.Double(-size / 2.0, -size / 2.0, size,
							size),
					new BasicStroke(1.0f), color));
		}
	}

	@Override
	public LegendItemCollection getLegendItems() {
		return lic;
	}
}
