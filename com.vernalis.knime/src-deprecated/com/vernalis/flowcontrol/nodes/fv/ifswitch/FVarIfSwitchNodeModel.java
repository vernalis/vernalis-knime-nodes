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
package com.vernalis.flowcontrol.nodes.fv.ifswitch;

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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.inactive.InactiveBranchPortObject;
import org.knime.core.node.port.inactive.InactiveBranchPortObjectSpec;

import com.vernalis.flowcontrol.FlowControlHelpers;

/**
 * This is the model implementation of FVarFVIfSwitch. Flow variable if switch,
 * mimicking the If Switch node
 * 
 * @author S. D. Roughley
 */
public class FVarIfSwitchNodeModel extends NodeModel {
	// the logger instance
	private static final NodeLogger logger = NodeLogger
			.getLogger(FVarIfSwitchNodeModel.class);
	private static final int m_OutPorts = 2;
	private static final PortType m_portType = FlowVariablePortObject.TYPE;

	private final SettingsModelString m_port = FVarIfSwitchNodeDialog
			.createSettingsModel();

	/**
	 * Constructor for the node model.
	 */
	protected FVarIfSwitchNodeModel() {

		super(FlowControlHelpers.createStartInPort(m_portType),
				FlowControlHelpers.createStartOutPorts(m_portType, m_OutPorts));

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData,
			final ExecutionContext exec) throws Exception {

		if (m_port.getStringValue().equals("both")) {
			return new PortObject[] { FlowVariablePortObject.INSTANCE,
					FlowVariablePortObject.INSTANCE };
		} else if (m_port.getStringValue().equals("top")) {
			return new PortObject[] { FlowVariablePortObject.INSTANCE,
					InactiveBranchPortObject.INSTANCE };
		}
		// We will assume by default that we are at the bottom port
		return new PortObject[] { InactiveBranchPortObject.INSTANCE,
				FlowVariablePortObject.INSTANCE };
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

		if (m_port.getStringValue().equals("both")) {
			return new PortObjectSpec[] { inSpecs[0]	,
					inSpecs[0] };
		} else if (m_port.getStringValue().equals("top")) {
			return new PortObjectSpec[] { inSpecs[0],
					InactiveBranchPortObjectSpec.INSTANCE };
		} else if (m_port.getStringValue().equals("bottom")) {
			return new PortObjectSpec[] { 
					InactiveBranchPortObjectSpec.INSTANCE, inSpecs[0] };
		}
		// Otherwise the settings are broken
		throw new InvalidSettingsException("Select the active port(s) - top, bottom or both");
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
