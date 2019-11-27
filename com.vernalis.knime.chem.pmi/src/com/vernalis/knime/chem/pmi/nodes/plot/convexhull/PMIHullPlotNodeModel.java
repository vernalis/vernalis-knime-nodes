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

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.LegendItemSource;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.Title;
import org.jfree.ui.RectangleEdge;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.property.ColorAttr;
import org.knime.core.data.property.ColorHandler.ColorModel;
import org.knime.core.data.property.ColorModelNominal;
import org.knime.core.data.property.ShapeFactory;
import org.knime.core.data.property.ShapeHandler;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesPlotNodeModel;
import com.vernalis.knime.jfcplot.core.legend.TitledLegend;

import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.DOUBLE_VALUE_COLUMN_FILTER;
import static com.vernalis.knime.chem.pmi.nodes.plot.convexhull.PMIHullPlotNodeDialogPane.createAreaThresholdModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.convexhull.PMIHullPlotNodeDialogPane.createGroupColumnModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.convexhull.PMIHullPlotNodeDialogPane.createHullAlphaValueModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.convexhull.PMIHullPlotNodeDialogPane.createHullNpr1ColumnModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.convexhull.PMIHullPlotNodeDialogPane.createHullNpr2ColumnModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.convexhull.PMIHullPlotNodeDialogPane.createShowHullBoundriesModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.convexhull.PMIHullPlotNodeDialogPane.createShowRelativeAreaModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.convexhull.PMIHullPlotNodeDialogPane.createUseDefaultGreyscale;
import static com.vernalis.knime.chem.pmi.nodes.plot.convexhull.PMIHullPlotNodeDialogPane.getShowLegendModel;

/**
 * Node Model implementation for the PMI Convex Hull plot
 * 
 */
