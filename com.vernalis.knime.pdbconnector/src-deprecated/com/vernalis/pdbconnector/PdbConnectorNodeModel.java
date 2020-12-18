/*******************************************************************************
 * Copyright (C) 2012, 2014 Vernalis (R&D) Ltd and Enspiral Discovery Limited
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 ******************************************************************************/
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
import org.knime.core.data.container.DataContainer;
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
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
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
 * 
 * @deprecated Use {@link PdbConnectorNodeModel2}
 */
@Deprecated
public class PdbConnectorNodeModel extends NodeModel {

	static final NodeLogger logger =
			NodeLogger.getLogger(PdbConnectorNodeModel.class);

	// Settings model keys
	static final String[] PDB_COLUMNS = { Properties.PDB_COLUMN_NAME };
	static final String STD_REPORT_KEY = "STANDARD_REPORT";
	static final String LIGAND_IMG_SIZE_KEY = "LIGAND_IMAGE_SIZE";
	static final String CONJUNCTION_KEY = "CONJUNCTION";
	static final String USE_POST_KEY = "USE_POST";
	static final String MAX_URL_LENGTH_KEY = "MAX_URL_LENGTH";

	private final List<QueryOptionModel> m_queryModels = new ArrayList<>();
	private final List<ReportFieldModel> m_reportModels = new ArrayList<>();
	private final List<ReportFieldModel> m_hiddenReportModels =
			new ArrayList<>();
	private final List<ReportField> m_selectedFields = new ArrayList<>();
	private ReportField m_primaryCitationSuffix = null;
	private QueryOptionModel m_simModel = null;
	private SettingsModelString m_ligandImgSize = null;
	private SettingsModelString m_conjunction = null;
	private SettingsModelBoolean m_usePOST = null;
	private SettingsModelIntegerBounded m_maxUrlLength = null;
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
	protected PdbConnectorNodeModel(PdbConnectorConfig config) {
		super(0, 2);
		if (!config.isOK()) {
			m_lastError = config.getLastErrorMessage();
			logger.fatal(
					"Error loading query and report definitions from PdbConnectorConfig.xml/.dtd");
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
			m_usePOST = new SettingsModelBoolean(USE_POST_KEY, true);
			m_maxUrlLength = new SettingsModelIntegerBounded(MAX_URL_LENGTH_KEY,
					2000, 1000, 8000);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#execute(org.knime.core.node.
	 * BufferedDataTable [], org.knime.core.node.ExecutionContext)
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
		BufferedDataContainer container0 =
				exec.createDataContainer(outputSpec0);

		// select the appropriate conjunction string (either AND or OR)
		String conjunction = m_conjunction.getStringValue().equals(
				Properties.CONJUNCTION_AND_LABEL) ? Properties.CONJUNCTION_AND
						: Properties.CONJUNCTION_OR;
		// run the query
		String xmlQuery = ModelHelperFunctions.getXmlQuery(m_queryModels,
				m_simModel, conjunction);
		pushFlowVariableString("xmlQuery", xmlQuery);
		logger.info("getXmlQuery=" + xmlQuery);
		exec0.setProgress(0.0,
				"Posting xmlQuery to " + Properties.SEARCH_LOCATION);
		List<String> pdbIds = ModelHelperFunctions.postQuery(xmlQuery);
		exec0.setProgress(1.0, "xmlQuery returned " + pdbIds.size() + " rows");
		logger.info("xmlQuery returned " + pdbIds.size() + " rows");
		exec0.checkCanceled();
		// create outport0 table
		long row = 0;
		if (pdbIds.size() > 0) {
			for (String pdbId : pdbIds) {
				RowKey key = RowKey.createRowKey(row);
				// the cells of the current row, the types of the cells must
				// match
				// the column spec (see above)
				DataCell[] cells = new DataCell[outputSpec0.getNumColumns()];
				cells[0] = new StringCell(pdbId);
				container0.addRowToTable(new DefaultRow(key, cells));
				++row;
			}
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
		BufferedDataContainer container1 =
				exec.createDataContainer(outputSpec1);
		// generate the report
		if (pdbIds.size() > 0) {
			if (m_usePOST.getBooleanValue()) {
				runReportWithPOST(exec1, pdbIds, outputSpec1, container1);
			} else {
				runReportWithGET(exec1, pdbIds, outputSpec1, container1);
			}
		}
		container1.close();
		BufferedDataTable out1 = container1.getTable();
		exec1.setProgress(1.0, "OUTPORT1 complete");
		exec.setProgress(1.0, "Done");
		pushFlowVariableString("xmlQuery", xmlQuery);
		return new BufferedDataTable[] { out0, out1 };
	}

	/**
	 * The new method of running the query, with a single call to the POST
	 * service. Should be used unless memory shortage is a problem.
	 * 
	 * @param exec1
	 *            The Execution Monitor
	 * @param pdbIds
	 *            The list of PDB IDs to report on
	 * @param outputSpec1
	 *            The output DataTableSpec
	 * @param container1
	 *            The DataContainer for the results
	 * @throws Exception
	 *             Throws exceptions if user cancels or there are IO errors
	 */
	private void runReportWithPOST(ExecutionMonitor exec1, List<String> pdbIds,
			DataTableSpec outputSpec1, BufferedDataContainer container1)
			throws Exception {
		exec1.setProgress("Fetching custom report data");

		final String imgSize = m_ligandImgSize.getStringValue();
		final String urlSuffix = m_ligandImgOptions.isExists(imgSize)
				? m_ligandImgOptions.getValue(imgSize)
				: imgSize;
		int row = 0;
		// Increasing delays in seconds - we start with 0 in case the url
		// timeout is a sufficient delay. Hopefully never relevant with POST
		// service
		// but we implement to be safe!
		int[] retryDelays = { 0, 1, 5, 10, 30, 60, 300, 600 };
		List<List<String>> report = null;
		for (int delay : retryDelays) {
			try {
				report = ModelHelperFunctions.postCustomReportXml2(pdbIds,
						m_selectedFields, m_primaryCitationSuffix);
				break;
			} catch (IOException e) {
				// Wait for an increasing period before retrying
				logger.warn("POST request failed for report" + " - Waiting "
						+ delay + " seconds before re-trying...");
				exec1.checkCanceled();
				pause(delay, exec1);
			}
		}

		if (report == null) {
			// In this case, we never managed to contact the server...
			String errMsg =
					"Unable to contact the remote server - please try again later!";
			logger.error(errMsg);
			throw new IOException(errMsg);
		}

		// Now add the report fields to the output table - dont care about
		// return value!
		addRowsToReport(report, exec1, row, outputSpec1, urlSuffix, container1);
	}

	/**
	 * The original method of running the query, with multiple calls to the GET
	 * service. Should only be used now if the user's memory is insufficient to
	 * use the POST method.
	 * 
	 * @param exec1
	 *            The Execution Monitor
	 * @param pdbIds
	 *            The list of PDB IDs to report on
	 * @param outputSpec1
	 *            The output DataTableSpec
	 * @param container1
	 *            The DataContainer for the results
	 * @throws Exception
	 *             Throws exceptions if user cancels or there are IO errors
	 */
	private void runReportWithGET(ExecutionMonitor exec1, List<String> pdbIds,
			DataTableSpec outputSpec1, DataContainer container1)
			throws Exception {
		Queue<String> toProcess = new LinkedList<>(pdbIds);
		// determine how many PDB IDs we can process at once without exceeding
		// MAX_URL_LENGTH
		// Each PDB ID requires 5 characters, but have to allow for length of
		// selected column field strings.
		final int CHUNK_SIZE =
				(m_maxUrlLength.getIntValue()
						- Properties.REPORT_LOCATION.length()
						- ModelHelperFunctions
								.getReportColumnsUrl(m_selectedFields).length())
						/ 5;
		logger.debug("CHUNK_SIZE=" + CHUNK_SIZE);
		// retrieve the url suffix for the currently selected ligand image size
		final String imgSize = m_ligandImgSize.getStringValue();
		final String urlSuffix = m_ligandImgOptions.isExists(imgSize)
				? m_ligandImgOptions.getValue(imgSize)
				: imgSize;
		long row = 0;
		long block = 0;
		while (!toProcess.isEmpty() && (CHUNK_SIZE > 0)) {
			double progress =
					1.0 - (double) toProcess.size() / (double) pdbIds.size();
			exec1.setProgress(progress,
					"Getting custom report (" + toProcess.size() + "/"
							+ pdbIds.size() + " PDB IDs remaining)");
			List<String> nextChunk = new ArrayList<>();
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
					report = ModelHelperFunctions.getCustomReportXml2(nextChunk,
							m_selectedFields, m_primaryCitationSuffix);
					break;
				} catch (IOException e) {
					// Wait for an increasing period before retrying
					logger.warn("GET request failed for data block " + block
							+ " - Waiting " + delay
							+ " seconds before re-trying...");
					exec1.checkCanceled();
					pause(delay, exec1);
				}
			}
			if (report == null) {
				// In this case, we never managed to contact the server...
				String errMsg =
						"Unable to contact the remote server - please try again later!";
				logger.error(errMsg);
				throw new IOException(errMsg);
			}

			// Now add the rows to the report
			row = addRowsToReport(report, exec1, row, outputSpec1, urlSuffix,
					container1);
		}
	}

	/**
	 * Utility function to add rows to report table. Called once by
	 * {@link #runReportWithPOST} and repeatedly by {@link #runReportWithGET}
	 * 
	 * @param report
	 *            List of Lists containing the report data to add
	 * @param exec1
	 *            The execution monitor
	 * @param row
	 *            the starting row index for the block to be added
	 * @param outputSpec1
	 *            the output DataTableSpec
	 * @param urlSuffix
	 *            the urlSuffix for the image field
	 * @param container1
	 *            the DataContainer with the report table being built
	 * @return the index of the last row added
	 * @throws Exception
	 *             Throws exceptions if the user cancels
	 */
	private long addRowsToReport(List<List<String>> report,
			ExecutionMonitor exec1, long row, DataTableSpec outputSpec1,
			String urlSuffix, DataContainer container1) throws Exception {
		for (List<String> recordVals : report) {
			exec1.checkCanceled();
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
				exec1.setProgress("Added row " + row + " to report table");
				container1.addRowToTable(new DefaultRow(key, cells));
			}
			++row;
		}
		return row;
	}

	private static void pause(int seconds, ExecutionMonitor exec1)
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
		// Only relevant if using GET query
		final int CHUNK_SIZE =
				(m_maxUrlLength.getIntValue()
						- Properties.REPORT_LOCATION.length()
						- ModelHelperFunctions
								.getReportColumnsUrl(m_selectedFields).length())
						/ 5;
		if (CHUNK_SIZE < 1 && !m_usePOST.getBooleanValue()) {
			throw new InvalidSettingsException(
					"Too many report fields selected: MAX_URL_LENGTH exceeded");
		}
		pushFlowVariableString("xmlQuery", "");
		throw new InvalidSettingsException(
				"This node has been deprecated as the remote "
						+ "webservices have been permanently shutdown");
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
			try {
				reportModel.loadValidatedSettingsFrom(settings);
			} catch (InvalidSettingsException e) {
				// Skip...
			}
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
			throw new InvalidSettingsException("Invalid string \"" + stdReportId
					+ "\" for " + STD_REPORT_KEY + " setting");
		}

		// New settings models in this version, with backwards compatability
		// settings
		if (m_usePOST != null) {
			try {
				m_usePOST.loadSettingsFrom(settings);
			} catch (Exception e) {
				m_usePOST.setBooleanValue(true);
			}
		}
		if (m_maxUrlLength != null) {
			try {
				m_maxUrlLength.loadSettingsFrom(settings);
			} catch (Exception e) {
				m_maxUrlLength.setIntValue(2000);
			}
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
		// for (ReportFieldModel reportModel : m_reportModels) {
		// reportModel.validateSettings(settings);
		// }
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
			throw new InvalidSettingsException("Invalid string \"" + stdReportId
					+ "\" for " + STD_REPORT_KEY + " setting");
		}

		// NB Dont validate the new settings - their possible absence is handled
		// in the #loadSettings method
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#loadInternals(java.io.File,
	 * org.knime.core.node.ExecutionMonitor)
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

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
			final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

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
			allColSpecs[i] =
					new DataColumnSpecCreator(PDB_COLUMNS[i], StringCell.TYPE)
							.createSpec();
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
		Set<String> columnNames = new HashSet<>();
		Set<String> reportValues = new HashSet<>();
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
	private void createQueryModels(PdbConnectorConfig config) {
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
	private void createReportModels(PdbConnectorConfig config) {
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
	private StandardReport getStandardReport(String reportId) {
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
