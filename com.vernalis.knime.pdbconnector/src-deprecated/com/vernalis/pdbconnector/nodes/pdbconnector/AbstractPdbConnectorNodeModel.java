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
import java.util.Arrays;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;

import com.vernalis.pdbconnector.config.PdbConnectorConfig2;

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
	 * 
	 * @throws IllegalArgumentException
	 *             If a nonsense combination of boolean parameters is supplied
	 */
	protected AbstractPdbConnectorNodeModel(PdbConnectorConfig2 config,
			boolean hasQueryBuilder, boolean runQuery, boolean runReport)
			throws IllegalArgumentException {

		super(getInputPorts(hasQueryBuilder, runQuery, runReport),
				getOutputPorts(hasQueryBuilder, runQuery, runReport));

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
	 * 
	 * @throws IllegalArgumentException
	 *             If all three parameters are false
	 * 
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
	 * 
	 * @return The output ports
	 * 
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
		throw new InvalidSettingsException(PdbConnectorConfig2.ERROR_MSG);
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

		throw new InvalidSettingsException(PdbConnectorConfig2.ERROR_MSG);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#saveSettingsTo(org.knime.core.node.
	 * NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		// DO nothing
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

		//
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
		//
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
		//
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
		//
	}

	@Override
	public String getXMLQuery() {
		throw new UnsupportedOperationException(PdbConnectorConfig2.ERROR_MSG);
	}

	@Override
	protected void reset() {
		// Nothing to do

	}
}
