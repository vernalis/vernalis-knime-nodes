/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.nodes.credvar;

import static com.vernalis.nodes.credvar.CredentialVariableNodeDialog.createCredNameModel;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;
import org.knime.core.node.workflow.ICredentials;

/**
 * This is the model implementation of CredentialVariable.
 *
 *
 * @author Steve <knime@vernalis.com>
 * @since 1.27.0
 */
public class CredentialVariableNodeModel extends NodeModel {

	private final SettingsModelString cpNameMdl = createCredNameModel();

	/**
	 * Constructor for the node model.
	 */
	protected CredentialVariableNodeModel() {
		super(new PortType[] { FlowVariablePortObject.TYPE_OPTIONAL },
				new PortType[] { FlowVariablePortObject.TYPE });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		if (cpNameMdl.getStringValue() == null || cpNameMdl.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("A valid credentials name must be provided");
		}
		final Collection<String> credNames = getCredentialsProvider().listNames();
		if (!credNames.contains(cpNameMdl.getStringValue())) {
			throw new InvalidSettingsException("An invalid credentials name was supplied ('"
					+ cpNameMdl.getStringValue() + "')");
		}
		pushFlowVariableString("username", getUserName());
		return new PortObjectSpec[] { FlowVariablePortObjectSpec.INSTANCE };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec)
			throws Exception {
		pushFlowVariableString("username", getUserName());
		return new PortObject[] { FlowVariablePortObject.INSTANCE };
	}

	private String getUserName() {
		try {
			final ICredentials creds = getCredentialsProvider().get(cpNameMdl.getStringValue());
			return creds.getLogin();
		} catch (final Exception e) {
			setWarningMessage("Error retrieving credentials with id '" + cpNameMdl.getStringValue()
			+ "' - " + e.getMessage());
			return "";
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
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		cpNameMdl.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		cpNameMdl.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		cpNameMdl.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

}