public class PMIHullPlotNodeModel
		extends AbstractPMIDrawableSeriesPlotNodeModel<ConvexHull> {

	/** column indices that specify which columns are used. */
	private int groupColIdx = -1;
	private int hullNpr1ColIdx = -1;
	private int hullNpr2ColIdx = -1;

	private final SettingsModelString hullNpr1ColNameMdl =
			registerSettingsModel(createHullNpr1ColumnModel());
	private final SettingsModelString hullNpr2ColNameMdl =
			registerSettingsModel(createHullNpr2ColumnModel());

	private final SettingsModelString m_groupColumnName =
			registerSettingsModel(createGroupColumnModel());
	private final SettingsModelBoolean m_showLegendModel =
			registerSettingsModel(getShowLegendModel());
	private final SettingsModelIntegerBounded m_hullAlpha =
			registerSettingsModel(createHullAlphaValueModel());

	private SettingsModelBoolean m_showBoundries =
			registerSettingsModel(createShowHullBoundriesModel());

	private final SettingsModelBoolean m_showRelAreaOnLegend =
			registerSettingsModel(createShowRelativeAreaModel());
	private final SettingsModelBoolean m_useGreyScale =
			registerSettingsModel(createUseDefaultGreyscale());
	private final SettingsModelDoubleBounded m_areaForPointsModel =
			registerSettingsModel(createAreaThresholdModel());

	/**
	 * Constructor - 2 ports, the first is the data for hulling, the second
	 * optionally a scatter overlay
	 */
	public PMIHullPlotNodeModel() {
		super(2);
		m_groupColumnName.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				m_showLegendModel
						.setEnabled(smr.isStringModelFilled(m_groupColumnName));

			}
		});
		m_showLegendModel.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				m_showRelAreaOnLegend.setEnabled(m_showLegendModel.isEnabled()
						&& m_showLegendModel.getBooleanValue());
			}
		});
		m_showLegendModel
				.setEnabled(smr.isStringModelFilled(m_groupColumnName));
		m_showRelAreaOnLegend.setEnabled(m_showLegendModel.isEnabled()
				&& m_showLegendModel.getBooleanValue());
	}

	/**
	 * Create default color mapping for the given set of possible
	 * <code>DataCell</code> values.
	 * 
	 * @param set
	 *            possible values
	 * @return a map of possible value to color
	 */
	final ColorModelNominal createColorMapping(final Set<DataCell> set) {
		if (set == null) {
			return new ColorModelNominal(Collections.emptyMap());
		}
		Map<DataCell, ColorAttr> map = new LinkedHashMap<>();
		int idx = 0;
		for (DataCell cell : set) {
			// use Color, half saturated, half bright for base color
			float value = (float) idx++ / (float) set.size();
			Color color = m_useGreyScale.getBooleanValue()
					? new Color(value, value, value)
					: new Color(Color.HSBtoRGB(value, 1.0f, 1.0f));
			map.put(cell, ColorAttr.getInstance(color));
		}

		return new ColorModelNominal(map);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.chem.pmi.nodes.plot.scatter.
	 * AbstractPMIDrawableSeriesPlotNodeModel#doConfigure(org.knime.core.data.
	 * DataTableSpec, int)
	 */
	@Override
	protected void doConfigure(DataTableSpec dataTableSpec, int portId)
			throws InvalidSettingsException {
		switch (portId) {
		case 0:
			// The hull npr1/npr2 columns
			if (dataTableSpec.stream()
					.filter(colSpec -> DOUBLE_VALUE_COLUMN_FILTER
							.includeColumn(colSpec))
					.count() < 2) {
				throw new InvalidSettingsException(
						"At least 2 double columns are required!");
			}

			// Now validate the selections, guessing if needed
			hullNpr1ColIdx = getValidatedColumnSelectionModelColumnIndex(
					hullNpr1ColNameMdl, DOUBLE_VALUE_COLUMN_FILTER,
					dataTableSpec, NPR1_PATTERN, false, getLogger(), false,
					hullNpr2ColNameMdl);
			hullNpr2ColIdx = getValidatedColumnSelectionModelColumnIndex(
					hullNpr2ColNameMdl, DOUBLE_VALUE_COLUMN_FILTER,
					dataTableSpec, NPR2_PATTERN, false, getLogger(), false,
					hullNpr1ColNameMdl);

			// Finally, check the column bounds, if they exist, are within the
			// appropriate range
			if (hullNpr1ColIdx >= 0
					&& dataTableSpec.getColumnSpec(hullNpr1ColIdx).getDomain()
							.hasBounds()
					&& ((((DoubleValue) dataTableSpec
							.getColumnSpec(hullNpr1ColIdx).getDomain()
							.getLowerBound()).getDoubleValue() < 0.0)
							|| ((DoubleValue) dataTableSpec
									.getColumnSpec(hullNpr1ColIdx).getDomain()
									.getUpperBound()).getDoubleValue() > 1.0)) {
				throw new InvalidSettingsException(
						"Hull nPMI1 column domain is outside the bounds 0.0 - 1.0");
			}

			if (hullNpr2ColIdx >= 0
					&& dataTableSpec.getColumnSpec(hullNpr2ColIdx).getDomain()
							.hasBounds()
					&& ((((DoubleValue) dataTableSpec
							.getColumnSpec(hullNpr2ColIdx).getDomain()
							.getLowerBound()).getDoubleValue() < 0.5)
							|| ((DoubleValue) dataTableSpec
									.getColumnSpec(hullNpr2ColIdx).getDomain()
									.getUpperBound()).getDoubleValue() > 1.0)) {
				throw new InvalidSettingsException(
						"Hull nPMI2 column domain is outside the bounds 0.5 - 1.0");
			}

			// Now the optional grouping column
			if (m_groupColumnName.getStringValue() != null
					&& !"".equals(m_groupColumnName.getStringValue())) {

				groupColIdx = dataTableSpec
						.findColumnIndex(m_groupColumnName.getStringValue());
			} else {
				// All are in single group - no column was selected
				groupColIdx = -1;
			}

			String svm = getWarningMessage();
			if (svm == null) {
				svm = "";
			}
			if (groupColIdx < 0) {
				svm += "No group column selected; All points will be included in a single hull";
			}

			if (!svm.isEmpty()) {
				setWarningMessage(svm);
			}
			break;
		default:
			break;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.chem.pmi.nodes.plot.scatter.
	 * AbstractPMIDrawableSeriesPlotNodeModel#createLegendsFromInputPort(org.
	 * knime.core.data.DataTableSpec, java.util.Collection, int, int, int, int,
	 * org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected Collection<? extends Title> createLegendsFromInputPort(
			DataTableSpec dataTableSpec,
			Collection<ConvexHull> dataSeriesFromInputPort, int portId,
			int colorcolumn, int shapecolumn, int sizecolumn,
			ExecutionContext exec) throws CanceledExecutionException {
		switch (portId) {
		case 0:
			if (!m_showLegendModel.isEnabled()
					|| !m_showLegendModel.getBooleanValue()) {
				return Collections.emptySet();
			}

			final String groupColName =
					dataTableSpec.getColumnSpec(groupColIdx).getName();
			LegendItemSource legendItems =
					new HullLegendItemSource(dataSeriesFromInputPort,
							m_showRelAreaOnLegend.getBooleanValue());

			Title legend;
			if (!(sizeLegendMdl.getBooleanValue() && sizecolumn > -1)
					&& legendItems.getLegendItems().getItemCount() < 3) {
				legend = new TitledLegend(groupColName,
						new LegendTitle(legendItems));
				legend.setPosition(RectangleEdge.BOTTOM);
			} else {
				legend = new TitledLegend(groupColName, legendItems);
				legend.setPosition(RectangleEdge.RIGHT);
			}
			// set the padding around the legend
			legend.setPadding(5D, 5D, 40D, 5D);
			return Collections.singleton(legend);

		default:
			assert false;// Shouldnt get here
			return Collections.emptySet();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.chem.pmi.nodes.plot.scatter.
	 * AbstractPMIDrawableSeriesPlotNodeModel#createDataSeriesFromInputPort(org.
	 * knime.core.node.BufferedDataTable, int, int, int, int,
	 * org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected void addDataSeriesFromInputPort(BufferedDataTable dTable,
			int portId, int colorcolumn, int shapecolumn, int sizecolumn,
			ChartData<ConvexHull> chartData, ExecutionContext exec)
			throws CanceledExecutionException {
		switch (portId) {
		case 0:

			// If no shape handler is provided on the grouping column we use the
			// shape from the dialog
			ShapeFactory.Shape defaultShape = defaultShapeMdl.getShapeValue();

			DataTableSpec tableSpec = dTable.getDataTableSpec();
			ShapeHandler shapeHandler = shapecolumn < 0 ? null
					: tableSpec.getColumnSpec(shapecolumn).getShapeHandler();

			// Check there is no shape handler - in which case we use the
			// default
			boolean useDefaultShape = shapeHandler == null;

			// If the grouping column doesnt provide a color model, then we make
			// one
			// up...
			ColorModel colorModel =
					colorcolumn < 0 || colorcolumn != groupColIdx ? null
							: tableSpec.getColumnSpec(colorcolumn)
									.getColorHandler().getColorModel();

			if (!(colorModel instanceof ColorModelNominal)) {
				Set<DataCell> values = null;
				try {
					values = tableSpec
							.getColumnSpec(m_groupColumnName.getStringValue())
							.getDomain().getValues();
				} catch (Exception e) {

				}
				if (values == null && groupColIdx >= 0) {
					exec.setMessage("Scanning table for group IDs...");
					ExecutionContext exec0 =
							exec.createSubExecutionContext(0.5);
					double progresPerRow = 1.0 / dTable.size();
					long rowidx = 0L;
					values = new LinkedHashSet<>();
					for (DataRow row : dTable) {
						values.add(row.getCell(groupColIdx));
						exec0.checkCanceled();
						exec0.setProgress((++rowidx) * progresPerRow,
								"Read " + rowidx + " rows of " + dTable.size());
					}
				}
				colorModel = createColorMapping(values);
			}

			Map<String, ConvexHull> retVal = new LinkedHashMap<>();
			exec.setMessage("Reading data table...");
			ExecutionContext exec0 = exec.createSubExecutionContext(0.9);
			double progresPerRow = 1.0 / dTable.size();
			long rowidx = 0L;
			boolean hasWarning = false;
			for (DataRow row : dTable) {
				DataCell xVal = row.getCell(hullNpr1ColIdx);
				DataCell yVal = row.getCell(hullNpr2ColIdx);
				DataCell groupCell =
						groupColIdx < 0 ? null : row.getCell(groupColIdx);
				if (xVal.isMissing() || yVal.isMissing()
						|| (groupColIdx >= 0 && groupCell.isMissing())) {
					if (!hasWarning) {
						setWarningMessage(
								"Missing values are ignored in the view!");
						hasWarning = true;
					}
				} else {
					String ID = groupColIdx < 0 ? "Series 1"
							: ((StringValue) groupCell).getStringValue();
					Color rowColor =
							colorModel.getColorAttr(groupCell).getColor();
					ShapeFactory.Shape rowShape = useDefaultShape ? defaultShape
							: shapeHandler.getShape(groupCell);
					// Now retrieve series for the current row...
					if (!retVal.containsKey(ID)) {
						retVal.put(ID,
								new ConvexHull(ID, rowShape, rowColor,
										m_hullAlpha.getIntValue(),
										m_showBoundries.getBooleanValue(),
										itemSizeMdl.getDoubleValue(),
										m_areaForPointsModel.getDoubleValue()));
					}
					ConvexHull currentSeries = retVal.get(ID);
					currentSeries.addPoint(
							((DoubleValue) xVal).getDoubleValue(),
							((DoubleValue) yVal).getDoubleValue());
				}
				exec0.checkCanceled();
				exec0.setProgress((++rowidx) * progresPerRow,
						"Read " + rowidx + " rows of " + dTable.size());
			}
			exec.setProgress("Generating hulls...");
			retVal.values().forEach(x -> x.generateHull());
			chartData.addData(retVal.values());
			return;
		default:
			assert false; // Shouldn't get here!
			// do nothing!
			return;
		}
	}

}
