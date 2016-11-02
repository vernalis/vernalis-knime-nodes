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
package com.vernalis.knime.flowcontrol.nodes.abstrct.ifswitch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

import com.vernalis.knime.flowcontrol.FlowControlHelpers;

/**
 * This is the abstracted node model implementation of for IF Switch nodes.
 * 
 * @author S. D. Roughley
 */
public class AbstractIfSwitchNodeModel extends NodeModel {

	/** the logger instance */
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger
			.getLogger(AbstractIfSwitchNodeModel.class);

	/** The number of output ports */
	private static final int m_OutPorts = 2;

	/** The selected port model. */
	private final SettingsModelString m_port = AbstractIfSwitchNodeDialog
			.createSettingsModel();

	/**
	 * Constructor for the node model.
	 * 
	 * @param portType
	 *            The type of port for the node
	 */
	public AbstractIfSwitchNodeModel(PortType portType) {

		super(FlowControlHelpers.createStartInPort(portType),
				FlowControlHelpers.createStartOutPorts(portType, m_OutPorts));

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData,
			final ExecutionContext exec) throws Exception {

		ArrayList<Integer> activePorts = new ArrayList<Integer>();
		if (m_port.getStringValue().equals("both")
				|| m_port.getStringValue().equals("top")) {
			activePorts.add(0);
		}
		if (m_port.getStringValue().equals("both")
				|| m_port.getStringValue().equals("bottom")) {
			activePorts.add(1);
		}
		return FlowControlHelpers.createStartOutputPortObjects(inData,
				m_OutPorts, activePorts);
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

		// Check for meaningful settings
		if (!(m_port.getStringValue().equals("top")
				|| m_port.getStringValue().equals("bottom") || m_port
				.getStringValue().equals("both"))) {
			throw new InvalidSettingsException(
					"Select the active port(s) - top, bottom or both");
		}

		ArrayList<Integer> activePorts = new ArrayList<Integer>();
		if (m_port.getStringValue().equals("both")
				|| m_port.getStringValue().equals("top")) {
			activePorts.add(0);
		}
		if (m_port.getStringValue().equals("both")
				|| m_port.getStringValue().equals("bottom")) {
			activePorts.add(1);
		}

		return FlowControlHelpers.createStartOutputPortObjectSpecs(inSpecs,
				m_OutPorts, activePorts);

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
