/*******************************************************************************
 * Copyright (c) 2018, 2020, Vernalis (R&D) Ltd
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
package com.vernalis.knime.database.nodes.extractbdt;

import java.io.File;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.database.DatabaseConnectionPortObject;
import org.knime.core.node.port.database.DatabaseConnectionPortObjectSpec;
import org.knime.core.node.port.database.DatabaseConnectionSettings;
import org.knime.core.node.port.database.DatabasePortObject;
import org.knime.core.node.port.database.DatabasePortObjectSpec;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortObjectInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;

import com.vernalis.knime.nodes.SettingsModelRegistry;

/**
 * Abstract node model to generate a BDT from an incoming Database Connection
 * 
 * <p>
 * Implementations must implement the following:
 * <ul>
 * <li>{@link #getRowCellsFromResultSet(ResultSet)} - converts the next row in
 * the result set to an array of cells</li>
 * <li{@link #getResultSetFromMetaData(DatabaseMetaData)} - get the result set
 * from the database metadata></li>
 * <li>{@link #getOutputSpec()} - get the output table spec during configure and
 * for output table creation</li>
 * </ul>
 * 
 * Optionally, implementations should override
 * {@link #configure(DatabaseConnectionPortObjectSpec)} to validate settings
 * </p>
 * 
 * @since 07-Sep-2022
 * @since v1.36.0

 * @author S Roughley
 *
 */
public class DbQueryToEmptyTableNodeModel extends NodeModel
		implements SettingsModelRegistry {

	Set<SettingsModel> models = new LinkedHashSet<>();

	public DbQueryToEmptyTableNodeModel() {
		super(new PortType[] { DatabasePortObject.TYPE },
				new PortType[] { BufferedDataTable.TYPE });

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.nodes.SettingsModelRegistry#getModels()
	 */
	@Override
	public Set<SettingsModel> getModels() {
		return models;
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {
		exec.setMessage("Retrieving metadata from database");
		final DatabasePortObject dbConnPortObject =
				(DatabasePortObject) inObjects[0];
		BufferedDataTableRowOutput ro = new BufferedDataTableRowOutput(exec
				.createDataContainer(configure(dbConnPortObject.getSpec())));
		execute(dbConnPortObject, ro, exec);
		return new BufferedDataTable[] { ro.getDataTable() };
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
	 * org.knime.core.node.NodeModel#createStreamableOperator(org.knime.core.
	 * node.streamable.PartitionInfo, org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	public StreamableOperator createStreamableOperator(
			PartitionInfo partitionInfo, PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		return new StreamableOperator() {

			@Override
			public void runFinal(PortInput[] inputs, PortOutput[] outputs,
					ExecutionContext exec) throws Exception {
				execute((DatabaseConnectionPortObject) ((PortObjectInput) inputs[0])
						.getPortObject(), (RowOutput) outputs[0], exec);
			}
		};
	}

	protected void execute(DatabaseConnectionPortObject dbConnPortObject,
			RowOutput output, ExecutionContext exec)
			throws InvalidSettingsException, SQLException, InterruptedException,
			CanceledExecutionException {
		// Nothing to do!
		output.close();
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		if (!(inSpecs[0] instanceof DatabasePortObjectSpec)) {
			throw new InvalidSettingsException(
					"Incoming port is not a Database Query Connection Port");
		}

		return new DataTableSpec[] {
				configure((DatabasePortObjectSpec) inSpecs[0]) };
	}

	protected DataTableSpec configure(DatabasePortObjectSpec inSpec)
			throws InvalidSettingsException {
		return inSpec.getDataTableSpec();

	}

	/**
	 * @param inSpecs
	 * @return
	 * @throws InvalidSettingsException
	 */
	protected final DatabaseConnectionSettings getConnectionSettings(
			DatabaseConnectionPortObjectSpec inSpec)
			throws InvalidSettingsException {
		return inSpec.getConnectionSettings(getCredentialsProvider());
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
	public void saveSettingsTo(NodeSettingsWO settings) {
		SettingsModelRegistry.super.saveSettingsTo(settings);
	}

	@Override
	public void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		SettingsModelRegistry.super.validateSettings(settings);
	}

	@Override
	public void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		SettingsModelRegistry.super.loadValidatedSettingsFrom(settings);
	}

	@Override
	protected void reset() {

	}

}
