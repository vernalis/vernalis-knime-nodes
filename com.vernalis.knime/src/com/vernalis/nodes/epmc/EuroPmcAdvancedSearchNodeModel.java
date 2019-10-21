/*******************************************************************************
 * Copyright (c) 2016,2019 Vernalis (R&D) Ltd
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
package com.vernalis.nodes.epmc;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.xml.XMLCell;
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
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.MergeOperator;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortObjectOutput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.core.node.streamable.StreamableOperatorInternals;

import static com.vernalis.nodes.epmc.EuroPmcAdvancedSearchNodeDialog.createEmailModel;
import static com.vernalis.nodes.epmc.EuroPmcAdvancedSearchNodeDialog.createOAOnlyModel;
import static com.vernalis.nodes.epmc.EuroPmcAdvancedSearchNodeDialog.createPageSizeModel;

/**
 * This is the model implementation of EuroPmcAdvancedSearch. Node to run a
 * reference query on the European Pub Med Central webservice and return the
 * results as an XML table
 * 
 * @author SDR
 */
public class EuroPmcAdvancedSearchNodeModel extends NodeModel {

	// the logger instance
	private static final NodeLogger logger =
			NodeLogger.getLogger(EuroPmcAdvancedSearchNodeModel.class);

	// Settings models
	static final String CFG_TITLE = "Title";
	static final String CFG_AUTHORS = "Authors";
	static final String CFG_AFFILIATION = "Affiliation";
	static final String CFG_YEAR_FROM = "Year_From";
	static final String CFG_YEAR_TO = "Year_To";
	static final String CFG_JOURNALS = "Journals";
	static final String CFG_MESH = "MeSH_Terms";
	static final String CFG_GNL_QUERY = "General_Query";
	static final String CFG_QUERY_TYPE = "Query_Type";
	static final String CFG_SORT_ORDER = "Sort_Order";

	private final SettingsModelString m_Title =
			new SettingsModelString(CFG_TITLE, null);
	private final SettingsModelString m_Authors =
			new SettingsModelString(CFG_AUTHORS, null);
	private final SettingsModelString m_Affiliation =
			new SettingsModelString(CFG_AFFILIATION, null);
	private final SettingsModelString m_From =
			new SettingsModelString(CFG_YEAR_FROM, null);
	private final SettingsModelString m_To =
			new SettingsModelString(CFG_YEAR_TO, null);
	private final SettingsModelString m_Journals =
			new SettingsModelString(CFG_JOURNALS, null);
	private final SettingsModelString m_MeSH =
			new SettingsModelString(CFG_MESH, null);
	private final SettingsModelString m_Gnl =
			new SettingsModelString(CFG_GNL_QUERY, null);
	private final SettingsModelString m_QueryType =
			new SettingsModelString(CFG_QUERY_TYPE, "Core");
	private final SettingsModelString m_SortOrder =
			new SettingsModelString(CFG_SORT_ORDER, "Date");
	private final SettingsModelIntegerBounded m_pageSize =
			createPageSizeModel();
	private final SettingsModelString m_email = createEmailModel();
	private final SettingsModelBoolean m_openAccess = createOAOnlyModel();

	// Data table Spec
	private static final DataTableSpec spec =
			new DataTableSpec(createDataColumnSpec());

