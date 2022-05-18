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
package com.vernalis.knime.flowcontrol.nodes.switches.emptytable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.knime.core.node.BufferedDataTable;
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

/**
 * Node Model for the Configurable Empty Table Switch node
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class EmptyTableSwitchNodeModel extends NodeModel {

	private final boolean secondPortVariable;

	/**
	 * Constructor
	 * 
	 * @param creationConfig
	 *            the node creation configurations
	 */
	public EmptyTableSwitchNodeModel(NodeCreationConfiguration creationConfig) {
		super(createInputPorts(creationConfig), creationConfig.getPortConfig()
				.orElseThrow(IllegalArgumentException::new).getOutputPorts());
		secondPortVariable =
				getOutPortType(1).equals(FlowVariablePortObject.TYPE);
	}

	private static PortType[]
			createInputPorts(NodeCreationConfiguration creationConfig) {
		final PortType[] retVal = creationConfig.getPortConfig()
				.orElseThrow(IllegalArgumentException::new).getInputPorts();
		if (retVal[1].equals(FlowVariablePortObject.TYPE)) {
			retVal[1] = FlowVariablePortObject.TYPE_OPTIONAL;
		}
		return retVal;
	}

	@Override
	protected PortObject[] execute(PortObject[] inData, ExecutionContext exec)
			throws Exception {
		PortObject[] outData = new PortObject[2];
		Arrays.fill(outData, InactiveBranchPortObject.INSTANCE);
		if (((BufferedDataTable) inData[0]).size() == 0L) {
			outData[1] = secondPortVariable ? FlowVariablePortObject.INSTANCE
					: inData[1];
		} else {
			outData[0] = inData[0];
		}
		return outData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#configure(org.knime.core.data.DataTableSpec
	 * [])
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		// We let both branches be active, in order to allow downstream
		// configuring of both
		if (secondPortVariable && inSpecs[1] == null) {
			return new PortObjectSpec[] { inSpecs[0],
					FlowVariablePortObjectSpec.INSTANCE };
		}
		return inSpecs;
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
		//
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		//
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		//
	}

	@Override
	protected void reset() {
		//
	}

}
