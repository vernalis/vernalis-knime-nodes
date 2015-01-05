/*******************************************************************************
 * Copyright (c) 2014, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License, Version 3, as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *  
 *******************************************************************************/
package com.vernalis.nodes.epmc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.xml.XMLCell;
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
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;

/**
 * This is the model implementation of EuroPmcAdvancedSearch. Node to run a
 * reference query on the European Pub Med Central webservice and return the
 * results as an XML table
 * 
 * @author SDR
 */
public class EuroPmcAdvancedSearchNodeModel extends NodeModel {
	// the logger instance
	private static final NodeLogger logger = NodeLogger
			.getLogger(EuroPmcAdvancedSearchNodeModel.class);

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

	private final SettingsModelString m_Title = new SettingsModelString(
			CFG_TITLE, null);
	private final SettingsModelString m_Authors = new SettingsModelString(
			CFG_AUTHORS, null);
	private final SettingsModelString m_Affiliation = new SettingsModelString(
			CFG_AFFILIATION, null);
	private final SettingsModelString m_From = new SettingsModelString(
			CFG_YEAR_FROM, null);
	private final SettingsModelString m_To = new SettingsModelString(
			CFG_YEAR_TO, null);
	private final SettingsModelString m_Journals = new SettingsModelString(
			CFG_JOURNALS, null);
	private final SettingsModelString m_MeSH = new SettingsModelString(
			CFG_MESH, null);
	private final SettingsModelString m_Gnl = new SettingsModelString(
			CFG_GNL_QUERY, null);
	private final SettingsModelString m_QueryType = new SettingsModelString(
			CFG_QUERY_TYPE, "Core");
	private final SettingsModelString m_SortOrder = new SettingsModelString(
			CFG_SORT_ORDER, "Date");

	// Data table Spec
	private static final DataTableSpec spec = new DataTableSpec(
			createDataColumnSpec());

	// Data container for results
	private BufferedDataContainer m_dc;

	// Row counter
	private int m_currentRowID;

	// page counter
	private int m_pageCount;

	// hit count
	private int hitCount;

	/**
	 * Constructor for the node model.
	 */
	protected EuroPmcAdvancedSearchNodeModel() {

		// TODO: Specify the amount of input and output ports needed.
		// TODO: Add a flow variable output port for the query, hit count
		super(new PortType[] {}, new PortType[] { BufferedDataTable.TYPE,
				FlowVariablePortObject.TYPE });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData,
			final ExecutionContext exec) throws Exception {

		// Now create a data container for the new output table
		m_dc = exec.createDataContainer(spec);
		m_currentRowID = 0;
		m_pageCount = 1;

		// Build the query string
		boolean sortDate = m_SortOrder.getStringValue().equals("Date");
		String queryText = EpmcHelpers.buildQueryString(
				m_Title.getStringValue(), m_Authors.getStringValue(),
				m_Affiliation.getStringValue(), m_From.getStringValue(),
				m_To.getStringValue(), m_Journals.getStringValue(),
				m_MeSH.getStringValue(), m_Gnl.getStringValue(), sortDate);

		// Inform the user of progress...
		logger.info("Query string used: " + queryText);
		// Now we need to run the initial query and add the results from this
		// to the buffered dc
		String xmlResult = EpmcHelpers.askEpmc(EpmcHelpers.buildQueryURL(
				queryText, m_QueryType.getStringValue(), m_pageCount));
		hitCount = Integer.parseInt(EpmcHelpers.getStringFromXmlField(
				xmlResult, "hitCount"));

		if (hitCount > 0) {
			List<DataCell> results = new ArrayList<DataCell>();
			results = EpmcHelpers.getRecordsFromXml(xmlResult, "result",
					"result");
			addResultsToDc(m_dc, results, exec);

			// Now we need to loop through the remaining pages
			while (m_currentRowID < hitCount) {
				xmlResult = EpmcHelpers.askEpmc(EpmcHelpers.buildQueryURL(
						queryText, m_QueryType.getStringValue(), m_pageCount));
				results = EpmcHelpers.getRecordsFromXml(xmlResult, "result",
						"result");
				addResultsToDc(m_dc, results, exec);
			}
		}
		m_dc.close();

		// Now we add the flow variables. Note that they don't actually
		// specifically associate
		// with the fv port, but we need to create this for the return object
		final FlowVariablePortObject flowVars = FlowVariablePortObject.INSTANCE;
		pushFlowVariableInt("hitCount", hitCount);
		pushFlowVariableInt("pagecount", --m_pageCount);
		pushFlowVariableString("queryString", queryText);
		pushFlowVariableString(
				"queryURL",
				EpmcHelpers.buildQueryURL(queryText,
						m_QueryType.getStringValue(), 1).toString());
		pushFlowVariableString("resultType", m_QueryType.getStringValue());
		pushFlowVariableString("queryFromEPMCXml",
				EpmcHelpers.getStringFromXmlField(xmlResult, "query"));
		return new PortObject[] { m_dc.getTable(), flowVars };
	}

	private void addResultsToDc(BufferedDataContainer m_dc2,
			List<DataCell> results, final ExecutionContext exec)
			throws CanceledExecutionException {
		// Update the status text
		exec.setProgress(m_pageCount + " page(s) fetched..." + m_currentRowID
				+ " hits of " + hitCount + " added to output");
		exec.checkCanceled();

		// And now add the results
		Iterator<DataCell> itr = results.iterator();
		while (itr.hasNext()) {
			m_dc.addRowToTable(new DefaultRow("Row " + m_currentRowID, itr
					.next()));
			m_currentRowID++;
		}

		// Now update the page counter
		m_pageCount++;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// TODO: generated method stub
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

		return new PortObjectSpec[] { spec, FlowVariablePortObjectSpec.INSTANCE };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		// TODO: generated method stub
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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		// TODO: generated method stub
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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		// TODO: generated method stub
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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// TODO: generated method stub
	}

	private static DataColumnSpec[] createDataColumnSpec() {
		// Only have a single XML Column to hold the results at the moment
		DataColumnSpec[] dcs = new DataColumnSpec[1];
		dcs[0] = new DataColumnSpecCreator("Results", XMLCell.TYPE)
				.createSpec();
		return dcs;
	}
}
