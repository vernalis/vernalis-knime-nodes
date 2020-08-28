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
package com.vernalis.knime.jfcplot.core.nodes;

import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.FlowArrangement;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.title.Title;
import org.jfree.ui.RectangleEdge;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.util.ColumnFilter;
import org.knime.ext.jfc.node.base.JfcGenericBaseNodeModel;

import com.vernalis.knime.jfcplot.core.drawabledataobject.DrawableDataObject;
import com.vernalis.knime.jfcplot.core.drawabledataobject.DrawableDataObjectFastPlot;
import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.nodes.SettingsModelRegistry;

import static com.vernalis.knime.jfcplot.core.nodes.AbstractDrawableSeriesPlotNodeDialogPane.createColumnNameModel;

/**
 * Base node model class for nodes generating plots via the
 * {@link DrawableDataObject} interface. This node provides the methods of
 * {@link SettingsModelRegistry}, with all methods mirrored, e.g.
 * {@link #registerSettingsModel(SettingsModel)}.
 * <p>
 * Column selections are guessed automatically according to the
 * {@link ColumnFilter} and regex {@link Pattern}s supplier to the constructor.
 * Subclasses with further configure requirements should override
 * {@link #doConfigure(DataTableSpec)}, preferably makeing a call to
 * {@code super.doConfigure(inSpec)}
 * </p>
 * 
 * 
 * @author S.Roughley knime@vernalis.com
 *
 * @param <T>
 */
