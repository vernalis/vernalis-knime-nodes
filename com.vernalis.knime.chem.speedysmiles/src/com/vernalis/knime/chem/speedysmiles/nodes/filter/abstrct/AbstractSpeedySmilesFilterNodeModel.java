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
package com.vernalis.knime.chem.speedysmiles.nodes.filter.abstrct;

import static com.vernalis.knime.chem.speedysmiles.nodes.abstrct.AbstractSpeedySmilesNodeDialog.createColumnNameModel;
import static com.vernalis.knime.chem.speedysmiles.nodes.filter.abstrct.AbstractSpeedySmilesFilterNodeDialog.createKeepMatchingModel;
import static com.vernalis.knime.chem.speedysmiles.nodes.filter.abstrct.AbstractSpeedySmilesFilterNodeDialog.createKeepMissingModel;

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
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
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

import com.vernalis.knime.chem.speedysmiles.helpers.SmilesHelpers;

/**
 * This is the base node model class for the SpeedySMILES filter/splitter nodes.
 * It implements the KNIME Streaming API to allow streaming execution where
 * available
 * 
 * @author S Roughley
 */
public abstract class AbstractSpeedySmilesFilterNodeModel extends NodeModel {
	/** The node logger instance */
	protected NodeLogger m_logger = NodeLogger.getLogger(this.getClass());

	protected final SettingsModelString m_colName = createColumnNameModel();
	protected final SettingsModelBoolean m_keepMissing = createKeepMissingModel();
	protected final SettingsModelBoolean m_keepMatches = createKeepMatchingModel();

	protected final boolean isSplitter;

	private BufferedDataContainer[] previewContainers;

	/**
	 * Constructor for the node model.
	 * 
	 * @param isSplitter
	 *            if <code>true</code> then 2 outputs, otherwise 1
	 */
	protected AbstractSpeedySmilesFilterNodeModel(boolean isSplitter) {
		super(1, isSplitter ? 2 : 1);
		this.isSplitter = isSplitter;
	}

	/**
	 * Method to check if row is to be kept. The result of the method will be
	 * combined with the keepMatches setting from the node dialog using !(a^b)
	 * to determine whether row is kept or discarded
	 * 
	 * @param smilesValue
	 * @return <code>true</code> if the row matches the filter
	 */
	protected abstract boolean rowMatches(String smi);

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		try {
			String msg = SmilesHelpers.findSmilesColumn(inSpecs[0], m_colName);
			if (msg != null) {
				m_logger.warn(msg);
			}
		} catch (InvalidSettingsException e) {
			m_logger.error(e.getMessage());
			throw e;
		}
		DataTableSpec[] retVal = new DataTableSpec[isSplitter ? 2 : 1];
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
		int smiColIdx = inTable.getDataTableSpec().findColumnIndex(m_colName.getStringValue());
		// Create the output containers
		BufferedDataContainer[] bdc = new BufferedDataContainer[isSplitter ? 2 : 1];
		previewContainers = new BufferedDataContainer[isSplitter ? 2 : 1];
		bdc[0] = exec.createDataContainer(inTable.getDataTableSpec());
		previewContainers[0] = exec.createDataContainer(inTable.getDataTableSpec());
		BufferedDataTableRowOutput keeps = new BufferedDataTableRowOutput(bdc[0]);
		BufferedDataTableRowOutput drops;
		if (isSplitter) {
			bdc[1] = exec.createDataContainer(inTable.getDataTableSpec());
			drops = new BufferedDataTableRowOutput(bdc[1]);
			previewContainers[1] = exec.createDataContainer(inTable.getDataTableSpec());
		} else {
			drops = null;
		}
		this.execute(inputRow, keeps, drops, smiColIdx, inTable.size(), exec);
		return (isSplitter) ? new BufferedDataTable[] { keeps.getDataTable(), drops.getDataTable() }
				: new BufferedDataTable[] { keeps.getDataTable() };
	}

	protected void execute(final RowInput inRow, RowOutput keeps, RowOutput drop, int smiColIdx,
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

			String smi = SmilesHelpers.getSmilesFromCell(row.getCell(smiColIdx));
			if (smi == null) {
				if (m_keepMissing.getBooleanValue()) {
					keeps.push(row);
				} else if (drop != null) {
					drop.push(row);
				}
			}

			if (rowMatches(smi) == m_keepMatches.getBooleanValue()) {
				keeps.push(row);
			} else if (drop != null) {
				drop.push(row);
			}
		}
		keeps.close();
		if (drop != null) {
			drop.close();
		}
	}

	@Override
	public StreamableOperator createStreamableOperator(final PartitionInfo partitionInfo,
			final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		int smiColIdx = ((DataTableSpec) inSpecs[0]).findColumnIndex(m_colName.getStringValue());

		return new StreamableOperator() {

			@Override
			public void runFinal(PortInput[] inputs, PortOutput[] outputs, ExecutionContext exec)
					throws Exception {
				previewContainers = new BufferedDataContainer[isSplitter ? 2 : 1];
				previewContainers[0] = exec
						.createDataContainer(((RowInput) inputs[0]).getDataTableSpec());
				if (isSplitter) {
					previewContainers[1] = exec
							.createDataContainer(((RowInput) inputs[0]).getDataTableSpec());
				}
				// Run it - dont know row count!
				AbstractSpeedySmilesFilterNodeModel.this.execute((RowInput) inputs[0],
						(RowOutput) outputs[0], isSplitter ? (RowOutput) outputs[1] : null,
						smiColIdx, -1, exec);

			}

		};
	}

	@Override
	public InputPortRole[] getInputPortRoles() {
		return new InputPortRole[] { InputPortRole.DISTRIBUTED_STREAMABLE };
	}

	@Override
	public OutputPortRole[] getOutputPortRoles() {
		OutputPortRole[] retVal = new OutputPortRole[isSplitter ? 2 : 1];
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
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_colName.saveSettingsTo(settings);
		m_keepMissing.saveSettingsTo(settings);
		m_keepMatches.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_colName.loadSettingsFrom(settings);
		m_keepMissing.loadSettingsFrom(settings);
		m_keepMatches.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_colName.validateSettings(settings);
		m_keepMissing.validateSettings(settings);
		m_keepMatches.validateSettings(settings);
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
