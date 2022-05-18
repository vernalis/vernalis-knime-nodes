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
package com.vernalis.knime.flowcontrol.nodes.switches.ifcaseswitch;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;
import org.knime.core.node.port.inactive.InactiveBranchPortObject;
import org.knime.core.node.port.inactive.InactiveBranchPortObjectSpec;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.VariableType.IntType;

import com.vernalis.knime.nodes.SettingsModelRegistry;

import static com.vernalis.knime.flowcontrol.nodes.switches.ifcaseswitch.ConfigurableIfCaseSwitchNodeDialog.createCountNegativeFromEndModel;
import static com.vernalis.knime.flowcontrol.nodes.switches.ifcaseswitch.ConfigurableIfCaseSwitchNodeDialog.createOutOfBoundsModel;
import static com.vernalis.knime.flowcontrol.nodes.switches.ifcaseswitch.ConfigurableIfCaseSwitchNodeDialog.createVarNameModel;

/**
 * Node Model for the Configurable IF/Case Switch node
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class ConfigurableIfCaseSwitchNodeModel extends NodeModel
		implements SettingsModelRegistry {

	private final Set<SettingsModel> models = new LinkedHashSet<>();
	private final SettingsModelString varNameMdl =
			registerSettingsModel(createVarNameModel());
	private final SettingsModelString outOfBoundsMdl =
			registerSettingsModel(createOutOfBoundsModel());
	private final boolean[] varOutput;
	private OutOfBoundsBehaviour oobb;
	private SettingsModelBoolean countNegativesFromEndMdl =
			registerSettingsModel(createCountNegativeFromEndModel());

	/**
	 * Constructor from port type arrays
	 * 
	 * @param inPortTypes
	 *            the input port types
	 * @param outPortTypes
	 *            the output port types
	 */
	public ConfigurableIfCaseSwitchNodeModel(PortType[] inPortTypes,
			PortType[] outPortTypes) {
		super(inPortTypes, outPortTypes);
		varOutput = new boolean[outPortTypes.length];
		for (int i = 0; i < varOutput.length; i++) {
			varOutput[i] = outPortTypes[i].equals(FlowVariablePortObject.TYPE);
		}
	}

	/**
	 * Constructor from node creation configuration
	 * 
	 * @param creationConfig
	 *            the creation config
	 */
	public ConfigurableIfCaseSwitchNodeModel(
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
		final int[] activePortIndices = getActivePortIndices();
		for (int activePortIndex : activePortIndices) {
			outPorts[activePortIndex] =
					varOutput[activePortIndex] ? FlowVariablePortObject.INSTANCE
							: inObjects[0];
		}
		return outPorts;
	}

	private int[] getActivePortIndices() throws IndexOutOfBoundsException {
		FlowVariable fVar = getAvailableFlowVariables(IntType.INSTANCE)
				.get(varNameMdl.getStringValue());
		Integer outputPortIndex = oobb.getActivePortIndex(
				fVar.getValue(IntType.INSTANCE), getNrOutPorts(),
				countNegativesFromEndMdl.getBooleanValue());
		// null indicates all ports are active
		return outputPortIndex == null
				? IntStream.range(0, getNrOutPorts()).toArray()
				: new int[] { outputPortIndex.intValue() };
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
		Map<String, FlowVariable> vars =
				getAvailableFlowVariables(IntType.INSTANCE);
		if ((varNameMdl.getStringValue() == null
				|| varNameMdl.getStringValue().isEmpty()) && !vars.isEmpty()) {
			varNameMdl.setStringValue(vars.keySet().stream().findFirst().get());
			if (warningMsg.length() > 0) {
				warningMsg.append("; ");
			}
			warningMsg.append("Guessed variable name '"
					+ varNameMdl.getStringValue() + "'");
		}

		if (!vars.containsKey(varNameMdl.getStringValue())) {
			throw new InvalidSettingsException("Variable '"
					+ varNameMdl.getStringValue() + "' not available");
		}

		if (warningMsg.length() > 0
				&& !warningMsg.toString().equals(getWarningMessage())) {
			setWarningMessage(warningMsg.toString());
		}

		oobb = setOutOfBoundsBehaviour();

		PortObjectSpec[] outSpecs = new PortObjectSpec[getNrOutPorts()];
		Arrays.fill(outSpecs, InactiveBranchPortObjectSpec.INSTANCE);
		final int[] activePortIndices = getActivePortIndices();
		for (int activePortIndex : activePortIndices) {
			outSpecs[activePortIndex] = varOutput[activePortIndex]
					? FlowVariablePortObjectSpec.INSTANCE
					: inSpecs[0];
		}
		return outSpecs;
	}

	private OutOfBoundsBehaviour setOutOfBoundsBehaviour()
			throws InvalidSettingsException {
		try {
			return OutOfBoundsBehaviour
					.valueOf(outOfBoundsMdl.getStringValue());
		} catch (NullPointerException | IllegalArgumentException e) {
			throw new InvalidSettingsException("The out-of-bounds behaviour '"
					+ outOfBoundsMdl.getStringValue()
					+ "' is not a valid value (One of '"
					+ Arrays.stream(OutOfBoundsBehaviour.values())
							.map(map -> map.getActionCommand())
							.collect(Collectors.joining("', '"))
					+ "')");
		}
	}

	/**
	 * @param creationConfig
	 * 
	 * @return
	 */
	private static PortType[] createOutputPortsFromConfig(
			NodeCreationConfiguration creationConfig) {
		// The input port type is used as the base type for the outputs
		PortType type = creationConfig.getPortConfig()
				.orElseThrow(IllegalArgumentException::new).getInputPorts()[0];
		PortType[] outputs = creationConfig.getPortConfig()
				.orElseThrow(IllegalArgumentException::new).getOutputPorts();
		for (int i = 0; i < outputs.length; i++) {
			if (!outputs[i].equals(FlowVariablePortObject.TYPE)) {
				outputs[i] = type;
			}
		}
		return outputs;
	}

	/**
	 * @param creationConfig
	 * 
	 * @return
	 */
	private static PortType[] createInputPortFromConfig(
			NodeCreationConfiguration creationConfig) {
		PortType[] input = creationConfig.getPortConfig()
				.orElseThrow(IllegalArgumentException::new).getInputPorts();
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
	public void saveSettingsTo(NodeSettingsWO settings) {
		SettingsModelRegistry.super.saveSettingsTo(settings);

	}

	@Override
	public void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		SettingsModelRegistry.super.validateSettings(settings);

	}

	@Override
	public void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		SettingsModelRegistry.super.loadValidatedSettingsFrom(settings);
	}

	@Override
	protected void reset() {
		//
	}

	@Override
	public Set<SettingsModel> getModels() {
		return models;
	}

}
