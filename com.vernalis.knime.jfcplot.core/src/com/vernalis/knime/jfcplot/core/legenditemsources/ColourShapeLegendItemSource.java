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
 * A {@link LegendItemSource} for when both shape and colour are controlled by
 * the same column and so the key should contain both
 * 
 * @author s.roughley
 *
 */
public class ColourShapeLegendItemSource implements LegendItemSource {

	private final LegendItemCollection lic = new LegendItemCollection();

	/**
	 * @param data
	 *            The data to build the legend from
	 * @param size
	 *            The size of the symbol
	 */
	public ColourShapeLegendItemSource(
			Collection<SimpleShapeDrawableDataObject> data, double size) {
		super();
		// Colour and shape labels should be identical!
		// We need a unique mapping of labels to shape and colour
		Map<String, Color> colors = data.stream()
				.collect(Collectors.toMap(
						SimpleShapeDrawableDataObject::getColourLabel,
						SimpleShapeDrawableDataObject::getColour,
						(col0, col1) -> col0));
		Map<String, Shape> shapes = data.stream()
				.collect(Collectors.toMap(
						SimpleShapeDrawableDataObject::getColourLabel,
						SimpleShapeDrawableDataObject::getShape,
						(shape0, shape1) -> shape0));
		ShapeTranslator shapeTrans = new ShapeTranslator((float) size);
		colors.keySet()
				.forEach(series -> lic.add(new LegendItem(series, null, null,
						null, true, shapeTrans.getAWTShape(shapes.get(series)),
						true, colors.get(series), true, colors.get(series),
						new BasicStroke(1.0f), false,
						new Rectangle2D.Double(-size / 2.0, -size / 2.0, size,
								size),
						new BasicStroke(1.0f), colors.get(series))));
	}

	@Override
	public LegendItemCollection getLegendItems() {
		return lic;
	}

}
