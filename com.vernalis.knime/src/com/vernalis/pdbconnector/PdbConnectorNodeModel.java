/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2012, Vernalis (R&D) Ltd and Enspiral Discovery Limited
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ------------------------------------------------------------------------
 */
package com.vernalis.pdbconnector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.pdbconnector.config.PdbConnectorConfig;
import com.vernalis.pdbconnector.config.Properties;
import com.vernalis.pdbconnector.config.QueryCategory;
import com.vernalis.pdbconnector.config.QueryOption;
import com.vernalis.pdbconnector.config.ReportCategory;
import com.vernalis.pdbconnector.config.ReportField;
import com.vernalis.pdbconnector.config.StandardCategory;
import com.vernalis.pdbconnector.config.StandardReport;
import com.vernalis.pdbconnector.config.Values;

/**
 * PdbConnectorNode model class.
 */
public class PdbConnectorNodeModel extends NodeModel {
	/**
	 * Maximum URL length supported by PDB custom report web service.
	 *
	 * True limit is 8192, but allow some headroom for integer division rounding
	 * errors.
	 */
	public static final int MAX_URL_LENGTH = 8000;
	static final NodeLogger logger = NodeLogger
			.getLogger(PdbConnectorNodeModel.class);
	static final String[] PDB_COLUMNS = { Properties.PDB_COLUMN_NAME };
	static final String STD_REPORT_KEY = "STANDARD_REPORT";
	static final String LIGAND_IMG_SIZE_KEY = "LIGAND_IMAGE_SIZE";
	static final String CONJUNCTION_KEY = "CONJUNCTION";

	private final List<QueryOptionModel> m_queryModels = new ArrayList<QueryOptionModel>();
	private final List<ReportFieldModel> m_reportModels = new ArrayList<ReportFieldModel>();
	private final List<ReportFieldModel> m_hiddenReportModels = new ArrayList<ReportFieldModel>();
	private final List<ReportField> m_selectedFields = new ArrayList<ReportField>();
	private ReportField m_primaryCitationSuffix = null;
	private QueryOptionModel m_simModel = null;
	private SettingsModelString m_ligandImgSize = null;
	private SettingsModelString m_conjunction = null;
	private String m_lastError = "";
	private List<StandardCategory> m_stdCategories;
	private StandardReport m_stdReport = null;
	private int m_numNonHidden = 0;// number of (non-hidden) selected fields
	private Values m_ligandImgOptions = null;// ligand image size options

