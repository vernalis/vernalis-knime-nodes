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
package com.vernalis.knime.plot.nodes.kerneldensity;

/**
 * A Utility class of constants relating to the KernelDensity Nodes
 * 
 * @author s.roughley
 *
 */
public class KernelConstants {

	static final String BANDWIDTH = " Bandwidth";
	static final String BANDWIDTH_H = "Bandwidth (H)";
	static final String COLUMN_SELECTION = "Column Selection";
	static final String CONTOUR_INTERVAL_SCHEMA = "Contour Interval Schema";
	static final String CONTOUR_OPTIONS = "Contour Options";
	static final int DEFAULT_GRID_POINTS = 500;
	static final int DEFAULT_OUTLIERS = 5;
	static final String EXPAND_ALL_KERNELS_TO_WHOLE_RANGE =
			"Expand all kernels to whole range";
	static final String FILL_CONTOURS = "Fill Contours";
	static final String GROUPING_COLUMN = "Grouping Column";
	static final String KERNEL_ESTIMATOR = "Kernel Estimator";
	public static final String KERNEL_OPTIONS = "Kernel Options";
	static final String KERNEL_SYMMETRY = "Kernel Symmetry";
	static final String LOWER_BOUND = "Lower bound";
	static final String NUMBER_OF_CONTOURS = "Number of Contours";
	static final String NUMBER_OF_OUTLIERS = "Number of outliers";
	static final String NUMBER_OF_OUTLIERS_OF_DATASET =
			"Number of outliers (% of dataset)";
	static final String OUTLIER_COLOUR = "Outlier Colour";
	static final String OUTLIER_OPTIONS = "Outlier Options";
	static final String OUTLIER_SHAPE = "Outlier shape";
	static final String OUTLIER_SIZE = "Outlier Size";
	static final String PLOT_OPTIONS = "Plot Options";
	static final String SHOW_ALL_DATA_KERNEL = "Show all-data kernel";
	static final String SHOW_LEGEND = "Show legend";
	static final String UPPER_BOUND = "Upper bound";
	static final String VALUES_COLUMN = "Values Column";
	static final String X = "x";
	static final String X_BANDWIDTH_HX = "X-Bandwidth (Hx)";
	static final String X_VALUES_COLUMN = "X-Values Column";
	static final String Y = "y";
	static final String Y_BANDWIDTH_HY = "Y-Bandwidth (Hy)";
	static final String Y_VALUES_COLUMN = "Y-Values Column";

	private KernelConstants() {
		// Utility Class - Do not Instantiate
	}

}
