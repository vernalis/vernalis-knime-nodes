/*******************************************************************************
 * Copyright (c) 2014, Vernalis (R&D) Ltd
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
 *******************************************************************************/
package com.vernalis.knime.flowcontrol.nodes.timedloops.varloopend;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
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
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.LoopEndNode;
import org.knime.core.node.workflow.LoopStartNodeTerminator;

import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeModel;

/**
 * Variable Timed Loop End NodeModel implementatin
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class VariableTimedLoopEndNodeModel extends NodeModel implements
		LoopEndNode {

	/** The logger. */
	NodeLogger logger = NodeLogger
			.getLogger(VariableTimedLoopEndNodeModel.class);

	/** The m_result container. */
	private BufferedDataContainer m_resultContainer;

	/** The m_iteration. */
	private long m_iteration = 0;

	/**
	 * Constructor for the VariableTimedLoopEndNodeModel.
	 */
	protected VariableTimedLoopEndNodeModel() {
		super(new PortType[] { FlowVariablePortObject.TYPE }, new PortType[] {
				BufferedDataTable.TYPE, BufferedDataTable.TYPE });
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
		// Check for a loop start node of some sort
		if (!(this.getLoopStartNode() instanceof LoopStartNodeTerminator)) {
			throw new IllegalStateException(
					"No matching loop start node found!");
		}

		// Now need to check that the loop start is a timed loop start
		if (!(this.getLoopStartNode() instanceof AbstractTimedLoopStartNodeModel)) {
			throw new IllegalStateException(
					"Start loop is of wrong type - must be a "
							+ "'Run-to-time' or 'Run-for-time' Loop Start node");
		}

		DataTableSpec currentSpec = (DataTableSpec) createVariableTableSpec();

		if (m_resultContainer == null) {
			// First iteration
			m_resultContainer = exec.createDataContainer(currentSpec);
		} else if (!currentSpec
				.equalStructure(m_resultContainer.getTableSpec())) {
			throw new Exception("Variable tables have changed during execution");
		}

		// Now add the row to the table
		RowKey rowId = new RowKey("Row_" + m_iteration++);
		DataCell[] cells = variablesToCells(currentSpec);
		m_resultContainer.addRowToTable(new DefaultRow(rowId, cells));

		// Now check whether we are at the end of the loop
		// Get the loop start node
		AbstractTimedLoopStartNodeModel loopStartModel = (AbstractTimedLoopStartNodeModel) this
				.getLoopStartNode();
		if (loopStartModel.terminateLoop()) {

			m_resultContainer.close();
			// And add the iteration as a flow variable - the iteration counter
			// will be one higher!
			pushFlowVariableInt("Last Iteration",
					(int) (loopStartModel.getIteration() - 1));
			pushFlowVariableString("End Time", loopStartModel.getEndTime()
					.toString());

			// get the table
			BufferedDataTable table = m_resultContainer.getTable();
			// Finally, do a reset to restore the interation counter and result
			// container
			reset();

			return new PortObject[] { table,
					loopStartModel.getUnprocessedRows(exec) };
		} else {
			continueLoop();
			return new BufferedDataTable[2];
		}

	}

	/**
	 * Converts the input {@link FlowVariable}s to {@link DataCell}s.
	 * 
	 * @param tableSpec
	 *            the table spec
	 * @return the {@link DataCell}[] containing the variables
	 * @throws Exception
	 */
	private DataCell[] variablesToCells(final DataTableSpec tableSpec)
			throws Exception {
		// Somewhere to put the cells
		DataCell[] cells = new DataCell[tableSpec.getNumColumns()];

		// The input variables (because the node generates some of it's own,
		// which will
		// Cause an exception otherwise!
		Map<String, FlowVariable> flowVars = getAvailableInputFlowVariables();
		for (Entry<String, FlowVariable> entVar : flowVars.entrySet()) {
			String varName = entVar.getKey();
			if (!varName
					.startsWith(FlowVariable.Scope.Global.getPrefix() + ".")) {
				// skip core knime variables

				// Find the column for the current variable -they may change
				// order!
				int colIdx = tableSpec.findColumnIndex(varName);
				FlowVariable fvar = entVar.getValue();
				FlowVariable.Type fvarType = fvar.getType();
				switch (fvarType) {
				case INTEGER:
					cells[colIdx] = new IntCell(fvar.getIntValue());
					break;
				case DOUBLE:
					cells[colIdx] = new DoubleCell(fvar.getDoubleValue());
					break;
				case STRING:
					cells[colIdx] = new StringCell(fvar.getStringValue());
					break;
				default:
					throw new Exception("Unknown variable type");
				}
			}
		}

		return cells;
	}

	/**
	 * Configure method. Attempts to create table specs for the output tables
	 * and create some variable placeholders.
	 * 
	 * @param inSpecs
	 *            the in specs
	 * @return the port object spec[]
	 * @throws InvalidSettingsException
	 *             the invalid settings exception
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {

		PortObjectSpec[] outSpecs = new PortObjectSpec[2];
		// Get the spec based on the flow variables
		outSpecs[0] = createVariableTableSpec();

		// Get the spec for the unprocessed rows
		// Now need to check that the loop start is a timed loop start
		if (this.getLoopStartNode() instanceof AbstractTimedLoopStartNodeModel) {
			outSpecs[1] = ((AbstractTimedLoopStartNodeModel) this
					.getLoopStartNode()).getInSpec();
		}

		pushFlowVariableInt("Last Iteration", 0);
		pushFlowVariableString("End Time", "");
		return outSpecs;
	}

	/**
	 * Create a table spec based on the available flow variables.
	 * 
	 * @return the port object spec
	 * @throws InvalidSettingsException
	 *             the invalid settings exception
	 */
	private PortObjectSpec createVariableTableSpec()
			throws InvalidSettingsException {
		// Use available input variables to not throw exceptions at completion
		// due to variables added by node (which we dont want to include in the
		// output table anyway!)
		Map<String, FlowVariable> fvars = getAvailableInputFlowVariables();
		ArrayList<DataColumnSpec> colSpecs = new ArrayList<DataColumnSpec>();
		for (Entry<String, FlowVariable> fvarEnt : fvars.entrySet()) {
			String fname = fvarEnt.getKey();
			if (!fname.startsWith(FlowVariable.Scope.Global.getPrefix() + ".")) {
				// Only process non-core variables
				FlowVariable fvar = fvarEnt.getValue();
				FlowVariable.Type fvType = fvar.getType();
				DataColumnSpecCreator newColSpec;
				switch (fvType) {
				case INTEGER:
					newColSpec = new DataColumnSpecCreator(fname, IntCell.TYPE);
					break;
				case DOUBLE:
					newColSpec = new DataColumnSpecCreator(fname,
							DoubleCell.TYPE);
					break;
				case STRING:
					newColSpec = new DataColumnSpecCreator(fname,
							StringCell.TYPE);
					break;
				default:
					throw new InvalidSettingsException("Unknown Variable Type");
				}
				colSpecs.add(newColSpec.createSpec());
			}
		}
		return new DataTableSpec(colSpecs.toArray(new DataColumnSpec[0]));
	}

	/** {@inheritDoc} */
	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Do nothing

	}

	/** {@inheritDoc} */
	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Do nothing

	}

	/** {@inheritDoc} */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		// No Settings

	}

	/** {@inheritDoc} */
	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		// No Settings

	}

	/** {@inheritDoc} */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		// No Settings

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#reset()
	 */
	@Override
	protected void reset() {
		m_resultContainer = null;
		m_iteration = 0;
	}

}
