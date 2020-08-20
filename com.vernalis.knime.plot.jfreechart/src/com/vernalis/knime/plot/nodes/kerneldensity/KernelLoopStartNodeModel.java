/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.knime.plot.nodes.kerneldensity;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.workflow.LoopStartNodeTerminator;

import static com.vernalis.knime.plot.nodes.kerneldensity.KernelLoopStartNodeDialog.createMultimensionalModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelLoopStartNodeDialog.createSelectedKernelEstimatorsModel;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelLoopStartNodeDialog.createSelectedKernelSymmetriesModel;

/**
 * {@link NodeModel} implementation for the Kernel Loop Start node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class KernelLoopStartNodeModel extends NodeModel
		implements LoopStartNodeTerminator {

	protected final SettingsModelStringArray kernelEstimatorsMdl =
			createSelectedKernelEstimatorsModel();
	protected final SettingsModelStringArray kernelSymmsMdl =
			createSelectedKernelSymmetriesModel();
	protected final SettingsModelBoolean isMultimensionalMdl =
			createMultimensionalModel();

	protected KernelEstimator[] kEsts = null;
	protected KernelSymmetry[] kSymms = null;
	protected int kEstIdx = 0;
	protected int kSymmIdx = 0;
	protected int iteration = 0;

	/**
	 * Constructor for the node model
	 */
	protected KernelLoopStartNodeModel() {
		super(1, 1);
		isMultimensionalMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				kernelSymmsMdl
						.setEnabled(isMultimensionalMdl.getBooleanValue());

			}
		});
		kernelSymmsMdl.setEnabled(isMultimensionalMdl.getBooleanValue());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#execute(org.knime.core.node.
	 * BufferedDataTable [], org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {
		pushFlowVariableInt("Current Iteration", iteration);
		pushFlowVariableInt("Max Iterations", kEsts.length
				* (isMultimensionalMdl.getBooleanValue() ? kSymms.length : 1));
		pushFlowVariableString("Kernel Estimator", kEsts[kEstIdx++].name());
		if (isMultimensionalMdl.getBooleanValue()) {
			pushFlowVariableString("Kernel Symmetry", kSymms[kSymmIdx].name());
		}
		if (kEstIdx >= kEsts.length) {
			kEstIdx = 0;
			kSymmIdx++;
		}

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
		kEstIdx = 0;
		kSymmIdx = 0;
		kEsts = Arrays.stream(kernelEstimatorsMdl.getStringArrayValue())
				.map(x -> KernelEstimator.valueOf(x))
				.toArray(size -> new KernelEstimator[size]);
		kSymms = isMultimensionalMdl.getBooleanValue()
				? Arrays.stream(kernelSymmsMdl.getStringArrayValue())
						.map(x -> KernelSymmetry.valueOf(x))
						.toArray(size -> new KernelSymmetry[size])
				: null;

		if (kEsts.length == 0 || (isMultimensionalMdl.getBooleanValue()
				&& kSymms.length == 0)) {
			throw new InvalidSettingsException(
					"At least one Kernel Estimator and one Kernel Symmetry must be selected");
		}
		pushFlowVariableInt("Current Iteration", iteration);
		pushFlowVariableInt("Max Iterations",
				(isMultimensionalMdl.getBooleanValue() ? kSymms.length : 1)
						* kEsts.length);
		pushFlowVariableString("Kernel Estimator", kEsts[kEstIdx].name());
		if (isMultimensionalMdl.getBooleanValue()) {
			pushFlowVariableString("Kernel Symmetry", kSymms[kSymmIdx].name());
		}
		return inSpecs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.workflow.LoopStartNodeTerminator#terminateLoop()
	 */
	@Override
	public boolean terminateLoop() {
		if (kEsts == null && kSymms == null) {
			return true;
		}
		return iteration >= (isMultimensionalMdl.getBooleanValue()
				? kSymms.length : 1) * kEsts.length;
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
		kernelEstimatorsMdl.saveSettingsTo(settings);
		kernelSymmsMdl.saveSettingsTo(settings);
		isMultimensionalMdl.saveSettingsTo(settings);
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
		kernelEstimatorsMdl.validateSettings(settings);
		kernelSymmsMdl.validateSettings(settings);
		isMultimensionalMdl.validateSettings(settings);
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
		kernelEstimatorsMdl.loadSettingsFrom(settings);
		kernelSymmsMdl.loadSettingsFrom(settings);
		isMultimensionalMdl.loadSettingsFrom(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#reset()
	 */
	@Override
	protected void reset() {
		iteration = 0;
		kEstIdx = 0;
		kSymmIdx = 0;
	}

}
