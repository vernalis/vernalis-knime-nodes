/*******************************************************************************
 * Copyright (c) 2016,2020 Vernalis (R&D) Ltd, based on earlier PDB Connector work.
 * 
 * Copyright (c) 2012, 2014 Vernalis (R&D) Ltd and Enspiral Discovery Limited
 * 
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.pdbconnector.nodes.pdbconnector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.append.AppendedColumnRow;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;

import com.vernalis.pdbconnector.ModelHelperFunctions2;
import com.vernalis.pdbconnector.QueryOptionModel;
import com.vernalis.pdbconnector.ReportFieldModel2;
import com.vernalis.pdbconnector.config.PdbConnectorConfig2;
import com.vernalis.pdbconnector.config.Properties;
import com.vernalis.pdbconnector.config.QueryCategory;
import com.vernalis.pdbconnector.config.QueryOption;
import com.vernalis.pdbconnector.config.ReportCategory2;
import com.vernalis.pdbconnector.config.ReportField2;
import com.vernalis.pdbconnector.config.ReportOverflowException;
import com.vernalis.pdbconnector.config.StandardCategory;
import com.vernalis.pdbconnector.config.StandardReport;
import com.vernalis.pdbconnector.config.Values;
import com.vernalis.pdbconnector.config.XmlDataParsingException;

/**
 * PdbConnector Node family model class.
 * 
 * Major Changes in this version
 * <ul>
 * <li>Handles optional Query Builder or XML Input</li>
 * <li>Handles optional running of query</li>
 * <li>Handles optional running of report</li>
 * <li>Query results are added directly to a table rather than via an
 * intermediate List
 * <ul>
 * <li>Reduces memory overhead for large result sets</li>
 * </ul>
 * </li>
 * <li>POST &amp; GET reports allow chunking of results</li>
 * <li>Report is run on a DataTable not a list of PDB IDs
 * <ul>
 * <li>Removes in-memory storage of hit list</li>
 * <li>Allows node to generate report from incoming table</li>
 * </ul>
 * </li>
 * <li>Data parsing problems encountered during the report processing are added
 * to a new errors column at the end of the table
 * <ul>
 * <li>Previously only reported to the console</li>
 * <li>WARN level problems (e.g. number ranges in place of a single number) are
 * reported to new column</li>
 * <li>INFO level problems (e.g. trying different date formats) are only
 * reported to the console</li>
 * </ul>
 * </li>
 * <li>Where both query and report are run, the balance is shifted to 10/90 from
 * 30/70</li>
 * <li>Fields in the XML Definition with a deliminator property will now return
 * ListCells</li>
 * <li>Added more cancellation checks to allow cancellation during query and
 * report output processing</li>
 * <li>POST and GET requests handled in separate threads allowing cancellation
 * while waiting for server response</li>
 * </ul>
 * 
 */
