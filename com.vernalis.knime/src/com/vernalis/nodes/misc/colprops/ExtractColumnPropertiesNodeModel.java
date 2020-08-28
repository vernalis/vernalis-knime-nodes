/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.misc.colprops;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.MergeOperator;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowInput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.core.node.streamable.StreamableOperatorInternals;
import org.knime.core.node.streamable.simple.SimpleStreamableOperatorInternals;

import static com.vernalis.nodes.misc.colprops.ExtractColumnPropertiesNodeDialog.createColumnsModel;

/**
 * The {@link NodeModel} implementation for the Extract Column Properties node
 * 
 * @author s.roughley
 *
 */
public class ExtractColumnPropertiesNodeModel extends NodeModel {

	/**
	 * Key for the column names array stored in the Streaming Internals object
	 */
	private static final String COL_NAMES = "Col_Names";

	/**
	 * The column name for the 'Property Value' column
	 */
	static final String PROPERTY_VALUE = "Property Value";

	/**
	 * The column name for the 'Property Name' column
	 */
	static final String PROPERTY_NAME = "Property Name";

	/**
	 * The column name for the 'Column Name' column
	 */
	static final String COLUMN_NAME = "Column Name";

	private final SettingsModelColumnFilter2 colNamesMdl = createColumnsModel();

	protected ExtractColumnPropertiesNodeModel() {
		super(1, 2);
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
		if (colNamesMdl.applyTo(inSpecs[0]).getIncludes().length < 1) {
			throw new InvalidSettingsException("No columns selected");
		}
		return new DataTableSpec[] { inSpecs[0], getOutputSpec() };
	}

