/*******************************************************************************
 * Copyright (c) 2019, 2023, Vernalis (R&D) Ltd
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

import static com.vernalis.knime.nodes.NodeDescriptionUtils.addOptionToTab;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.CONTOUR_INTERVAL_SCHEMA;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.EXPAND_ALL_KERNELS_TO_WHOLE_RANGE;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.FILL_CONTOURS;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.GROUPING_COLUMN;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.KERNEL_ESTIMATOR;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.KERNEL_OPTIONS;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.KERNEL_SYMMETRY;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.LOWER_BOUND;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.NUMBER_OF_CONTOURS;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.NUMBER_OF_OUTLIERS_OF_DATASET;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.OUTLIER_COLOUR;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.OUTLIER_SHAPE;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.OUTLIER_SIZE;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.SHOW_ALL_DATA_KERNEL;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.SHOW_LEGEND;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.UPPER_BOUND;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.VALUES_COLUMN;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.X;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.Y;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.getDialogAutoRangeAxisName;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.getDialogAxisRangeName;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.getDialogBandwidthBoxTitle;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.getDialogGridPointsName;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.getDialogManualBandwidthName;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane.getDialogShowBandwidthName;

import org.apache.xmlbeans.XmlCursor;
import org.knime.core.node.NodeFactory;
import org.knime.node.v41.FullDescription;
import org.knime.node.v41.Tab;

import com.vernalis.knime.jfcplot.core.nodes.AbstractDrawableSeriesPlotNodeDescription;
import com.vernalis.knime.nodes.NodeDescriptionUtils;
import com.vernalis.knime.nodes.NodeDescriptionUtils.TableFactory;

/**
 * Node Description class for Kernel plots, handles 1D and 2D options and
 * optional axis ranging dialogue options
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class AbstractKernelDensityPlotNodeDescription
		extends AbstractDrawableSeriesPlotNodeDescription {

	protected final boolean is2D, hasAutoRangeAxesOptions;

	/**
	 * Overloaded constructor for nodes with default axis range options
	 * 
	 * @param iconName
	 *            Relative path to icon
	 * @param nodeName
	 *            The node name
	 * @param tabName
	 *            The name of the extra options tab
	 * @param columnNames
	 *            The name of the column selectors in the node dialog
	 * @param factoryClazz
	 *            The {@link NodeFactory} class for adding the correct bundle
	 *            information and locating the icon
	 */
	public AbstractKernelDensityPlotNodeDescription(String iconName,
			String nodeName, String tabName, String[] columnNames,
			Class<? extends NodeFactory<?>> factoryClazz) {
		this(iconName, nodeName, tabName, columnNames, factoryClazz, true);
	}

	/**
	 * Full constructor
	 * 
	 * @param iconName
	 *            Relative path to icon
	 * @param nodeName
	 *            The node name
	 * @param tabName
	 *            The name of the extra options tab
	 * @param columnNames
	 *            The name of the column selectors in the node dialog
	 * @param factoryClazz
	 *            The {@link NodeFactory} class for adding the correct bundle
	 *            information and locating the icon
	 * @param hasAutorangeAxes
	 *            Does the node have the axis ranging options?
	 */
	public AbstractKernelDensityPlotNodeDescription(String iconName,
			String nodeName, String tabName, String[] columnNames,
			Class<? extends NodeFactory<?>> factoryClazz,
			boolean hasAutorangeAxes) {
		super(iconName, nodeName, tabName, columnNames, factoryClazz);
		is2D = columnNames.length > 1;
		this.hasAutoRangeAxesOptions = hasAutorangeAxes;
	}

	@Override
	public String getViewDescription(int index) {
		return "View showing the " + getInteractiveViewName();
	}

	@Override
	public String getOutportName(int index) {
		return getInteractiveViewName();
	}

	@Override
	public String getInteractiveViewName() {
		return (is2D ? "2D" : "1D") + " Kernel Density Plot";
	}

	@Override
	protected String getShortDescriptionImpl() {
		return "Node to generate and draw a " + (is2D ? "2D" : "1D")
				+ " Kernel Density Function";
	}

	@Override
	protected void addNodeDescription(XmlCursor introCursor) {
		createIntroParagraphs(introCursor);

		introCursor.insertElementWithText("h2", "Kernel Estimators");
		TableFactory tf = new TableFactory("Name", "Function").setPreTableText(
				"A variety of kernel estimators are available, as shown in the table:");
		for (KernelEstimator kEst : KernelEstimator.values()) {
			tf.addRowToTable(kEst.name(), kEst.getDescription());
		}

		tf.buildTable(introCursor);
		if (is2D) {
			tf = new TableFactory("Name", "Function").setPreTableText(
					"In the 2D case, u is a vector.  The '" + KERNEL_SYMMETRY
							+ "' option controls how the 1-dimensional '"
							+ KERNEL_ESTIMATOR
							+ "' is applied, as shown in the table");
			for (KernelSymmetry kSymm : KernelSymmetry.values()) {
				tf.addRowToTable(kSymm.name(), kSymm.getDescription());
			}
			tf.buildTable(introCursor);
		}

		introCursor.insertElementWithText("h2", "Bandwidth estimation");
		introCursor.beginElement("p");
		introCursor.insertChars(
				"The bandwidth effects the 'smoothness' of the kernel density function. "
						+ "There are a number of methods to automatically guess a suitable bandwidth.  In this "
						+ "node we only offer three options, as shown in the table below.  "
						+ "For further details see the ");
		introCursor.beginElement("a");
		introCursor.insertAttributeWithValue("href",
				"https://en.wikipedia.org/wiki/Multivariate_kernel_density_estimation#Rule_of_thumb");
		introCursor.insertChars(
				"Wikipedia Multivariate Kernel Density estimation");
		introCursor.toEndToken();
		introCursor.toNextToken();
		introCursor.insertChars(" page.");
		if (is2D) {
			introCursor.insertChars(
					" Bandwidths and estimation methods are set independantly for each dimension."
							+ " The bandwidth matrix, H is a diagonal matrix. "
							+ "Currently off-diagonal elements are not supported.");
		}
		introCursor.toEndToken();
		introCursor.toNextToken();
		tf = new TableFactory("Name", "Function")
				.setPreTableText("The methods offered are:").setPostTableText(
						"All methods are for a constant bandwidth across the whole data series");
		for (BandwidthEstimationMethod bem : BandwidthEstimationMethod
				.values()) {
			tf.addRowToTable(bem.getText(), bem.getToolTip());
		}
		tf.buildTable(introCursor);

	}

	/**
	 * Method to add the introductory paragraph prior the the 'Kernel
	 * Estimators' section. The default implementation adds a simple descriptive
	 * paragraph with link to the Wikipedia Kernel (Statistics) article
	 * 
	 * @param introCursor
	 *            The {@link XmlCursor} to add the paragraph with
	 */
	protected void createIntroParagraphs(XmlCursor introCursor) {
		introCursor.beginElement("p");
		introCursor.insertChars("This node plots a ");
		introCursor.insertChars(is2D ? "2D " : "1D ");
		introCursor.beginElement("a");
		introCursor.insertAttributeWithValue("href",
				"https://en.wikipedia.org/wiki/Kernel_(statistics)");
		introCursor.insertChars("Kernel density function");
		introCursor.toEndToken();
		introCursor.toNextToken();
		introCursor.insertChars(" based on an incoming data table");
		introCursor.toEndToken();
		introCursor.toNextToken();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.jfcplot.core.nodes.
	 * AbstractDrawableSeriesPlotNodeDescription#
	 * prependAdditionalTabbedOptions(org.knime.node2012.
	 * FullDescriptionDocument.FullDescription)
	 */
	@Override
	protected Tab prependAdditionalTabbedOptions(FullDescription fullDesc) {
		Tab tab = NodeDescriptionUtils.createTab(fullDesc, KERNEL_OPTIONS);

		/*
		 * Column Selection Group
		 */
		if (is2D) {
			addOptionToTab(tab, columnNames[0],
					"The column in the incoming table containing the " + X
							+ "-values from which "
							+ "to generate the kernel(s)");
			addOptionToTab(tab, columnNames[0],
					"The column in the incoming table containing the " + Y
							+ "-values from which "
							+ "to generate the kernel(s)");
		} else {
			addOptionToTab(tab, columnNames[0],
					"The column in the incoming table containing the values from which "
							+ "to generate the kernel(s)");
			addOptionToTab(tab, GROUPING_COLUMN,
					"An optional grouping column, to generate separate kernels for each series");
			addOptionToTab(tab, SHOW_ALL_DATA_KERNEL,
					"If a grouping column is selected, this option "
							+ "allows an additional kernel to be shown, which includes the entire data column, "
							+ "including those rows where the grouping column contains a missing value");
		}

		/*
		 * Kernel Options Group
		 */
		addOptionToTab(tab, KERNEL_ESTIMATOR,
				"The Kernel function to apply at each data point.  See above for details "
						+ "of the individual kernel estimators");
		if (is2D) {
			addOptionToTab(tab, KERNEL_SYMMETRY,
					"The kernel symmetry function to be applied to combined kernel estimators from the "
							+ X + "- and " + Y
							+ "-dimensions. See above for further details.");
			addOptionToTab(tab, getDialogBandwidthBoxTitle(columnNames[0]),
					"The bandwidth estimation method to used for the " + X
							+ "-dimension. See above for details");
			addOptionToTab(tab, getDialogManualBandwidthName(true, X),
					"User-defined bandwidth");
			addOptionToTab(tab, getDialogBandwidthBoxTitle(columnNames[1]),
					"The bandwidth estimation method to used for the " + Y
							+ "-dimension. See above for details");
			addOptionToTab(tab, getDialogManualBandwidthName(true, Y),
					"User-defined bandwidth");
		} else {
			addOptionToTab(tab, getDialogBandwidthBoxTitle(VALUES_COLUMN),
					"The bandwidth estimation method to used for the dataset. See above for details");
			addOptionToTab(tab, getDialogManualBandwidthName(false, null),
					"User-defined bandwidth");
		}
		addOptionToTab(tab, getDialogGridPointsName(false),
				"The number of grid points to calculate the kernel density function value for");

		/*
		 * Outlier Options Group
		 */
		if (is2D) {
			addOptionToTab(tab, NUMBER_OF_OUTLIERS_OF_DATASET,
					"The %age of the dataset to show as outliers. "
							+ "Outliers are defined here as the first n points when sorted by increasing value of the "
							+ "kernel density function");
			addOptionToTab(tab, OUTLIER_SIZE,
					"The size of the outlier symbols");
			addOptionToTab(tab, OUTLIER_SHAPE,
					"The plot symbol to use for the outliers");
			addOptionToTab(tab, OUTLIER_COLOUR,
					"The colour of the outlier symbols");
		} else {
			addOptionToTab(tab, EXPAND_ALL_KERNELS_TO_WHOLE_RANGE,
					"If a grouping column is selected, this option "
							+ "allows all kernel density functions to be calculated over "
							+ "the full range of the plot when axis autoranging is selected");
		}

		/*
		 * Plot Options Grouping
		 */
		if (hasAutoRangeAxesOptions) {
			addOptionToTab(tab, getDialogAutoRangeAxisName(X), "Should the " + X
					+ "-Axis range be calculated automatically?"
					+ (is2D ? ""
							: " If so, "
									+ "it will be extended beyond the most extreme values by 5% of the range between them."));
			addOptionToTab(tab, getDialogAxisRangeName(X),
					"The manual axis range");
			if (is2D) {
				addOptionToTab(tab, getDialogAutoRangeAxisName(Y), "Should the "
						+ Y + "-Axis range be calculated automatically?");
				addOptionToTab(tab, getDialogAxisRangeName(Y),
						"The manual axis range");
			}
		}
		addOptionToTab(tab, SHOW_LEGEND, is2D
				? ("The colour spectrum or contour colours")
				: ("If a grouping column is selected, this option "
						+ "shows a legend for the colours of the individual kernels on the plot.  "
						+ "The colours will be derived from a color manager applied to the "
						+ "grouping column, or generated dynamically if there isnt an "
						+ "appropriate color manager"));
		addOptionToTab(tab, getDialogShowBandwidthName(is2D),
				"Should the bandwidth be shown on the axis label "
						+ "(or in the legend if a grouping column is selected)?");
		if (is2D) {
			addOptionToTab(tab, UPPER_BOUND,
					"The colour used for the highest density regions");
			addOptionToTab(tab, LOWER_BOUND,
					"The colour to use for the lowest density regions");
			addOptionToTab(tab, NUMBER_OF_CONTOURS,
					"The number of contours to plot. If this value is '0', "
							+ "then a continuous colour gradient will be used");
			addOptionToTab(tab, FILL_CONTOURS,
					"Should the contours be filled with solid colour, "
							+ "or only drawn as contour lines? Filled contours show all "
							+ "areas between contour levels as the same block colour");
			addOptionToTab(tab, CONTOUR_INTERVAL_SCHEMA,
					"The method used to determine contour intervals. "
							+ "Options are 'LINEAR', where contours are spaced equally across "
							+ "the intensity range, and 'QUANTILE' where the contours are "
							+ "spaced to give equal areas of each contour interval");
		}
		return tab;
	}
}