@Deprecated
public class AbstractPdbConnectorNodeModel
		extends AbstractXMLQueryProviderNodeModel {

	protected final NodeLogger logger = NodeLogger.getLogger(this.getClass());

	// Settings model keys
	protected static final String[] PDB_COLUMNS =
			{ Properties.PDB_COLUMN_NAME };
	public static final String STD_REPORT_KEY = "STANDARD_REPORT";
	protected static final String LIGAND_IMG_SIZE_KEY = "LIGAND_IMAGE_SIZE";
	protected static final String CONJUNCTION_KEY = "CONJUNCTION";
	protected static final String USE_POST_KEY = "USE_POST";
	protected static final String MAX_QUERY_LENGTH_KEY = "MAX_QUERY_LENGTH";
	protected static final String ID_COL_NAME_KEY = "PDB ID Column Name";
	protected static final String XML_QUERY_KEY = "XML_QUERY";
	public static final String XML_VARNAME_KEY = "XML QUERY VAR NAME";

	protected final List<QueryOptionModel> m_queryModels = new ArrayList<>();
	protected final List<ReportFieldModel2> m_reportModels = new ArrayList<>();
	protected final List<ReportFieldModel2> m_hiddenReportModels =
			new ArrayList<>();
	protected final List<ReportField2> m_selectedFields = new ArrayList<>();
	protected ReportField2 m_primaryCitationSuffix = null;
	protected QueryOptionModel m_simModel = null;
	protected SettingsModelString m_ligandImgSize = null;
	protected SettingsModelString m_conjunction = null;
	protected SettingsModelBoolean m_usePOST = null;
	protected SettingsModelIntegerBounded m_maxQueryLength = null;
	protected SettingsModelString m_idColumnName = null;
	protected SettingsModelString m_xmlQuery = null;
	protected SettingsModelString m_xmlVarName = null;

	protected String m_lastError = "";
	protected List<StandardCategory> m_stdCategories;
	protected StandardReport m_stdReport = null;
	protected int m_numNonHidden = 0;// number of (non-hidden) selected fields
	protected Values m_ligandImgOptions = null;// ligand image size options
	protected boolean m_hasQueryBuilder, m_runQuery, m_runReport;

	/**
	 * Instantiates a new pdb connector node model.
	 * 
	 * @param config
	 *            the configuration
	 * @param hasQueryBuilder
	 *            Does the node have a query builder
	 * @param runQuery
	 *            Does the node run a query
	 * @param runReport
	 *            Does the node run a report?
	 * @throws IllegalArgumentException
	 *             If a nonsense combination of boolean parameters is supplied
	 */
	protected AbstractPdbConnectorNodeModel(PdbConnectorConfig2 config,
			boolean hasQueryBuilder, boolean runQuery, boolean runReport)
			throws IllegalArgumentException {

		super(getInputPorts(hasQueryBuilder, runQuery, runReport),
				getOutputPorts(hasQueryBuilder, runQuery, runReport));
		m_hasQueryBuilder = hasQueryBuilder;
		m_runQuery = runQuery;
		m_runReport = runReport;

		if (!config.isOK()) {
			m_lastError = config.getLastErrorMessage();
			logger.fatal(
					"Error loading query and report definitions from PdbConnectorConfig.xml/.dtd");
			logger.fatal("Last Error: " + m_lastError);
		} else {
			if (hasQueryBuilder) {
				m_simModel = new QueryOptionModel(config.getSimilarity());
				createQueryModels(config);
				m_conjunction = new SettingsModelString(CONJUNCTION_KEY,
						Properties.CONJUNCTION_AND_LABEL);

			} else if (runQuery) {
				m_xmlQuery = new SettingsModelString(XML_QUERY_KEY, "");
			}
			if (hasQueryBuilder || runQuery) {
				m_xmlVarName =
						new SettingsModelString(XML_VARNAME_KEY, "xmlQuery");
			}
			if (runReport) {
				createReportModels(config);
				m_ligandImgOptions = config.getLigandImgOptions();
				m_ligandImgSize = new SettingsModelString(LIGAND_IMG_SIZE_KEY,
						m_ligandImgOptions.getDefaultLabel());

				m_stdCategories = config.getStandardCategories();
				m_stdReport = config.getDefaultStandardReport();
				m_usePOST = new SettingsModelBoolean(USE_POST_KEY, true);
				m_maxQueryLength = new SettingsModelIntegerBounded(
						MAX_QUERY_LENGTH_KEY, 2000, 1000, 100000);
				if (!runQuery) {
					m_idColumnName =
							new SettingsModelString(ID_COL_NAME_KEY, null);
				}
			}
		}
	}

	/**
	 * Get the input ports.
	 * <p>
	 * If the node has a query builder, then no input ports are required.
	 * Otherwise, the node is either an XML Query node of some sort if it runs a
	 * query, and as such has an optional flow variable port, or is a
	 * report-only node, in which case it has an incoming BDT to append a report
	 * to
	 * </p>
	 * 
	 * @param hasQueryBuilder
	 *            Does the node have a query builder
	 * @param runQuery
	 *            Does the node run a query?
	 * @param runReport
	 *            Does the node run a report?
	 * @throws IllegalArgumentException
	 *             If all three parameters are false
	 * @return The port type array required for the node
	 */
	protected static PortType[] getInputPorts(boolean hasQueryBuilder,
			boolean runQuery, boolean runReport)
			throws IllegalArgumentException {
		if (!hasQueryBuilder && !runQuery && !runReport) {
			throw new IllegalArgumentException(
					"No node possible for false/false/false combination");
		}
		int numInputs = 0;
		if (!hasQueryBuilder) {
			// Has a port
			numInputs++;
		}
		PortType[] ports = new PortType[numInputs];
		if (runQuery) {
			// XML Query node, with optional FVP
			Arrays.fill(ports, FlowVariablePortObject.TYPE_OPTIONAL);
		} else {
			// Reporter node with BDT
			Arrays.fill(ports, BufferedDataTable.TYPE);
		}
		return ports;
	}

	/**
	 * Get the output ports.
	 * <p>
	 * We have one port for a query output table, and one for a report table. If
	 * we run neither report nor query, then we must be a query builder node,
	 * outputting the XML query string as a flow variable
	 * 
	 * @param hasQueryBuilder
	 *            Does the node have a query builder?
	 * @param runQuery
	 *            Does the node run a query?
	 * @param runReport
	 *            Does the node run a report?
	 * @return The output ports
	 * @throws IllegalArgumentException
	 *             if the nonsense hasQueryBuilder/doesnt run query/runs report
	 *             combination is supplied
	 */
	protected static PortType[] getOutputPorts(boolean hasQueryBuilder,
			boolean runQuery, boolean runReport)
			throws IllegalArgumentException {
		if (hasQueryBuilder && !runQuery && runReport) {
			// false/false/false already discarded by getInputPorts
			throw new IllegalArgumentException(
					"No node possible for true/false/true combination");
		}
		int numBDT = 0;
		if (runQuery) {
			// Port for the Structure IDs
			numBDT++;
		}
		if (runReport) {
			// Port for the Report Table
			numBDT++;
		}
		if (numBDT > 0) {
			PortType[] ports = new PortType[numBDT];
			Arrays.fill(ports, BufferedDataTable.TYPE);
			return ports;
		} else {
			assert hasQueryBuilder;
			// We must be an as yet unimplemented query builder node
			return new PortType[] { FlowVariablePortObject.TYPE };
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#execute(org.knime.core.node.
	 * BufferedDataTable [], org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData,
			final ExecutionContext exec) throws Exception {
		logger.debug("PdbConnectorNode executing...");

		// Firstly, we sort out what the query string looks like
		String xmlQuery = getXMLQuery();
		if (xmlQuery != null && xmlQuery.equals("")) {
			logger.error("No query supplied.  Halting execution!");
			throw new Exception("No query supplied.  Halting execution!");
		}
		if (xmlQuery != null) {
			pushFlowVariableString(m_xmlVarName.getStringValue(), xmlQuery);
			logger.info("getXmlQuery=" + xmlQuery);
		}

		// this might be it!
		if (!m_runQuery && !m_runReport) {
			assert xmlQuery != null;
			return new PortObject[] { FlowVariablePortObject.INSTANCE };
		}

		// Otherwise, we have to run at least one of a report and / or query

		// Divide progress into two subprogress monitors, one for each outport
		// table.

		BufferedDataTable pdbIDTable;
		int idColIdx;
		if (m_runQuery) {

			ExecutionMonitor exec0 =
					exec.createSubProgress(m_runReport ? 0.1 : 1.0);
			DataTableSpec querySpec = createQueryOutputTableSpec();
			BufferedDataContainer queryContainer =
					exec.createDataContainer(querySpec);

			exec0.setProgress(0.0,
					"Posting xmlQuery to " + Properties.SEARCH_LOCATION);
			long hitCount = ModelHelperFunctions2.postQueryToTable(xmlQuery,
					queryContainer, exec0);
			exec0.setProgress(1.0, "xmlQuery returned " + hitCount + " rows");
			logger.info("xmlQuery returned " + hitCount + " rows");
			exec0.checkCanceled();
			// once we are done, we close the container and return its table
			queryContainer.close();
			pdbIDTable = queryContainer.getTable();
			idColIdx = 0;
		} else {
			// The IDs are going to come from the incoming table at inPort 0
			pdbIDTable = (BufferedDataTable) inData[0];
			idColIdx = pdbIDTable.getSpec()
					.findColumnIndex(m_idColumnName.getStringValue());
		}

		BufferedDataTable reportTable = null;
		if (m_runReport) {
			// OUTPORT 1 (report columns) (nominal 70% of total progress)
			ExecutionMonitor exec1 =
					exec.createSubProgress(m_runQuery ? 0.9 : 1.0);
			DataTableSpec reportSpec =
					createReportTableSpec(pdbIDTable.getSpec());
			BufferedDataContainer reportContainer =
					exec.createDataContainer(reportSpec);
			// generate the report
			if (pdbIDTable.size() > 0) {
				runReport(exec1, pdbIDTable, idColIdx, reportContainer);
			}
			reportContainer.close();
			reportTable = reportContainer.getTable();
			exec1.setProgress(1.0, "OUTPORT1 complete");
			exec.setProgress(1.0, "Done");
		}

		if (m_runReport && m_runQuery
				&& pdbIDTable.size() > reportTable.size()) {
			// NB There can be more report rows than IDs, but not less
			throw new Exception("Error generating report - " + pdbIDTable.size()
					+ " PDB IDs found, " + reportTable.size()
					+ " report rows generated");
		}
		if (reportTable == null) {
			// Only ran a query
			return new BufferedDataTable[] { pdbIDTable };
		} else if (!m_runQuery) {
			// Only ran a report - dont return the incoming table separately!
			return new BufferedDataTable[] { reportTable };
		} else {
			return new BufferedDataTable[] { pdbIDTable, reportTable };
		}
	}

	@Override
	public String getXMLQuery() {
		String xmlQuery = null;

		if (m_hasQueryBuilder) {
			// We have a query builder, and so that is where the query comes
			// from
			// select the appropriate conjunction string (either AND or OR)
			String conjunction = m_conjunction.getStringValue()
					.equals(Properties.CONJUNCTION_AND_LABEL)
							? Properties.CONJUNCTION_AND
							: Properties.CONJUNCTION_OR;
			// build query
			xmlQuery = ModelHelperFunctions2.getXmlQuery(m_queryModels,
					m_simModel, conjunction);
		} else if (m_runQuery) {
			// We have no query builder, but we still run a query, so get from
			// settings model
			xmlQuery = m_xmlQuery.getStringValue();
		}
		return xmlQuery;
	}

	/**
	 * Method to generate the report columns by one or more service calls
	 * 
	 * @param exec
	 *            Execution context to allow progress monitoring and
	 *            cancellation
	 * @param pdbIDTable
	 *            The table containing a column of PDB IDs
	 * @param idColIdx
	 *            The index of the PDB IDs column
	 * @param container
	 *            The data container to add the new table rows to
	 * @throws Exception
	 */
	protected void runReport(ExecutionMonitor exec,
			BufferedDataTable pdbIDTable, int idColIdx,
			BufferedDataContainer container) throws Exception {

		// determine how many PDB IDs we can process at once without exceeding
		// MAX_URL_LENGTH
		// Each PDB ID requires 5 characters, but have to allow for length of
		// selected column field strings.
		final int CHUNK_SIZE =
				(m_maxQueryLength.getIntValue()
						- Properties.REPORT_LOCATION.length()
						- ModelHelperFunctions2
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

		CloseableRowIterator iter = pdbIDTable.iterator();
		while (iter.hasNext() && (CHUNK_SIZE > 0)) {
			double progress = (double) row / (double) pdbIDTable.size();
			exec.setProgress(progress,
					"Getting custom report (" + (pdbIDTable.size() - row) + "/"
							+ pdbIDTable.size() + " PDB IDs remaining)");
			List<String> nextChunk = new ArrayList<>();
			List<DataRow> currentBlockRows = new ArrayList<>();
			while (iter.hasNext() && (nextChunk.size() < CHUNK_SIZE)) {
				DataRow currentRow = iter.next();
				currentBlockRows.add(currentRow);
				row++;
				DataCell idCell = currentRow.getCell(idColIdx);
				if (!idCell.isMissing()) {
					nextChunk.add(((StringValue) idCell).getStringValue());
				}
			}

			// Increasing delays in seconds - we start with 0 in case the url
			// timeout is a sufficient delay
			int[] retryDelays = { 0, 1, 5, 10, 30, 60, 300, 600 };
			block++;
			Map<String, List<List<String>>> report = null;

			for (int delay : retryDelays) {
				try {
					long start = System.nanoTime();
					report = m_usePOST.getBooleanValue()
							? ModelHelperFunctions2.postCustomReportXml3(
									nextChunk, m_selectedFields,
									m_primaryCitationSuffix, exec)
							: ModelHelperFunctions2.getCustomReportXml3(
									nextChunk, m_selectedFields,
									m_primaryCitationSuffix, exec);
					logger.info("Chunk " + block + " fetched in "
							+ (System.nanoTime() - start) / 1000000000
							+ " seconds");
					break;
				} catch (ReportOverflowException e) {
					logger.error(
							"Unable to process request - please select smaller maximum query size");
					throw new IOException(
							"Unable to process request - please select smaller maximum query size");
				} catch (IOException e) {
					// Wait for an increasing period before retrying
					logger.warn("POST request failed for data block " + block
							+ " - Waiting " + delay
							+ " seconds before re-trying...");
					exec.checkCanceled();
					pause(delay, exec);
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
			addRowsToReport(currentBlockRows, idColIdx, report, exec, urlSuffix,
					container);
		}

	}

	/**
	 * Method add the processed report items to the output table
	 * 
	 * @param currentBlockRows
	 *            The data rows used in the current service call
	 * @param idColIdx
	 *            The index of the pdb id column in the incoming table
	 * @param report
	 *            The report items. The Map is keyed on the PDB ID. The top
	 *            level of the list is the rows to add to the table, and inner
	 *            list is the columns
	 * @param exec
	 *            {@link ExecutionContext} for node cancellation and progress
	 *            monitoring
	 * @param urlSuffix
	 *            The url suffix (for PNG_URL fields only)
	 * @param container
	 *            The {@link BufferedDataContainer} to add the rows to
	 * @throws CanceledExecutionException
	 *             If the user cancelled
	 * @throws IOException
	 */
	protected void addRowsToReport(List<DataRow> currentBlockRows, int idColIdx,
			Map<String, List<List<String>>> report, ExecutionMonitor exec,
			String urlSuffix, BufferedDataContainer container)
			throws CanceledExecutionException, IOException {

		for (DataRow row : currentBlockRows) {
			int subRowIdx = 0;
			DataCell[] newCells =
					new DataCell[container.getTableSpec().getNumColumns()
							- row.getNumCells()];
			Arrays.fill(newCells, DataType.getMissingCell());

			DataCell idCell = row.getCell(idColIdx);
			if (idCell.isMissing()) {
				// No PDB ID in this incoming row
				container.addRowToTable(new AppendedColumnRow(row, newCells));
			} else {
				String id = ((StringValue) idCell).getStringValue();
				List<List<String>> idReport = report.get(id);
				if (idReport != null) {
					boolean isMultiRow = idReport.size() > 1;
					for (List<String> subRow : idReport) {
						exec.checkCanceled();
						RowKey key =
								isMultiRow
										? new RowKey(row.getKey().getString()
												+ "_" + (subRowIdx++))
										: row.getKey();
						List<DataCell> problems = new ArrayList<>();
						for (int i = 1; i < m_selectedFields.size(); ++i) {
							// Skip the ID cell in column 0 of the report!
							ReportField2 field = m_selectedFields.get(i);
							String fieldValue = subRow.get(i);
							try {
								newCells[i - 1] =
										ModelHelperFunctions2.getDataCell(field,
												fieldValue, urlSuffix);
							} catch (XmlDataParsingException e) {
								// Handle any processing/parsing errors
								// We add the cell as it was return within the
								// exception and collect the WARNings
								newCells[i - 1] = e.getCell();
								problems.addAll(e.getWarnings());
								// Finally, we also add any INFOs or WARNings to
								// the console
								for (DataCell err : e.getWarnings()) {
									logger.warn("Cell parsing issue - "
											+ ((StringValue) err)
													.getStringValue()
													.replace("WARN: ", ""));
								}
								for (DataCell err : e.getInfos()) {
									logger.info("Cell parsing issue - "
											+ ((StringValue) err)
													.getStringValue()
													.replace("INFO: ", ""));
								}
							}

						}
						// Add any errors to the last column in the report table
						if (problems.size() > 0) {
							newCells[newCells.length - 1] =
									CollectionCellFactory
											.createListCell(problems);
						}

						DataRow outRow = new AppendedColumnRow(row, newCells);
						if (isMultiRow) {
							outRow = new DefaultRow(key, outRow);
						}
						container.addRowToTable(outRow);

					}
				}
			}
		}

	}

	/**
	 * A simple delay to wait for a while
	 * 
	 * @param seconds
	 *            The number of seconds to wait
	 * @param exec
	 *            The execution context for user cancellation
	 * @throws CanceledExecutionException
	 *             if the user cancels
	 */
	protected static void pause(int seconds, ExecutionMonitor exec)
			throws CanceledExecutionException {
		// simple delay function without using threads
		Date start = new Date();
		while (new Date().getTime() - start.getTime() < seconds * 1000) {
			exec.checkCanceled();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#reset()
	 */
	@Override
	protected void reset() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#configure(org.knime.core.data.DataTableSpec
	 * [])
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {

		if (!m_lastError.isEmpty()) {
			throw new InvalidSettingsException(
					"Error loading query and report definitions from PdbConnectorConfig.xml/.dtd"
							+ " (" + m_lastError + ")");
		}

		String xmlQuery = getXMLQuery();
		if (xmlQuery != null) {
			if (getAvailableInputFlowVariables()
					.containsKey(m_xmlVarName.getStringValue())) {
				logger.warn("Flow variable '" + m_xmlVarName.getStringValue()
						+ "' will be overwritten with XML Query");
				setWarningMessage(
						"Flow variable '" + m_xmlVarName.getStringValue()
								+ "' will be overwritten with XML Query");
			}
			pushFlowVariableString(m_xmlVarName.getStringValue(), xmlQuery);
		}

		if (!m_runQuery && !m_runReport) {
			// we have a query builder only node
			return new PortObjectSpec[] { FlowVariablePortObjectSpec.INSTANCE };
		}

		DataTableSpec querySpec =
				m_runQuery ? createQueryOutputTableSpec() : null;
		DataTableSpec reportSpec = m_runReport
				? createReportTableSpec(
						(DataTableSpec) (m_runQuery ? querySpec : inSpecs[0]))
				: null;

		if (m_runReport) {
			// Check at least one (non-hidden) field is selected
			if (m_numNonHidden < 1) {
				throw new InvalidSettingsException("No report fields selected");
			}
			// determine how many PDB IDs we can process at once without
			// exceeding
			// MAX_URL_LENGTH
			// Each PDB ID requires 5 characters, but have to allow for length
			// of
			// selected column field strings.
			// Only relevant if using GET query

			final int CHUNK_SIZE = (m_maxQueryLength.getIntValue()
					- Properties.REPORT_LOCATION.length()
					- ModelHelperFunctions2
							.getReportColumnsUrl(m_selectedFields).length())
					/ 5;
			if (CHUNK_SIZE < 1) {
				throw new InvalidSettingsException(
						"Too many report fields selected: MAX_QUERY_LENGTH exceeded");
			}
			if (!m_usePOST.getBooleanValue()
					&& m_maxQueryLength.getIntValue() > 8000) {
				throw new InvalidSettingsException(
						"Max query length must be <= 8000 when using GET method");
			}
		}

		if (m_runReport && !m_runQuery) {
			// Need a string column selected
			// Do we have a selection in the model?
			if (m_idColumnName.getStringValue() == null
					|| m_idColumnName.getStringValue().isEmpty()) {
				boolean typeFound = false;
				for (int i = ((DataTableSpec) inSpecs[0]).getNumColumns()
						- 1; i >= 0; i--) {
					if (((DataTableSpec) inSpecs[0]).getColumnSpec(i).getType()
							.isCompatible(StringValue.class)) {
						m_idColumnName
								.setStringValue(((DataTableSpec) inSpecs[0])
										.getColumnSpec(i).getName());
						typeFound = true;
						logger.warn("No column selected, auto-guessing "
								+ m_idColumnName.getStringValue());
						setWarningMessage("No column selected, auto-guessing "
								+ m_idColumnName.getStringValue());
						break;
					}
				}
				if (!typeFound) {
					throw new InvalidSettingsException(
							"No String column found for IDs");
				}
			}
			int idColIdx = ((DataTableSpec) inSpecs[0])
					.findColumnIndex(m_idColumnName.getStringValue());
			if (idColIdx < 0) {
				// Specified column not found
				throw new InvalidSettingsException("The selected column ("
						+ m_idColumnName.getStringValue()
						+ ") was not found in the incoming table");
			}
		}

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
		if (m_hasQueryBuilder) {
			for (QueryOptionModel queryModel : m_queryModels) {
				queryModel.saveSettingsTo(settings);
			}
			m_simModel.saveSettingsTo(settings);
			m_conjunction.saveSettingsTo(settings);
		} else if (m_runQuery) {
			m_xmlQuery.saveSettingsTo(settings);
		}

		if (m_hasQueryBuilder || m_runQuery) {
			m_xmlVarName.saveSettingsTo(settings);
		}

		if (m_runReport) {
			for (ReportFieldModel2 reportModel : m_reportModels) {
				reportModel.saveSettingsTo(settings);
			}
			if (m_stdReport != null) {
				settings.addString(STD_REPORT_KEY, m_stdReport.getId());
			}
			m_ligandImgSize.saveSettingsTo(settings);
			m_usePOST.saveSettingsTo(settings);
			m_maxQueryLength.saveSettingsTo(settings);
			if (!m_runQuery) {
				m_idColumnName.saveSettingsTo(settings);
			}
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

		if (m_hasQueryBuilder) {
			for (QueryOptionModel queryModel : m_queryModels) {
				try {
					queryModel.loadValidatedSettingsFrom(settings);
				} catch (InvalidSettingsException e) {
					if (!queryModel.getQueryOption().isNew()) {
						throw e;
					} else {
						logger.info("New query option '"
								+ queryModel.getQueryOption().getLabel()
								+ "' detected - will use default settings");
					}
				}
			}
			m_simModel.loadValidatedSettingsFrom(settings);
			m_conjunction.loadSettingsFrom(settings);
		} else if (m_runQuery) {
			m_xmlQuery.loadSettingsFrom(settings);
		}

		if (m_hasQueryBuilder || m_runQuery) {
			m_xmlVarName.loadSettingsFrom(settings);
		}

		if (m_runReport) {
			for (ReportFieldModel2 reportModel : m_reportModels) {
				try {
					reportModel.loadValidatedSettingsFrom(settings);
				} catch (InvalidSettingsException e) {
					if (!reportModel.getField().isNew()) {
						throw e;
					} else {
						logger.info(
								"Using default settings for new report field '"
										+ reportModel.getField().getColName()
										+ "'...");
					}
				}
			}
			m_ligandImgSize.loadSettingsFrom(settings);
			m_usePOST.loadSettingsFrom(settings);
			m_maxQueryLength.loadSettingsFrom(settings);

			// Retrieve the current standard report object by ID
			String stdReportId = settings.getString(STD_REPORT_KEY);
			m_stdReport = getStandardReport(stdReportId);
			if (m_stdReport == null) {
				throw new InvalidSettingsException(
						"Invalid string \"" + stdReportId + "\" for "
								+ STD_REPORT_KEY + " setting");
			}
			if (!m_runQuery) {
				m_idColumnName.loadSettingsFrom(settings);
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
		if (m_hasQueryBuilder) {
			for (QueryOptionModel queryModel : m_queryModels) {
				try {
					queryModel.validateSettings(settings);
				} catch (InvalidSettingsException e) {
					if (!queryModel.getQueryOption().isNew()) {
						throw e;
					} else {
						logger.info("New query option '"
								+ queryModel.getQueryOption().getLabel()
								+ "' detected - will use default settings");
					}
				}
			}
			m_simModel.validateSettings(settings);
			m_conjunction.validateSettings(settings);
		} else if (m_runQuery) {
			m_xmlQuery.validateSettings(settings);
		}

		if (m_hasQueryBuilder || m_runQuery) {
			m_xmlVarName.validateSettings(settings);
		}

		if (m_runReport) {
			for (ReportFieldModel2 reportModel : m_reportModels) {
				try {
					reportModel.validateSettings(settings);
				} catch (InvalidSettingsException e) {
					if (!reportModel.getField().isNew()) {
						throw e;
					} else {
						// New field - just log a warning
						logger.info("New field '"
								+ reportModel.getField().getColName()
								+ "' detected - will use default inclusion settings");
					}
				}
			}
			m_usePOST.validateSettings(settings);
			m_maxQueryLength.validateSettings(settings);
			m_ligandImgSize.validateSettings(settings);

			// Check if standard report id is valid
			String stdReportId = settings.getString(STD_REPORT_KEY);
			if (getStandardReport(stdReportId) == null) {
				throw new InvalidSettingsException(
						"Invalid string \"" + stdReportId + "\" for "
								+ STD_REPORT_KEY + " setting");
			}
			if (!m_runQuery) {
				m_idColumnName.validateSettings(settings);
			}
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
			final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

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

	}

	/**
	 * Creates the output table spec for the query result. At present, we only
	 * retain the Structure ID, regardless of query type
	 * 
	 * @return the data table spec
	 */
	protected DataTableSpec createQueryOutputTableSpec() {
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
	 * Creates the output table spec for the report table, by appending report
	 * columns to the incoming spec
	 * 
	 * @param querySpec
	 *            The incoming query spec for the IDs table
	 * 
	 * @return the data table spec
	 */
	protected DataTableSpec createReportTableSpec(DataTableSpec querySpec) {
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
		for (ReportFieldModel2 model : m_reportModels) {
			model.applyStandardReport(m_stdReport);
		}

		// Compile list of all selected fields (hidden first, then from UI)
		for (ReportFieldModel2 hidden : m_hiddenReportModels) {
			if (hidden.applyTrigger(m_reportModels)) {
				ReportField2 field = hidden.getField();
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
		for (ReportFieldModel2 model : m_reportModels) {
			ReportField2 field = model.getField();
			if (model.isSelected() && !columnNames.contains(field.getColName())
					&& !reportValues.contains(field.getValue())) {
				m_selectedFields.add(field);
				columnNames.add(field.getColName());
				reportValues.add(field.getValue());
				++m_numNonHidden;
			}
		}
		int nCols = m_selectedFields.size(); // Dont include the 1st, ID,
												// column, but do include an
												// error column at the end
		DataColumnSpec[] allColSpecs = new DataColumnSpec[nCols];
		for (int i = 1; i < nCols; i++) {
			final ReportField2 field = m_selectedFields.get(i);
			allColSpecs[i - 1] = new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(querySpec,
							field.getColName()),
					ModelHelperFunctions2.getDataType(field)).createSpec();
		}
		allColSpecs[nCols - 1] = new DataColumnSpecCreator(
				DataTableSpec.getUniqueColumnName(querySpec,
						"Data Parsing errors"),
				ListCell.getCollectionType(StringCell.TYPE)).createSpec();
		DataTableSpecCreator specFact = new DataTableSpecCreator(querySpec);
		specFact.addColumns(allColSpecs);
		return specFact.createSpec();
	}

	/**
	 * Creates the query models.
	 * 
	 * @param config
	 *            the configuration
	 */
	protected void createQueryModels(PdbConnectorConfig2 config) {
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
	protected void createReportModels(PdbConnectorConfig2 config) {
		m_reportModels.clear();
		m_hiddenReportModels.clear();
		List<ReportCategory2> categories = config.getReportCategories();
		for (ReportCategory2 category : categories) {
			List<ReportField2> fields = category.getReportFields();
			for (ReportField2 field : fields) {
				if (category.isHidden()) {
					m_hiddenReportModels.add(new ReportFieldModel2(field));
				} else {
					m_reportModels.add(new ReportFieldModel2(field));
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
	protected StandardReport getStandardReport(String reportId) {
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
