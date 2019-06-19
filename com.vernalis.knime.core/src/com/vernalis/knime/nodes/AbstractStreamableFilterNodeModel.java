/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.nodes;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.DataTableRowInput;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowInput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;

/**
 * This is the base node model class for the Streamable filter/splitter nodes.
 * Implementations should implement the {@link #rowMatches(DataRow)} and
 * {@link #checkIncomingTable(DataTableSpec)} methods. There is a
 * {@link NodeLogger} instance for each subclass accessible via
 * {@link #getNodeLogger()}. The splitter/filter nature of the node is
 * accessible via {@link #isSplitter()}
 * 
 * @author S Roughley
 */
public abstract class AbstractStreamableFilterNodeModel extends NodeModel {
	/** The node logger instance */
	private final NodeLogger m_logger = NodeLogger.getLogger(this.getClass());

	/**
	 * Indicator as to whether the node is a 'splitter' with two outputs, or a
	 * 'filter' with only a single output
	 */
	private final boolean isSplitter;

	/**
	 * Constructor for the node model.
	 * 
	 * @param isSplitter
	 *            if <code>true</code> then 2 outputs, otherwise 1
	 */
	protected AbstractStreamableFilterNodeModel(boolean isSplitter) {
		super(1, isSplitter ? 2 : 1);
		this.isSplitter = isSplitter;
	}

	/**
	 * Method to check if row is to be kept. Implementations should consider the
	 * fate of rows containing missing cells
	 * 
	 * @param row
	 *            The incoming data row
	 * @return <code>true</code> if the row matches the filter (in which case
	 *         the row will be kept)
	 */
	protected abstract boolean rowMatches(DataRow row);

	/**
	 * This method is called from the {@link #configure(DataTableSpec[])} method
	 * and should check required incoming column(s) settings and any other
	 * incoming settings
	 * 
	 * @param inSpec
	 *            The incoming {@link DataTableSpec}
	 * @throws InvalidSettingsException
	 *             If any of the settings are invalid or incompatible
	 */
	protected abstract void checkIncomingTable(DataTableSpec inSpec)
			throws InvalidSettingsException;

	/**
	 * @return the {@link NodeLogger} instance for the subclass
	 */
	protected NodeLogger getNodeLogger() {
		return m_logger;
	}

	/**
	 * Indicator as to whether the node is a 'splitter' with two outputs, or a
	 * 'filter' with only a single output
	 * 
	 * @return the isSplitter flag
	 */
	protected boolean isSplitter() {
		return isSplitter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		checkIncomingTable(inSpecs[0]);
		DataTableSpec[] retVal = new DataTableSpec[isSplitter() ? 2 : 1];
		Arrays.fill(retVal, inSpecs[0]);
		return retVal;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		BufferedDataTable inTable = inData[0];
		RowInput inputRow = new DataTableRowInput(inTable);

		// Create the output containers
		BufferedDataContainer[] bdc = new BufferedDataContainer[isSplitter() ? 2 : 1];
		bdc[0] = exec.createDataContainer(inTable.getDataTableSpec());
		RowOutput keeps = new BufferedDataTableRowOutput(bdc[0]);
		RowOutput drops;
		if (isSplitter()) {
			bdc[1] = exec.createDataContainer(inTable.getDataTableSpec());
			drops = new BufferedDataTableRowOutput(bdc[1]);
		} else {
			drops = null;
		}
		this.execute(inputRow, keeps, drops, inTable.size(), exec);
		return (isSplitter()) ? new BufferedDataTable[] { bdc[0].getTable(), bdc[1].getTable() }
				: new BufferedDataTable[] { bdc[0].getTable() };
	}

	/**
	 * Method to actually perform the table iteration, either via a call from
	 * {@link #execute(BufferedDataTable[], ExecutionContext)} or via the
	 * {@link StreamableOperator}
	 * 
	 * @param inRow
	 *            the supplier of incoming rows
	 * @param keeps
	 *            The output for kept rows
	 * @param drops
	 *            The optional output for dropped rows (<code>null</code> if the
	 *            node is a filter rather than a splitter)
	 * @param numRows
	 *            The number of rows for progress reporting during non-streaming
	 *            execution (use -1 when unknown)
	 * @param exec
	 *            The {@link ExecutionContext} to allow user cancelling and
	 *            progress reporting
	 * @throws InterruptedException
	 *             If another thread is cancelled
	 * @throws CanceledExecutionException
	 *             If the current thread is cancelled
	 */
	protected void execute(final RowInput inRow, RowOutput keeps, RowOutput drops,
			final long numRows, final ExecutionContext exec)
			throws InterruptedException, CanceledExecutionException {

		long rowIdx = 0;
		DataRow row;
		while ((row = inRow.poll()) != null) {
			if (numRows > 0) {
				exec.setProgress((++rowIdx) / (double) numRows,
						"Filtering row " + rowIdx + " of " + numRows);
			} else {
				exec.setProgress("Filtering row " + rowIdx);
			}
			exec.checkCanceled();
			if (rowMatches(row)) {
				keeps.push(row);
			} else if (drops != null) {
				drops.push(row);
			}
		}
		keeps.close();
		if (drops != null) {
			drops.close();
		}
	}

	@Override
	public StreamableOperator createStreamableOperator(final PartitionInfo partitionInfo,
			final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		return new StreamableOperator() {

			@Override
			public void runFinal(PortInput[] inputs, PortOutput[] outputs, ExecutionContext exec)
					throws Exception {
				// Run it - dont know row count!
				AbstractStreamableFilterNodeModel.this.execute((RowInput) inputs[0],
						(RowOutput) outputs[0], isSplitter() ? (RowOutput) outputs[1] : null, -1,
						exec);
			}
		};
	}

	@Override
	public InputPortRole[] getInputPortRoles() {
		return new InputPortRole[] { InputPortRole.DISTRIBUTED_STREAMABLE };
	}

	@Override
	public OutputPortRole[] getOutputPortRoles() {
		OutputPortRole[] retVal = new OutputPortRole[isSplitter() ? 2 : 1];
		Arrays.fill(retVal, OutputPortRole.DISTRIBUTED);
		return retVal;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

}
