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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.knime.core.data.BooleanValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.property.ColorHandler;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColor;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.filter.NameFilterConfiguration.FilterResult;
import org.knime.ext.jfc.node.base.JfcGenericBaseNodeModel;

import static com.vernalis.knime.plot.nodes.box.BoxplotNodeDialogPane.createCategoricalColumnNameModel;
import static com.vernalis.knime.plot.nodes.box.BoxplotNodeDialogPane.createDefaultFillColourModel;
import static com.vernalis.knime.plot.nodes.box.BoxplotNodeDialogPane.createDefaultOutlineColourModel;
import static com.vernalis.knime.plot.nodes.box.BoxplotNodeDialogPane.createEnsureOutliersModel;
import static com.vernalis.knime.plot.nodes.box.BoxplotNodeDialogPane.createFarOutlierSizeModel;
import static com.vernalis.knime.plot.nodes.box.BoxplotNodeDialogPane.createHorizontalBoxesModel;
import static com.vernalis.knime.plot.nodes.box.BoxplotNodeDialogPane.createLineWidthModel;
import static com.vernalis.knime.plot.nodes.box.BoxplotNodeDialogPane.createMaintainInputCategoryOrderModel;
import static com.vernalis.knime.plot.nodes.box.BoxplotNodeDialogPane.createMeanSizeModel;
import static com.vernalis.knime.plot.nodes.box.BoxplotNodeDialogPane.createNotchSizeModel;
import static com.vernalis.knime.plot.nodes.box.BoxplotNodeDialogPane.createOutlierSizeModel;
import static com.vernalis.knime.plot.nodes.box.BoxplotNodeDialogPane.createShowLegendModel;
import static com.vernalis.knime.plot.nodes.box.BoxplotNodeDialogPane.createShowMeanModel;
import static com.vernalis.knime.plot.nodes.box.BoxplotNodeDialogPane.createShowMedianModel;
import static com.vernalis.knime.plot.nodes.box.BoxplotNodeDialogPane.createShowNotchModel;
import static com.vernalis.knime.plot.nodes.box.BoxplotNodeDialogPane.createValueColumnFilterModel;
import static com.vernalis.knime.plot.nodes.box.BoxplotNodeDialogPane.createWhiskerWidthModel;

