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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;

/**
 * Simple Legend object class for the hull chart. Determines whether to show the
 * point plot shape in addition to the colour 'blob'
 * 
 * @author S.Roughley
 *
 */
class HullLegendItemSource implements LegendItemSource {

	private final Collection<ConvexHull> data;
	private final boolean showRelativeArea;

	/**
	 * @param data
	 *            The hull data
	 * @param showRelativeArea
	 *            Should the text of the legend include the relative area?
	 */
	HullLegendItemSource(Collection<ConvexHull> data,
			boolean showRelativeArea) {
		this.data = data;
		this.showRelativeArea = showRelativeArea;
	}

	@Override
	public LegendItemCollection getLegendItems() {

		LegendItemCollection retVal = new LegendItemCollection();
		for (ConvexHull item : data) {
			// Show the shape followed by the outline/fill
			float size = item.getShapeTrans().size();
			Area ar = new Area(new Rectangle2D.Double(1.1 * size + size / 2.0,
					-size / 2.0, size, size));
			if (item.isShowPoints()) {
				ar.add(new Area(
						item.getShapeTrans().getAWTShape(item.getShape())));
			}
			LegendItem legItem =
					new LegendItem(
							item.getID() + (showRelativeArea
									? String.format(" (%.2f)",
											100.0 * item.getArea()
													/ item.getPmiTriangleArea())
									: ""),
							null, null, null, true,
							/* new Rectangle2D.Double(-5.0, -5.0, 10.0, 10.0) */ ar,
							true, item.getFillColour(), item.isShowBondaries(),
							item.getLineColour(), new BasicStroke(1.0f), false,
							new Rectangle2D.Double(-5.0, -0.0, 10.0, 1.0),
							new BasicStroke(1.0f), Color.red);
			retVal.add(legItem);
		}
		return retVal;
	}

}
