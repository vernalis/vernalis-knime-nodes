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
package com.vernalis.knime.chem.pmi.nodes.plot.abstrct;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.ArrayList;
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
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.ColumnArrangement;
import org.jfree.chart.block.FlowArrangement;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.title.Title;
import org.jfree.ui.RectangleEdge;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.property.ColorModelRange;
import org.knime.core.data.property.ShapeFactory;
import org.knime.core.data.property.SizeHandler;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColor;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.ColumnFilter;
import org.knime.ext.jfc.node.base.JfcGenericBaseNodeModel;
import org.knime.ext.jfc.node.heatmap.util.TwoValuePaintScale;

import com.vernalis.knime.dialog.components.SettingsModelShape;
import com.vernalis.knime.jfcplot.core.drawabledataobject.DrawableDataObject;
import com.vernalis.knime.jfcplot.core.drawabledataobject.SimpleShapeDrawableDataObject;
import com.vernalis.knime.jfcplot.core.legend.TitledLegend;
import com.vernalis.knime.jfcplot.core.legenditemsources.ColourLegendItemSource;
import com.vernalis.knime.jfcplot.core.legenditemsources.ColourShapeLegendItemSource;
import com.vernalis.knime.jfcplot.core.legenditemsources.ShapeLegendItemSource;
import com.vernalis.knime.jfcplot.core.legenditemsources.SizeLegendItemSource;
import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.nodes.SettingsModelRegistry;

import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.createShowFullTriangleModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.getColourGradientModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.getDefaultShapeModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.getElementSizeModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.getNPMI1ColumnModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.getNPMI2ColumnModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.getShapeLegendModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.getTriangleColourModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.getVertexColourModel;

/**
 * The node model implementation for the PMI triangle scatter plot node.
 * Subclasses may need to override or implement the following methods:
 * <ul>
 * <li>{@link #doConfigure(DataTableSpec, int)}</li>
 * <li>{@link #addDataSeriesFromInputPort(BufferedDataTable, int, int, int, int, ChartData, ExecutionContext)}</li>
 * <li>{@link #createLegendsFromInputPort(DataTableSpec, Collection, int, int, int, int, ExecutionContext)}</li>
 * </ul>
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The type of the {@link DrawableDataObject}s from the 0th input
 *            port if it is not also the last port (the optional - if there is
 *            more than 1 input port - scatter port)
 */
