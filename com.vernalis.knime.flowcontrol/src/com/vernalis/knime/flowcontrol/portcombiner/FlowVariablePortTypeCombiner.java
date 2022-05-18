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
package com.vernalis.knime.flowcontrol.portcombiner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;

import com.vernalis.knime.flowcontrol.portcombiner.api.PortTypeCombiner;
import com.vernalis.knime.flowcontrol.portcombiner.api.Warnable;

/**
 * PortTypeCombiner to combine FlowVariable ports
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class FlowVariablePortTypeCombiner implements PortTypeCombiner {

	/**
	 * Private constructor
	 */
	private FlowVariablePortTypeCombiner() {
		//
	}

	/**
	 * Holding class idiom provides automatic lazy instantiation and
	 * synchronization See Joshua Bloch Effective Java Item 71
	 */
	private static final class HoldingClass {

		private static FlowVariablePortTypeCombiner INSTANCE =
				new FlowVariablePortTypeCombiner();
	}

	/**
	 * Method to get the singleton instance
	 * 
	 * @return The singleton instance of FlowVariablePortTypeCombiner
	 */
	public static FlowVariablePortTypeCombiner getInstance() {
		return HoldingClass.INSTANCE;
	}

	@Override
	public Map<String, SettingsModel> getCombinerModels() {
		return Collections.emptyMap();
	}

	@Override
	public void createDialog(DefaultNodeSettingsPane dialog, Map<String, SettingsModel> models) {
		// Nothing to configure

	}

	@Override
	public boolean hasDialogOptions() {
		return false;
	}

	@Override
	public PortObjectSpec createOutputPortObjectSpec(
			List<? extends PortObjectSpec> activePorts,
			Map<String, SettingsModel> models, Warnable warnable)
			throws InvalidSettingsException {
		// We always simply return the singleton instance
		return FlowVariablePortObjectSpec.INSTANCE;
	}

	@Override
	public PortObject createOutputPortObject(int[] activePortIndices,
			ExecutionContext exec, Map<String, SettingsModel> models,
			PortObject[] inPorts)
			throws InvalidSettingsException, CanceledExecutionException {
		// We always simply return the singleton instance
		return FlowVariablePortObject.INSTANCE;
	}

}
