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
package com.vernalis.knime.misc.gc.node;

import java.io.File;
import java.io.IOException;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;

/**
 * This is the model implementation of RunGarbageCollector.
 * 
 * 
 * @author
 */
public class RunGarbageCollectorNodeModel extends NodeModel {
	private final NodeLogger m_logger = NodeLogger
			.getLogger(RunGarbageCollectorNodeModel.class);

	private Runtime m_rt = null;
	private final long MB = 1024 * 1024;

	/**
	 * Constructor for the node model.
	 */
	protected RunGarbageCollectorNodeModel() {
		super(new PortType[] { FlowVariablePortObject.TYPE_OPTIONAL },
				new PortType[] { FlowVariablePortObject.TYPE });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#execute(org.knime.core.node.port.PortObject
	 * [], org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec)
			throws Exception {
		m_logger.info("Memory Status before GC:");
		reportMemoryStatus(false);
		m_logger.info("Running Garbage Collection...");
		System.gc();
		reportMemoryStatus(true);
		return new PortObject[] { FlowVariablePortObject.INSTANCE };
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
		m_rt = Runtime.getRuntime();

		return new PortObjectSpec[] { FlowVariablePortObjectSpec.INSTANCE };
	}

	/**
	 * @param reportTotalMemory
	 * 
	 */
	private void reportMemoryStatus(boolean reportTotalMemory) {
		m_logger.info("Free memory: " + m_rt.freeMemory() / MB + " MB");
		m_logger.info("Used memory: "
				+ (m_rt.totalMemory() - m_rt.freeMemory()) / MB + " MB");
		m_logger.info("Allocated memory: " + m_rt.totalMemory() / MB + " MB");
		if (reportTotalMemory) {
			m_logger.info("Max. available memory: " + m_rt.maxMemory() / MB
					+ " MB");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		// nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		// nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		// nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// nothing
	}

}
