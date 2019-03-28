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
package com.vernalis.knime.perfmon.nodes.memoryuse;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
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

import com.vernalis.knime.core.os.UnsupportedOperatingSystemException;
import com.vernalis.knime.core.system.CommandExecutionException;

import static com.vernalis.knime.core.memory.MemoryUtils.getJVMAllocatedMemory;
import static com.vernalis.knime.core.memory.MemoryUtils.getJVMFreeMemory;
import static com.vernalis.knime.core.memory.MemoryUtils.getJVMMaxAvailableMemory;
import static com.vernalis.knime.core.memory.MemoryUtils.getJVMUsedMemory;
import static com.vernalis.knime.core.memory.MemoryUtils.getSystemProcessMemory;
import static com.vernalis.knime.core.system.SystemUtils.getPID;

/**
 * @author s.roughley
 * 
 */
public class MemoryUsageNodeModel extends NodeModel {

	/**
	 * @param nrInDataPorts
	 * @param nrOutDataPorts
	 */
	protected MemoryUsageNodeModel() {
		super(new PortType[] { FlowVariablePortObject.TYPE_OPTIONAL },
				new PortType[] { FlowVariablePortObject.TYPE,
						BufferedDataTable.TYPE });
		// TODO Auto-generated constructor stub
	}

	protected static final String ERROR_PROCESSING_COMMAND_RESULT =
			"Error processing command result: ";
	protected static final String ERROR_RUNNING_COMMAND =
			"Error running command.  Returned: ";
	protected static final String MAC_OS_WARNING =
			"Mac OS not explicitly supported.  Will try Linux command";
	public static final String UNSUPPORTED_OPERATING_SYSTEM =
			"Unsupported Operating System";

	protected NodeLogger m_logger = NodeLogger.getLogger(this.getClass());

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#configure(org.knime.core.node.port.
	 * PortObjectSpec[])
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		initialiseFlowVars();
		return new PortObjectSpec[] { FlowVariablePortObjectSpec.INSTANCE,
				getDataTableSpec() };
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

		RowKey key = new RowKey("Memory Use (MB)");
		DataCell[] cells;
		try {
			cells = getDataRow();
		} catch (UnsupportedOperatingSystemException e) {
			m_logger.error(UNSUPPORTED_OPERATING_SYSTEM);
			throw new Exception(e.getMessage());
		}
		BufferedDataContainer dc =
				exec.createDataContainer((DataTableSpec) getDataTableSpec());
		dc.addRowToTable(new DefaultRow(key, cells));
		dc.close();
		return new PortObject[] { FlowVariablePortObject.INSTANCE,
				dc.getTable() };
	}

	/**
	 * @return
	 */
	private PortObjectSpec getDataTableSpec() {
		// TODO Auto-generated method stub
		DataColumnSpec[] colSpecs = new DataColumnSpec[6];
		int colIdx = 0;
		colSpecs[colIdx++] =
				new DataColumnSpecCreator("JVM PID", IntCell.TYPE).createSpec();
		colSpecs[colIdx++] =
				new DataColumnSpecCreator("System JVM process Memory (MB)",
						DoubleCell.TYPE).createSpec();
		colSpecs[colIdx++] =
				new DataColumnSpecCreator("Max. Available Memory (MB)",
						DoubleCell.TYPE).createSpec();
		colSpecs[colIdx++] =
				new DataColumnSpecCreator("Currently Allocated Memory (MB)",
						DoubleCell.TYPE).createSpec();
		colSpecs[colIdx++] =
				new DataColumnSpecCreator("Used Memory (MB)", DoubleCell.TYPE)
						.createSpec();
		colSpecs[colIdx++] =
				new DataColumnSpecCreator("Free Memory (MB)", DoubleCell.TYPE)
						.createSpec();

		return new DataTableSpec(colSpecs);
	}

	/**
	 * @throws UnsupportedOperatingSystemException
	 * @throws CommandExecutionException
	 */
	protected DataCell[] getDataRow()
			throws UnsupportedOperatingSystemException {
		DataCell[] cells = new DataCell[6];
		Arrays.fill(cells, DataType.getMissingCell());
		int colIdx = 0;
		cells[colIdx++] = new IntCell(getPID());
		Double mem;
		try {
			mem = getSystemProcessMemory();
			cells[colIdx++] = new DoubleCell(mem);
			pushFlowVariableDouble("System JVM process Memory (MB)", mem);
		} catch (CommandExecutionException e) {
			m_logger.info("Unable to retrieve system process memory: "
					+ e.getMessage());
			setWarningMessage("Unable to retrieve system process memory: "
					+ e.getMessage());
		}
		mem = getJVMMaxAvailableMemory();
		cells[colIdx++] = new DoubleCell(mem);
		pushFlowVariableDouble("Max. Available Memory (MB)", mem);
		mem = getJVMAllocatedMemory();
		cells[colIdx++] = new DoubleCell(mem);
		pushFlowVariableDouble("Currently Allocated Memory (MB)", mem);
		mem = getJVMUsedMemory();
		cells[colIdx++] = new DoubleCell(mem);
		pushFlowVariableDouble("Used Memory (MB)", mem);
		mem = getJVMFreeMemory();
		cells[colIdx++] = new DoubleCell(mem);
		pushFlowVariableDouble("Free Memory (MB)", mem);

		return cells;
	}

	protected void initialiseFlowVars() {
		Double mem = 0.0;
		pushFlowVariableDouble("System JVM process Memory (MB)", mem);
		pushFlowVariableDouble("Max. Available Memory (MB)", mem);
		pushFlowVariableDouble("Currently Allocated Memory (MB)", mem);
		pushFlowVariableDouble("Used Memory (MB)", mem);
		pushFlowVariableDouble("Free Memory (MB)", mem);
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
		// nothing

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
		// nothing

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#saveSettingsTo(org.knime.core.node.
	 * NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		// nothing

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
		// nothing

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
		// nothing

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#reset()
	 */
	@Override
	protected void reset() {
		// nothing
	}

}
