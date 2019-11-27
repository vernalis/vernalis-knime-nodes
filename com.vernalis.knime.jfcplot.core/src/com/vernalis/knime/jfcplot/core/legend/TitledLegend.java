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
package com.vernalis.knime.jfcplot.core.legend;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.block.ColumnArrangement;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.Title;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.knime.ext.jfc.node.base.KnimeChartTheme;

/**
 * Convenience class to create a legend ('Title' in JFreeChart-speak) with a
 * title. By default, the legend is set to be placed on the RHS of the plot.
 * This can be changed with {@link #setPosition(RectangleEdge)}
 * 
 * @author s.roughley
 *
 */
@SuppressWarnings("serial")
public class TitledLegend extends CompositeTitle {

	/**
	 * Convenience constructor - creates a new {@link LegendTitle} from the
	 * {@link LegendItemSource}
	 * 
	 * @param title
	 *            The legend title
	 * @param lis
	 *            The {@link LegendItemSource} containing the legend items
	 */
	public TitledLegend(String title, LegendItemSource lis) {
		this(title, new LegendTitle(lis, new ColumnArrangement(),
				new ColumnArrangement()));
	}

	/**
	 * Constructor to add a title to a legend
	 * 
	 * @param title
	 *            The legend titel
	 * @param legend
	 *            The pre-built legend
	 */
	public TitledLegend(String title, Title legend) {
		super(new BlockContainer(new BorderArrangement()));
		BlockContainer bc = getContainer();
		// We use a legend rather than a TextTitle to ensure font stays in
		// keeping with content

		LegendTitle legendTitle = new LegendTitle(new LegendItemSource() {

			@Override
			public LegendItemCollection getLegendItems() {
				LegendItemCollection lic = new LegendItemCollection();
				LegendItem titleLegItem = new LegendItem(title, null, null,
						null, true,
						new Rectangle2D.Double(-5.0, -0.0, 10.0, 1.0), false,
						Color.BLACK, false, Color.BLACK, new BasicStroke(1.0f),
						false, new Rectangle2D.Double(-5.0, -0.0, 10.0, 1.0),
						new BasicStroke(1.0f), Color.red);
				Font labelFont = titleLegItem.getLabelFont();
				if (labelFont != null) {
					labelFont = labelFont.deriveFont(Font.BOLD);
				} else {
					labelFont = ((KnimeChartTheme) ChartFactory.getChartTheme())
							.getRegularFont().deriveFont(Font.BOLD);
				}
				titleLegItem.setLabelFont(labelFont);
				lic.add(titleLegItem);
				return lic;
			}
		});
		legendTitle.setHorizontalAlignment(HorizontalAlignment.CENTER);

		bc.add(legendTitle, RectangleEdge.TOP);
		bc.add(legend, RectangleEdge.BOTTOM);
		setPosition(RectangleEdge.RIGHT);
	}

}
