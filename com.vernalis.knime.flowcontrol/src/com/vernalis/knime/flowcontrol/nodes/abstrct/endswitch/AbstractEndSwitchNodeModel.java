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
package com.vernalis.knime.flowcontrol.nodes.abstrct.endswitch;

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
import org.knime.core.node.port.inactive.InactiveBranchConsumer;

import com.vernalis.knime.flowcontrol.FlowControlHelpers;

// TODO: Auto-generated Javadoc
/**
 * This is the abstracted model implementation for flow variable end switch
 * nodes. The flow variable version does not need to handle merging as this is
 * handled by the core.
 * 
 * @author S.D. Roughley <s.roughley@vernalis.com>
 */
public class AbstractEndSwitchNodeModel extends NodeModel implements
		InactiveBranchConsumer {

	/** The logger instance */
	private static final NodeLogger logger = NodeLogger
			.getLogger(AbstractEndSwitchNodeModel.class);

	/** The multiple-active ports behaviour. */
	private final SettingsModelString m_NodeBehaviour = AbstractEndSwitchNodeDialog
			.createSettingsModel();

	/**
	 * Constructor for the node model. The optional/required status is handled
	 * automatically - all are required if only 2, otherwise the second port
	 * upwards are optional
	 * 
	 * @param portType
	 *            The type of ports
	 * @param numPorts
	 *            The number of input ports
	 */
	public AbstractEndSwitchNodeModel(PortType portType, int numPorts) {

		super(FlowControlHelpers.createEndInPorts(portType, numPorts),
				FlowControlHelpers.createStartInPort(portType));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData,
			final ExecutionContext exec) throws Exception {

		// Need to count the active inports
		int numActivePorts = FlowControlHelpers.countActivePorts(inData);

		// And behave accordingly
		switch (numActivePorts) {
		case 0:
			// If there are no active ports, we need to return an inactive port
			// We pass through port 0 in this case
			return new PortObject[] { inData[0] };
		case 1:
			// With only 1 active port, we just pass it through
			return new PortObject[] { FlowControlHelpers
					.getFirstActivePortObject(inData) };
		default:
			// Anything else depends on the user setting
			if ("Fail execution".equals(m_NodeBehaviour.getStringValue())) {
				// Here we must throw an exception, because that's what the user
				// asked for!
				logger.warn(numActivePorts + " active ports found.  "
						+ "Node execution fails as per user settings");
				throw new Exception(numActivePorts + " active ports found.  "
						+ "Node execution fails as per user settings");
			} else if ("Use first active branch".equals(m_NodeBehaviour
					.getStringValue())) {
				// In this case, we pass through the first active branch
				logger.info(numActivePorts
						+ " active branches found. Using port "
						+ FlowControlHelpers.getFirstActivePortId(inData));
				return new PortObject[] { FlowControlHelpers
						.getFirstActivePortObject(inData) };
			} else {
				// Otherwise, we pass through the last active branch
				logger.info(numActivePorts
						+ " active branches found. Using port "
						+ FlowControlHelpers.getLastActivePortId(inData));
				return new PortObject[] { FlowControlHelpers
						.getLastActivePortObject(inData) };
			}
		}
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

		// Firstly check that the settings model contains an allowed value
		if (!("Fail execution".equals(m_NodeBehaviour.getStringValue())
				|| "Use first active branch".equals(m_NodeBehaviour
						.getStringValue()) || "Use last active branch"
					.equals(m_NodeBehaviour.getStringValue()))) {
			throw new InvalidSettingsException(
					"Please select a valid option in the node dialogue");
		}

		// Need to count the active inports
		int numActivePorts = FlowControlHelpers.countActivePorts(inSpecs);

		// And behave accordingly
		switch (numActivePorts) {
		case 0:
			// If there are no active ports, we need to return an inactive port
			// We pass through port 0 in this case
			return new PortObjectSpec[] { inSpecs[0] };
		case 1:
			// With only 1 active port, we just pass it through
			return new PortObjectSpec[] { FlowControlHelpers
					.getFirstActivePortObjectSpec(inSpecs) };
		default:
			// Anything else depends on the user setting
			if ("Fail execution".equals(m_NodeBehaviour.getStringValue())) {
				// Here we must throw an exception, because that's what the user
				// asked for!
				logger.warn(numActivePorts + " active ports found.  "
						+ "Node execution fails as per user settings");
				throw new InvalidSettingsException(numActivePorts
						+ " active ports found.  "
						+ "Node execution fails as per user settings");
			} else if ("Use first active branch".equals(m_NodeBehaviour
					.getStringValue())) {
				// In this case, we pass through the first active branch
				logger.info(numActivePorts
						+ " active branches found. Using port "
						+ FlowControlHelpers.getFirstActivePortId(inSpecs));
				return new PortObjectSpec[] { FlowControlHelpers
						.getFirstActivePortObjectSpec(inSpecs) };
			} else {
				// Otherwise, we pass through the last active branch
				logger.info(numActivePorts
						+ " active branches found. Using port "
						+ FlowControlHelpers.getLastActivePortId(inSpecs));
				return new PortObjectSpec[] { FlowControlHelpers
						.getLastActivePortObjectSpec(inSpecs) };
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_NodeBehaviour.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_NodeBehaviour.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_NodeBehaviour.validateSettings(settings);
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