/**
 * Node Model implementation for the boxplot node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class BoxplotNodeModel extends JfcGenericBaseNodeModel {

	protected final SettingsModelString catColNameMdl =
			createCategoricalColumnNameModel();
	protected final SettingsModelBoolean preserveCatOrderMdl =
			createMaintainInputCategoryOrderModel();
	protected final SettingsModelColumnFilter2 seriesColsMdl =
			createValueColumnFilterModel();
	protected final SettingsModelBoolean showMeanMdl = createShowMeanModel();
	protected final SettingsModelBoolean showMedianMdl =
			createShowMedianModel();
	protected final SettingsModelBoolean inclLegendMdl =
			createShowLegendModel();
	protected final SettingsModelDoubleBounded lineWidthMdl =
			createLineWidthModel();
	protected final SettingsModelBoolean ensureExtremeOutliersMdl =
			createEnsureOutliersModel();
	protected final SettingsModelIntegerBounded meanSizeMdl =
			createMeanSizeModel();
	protected final SettingsModelIntegerBounded outlierSizeMdl =
			createOutlierSizeModel();
	protected final SettingsModelIntegerBounded farOutlierSizeMdl =
			createFarOutlierSizeModel();
	protected final SettingsModelColor defaultOutlineColourMdl =
			createDefaultOutlineColourModel();
	protected final SettingsModelColor defaultFillColourMdl =
			createDefaultFillColourModel();
	protected final SettingsModelDoubleBounded whiskerWidthMdl =
			createWhiskerWidthModel();
	protected final SettingsModelBoolean showNotchMdl = createShowNotchModel();
	protected final SettingsModelDoubleBounded notchSizeMdl =
			createNotchSizeModel();
	protected final SettingsModelBoolean horizontalBoxesMdl =
			createHorizontalBoxesModel();

	private int catColIdx;
	private List<String> seriesColumns;
	private ArrayList<Integer> seriesColumnIds;
	private ArrayList<DataCell> categoryCells;
	protected ColorHandler inPort0Colours;
	protected ColorHandler inPort1Colours;

	/**
	 * Default constructor - one input table and one optional table
	 */
	public BoxplotNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE,
				BufferedDataTable.TYPE_OPTIONAL });
		meanSizeMdl.setEnabled(showMeanMdl.getBooleanValue());
		showMeanMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				meanSizeMdl.setEnabled(showMeanMdl.getBooleanValue());

			}
		});
		notchSizeMdl.setEnabled(showNotchMdl.getBooleanValue());
		showNotchMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				notchSizeMdl.setEnabled(showNotchMdl.getBooleanValue());

			}
		});
	}

	@Override
	public void saveAdditionalSettingsTo(NodeSettingsWO settings) {
		catColNameMdl.saveSettingsTo(settings);
		preserveCatOrderMdl.saveSettingsTo(settings);
		seriesColsMdl.saveSettingsTo(settings);
		showMeanMdl.saveSettingsTo(settings);
		showMedianMdl.saveSettingsTo(settings);
		inclLegendMdl.saveSettingsTo(settings);
		lineWidthMdl.saveSettingsTo(settings);
		ensureExtremeOutliersMdl.saveSettingsTo(settings);
		meanSizeMdl.saveSettingsTo(settings);
		outlierSizeMdl.saveSettingsTo(settings);
		farOutlierSizeMdl.saveSettingsTo(settings);
		defaultFillColourMdl.saveSettingsTo(settings);
		defaultOutlineColourMdl.saveSettingsTo(settings);
		whiskerWidthMdl.saveSettingsTo(settings);
		showNotchMdl.saveSettingsTo(settings);
		notchSizeMdl.saveSettingsTo(settings);
		horizontalBoxesMdl.saveSettingsTo(settings);
	}

	@Override
	public void validateAdditionalSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		catColNameMdl.validateSettings(settings);
		preserveCatOrderMdl.validateSettings(settings);
		seriesColsMdl.validateSettings(settings);
		showMeanMdl.validateSettings(settings);
		showMedianMdl.validateSettings(settings);
		inclLegendMdl.validateSettings(settings);
		lineWidthMdl.validateSettings(settings);
		ensureExtremeOutliersMdl.validateSettings(settings);
		meanSizeMdl.validateSettings(settings);
		outlierSizeMdl.validateSettings(settings);
		farOutlierSizeMdl.validateSettings(settings);
		defaultFillColourMdl.validateSettings(settings);
		defaultOutlineColourMdl.validateSettings(settings);
		whiskerWidthMdl.validateSettings(settings);
		showNotchMdl.validateSettings(settings);
		notchSizeMdl.validateSettings(settings);
		horizontalBoxesMdl.validateSettings(settings);

	}

	@Override
	public void loadAdditionalValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {

		catColNameMdl.loadSettingsFrom(settings);
		preserveCatOrderMdl.loadSettingsFrom(settings);
		seriesColsMdl.loadSettingsFrom(settings);
		showMeanMdl.loadSettingsFrom(settings);
		showMedianMdl.loadSettingsFrom(settings);
		inclLegendMdl.loadSettingsFrom(settings);
		lineWidthMdl.loadSettingsFrom(settings);
		ensureExtremeOutliersMdl.loadSettingsFrom(settings);
		meanSizeMdl.loadSettingsFrom(settings);
		outlierSizeMdl.loadSettingsFrom(settings);
		farOutlierSizeMdl.loadSettingsFrom(settings);
		defaultFillColourMdl.loadSettingsFrom(settings);
		defaultOutlineColourMdl.loadSettingsFrom(settings);
		whiskerWidthMdl.loadSettingsFrom(settings);
		showNotchMdl.loadSettingsFrom(settings);
		notchSizeMdl.loadSettingsFrom(settings);
		horizontalBoxesMdl.loadSettingsFrom(settings);

	}

	@Override
	public JFreeChart createChart(PortObject[] inPos, ExecutionContext exec)
			throws Exception {

		// Create the dataset
		NotchedBoxAndWhiskerCategoryDataset dataset =
				createDataSet((BufferedDataTable) inPos[0], exec);

		// Handle the axes and check for outliers
		CategoryAxis catAxis = new CategoryAxis(catColNameMdl.getStringValue());
		NumberAxis valueAxis = new NumberAxis(
				seriesColumns.size() > 1 ? "Value" : seriesColumns.get(0));

		valueAxis.setAutoRangeIncludesZero(false);
		if (ensureExtremeOutliersMdl.getBooleanValue()) {
			double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
			for (int cat = 0; cat < dataset.getColumnCount(); cat++) {
				for (int ser = 0; ser < dataset.getRowCount(); ser++) {
					double maxOutlier =
							dataset.getMaxOutlier(ser, cat).doubleValue();
					double minOutlier =
							dataset.getMinOutlier(ser, cat).doubleValue();

					minY = Math.min(minY, minOutlier);
					maxY = Math.max(maxY, maxOutlier);

					if (dataset.getOutliers(ser, cat).size() > 0) {
						List<Double> outliers = dataset.getOutliers(ser, cat);
						minY = Math.min(minY,
								outliers.stream()
										.mapToDouble(x -> new Double(x)).min()
										.getAsDouble());
						maxY = Math.max(maxY,
								outliers.stream()
										.mapToDouble(x -> new Double(x)).max()
										.getAsDouble());
					}
				}
			}
			double overlap = 0.1 * (maxY - minY);
			valueAxis.setRange(minY - overlap, maxY + overlap);
		}
		// TODO: Scale label sizes - unfortunately the scale parameter is not
		// private in the superclass and we are not using ChartFactory which is
		// how this is propagated

		// Now check for outliers / far outliers
		boolean hasOutliers = false;
		boolean hasFarOutliers = false;
		for (int cat = 0; cat < dataset.getColumnCount(); cat++) {
			for (int ser = 0; ser < dataset.getRowCount(); ser++) {
				double maxOutlier =
						dataset.getMaxOutlier(ser, cat).doubleValue();
				double minOutlier =
						dataset.getMinOutlier(ser, cat).doubleValue();
				hasOutliers = hasOutliers
						|| maxOutlier > dataset.getMaxRegularValue(ser, cat)
								.doubleValue()
						|| minOutlier < dataset.getMinRegularValue(ser, cat)
								.doubleValue();

				if (dataset.getOutliers(ser, cat).size() > 0) {
					List<Double> outliers = dataset.getOutliers(ser, cat);
					hasFarOutliers = hasFarOutliers || outliers.stream()
							.anyMatch(x -> x > maxOutlier || x < minOutlier);
				}
			}
		}

		// Now sort out the renderer
		MulticolourNotchedBoxAndWhiskerRenderer renderer =
				new MulticolourNotchedBoxAndWhiskerRenderer(
						dataset.getColumnCount(), dataset.getRowCount(),
						defaultFillColourMdl.getColorValue(),
						defaultOutlineColourMdl.getColorValue(),
						outlierSizeMdl.getIntValue(),
						farOutlierSizeMdl.getIntValue(),
						meanSizeMdl.getIntValue());
		renderer.setMeanVisible(showMeanMdl.getBooleanValue());
		renderer.setMedianVisible(showMedianMdl.getBooleanValue());
		renderer.setWhiskerWidth(whiskerWidthMdl.getDoubleValue());
		renderer.setBaseOutlineStroke(
				new BasicStroke((float) lineWidthMdl.getDoubleValue()));
		renderer.setShowNotches(showNotchMdl.getBooleanValue());
		renderer.setNotchSize(notchSizeMdl.getDoubleValue());
		setRendererColours(inPos, renderer, dataset);

		// And generate the plot
		CategoryPlot plot =
				new CategoryPlot(dataset, catAxis, valueAxis, renderer);
		JFreeChart chart = new JFreeChart(plot);
		chart.removeLegend();
		plot.setOrientation(horizontalBoxesMdl.getBooleanValue()
				? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL);
		if (inclLegendMdl.getBooleanValue()) {
			addLegendToChart(chart, hasOutliers, hasFarOutliers);
		}

		// Set the font size and other 'theme' data
		ChartUtilities.applyCurrentTheme(chart);
		return chart;
	}

	/**
	 * Method to add the legend to the chart
	 * 
	 * @param chart
	 *            The chart
	 */
	protected void addLegendToChart(JFreeChart chart, boolean hasOutliers,
			boolean hasFarOutliers) {

		Map<String, Color> seriesColours =
				inPort1Colours == null ? null : new LinkedHashMap<>();
		if (seriesColours != null) {
			for (String series : seriesColumns) {
				seriesColours.put(series, inPort1Colours
						.getColorAttr(new StringCell(series)).getColor());
			}
		}
		Map<String, Color> catColours =
				inPort0Colours == null ? null : new LinkedHashMap<>();
		if (catColours != null) {
			for (DataCell cat : categoryCells) {
				catColours.put(cat.toString(),
						inPort0Colours.getColorAttr(cat).getColor());
			}
		}

		// Finally, if we have no colurs, then we need at least the series names

		LegendTitle legend = new LegendTitle(new BoxplotLegendItemSource(
				seriesColumns, seriesColours, catColours,
				showMeanMdl.getBooleanValue(), hasOutliers, hasFarOutliers,
				defaultFillColourMdl.getColorValue(),
				defaultOutlineColourMdl.getColorValue(),
				new BasicStroke((float) lineWidthMdl.getDoubleValue())));

		// position of the paint legend on the final image
		legend.setPosition(RectangleEdge.RIGHT);
		// set the padding around the legend
		legend.setPadding(5D, 5D, 40D, 5D);
		legend.setBackgroundPaint(chart.getBackgroundPaint());
		// change listener for the legend background color
		chart.addChangeListener(new ChartChangeListener() {

			@Override
			public void chartChanged(final ChartChangeEvent event) {
				if (event.getSource() instanceof JFreeChart) {
					JFreeChart c = (JFreeChart) event.getSource();
					LegendTitle l = (LegendTitle) c.getSubtitle(0);
					l.setBackgroundPaint(c.getBackgroundPaint());
				}
			}
		});
		chart.addSubtitle(legend);
	}

	/**
	 * Method to apply the possible colour settings from the input table(s)
	 * <p>
	 * If the 2nd Port is null, then the colour is as follows.
	 * <ul>
	 * <li>The outline and whisker are black</li>
	 * <li>If there is a ColorModel on the 1st port for the category column,
	 * then the fill is from the colour model</li>
	 * <li>If no colormodel, or it is not on the category column, then there is
	 * no fill</li>
	 * </ul>
	 * 
	 * </p>
	 * <p>
	 * If the 2nd port is not null, then it must contain a single string column
	 * with a colour model associated to it. The column must contain the names
	 * of the cells. Now, the colouring is as follows:
	 * 
	 * <p>
	 * <i>Outline &amp; Whisker</i>
	 * <ul>
	 * <li>If there is a ColorModel on the 1st port for the category column,
	 * then the outline is from the colour model</li>
	 * <li>If no colormodel, or it is not on the category column, then the
	 * outline is black</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <i>Fill</i>
	 * <ul>
	 * <li>If there is a single StringCell column in the second table, then the
	 * series fill colours come from the color model</li>
	 * <li>if not, of for series names not represented, the fill colour is white
	 * </li>
	 * </ul>
	 * </p>
	 * 
	 * @param inPos
	 *            The incoming ports
	 * @param renderer
	 *            The renderer
	 * @param dataset
	 *            The dataset
	 */
	protected void setRendererColours(PortObject[] inPos,
			MulticolourNotchedBoxAndWhiskerRenderer renderer,
			BoxAndWhiskerCategoryDataset dataset) {

		renderer.setUseOutlinePaintForWhiskers(true);
		inPort0Colours = null;
		inPort1Colours = null;
		if (inPos.length > 1 && inPos[1] == null) {
			// No second port - will be colour model if available
			inPort0Colours = ((BufferedDataTable) inPos[0]).getDataTableSpec()
					.getColumnSpec(catColIdx).getColorHandler();
			if (inPort0Colours != null) {
				// There is a colour model - otherwise the defaults will be used
				for (int colIdx = 0; colIdx < dataset
						.getColumnCount(); colIdx++) {
					renderer.setCategoryFillPaint(colIdx,
							inPort0Colours
									.getColorAttr(categoryCells.get(colIdx))
									.getColor());
				}
			}
		} else {
			// There is a second port. It must have a single StringCell column

			inPort0Colours = ((BufferedDataTable) inPos[0]).getDataTableSpec()
					.getColumnSpec(catColIdx).getColorHandler();
			if (inPort0Colours != null) {
				// There is a colour model - otherwise the defaults will be used
				for (int colIdx = 0; colIdx < dataset
						.getColumnCount(); colIdx++) {
					renderer.setCategoryPaint(colIdx,
							inPort0Colours
									.getColorAttr(categoryCells.get(colIdx))
									.getColor());
				}
			}

			// Now handle the fill colour
			DataTableSpec spec1 =
					((BufferedDataTable) inPos[1]).getDataTableSpec();
			if (spec1.getNumColumns() == 1 && spec1.getColumnSpec(0).getType()
					.isCompatible(StringValue.class)) {
				inPort1Colours = spec1.getColumnSpec(0).getColorHandler();
				if (inPort1Colours != null) {
					// There is a colour model - otherwise the defaults will be
					// used
					for (int seriesIdx = 0; seriesIdx < dataset
							.getRowCount(); seriesIdx++) {
						renderer.setSeriesFillPaint(seriesIdx,
								inPort1Colours
										.getColorAttr(new StringCell(
												seriesColumns.get(seriesIdx)))
										.getColor());
					}
				}
			}
		}
	}

	/**
	 * Method to create the box and whisker dataset from the input table. Takes
	 * 95% of execution time
	 * 
	 * @param dataTable
	 *            The incoming data table
	 * @param exec
	 *            The execution context
	 * @return The dataset for plotting
	 * @throws CanceledExecutionException
	 *             if the user cancels during execution
	 */
	protected NotchedBoxAndWhiskerCategoryDataset createDataSet(
			BufferedDataTable dataTable, ExecutionContext exec)
			throws CanceledExecutionException {
		NotchedBoxAndWhiskerCategoryDataset retVal =
				new NotchedBoxAndWhiskerCategoryDataset();
		// temporary data storage - could get big!
		// Outer key is category name
		// Inner key is series name
		// List is the data for the category series
		Map<String, Map<String, List<Double>>> data =
				preserveCatOrderMdl.getBooleanValue() ? new LinkedHashMap<>()
						: new TreeMap<>();
		categoryCells = new ArrayList<>();
		ExecutionContext exec0 = exec.createSubExecutionContext(0.95);
		exec.setMessage("Preparing data");

		// First we have to extract all the series and categories
		long numRows = dataTable.size();
		long rowCnt = 0;
		for (DataRow row : dataTable) {
			exec0.checkCanceled();
			if (rowCnt++ % 20 == 0) {
				exec0.setProgress((double) rowCnt / (double) numRows,
						"Checked " + rowCnt + " of " + numRows + " rows. Found "
								+ data.size() + " categories");
			}

			// Deal with the category name
			DataCell catCell = row.getCell(catColIdx);
			if (catCell.isMissing()) {
				continue;
			}
			String catName;
			if (catCell.getType().isCompatible(BooleanValue.class)) {
				catName = ((BooleanValue) catCell).getBooleanValue() ? "true"
						: "false";
			} else if (catCell.getType().isCompatible(IntValue.class)) {
				catName = ((IntValue) catCell).getIntValue() + "";
			} else {
				catName = ((StringValue) catCell).getStringValue();
			}
			if (!data.containsKey(catName)) {
				categoryCells.add(catCell);
				LinkedHashMap<String, List<Double>> catData =
						new LinkedHashMap<>();
				for (String seriesName : seriesColumns) {
					catData.put(seriesName, new ArrayList<>());
				}
				data.put(catName, catData);
			}
			Map<String, List<Double>> catData = data.get(catName);
			for (int i = 0; i < seriesColumns.size(); i++) {
				DataCell valCell = row.getCell(seriesColumnIds.get(i));
				if (!valCell.isMissing()) {
					catData.get(seriesColumns.get(i))
							.add(((DoubleValue) valCell).getDoubleValue());
				}
			}
		}

		// Now we have to put them all into the data structure...
		for (Entry<String, Map<String, List<Double>>> cat : data.entrySet()) {
			for (Entry<String, List<Double>> catData : cat.getValue()
					.entrySet()) {
				retVal.add(catData.getValue(), catData.getKey(), cat.getKey());
			}
		}

		// Finally, we may need to sort the category cells
		if (!preserveCatOrderMdl.getBooleanValue()) {
			Collections.sort((List<DataCell>) categoryCells,
					new Comparator<DataCell>() {

						@Override
						public int compare(DataCell o1, DataCell o2) {
							return o1.getType().getComparator().compare(o1, o2);
						}
					});
		}
		return retVal;
	}

	@Override
	public void doReset() {

	}

	@Override
	public void doConfigure(DataTableSpec inSpec)
			throws InvalidSettingsException {
		List<String> allCategoricals = new LinkedList<>();
		List<String> allSeries = new LinkedList<>();
		for (DataColumnSpec colspec : inSpec) {
			if (BoxplotNodeDialogPane.categoricalColumnFilter
					.includeColumn(colspec)) {
				allCategoricals.add(colspec.getName());
			} else if (colspec.getType() == DoubleCell.TYPE) {
				allSeries.add(colspec.getName());
			}
		}
		if (allCategoricals.size() < 1 || allSeries.size() < 1) {
			throw new InvalidSettingsException(
					"At least one category column and 1 series column required");
		}

		// Guess what to use if nothing selected
		String svm = "";
		if (catColNameMdl.getStringValue() == null
				|| catColNameMdl.getStringValue().isEmpty()) {
			svm += "No Category column selected - autoguessing '"
					+ allCategoricals.get(allCategoricals.size() - 1) + "'\n";
			catColNameMdl.setStringValue(
					allCategoricals.get(allCategoricals.size() - 1));
		}

		catColIdx = inSpec.findColumnIndex(catColNameMdl.getStringValue());

		// Now check if the columns exist and use something else instead
		if (catColIdx < 0) {
			svm += "Column " + catColNameMdl.getStringValue()
					+ " not found in input name. " + " Column "
					+ (allCategoricals.get(allCategoricals.size() - 1))
					+ " used instead.\n";
			catColIdx = inSpec.findColumnIndex(
					allCategoricals.get(allCategoricals.size() - 1));
			catColNameMdl.setStringValue(
					allCategoricals.get(allCategoricals.size() - 1));
		}

		seriesColumns = getSeriesColumnsList(inSpec);
		seriesColumnIds = new ArrayList<>(seriesColumns.size());
		for (String seriesCol : seriesColumns) {
			seriesColumnIds.add(inSpec.findColumnIndex(seriesCol));
		}

		if (seriesColumns.isEmpty()) {
			svm += "No series columns selected";
			throw new InvalidSettingsException(svm);
		}
		if (!svm.isEmpty()) {
			setWarningMessage(svm);
		}
	}

	/**
	 * Method to find the names of the series columns as a list
	 * 
	 * @param mySpec
	 *            The incoming table spec
	 * @return A list of the series column names
	 */
	protected List<String> getSeriesColumnsList(final DataTableSpec mySpec) {

		FilterResult filtRes = seriesColsMdl.applyTo(mySpec);
		if (filtRes.getIncludes().length == 0
				&& filtRes.getExcludes().length == 0) {
			// node is not configured we use all appropriate columns
			seriesColsMdl.loadDefaults(mySpec);
			filtRes = seriesColsMdl.applyTo(mySpec);
		}

		List<String> yInclude = new ArrayList<>();
		for (String colName : filtRes.getIncludes()) {
			if (mySpec.getColumnSpec(colName).getType()
					.isCompatible(DoubleValue.class)) {
				// This can be not the case if a previously selected column has
				// changed
				// type
				yInclude.add(colName);
			} else {
				getLogger().info(
						"Ignored column '" + colName + "' - not correct type");
			}
		}

		return yInclude;
	}

}
