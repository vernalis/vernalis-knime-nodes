/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.flowcontrol.nodes.switches.crossover;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.FlowVariable;

import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition;
import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarConditionRegistry;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.SettingsModelFlowVarCondition;
import com.vernalis.knime.misc.ArrayUtils;

import static com.vernalis.knime.flowcontrol.nodes.switches.crossover.ConfigurableCrossoverNodeDialog.createCondModel;

/**
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class ConfigurableCrossoverNodeModel extends NodeModel {

	private final SettingsModelFlowVarCondition mdl = createCondModel();
	private static final FlowVarConditionRegistry conditionsRegistry =
			FlowVarConditionRegistry.getInstance();

	/**
	 * Constructor
	 * 
	 * @param creationConfig
	 *            the node creation config
	 */
	public ConfigurableCrossoverNodeModel(
			NodeCreationConfiguration creationConfig) {
		super(ArrayUtils.of(creationConfig.getPortConfig()
				.orElseThrow(IllegalArgumentException::new).getInputPorts()[0],
				2),
				ArrayUtils.of(creationConfig.getPortConfig()
						.orElseThrow(IllegalArgumentException::new)
						.getInputPorts()[0], 2));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {

		StringBuilder warningMsg =
				getWarningMessage() == null ? new StringBuilder()
						: new StringBuilder(getWarningMessage());
		Map<String, FlowVariable> vars = getAvailableFlowVariables(
				conditionsRegistry.getAvailableFilterVariableTypes());

		if ((mdl.getVariableName() == null || mdl.getVariableName().isEmpty())
				&& !vars.isEmpty()) {
			mdl.setVariableName(vars.keySet().stream().findFirst().get());
			if (warningMsg.length() > 0) {
				warningMsg.append("; ");
			}
			warningMsg.append(
					"Guessed variable name '" + mdl.getVariableName() + "'");
		}

		if (!vars.containsKey(mdl.getVariableName())) {
			throw new InvalidSettingsException(
					"Variable '" + mdl.getVariableName() + "' not available");
		}
		if (mdl.getConditionName() == null
				|| mdl.getConditionName().isEmpty()) {
			mdl.setCondition(conditionsRegistry
					.getConditions(
							vars.get(mdl.getVariableName()).getVariableType())
					.get(0));
			if (warningMsg.length() > 0) {
				warningMsg.append("; ");
			}
			warningMsg.append(
					String.format("Guessed condition '%s' for variable '%s'",
							mdl.getConditionName(), mdl.getVariableName()));
		}
		if (conditionsRegistry.getCondition(
				vars.get(mdl.getVariableName()).getVariableType(),
				mdl.getConditionName()) == null) {
			throw new InvalidSettingsException(
					"The condition '" + mdl.getConditionName()
							+ "' is not applicable for the variable '"
							+ mdl.getVariableName() + "'");
		}

		if (warningMsg.length() > 0
				&& !warningMsg.toString().equals(getWarningMessage())) {
			setWarningMessage(warningMsg.toString());
		}

		return crossover() ? new PortObjectSpec[] { inSpecs[1], inSpecs[0] }
				: inSpecs;
	}

	private boolean crossover() {
		Map<String, FlowVariable> vars = getAvailableFlowVariables(
				conditionsRegistry.getAvailableFilterVariableTypes());
		FlowVariable testVar = vars.get(mdl.getVariableName());
		FlowVarCondition<?> cond = conditionsRegistry.getCondition(
				testVar.getVariableType(), mdl.getConditionName());
		// We want to swap (= true) if the condition is NOT met:
		return !cond.compare(testVar, mdl);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData,
			final ExecutionContext exec) throws Exception {
		// The validity of performing the comparison has already been checked in
		// the #configure method, so now we just need to pass the input port to
		// the active output port based on the comparison
		// If the comparison is true the ports are not swapped, if it is false,
		// they are swapped
		return crossover() ? new PortObject[] { inData[1], inData[0] } : inData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#saveSettingsTo(org.knime.core.node.
	 * NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		mdl.saveSettingsTo(settings);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#validateSettings(org.knime.core.node.
	 * NodeSettingsRO)
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		mdl.validateSettings(settings);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#loadValidatedSettingsFrom(org.knime.core.
	 * node.NodeSettingsRO)
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		mdl.loadSettingsFrom(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#loadInternals(java.io.File,
	 * org.knime.core.node.ExecutionMonitor)
	 */
	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
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
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		//
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		//
	}

}
