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

import java.awt.Shape;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.knime.core.data.DataCell;
import org.knime.core.data.property.ColorModelNominal;

/**
 * A simple legend for a nominal colour series all using the same, supplied
 * shape, or the default if no shape is supplied
 * 
 * @author s.roughley
 *
 */
public class ColorModelNominalLegendItemSource implements LegendItemSource {

	private final LegendItemCollection legItems = new LegendItemCollection();

	/**
	 * Override constructor using a small square block of colour
	 * 
	 * @param colModel
	 *            The {@link ColorModelNominal}
	 */
	public ColorModelNominalLegendItemSource(ColorModelNominal colModel) {
		this(colModel, null);
	}

	/**
	 * Constructor using the supplied shape
	 * 
	 * @param colModel
	 *            The {@link ColorModelNominal}
	 * @param shape
	 *            The shape to use
	 */
	public ColorModelNominalLegendItemSource(ColorModelNominal colModel,
			Shape shape) {
		for (DataCell dc : colModel.getValues()) {
			LegendItem legItem = shape == null
					? new LegendItem(dc.isMissing() ? "Missing" : dc.toString(),
							colModel.getColorAttr(dc).getColor())
					: new LegendItem(dc.isMissing() ? "Missing" : dc.toString(),
							null, null, null, shape,
							colModel.getColorAttr(dc).getColor());
			legItems.add(legItem);
		}
	}

	@Override
	public LegendItemCollection getLegendItems() {
		return legItems;
	}
}
