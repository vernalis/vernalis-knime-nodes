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
package com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;
import org.knime.core.node.port.inactive.InactiveBranchPortObject;
import org.knime.core.node.port.inactive.InactiveBranchPortObjectSpec;
import org.knime.core.node.workflow.FlowVariable;

import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition;
import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarConditionRegistry;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.SettingsModelFlowVarCondition;

import static com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.ConfigurableFvvalIfSwitchNodeDialog.createCondModel;

/**
 * Node Model for the Configurable IF/Case Switch (Flow Variable Value) node
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class ConfigurableFvvalIfSwitchNodeModel extends NodeModel {

	private static final String NEED_PORT_CONFIG_IN_NODE_CREATION_CONFIG =
			"Need port config in NodeCreationConfig";
	private final List<SettingsModelFlowVarCondition> condModels =
			new ArrayList<>();
	private static final FlowVarConditionRegistry conditionRegistry =
			FlowVarConditionRegistry.getInstance();
	private final boolean[] varOutput;

	/**
	 * Constructor with port types
	 * 
	 * @param inPortTypes
	 *            The input port types
	 * @param outPortTypes
	 *            The output port types
	 */
	public ConfigurableFvvalIfSwitchNodeModel(PortType[] inPortTypes,
			PortType[] outPortTypes) {
		super(inPortTypes, outPortTypes);
		varOutput = new boolean[outPortTypes.length];
		for (int i = 0; i < outPortTypes.length - 1; i++) {
			condModels.add(createCondModel(i));
			varOutput[i] = outPortTypes[i].equals(FlowVariablePortObject.TYPE);
		}
		varOutput[outPortTypes.length - 1] =
				outPortTypes[outPortTypes.length - 1]
						.equals(FlowVariablePortObject.TYPE);
	}

	/**
	 * Constructor from {@link NodeCreationConfiguration}
	 * 
	 * @param creationConfig
	 *            The configuration for changeable port types
	 */
	public ConfigurableFvvalIfSwitchNodeModel(
			NodeCreationConfiguration creationConfig) {
		this(createInputPortFromConfig(creationConfig),
				createOutputPortsFromConfig(creationConfig));
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
		PortObject[] outPorts = new PortObject[getNrOutPorts()];
		Arrays.fill(outPorts, InactiveBranchPortObject.INSTANCE);
		final int activePortIndex = getActivePortIndex();
		outPorts[activePortIndex] =
				varOutput[activePortIndex] ? FlowVariablePortObject.INSTANCE
						: inObjects[0];
		return outPorts;
	}

	private int getActivePortIndex() {
		Map<String, FlowVariable> vars = getAvailableFlowVariables(
				conditionRegistry.getAvailableFilterVariableTypes());

		int retVal = 0;
		for (SettingsModelFlowVarCondition mdl : condModels) {
			FlowVariable testVar = vars.get(mdl.getVariableName());
			FlowVarCondition<?> cond = conditionRegistry.getCondition(
					testVar.getVariableType(), mdl.getConditionName());
			if (cond.compare(testVar, mdl)) {
				return retVal;
			}
			retVal++;
		}
		return retVal;
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

		StringBuilder warningMsg =
				getWarningMessage() == null ? new StringBuilder()
						: new StringBuilder(getWarningMessage());
		Map<String, FlowVariable> vars = getAvailableFlowVariables(
				conditionRegistry.getAvailableFilterVariableTypes());
		for (SettingsModelFlowVarCondition mdl : condModels) {
			if ((mdl.getVariableName() == null
					|| mdl.getVariableName().isEmpty()) && !vars.isEmpty()) {

				mdl.setVariableName(vars.keySet().stream().findFirst().get());
				if (warningMsg.length() > 0) {
					warningMsg.append("; ");
				}
				warningMsg.append("Guessed variable name '"
						+ mdl.getVariableName() + "'");
			}
			if (!vars.containsKey(mdl.getVariableName())) {
				throw new InvalidSettingsException("Variable '"
						+ mdl.getVariableName() + "' not available");
			}
			if (mdl.getConditionName() == null
					|| mdl.getConditionName().isEmpty()) {
				mdl.setCondition(conditionRegistry.getConditions(
						vars.get(mdl.getVariableName()).getVariableType())
						.get(0));
				if (warningMsg.length() > 0) {
					warningMsg.append("; ");
				}
				warningMsg.append(String.format(
						"Guessed condition '%s' for variable '%s'",
						mdl.getConditionName(), mdl.getVariableName()));
			}
			if (conditionRegistry.getCondition(
					vars.get(mdl.getVariableName()).getVariableType(),
					mdl.getConditionName()) == null) {
				throw new InvalidSettingsException(
						"The condition '" + mdl.getConditionName()
								+ "' is not applicable for the variable '"
								+ mdl.getVariableName() + "'");
			}
		}
		if (warningMsg.length() > 0
				&& !warningMsg.toString().equals(getWarningMessage())) {
			setWarningMessage(warningMsg.toString());
		}
		PortObjectSpec[] outSpecs = new PortObjectSpec[getNrOutPorts()];
		Arrays.fill(outSpecs, InactiveBranchPortObjectSpec.INSTANCE);
		final int activePortIndex = getActivePortIndex();
		outSpecs[activePortIndex] =
				varOutput[activePortIndex] ? FlowVariablePortObjectSpec.INSTANCE
						: inSpecs[0];
		return outSpecs;
	}

	/**
	 * @param creationConfig
	 *            the {@link NodeCreationConfiguration}
	 * 
	 * @return thr output ports
	 */
	private static PortType[] createOutputPortsFromConfig(
			NodeCreationConfiguration creationConfig) {
		// The input port type is used as the base type for the outputs
		PortType type = creationConfig.getPortConfig()
				.orElseThrow(() -> new IllegalArgumentException(
						NEED_PORT_CONFIG_IN_NODE_CREATION_CONFIG))
				.getInputPorts()[0];
		PortType[] outputs = creationConfig.getPortConfig()
				.orElseThrow(() -> new IllegalArgumentException(
						NEED_PORT_CONFIG_IN_NODE_CREATION_CONFIG))
				.getOutputPorts();
		for (int i = 0; i < outputs.length; i++) {
			if (!outputs[i].equals(FlowVariablePortObject.TYPE)) {
				outputs[i] = type;
			}
		}
		return outputs;
	}

	/**
	 * @param creationConfig
	 *            the {@link NodeCreationConfiguration}
	 * 
	 * @return the input ports
	 */
	private static PortType[] createInputPortFromConfig(
			NodeCreationConfiguration creationConfig) {
		PortType[] input = creationConfig.getPortConfig()
				.orElseThrow(() -> new IllegalArgumentException(
						NEED_PORT_CONFIG_IN_NODE_CREATION_CONFIG))
				.getInputPorts();
		// Make the input optional if we have a flow variable
		if (input[0].equals(FlowVariablePortObject.TYPE)) {
			input[0] = FlowVariablePortObject.TYPE_OPTIONAL;
		}
		return input;
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		//
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		//
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		condModels.forEach(x -> x.saveSettingsTo(settings));

	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		for (SettingsModelFlowVarCondition mdl : condModels) {
			mdl.validateSettings(settings);
		}

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		for (SettingsModelFlowVarCondition mdl : condModels) {
			mdl.loadSettingsFrom(settings);
		}
	}

	@Override
	protected void reset() {
		//
	}

}
