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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.StringValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.MergeOperator;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortObjectInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowInput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.core.node.streamable.StreamableOperatorInternals;
import org.knime.core.node.streamable.simple.SimpleStreamableOperatorInternals;

import static com.vernalis.nodes.misc.colprops.ExtractColumnPropertiesNodeModel.COLUMN_NAME;
import static com.vernalis.nodes.misc.colprops.ExtractColumnPropertiesNodeModel.PROPERTY_NAME;
import static com.vernalis.nodes.misc.colprops.ExtractColumnPropertiesNodeModel.PROPERTY_VALUE;
import static com.vernalis.nodes.misc.colprops.InjectColumnPropertiesNodeDialog.createApplyToAllColumnsModel;
import static com.vernalis.nodes.misc.colprops.InjectColumnPropertiesNodeDialog.createClearExistingPropertiesModel;
import static com.vernalis.nodes.misc.colprops.InjectColumnPropertiesNodeDialog.createOverwriteDuplicatePropertiesModel;
import static com.vernalis.nodes.misc.colprops.InjectColumnPropertiesNodeDialog.createUseIncomingSpecAtConfigureModel;

/**
 * {@link NodeModel} implementation for the Inject Column Properties node
 * 
 * @author s.roughley
 *
 */
public class InjectColumnPropertiesNodeModel extends NodeModel {

	private final SettingsModelBoolean clearExistingPropertiesMdl =
			createClearExistingPropertiesModel();
	private final SettingsModelBoolean overwriteDuplicatePropertiesMdl =
			createOverwriteDuplicatePropertiesModel();
	private final SettingsModelBoolean applyToAllColumnsMdl =
			createApplyToAllColumnsModel();
	private final SettingsModelBoolean useIncomingSpecForConfigureMdl =
			createUseIncomingSpecAtConfigureModel();

