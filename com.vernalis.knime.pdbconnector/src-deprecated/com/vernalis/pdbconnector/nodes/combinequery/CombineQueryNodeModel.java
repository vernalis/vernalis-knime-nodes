/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector.nodes.combinequery;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.FlowVariable.Type;

import com.vernalis.pdbconnector.ModelHelperFunctions2;
import com.vernalis.pdbconnector.config.Properties;
import com.vernalis.pdbconnector.nodes.pdbconnector.AbstractXMLQueryProviderNodeModel;

import static com.vernalis.pdbconnector.nodes.combinequery.CombineQueryNodeDialog.createConjunctionModel;
import static com.vernalis.pdbconnector.nodes.combinequery.CombineQueryNodeDialog.createLeftVarNameModel;
import static com.vernalis.pdbconnector.nodes.combinequery.CombineQueryNodeDialog.createQueryNameModel;
import static com.vernalis.pdbconnector.nodes.combinequery.CombineQueryNodeDialog.createRightVarNameModel;

/**
 * Node model for the Combine Query node
 */
@Deprecated
public class CombineQueryNodeModel extends AbstractXMLQueryProviderNodeModel {

	protected NodeLogger logger = NodeLogger.getLogger(this.getClass());

	protected final SettingsModelString m_leftQueryNameMdl =
			createLeftVarNameModel();
	protected final SettingsModelString m_rightQueryNameMdl =
			createRightVarNameModel();
	protected final SettingsModelString m_conjMdl = createConjunctionModel();
	protected final SettingsModelString m_outVarNameMdl =
			createQueryNameModel();

	/**
	 * Constructor
	 */
	public CombineQueryNodeModel() {
		super(new PortType[] { FlowVariablePortObject.TYPE,
				FlowVariablePortObject.TYPE_OPTIONAL },
				new PortType[] { FlowVariablePortObject.TYPE });

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#configure(org.knime.core.node.port.
	 * PortObjectSpec[])
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		Map<String, FlowVariable> fVars = getAvailableFlowVariables();
		List<String> strVars = new ArrayList<>();
		for (Entry<String, FlowVariable> ent : fVars.entrySet()) {
			if (ent.getValue().getType() == Type.STRING
					&& !ent.getValue().isGlobalConstant()) {
				strVars.add(ent.getKey());
			}
		}
		if (strVars.size() < 2) {
			throw new InvalidSettingsException(
					"At least two string flow variables are required");
		}
		int nextGuess = strVars.size() - 1;

		if (m_rightQueryNameMdl.getStringValue() == null
				|| m_rightQueryNameMdl.getStringValue().isEmpty()) {
			logger.info("Guessing " + strVars.get(nextGuess)
					+ " for right query variable name");
			m_rightQueryNameMdl.setStringValue(strVars.get(nextGuess--));
		}

		if (m_leftQueryNameMdl.getStringValue() == null
				|| m_leftQueryNameMdl.getStringValue().isEmpty()) {
			logger.info("Guessing " + strVars.get(nextGuess)
					+ " for left query variable name");
			m_leftQueryNameMdl.setStringValue(strVars.get(nextGuess--));
		}

		if (!strVars.contains(m_leftQueryNameMdl.getStringValue())) {
			throw new InvalidSettingsException("Left query variable ("
					+ m_leftQueryNameMdl.getStringValue()
					+ ") is not available");
		}
		if (!strVars.contains(m_rightQueryNameMdl.getStringValue())) {
			throw new InvalidSettingsException("Right query variable ("
					+ m_rightQueryNameMdl.getStringValue()
					+ ") is not available");
		}

		if (m_rightQueryNameMdl.getStringValue()
				.equals(m_leftQueryNameMdl.getStringValue())) {
			throw new InvalidSettingsException(
					"Both variables cannot be the same!");
		}
		if (strVars.contains(m_outVarNameMdl.getStringValue())) {
			logger.warn("Flow variable '" + m_outVarNameMdl.getStringValue()
					+ "' exists and will be overwritten");
		} else {
			pushFlowVariableString(m_outVarNameMdl.getStringValue(), "");
		}
		throw new InvalidSettingsException(
				"This node has been deprecated as the remote "
						+ "webservices have been permanently shutdown");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#execute(org.knime.core.node.port.PortObject
	 * [], org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {
		String conjunction = m_conjMdl.getStringValue().equals("AND")
				? Properties.CONJUNCTION_AND
				: Properties.CONJUNCTION_OR;
		pushFlowVariableString(m_outVarNameMdl.getStringValue(),
				ModelHelperFunctions2.combineQueries(
						peekFlowVariableString(
								m_leftQueryNameMdl.getStringValue()),
						peekFlowVariableString(
								m_rightQueryNameMdl.getStringValue()),
						conjunction));
		return new PortObject[] { FlowVariablePortObject.INSTANCE };
	}

	@Override
	public String getXMLQuery() {
		String conjunction = m_conjMdl.getStringValue().equals("AND")
				? Properties.CONJUNCTION_AND
				: Properties.CONJUNCTION_OR;
		return ModelHelperFunctions2.combineQueries(
				peekFlowVariableString(m_leftQueryNameMdl.getStringValue()),
				peekFlowVariableString(m_rightQueryNameMdl.getStringValue()),
				conjunction);
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_leftQueryNameMdl.saveSettingsTo(settings);
		m_conjMdl.saveSettingsTo(settings);
		m_rightQueryNameMdl.saveSettingsTo(settings);
		m_outVarNameMdl.saveSettingsTo(settings);

	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_leftQueryNameMdl.validateSettings(settings);
		m_conjMdl.validateSettings(settings);
		m_rightQueryNameMdl.validateSettings(settings);
		m_outVarNameMdl.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_leftQueryNameMdl.loadSettingsFrom(settings);
		m_conjMdl.loadSettingsFrom(settings);
		m_rightQueryNameMdl.loadSettingsFrom(settings);
		m_outVarNameMdl.loadSettingsFrom(settings);
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	@Override
	protected void reset() {

	}

}
