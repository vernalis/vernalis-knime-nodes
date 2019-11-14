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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.Statistics;

import static org.jfree.data.statistics.BoxAndWhiskerCalculator.calculateQ1;
import static org.jfree.data.statistics.BoxAndWhiskerCalculator.calculateQ3;

/**
 * A Box and Whisker plot data item, which also contains the 95% confidence
 * interval of the median, definded as +/- 1.57 * IQR/Sqrt(n) (see
 * https://sites.google.com/site/davidsstatistics/home/notched-box-plots and
 * Chambers, John M., William S. Cleveland, Beat Kleiner, and Paul A. Tukey.
 * "Comparing Data Distributions." In Graphical Methods for Data Analysis, 62.
 * Belmont, California: Wadsworth International Group;, 1983. ISBN 0-87150-413-8
 * International ISBN 0-534-98052-X. Copy of the pages from this book can be
 * found at https://docs.google.com/viewer?a=v&pid=sites&srcid=
 * ZGVmYXVsdGRvbWFpbnxkYXZpZHNzdGF0aXN0aWNzfGd4OjYxNTMzNTE4ZmY4Y2ZhNGQ)
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class NotchedBoxAndWhiskerItem extends BoxAndWhiskerItem {

	/**
	 * Generated serial ID
	 */
	private static final long serialVersionUID = 2264059219017406784L;
	protected Number medianConfidenceInterval, standardDeviation;
	protected int numPoints;

	private NotchedBoxAndWhiskerItem(Number mean, Number median, Number q1,
			Number q3, Number minRegularValue, Number maxRegularValue,
			Number minOutlier, Number maxOutlier,
			List<? extends Number> outliers, Number medianConfidenceInterval,
			Number stdDev, int numberOfDataPoints) {
		super(mean, median, q1, q3, minRegularValue, maxRegularValue,
				minOutlier, maxOutlier, outliers);
		this.medianConfidenceInterval = medianConfidenceInterval;
		this.numPoints = numberOfDataPoints;
		this.standardDeviation = stdDev;
	}

	private NotchedBoxAndWhiskerItem(double mean, double median, double q1,
			double q3, double minRegularValue, double maxRegularValue,
			double minOutlier, double maxOutlier,
			List<? extends Number> outliers, double medianConfidenceInterval,
			double stdDev, int numberOfDataPoints) {
		super(mean, median, q1, q3, minRegularValue, maxRegularValue,
				minOutlier, maxOutlier, outliers);
		this.medianConfidenceInterval = medianConfidenceInterval;
		this.numPoints = numberOfDataPoints;
		this.standardDeviation = stdDev;
	}

	/**
	 * Factory method in which the item is created from a list of values, with
	 * any {@code null} or {@code NaN} values removed
	 * 
	 * @param values
	 *            The data values
	 * @return The box plot item with statistics calculated
	 * @see #create(List, boolean)
	 */
	public static NotchedBoxAndWhiskerItem create(
			List<? extends Number> values) {
		return create(values, true);
	}

	/**
	 * Factory method in which the item is created from a list of values, with
	 * any {@code null} or {@code NaN} values optionally removed
	 * 
	 * @param values
	 *            The data values
	 * @param stripNullAndNaNItems
	 *            Should {@code null} or {@code NaN} values be removed?
	 * @return The box plot item with statistics calculated
	 */
	@SuppressWarnings("unchecked")
	public static NotchedBoxAndWhiskerItem create(List<? extends Number> values,
			boolean stripNullAndNaNItems) {
		if (values == null) {
			throw new IllegalArgumentException("Null 'values' argument.");
		}

		List<Number> vlist;
		if (stripNullAndNaNItems) {
			vlist = new ArrayList<>(values.size());
			ListIterator<? extends Number> iterator = values.listIterator();
			while (iterator.hasNext()) {
				Number n = iterator.next();
				if (n != null) {
					double v = n.doubleValue();
					if (!Double.isNaN(v)) {
						vlist.add(n);
					}
				}
			}
		} else {
			vlist = (List<Number>) values;
		}
		Collections.sort(vlist, new Comparator<Number>() {

			@Override
			public int compare(Number o1, Number o2) {
				if (o1 == null) {
					if (o2 == null) {
						return 0;
					} else {
						return -1;
					}
				}
				if (o2 == null) {
					return 1;
				}
				return Double.compare(o1.doubleValue(), o2.doubleValue());
			}
		});

		double mean = Statistics.calculateMean(vlist, false);
		double median = Statistics.calculateMedian(vlist, false);
		double stdDev =
				Statistics.getStdDev(vlist.toArray(new Number[vlist.size()]));
		double q1 = calculateQ1(vlist);
		double q3 = calculateQ3(vlist);

		double interQuartileRange = q3 - q1;
		double confidenceInterval =
				1.57 * interQuartileRange / Math.sqrt(vlist.size());

		double upperOutlierThreshold = q3 + (interQuartileRange * 1.5);
		double lowerOutlierThreshold = q1 - (interQuartileRange * 1.5);

		double upperFaroutThreshold = q3 + (interQuartileRange * 2.0);
		double lowerFaroutThreshold = q1 - (interQuartileRange * 2.0);

		double minRegularValue = Double.POSITIVE_INFINITY;
		double maxRegularValue = Double.NEGATIVE_INFINITY;
		double minOutlier = Double.POSITIVE_INFINITY;
		double maxOutlier = Double.NEGATIVE_INFINITY;
		List<Number> outliers = new ArrayList<>();

		Iterator<? extends Number> iterator = vlist.iterator();
		while (iterator.hasNext()) {
			Number number = iterator.next();
			if (number == null) {
				continue;
			}
			double value = number.doubleValue();
			if (value > upperOutlierThreshold) {
				outliers.add(number);
				if (value > maxOutlier && value <= upperFaroutThreshold) {
					maxOutlier = value;
				}
			} else if (value < lowerOutlierThreshold) {
				outliers.add(number);
				if (value < minOutlier && value >= lowerFaroutThreshold) {
					minOutlier = value;
				}
			} else {
				minRegularValue = Math.min(minRegularValue, value);
				maxRegularValue = Math.max(maxRegularValue, value);
			}
			minOutlier = Math.min(minOutlier, minRegularValue);
			maxOutlier = Math.max(maxOutlier, maxRegularValue);
		}

		return new NotchedBoxAndWhiskerItem(mean, median, q1, q3,
				minRegularValue, maxRegularValue, minOutlier, maxOutlier,
				outliers, confidenceInterval, stdDev, vlist.size());
	}

	/**
	 * 
	 * @return the medianConfidenceInterval - see
	 *         {@link NotchedBoxAndWhiskerItem} for details
	 */
	public double getMedianConfidenceInterval() {
		return medianConfidenceInterval.doubleValue();
	}

	/**
	 * @return The top of the 'notch' - i.e. the median + the condidence
	 *         interval - see {@link NotchedBoxAndWhiskerItem} for details
	 */
	public double getMaxNotchValue() {
		return getMedian().doubleValue() + getMedianConfidenceInterval();
	}

	/**
	 * @return The bottom of the 'notch' - i.e. the median - the condidence
	 *         interval - see {@link NotchedBoxAndWhiskerItem} for details
	 */
	public double getMinNotchValue() {
		return getMedian().doubleValue() - getMedianConfidenceInterval();
	}

	/**
	 * @return The standard deviation of the data items
	 */
	public double getStandardDeviation() {
		return standardDeviation.doubleValue();
	}

	/**
	 * @return The number of data items
	 */
	public int getNumPoints() {
		return numPoints;
	}

}