public abstract class AbstractDrawableSeriesPlotNodeModel<T extends DrawableDataObject>
		extends JfcGenericBaseNodeModel {

	/**
	 * A simple data container which holds two collections of objects:
	 * <ul>
	 * <li>A collection of data for plotting</li>
	 * <li>A collection of {@link Title}s to add to the chart as legends</li>
	 * </ul>
	 * Also tracks the {@link DoubleSummaryStatistics} for the x and y axis
	 * 
	 * @author S.Roughley knime@vernalis.com
	 *
	 * @param <E>
	 *            The type of the data items, which must be subclasses of
	 *            {@link DrawableDataObject}
	 */
	public static final class ChartData<E extends DrawableDataObject> {

		private final Collection<E> data;
		private final Collection<Title> legends;
		private final DoubleSummaryStatistics xDss =
				new DoubleSummaryStatistics();

		private final DoubleSummaryStatistics yDss =
				new DoubleSummaryStatistics();

		public static <T extends DrawableDataObject> ChartData<T> createNoMemberList() {
			return new ChartData<>(new ArrayList<>(), new ArrayList<>());
		}

		public ChartData(Collection<E> data, Collection<Title> legends) {
			this.data = data == null ? Collections.emptySet() : data;
			this.legends = legends == null ? Collections.emptySet() : legends;
		}

		public boolean addData(E datum) {
			boolean retVal = this.data.add(datum);
			if (retVal) {
				xDss.combine(datum.getXStream().summaryStatistics());
				yDss.combine(datum.getYStream().summaryStatistics());
			}
			return retVal;
		}

		public void addData(Collection<? extends E> data) {
			boolean retVal = this.data.addAll(data);
			if (retVal) {
				xDss.combine(data.stream().flatMapToDouble(d -> d.getXStream())
						.summaryStatistics());
				yDss.combine(data.stream().flatMapToDouble(d -> d.getYStream())
						.summaryStatistics());
			}
		}

		public boolean addLegend(Title t) {
			return this.legends.add(t);
		}

		public boolean addLegends(Collection<? extends Title> c) {
			return this.legends.addAll(c);
		}

		/**
		 * @return the data
		 */
		public final Collection<E> getData() {
			return data;
		}

		/**
		 * @return the legends
		 */
		public final Collection<Title> getLegends() {
			return legends;
		}

		/**
		 * @return the x-axis {@link DoubleSummaryStatistics}
		 */
		public final DoubleSummaryStatistics getxDss() {
			return xDss;
		}

		/**
		 * @return the y-axis {@link DoubleSummaryStatistics}
		 */
		public final DoubleSummaryStatistics getyDss() {
			return yDss;
		}

		/**
		 * This method merges a second data object into the current object. It
		 * is the equivalent of performing {@link Collection#addAll(Collection)}
		 * on the respective pairs of underlying collections. NB If the attempt
		 * to add the legends fails, the changes made to the data will remain
		 *
		 * @param o
		 *            The object to merge
		 * @return {@code} true if either underlying collection is changed by
		 *         the merge event
		 * @throws UnsupportedOperationException
		 *             if the addAll operation is not supported by the relevant
		 *             underlying collection
		 * @throws ClassCastException
		 *             - if the class of an element of the specified object
		 *             prevents it from being added to the relevant underlying
		 *             collection
		 * @throws NullPointerException
		 *             - if the specified object contains a null element and the
		 *             relevant underlying collection does not permit null
		 *             elements, or if argument is null
		 * @throws IllegalArgumentException
		 *             - if some property of an element of the specified object
		 *             prevents it from being added to the relevant underlying
		 *             collection
		 * @throws IllegalStateException
		 *             - if not all the elements can be added at this time due
		 *             to insertion restrictions
		 */
		public boolean merge(ChartData<? extends E> o)
				throws UnsupportedOperationException, NullPointerException,
				ClassCastException, IllegalArgumentException,
				IllegalStateException {
			if (o == null) {
				throw new NullPointerException();
			}
			xDss.combine(o.xDss);
			yDss.combine(o.yDss);
			return data.addAll(o.getData()) | legends.addAll(o.getLegends());
		}
	}

	private final Set<SettingsModel> models = new LinkedHashSet<>();
	protected final SettingsModelRegistry smr = new SettingsModelRegistry() {

		@Override
		public Set<SettingsModel> getModels() {
			return models;
		}
	};

	protected final SettingsModelString[] columnNameMdls;
	protected final ColumnFilter[] acceptedColumnsFilters;
	protected final Pattern[] columnPreferredPatterns;

	/** column indices that specify which columns are used. */
	protected int[] colIdx;
	protected int colorcolumn = -1;
	protected int shapecolumn = -1;
	protected int sizecolumn = -1;

	/**
	 * Overloaded constructor for the node model. This default constructor
	 * creates a node with one input port and one output image port. The node
	 * has no regex patterns for the column selectors.
	 * 
	 * @param columnLabels
	 *            The names of the column selectors
	 * @param classFilters
	 *            The {@link ColumnFilter}s for the columns - must be the same
	 *            number as columnLabels
	 */
	public AbstractDrawableSeriesPlotNodeModel(String[] columnLabels,
			ColumnFilter[] classFilters) {
		this(columnLabels, classFilters, null);
	}

	/**
	 * Full constructor for the node model. This default constructor creates a
	 * node with one input port and one output image port. The node has regex
	 * patterns for the column selectors to guess with
	 * 
	 * @param columnLabels
	 *            The names of the column selectors
	 * @param classFilters
	 *            The {@link ColumnFilter}s for the columns - must be the same
	 *            number as columnLabels
	 * @param columnPreferredPatterns
	 *            The preferred regex patterns. If this is {@code null}, or
	 *            contains and {@code null} values, they will be replaced with
	 *            the catch-all '.*' regular expression
	 */
	public AbstractDrawableSeriesPlotNodeModel(String[] columnLabels,
			ColumnFilter[] classFilters, Pattern[] columnPreferredPatterns) {
		super();
		if (columnLabels == null || classFilters == null
				|| columnLabels.length != classFilters.length
				|| (columnPreferredPatterns != null
						&& columnPreferredPatterns.length != columnLabels.length)) {
			throw new IllegalArgumentException(
					"Must supply non-null column selection details, with same number of names and filters");
		}

		columnNameMdls = new SettingsModelString[columnLabels.length];
		for (int i = 0; i < columnLabels.length; i++) {
			if (columnLabels[i] == null || classFilters[i] == null) {
				throw new IllegalArgumentException(
						"Must supply non-null column selection details, with same number of names and filters");
			}
			columnNameMdls[i] = registerSettingsModel(
					createColumnNameModel(columnLabels[i]));
		}

		this.acceptedColumnsFilters = ArrayUtils.copy(classFilters);
		if (columnPreferredPatterns == null) {
			this.columnPreferredPatterns =
					ArrayUtils.of(Pattern.compile(".*"), columnLabels.length);
		} else {
			this.columnPreferredPatterns =
					Arrays.stream(columnPreferredPatterns)
							.map(x -> x == null ? Pattern.compile(".*") : x)
							.toArray(Pattern[]::new);
		}
	}

	/**
	 * Method to store non-null {@link SettingsModel}s for saving/loading
	 * 
	 * Delegates to
	 * {@link SettingsModelRegistry#registerSettingsModel(SettingsModel)}
	 */
	protected final <U extends SettingsModel> U registerSettingsModel(U model) {
		return smr.registerSettingsModel(model);
	}

	/**
	 * Method to store multiple {@link SettingsModel}s for saving/loading
	 * 
	 * Delegates to {@link SettingsModelRegistry#registerModels(Iterable)}
	 */
	protected final <U extends Iterable<V>, V extends SettingsModel> U registerModels(
			U models) {
		return smr.registerModels(models);
	}

	/**
	 * Method to store multiple models passed as the values of a {@link Map} for
	 * saving / loading
	 * 
	 * Delegates to {@link SettingsModelRegistry#registerMapValuesModels(Map))}
	 */
	protected final <U extends Map<?, V>, V extends SettingsModel> U registerMapValuesModels(
			U models) {
		return smr.registerMapValuesModels(models);
	}

	/**
	 * Method to store multiple models passed as the keys of a {@link Map} for
	 * saving / loading
	 * 
	 * Delegates to {@link SettingsModelRegistry#registerMapKeysModels(Map)}
	 */
	protected final <U extends Map<K, ?>, K extends SettingsModel> U registerMapKeysModels(
			U models) {
		return smr.registerMapKeysModels(models);
	}

	/**
	 * Delegates to
	 * {@link SettingsModelRegistry#getValidatedColumnSelectionModelColumnIndex(SettingsModelString, ColumnFilter, DataTableSpec, Pattern, boolean, NodeLogger, boolean, SettingsModelString...)}
	 */
	protected final int getValidatedColumnSelectionModelColumnIndex(
			SettingsModelString model, ColumnFilter filter, DataTableSpec spec,
			Pattern preferredPattern, boolean matchWholeName, NodeLogger logger,
			boolean dontAllowDuplicatesWithAvoid,
			SettingsModelString... modelsToAvoid)
			throws InvalidSettingsException {
		return smr.getValidatedColumnSelectionModelColumnIndex(model, filter,
				spec, preferredPattern, matchWholeName, logger,
				dontAllowDuplicatesWithAvoid, modelsToAvoid);
	}

	/**
	 * Delegates to
	 * {@link SettingsModelRegistry#getValidatedColumnSelectionModelColumnIndex(SettingsModelString, ColumnFilter, DataTableSpec, NodeLogger, SettingsModelString...)}
	 */
	protected final int getValidatedColumnSelectionModelColumnIndex(
			SettingsModelString model, ColumnFilter filter, DataTableSpec spec,
			NodeLogger logger, SettingsModelString... modelsToAvoid)
			throws InvalidSettingsException {
		return smr.getValidatedColumnSelectionModelColumnIndex(model, filter,
				spec, logger, modelsToAvoid);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveAdditionalSettingsTo(final NodeSettingsWO settings) {
		smr.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validateAdditionalSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		smr.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void loadAdditionalValidatedSettingsFrom(
			final NodeSettingsRO settings) throws InvalidSettingsException {
		smr.loadValidatedSettingsFrom(settings);
	}

	@Override
	public void doReset() {
		colIdx = ArrayUtils.of(-1, columnNameMdls.length);
		colorcolumn = -1;
		shapecolumn = -1;
		sizecolumn = -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.ext.jfc.node.base.JfcGenericBaseNodeModel#doConfigure(org.knime
	 * .core.data.DataTableSpec)
	 */
	@Override
	public void doConfigure(DataTableSpec inSpec)
			throws InvalidSettingsException {

		// Now validate the selections, guessing if needed
		colIdx = ArrayUtils.of(-1, columnNameMdls.length);
		List<SettingsModelString> colModels = new ArrayList<>();
		Collections.addAll(colModels, columnNameMdls);
		for (int i = 0; i < columnNameMdls.length; i++) {
			final SettingsModelString currColMdl = columnNameMdls[i];
			colModels.remove(currColMdl);
			colIdx[i] = getValidatedColumnSelectionModelColumnIndex(currColMdl,
					acceptedColumnsFilters[i], inSpec,
					columnPreferredPatterns[i], true, getLogger(),
					true/* Assume x and y are never same */,
					colModels.toArray(new SettingsModelString[0]));
			colModels.add(currColMdl);
		}
		// Now sort out the columns used for the legend
		colorcolumn = -1;
		shapecolumn = -1;
		sizecolumn = -1;

		for (int i = 0; i < inSpec.getNumColumns(); i++) {
			DataColumnSpec colSpec = inSpec.getColumnSpec(i);
			if (colorcolumn < 0 && colSpec.getColorHandler() != null) {
				colorcolumn = i;
			}
			if (shapecolumn < 0 && colSpec.getShapeHandler() != null) {
				shapecolumn = i;
			}
			if (sizecolumn < 0 && colSpec.getSizeHandler() != null) {
				sizecolumn = i;
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final JFreeChart createChart(final PortObject[] portObjs,
			final ExecutionContext exec) throws Exception {

		// 90% of process is compiling data
		ExecutionContext exec0 = exec.createSubExecutionContext(0.9);

		// Now handle the non-scatter ports
		ChartData<DrawableDataObject> dataPoints =
				ChartData.createNoMemberList();

		exec.setMessage("Compiling data");

		compileDataSet((BufferedDataTable) portObjs[0], dataPoints, exec0);

		// Axis labels
		AttributedString xAxisLabel = getXAxisLabel(dataPoints);
		if (xAxisLabel == null) {
			xAxisLabel = new AttributedString("");
		}
		AttributedString yAxisLabel = getYAxisLabel(dataPoints);
		if (yAxisLabel == null) {
			yAxisLabel = new AttributedString("");
		}

		DrawableDataObjectFastPlot plot =
				createPlotFromChartData(dataPoints, xAxisLabel, yAxisLabel);

		// Wrap the plot in a chart
		JFreeChart chartObj = new JFreeChart(plot);

		// Add any legends - we need to combine any for each side of the chart
		Map<RectangleEdge, List<Title>> legends = dataPoints.getLegends()
				.stream().collect(Collectors.groupingBy(Title::getPosition));
		for (Entry<RectangleEdge, List<Title>> legend : legends.entrySet()) {

			final List<Title> sideLegends = legend.getValue();
			if (sideLegends.size() > 1) {
				// Multiple legends - wrap them in a flow arrangement
				BlockContainer container =
						new BlockContainer(new FlowArrangement());
				sideLegends.forEach(l -> container.add(l));
				final CompositeTitle compositeTitle =
						new CompositeTitle(container);
				compositeTitle.setPosition(legend.getKey());
				chartObj.addSubtitle(compositeTitle);
			} else if (!sideLegends.isEmpty()) {
				// Only 1 - just add it
				chartObj.addSubtitle(sideLegends.get(0));
			}
		}

		// Set the font size and other 'theme' data
		ChartUtilities.applyCurrentTheme(chartObj);
		return chartObj;

	}

	/**
	 * Method to create the plot from the dataPoints.
	 * 
	 * @param dataPoints
	 *            The {@link ChartData} object
	 * @param xAxisLabel
	 *            The x-axis label
	 * @param yAxisLabel
	 *            The y-axis label
	 * @return The {@link DrawableDataObjectFastPlot}-based chart object
	 */
	protected abstract DrawableDataObjectFastPlot createPlotFromChartData(
			ChartData<DrawableDataObject> dataPoints,
			AttributedString xAxisLabel, AttributedString yAxisLabel);

	/**
	 * @param chartData
	 *            The {@link ChartData} object for the current chart
	 * @return Return the label for the x axis
	 */
	protected abstract AttributedString getXAxisLabel(
			ChartData<? extends DrawableDataObject> chartData);

	/**
	 * @param chartData
	 *            The {@link ChartData} object for the current chart
	 * @return Return the label for the y axis
	 */
	protected abstract AttributedString getYAxisLabel(
			ChartData<? extends DrawableDataObject> chartData);

	private final void compileDataSet(BufferedDataTable dataTable,
			ChartData<DrawableDataObject> chartData, ExecutionContext exec)
			throws Exception {

		DataTableSpec tableSpec = dataTable.getDataTableSpec();
		int portColorColumn = -1;
		int portShapeColumn = -1;
		int portSizeColumn = -1;
		for (int i = 0; i < tableSpec.getNumColumns(); i++) {
			DataColumnSpec colSpec = tableSpec.getColumnSpec(i);
			if (colSpec.getColorHandler() != null) {
				portColorColumn = i;
			}
			if (colSpec.getShapeHandler() != null) {
				portShapeColumn = i;
			}
			if (colSpec.getSizeHandler() != null) {
				portSizeColumn = i;
			}
			if (portColorColumn != -1 && portShapeColumn != -1
					&& portSizeColumn != -1) {
				break;
			}
		}
		ChartData<T> tempData = ChartData.createNoMemberList();

		addDataSeriesFromInputPort(dataTable, portColorColumn, portShapeColumn,
				portSizeColumn, tempData, exec);

		tempData.addLegends(createLegendsFromInputPort(tableSpec,
				tempData.getData(), portColorColumn, portShapeColumn,
				portSizeColumn, exec));
		chartData.merge(tempData);
	}

	/**
	 * Method to create the legend items from any non-scatter ports (the scatter
	 * port is handled in this abstract class). The default implementation
	 * returns an empty collection
	 * 
	 * @param tableSpec
	 *            The table spec of the incoming table, never {@code null}
	 * @param dataFromTable
	 *            The data series from the incoming table, as created by
	 *            {@link #addDataSeriesFromInputPort(BufferedDataTable, int, int, int, ChartData, ExecutionContext)}
	 * @param colorcolumn
	 *            The index of the column containing the color manager in the
	 *            incoming port, or -1 if none is specified
	 * @param shapecolumn
	 *            The index of the column containing the shape manager in the
	 *            incoming port, or -1 if none is specified
	 * @param sizecolumn
	 *            The index of the column containing the size manager in the
	 *            incoming port, or -1 if none is specified
	 * @param exec
	 *            The node {@link ExecutionContext} to allow cancelling or
	 *            progress monitoring. This is a sub-execution context with the
	 *            appropriate fraction of execution for the incoming table
	 * @return The legends for the current data port
	 * @throws CanceledExecutionException
	 *             If the node execution was cancelled by the user
	 */
	protected abstract Collection<? extends Title> createLegendsFromInputPort(
			DataTableSpec tableSpec, Collection<T> dataFromTable,
			int colorcolumn, int shapecolumn, int sizecolumn,
			ExecutionContext exec) throws CanceledExecutionException;

	/**
	 * Method to create a collection of {@link DrawableDataObject}s for plotting
	 * from the data table in the specified port.
	 * 
	 * @param table
	 *            The incoming data table
	 * @param colorcolumn
	 *            The index of the column containing the color manager in the
	 *            incoming port, or -1 if none is specified
	 * @param shapecolumn
	 *            The index of the column containing the shape manager in the
	 *            incoming port, or -1 if none is specified
	 * @param sizecolumn
	 *            The index of the column containing the size manager in the
	 *            incoming port, or -1 if none is specified
	 * @param chartData
	 *            The chart data - {@link DrawableDataObject}s to be plotted
	 *            <em>must</em> be added to this data container in order to be
	 *            plotted
	 * @param exec
	 *            The node {@link ExecutionContext} to allow cancelling or
	 *            progress monitoring. This is a sub-execution context with the
	 *            appropriate fraction of execution for the incoming table
	 * @throws CanceledExecutionException
	 *             If the node execution was cancelled by the user
	 * @throws Exception
	 *             Any other exception thrown during node execution
	 */
	protected abstract void addDataSeriesFromInputPort(
			final BufferedDataTable table, final int colorcolumn,
			final int shapecolumn, final int sizecolumn, ChartData<T> chartData,
			ExecutionContext exec) throws CanceledExecutionException, Exception;

}