	protected InjectColumnPropertiesNodeModel() {
		super(2, 1);
		clearExistingPropertiesMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				overwriteDuplicatePropertiesMdl.setEnabled(
						!clearExistingPropertiesMdl.getBooleanValue());
				applyToAllColumnsMdl.setEnabled(
						clearExistingPropertiesMdl.getBooleanValue());
			}
		});
		overwriteDuplicatePropertiesMdl
				.setEnabled(!clearExistingPropertiesMdl.getBooleanValue());
		applyToAllColumnsMdl
				.setEnabled(clearExistingPropertiesMdl.getBooleanValue());
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
		DataTableSpec inSpec = inSpecs[1];
		if (inSpec.getNumColumns() < 3) {
			throw new InvalidSettingsException(
					"At least three columns are required in the second input table");
		}
		final DataColumnSpec colNameColSpec = inSpec.getColumnSpec(0);
		if (!colNameColSpec.getName().equals(COLUMN_NAME)
				|| !colNameColSpec.getType().isCompatible(StringValue.class)) {
			throw new InvalidSettingsException(
					"The first column of the second input table must have the name '"
							+ COLUMN_NAME + "' and be a String type");
		}

		final DataColumnSpec propNameColSpec = inSpec.getColumnSpec(1);
		if (!propNameColSpec.getName().equals(PROPERTY_NAME)
				|| !propNameColSpec.getType().isCompatible(StringValue.class)) {
			throw new InvalidSettingsException(
					"The second column of the second input table must have the name '"
							+ PROPERTY_NAME + "' and be a String type");
		}

		final DataColumnSpec propValueColSpec = inSpec.getColumnSpec(2);
		if (!propValueColSpec.getName().equals(PROPERTY_VALUE)
				|| !propValueColSpec.getType()
						.isCompatible(StringValue.class)) {
			throw new InvalidSettingsException(
					"The third column of the second input table must have the name '"
							+ PROPERTY_VALUE + "' and be a String type");
		}

		return new DataTableSpec[] {
				useIncomingSpecForConfigureMdl.getBooleanValue() ? inSpecs[0]
						: null };
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
		DataTableSpec outSpec =
				createOutputSpec(inData[0].getSpec(), inData[1]);
		// = rearranger.createSpec();
		// new DataTableSpecCreator().addColumns(newColSpecs).createSpec();
		return new BufferedDataTable[] {
				exec.createSpecReplacerTable(inData[0], outSpec) };

	}

	/**
	 * @param propsTable
	 *            The properties table from the 2nd input port
	 * @return A Map of the column names, and new properties for each column. K
	 *         = Column name, V = Map of properties for the column (K = Property
	 *         name, V = Property Value)
	 */
	protected Map<String, Map<String, String>> readNewProperties(
			BufferedDataTable propsTable) {
		Map<String, Map<String, String>> newProps = new HashMap<>();

		for (DataRow row : propsTable) {
			if (row.getCell(0).isMissing() || row.getCell(1).isMissing()
					|| row.getCell(2).isMissing()) {
				continue;
			}
			String colName = ((StringValue) row.getCell(0)).getStringValue();
			String propName = ((StringValue) row.getCell(1)).getStringValue();
			String propValue = ((StringValue) row.getCell(2)).getStringValue();
			if (!newProps.containsKey(colName)) {
				newProps.put(colName, new HashMap<>());
			}
			newProps.get(colName).put(propName, propValue);
		}
		return newProps;
	}

	/**
	 * @param inSpec
	 *            The incoming table spec at the 1st input port
	 * @param propsTable
	 *            The properties table at the 2nd input port
	 * @return The output {@link DataTableSpec} based on the settings, input
	 *         spec and properties table
	 */
	protected DataTableSpec createOutputSpec(DataTableSpec inSpec,
			BufferedDataTable propsTable) {
		// Firstly, we need to read the properties..
		Map<String, Map<String, String>> newProps =
				readNewProperties(propsTable);

		// Now new need to build the new table spec...
		DataColumnSpec[] newColSpecs =
				new DataColumnSpec[inSpec.getNumColumns()];
		for (int i = 0; i < inSpec.getNumColumns(); i++) {
			final DataColumnSpec inColSpec = inSpec.getColumnSpec(i);
			String colName = inColSpec.getName();
			if (clearExistingPropertiesMdl.getBooleanValue()) {
				// Just need to use an new properties where relevant
				if (applyToAllColumnsMdl.getBooleanValue()
						|| newProps.containsKey(colName)) {
					DataColumnSpecCreator dcsFact =
							new DataColumnSpecCreator(inColSpec);
					// Replace the properties
					dcsFact.setProperties(new DataColumnProperties(
							newProps.containsKey(colName)
									? newProps.get(colName)
									: Collections.emptyMap()));
					newColSpecs[i] = dcsFact.createSpec();
				} else {
					// Retain the old spec
					newColSpecs[i] = inColSpec;
				}
			} else {
				// Need to retain old and respect overwrite..
				Map<String, String> newColProps = new HashMap<>();
				if (newProps.containsKey(colName)) {
					newColProps.putAll(newProps.get(colName));
				}
				DataColumnProperties colProps = inColSpec.getProperties();
				Enumeration<String> propIter = colProps.properties();
				while (propIter.hasMoreElements()) {
					String propName = propIter.nextElement();
					if (!overwriteDuplicatePropertiesMdl.getBooleanValue()
							|| !newColProps.containsKey(propName)) {
						newColProps.put(propName,
								colProps.getProperty(propName));
					}
				}
				DataColumnSpecCreator dcsFact =
						new DataColumnSpecCreator(inColSpec);
				dcsFact.setProperties(new DataColumnProperties(newColProps));
				newColSpecs[i] = dcsFact.createSpec();

			}
		}

		return new DataTableSpecCreator().addColumns(newColSpecs).createSpec();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#getInputPortRoles()
	 */
	@Override
	public InputPortRole[] getInputPortRoles() {
		return new InputPortRole[] { InputPortRole.DISTRIBUTED_STREAMABLE,
				InputPortRole.NONDISTRIBUTED_NONSTREAMABLE };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#getOutputPortRoles()
	 */
	@Override
	public OutputPortRole[] getOutputPortRoles() {
		return new OutputPortRole[] { OutputPortRole.DISTRIBUTED };
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
		try {
			// Try to load the output spec from the internals
			DataTableSpec.load(((SimpleStreamableOperatorInternals) internals)
					.getConfig());
			// If we succeed, we dont need to go again
			return false;
		} catch (Exception e) {
			// Need to try again
			return true;
		}

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
			@Override
			public void runIntermediate(PortInput[] inputs,
					ExecutionContext exec) throws Exception {
				// First time round, we save the output spec from to
				// specInternals
				createOutputSpec((DataTableSpec) inSpecs[0],
						(BufferedDataTable) ((PortObjectInput) inputs[1])
								.getPortObject())
										.save(specInternals.getConfig());
			}

			@Override
			public void runFinal(PortInput[] inputs, PortOutput[] outputs,
					ExecutionContext exec) throws Exception {

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

			@Override
			public StreamableOperatorInternals saveInternals() {
				return specInternals;
			}

			@Override
			public void loadInternals(StreamableOperatorInternals internals) {
				specInternals = (SimpleStreamableOperatorInternals) internals;
			}
		};

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#computeFinalOutputSpecs(org.knime.core.node
	 * .streamable.StreamableOperatorInternals,
	 * org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	public PortObjectSpec[] computeFinalOutputSpecs(
			StreamableOperatorInternals internals, PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		// Get the final output spec, with the properties, from the internals
		return new PortObjectSpec[] { DataTableSpec.load(
				((SimpleStreamableOperatorInternals) internals).getConfig()) };
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
		// nothing to do
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 *
	 * org.knime.core.node.NodeModel#createInitialStreamableOperatorInternals()
	 */
	@Override
	public StreamableOperatorInternals createInitialStreamableOperatorInternals() {
		return new SimpleStreamableOperatorInternals();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#createMergeOperator()
	 */
	@Override
	public MergeOperator createMergeOperator() {
		return new MergeOperator() {

			@Override
			public SimpleStreamableOperatorInternals mergeFinal(
					StreamableOperatorInternals[] operators) {
				// All should be the same, so just grab the first
				return (SimpleStreamableOperatorInternals) operators[0];
			}

			@Override
			public SimpleStreamableOperatorInternals mergeIntermediate(
					StreamableOperatorInternals[] operators) {
				return mergeFinal(operators);
			}
		};

	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		clearExistingPropertiesMdl.saveSettingsTo(settings);
		overwriteDuplicatePropertiesMdl.saveSettingsTo(settings);
		applyToAllColumnsMdl.saveSettingsTo(settings);
		useIncomingSpecForConfigureMdl.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		clearExistingPropertiesMdl.validateSettings(settings);
		overwriteDuplicatePropertiesMdl.validateSettings(settings);
		applyToAllColumnsMdl.validateSettings(settings);
		useIncomingSpecForConfigureMdl.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		clearExistingPropertiesMdl.loadSettingsFrom(settings);
		overwriteDuplicatePropertiesMdl.loadSettingsFrom(settings);
		applyToAllColumnsMdl.loadSettingsFrom(settings);
		useIncomingSpecForConfigureMdl.loadSettingsFrom(settings);
	}

	@Override
	protected void reset() {
		// Nothing to do
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Nothing to do
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Nothing to do
	}

}
