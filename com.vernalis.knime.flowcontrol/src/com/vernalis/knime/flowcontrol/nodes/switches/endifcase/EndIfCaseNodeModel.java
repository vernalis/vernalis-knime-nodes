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
package com.vernalis.knime.flowcontrol.nodes.switches.endifcase;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.inactive.InactiveBranchConsumer;
import org.knime.core.node.port.inactive.InactiveBranchPortObject;
import org.knime.core.node.port.inactive.InactiveBranchPortObjectSpec;

import com.vernalis.knime.flowcontrol.portcombiner.PortTypeCombinerRegistry;
import com.vernalis.knime.flowcontrol.portcombiner.api.PortTypeCombiner;
import com.vernalis.knime.flowcontrol.portcombiner.api.Warnable;
import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.nodes.SettingsModelRegistry;

import static com.vernalis.knime.flowcontrol.nodes.switches.endifcase.EndIfCaseNodeDialog.createMultiActivesModel;

/**
 * Node Model implementation for the Configurable End IF/Case node
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class EndIfCaseNodeModel extends NodeModel
		implements SettingsModelRegistry, InactiveBranchConsumer, Warnable {

	private Set<SettingsModel> models = new LinkedHashSet<>();
	private final SettingsModelString multipleActiveBranchMdl;
	private final Map<String, SettingsModel> combinerModels;

	private final PortType portType;
	private final PortTypeCombiner combiner;

	/**
	 * Constructor
	 * 
	 * @param creationConfig
	 *            the node creation configuration
	 */
	public EndIfCaseNodeModel(NodeCreationConfiguration creationConfig) {
		super(ArrayUtils.of(creationConfig.getPortConfig()
				.orElseThrow(IllegalArgumentException::new).getOutputPorts()[0],
				creationConfig.getPortConfig()
						.orElseThrow(IllegalArgumentException::new)
						.getInputPorts().length),
				creationConfig.getPortConfig()
						.orElseThrow(IllegalArgumentException::new)
						.getOutputPorts());

		this.portType = getOutPortType(0);

		combiner = PortTypeCombinerRegistry.getInstance().getCombiner(portType);

		if (combiner == null) {
			multipleActiveBranchMdl =
					registerSettingsModel(createMultiActivesModel());
			combinerModels = null;
		} else {
			multipleActiveBranchMdl = null;
			combinerModels =
					registerMapValuesModels(combiner.getCombinerModels());
		}
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

		List<PortObjectSpec> activePorts = Arrays.stream(inSpecs)
				.filter(spec -> spec != null)
				.filter(spec -> !(spec instanceof InactiveBranchPortObjectSpec))
				.collect(Collectors.toList());

		PortObjectSpec outputSpec;
		if (activePorts.isEmpty()) {
			// No active inputs - so no active output
			outputSpec = InactiveBranchPortObjectSpec.INSTANCE;
		} else if (combiner != null) {
			outputSpec = combiner.createOutputPortObjectSpec(activePorts,
					combinerModels, this);
		} else if (activePorts.size() == 1) {
			// No combiner, but only 1 active port so that goes through
			outputSpec = activePorts.get(0);
		} else {
			// Multiple active ports - depends on the behaviour setting
			MultiActivesPolicy multiPolicy;
			try {
				multiPolicy = MultiActivesPolicy
						.valueOf(multipleActiveBranchMdl.getStringValue());
			} catch (IllegalArgumentException | NullPointerException e) {
				throw new InvalidSettingsException("The multi-actives policy '"
						+ multipleActiveBranchMdl.getStringValue()
						+ "' is not a valid value (One of '"
						+ Arrays.stream(MultiActivesPolicy.values())
								.map(map -> map.getActionCommand())
								.collect(Collectors.joining("', '"))
						+ "')");
			}
			switch (multiPolicy) {
				case FAIL:
					throw new InvalidSettingsException(
							"Multiple active branches, but settings only allow 1");
				case FIRST:
					outputSpec = activePorts.get(0);
					break;
				case LAST:
					outputSpec = activePorts.get(activePorts.size() - 1);
					break;
				default:
					// Should never be here!
					throw new InvalidSettingsException(
							"Unimplemented policy - '" + multiPolicy.getText()
									+ "' - Strange!");
			}
		}
		return new PortObjectSpec[] { outputSpec };

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
		int[] activePorts = IntStream.range(0, inObjects.length)
				.filter(i -> inObjects[i] != null)
				.filter(i -> !(inObjects[i] instanceof InactiveBranchPortObject))
				.toArray();

		PortObject outputPort;
		if (activePorts.length == 0) {
			// No active inputs - so no active output
			outputPort = InactiveBranchPortObject.INSTANCE;
		} else if (combiner != null) {
			outputPort = combiner.createOutputPortObject(activePorts, exec,
					combinerModels, inObjects);
		} else if (activePorts.length == 1) {
			// No combined, but only one active, so that passes through
			outputPort = inObjects[activePorts[0]];
		} else {
			// Multiple active ports - depends on the behaviour setting
			MultiActivesPolicy multiPolicy;
			try {
				multiPolicy = MultiActivesPolicy
						.valueOf(multipleActiveBranchMdl.getStringValue());
			} catch (IllegalArgumentException | NullPointerException e) {
				throw new InvalidSettingsException("The multi-actives policy '"
						+ multipleActiveBranchMdl.getStringValue()
						+ "' is not a valid value (One of '"
						+ Arrays.stream(MultiActivesPolicy.values())
								.map(map -> map.getActionCommand())
								.collect(Collectors.joining("', '"))
						+ "')");
			}
			switch (multiPolicy) {
				case FAIL:
					throw new InvalidSettingsException(
							"Multiple active branches, but settings only allow 1");
				case FIRST:
					outputPort = inObjects[activePorts[0]];
					break;
				case LAST:
					outputPort = inObjects[activePorts[activePorts.length - 1]];
					break;
				default:
					// Should never be here!
					throw new InvalidSettingsException(
							"Unimplemented policy - '" + multiPolicy.getText()
									+ "' - Strange!");
			}
		}
		return new PortObject[] { outputPort };
	}

	@Override
	public Set<SettingsModel> getModels() {
		return models;
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
	protected void reset() {
		//
	}

	@Override
	public void setWarning(String message) {
		setWarningMessage(message);

	}

}
