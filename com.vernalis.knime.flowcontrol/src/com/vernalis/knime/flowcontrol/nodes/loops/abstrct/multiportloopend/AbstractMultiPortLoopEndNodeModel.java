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
package com.vernalis.knime.flowcontrol.nodes.loops.abstrct.multiportloopend;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.knime.base.data.append.column.AppendedColumnRow;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
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
import org.knime.core.node.port.inactive.InactiveBranchPortObject;
import org.knime.core.node.port.inactive.InactiveBranchPortObjectSpec;
import org.knime.core.node.workflow.LoopEndNode;
import org.knime.core.node.workflow.LoopStartNodeTerminator;

import com.vernalis.knime.flowcontrol.FlowControlHelpers;

/**
 * This is the model implementation of AbstractMultiPortLoopEnd. Loop end node
 * to handle optional input ports n
 * 
 * @author S. Roughley
 */
public class AbstractMultiPortLoopEndNodeModel extends NodeModel implements
		LoopEndNode {

	/** the logger instance. */
	private static final NodeLogger logger = NodeLogger
			.getLogger(AbstractMultiPortLoopEndNodeModel.class);

	// Settings Keys
	/** The Constant NON_CONNECTED_OPTION. */
	static final String NON_CONNECTED_OPTION = "Non-Connected_Branch_Behaviour";

	/** The m_ num ports. */
	private final int m_NumPorts;

	/** The m_result container. */
	private BufferedDataContainer[] m_resultContainer;

	/** The m_current iteration. */
	private int m_currentIteration = 0;

	/** The m_settings. */
	private final AbstractMultiPortLoopEndSettings m_settings;

	/** The m_empty table. */
	private BufferedDataTable[] m_emptyTable;

	/** The m_specs. */
	private PortObjectSpec[] m_specs;

	/**
	 * Single argument constructor for the model, specifying the number of
	 * ports. If there are more than two ports, all except the first are
	 * optional.
	 * 
	 * @param numPorts
	 *            The number of dataports
	 */
	public AbstractMultiPortLoopEndNodeModel(final int numPorts) {

		super(FlowControlHelpers.createEndInPorts(BufferedDataTable.TYPE,
				numPorts), FlowControlHelpers.createStartOutPorts(
				BufferedDataTable.TYPE, numPorts));
		m_NumPorts = numPorts;
		m_resultContainer = new BufferedDataContainer[numPorts];
		m_settings = new AbstractMultiPortLoopEndSettings(numPorts);
		m_emptyTable = new BufferedDataTable[numPorts];
	}

	/**
	 * Constructor for the model, specifying the number of ports and whether any
	 * are optional. If there are more than two ports, and
	 * {@link optionalInPorts} are specified, then all except the first are
	 * optional
	 * 
	 * @param numPorts
	 *            The number of dataports
	 * @param optionalInPorts
	 *            Boolean specifying whether the in-ports are optional when
	 *            there {@link numPorts} > 2. Omitting this argument treats it
	 *            as 'true'
	 */
	public AbstractMultiPortLoopEndNodeModel(final int numPorts,
			final boolean optionalInPorts) {

		super((optionalInPorts) ? FlowControlHelpers.createEndInPorts(
				BufferedDataTable.TYPE, numPorts) : FlowControlHelpers
				.createStartOutPorts(BufferedDataTable.TYPE, numPorts),
				FlowControlHelpers.createStartOutPorts(BufferedDataTable.TYPE,
						numPorts));
		m_NumPorts = numPorts;
		m_resultContainer = new BufferedDataContainer[numPorts];
		m_settings = new AbstractMultiPortLoopEndSettings(numPorts);
		m_emptyTable = new BufferedDataTable[numPorts];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData,
			final ExecutionContext exec) throws Exception {
		if (!(this.getLoopStartNode() instanceof LoopStartNodeTerminator)) {
			throw new IllegalStateException(
					"No matching loop start node found!");
		}

		// final IntCell currIterCell = new IntCell(m_currentIteration);

		// Handle whatever data tables we find in the current iteration
		for (int i = 0; i < m_NumPorts; i++) {
			if (inData[i] != null) {
				if (m_settings.ignoreEmptyTables(i)
						&& ((BufferedDataTable) inData[i]).getRowCount() < 1) {
					if (m_emptyTable[i] == null) {
						m_emptyTable[i] = (BufferedDataTable) inData[i];
					}
				} else if (m_resultContainer[i] == null) {
					m_resultContainer[i] = exec
							.createDataContainer(createOutputTableSpec(inData[i]));
				}

				if (!m_settings.ignoreEmptyTables(i)
						|| ((BufferedDataTable) inData[i]).getRowCount() > 0) {
					if (!(createOutputTableSpec(inData[i]))
							.equalStructure(m_resultContainer[i].getTableSpec())) {
						// The output table does not match the existing spec
						String error = getComparisonMessage(
								createOutputTableSpec(inData[i]),
								m_resultContainer[i].getTableSpec(), i);
						for (String errorLine : error.split(";[\\s]*")) {
							// Report the problem(s) to the console in a
							// multi-line format
							logger.error(errorLine);
						}
						// And throw an error
						throw new IllegalArgumentException(error);
					}
					for (DataRow row : (BufferedDataTable) inData[i]) {
						// Now we add the rows from the current iteration to the
						// output port
						AppendedColumnRow newRow = getNewRow(row);
						m_resultContainer[i].addRowToTable(newRow);
					}
				}
			} else {
				// Put an empty table in the output for missing ports
				m_resultContainer[i] = exec
						.createDataContainer(new DataTableSpec());
			}
		}

		// Now check whether we are at the end of the loop
		if (((LoopStartNodeTerminator) this.getLoopStartNode()).terminateLoop()) {
			final PortObject[] outTables = new PortObject[m_NumPorts];
			for (int i = 0; i < m_NumPorts; i++) {
				if (inData[i] == null) {
					if (m_settings.inactivateDisconnectedBranches()) {
						outTables[i] = InactiveBranchPortObject.INSTANCE;
					} else {
						m_resultContainer[i].close();
						outTables[i] = m_resultContainer[i].getTable();
					}
				} else {
					if (m_settings.ignoreEmptyTables(i)
							&& m_resultContainer[i] == null) {
						outTables[i] = m_emptyTable[i];
					} else {
						m_resultContainer[i].close();
						outTables[i] = m_resultContainer[i].getTable();
					}
				}
			}

			// Finally, do a reset to restore the interation counter and result
			// container
			reset();
			return outTables;
		} else {
			continueLoop();
			m_currentIteration++;
			return new BufferedDataTable[m_NumPorts];
		}

	}

	/**
	 * Utility function to generate a new output {@link DataRow} taking into
	 * account the addIterationColumn and uniqueRowIDs settings.
	 * 
	 * @param row
	 *            {@link DataRow} to generate new {@link DataRow} from
	 * @return the new output {@link DataRow}
	 */
	private AppendedColumnRow getNewRow(final DataRow row) {
		RowKey newRowKey = getNewRowKey(row);
		return new AppendedColumnRow(newRowKey, row, new DefaultRow(newRowKey,
				new IntCell(m_currentIteration)),
				new boolean[] { m_settings.addIterationColumn() });
	}

	/**
	 * Utility function to get a new {@link RowKey}, taking into account the
	 * uniqueRowIDs setting.
	 * 
	 * @param row
	 *            {@link DataRow} to generate new {@link RowKey} from
	 * @return the new {@link RowKey}
	 */
	private RowKey getNewRowKey(final DataRow row) {
		return (m_settings.uniqueRowIDs()) ? new RowKey(row.getKey() + "_Iter#"
				+ m_currentIteration) : row.getKey();
	}

	/**
	 * Utility function to generate output table {@link DataTableSpec} from a
	 * {@link PortObjectSpec}, taking into account the addIterationColumn
	 * setting.
	 * 
	 * @param inSpec
	 *            the in spec
	 * @return The {@link DataTableSpec} for the output port
	 */
	private DataTableSpec createOutputTableSpec(final PortObjectSpec inSpec) {
		return (m_settings.addIterationColumn()) ? new DataTableSpec(
				(DataTableSpec) inSpec, new DataTableSpec(
						(new DataColumnSpecCreator(
								DataTableSpec.getUniqueColumnName(
										((DataTableSpec) inSpec), "Iteration"),
								IntCell.TYPE)).createSpec()))
				: ((DataTableSpec) inSpec);
	}

	/**
	 * Utility function to generate output table {@link DataTableSpec} from a
	 * {@link PortObject}, taking into account the addIterationColumn setting.
	 * 
	 * @param inData
	 *            The {@link PortObject}
	 * @return The {@link DataTableSpec} for the output port
	 */
	private DataTableSpec createOutputTableSpec(final PortObject inData) {
		PortObjectSpec inSpec = ((BufferedDataTable) inData).getDataTableSpec();
		return createOutputTableSpec(inSpec);
	}

	/**
	 * Utility function to generate meaningful description of the nature of a
	 * mis-match between two {@link DataTableSpec}s.
	 * 
	 * @param dataTableSpec0
	 *            The first {@link DataTableSpec}
	 * @param dataTableSpec1
	 *            The second {@link DataTableSpec}
	 * @param portIndex
	 *            the port index
	 * @return Description of the mis-match
	 */
	@SuppressWarnings("unchecked")
	private String getComparisonMessage(final DataTableSpec dataTableSpec0,
			final DataTableSpec dataTableSpec1, final int portIndex) {

		int colcnt0 = dataTableSpec0.getNumColumns();
		int colcnt1 = dataTableSpec1.getNumColumns();

		ArrayList<String> colNames0 = new ArrayList<String>(
				Arrays.asList(dataTableSpec0.getColumnNames()));
		ArrayList<String> colNames1 = new ArrayList<String>(
				Arrays.asList(dataTableSpec1.getColumnNames()));

		StringBuilder sb = new StringBuilder(
				"Tables have different specs; Iteration #")
				.append(m_currentIteration).append(", Port ").append(portIndex)
				.append("; ");

		boolean typesChanged = false;
		int movedCols = 0;
		// List moved columns including type changes different types and
		// re-typed columns
		for (String colName : colNames0) {
			// Index of colName in 1st table
			int colIndex0 = colNames0.indexOf(colName);
			// Index of colName in 2nd table - -1 if not present
			int colIndex1 = colNames1.indexOf(colName);
			if (colIndex0 != colIndex1 && colIndex1 >= 0) {
				// column has moved
				movedCols++;
				sb.append("Column moved: [").append(colName).append("] from ")
						.append(colIndex0);
				sb.append(getOrdinalSuffix(colIndex0)).append(" to ");
				sb.append(colIndex1).append(getOrdinalSuffix(colIndex1))
						.append(" position");
				// Now check the types are the same for the moved columns
				if (!dataTableSpec0.getColumnSpec(colName).equalStructure(
						dataTableSpec1.getColumnSpec(colName))) {
					sb.append(" (types also changed - ")
							.append(dataTableSpec0.getColumnSpec(colName)
									.getType())
							.append(" to ")
							.append(dataTableSpec1.getColumnSpec(colName)
									.getType()).append(")); ");
					typesChanged = true;
				} else {
					sb.append(" (types unchanged); ");
				}
			} else if (colNames0.indexOf(colName) == colNames1.indexOf(colName)) {
				// Check that the type is unchanged if the column has not moved
				if (!dataTableSpec0.getColumnSpec(colName).equalStructure(
						dataTableSpec1.getColumnSpec(colName))) {
					sb.append("Column re-typed: [")
							.append(colName)
							.append("] - ")
							.append(dataTableSpec0.getColumnSpec(colName)
									.getType())
							.append(" to ")
							.append(dataTableSpec1.getColumnSpec(colName)
									.getType()).append("; ");
					typesChanged = true;
				}
			}

		}

		if (colNames0.containsAll(colNames1)
				&& colNames1.containsAll(colNames0)) {
			// Simple cases....
			sb.append("Summary: ");
			if (movedCols > 0) {
				// Just a re-arrangement
				sb.append("Columns re-ordered");
				if (typesChanged) {
					// with or without re-typing
					sb.append(" with type changes");
				}
			} else {
				// Just a re-typing
				sb.append("Column type change(s)");
			}
			return sb.toString();
		}

		// Now we have something more complicated - which will be some added or
		// lost columns. We've already listed columns which have moved or been
		// re-typed
		// so now we need to deal with those lost or added
		// First deal with different numbers of columns
		if (colcnt0 != colcnt1) {
			sb.append("Tables have different number of columns: ")
					.append(colcnt0).append(" vs. ").append(colcnt1)
					.append("; ");
		}

		// List all lost columns
		ArrayList<String> temp = (ArrayList<String>) colNames0.clone();
		temp.removeAll(colNames1);
		for (String colName : temp) {
			sb.append("Column lost: ").append(colName).append("; ");
		}

		// Now list added columns
		temp = (ArrayList<String>) colNames1.clone();
		temp.removeAll(colNames0);
		for (String colName : temp) {
			sb.append("Column added: ").append(colName).append(" (")
					.append(dataTableSpec1.getColumnSpec(colName).getType())
					.append("); ");
		}
		sb.append("Summary: Complex changes to table structure");
		return sb.toString();
	}

	/**
	 * Routine to return correct ordinal number text suffix (th, st, nd, rd) for
	 * a given integer.
	 * 
	 * @param number
	 *            The number to process
	 * @return th, st, nd or rd, depending on the last digit of number
	 */
	private String getOrdinalSuffix(final int number) {
		if (number >= 11 & number <= 13) {
			// These are irregular and all are 'th'
			return "th";
		}
		switch (number % 10) {
		case 1:
			return "st";
		case 2:
			return "nd";
		case 3:
			return "rd";
		default:
			return "th";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// restore the interation counter and result container
		m_resultContainer = new BufferedDataContainer[m_NumPorts];
		m_currentIteration = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		m_specs = new PortObjectSpec[m_NumPorts];
		Arrays.fill(m_specs, null);
		for (int i = 0; i < m_NumPorts; i++) {
			if (inSpecs[i] != null) {
				if (!m_settings.ignoreEmptyTables(i)) {
					m_specs[i] = createOutputTableSpec(inSpecs[i]);
				}
			} else {
				m_specs[i] = (m_settings.inactivateDisconnectedBranches()) ? InactiveBranchPortObjectSpec.INSTANCE
						: new DataTableSpec();
			}
		}
		return m_specs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_settings.saveSettings(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_settings.loadSettings(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		// We just try loading
		AbstractMultiPortLoopEndSettings s = new AbstractMultiPortLoopEndSettings(
				m_NumPorts);
		s.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

}
