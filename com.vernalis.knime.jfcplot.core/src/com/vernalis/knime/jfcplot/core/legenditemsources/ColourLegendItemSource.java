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
import java.util.stream.Collectors;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.knime.core.data.property.ShapeFactory.Shape;
import org.knime.ext.jfc.node.scatterplot.util.ShapeTranslator;

import com.vernalis.knime.jfcplot.core.drawabledataobject.SimpleShapeDrawableDataObject;

/**
 * A simple legend for a colour series.
 * 
 * @author s.roughley
 *
 */
public class ColourLegendItemSource implements LegendItemSource {

	private final LegendItemCollection lic = new LegendItemCollection();

	/**
	 * Constructor from a map of colours and names
	 * 
	 * @param colours
	 *            The colour/name pairings
	 * @param size
	 *            The size of plot symbol
	 * @param shape
	 *            The shape to use in the legend
	 */
	public ColourLegendItemSource(Map<String, Color> colours, double size,
			Shape shape) {
		ShapeTranslator shapeTrans = new ShapeTranslator((float) size);
		colours.entrySet()
				.forEach(series -> lic.add(new LegendItem(series.getKey(), null,
						null, null, true, shapeTrans.getAWTShape(shape), true,
						series.getValue(), true, series.getValue(),
						new BasicStroke(1.0f), false,
						new Rectangle2D.Double(-size / 2.0, -size / 2.0, size,
								size),
						new BasicStroke(1.0f), series.getValue())));
	}

	/**
	 * Convenience constructor from a collection of
	 * {@link SimpleShapeDrawableDataObject}
	 * 
	 * @param dataSeries
	 *            The {@link SimpleShapeDrawableDataObject} collection
	 *            representing the data series
	 * @param size
	 *            The size of plot symbol
	 * @param shape
	 *            The shape to use in the legend
	 */
	public ColourLegendItemSource(
			Collection<SimpleShapeDrawableDataObject> dataSeries, double size,
			Shape shape) {

		// We need a unique mapping of labels to colour
		this(dataSeries.stream()
				.collect(Collectors.toMap(
						SimpleShapeDrawableDataObject::getColourLabel,
						SimpleShapeDrawableDataObject::getColour,
						(col0, col1) -> col0)),
				size, shape);

	}

	@Override
	public LegendItemCollection getLegendItems() {
		return lic;
	}
}