	private DataTableSpec getOutputSpec() {
		DataTableSpecCreator tSpecFact = new DataTableSpecCreator();
		tSpecFact.setName("Column Properties");
		DataColumnSpec[] propColSpec = new DataColumnSpec[3];
		propColSpec[0] = new DataColumnSpecCreator(COLUMN_NAME, StringCell.TYPE)
				.createSpec();
		propColSpec[1] =
				new DataColumnSpecCreator(PROPERTY_NAME, StringCell.TYPE)
						.createSpec();
		propColSpec[2] =
				new DataColumnSpecCreator(PROPERTY_VALUE, StringCell.TYPE)
						.createSpec();
		return tSpecFact.addColumns(propColSpec).createSpec();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#execute(org.knime.core.node.
	 * BufferedDataTable[], org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {
		String[] colNames =
				colNamesMdl.applyTo(inData[0].getDataTableSpec()).getIncludes();
		BufferedDataContainer propDC =
				exec.createDataContainer(getOutputSpec());
		for (String colName : colNames) {
			long idx = 0L;
			DataColumnProperties colProps = inData[0].getDataTableSpec()
					.getColumnSpec(colName).getProperties();
			Enumeration<String> iter = colProps.properties();
			while (iter.hasMoreElements()) {
				String propName = iter.nextElement();
				DataCell[] cells = new DataCell[3];
				cells[0] = new StringCell(colName);
				cells[1] = new StringCell(propName);
				cells[2] = new StringCell(colProps.getProperty(propName));
				RowKey key = new RowKey(colName + "_" + (idx++));
				propDC.addRowToTable(new DefaultRow(key, cells));
			}
		}
		propDC.close();
		return new BufferedDataTable[] { inData[0], propDC.getTable() };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#getInputPortRoles()
	 */
	@Override
	public InputPortRole[] getInputPortRoles() {
		return new InputPortRole[] { InputPortRole.DISTRIBUTED_STREAMABLE };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#getOutputPortRoles()
	 */
	@Override
	public OutputPortRole[] getOutputPortRoles() {
		return new OutputPortRole[] { OutputPortRole.DISTRIBUTED,
				OutputPortRole.NONDISTRIBUTED };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#createInitialStreamableOperatorInternals()
	 */
	@Override
	public StreamableOperatorInternals createInitialStreamableOperatorInternals() {
		return new SimpleStreamableOperatorInternals();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#iterate(org.knime.core.node.streamable.
	 * StreamableOperatorInternals)
	 */
	@Override
	public boolean iterate(StreamableOperatorInternals internals) {
		// Need to iterate if we havent yet stored the column properties
		// return !((SimpleStreamableOperatorInternals) internals).getConfig()
		// .containsKey(COL_NAMES);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#createMergeOperator()
	 */
	@Override
	public MergeOperator createMergeOperator() {
		return new MergeOperator() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.knime.core.node.streamable.MergeOperator#mergeIntermediate(
			 * org.knime.core.node.streamable.StreamableOperatorInternals[])
			 */
			@Override
			public StreamableOperatorInternals mergeIntermediate(
					StreamableOperatorInternals[] operators) {
				return mergeFinal(operators);
			}

			@Override
			public StreamableOperatorInternals mergeFinal(
					StreamableOperatorInternals[] operators) {
				return operators[0];
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#finishStreamableExecution(org.knime.core.
	 * node.streamable.StreamableOperatorInternals,
	 * org.knime.core.node.ExecutionContext,
	 * org.knime.core.node.streamable.PortOutput[])
	 */
	@Override
	public void finishStreamableExecution(StreamableOperatorInternals internals,
			ExecutionContext exec, PortOutput[] output) throws Exception {

		// Write the output table of column properties stored in internals
		RowOutput propDC = (RowOutput) output[1];

		for (String colName : ((SimpleStreamableOperatorInternals) internals)
				.getConfig().getStringArray(COL_NAMES)) {
			long idx = 0L;
			DataColumnProperties colProps = DataColumnProperties
					.load(((SimpleStreamableOperatorInternals) internals)
							.getConfig().getConfig(colName));

			Enumeration<String> iter = colProps.properties();
			while (iter.hasMoreElements()) {
				String propName = iter.nextElement();
				DataCell[] cells = new DataCell[3];
				cells[0] = new StringCell(colName);
				cells[1] = new StringCell(propName);
				cells[2] = new StringCell(colProps.getProperty(propName));
				RowKey key = new RowKey(colName + "_" + (idx++));
				propDC.push(new DefaultRow(key, cells));
			}
		}
		propDC.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#createStreamableOperator(org.knime.core.
	 * node.streamable.PartitionInfo, org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	public StreamableOperator createStreamableOperator(
			PartitionInfo partitionInfo, PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		return new StreamableOperator() {

			SimpleStreamableOperatorInternals specInternals =
					new SimpleStreamableOperatorInternals();

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.knime.core.node.streamable.StreamableOperator#runIntermediate
			 * (org.knime.core.node.streamable.PortInput[],
			 * org.knime.core.node.ExecutionContext)
			 */
			// @Override
			// public void runIntermediate(PortInput[] inputs,
			// ExecutionContext exec) throws Exception {
			// // Temporarily write the properties to the config
			// final DataTableSpec inSpec = (DataTableSpec) inSpecs[0];
			// String[] colNames = colNamesMdl.applyTo(inSpec).getIncludes();
			//
			// for (String colName : colNames) {
			// inSpec.getColumnSpec(colName).getProperties()
			// .save(specInternals.getConfig().addConfig(colName));
			// }
			// specInternals.getConfig().addStringArray(COL_NAMES, colNames);
			//
			// }

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.knime.core.node.streamable.StreamableOperator#loadInternals(
			 * org.knime.core.node.streamable.StreamableOperatorInternals)
			 */
			@Override
			public void loadInternals(StreamableOperatorInternals internals) {
				specInternals = (SimpleStreamableOperatorInternals) internals;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.knime.core.node.streamable.StreamableOperator#saveInternals()
			 */
			@Override
			public StreamableOperatorInternals saveInternals() {
				return specInternals;
			}

			@Override
			public void runFinal(PortInput[] inputs, PortOutput[] outputs,
					ExecutionContext exec) throws Exception {
				if (!specInternals.getConfig().containsKey(COL_NAMES)) {
					// Temporarily write the properties to the config
					final DataTableSpec inSpec = (DataTableSpec) inSpecs[0];
					String[] colNames =
							colNamesMdl.applyTo(inSpec).getIncludes();

					for (String colName : colNames) {
						inSpec.getColumnSpec(colName).getProperties().save(
								specInternals.getConfig().addConfig(colName));
					}
					specInternals.getConfig().addStringArray(COL_NAMES,
							colNames);

				}

				// Just add the rows from the input to the output
				final RowInput rowInput = (RowInput) inputs[0];
				final RowOutput rowOutput = (RowOutput) outputs[0];
				DataRow inRow;
				while ((inRow = rowInput.poll()) != null) {
					exec.checkCanceled();
					rowOutput.push(inRow);
				}
				rowInput.close();
				rowOutput.close();

			}
		};
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		colNamesMdl.saveSettingsTo(settings);

	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		colNamesMdl.validateSettings(settings);

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		colNamesMdl.loadSettingsFrom(settings);

	}

	@Override
	protected void reset() {

	}

}