	/**
	 * Instantiates a new pdb connector node model.
	 *
	 * @param config
	 *            the configuration
	 */
	protected PdbConnectorNodeModel(final PdbConnectorConfig config) {
		super(0, 2);
		if (!config.isOK()) {
			m_lastError = config.getLastErrorMessage();
			logger.fatal("Error loading query and report definitions from PdbConnectorConfig.xml/.dtd");
			logger.fatal("Last Error: " + m_lastError);
		} else {
			m_simModel = new QueryOptionModel(config.getSimilarity());
			createQueryModels(config);
			createReportModels(config);
			m_ligandImgOptions = config.getLigandImgOptions();
			m_ligandImgSize = new SettingsModelString(LIGAND_IMG_SIZE_KEY,
					m_ligandImgOptions.getDefaultLabel());
			m_conjunction = new SettingsModelString(CONJUNCTION_KEY,
					Properties.CONJUNCTION_AND_LABEL);
			m_stdCategories = config.getStandardCategories();
			m_stdReport = config.getDefaultStandardReport();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.knime.core.node.NodeModel#execute(org.knime.core.node.BufferedDataTable
	 * [], org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		logger.debug("PdbConnectorNode executing...");

		// Divide progress into two subprogress monitors, one for each outport
		// table.

		// OUTPORT 0 (PDB IDs) (nominal 30% of total progress)
		ExecutionMonitor exec0 = exec.createSubProgress(0.3);
		DataTableSpec outputSpec0 = createOutputTableSpec0();
		// the execution context will provide us with storage capacity, in this
		// case a data container to which we will add rows sequentially
		// Note, this container can also handle arbitrary big data tables, it
		// will buffer to disc if necessary.
		BufferedDataContainer container0 = exec
				.createDataContainer(outputSpec0);

		// select the appropriate conjunction string (either AND or OR)
		String conjunction = m_conjunction.getStringValue().equals(
				Properties.CONJUNCTION_AND_LABEL) ? Properties.CONJUNCTION_AND
				: Properties.CONJUNCTION_OR;
		// run the query
		String xmlQuery = ModelHelperFunctions.getXmlQuery(m_queryModels,
				m_simModel, conjunction);
		logger.info("getXmlQuery=" + xmlQuery);
		exec0.setProgress(0.0, "Posting xmlQuery to "
				+ Properties.SEARCH_LOCATION);
		List<String> pdbIds = ModelHelperFunctions.postQuery(xmlQuery);
		exec0.setProgress(1.0, "xmlQuery returned " + pdbIds.size() + " rows");
		exec0.checkCanceled();
		// create outport0 table
		int row = 0;
		for (String pdbId : pdbIds) {
			RowKey key = RowKey.createRowKey(row);
			// the cells of the current row, the types of the cells must match
			// the column spec (see above)
			DataCell[] cells = new DataCell[outputSpec0.getNumColumns()];
			cells[0] = new StringCell(pdbId);
			container0.addRowToTable(new DefaultRow(key, cells));
			++row;
		}
		// once we are done, we close the container and return its table
		container0.close();
		BufferedDataTable out0 = container0.getTable();

		// OUTPORT 1 (report columns) (nominal 70% of total progress)
		ExecutionMonitor exec1 = exec.createSubProgress(0.7);
		DataTableSpec outputSpec1 = createOutputTableSpec1();
		// the execution context will provide us with storage capacity, in this
		// case a data container to which we will add rows sequentially
		// Note, this container can also handle arbitrary big data tables, it
		// will buffer to disc if necessary.
		BufferedDataContainer container1 = exec
				.createDataContainer(outputSpec1);
		// generate the report
		Queue<String> toProcess = new LinkedList<String>(pdbIds);
		// determine how many PDB IDs we can process at once without exceeding
		// MAX_URL_LENGTH
		// Each PDB ID requires 5 characters, but have to allow for length of
		// selected column field strings.
		final int CHUNK_SIZE = (MAX_URL_LENGTH
				- Properties.REPORT_LOCATION.length() - ModelHelperFunctions
				.getReportColumnsUrl(m_selectedFields).length()) / 5;
		logger.debug("CHUNK_SIZE=" + CHUNK_SIZE);
		// retrieve the url suffix for the currently selected ligand image size
		final String imgSize = m_ligandImgSize.getStringValue();
		final String urlSuffix = m_ligandImgOptions.isExists(imgSize) ? m_ligandImgOptions
				.getValue(imgSize) : imgSize;
		row = 0;
		int block = 0;
		while (!toProcess.isEmpty() && (CHUNK_SIZE > 0)) {
			double progress = 1.0 - (double) toProcess.size()
					/ (double) pdbIds.size();
			exec1.setProgress(
					progress,
					"Getting custom report (" + toProcess.size() + "/"
							+ pdbIds.size() + " PDB IDs remaining)");
			List<String> nextChunk = new ArrayList<String>();
			while (!toProcess.isEmpty() && (nextChunk.size() < CHUNK_SIZE)) {
				nextChunk.add(toProcess.remove());
			}

			// Increasing delays in seconds - we start with 0 in case the url
			// timeout is a sufficient delay
			int[] retryDelays = { 0, 1, 5, 10, 30, 60, 300, 600 };
			block++;
			List<List<String>> report = null;
			for (int delay : retryDelays) {
				try {
					report = ModelHelperFunctions.getCustomReportXml2(
							nextChunk, m_selectedFields,
							m_primaryCitationSuffix);
					break;
				} catch (IOException e) {
					// Wait for an increasing period before retrying
					logger.warn("GET request failed for data block " + block
							+ " - Waiting " + delay
							+ " seconds before re-trying...", e);
					exec1.checkCanceled();
					pause(delay, exec1);
				}
			}
			if (report == null) {
				// In this case, we never managed to contact the server...
				String errMsg = "Unable to contact the remote server - please try again later!";
				logger.error(errMsg);
				throw new IOException(errMsg);
			}
			// List<List<String>> report =
			// ModelHelperFunctions.getCustomReportXml(nextChunk,
			// m_selectedFields);
			// List<List<String>> report =
			// ModelHelperFunctions.getCustomReportCsv(nextChunk,
			// m_selectedFields);
			exec1.checkCanceled();
			for (List<String> recordVals : report) {
				RowKey key = RowKey.createRowKey(row);
				if (recordVals.size() != outputSpec1.getNumColumns()) {
					logger.warn("Invalid record " + recordVals);
					logger.warn("Number of fields expected="
							+ outputSpec1.getNumColumns() + "; actual="
							+ recordVals.size());
				} else {
					// the cells of the current row, the types of the cells must
					// match
					// the column spec (see above)
					DataCell[] cells = new DataCell[outputSpec1.getNumColumns()];
					for (int i = 0; i < outputSpec1.getNumColumns(); ++i) {
						ReportField field = m_selectedFields.get(i);
						String fieldValue = recordVals.get(i);
						cells[i] = ModelHelperFunctions.getDataCell(field,
								fieldValue, urlSuffix);
					}
					container1.addRowToTable(new DefaultRow(key, cells));
				}
				++row;
			}
		}
		container1.close();
		BufferedDataTable out1 = container1.getTable();
		exec1.setProgress(1.0, "OUTPORT1 complete");
		exec.setProgress(1.0, "Done");
		return new BufferedDataTable[] { out0, out1 };
	}

	private static void pause(final int seconds, final ExecutionMonitor exec1)
			throws Exception {
		// simple delay function without using threads
		Date start = new Date();
		Date end = new Date();
		while (end.getTime() - start.getTime() < seconds * 1000) {
			end = new Date();
			exec1.checkCanceled();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.knime.core.node.NodeModel#reset()
	 */
	@Override
	protected void reset() {
		// TODO Code executed on reset.
		// Models build during execute are cleared here.
		// Also data handled in load/saveInternals will be erased here.
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.knime.core.node.NodeModel#configure(org.knime.core.data.DataTableSpec
	 * [])
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		if (!m_lastError.isEmpty()) {
			throw new InvalidSettingsException(
					"Error loading query and report definitions from PdbConnectorConfig.xml/.dtd"
							+ " (" + m_lastError + ")");
		}
		DataTableSpec outputSpec0 = createOutputTableSpec0();
		DataTableSpec outputSpec1 = createOutputTableSpec1();
		// Check at least one (non-hidden) field is selected
		if (m_numNonHidden < 1) {
			throw new InvalidSettingsException("No report fields selected");
		}
		// determine how many PDB IDs we can process at once without exceeding
		// MAX_URL_LENGTH
		// Each PDB ID requires 5 characters, but have to allow for length of
		// selected column field strings.
		final int CHUNK_SIZE = (MAX_URL_LENGTH
				- Properties.REPORT_LOCATION.length() - ModelHelperFunctions
				.getReportColumnsUrl(m_selectedFields).length()) / 5;
		if (CHUNK_SIZE < 1) {
			throw new InvalidSettingsException(
					"Too many report fields selected: MAX_URL_LENGTH exceeded");
		}
		return new DataTableSpec[] { outputSpec0, outputSpec1 };
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.knime.core.node.NodeModel#saveSettingsTo(org.knime.core.node.
	 * NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		for (QueryOptionModel queryModel : m_queryModels) {
			queryModel.saveSettingsTo(settings);
		}
		for (ReportFieldModel reportModel : m_reportModels) {
			reportModel.saveSettingsTo(settings);
		}
		if (m_simModel != null) {
			m_simModel.saveSettingsTo(settings);
		}
		if (m_ligandImgSize != null) {
			m_ligandImgSize.saveSettingsTo(settings);
		}
		if (m_conjunction != null) {
			m_conjunction.saveSettingsTo(settings);
		}
		if (m_stdReport != null) {
			settings.addString(STD_REPORT_KEY, m_stdReport.getId());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.knime.core.node.NodeModel#loadValidatedSettingsFrom(org.knime.core
	 * .node.NodeSettingsRO)
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		if (!m_lastError.isEmpty()) {
			throw new InvalidSettingsException(
					"Error loading query and report definitions from PdbConnectorConfig.xml/.dtd"
							+ " (" + m_lastError + ")");
		}
		for (QueryOptionModel queryModel : m_queryModels) {
			queryModel.loadValidatedSettingsFrom(settings);
		}
		for (ReportFieldModel reportModel : m_reportModels) {
			reportModel.loadValidatedSettingsFrom(settings);
		}
		if (m_simModel != null) {
			m_simModel.loadValidatedSettingsFrom(settings);
		}
		if (m_ligandImgSize != null) {
			m_ligandImgSize.loadSettingsFrom(settings);
		}
		if (m_conjunction != null) {
			m_conjunction.loadSettingsFrom(settings);
		}
		// Retrieve the current standard report object by ID
		String stdReportId = settings.getString(STD_REPORT_KEY);
		m_stdReport = getStandardReport(stdReportId);
		if (m_stdReport == null) {
			throw new InvalidSettingsException("Invalid string \""
					+ stdReportId + "\" for " + STD_REPORT_KEY + " setting");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.knime.core.node.NodeModel#validateSettings(org.knime.core.node.
	 * NodeSettingsRO)
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		if (!m_lastError.isEmpty()) {
			throw new InvalidSettingsException(
					"Error loading query and report definitions from PdbConnectorConfig.xml/.dtd"
							+ " (" + m_lastError + ")");
		}
		for (QueryOptionModel queryModel : m_queryModels) {
			queryModel.validateSettings(settings);
		}
		for (ReportFieldModel reportModel : m_reportModels) {
			reportModel.validateSettings(settings);
		}
		if (m_simModel != null) {
			m_simModel.validateSettings(settings);
		}
		if (m_ligandImgSize != null) {
			m_ligandImgSize.validateSettings(settings);
		}
		if (m_conjunction != null) {
			m_conjunction.validateSettings(settings);
		}
		// Check if standard report id is valid
		String stdReportId = settings.getString(STD_REPORT_KEY);
		if (getStandardReport(stdReportId) == null) {
			throw new InvalidSettingsException("Invalid string \""
					+ stdReportId + "\" for " + STD_REPORT_KEY + " setting");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.knime.core.node.NodeModel#loadInternals(java.io.File,
	 * org.knime.core.node.ExecutionMonitor)
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

		// TODO load internal data.
		// Everything handed to output ports is loaded automatically (data
		// returned by the execute method, models loaded in loadModelContent,
		// and user settings set through loadSettingsFrom - is all taken care
		// of). Load here only the other internals that need to be restored
		// (e.g. data used by the views).

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.knime.core.node.NodeModel#saveInternals(java.io.File,
	 * org.knime.core.node.ExecutionMonitor)
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

		// TODO save internal models.
		// Everything written to output ports is saved automatically (data
		// returned by the execute method, models saved in the saveModelContent,
		// and user settings saved through saveSettingsTo - is all taken care
		// of). Save here only the other internals that need to be preserved
		// (e.g. data used by the views).

	}

	/**
	 * Creates the output table spec0.
	 *
	 * @return the data table spec
	 */
	private DataTableSpec createOutputTableSpec0() {
		int nCols = PDB_COLUMNS.length;
		DataColumnSpec[] allColSpecs = new DataColumnSpec[nCols];
		for (int i = 0; i < nCols; ++i) {
			allColSpecs[i] = new DataColumnSpecCreator(PDB_COLUMNS[i],
					StringCell.TYPE).createSpec();
		}
		DataTableSpec retVal = new DataTableSpec(allColSpecs);
		return retVal;
	}

	/**
	 * Creates the output table spec1.
	 *
	 * @return the data table spec
	 */
	private DataTableSpec createOutputTableSpec1() {
		// Track selected column names and report URL values, to ensure no
		// duplicates
		Set<String> columnNames = new HashSet<String>();
		Set<String> reportValues = new HashSet<String>();
		m_selectedFields.clear();
		m_primaryCitationSuffix = null;
		m_numNonHidden = 0;

		// Apply the current standard report
		// This may update the individual report field selections
		// except for custom reports
		for (ReportFieldModel model : m_reportModels) {
			model.applyStandardReport(m_stdReport);
		}

		// Compile list of all selected fields (hidden first, then from UI)
		for (ReportFieldModel hidden : m_hiddenReportModels) {
			if (hidden.applyTrigger(m_reportModels)) {
				ReportField field = hidden.getField();
				// DM 2012May02 - workaround for primary/all citations
				if (field.isPrimaryCitationSuffix()) {
					m_primaryCitationSuffix = field;
				} else if (!columnNames.contains(field.getColName())
						&& !reportValues.contains(field.getValue())) {
					m_selectedFields.add(field);
					columnNames.add(field.getColName());
					reportValues.add(field.getValue());
				}
			}
		}
		// now the visible fields
		for (ReportFieldModel model : m_reportModels) {
			ReportField field = model.getField();
			if (model.isSelected() && !columnNames.contains(field.getColName())
					&& !reportValues.contains(field.getValue())) {
				m_selectedFields.add(field);
				columnNames.add(field.getColName());
				reportValues.add(field.getValue());
				++m_numNonHidden;
			}
		}
		int nCols = m_selectedFields.size();
		DataColumnSpec[] allColSpecs = new DataColumnSpec[nCols];
		for (int i = 0; i < nCols; ++i) {
			final ReportField field = m_selectedFields.get(i);
			allColSpecs[i] = new DataColumnSpecCreator(field.getColName(),
					ModelHelperFunctions.getDataType(field)).createSpec();
		}
		DataTableSpec retVal = new DataTableSpec(allColSpecs);
		return retVal;
	}

	/**
	 * Creates the query models.
	 *
	 * @param config
	 *            the configuration
	 */
	private void createQueryModels(final PdbConnectorConfig config) {
		m_queryModels.clear();
		List<QueryCategory> categories = config.getQueryCategories();
		for (QueryCategory category : categories) {
			List<QueryOption> queryOptions = category.getQueryOptions();
			for (QueryOption queryOption : queryOptions) {
				m_queryModels.add(new QueryOptionModel(queryOption));
			}
		}
	}

	/**
	 * Creates the report models.
	 *
	 * @param config
	 *            the configuration
	 */
	private void createReportModels(final PdbConnectorConfig config) {
		m_reportModels.clear();
		m_hiddenReportModels.clear();
		List<ReportCategory> categories = config.getReportCategories();
		for (ReportCategory category : categories) {
			List<ReportField> fields = category.getReportFields();
			for (ReportField field : fields) {
				if (category.isHidden()) {
					m_hiddenReportModels.add(new ReportFieldModel(field));
				} else {
					m_reportModels.add(new ReportFieldModel(field));
				}
			}
		}
	}

	/**
	 * Gets the StandardReport for a given report id.
	 *
	 * @param reportId
	 *            the report id
	 * @return the standard report
	 */
	private StandardReport getStandardReport(final String reportId) {
		StandardReport retVal = null;
		if (reportId != null) {
			Iterator<StandardCategory> iter = m_stdCategories.iterator();
			while (iter.hasNext() && (retVal == null)) {
				retVal = iter.next().getStandardReport(reportId);
			}
		}
		return retVal;
	}
}