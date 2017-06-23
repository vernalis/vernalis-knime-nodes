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
package com.vernalis.knime.chem.speedysmiles.nodes.manip.stereo.enumerate;

import static com.vernalis.knime.chem.speedysmiles.nodes.abstrct.AbstractSpeedySmilesNodeDialog.createColumnNameModel;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.knime.chem.types.SmilesCellFactory;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
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
 * Node Model Implementation for the for the SpeedySMILES Stereoenumerate node
 * 
 * @author S Roughley
 */
public class SimpleSpeedySmilesStereoenumerateNodeModel extends NodeModel {
	/** The node logger instance */
	protected NodeLogger m_logger = NodeLogger.getLogger(this.getClass());

	protected final SettingsModelString m_colName = createColumnNameModel();

	/**
	 * Constructor for the node model.
	 *
	 */
	protected SimpleSpeedySmilesStereoenumerateNodeModel() {
		super(1, 1);

	}

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
		DataColumnSpec newColSpec = new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(
				inSpecs[0], m_colName.getStringValue() + " Enumerated Isomers"),
				SmilesCellFactory.TYPE).createSpec();
		DataTableSpecCreator specFact = new DataTableSpecCreator(inSpecs[0]);
		specFact.addColumns(newColSpec);
		return new DataTableSpec[] { specFact.createSpec() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		DataTableSpec inSpec = inData[0].getDataTableSpec();
		DataColumnSpec newColSpec = new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(
				inSpec, m_colName.getStringValue() + " Enumerated Isomers"), SmilesCellFactory.TYPE)
						.createSpec();
		DataTableSpecCreator specFact = new DataTableSpecCreator(inSpec);
		specFact.addColumns(newColSpec);
		DataTableSpec outSpec = specFact.createSpec();

		BufferedDataTable inTable = inData[0];
		RowInput inputRow = new DataTableRowInput(inTable);

		int smiColIdx = inTable.getDataTableSpec().findColumnIndex(m_colName.getStringValue());
		// Create the output containers
		BufferedDataContainer bdc = exec.createDataContainer(outSpec);
		RowOutput outputRow = new BufferedDataTableRowOutput(bdc);

		this.execute(inputRow, outputRow, smiColIdx, inTable.size(), exec);
		return new BufferedDataTable[] { bdc.getTable() };
	}

	protected void execute(final RowInput inRow, RowOutput outputRow, int smiColIdx,
			final long numRows, final ExecutionContext exec)
			throws InterruptedException, CanceledExecutionException, ExecutionException {

		long rowIdx = 0;
		DataRow row;
		while ((row = inRow.poll()) != null) {
			if (numRows > 0) {
				exec.setProgress((++rowIdx) / (double) numRows,
						"Enumerating row " + rowIdx + " of " + numRows);
			} else {
				exec.setProgress("Enumerating row " + rowIdx);
			}
			exec.checkCanceled();
			String smi = SmilesHelpers.getSmilesFromCell(row.getCell(smiColIdx));
			SmilesHelpers.enumerateLabelledStereoisomers(smi, row, outputRow, exec);
		}
		outputRow.close();

	}

	@Override
	public StreamableOperator createStreamableOperator(final PartitionInfo partitionInfo,
			final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		int smiColIdx = ((DataTableSpec) inSpecs[0]).findColumnIndex(m_colName.getStringValue());

		return new StreamableOperator() {

			@Override
			public void runFinal(PortInput[] inputs, PortOutput[] outputs, ExecutionContext exec)
					throws Exception {

				// Run it - dont know row count!
				SimpleSpeedySmilesStereoenumerateNodeModel.this.execute((RowInput) inputs[0],
						(RowOutput) outputs[0], smiColIdx, -1, exec);

			}

		};
	}

	@Override
	public InputPortRole[] getInputPortRoles() {
		return new InputPortRole[] { InputPortRole.DISTRIBUTED_STREAMABLE };
	}

	@Override
	public OutputPortRole[] getOutputPortRoles() {
		return new OutputPortRole[] { OutputPortRole.DISTRIBUTED };
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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_colName.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_colName.validateSettings(settings);
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
