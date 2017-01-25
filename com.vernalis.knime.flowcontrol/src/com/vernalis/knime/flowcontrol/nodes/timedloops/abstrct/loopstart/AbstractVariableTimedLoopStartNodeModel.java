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
package com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart;

import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.DateFunctions.isNowAfter;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog.createMissingDblModel;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog.createMissingIntModel;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog.createMissingStrModel;
import static com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog.createOnMissingModel;

import java.util.Date;
import java.util.HashSet;

import org.knime.base.node.flowvariable.tablerowtovariable.TableToVariableNodeModel;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;
import org.knime.core.node.workflow.FlowVariable;

import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.TimedMissingValuePolicy;
import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.TimedNodeType;

// TODO: Auto-generated Javadoc
/**
 * <p>
 * This is the Abstract model implementation of
 * AbstractVariableTimedLoopStartNodeModel, which should be subclassed for
 * variable timed loop starts. This node provides additional methods for
 * handling the variable loops.
 * </p>
 * <p>
 * Subclasses should implement {@link RunForTime} or {@link RunToTime}.
 * </p>
 * 
 * @author "Stephen Roughley  knime@vernalis.com"
 */
public class AbstractVariableTimedLoopStartNodeModel extends
		AbstractTimedLoopStartNodeModel {

	// Settings models
	/** The m_on missing. */
	protected SettingsModelString m_onMissing = createOnMissingModel();

	/** The m_missing string. */
	protected SettingsModelString m_missingString = createMissingStrModel();

	/** The m_missing double. */
	protected SettingsModelDouble m_missingDouble = createMissingDblModel();

	/** The m_missing integer. */
	protected SettingsModelInteger m_missingInteger = createMissingIntModel();

	/** The m_skipped rows. */
	protected BufferedDataContainer m_skippedRows;

	/**
	 * Constructor for the node model. NB subclasses need to initialise m_logger
	 * 
	 * @param nodeType
	 *            the node type
	 */
	public AbstractVariableTimedLoopStartNodeModel(TimedNodeType nodeType) {
		super(new PortType[] { BufferedDataTable.TYPE },
				new PortType[] { FlowVariablePortObject.TYPE }, nodeType);
		synchronizeModelStati();
		m_logger = NodeLogger
				.getLogger(AbstractVariableTimedLoopStartNodeModel.class);
	}

	/**
	 * Synchronizes the stati of the node settings models on initialisation.
	 */
	private void synchronizeModelStati() {
		TimedMissingValuePolicy selOption = TimedMissingValuePolicy
				.valueOf(m_onMissing.getStringValue());
		if (selOption == TimedMissingValuePolicy.DEFAULT) {
			m_missingDouble.setEnabled(true);
			m_missingInteger.setEnabled(true);
			m_missingString.setEnabled(true);
		} else {
			m_missingDouble.setEnabled(false);
			m_missingInteger.setEnabled(false);
			m_missingString.setEnabled(false);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {

		m_iteration = m_ZerothIteration.getIntValue();

		// Calculate the endTime at config, but NB this may not be that used at
		// execution
		Date endTime = calculateEndTime();
		if (isNowAfter(endTime)) {
			// throw new
			// InvalidSettingsException("End time has already passed!");
			// Warn but dont throw exception as user may want to configure now
			// for later execution
			m_logger.warn("End time has already passed - node may fail on execution!");
			setWarningMessage("End time has already passed - node may fail on execution!");
		} else {
			m_logger.info("Loop execution will terminate after " + endTime);
			m_logger.info("NB This based on the current time.  "
					+ "The value will be re-calculated at execution start");
		}

		// Put the default values onto the variable stack if appropriate
		// Not sure this is the optimal behaviour regards the settings, but it
		// emulates that in the knime base tablerow to variable nodes.
		TimedMissingValuePolicy selOption = TimedMissingValuePolicy
				.valueOf(m_onMissing.getStringValue());
		if (selOption != TimedMissingValuePolicy.SKIP_ADDTOUNPROCESSED
				&& selOption != TimedMissingValuePolicy.SKIP) {
			pushDefaultVariables(inSpecs[0]);
		}

		pushFlowVariableInt("currentIteration", m_iteration);
		pushFlowVariableInt("maxIterations", 0);
		pushFlowVariableString("endTime", endTime.toString());
		m_inspec = (DataTableSpec) inSpecs[0];
		return new PortObjectSpec[] { FlowVariablePortObjectSpec.INSTANCE };
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation should be OK to use in most cases.
	 * </p>
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData,
			final ExecutionContext exec) throws Exception {
		BufferedDataTable table = (BufferedDataTable) inData[0];
		long rowCount = table.size();

		if (m_iteration == m_ZerothIteration.getIntValue()) {
			// First iteration - set the end time and tables
			assert getLoopEndNode() == null : "1st iteration but end node set";
			m_table = table;
			m_iterator = table.iterator();
			m_endTime = calculateEndTime();
			pushFlowVariableString("endTime", m_endTime.toString());
			m_logger.info("Loop execution will terminate after " + m_endTime);
			if (isNowAfter(m_endTime)) {
				throw new Exception(
						"No rows executed as end time has already passed");
			}
			m_skippedRows = exec
					.createDataContainer(m_table.getDataTableSpec());
		} else {
			// Just some assertions for second iteration and beyond
			assert getLoopEndNode() != null : "No end node set";
			assert table == m_table : "Input table changed between iterations";
		}
		if (rowCount > 0) {
			// Now get the next row from the table
			DataRow row = m_iterator.next();
			// Now process the row - either it goes onto the variable stack, or
			// it
			// is saved for the loop end
			boolean addToUnprocessedRows = pushVariables(
					m_table.getDataTableSpec(), row);
			if (addToUnprocessedRows) {
				m_skippedRows.addRowToTable(row);
				// TODO: Maybe try again here instead of pushing the row anyway?
			}
		}
		// Update the loop counters
		pushFlowVariableInt("currentIteration", m_iteration++);
		pushFlowVariableInt("maxIterations", (int) rowCount);
		pushFlowVariableString("endTime", m_endTime.toString());
		return new PortObject[] { FlowVariablePortObject.INSTANCE };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		super.saveSettingsTo(settings);
		m_onMissing.saveSettingsTo(settings);
		m_missingDouble.saveSettingsTo(settings);
		m_missingInteger.saveSettingsTo(settings);
		m_missingString.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		super.loadValidatedSettingsFrom(settings);
		m_onMissing.loadSettingsFrom(settings);
		m_missingDouble.loadSettingsFrom(settings);
		m_missingInteger.loadSettingsFrom(settings);
		m_missingString.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		super.validateSettings(settings);
		m_onMissing.validateSettings(settings);
		m_missingDouble.validateSettings(settings);
		m_missingInteger.validateSettings(settings);
		m_missingString.validateSettings(settings);
	}

	/**
	 * This puts the variables, stored in a DataRow to the flow variable stack.
	 * 
	 * @param variablesSpec
	 *            The Spec (to retrieve names and types)
	 * @param currentVariables
	 *            The current values
	 * @return Returns true if the row needs to be added to the unprocessed rows
	 *         table
	 * @throws Exception
	 *             the exception
	 * @see {@link TableToVariableNodeModel}
	 */
	protected boolean pushVariables(final DataTableSpec variablesSpec,
			final DataRow currentVariables) throws Exception {
		// Start by putting the RowID onto the stack
		final String rowIDName = "RowID";
		pushFlowVariableString(rowIDName, currentVariables == null ? ""
				: currentVariables.getKey().getString());

		final boolean fail = m_onMissing.getStringValue().equals(
				TimedMissingValuePolicy.FAIL);
		final boolean defaults = m_onMissing.getStringValue().equals(
				TimedMissingValuePolicy.DEFAULT);
		final boolean addSkippedToUnproc = m_onMissing.getStringValue().equals(
				TimedMissingValuePolicy.SKIP_ADDTOUNPROCESSED);
		boolean retVal = false;

		final DataCell[] defaultCells = createDefaultCells(variablesSpec);

		// Column names which start with "knime." need to be uniquified
		String internalNamePrefix = FlowVariable.Scope.Global.getPrefix() + ".";
		final HashSet<String> varNames = new HashSet<String>();
		varNames.add(rowIDName);
		final int colCnt = variablesSpec.getNumColumns();
		for (int i = colCnt; --i >= 0;) {
			DataColumnSpec spec = variablesSpec.getColumnSpec(i);
			DataType colType = spec.getType();

			// Sort out the name first
			String colName = spec.getName();
			if (colName.equals(internalNamePrefix)) {
				colName = "column_" + i;
			} else if (colName.startsWith(internalNamePrefix)) {
				colName = colName.substring(internalNamePrefix.length());
			}
			int uniquifier = 1;
			String baseName = colName;
			while (!varNames.add(colName)) {
				colName = baseName + "(#" + (uniquifier++) + ")";
			}

			// Now sort out the value
			final DataCell cell;
			if (currentVariables == null
					|| currentVariables.getCell(i).isMissing()) {
				// A missing row or a row with missing values
				if (fail) {
					throw new Exception(
							(currentVariables == null) ? "No rows in input table"
									: "Missing values are not allowed (Row ID: "
											+ currentVariables.getKey()
													.getString()
											+ "; Column \"" + baseName + "\")");
				} else if (defaults) {
					cell = defaultCells[i];
				} else {
					// Omit, and set the return flag
					cell = null;
					retVal = addSkippedToUnproc;
				}
			} else {
				// Use the actual value!
				cell = currentVariables.getCell(i);
			}

			// Now convert the cell to the correct variable type
			if (cell != null) {
				if (colType.isCompatible(IntValue.class)) {
					pushFlowVariableInt(colName,
							((IntValue) cell).getIntValue());
				} else if (colType.isCompatible(DoubleValue.class)) {
					pushFlowVariableDouble(colName,
							((DoubleValue) cell).getDoubleValue());
				} else if (colType.isCompatible(StringValue.class)) {
					pushFlowVariableString(colName,
							((StringValue) cell).getStringValue());
				}
			}
		}

		return retVal;
	}

	/**
	 * Put the default variable values onto the flow variable stack.
	 * 
	 * @param portObjectSpec
	 *            The PortObject Spec which contains the columns to convert to
	 *            variables
	 */
	protected void pushDefaultVariables(PortObjectSpec portObjectSpec) {
		DataTableSpec variablesSpec = (DataTableSpec) portObjectSpec;
		final DefaultRow row = new DefaultRow("",
				createDefaultCells(variablesSpec));
		try {
			pushVariables(variablesSpec, row);
		} catch (Exception e) {
			// Do nothing -
		}

	}

	/**
	 * Private method to create default cell values based on the missing value
	 * settings.
	 * 
	 * @param variablesSpec
	 *            the variables spec
	 * @return the data cell[]
	 */
	private DataCell[] createDefaultCells(DataTableSpec variablesSpec) {
		final DataCell[] cells = new DataCell[variablesSpec.getNumColumns()];
		for (int i = cells.length; --i >= 0;) {
			final DataColumnSpec c = variablesSpec.getColumnSpec(i);
			if (c.getType().isCompatible(IntValue.class)) {
				cells[i] = new IntCell(m_missingInteger.getIntValue());
			} else if (c.getType().isCompatible(DoubleValue.class)) {
				cells[i] = new DoubleCell(m_missingDouble.getDoubleValue());
			} else {
				cells[i] = new StringCell(m_missingString.getStringValue());
			}
		}
		return cells;

	}

	/**
	 * <p>
	 * Call this method from the loop end node to retrieve unprocessed rows
	 * </p>
	 * <p>
	 * This amended version also adds any rows skipped during execution,
	 * according to the missing values behaviour
	 * </p>
	 * .
	 * 
	 * @param exec
	 *            the exec
	 * @return the unprocessed rows
	 * @throws CanceledExecutionException
	 *             the canceled execution exception
	 */
	@Override
	public BufferedDataTable getUnprocessedRows(ExecutionContext exec)
			throws CanceledExecutionException {
		// Here we use the m_skipped Rows to generate the table. This will have
		// been initiated, but may or may not already contain any rows
		exec.setMessage("Retrieving unprocessed rows...");
		long rowsToRetrieve = m_table.size()
				- ((m_iteration - m_ZerothIteration.getIntValue()) + 1);
		long rowcnt = 0;
		while (m_iterator.hasNext()) {
			exec.setProgress((double) rowcnt / rowsToRetrieve,
					"Retrieving unprocessed row " + rowcnt++ + " of "
							+ rowsToRetrieve);
			exec.checkCanceled();
			m_skippedRows.addRowToTable(m_iterator.next());
		}
		m_skippedRows.close();
		return m_skippedRows.getTable();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation also clears the data container with the
	 * skipped rows in
	 * </p>
	 */
	@Override
	protected void reset() {
		super.reset();
		if (m_skippedRows != null) {
			m_skippedRows.close();
		}
		m_skippedRows = null;
	}

}
