/*******************************************************************************
 * Copyright (c) 2014, Vernalis (R&D) Ltd
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
 *******************************************************************************/
package com.vernalis.knime.flowcontrol.nodes.abstrct.caseselect;

import java.io.File;
import java.io.IOException;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import com.vernalis.knime.flowcontrol.FlowControlHelpers;

/**
 * This is the shared node model implementation of the Case Select / IF Switch
 * nodes, mimicking the Case Switch node
 * 
 * @author S. D. Roughley
 */
public class AbstractCaseSelectNodeModel extends NodeModel {
	/** the logger instance */
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger
			.getLogger(AbstractCaseSelectNodeModel.class);

	/** The the number of output ports. */
	static int m_OutPorts;

	/** The selected port id model. */
	private final SettingsModelInteger m_port;

	/**
	 * Constructor for the node model.
	 * 
	 * @param portType
	 *            The type of port for the node
	 * @param numPorts
	 *            The number of output ports
	 */
	public AbstractCaseSelectNodeModel(PortType portType, int numPorts) {

		super(FlowControlHelpers.createStartInPort(portType),
				FlowControlHelpers.createStartOutPorts(portType, numPorts));

		// Populate the number of ports so the rest of the node can use it
		m_OutPorts = numPorts;

		// Create the settings model for the port selection
		m_port = AbstractCaseSelectNodeDialog.createSettingsModel(numPorts);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData,
			final ExecutionContext exec) throws Exception {
		return FlowControlHelpers.createStartOutputPortObject(inData,
				m_OutPorts, m_port.getIntValue());
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

		return FlowControlHelpers.createStartOutputPortObjectSpec(inSpecs,
				m_OutPorts, m_port.getIntValue());

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_port.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_port.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_port.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

	}

}
