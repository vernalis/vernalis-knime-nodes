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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.Outlier;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.ui.RectangleEdge;

/**
 * An extended Box and Whisker Renderer which allows the box outline and fill
 * colours to be specified by series and or category.
 * <p>
 * Enhancements from the standard {@link BoxAndWhiskerRenderer}
 * </p>
 * <ul>
 * <li>User defined box fill and box outline colours - can be set by
 * series/category or individually</li>
 * <li>Boxes without specified fill or border, or beyond the range of the
 * specified number of categories and series will use the defaults specified
 * </li>
 * <li>Implements optional mean and median</li>
 * <li>Implements whisker width</li>
 * <li>Draws all outlier/extreme outliers within plot limits</li>
 * <li>Draws optional notches at 95 confidence in median range</li>
 * </ul>
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class MulticolourNotchedBoxAndWhiskerRenderer
		extends BoxAndWhiskerRenderer {

	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = -1494815098742716889L;
	/**
	 * The default value of the extreme outlier radius
	 */
	protected static final double DEFAULT_FAROUTLIER_RADIUS = 4.0;
	/**
	 * The default value of the outlier radius
	 */
	protected static final double DEFAULT_OUTLIER_RADIUS = 2.0;
	/**
	 * The default value of the mean radius
	 */
	protected static final double DEFAULT_MEAN_RADIUS = 5.0;
	/**
	 * The default value of the default fill colour
	 */
	protected static Paint DEFAULT_DEFAULT_FILL = Color.white;
	/**
	 * The default value of the default outline colour
	 */
	protected static Paint DEFAULT_DEFAULT_BOX = Color.black;

	/**
	 * The default value of the notch size
	 */
	protected static double DEFAULT_NOTCH_SIZE = 0.2;

	/**
	 * The default value of the show notch setting
	 */
	protected static boolean DEFAULT_SHOW_NOTCH = true;

	protected Paint[][] fillPaints, boxPaints;
	protected Paint DEFAULT_FILL, DEFAULT_BOX;
	protected int numCategories;
	protected int numSeries;
	protected double outlierRadius;
	protected double farOutlierRadius;
	protected double meanRadius;
	protected boolean showNotches = DEFAULT_SHOW_NOTCH;
	protected double notchSize = DEFAULT_NOTCH_SIZE;

	/**
	 * Simple constructor, in which only the number of categories and series are
	 * specified. The other properties assume the values of
	 * {@link #DEFAULT_DEFAULT_BOX}, {@link #DEFAULT_DEFAULT_FILL},
	 * {@link #DEFAULT_MEAN_RADIUS},{@link #DEFAULT_OUTLIER_RADIUS} and
	 * {@link #DEFAULT_FAROUTLIER_RADIUS}
	 * 
	 * @param numCategories
	 *            The number of categories
	 * @param numSeries
	 *            The number of series
	 */
	public MulticolourNotchedBoxAndWhiskerRenderer(int numCategories,
			int numSeries) {
		this(numCategories, numSeries, DEFAULT_DEFAULT_FILL,
				DEFAULT_DEFAULT_BOX, DEFAULT_OUTLIER_RADIUS,
				DEFAULT_FAROUTLIER_RADIUS, DEFAULT_MEAN_RADIUS);
	}

	/**
	 * Constructor, in which only the number of categories and series and the
	 * default colours are specified. The other properties assume the values of
	 * {@link #DEFAULT_MEAN_RADIUS},{@link #DEFAULT_OUTLIER_RADIUS} and
	 * {@link #DEFAULT_FAROUTLIER_RADIUS}
	 * 
	 * @param numCategories
	 *            The number of categories
	 * @param numSeries
	 *            The number of series
	 * @param defaultFill
	 *            The default fill colour
	 * @param defaultBox
	 *            The default box outline colour
	 */
	public MulticolourNotchedBoxAndWhiskerRenderer(int numCategories,
			int numSeries, Paint defaultFill, Paint defaultBox) {
		this(numCategories, numSeries, defaultFill, defaultBox,
				DEFAULT_OUTLIER_RADIUS, DEFAULT_FAROUTLIER_RADIUS,
				DEFAULT_MEAN_RADIUS);
	}

	/**
	 * Constructor with full control of all categories except the mean radius,
	 * which is set to {@link #DEFAULT_MEAN_RADIUS}
	 * 
	 * @param numCategories
	 *            The number of categories
	 * @param numSeries
	 *            The number of series
	 * @param defaultFill
	 *            The default fill colour
	 * @param defaultBox
	 *            The default box outline colour
	 * @param outlierRadius
	 *            The radius for outliers
	 * @param farOutlierRadius
	 *            The radius for extreme outliers
	 */
	public MulticolourNotchedBoxAndWhiskerRenderer(int numCategories,
			int numSeries, Paint defaultFill, Paint defaultBox,
			double outlierRadius, double farOutlierRadius) {
		this(numCategories, numSeries, defaultFill, defaultBox, outlierRadius,
				farOutlierRadius, DEFAULT_MEAN_RADIUS);
	}

	/**
	 * Constructor with full control of all categories
	 * 
	 * @param numCategories
	 *            The number of categories
	 * @param numSeries
	 *            The number of series
	 * @param defaultFill
	 *            The default fill colour
	 * @param defaultBox
	 *            The default box outline colour
	 * @param outlierRadius
	 *            The radius for outliers
	 * @param farOutlierRadius
	 *            The radius for extreme outliers
	 * @param meanRadius
	 *            The radius for the mean
	 */
	public MulticolourNotchedBoxAndWhiskerRenderer(int numCategories,
			int numSeries, Paint defaultFill, Paint defaultBox,
			double outlierRadius, double farOutlierRadius, double meanRadius) {
		super();
		this.fillPaints = new Paint[numCategories][numSeries];
		this.boxPaints = new Paint[numCategories][numSeries];
		this.numCategories = numCategories;
		this.numSeries = numSeries;
		DEFAULT_FILL = defaultFill;
		DEFAULT_BOX = defaultBox;
		this.outlierRadius = outlierRadius;
		this.farOutlierRadius = farOutlierRadius;
		this.meanRadius = meanRadius;
	}

	@Override
	public void setSeriesFillPaint(int series, Paint paint)
			throws IllegalArgumentException {
		if (series < 0 || series >= numSeries) {
			throw new IllegalArgumentException(
					"Series must be in range 0 - " + (numSeries - 1));
		}
		for (int i = 0; i < numCategories; i++) {
			fillPaints[i][series] = paint;
		}
	}

	@Override
	public Paint getSeriesFillPaint(int series) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSeriesPaint(int series, Paint paint)
			throws IllegalArgumentException {
		if (series < 0 || series >= numSeries) {
			throw new IllegalArgumentException(
					"Series must be in range 0 - " + (numSeries - 1));
		}
		for (int i = 0; i < numCategories; i++) {
			boxPaints[i][series] = paint;
		}
	}

	@Override
	public Paint getSeriesPaint(int series) {
		// throw new UnsupportedOperationException();
		return null;
	}

	/**
	 * Set the fill colour of all items in the category. Any existing colours
	 * will be over-written
	 * 
	 * @param category
	 *            The category index
	 * @param paint
	 *            The colour
	 * @throws IllegalArgumentException
	 *             If the category index is out of the range
	 */
	public void setCategoryFillPaint(int category, Paint paint)
			throws IllegalArgumentException {
		if (category < 0 || category >= numCategories) {
			throw new IllegalArgumentException(
					"Category must be in range 0 - " + (numCategories - 1));
		}
		for (int i = 0; i < numSeries; i++) {
			fillPaints[category][i] = paint;
		}
	}

	/**
	 * Set the outline colour of all items in the category. Any existing colours
	 * will be over-written
	 * 
	 * @param category
	 *            The category index
	 * @param paint
	 *            The colour
	 * @throws IllegalArgumentException
	 *             If the category index is out of the range
	 */
	public void setCategoryPaint(int category, Paint paint)
			throws IllegalArgumentException {
		if (category < 0 || category >= numCategories) {
			throw new IllegalArgumentException(
					"Category must be in range 0 - " + (numCategories - 1));
		}
		for (int i = 0; i < numSeries; i++) {
			boxPaints[category][i] = paint;
		}
	}

	/**
	 * Set the fill colour of an individual item.
	 * 
	 * @param category
	 *            The category index
	 * @param series
	 *            The series index
	 * @param paint
	 *            The colour
	 * @throws IllegalArgumentException
	 *             If the category or series index is out of the range
	 */
	public void setItemFillPaint(int category, int series, Paint paint)
			throws IllegalArgumentException {
		if (series < 0 || series >= numSeries) {
			throw new IllegalArgumentException(
					"Series must be in range 0 - " + (numSeries - 1));
		}
		if (category < 0 || category >= numCategories) {
			throw new IllegalArgumentException(
					"Category must be in range 0 - " + (numCategories - 1));
		}

		fillPaints[category][series] = paint;
	}

	/**
	 * Set the outline colour of an individual item.
	 * 
	 * @param category
	 *            The category index
	 * @param series
	 *            The series index
	 * @param paint
	 *            The colour
	 * @throws IllegalArgumentException
	 *             If the category or series index is out of the range
	 */
	public void setItemBoxPaint(int category, int series, Paint paint)
			throws IllegalArgumentException {
		if (series < 0 || series >= numSeries) {
			throw new IllegalArgumentException(
					"Series must be in range 0 - " + (numSeries - 1));
		}
		if (category < 0 || category >= numCategories) {
			throw new IllegalArgumentException(
					"Category must be in range 0 - " + (numCategories - 1));
		}

		boxPaints[category][series] = paint;
	}

	/**
	 * @param category
	 *            The category index
	 * @param series
	 *            The series index
	 * @return The colour of the outline for the specified item, of the default
	 *         if either nothing is specified, or the index is out of range
	 */
	public Paint getItemBoxPaint(int category, int series) {
		if (series < 0 || series >= numSeries || category < 0
				|| category >= numCategories
				|| boxPaints[category][series] == null) {
			return DEFAULT_BOX;
		}
		return boxPaints[category][series];
	}

	/**
	 * @param category
	 *            The category index
	 * @param series
	 *            The series index
	 * @return The colour of the fill for the specified item, of the default if
	 *         either nothing is specified, or the index is out of range
	 */
	public Paint getItemBoxFillPaint(int category, int series) {
		if (series < 0 || series >= numSeries || category < 0
				|| category >= numCategories
				|| fillPaints[category][series] == null) {
			return DEFAULT_FILL;
		}
		return fillPaints[category][series];
	}

	/**
	 * @return the showNotches property
	 */
	public boolean isShowNotches() {
		return showNotches;
	}

	/**
	 * Set the showNotches property
	 * 
	 * @param showNotches
	 *            the showNotches to set
	 */
	public void setShowNotches(boolean showNotches) {
		this.showNotches = showNotches;
	}

	/**
	 * @return the notchSize
	 */
	public double getNotchSize() {
		return notchSize;
	}

	/**
	 * Set the notchSize property (between 0.05 and 0.5
	 * 
	 * @param notchSize
	 *            the notchSize to set
	 */
	public void setNotchSize(double notchSize) {
		if (notchSize < 0.05 || notchSize > 0.5) {
			throw new IllegalArgumentException(
					"Notch size must be in range 0.05 - 0.5");
		}
		this.notchSize = notchSize;
	}

	/**
	 * This method should not be used to generate a legend. Instead, a
	 * {@link BoxplotLegend} should be generated externally
	 * 
	 * @see org.jfree.chart.renderer.category.BoxAndWhiskerRenderer#getLegendItem(int,
	 *      int)
	 */
	@Override
	public LegendItem getLegendItem(int datasetIndex, int series) {
		return new LegendItem("");
	}

	/**
	 * Draws the visual representation of a single data item when the plot has a
	 * horizontal orientation.
	 *
	 * @param g2
	 *            the graphics device.
	 * @param state
	 *            the renderer state.
	 * @param dataArea
	 *            the area within which the plot is being drawn.
	 * @param plot
	 *            the plot (can be used to obtain standard color information
	 *            etc).
	 * @param domainAxis
	 *            the domain axis.
	 * @param rangeAxis
	 *            the range axis.
	 * @param dataset
	 *            the dataset.
	 * @param row
	 *            the row index (zero-based).
	 * @param column
	 *            the column index (zero-based).
	 */
	@Override
	public void drawHorizontalItem(Graphics2D g2,
			CategoryItemRendererState state, Rectangle2D dataArea,
			CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis,
			CategoryDataset dataset, int row, int column) {

		BoxAndWhiskerCategoryDataset bawDataset =
				(BoxAndWhiskerCategoryDataset) dataset;
		NotchedBoxAndWhiskerCategoryDataset notchedDataSet = null;
		if (dataset instanceof NotchedBoxAndWhiskerCategoryDataset) {
			notchedDataSet = (NotchedBoxAndWhiskerCategoryDataset) dataset;
		}

		// TODO: Add notching here too
		double categoryEnd = domainAxis.getCategoryEnd(column, getColumnCount(),
				dataArea, plot.getDomainAxisEdge());
		double categoryStart = domainAxis.getCategoryStart(column,
				getColumnCount(), dataArea, plot.getDomainAxisEdge());
		double categoryWidth = Math.abs(categoryEnd - categoryStart);

		double yy = categoryStart;
		int seriesCount = getRowCount();
		int categoryCount = getColumnCount();

		if (seriesCount > 1) {
			double seriesGap = dataArea.getWidth() * getItemMargin()
					/ (categoryCount * (seriesCount - 1));
			double usedWidth = (state.getBarWidth() * seriesCount)
					+ (seriesGap * (seriesCount - 1));
			// offset the start of the boxes if the total width used is smaller
			// than the category width
			double offset = (categoryWidth - usedWidth) / 2;
			yy = yy + offset + (row * (state.getBarWidth() + seriesGap));
		} else {
			// offset the start of the box if the box width is smaller than
			// the category width
			double offset = (categoryWidth - state.getBarWidth()) / 2;
			yy = yy + offset;
		}
		double yymid = yy + state.getBarWidth() / 2.0;

		Paint boxPaint = getItemBoxPaint(column, row);
		Paint fillPaint = getItemBoxFillPaint(column, row);
		Stroke s = getBaseOutlineStroke();
		g2.setStroke(s);

		RectangleEdge location = plot.getRangeAxisEdge();

		Number xQ1 = bawDataset.getQ1Value(row, column);
		Number xQ3 = bawDataset.getQ3Value(row, column);
		Number xMax = bawDataset.getMaxRegularValue(row, column);
		Number xMin = bawDataset.getMinRegularValue(row, column);

		Shape box = null;
		if (xQ1 != null && xQ3 != null && xMax != null && xMin != null) {

			double xxQ1 = rangeAxis.valueToJava2D(xQ1.doubleValue(), dataArea,
					location);
			double xxQ3 = rangeAxis.valueToJava2D(xQ3.doubleValue(), dataArea,
					location);
			double xxMax = rangeAxis.valueToJava2D(xMax.doubleValue(), dataArea,
					location);
			double xxMin = rangeAxis.valueToJava2D(xMin.doubleValue(), dataArea,
					location);

			double whiskerWidth = getWhiskerWidth() * state.getBarWidth() / 2.0;

			// draw the upper shadow...
			g2.setPaint(boxPaint);
			g2.draw(new Line2D.Double(xxMax, yymid, xxQ3, yymid));
			g2.draw(new Line2D.Double(xxMax, yymid - whiskerWidth, xxMax,
					yymid + whiskerWidth));

			// draw the lower shadow...
			g2.draw(new Line2D.Double(xxMin, yymid, xxQ1, yymid));
			g2.draw(new Line2D.Double(xxMin, yymid - whiskerWidth, xxMin,
					yymid + whiskerWidth));

			// draw the body...
			double xxLeft = Math.min(xxQ1, xxQ3);
			if (notchedDataSet == null || !showNotches
					|| bawDataset.getMedianValue(row, column) == null) {
				box = new Rectangle2D.Double(xxLeft, yy, state.getBarWidth(),
						Math.abs(xxQ1 - xxQ3));
			} else {
				double yyBottom = yy + state.getBarWidth();
				double xxMedian = rangeAxis.valueToJava2D(
						bawDataset.getMedianValue(row, column).doubleValue(),
						dataArea, location);
				double xxNotchRight = rangeAxis.valueToJava2D(
						notchedDataSet.getMaxNotchValue(row, column), dataArea,
						location);
				double xxNotchLeft = rangeAxis.valueToJava2D(
						notchedDataSet.getMinNotchValue(row, column), dataArea,
						location);
				double xxRight = Math.max(xxQ1, xxQ3);
				double yyNotchDepth = state.getBarWidth() * notchSize;

				Path2D notchedBox = new Path2D.Double();

				// Left end of the box
				notchedBox.moveTo(xxLeft, yyBottom);
				notchedBox.lineTo(xxLeft, yy); // |

				// Top of box
				notchedBox.lineTo(xxNotchLeft, yy); // -
				notchedBox.lineTo(xxMedian, yy + yyNotchDepth); // \
				notchedBox.lineTo(xxNotchRight, yy); // /
				notchedBox.lineTo(xxRight, yy); // -

				// Right end of box
				notchedBox.lineTo(xxRight, yyBottom); // |

				// Bottom of box
				notchedBox.lineTo(xxNotchRight, yyBottom);// -
				notchedBox.lineTo(xxMedian, yyBottom - yyNotchDepth); // \
				notchedBox.lineTo(xxNotchLeft, yyBottom); // /
				notchedBox.closePath(); // -
				box = notchedBox;
			}

			if (getFillBox()) {
				g2.setPaint(fillPaint);
				g2.fill(box);
				g2.setPaint(boxPaint);
			}
			g2.draw(box);

		}

		// draw median...
		g2.setPaint(boxPaint);
		Number xMedian = bawDataset.getMedianValue(row, column);
		if (xMedian != null && isMedianVisible()) {
			double xxMedian = rangeAxis.valueToJava2D(xMedian.doubleValue(),
					dataArea, location);

			if (notchedDataSet != null && showNotches) {
				g2.draw(new Line2D.Double(xxMedian,
						yy + state.getBarWidth() * notchSize, xxMedian,
						yy + state.getBarWidth()
								- state.getBarWidth() * notchSize));
			} else {
				g2.draw(new Line2D.Double(xxMedian, yy, xxMedian,
						yy + state.getBarWidth()));
			}
		}

		// draw yOutliers...
		// draw outliers
		List<Outlier> outliers = new ArrayList<>();
		List<Outlier> farOutliers = new ArrayList<>();

		// From outlier array sort out which are outliers
		List<Double> xOutliers = bawDataset.getOutliers(row, column);
		Number minOutlier = bawDataset.getMinOutlier(row, column);
		Number maxOutlier = bawDataset.getMaxOutlier(row, column);
		Number minRegular = bawDataset.getMinRegularValue(row, column);
		Number maxRegular = bawDataset.getMaxRegularValue(row, column);

		// And some tooltip stuff, because we will add on the fly...
		EntityCollection entities = state.getEntityCollection();
		CategoryURLGenerator itemURLGenerator =
				getItemURLGenerator(row, column);
		CategoryToolTipGenerator tipster = getToolTipGenerator(row, column);
		if (xOutliers != null) {
			// List the outliers into near and far outliers
			for (int i = 0; i < xOutliers.size(); i++) {
				double outlier = ((Number) xOutliers.get(i)).doubleValue();
				if (outlier > maxOutlier.doubleValue()
						|| outlier < minOutlier.doubleValue()) {
					// Faroutlier
					Outlier farOut = new Outlier(rangeAxis
							.valueToJava2D(outlier, dataArea, location), yymid,
							farOutlierRadius);
					if (dataArea.contains(farOut.getPoint())) {
						farOutliers.add(farOut);
					}
				} else if (outlier > maxRegular.doubleValue()
						|| outlier < minRegular.doubleValue()) {
					Outlier out = new Outlier(rangeAxis.valueToJava2D(outlier,
							dataArea, location), yymid, farOutlierRadius);
					if (dataArea.contains(out.getPoint())) {
						outliers.add(out);
					}
				}
			}

			// Draw them
			for (Outlier outlier : outliers) {
				// 'o' for near outliers
				Ellipse2D dot = createOutlierElipse(outlier.getPoint(),
						outlierRadius, outlierRadius);
				g2.setPaint(fillPaint);
				g2.fill(dot);
				g2.setPaint(boxPaint);
				g2.draw(dot);
				if (entities != null) {
					String tooltip = "Outlier: " + rangeAxis
							.java2DToValue(outlier.getX(), dataArea, location);
					String url = null;
					if (itemURLGenerator != null) {
						url = getItemURLGenerator(row, column)
								.generateURL(dataset, row, column);
					}
					OutlierEntity entity = new OutlierEntity(dot, tooltip, url);
					entities.add(entity);
				}
			}

			for (Outlier outlier : farOutliers) {
				// 'data' for far outliers
				Path2D x = createOutlierX(outlier.getPoint(), outlierRadius);
				g2.setPaint(boxPaint);
				g2.draw(x);
				if (entities != null) {
					String tooltip = "Extreme Outlier: " + rangeAxis
							.java2DToValue(outlier.getX(), dataArea, location);
					String url = null;
					if (itemURLGenerator != null) {
						url = getItemURLGenerator(row, column)
								.generateURL(dataset, row, column);
					}
					FarOutlierEntity entity =
							new FarOutlierEntity(x, tooltip, url);
					entities.add(entity);
				}
			}
		}

		// draw mean after outliers so it is visible
		g2.setPaint(boxPaint);
		Number xMean = bawDataset.getMeanValue(row, column);
		if (xMean != null && isMeanVisible()) {
			double xxMean = rangeAxis.valueToJava2D(xMean.doubleValue(),
					dataArea, location);
			Path2D.Double meanShape =
					createMeanShape(xxMean, yymid, meanRadius);
			g2.draw(meanShape);
		}

		// collect entity and tool tip information...
		if (state.getInfo() != null && box != null) {
			if (entities != null) {
				String tip = null;
				if (tipster != null) {
					tip = tipster.generateToolTip(dataset, row, column);
				}
				String url = null;
				if (getItemURLGenerator(row, column) != null) {
					url = getItemURLGenerator(row, column).generateURL(dataset,
							row, column);
				}
				@SuppressWarnings("deprecation")
				CategoryItemEntity entity =
						new CategoryItemEntity(box, tip, url, dataset, row,
								dataset.getColumnKey(column), column);
				entities.add(entity);
			}
		}

	}

	/**
	 * Draws the visual representation of a single data item when the plot has a
	 * vertical orientation.
	 *
	 * @param g2
	 *            the graphics device.
	 * @param state
	 *            the renderer state.
	 * @param dataArea
	 *            the area within which the plot is being drawn.
	 * @param plot
	 *            the plot (can be used to obtain standard color information
	 *            etc).
	 * @param domainAxis
	 *            the domain axis.
	 * @param rangeAxis
	 *            the range axis.
	 * @param dataset
	 *            the dataset.
	 * @param row
	 *            the row index (zero-based).
	 * @param column
	 *            the column index (zero-based).
	 */
	@SuppressWarnings({ "deprecation" })
	@Override
	public void drawVerticalItem(Graphics2D g2, CategoryItemRendererState state,
			Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis,
			ValueAxis rangeAxis, CategoryDataset dataset, int row, int column) {

		BoxAndWhiskerCategoryDataset bawDataset =
				(BoxAndWhiskerCategoryDataset) dataset;
		NotchedBoxAndWhiskerCategoryDataset notchedDataSet = null;
		if (dataset instanceof NotchedBoxAndWhiskerCategoryDataset) {
			notchedDataSet = (NotchedBoxAndWhiskerCategoryDataset) dataset;
		}

		double categoryEnd = domainAxis.getCategoryEnd(column, getColumnCount(),
				dataArea, plot.getDomainAxisEdge());
		double categoryStart = domainAxis.getCategoryStart(column,
				getColumnCount(), dataArea, plot.getDomainAxisEdge());
		double categoryWidth = categoryEnd - categoryStart;

		double xx = categoryStart;
		int seriesCount = getRowCount();
		int categoryCount = getColumnCount();

		if (seriesCount > 1) {
			double seriesGap = dataArea.getWidth() * getItemMargin()
					/ (categoryCount * (seriesCount - 1));
			double usedWidth = (state.getBarWidth() * seriesCount)
					+ (seriesGap * (seriesCount - 1));
			// offset the start of the boxes if the total width used is smaller
			// than the category width
			double offset = (categoryWidth - usedWidth) / 2;
			xx = xx + offset + (row * (state.getBarWidth() + seriesGap));
		} else {
			// offset the start of the box if the box width is smaller than the
			// category width
			double offset = (categoryWidth - state.getBarWidth()) / 2;
			xx = xx + offset;
		}
		double xxmid = xx + state.getBarWidth() / 2.0;

		double yyAverage = 0.0;

		Paint boxPaint = getItemBoxPaint(column, row);
		Paint fillPaint = getItemBoxFillPaint(column, row);
		Stroke s = getBaseOutlineStroke();
		g2.setStroke(s);

		RectangleEdge location = plot.getRangeAxisEdge();

		Number yQ1 = bawDataset.getQ1Value(row, column);
		Number yQ3 = bawDataset.getQ3Value(row, column);
		Number yMax = bawDataset.getMaxRegularValue(row, column);
		Number yMin = bawDataset.getMinRegularValue(row, column);
		Shape box = null;
		if (yQ1 != null && yQ3 != null && yMax != null && yMin != null) {

			double yyQ1 = rangeAxis.valueToJava2D(yQ1.doubleValue(), dataArea,
					location);
			double yyQ3 = rangeAxis.valueToJava2D(yQ3.doubleValue(), dataArea,
					location);
			double yyMax = rangeAxis.valueToJava2D(yMax.doubleValue(), dataArea,
					location);
			double yyMin = rangeAxis.valueToJava2D(yMin.doubleValue(), dataArea,
					location);

			double whiskerWidth = getWhiskerWidth() * state.getBarWidth() / 2.0;
			g2.setPaint(boxPaint);
			g2.draw(new Line2D.Double(xxmid, yyMax, xxmid, yyQ3)); // |
			g2.draw(new Line2D.Double(xxmid - whiskerWidth, yyMax,
					xxmid + whiskerWidth, yyMax));// --

			// draw the lower whisker...
			g2.draw(new Line2D.Double(xxmid, yyMin, xxmid, yyQ1));// |
			g2.draw(new Line2D.Double(xxmid - whiskerWidth, yyMin,
					xxmid + whiskerWidth, yyMin));// --

			// draw the body...
			double yyTop = Math.min(yyQ1, yyQ3); // Lower values are higher up
													// plot area!
			if (notchedDataSet == null || !showNotches
					|| bawDataset.getMedianValue(row, column) == null) {
				box = new Rectangle2D.Double(xx, yyTop, state.getBarWidth(),
						Math.abs(yyQ1 - yyQ3));
			} else {
				double xxRight = xx + state.getBarWidth();
				double yyMedian = rangeAxis.valueToJava2D(
						bawDataset.getMedianValue(row, column).doubleValue(),
						dataArea, location);
				double yyNotchTop = rangeAxis.valueToJava2D(
						notchedDataSet.getMaxNotchValue(row, column), dataArea,
						location);
				double yyNotchBottom = rangeAxis.valueToJava2D(
						notchedDataSet.getMinNotchValue(row, column), dataArea,
						location);
				double yyBottom = Math.max(yyQ1, yyQ3);
				double xxNotchDepth = state.getBarWidth() * notchSize;

				Path2D notchedBox = new Path2D.Double();

				// Top of the box
				notchedBox.moveTo(xx, yyTop);
				notchedBox.lineTo(xxRight, yyTop);

				// RHS of box
				notchedBox.lineTo(xxRight, yyNotchTop);
				notchedBox.lineTo(xxRight - xxNotchDepth, yyMedian);
				notchedBox.lineTo(xxRight, yyNotchBottom);
				notchedBox.lineTo(xxRight, yyBottom);

				// Bottom of box
				notchedBox.lineTo(xx, yyBottom);

				// LHS of box
				notchedBox.lineTo(xx, yyNotchBottom);
				notchedBox.lineTo(xx + xxNotchDepth, yyMedian);
				notchedBox.lineTo(xx, yyNotchTop);
				notchedBox.closePath();
				box = notchedBox;
			}
			if (getFillBox()) {
				// Draw a filled box first with the fill colour
				g2.setPaint(fillPaint);
				g2.fill(box);
				g2.setPaint(boxPaint);
			}
			g2.draw(box);

		}

		// draw median...
		g2.setPaint(boxPaint);
		Number yMedian = bawDataset.getMedianValue(row, column);
		if (yMedian != null && isMedianVisible()) {
			double yyMedian = rangeAxis.valueToJava2D(yMedian.doubleValue(),
					dataArea, location);
			if (notchedDataSet != null && showNotches) {
				g2.draw(new Line2D.Double(xx + state.getBarWidth() * notchSize,
						yyMedian, xx + state.getBarWidth()
								- state.getBarWidth() * notchSize,
						yyMedian));
			} else {
				g2.draw(new Line2D.Double(xx, yyMedian,
						xx + state.getBarWidth(), yyMedian));
			}
		}

		// draw outliers
		List<Outlier> outliers = new ArrayList<>();
		List<Outlier> farOutliers = new ArrayList<>();

		// From outlier array sort out which are outliers
		List<Double> yOutliers = bawDataset.getOutliers(row, column);
		Number minOutlier = bawDataset.getMinOutlier(row, column);
		Number maxOutlier = bawDataset.getMaxOutlier(row, column);
		Number minRegular = bawDataset.getMinRegularValue(row, column);
		Number maxRegular = bawDataset.getMaxRegularValue(row, column);

		// And some tooltip stuff, because we will add on the fly...
		EntityCollection entities = state.getEntityCollection();
		CategoryURLGenerator itemURLGenerator =
				getItemURLGenerator(row, column);
		CategoryToolTipGenerator tipster = getToolTipGenerator(row, column);
		if (yOutliers != null) {
			// List the outliers into near and far outliers
			for (int i = 0; i < yOutliers.size(); i++) {
				double outlier = ((Number) yOutliers.get(i)).doubleValue();
				if (outlier > maxOutlier.doubleValue()
						|| outlier < minOutlier.doubleValue()) {
					// Faroutlier
					Outlier farOut =
							new Outlier(
									xxmid, rangeAxis.valueToJava2D(outlier,
											dataArea, location),
									farOutlierRadius);
					if (dataArea.contains(farOut.getPoint())) {
						farOutliers.add(farOut);
					}
				} else if (outlier > maxRegular.doubleValue()
						|| outlier < minRegular.doubleValue()) {
					Outlier out =
							new Outlier(
									xxmid, rangeAxis.valueToJava2D(outlier,
											dataArea, location),
									farOutlierRadius);
					if (dataArea.contains(out.getPoint())) {
						outliers.add(out);
					}
				}
			}

			// Draw them
			for (Outlier outlier : outliers) {
				// 'o' for near outliers
				Ellipse2D dot = createOutlierElipse(outlier.getPoint(),
						outlierRadius, outlierRadius);
				g2.setPaint(fillPaint);
				g2.fill(dot);
				g2.setPaint(boxPaint);
				g2.draw(dot);
				if (entities != null) {
					String tooltip = "Outlier: " + rangeAxis
							.java2DToValue(outlier.getY(), dataArea, location);
					String url = null;
					if (itemURLGenerator != null) {
						url = getItemURLGenerator(row, column)
								.generateURL(dataset, row, column);
					}
					OutlierEntity entity = new OutlierEntity(dot, tooltip, url);
					entities.add(entity);
				}
			}

			for (Outlier outlier : farOutliers) {
				// 'data' for far outliers
				Path2D x = createOutlierX(outlier.getPoint(), outlierRadius);
				g2.setPaint(boxPaint);
				g2.draw(x);
				if (entities != null) {
					String tooltip = "Extreme Outlier: " + rangeAxis
							.java2DToValue(outlier.getY(), dataArea, location);
					String url = null;
					if (itemURLGenerator != null) {
						url = getItemURLGenerator(row, column)
								.generateURL(dataset, row, column);
					}
					FarOutlierEntity entity =
							new FarOutlierEntity(x, tooltip, url);
					entities.add(entity);
				}
			}
		}

		// draw mean after outliers so it is visible (outliers are filled, mean
		// isnt!
		g2.setPaint(boxPaint);
		Number yMean = bawDataset.getMeanValue(row, column);
		if (yMean != null && isMeanVisible()) {
			yyAverage = rangeAxis.valueToJava2D(yMean.doubleValue(), dataArea,
					location);
			Path2D.Double meanShape =
					createMeanShape(xxmid, yyAverage, meanRadius);
			g2.draw(meanShape);
		}

		// collect entity and tool tip information...
		if (state.getInfo() != null && box != null) {

			if (entities != null) {
				String tip = null;

				if (tipster != null) {
					tip = tipster.generateToolTip(dataset, row, column);
				}
				String url = null;

				if (itemURLGenerator != null) {
					url = itemURLGenerator.generateURL(dataset, row, column);
				}
				CategoryItemEntity entity =
						new CategoryItemEntity(box, tip, url, dataset, row,
								dataset.getColumnKey(column), column);
				entities.add(entity);
			}
		}

	}

	/**
	 * Method to create the mean shape - a 'crosshair' - i.e. an 'o' overlaid
	 * with a '+'
	 * 
	 * @param x
	 *            The centre x coordinate
	 * @param y
	 *            The centre y coordinate
	 * @param radius
	 *            The radius
	 * @return The shape required
	 */
	public static Path2D.Double createMeanShape(double x, double y,
			double radius) {
		Path2D.Double meanShape =
				new Path2D.Double(createCentredElipse(x, y, radius, radius));
		meanShape.moveTo(x - radius, y);
		meanShape.lineTo(x + radius, y);
		meanShape.moveTo(x, y + radius);
		meanShape.lineTo(x, y - radius);
		return meanShape;
	}

	/**
	 * Create an 'data' path.
	 * 
	 * @param point
	 *            The point at the top left corner of the bounding box
	 * @return A {@link Path2D} containing an 'data' shape
	 */
	public static Path2D createOutlierX(Point2D point, double radius) {
		Path2D x = new Path2D.Double();
		x.moveTo(point.getX(), point.getY());
		x.lineTo(point.getX() + 2.0 * radius, point.getY() + 2.0 * radius);
		x.moveTo(point.getX(), point.getY() + 2.0 * radius);
		x.lineTo(point.getX() + 2.0 * radius, point.getY());
		return x;
	}

	/**
	 * Create an Ellipse <i>centred</i> on (xCentre, yCentre), with principle
	 * radii xRadius and yRadius
	 * 
	 * @param xCentre
	 *            The x coordinate of the ellipse centre
	 * @param yCentre
	 *            The y coordinate of the ellipse centre
	 * @return An Ellipse
	 */
	public static Ellipse2D.Double createCentredElipse(double xCentre,
			double yCentre, double xRadius, double yRadius) {
		return new Ellipse2D.Double(xCentre - xRadius, yCentre - yRadius,
				xRadius * 2, yRadius * 2);
	}

	/**
	 * Create an Ellipse with the Top Left corner of the bounding box at point,
	 * with principle radii xRadius and yRadius
	 * 
	 * @param point
	 * @param xRadius
	 * @param yRadius
	 * @return
	 */
	public static Ellipse2D.Double createOutlierElipse(Point2D point,
			double xRadius, double yRadius) {
		return new Ellipse2D.Double(point.getX(), point.getY(), 2.0 * xRadius,
				2.0 * yRadius);
	}

	/**
	 * Simple class to identify outliers for tooltip generation
	 * 
	 * @author S.Roughley
	 *
	 */
	@SuppressWarnings("serial")
	protected static class OutlierEntity extends ChartEntity {

		public OutlierEntity(Shape area, String toolTipText, String urlText) {
			super(area, toolTipText, urlText);

		}

		/**
		 * @param area
		 * @param toolTipText
		 */
		public OutlierEntity(Shape area, String toolTipText) {
			super(area, toolTipText);
		}

		/**
		 * @param area
		 */
		public OutlierEntity(Shape area) {
			super(area);
		}

	}

	/**
	 * Simple class to identify extreme outliers for tooltip generation
	 * 
	 * @author S.Roughley
	 *
	 */
	@SuppressWarnings("serial")
	protected static class FarOutlierEntity extends ChartEntity {

		/**
		 * @param area
		 * @param toolTipText
		 * @param urlText
		 */
		public FarOutlierEntity(Shape area, String toolTipText,
				String urlText) {
			super(area, toolTipText, urlText);

		}

		/**
		 * @param area
		 * @param toolTipText
		 */
		public FarOutlierEntity(Shape area, String toolTipText) {
			super(area, toolTipText);

		}

		/**
		 * @param area
		 */
		public FarOutlierEntity(Shape area) {
			super(area);

		}

	}
}