	/**
	 * Constructor for the node model.
	 */
	protected EuroPmcAdvancedSearchNodeModel() {
		super(new PortType[] {}, new PortType[] { BufferedDataTable.TYPE,
				FlowVariablePortObject.TYPE });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData,
			final ExecutionContext exec) throws Exception {

		// // Now create a data container for the new output table
		BufferedDataTableRowOutput output =
				new BufferedDataTableRowOutput(exec.createDataContainer(spec));
		PortObjectOutput fvOutput = new PortObjectOutput();
		fvOutput.setPortObject(FlowVariablePortObject.INSTANCE);
		createStreamableOperator(null, null).runFinal(new PortInput[0],
				new PortOutput[] { output, fvOutput }, exec);
		return new PortObject[] { output.getDataTable(),
				fvOutput.getPortObject() };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#createStreamableOperator(org.knime.core.
	 * node.streamable.PartitionInfo, org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	public StreamableOperator createStreamableOperator(
			PartitionInfo partitionInfo, PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		return new StreamableOperator() {

			ServiceResultStreamableOperatorInternals internals =
					new ServiceResultStreamableOperatorInternals();

			@Override
			public void runFinal(PortInput[] inputs, PortOutput[] outputs,
					ExecutionContext exec) throws Exception {
				RowOutput out = (RowOutput) outputs[0];
				long currentRowID = 0;
				long pageCount = 1;
				String nextCursor = "*";

				// Build the query string
				boolean sortDate = m_SortOrder.getStringValue().equals("Date");
				String queryText = EpmcHelpers.buildQueryString(
						m_Title.getStringValue(), m_Authors.getStringValue(),
						m_Affiliation.getStringValue(), m_From.getStringValue(),
						m_To.getStringValue(), m_Journals.getStringValue(),
						m_MeSH.getStringValue(), m_Gnl.getStringValue(),
						sortDate, m_openAccess.getBooleanValue());

				// Inform the user of progress...
				logger.info("Query string used: " + queryText);
				// Now we need to run the initial query and add the results from
				// this to the output
				exec.setMessage("Fetching first result page...");
				URL queryURL = EpmcHelpers.buildQueryURL(queryText,
						m_QueryType.getStringValue(), nextCursor,
						m_pageSize.getIntValue(), null,
						m_email.getStringValue());
				String xmlResult = EpmcHelpers.askEpmc(queryURL, exec);
				long hitCount = Long.parseLong(EpmcHelpers
						.getStringFromXmlField(xmlResult, "hitCount"));
				String serviceVersion =
						EpmcHelpers.getStringFromXmlField(xmlResult, "version");

				if (hitCount > 0) {
					List<DataCell> results = new ArrayList<>();
					results = EpmcHelpers.getRecordsFromXml(xmlResult, "result",
							"result");

					// And now add the results
					for (DataCell result : results) {
						out.push(new DefaultRow("Row " + currentRowID++,
								result));
						exec.setProgress(pageCount + " page(s) fetched..."
								+ currentRowID + " hits of " + hitCount
								+ " added to output");
						exec.checkCanceled();
					}

					// Now update the page counter
					pageCount++;

					// Now we need to loop through the remaining pages
					while (currentRowID < hitCount) {
						nextCursor = EpmcHelpers.getStringFromXmlField(
								xmlResult, "nextCursorMark");
						queryURL = EpmcHelpers.buildQueryURL(queryText,
								m_QueryType.getStringValue(), nextCursor,
								m_pageSize.getIntValue(), null,
								m_email.getStringValue());
						xmlResult = EpmcHelpers.askEpmc(queryURL, exec);
						results = EpmcHelpers.getRecordsFromXml(xmlResult,
								"result", "result");

						// And now add the results
						for (DataCell result : results) {
							out.push(new DefaultRow("Row " + currentRowID++,
									result));
							// Update the status text
							exec.setProgress(pageCount + " page(s) fetched..."
									+ currentRowID + " hits of " + hitCount
									+ " added to output");
							exec.checkCanceled();
						}

						// Now update the page counter
						pageCount++;
					}
				}
				out.close();
				pushFlowVariableInt("hitCount", (int) hitCount);
				internals.setHitCount(hitCount);
				pushFlowVariableInt("pagecount", (int) --pageCount);
				internals.setPageCount(pageCount);
				pushFlowVariableString("queryString", queryText);
				internals.setQueryString(queryText);
				String url =
						EpmcHelpers
								.buildQueryURL(queryText,
										m_QueryType.getStringValue(), "*")
								.toString();
				pushFlowVariableString("queryURL", url);
				internals.setQueryURL(url);
				pushFlowVariableString("resultType",
						m_QueryType.getStringValue());
				internals.setResultType(m_QueryType.getStringValue());
				String queryFromXml = EpmcHelpers
						.getStringFromXmlField(xmlResult, "queryString");
				pushFlowVariableString("queryFromEPMCXml", queryFromXml);
				internals.setQueryFromXML(queryFromXml);
				pushFlowVariableString("Service Version", serviceVersion);
				internals.setVersion(serviceVersion);
				((PortObjectOutput) outputs[1])
						.setPortObject(FlowVariablePortObject.INSTANCE);
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see
			 *
			 * org.knime.core.node.streamable.StreamableOperator#saveInternals()
			 */
			@Override
			public StreamableOperatorInternals saveInternals() {
				return internals;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#getOutputPortRoles()
	 */
	@Override
	public OutputPortRole[] getOutputPortRoles() {
		return new OutputPortRole[] { OutputPortRole.DISTRIBUTED,
				OutputPortRole.NONDISTRIBUTED };
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.knime.core.node.NodeModel#createMergeOperator()
	 */
	@Override
	public MergeOperator createMergeOperator() {
		return new MergeOperator() {

			@Override
			public StreamableOperatorInternals mergeFinal(
					StreamableOperatorInternals[] operators) {
				// Should only ever be 1
				if (operators.length != 1) {
					logger.warn(
							"Something strange - wrong number of streamable operator internals found ("
									+ operators.length + ")");
				}
				return operators[0];
			}
		};

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.knime.core.node.NodeModel#finishStreamableExecution(org.knime.core.
	 * node.streamable.StreamableOperatorInternals,
	 * org.knime.core.node.ExecutionContext,
	 * org.knime.core.node.streamable.PortOutput[])
	 */
	@Override
	public void finishStreamableExecution(StreamableOperatorInternals internals,
			ExecutionContext exec, PortOutput[] output) throws Exception {
		ServiceResultStreamableOperatorInternals castInt =
				(ServiceResultStreamableOperatorInternals) internals;
		pushFlowVariableInt("hitCount", (int) castInt.getHitCount());
		pushFlowVariableInt("pageCount", (int) castInt.getPageCount());
		pushFlowVariableString("queryString", castInt.getQueryString());
		pushFlowVariableString("queryURL", castInt.getQueryURL());
		pushFlowVariableString("resultType", castInt.getResultType());
		pushFlowVariableString("queryFromEPMCXml", castInt.getQueryFromXML());
		pushFlowVariableString("Service Version", castInt.getVersion());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		// Push default values so downstream nodes know about the flow variables
		pushFlowVariableInt("hitCount", -1);
		pushFlowVariableInt("pagecount", -1);
		pushFlowVariableString("queryString", "");
		pushFlowVariableString("queryURL", "");
		pushFlowVariableString("resultType", "");
		pushFlowVariableString("queryFromEPMCXml", "");
		pushFlowVariableString("Service Version", "");
		return new PortObjectSpec[] { spec,
				FlowVariablePortObjectSpec.INSTANCE };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_Affiliation.saveSettingsTo(settings);
		m_Authors.saveSettingsTo(settings);
		m_From.saveSettingsTo(settings);
		m_Gnl.saveSettingsTo(settings);
		m_Journals.saveSettingsTo(settings);
		m_MeSH.saveSettingsTo(settings);
		m_QueryType.saveSettingsTo(settings);
		m_SortOrder.saveSettingsTo(settings);
		m_Title.saveSettingsTo(settings);
		m_To.saveSettingsTo(settings);
		m_pageSize.saveSettingsTo(settings);
		m_email.saveSettingsTo(settings);
		m_openAccess.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_Affiliation.loadSettingsFrom(settings);
		m_Authors.loadSettingsFrom(settings);
		m_From.loadSettingsFrom(settings);
		m_Gnl.loadSettingsFrom(settings);
		m_Journals.loadSettingsFrom(settings);
		m_MeSH.loadSettingsFrom(settings);
		m_QueryType.loadSettingsFrom(settings);
		m_SortOrder.loadSettingsFrom(settings);
		m_Title.loadSettingsFrom(settings);
		m_To.loadSettingsFrom(settings);
		try {
			m_pageSize.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// legacy behaviour
			m_pageSize.setIntValue(25);
		}
		try {
			m_email.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			m_email.setStringValue(null);
		}
		try {
			m_openAccess.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			m_openAccess.setBooleanValue(false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_Affiliation.validateSettings(settings);
		m_Authors.validateSettings(settings);
		m_From.validateSettings(settings);
		m_Gnl.validateSettings(settings);
		m_Journals.validateSettings(settings);
		m_MeSH.validateSettings(settings);
		m_QueryType.validateSettings(settings);
		m_SortOrder.validateSettings(settings);
		m_Title.validateSettings(settings);
		m_To.validateSettings(settings);
		// dont validate new settings
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	private static DataColumnSpec[] createDataColumnSpec() {
		// Only have a single XML Column to hold the results at the moment
		DataColumnSpec[] dcs = new DataColumnSpec[1];
		dcs[0] = new DataColumnSpecCreator("Results", XMLCell.TYPE)
				.createSpec();
		return dcs;
	}
}
