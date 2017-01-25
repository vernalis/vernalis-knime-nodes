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
package com.vernalis.knime.flowcontrol.nodes.abstrct.fvendswitch;

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
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.inactive.InactiveBranchConsumer;

import com.vernalis.knime.flowcontrol.FlowControlHelpers;

/**
 * This is the abstracted model implementation for flow variable end switch
 * nodes. The flow variable version does not need to handle merging as this is
 * handled by the core.
 * 
 * @author S.D. Roughley knime@vernalis.com
 */
public class AbstractFlowVarEndSwitchNodeModel extends NodeModel implements
		InactiveBranchConsumer {

	/** The logger instance */
	private static final NodeLogger logger = NodeLogger
			.getLogger(AbstractFlowVarEndSwitchNodeModel.class);

	/** The portType */
	private static final PortType m_portType = FlowVariablePortObject.TYPE;

	/**
	 * Constructor for the node model. The optional/required status is handled
	 * automatically - all are required if only 2, otherwise the second port
	 * upwards are optional
	 * 
	 * @param numPorts
	 *            The number of input ports
	 */
	public AbstractFlowVarEndSwitchNodeModel(int numPorts) {

		super(FlowControlHelpers.createEndInPorts(m_portType, numPorts),
				FlowControlHelpers.createStartInPort(m_portType));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData,
			final ExecutionContext exec) throws Exception {

		if (!FlowControlHelpers.hasActivePort(inData)) {
			// If there are no active ports, we need to return an inactive port
			// We pass through port 0 in this case
			return new PortObject[] { inData[0] };
		}
		// We need to check whether the 1st port (idx = 0) is the
		// Only active port - if not we need to warn the user of 'strangeness'

		if (FlowControlHelpers.getFirstActivePortId(inData) > 0) {
			logger.warn("Flow variables present in both branches will take their "
					+ "values from the top (inactive) branch");
		}
		return new PortObject[] { FlowControlHelpers
				.getFirstActivePortObject(inData) };
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

		if (!FlowControlHelpers.hasActivePort(inSpecs)) {
			// If there are no active ports, we need to return an inactive port
			// We pass through port 0 in this case
			return new PortObjectSpec[] { inSpecs[0] };
		}
		// We need to check whether the 1st port (idx = 0) is the
		// Only active port - if not we need to warn the user of 'strangeness'
		if (FlowControlHelpers.getFirstActivePortId(inSpecs) > 0) {
			logger.warn("Flow variables present in both branches will take their "
					+ "values from the top (inactive) branch");
		}
		return new PortObjectSpec[] { FlowControlHelpers
				.getFirstActivePortObjectSpec(inSpecs) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {

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