public class AbstractPMIDrawableSeriesPlotNodeModel<T extends DrawableDataObject>
		extends JfcGenericBaseNodeModel {

	/**
	 * A simple data container which holds two collections of objects:
	 * <ul>
	 * <li>A collection of data for plotting</li>
	 * <li>A collection of {@link Title}s to add to the chart as legends</li>
	 * </ul>
	 * Also tracks the {@link DoubleSummaryStatistics} for the x and y axis
	 * 
	 * @author s.roughley
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

	protected final SettingsModelString npr1ColumnNameMdl =
			registerSettingsModel(getNPMI1ColumnModel());
	protected final SettingsModelString npr2ColumnNameMdl =
			registerSettingsModel(getNPMI2ColumnModel());
	protected final SettingsModelDoubleBounded itemSizeMdl =
			registerSettingsModel(getElementSizeModel());
	protected final SettingsModelBoolean showFullTriangleMdl =
			registerSettingsModel(createShowFullTriangleModel());

	protected final SettingsModelColor vertexColourMdl =
			registerSettingsModel(getVertexColourModel());
	protected final SettingsModelColor triangleColourMdl =
			registerSettingsModel(getTriangleColourModel());

	protected final SettingsModelShape defaultShapeMdl =
			registerSettingsModel(getDefaultShapeModel());
	protected final SettingsModelBoolean shapeLegendMdl =
			registerSettingsModel(getShapeLegendModel());
	protected final SettingsModelBoolean colourRangeLegendMdl =
			registerSettingsModel(getColourGradientModel());
	protected final SettingsModelBoolean sizeLegendMdl = registerSettingsModel(
			AbstractPMIDrawableSeriesNodeDialogPane.getSizeLegendModel());

	public static final Pattern NPR1_PATTERN = Pattern
			.compile("^(I1\\s*/\\s*I3|npr1|nPMI1).*", Pattern.CASE_INSENSITIVE);
	public static final Pattern NPR2_PATTERN = Pattern
			.compile("^(I2\\s*/\\s*I3|npr2|nPMI2).*", Pattern.CASE_INSENSITIVE);

	/** column indices that specify which columns are used. */
	protected int npr1ColIdx = -1;
	protected int npr2ColIdx = -1;
	protected int colorcolumn = -1;
	protected int shapecolumn = -1;
	protected int sizecolumn = -1;
	protected final int scatterPortId;
	private double scatterPointMinSize;
	private double scatterPointMaxSize;

	/**
	 * Constructor for the node model. This default constructor creates a node
	 * with one input port and one output image port. The node has NPR columns
	 */
	public AbstractPMIDrawableSeriesPlotNodeModel() {
		this(1);
	}

	/**
	 * Constructor to specify the number of input ports
	 * 
	 * @param numPorts
	 *            The number of inputs - all are {@link BufferedDataTable}, and
	 *            if there are more than one, then the last is optional
	 */
	public AbstractPMIDrawableSeriesPlotNodeModel(int numPorts) {
		this(createInputPorts(numPorts));
	}

	/**
	 * Constructor allowing control over all input port types
	 * 
	 * @param inPorts
	 *            The input port types
	 */
	public AbstractPMIDrawableSeriesPlotNodeModel(PortType[] inPorts) {
		super(inPorts);
		scatterPortId = inPorts.length - 1;
	}

	/**
	 * Method to create the input ports. All are {@link BufferedDataTable}, and
	 * if there are more than one, then the last is optional
	 * 
	 * @param numPorts
	 *            The number of ports
	 * @return
	 */
	private static PortType[] createInputPorts(int numPorts) {
		final PortType[] portTypes =
				ArrayUtils.fill(new PortType[numPorts], BufferedDataTable.TYPE);
		// Last, scatter overlay, port is optional when other ports are present
		if (numPorts > 1) {
			portTypes[numPorts - 1] = BufferedDataTable.TYPE_OPTIONAL;
		}
		return portTypes;
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
		npr1ColIdx = -1;
		npr2ColIdx = -1;
		colorcolumn = -1;
		shapecolumn = -1;
		sizecolumn = -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void doConfigure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		for (int i = 0; i < scatterPortId; i++) {
			if (inSpecs[i] instanceof DataTableSpec) {
				doConfigure((DataTableSpec) inSpecs[i], i);
			} else {
				throw new InvalidSettingsException("Port index '" + i
						+ "' not connected or wrong port type");
			}
		}

		if (inSpecs[scatterPortId] instanceof DataTableSpec) {
			// Only if the scatter port is connected!
			doConfigure((DataTableSpec) inSpecs[scatterPortId]);
		} else {
			colorcolumn = -1;
			shapecolumn = -1;
			sizecolumn = -1;
			npr1ColIdx = -1;
			npr2ColIdx = -1;
		}

	}

	/**
	 * Configure method for the non-scatter ports. This NodeModel class handles
	 * the scatter port settings
	 * 
	 * @param dataTableSpec
	 *            The table spec of the incoming table
	 * @param portId
	 *            The port index
	 * @throws InvalidSettingsException
	 *             If there is a problem with the settings for this port
	 */
	protected void doConfigure(DataTableSpec dataTableSpec, int portId)
			throws InvalidSettingsException {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.ext.jfc.node.base.JfcGenericBaseNodeModel#doConfigure(org.knime
	 * .core.data.DataTableSpec)
	 */
	@Override
	public final void doConfigure(DataTableSpec inSpec)
			throws InvalidSettingsException {

		// We use this method to configure the scatter plot port - only called
		// if it is connected

		// Check we have enough columns
		if (inSpec.stream()
				.filter(cSpec -> AbstractPMIDrawableSeriesNodeDialogPane.DOUBLE_VALUE_COLUMN_FILTER
						.includeColumn(cSpec))
				.count() < 2) {
			throw new InvalidSettingsException(
					"As least two double columns are required in the input table");
		}

		// Now validate the selections, guessing if needed
		npr1ColIdx =
				getValidatedColumnSelectionModelColumnIndex(npr1ColumnNameMdl,
						AbstractPMIDrawableSeriesNodeDialogPane.DOUBLE_VALUE_COLUMN_FILTER,
						inSpec, NPR1_PATTERN, false, getLogger(), false,
						npr2ColumnNameMdl);
		npr2ColIdx =
				getValidatedColumnSelectionModelColumnIndex(npr2ColumnNameMdl,
						AbstractPMIDrawableSeriesNodeDialogPane.DOUBLE_VALUE_COLUMN_FILTER,
						inSpec, NPR2_PATTERN, false, getLogger(), false,
						npr1ColumnNameMdl);

		// Finally, check the column bounds, if they exist, are within the
		// appropriate range
		if (npr1ColIdx >= 0
				&& inSpec.getColumnSpec(npr1ColIdx).getDomain().hasBounds()
				&& ((((DoubleValue) inSpec.getColumnSpec(npr1ColIdx).getDomain()
						.getLowerBound()).getDoubleValue() < 0.0)
						|| ((DoubleValue) inSpec.getColumnSpec(npr1ColIdx)
								.getDomain().getUpperBound())
										.getDoubleValue() > 1.0)) {
			throw new InvalidSettingsException(
					"nPMI1 column domain is outside the bounds 0.0 - 1.0");
		}

		if (npr2ColIdx >= 0
				&& inSpec.getColumnSpec(npr2ColIdx).getDomain().hasBounds()
				&& ((((DoubleValue) inSpec.getColumnSpec(npr2ColIdx).getDomain()
						.getLowerBound()).getDoubleValue() < 0.5)
						|| ((DoubleValue) inSpec.getColumnSpec(npr2ColIdx)
								.getDomain().getUpperBound())
										.getDoubleValue() > 1.0)) {
			throw new InvalidSettingsException(
					"nPMI2 column domain is outside the bounds 0.5 - 1.0");
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

		boolean scatterConnected = portObjs[scatterPortId] != null;
		// 90% of process is compiling data
		ExecutionContext exec0 = exec.createSubExecutionContext(0.9);
		double progPerPort = 1.0
				/ (scatterConnected ? portObjs.length : (portObjs.length - 1));

		// Now handle the non-scatter ports
		ChartData<DrawableDataObject> dataPoints =
				ChartData.createNoMemberList();
		for (int i = 0; i < scatterPortId; i++) {
			exec0.setMessage("Compiling data for port " + i);
			ExecutionContext exec1 =
					exec0.createSilentSubExecutionContext(progPerPort);
			compileDataSet(portObjs[i], i, dataPoints, exec1);
		}

		// And the scatter port...

		if (scatterConnected) {
			exec0.setMessage("Compiling data for scatter points");
			ExecutionContext exec1 =
					exec0.createSilentSubExecutionContext(progPerPort);
			compileScatterDataSet(portObjs[scatterPortId], dataPoints, exec1);
		}

		// Axis labels
		AttributedString xAxisLabel =
				new AttributedString("I1/I3" + getXAxisLabelSuffix());
		xAxisLabel.addAttribute(TextAttribute.SUPERSCRIPT,
				TextAttribute.SUPERSCRIPT_SUB, 1, 2);
		xAxisLabel.addAttribute(TextAttribute.SUPERSCRIPT,
				TextAttribute.SUPERSCRIPT_SUB, 4, 5);
		AttributedString yAxisLabel =
				new AttributedString("I2/I3" + getYAxisLabelSuffix());
		yAxisLabel.addAttribute(TextAttribute.SUPERSCRIPT,
				TextAttribute.SUPERSCRIPT_SUB, 1, 2);
		yAxisLabel.addAttribute(TextAttribute.SUPERSCRIPT,
				TextAttribute.SUPERSCRIPT_SUB, 4, 5);

		PMITriangleDrawableDataObjectFastPlot plot =
				showFullTriangleMdl.getBooleanValue()
						? new PMITriangleDrawableDataObjectFastPlot(
								dataPoints.getData(), xAxisLabel, -0.05, 1.05,
								yAxisLabel, 0.475, 1.025,
								vertexColourMdl.getColorValue(),
								triangleColourMdl.getColorValue())
						: new PMITriangleDrawableDataObjectFastPlot(
								dataPoints.getData(), xAxisLabel, 0.05,
								dataPoints.getxDss(), yAxisLabel, 0.05,
								dataPoints.getyDss(),
								vertexColourMdl.getColorValue(),
								triangleColourMdl.getColorValue());

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
	 * @return Optionally return a suffix for the x-axis label
	 */
	protected String getXAxisLabelSuffix() {
		return "";
	}

	/**
	 * @return Optionally return a suffix for the y-axis label
	 */
	private String getYAxisLabelSuffix() {
		return "";
	}

	private final void compileDataSet(PortObject portObject, int portId,
			ChartData<DrawableDataObject> chartData, ExecutionContext exec)
			throws CanceledExecutionException {
		final BufferedDataTable dataTable = (BufferedDataTable) portObject;
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

		addDataSeriesFromInputPort(dataTable, portId, portColorColumn,
				portShapeColumn, portSizeColumn, tempData, exec);

		tempData.addLegends(createLegendsFromInputPort(tableSpec,
				tempData.getData(), portId, portColorColumn, portShapeColumn,
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
	 *            {@link #addDataSeriesFromInputPort(BufferedDataTable, int, int, int, int, ChartData, ExecutionContext)}
	 * @param portId
	 *            The incoming port index
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
	protected Collection<? extends Title> createLegendsFromInputPort(
			DataTableSpec tableSpec, Collection<T> dataFromTable, int portId,
			int colorcolumn, int shapecolumn, int sizecolumn,
			ExecutionContext exec) throws CanceledExecutionException {
		// Dafault - Do nothing
		return Collections.emptySet();
	}

	/**
	 * Method to create a collection of {@link DrawableDataObject}s for plotting
	 * from the data table in the specified port.
	 * 
	 * @param table
	 *            The incoming data table
	 * @param portId
	 *            The incoming port index
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
	 * 
	 * @throws CanceledExecutionException
	 *             If the node execution was cancelled by the user
	 */
	protected void addDataSeriesFromInputPort(final BufferedDataTable table,
			int portId, final int colorcolumn, final int shapecolumn,
			final int sizecolumn, ChartData<T> chartData, ExecutionContext exec)
			throws CanceledExecutionException {
		// default implementation - do nothing
	}

	private final void compileScatterDataSet(PortObject portObject,
			ChartData<DrawableDataObject> chartData, ExecutionContext exec)
			throws CanceledExecutionException {

		final BufferedDataTable dataTable = (BufferedDataTable) portObject;

		ChartData<SimpleShapeDrawableDataObject> tempData =
				ChartData.createNoMemberList();
		createDataSeriesFromScatterPort(dataTable, colorcolumn, shapecolumn,
				sizecolumn, tempData, exec);
		tempData.addLegends(createLegendsFromScatterPort(
				dataTable.getDataTableSpec(), tempData.getData(), colorcolumn,
				shapecolumn, sizecolumn));
		chartData.merge(tempData);
	}

	private final void createDataSeriesFromScatterPort(
			BufferedDataTable dataTable, int colorcolumn, int shapecolumn,
			int sizecolumn, ChartData<SimpleShapeDrawableDataObject> chartData,
			ExecutionContext exec) throws CanceledExecutionException {

		scatterPointMinSize = Double.POSITIVE_INFINITY;
		scatterPointMaxSize = Double.NEGATIVE_INFINITY;

		// If no shape handler is provided we use the shape from the dialog
		ShapeFactory.Shape defaultShape = defaultShapeMdl.getShapeValue();

		boolean useDefaultShape = shapecolumn < 0;
		DataTableSpec tableSpec = dataTable.getDataTableSpec();

		final long tableSize = dataTable.size();
		double progresPerRow = 1.0 / tableSize;
		long rowidx = 0L;
		// extracting values from the DataRow(s) of the table
		for (DataRow row : dataTable) {

			DataCell xVal = row.getCell(npr1ColIdx);
			DataCell yVal = row.getCell(npr2ColIdx);
			if (xVal.isMissing() || yVal.isMissing()) {
				setWarningMessage("Missing values are ignored in the view!");
			} else {
				// Sort out the shape, size and colour of the row.
				Color rowColor = tableSpec.getRowColor(row).getColor();
				ShapeFactory.Shape rowShape = useDefaultShape ? defaultShape
						: tableSpec.getRowShape(row);
				double rowSize = itemSizeMdl.getDoubleValue()
						* tableSpec.getRowSizeFactor(row);

				double x = ((DoubleValue) xVal).getDoubleValue();
				double y = ((DoubleValue) yVal).getDoubleValue();

				DataCell sizeCell;
				if (sizecolumn < 0) {
					sizeCell = null;
				} else {
					sizeCell = row.getCell(sizecolumn);
					if (!sizeCell.isMissing() && sizeCell.getType()
							.isCompatible(DoubleValue.class)) {
						double sizeValue =
								((DoubleValue) sizeCell).getDoubleValue();
						scatterPointMinSize =
								Math.min(scatterPointMinSize, sizeValue);
						scatterPointMaxSize =
								Math.max(scatterPointMaxSize, sizeValue);
					}
				}
				// Colour argument is ignored in SingleColourDataSeries subclass
				// so just call this method
				final DataCell colourCell =
						colorcolumn < 0 ? null : row.getCell(colorcolumn);
				final DataCell shapeCell =
						shapecolumn < 0 ? null : row.getCell(shapecolumn);
				chartData.addData(new SimpleShapeDrawableDataObject(x, y,
						rowShape, rowSize, rowColor,
						shapeCell == null ? null
								: shapeCell.isMissing() ? "Missing"
										: shapeCell.toString(),
						sizeCell == null ? null
								: sizeCell.isMissing() ? "Missing"
										: sizeCell.toString(),
						colourCell == null ? null
								: colourCell.isMissing() ? "Missing"
										: colourCell.toString()));
			}
			exec.checkCanceled();
			exec.setProgress((++rowidx) * progresPerRow,
					"Read " + rowidx + " rows of " + tableSize);
		}

	}

	private final Collection<Title> createLegendsFromScatterPort(
			DataTableSpec dataTableSpec,
			Collection<SimpleShapeDrawableDataObject> dataSeriesFromScatterPort,
			int colorcolumn2, int shapecolumn2, int sizecolumn2)
			throws CanceledExecutionException {
		Collection<Title> legends = new LinkedHashSet<>();

		boolean sizeLegend = sizeLegendMdl.getBooleanValue() && sizecolumn > -1;

		boolean shapeLegend =
				shapeLegendMdl.getBooleanValue() && shapecolumn > -1;

		boolean gradientColourLegend =
				colourRangeLegendMdl.getBooleanValue() && colorcolumn > -1
						&& dataTableSpec.getColumnSpec(colorcolumn)
								.getColorHandler()
								.getColorModel() instanceof ColorModelRange;

		boolean nominalColourLegend = shapeLegendMdl.getBooleanValue()
				&& colorcolumn > -1 && !gradientColourLegend;
		boolean combinedShapeColourLegend = shapeLegend && nominalColourLegend
				&& shapecolumn == colorcolumn;

		String shapeColumnName = shapecolumn >= 0
				? dataTableSpec.getColumnSpec(shapecolumn).getName() : "";
		String colourColumnName = colorcolumn >= 0
				? dataTableSpec.getColumnSpec(colorcolumn).getName() : "";

		// Shapes and colours combined
		if (combinedShapeColourLegend) {
			// Simple shape / series colour legend
			LegendItemSource legendItems = new ColourShapeLegendItemSource(
					dataSeriesFromScatterPort, itemSizeMdl.getDoubleValue());
			CompositeTitle title;
			if (!sizeLegend
					&& legendItems.getLegendItems().getItemCount() < 3) {
				title = new TitledLegend(shapeColumnName,
						new LegendTitle(legendItems));
				title.setPosition(RectangleEdge.BOTTOM);
			} else {
				title = new TitledLegend(shapeColumnName, legendItems);
				title.setPosition(RectangleEdge.RIGHT);
			}
			// set the padding around the legend
			title.setPadding(5D, 5D, 40D, 5D);
			legends.add(title);
		} else if (shapeLegend || nominalColourLegend) {
			CompositeTitle shapeTitle = null;
			if (shapeLegend) {
				LegendItemSource legendItems =
						new ShapeLegendItemSource(dataSeriesFromScatterPort,
								itemSizeMdl.getDoubleValue());

				if (!sizeLegend && !nominalColourLegend
						&& legendItems.getLegendItems().getItemCount() < 3) {
					shapeTitle = new TitledLegend(shapeColumnName,
							new LegendTitle(legendItems));
					shapeTitle.setPosition(RectangleEdge.BOTTOM);
				} else {
					shapeTitle = new TitledLegend(shapeColumnName, legendItems);
					shapeTitle.setPosition(RectangleEdge.RIGHT);
				}
				// set the padding around the legend
				shapeTitle.setPadding(5D, 5D, 40D, 5D);
			}

			CompositeTitle colourTitle = null;
			if (nominalColourLegend) {
				ShapeFactory.Shape legendShape =
						defaultShapeMdl.getShapeValue();
				LegendItemSource legendItems =
						new ColourLegendItemSource(dataSeriesFromScatterPort,
								itemSizeMdl.getDoubleValue(), legendShape);

				if (!sizeLegend && !shapeLegend
						&& legendItems.getLegendItems().getItemCount() < 3) {
					colourTitle = new TitledLegend(colourColumnName,
							new LegendTitle(legendItems));
					colourTitle.setPosition(RectangleEdge.BOTTOM);
				} else {
					colourTitle =
							new TitledLegend(colourColumnName, legendItems);
					colourTitle.setPosition(RectangleEdge.RIGHT);
				}
				// set the padding around the legend
				colourTitle.setPadding(5D, 5D, 40D, 5D);

			}
			if (shapeLegend && nominalColourLegend) {
				// Put them in a single legend, one above the other
				BlockContainer container =
						new BlockContainer(new ColumnArrangement());
				container.add(colourTitle);
				container.add(shapeTitle);
				final CompositeTitle compositeTitle =
						new CompositeTitle(container);
				compositeTitle.setPosition(RectangleEdge.RIGHT);
				legends.add(compositeTitle);

			} else if (shapeLegend) {
				legends.add(shapeTitle);
			} else {
				legends.add(colourTitle);
			}
		}

		// Do we have a gradient Colour Legend? If so, it is always on the
		// left
		if (gradientColourLegend) {
			// Colour bar
			ColorModelRange cmdl =
					(ColorModelRange) dataTableSpec.getColumnSpec(colorcolumn)
							.getColorHandler().getColorModel();
			TwoValuePaintScale scale = new TwoValuePaintScale(cmdl);

			NumberAxis zAxis = new NumberAxis(
					dataTableSpec.getColumnSpec(colorcolumn).getName());

			PaintScaleLegend psl = new PaintScaleLegend(scale, zAxis);
			psl.setPosition(RectangleEdge.LEFT);
			psl.setMargin(8, 4, 40, 4);
			psl.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
			legends.add(psl);
		}

		// Do we have a size legend? If so, it is always on the bottom
		if (sizeLegend) {
			// Sized range needed too...
			ShapeFactory.Shape legendShape = defaultShapeMdl.getShapeValue();
			String sizeColumnName =
					dataTableSpec.getColumnSpec(sizecolumn).getName();
			SizeHandler sizeHandler =
					dataTableSpec.getColumnSpec(sizecolumn).getSizeHandler();
			LegendItemSource sizeLegendItems = new SizeLegendItemSource(
					legendShape, itemSizeMdl.getDoubleValue(), sizeHandler,
					scatterPointMinSize, scatterPointMaxSize,
					sizeColumnName.length() > 30);
			Title legend = new TitledLegend(sizeColumnName,
					new LegendTitle(sizeLegendItems));
			legend.setPosition(RectangleEdge.BOTTOM);
			legend.setPadding(5D, 5D, 40D, 5D);
			legends.add(legend);
		}
		return legends;
	}
}
