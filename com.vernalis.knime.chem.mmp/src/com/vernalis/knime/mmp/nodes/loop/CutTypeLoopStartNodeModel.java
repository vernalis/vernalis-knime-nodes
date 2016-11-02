/*******************************************************************************
 * Copyright (c) 2015, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License, Version 3, as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.mmp.nodes.loop;

import static com.vernalis.knime.mmp.nodes.loop.CutTypeLoopStartNodeDialog.createSelectedCutTypesModel;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.workflow.LoopStartNodeTerminator;

import com.vernalis.knime.mmp.FragmentationTypes;

/**
 * Node Model class for the cut type loop start node
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class CutTypeLoopStartNodeModel extends NodeModel implements
		LoopStartNodeTerminator {
	SettingsModelStringArray m_selectedCutTypes = createSelectedCutTypesModel();
	FragmentationTypes[] selectedFragTypes = null;
	int iteration = 0;

	/**
	 * Constructor for the node model
	 */
	protected CutTypeLoopStartNodeModel() {
		super(1, 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#execute(org.knime.core.node.BufferedDataTable
	 * [], org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {
		pushFlowVariableInt("Current Iteration", iteration);
		pushFlowVariableInt("Max Iterations", selectedFragTypes.length);
		pushFlowVariableString("Fragmentation Type",
				selectedFragTypes[iteration].getActionCommand());
		iteration++;
		return inData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#configure(org.knime.core.data.DataTableSpec
	 * [])
	 */
	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		iteration = 0;
		selectedFragTypes = new FragmentationTypes[m_selectedCutTypes
				.getStringArrayValue().length];
		for (int i = 0; i < selectedFragTypes.length; i++) {
			selectedFragTypes[i] = FragmentationTypes
					.valueOf(m_selectedCutTypes.getStringArrayValue()[i]);

		}

		pushFlowVariableInt("Current Iteration", iteration);
		pushFlowVariableInt("Max Iterations", selectedFragTypes.length);
		pushFlowVariableString("Fragmentation Type",
				selectedFragTypes[iteration].getActionCommand());
		return inSpecs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.workflow.LoopStartNodeTerminator#terminateLoop()
	 */
	@Override
	public boolean terminateLoop() {
		if (selectedFragTypes == null) {
			return true;
		}
		return iteration >= selectedFragTypes.length;
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

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#saveSettingsTo(org.knime.core.node.
	 * NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_selectedCutTypes.saveSettingsTo(settings);

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
		m_selectedCutTypes.validateSettings(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#loadValidatedSettingsFrom(org.knime.core
	 * .node.NodeSettingsRO)
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_selectedCutTypes.loadSettingsFrom(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#reset()
	 */
	@Override
	protected void reset() {
		iteration = 0;
	}

}
