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
package com.vernalis.knime.plot.nodes.box;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;

/**
 * An legend for the boxplot, showing separate sets for series and category
 * colours where applicable
 * 
 * @author S.Roughley
 *
 */
public class BoxplotLegendItemSource implements LegendItemSource {

	Map<String, Color> seriesColours;
	Map<String, Color> categoryColours;
	List<String> seriesNames;
	boolean mean, outlier, farOutlier;
	BasicStroke line;
	Color defaultFill, defaultOutline;

	/**
	 * Simple constructor with default fill / outline colours (White/Black) and
	 * lines (1.0 width)
	 * 
	 * @param seriesColours
	 *            The Series Names/Colours - {@code null} for no series
	 *            colouring
	 * @param categoryColours
	 *            The Categories Name/Colours - {@code null} for no category
	 *            colouring
	 * @param mean
	 *            Is the mean shown?
	 * @param outlier
	 *            Are any outliers shown?
	 * @param farOutlier
	 *            Are any extreme outliers shown?
	 */
	public BoxplotLegendItemSource(List<String> seriesNames,
			Map<String, Color> seriesColours,
			Map<String, Color> categoryColours, boolean mean, boolean outlier,
			boolean farOutlier) {
		this(seriesNames, seriesColours, categoryColours, mean, outlier,
				farOutlier, Color.white, Color.black, new BasicStroke(1.0f));
	}

	/**
	 * Constructor with full control
	 * 
	 * @param seriesColours
	 *            The Series Names/Colours - {@code null} for no series
	 *            colouring
	 * @param categoryColours
	 *            The Categories Name/Colours - {@code null} for no category
	 *            colouring
	 * @param mean
	 *            Is the mean shown?
	 * @param outlier
	 *            Are any outliers shown?
	 * @param farOutlier
	 *            Are any extreme outliers shown?
	 * @param defaultFillColour
	 *            The default fill colour
	 * @param defaultOutlineColour
	 *            The default outline colour
	 * @param outlineStroke
	 *            The stroke used for the lines
	 */
	public BoxplotLegendItemSource(List<String> seriesNames,
			Map<String, Color> seriesColours,
			Map<String, Color> categoryColours, boolean mean, boolean outlier,
			boolean farOutlier, Color defaultFillColour,
			Color defaultOutlineColour, BasicStroke outlineStroke) {
		this.seriesNames = seriesNames;
		this.seriesColours = seriesColours;
		this.categoryColours = categoryColours;
		this.mean = mean;
		this.outlier = outlier;
		this.farOutlier = farOutlier;
		line = outlineStroke;
		defaultFill = defaultFillColour;
		defaultOutline = defaultOutlineColour;
	}

	@Override
	public LegendItemCollection getLegendItems() {
		LegendItemCollection retVal = new LegendItemCollection();

		if (seriesColours != null) {

			// Series is the fill colour
			retVal.add(createSubtitleItem("Series"));
			for (Entry<String, Color> seriesEnt : seriesColours.entrySet()) {
				retVal.add(createColorFilledLegendItem(seriesEnt.getKey(),
						seriesEnt.getValue()));
			}

			if (categoryColours != null) {
				// Optionally, categories are border colour
				retVal.add(createSubtitleItem(""));
				retVal.add(createSubtitleItem("Categories"));
				for (Entry<String, Color> categoryEnt : categoryColours
						.entrySet()) {
					retVal.add(new LegendItem(categoryEnt.getKey(), null, null,
							null, true,
							new Rectangle2D.Double(-5.0, -5.0, 10.0, 10.0),
							false, defaultFill, true, categoryEnt.getValue(),
							line, false,
							new Rectangle2D.Double(-5.0, -0.0, 10.0, 1.0), line,
							Color.red));
				}
			}
		} else if (categoryColours != null) {

			// No series, so cat colours are fill
			retVal.add(createSubtitleItem("Categories"));
			for (Entry<String, Color> categoryEnt : categoryColours
					.entrySet()) {
				retVal.add(createColorFilledLegendItem(categoryEnt.getKey(),
						categoryEnt.getValue()));
			}
		} else {
			// No colours, so all series names are drawn as default fill/border
			for (String seriesName : seriesNames) {
				retVal.add(
						createColorFilledLegendItem(seriesName, defaultFill));
			}
		}

		if (mean || outlier || farOutlier) {
			if (retVal.getItemCount() > 0) {
				retVal.add(createSubtitleItem(""));
			}
			retVal.add(createSubtitleItem("Points"));
			if (mean) {
				retVal.add(new LegendItem("Mean", null, null, null, true,
						MulticolourNotchedBoxAndWhiskerRenderer
								.createMeanShape(0, 0, 5.0),
						false, defaultFill, true, defaultOutline, line, false,
						new Rectangle2D.Double(-5.0, -0.0, 10.0, 1.0), line,
						Color.red));
			}
			if (outlier) {
				retVal.add(new LegendItem("Outlier", null, null, null, true,
						MulticolourNotchedBoxAndWhiskerRenderer
								.createOutlierElipse(
										new Point2D.Double(-5.0, -5.0), 5.0,
										5.0),
						true, defaultFill, true, defaultOutline, line, false,
						new Rectangle2D.Double(-5.0, -0.0, 10.0, 1.0), line,
						Color.red));
			}
			if (farOutlier) {
				retVal.add(new LegendItem("Extreme Outlier", null, null, null,
						true,
						MulticolourNotchedBoxAndWhiskerRenderer.createOutlierX(
								new Point2D.Double(-5.0, -5.0), 5.0),
						false, defaultFill, true, defaultOutline, line, false,
						new Rectangle2D.Double(-5.0, -0.0, 10.0, 1.0), line,
						Color.red));
			}
		}
		return retVal;
	}

	/**
	 * Method to create a legend item with the specified item name, and the fill
	 * the specified colour. The outline is the default outline colour
	 * 
	 * @param itemName
	 *            The text for the legend item
	 * @param fillColour
	 *            The fill colour
	 */
	protected LegendItem createColorFilledLegendItem(String itemName,
			Color fillColour) {
		return new LegendItem(itemName, null, null, null, true,
				new Rectangle2D.Double(-5.0, -5.0, 10.0, 10.0), true,
				fillColour, true, defaultOutline, line, false,
				new Rectangle2D.Double(-5.0, -0.0, 10.0, 1.0), line, Color.red);
	}

	/**
	 * Method to create a legend item comprising simple a text string
	 * 
	 * @param subtitle
	 *            The test string
	 */
	private LegendItem createSubtitleItem(String subtitle) {
		return new LegendItem(subtitle, null, null, null, false,
				new Rectangle2D.Double(-5.0, -5.0, 10.0, 10.0), false,
				Color.black, false, Color.black, line, false,
				new Rectangle2D.Double(-5.0, -0.0, 10.0, 1.0), line, Color.red);
	}
}
