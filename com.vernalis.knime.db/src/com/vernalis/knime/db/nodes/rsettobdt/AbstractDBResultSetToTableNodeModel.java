/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.db.nodes.rsettobdt;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Set;

import org.knime.base.node.io.database.DBNodeModel;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortObjectInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.database.node.util.PortObjectSpecHelper;
import org.knime.database.port.DBSessionPortObject;
import org.knime.database.port.DBSessionPortObjectSpec;
import org.knime.database.session.DBSession;

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
 * {@link #configure(DBSessionPortObjectSpec)} to validate settings
 * </p>
 * 
 * @author S Roughley
 *
 */
public abstract class AbstractDBResultSetToTableNodeModel extends DBNodeModel
		implements SettingsModelRegistry {

	private final Set<SettingsModel> models = new LinkedHashSet<>();
	/**
	 * Constructor. Provides a single DB Session input port and a single Data
	 * Table output port
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	protected AbstractDBResultSetToTableNodeModel() {
		super(new PortType[] { DBSessionPortObject.TYPE },
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
		BufferedDataTableRowOutput ro = new BufferedDataTableRowOutput(
				exec.createDataContainer(getOutputSpec()));
		execute((DBSessionPortObject) inObjects[0], ro, exec);
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
				execute((DBSessionPortObject) ((PortObjectInput) inputs[0])
						.getPortObject(), (RowOutput) outputs[0], exec);
			}
		};
	}

	private void execute(DBSessionPortObject dbSessionPort, RowOutput output,
			ExecutionContext exec) throws SQLException, InterruptedException,
			CanceledExecutionException {

		DBSession session = dbSessionPort.getDBSession();
		try (Connection connection =
				session.getConnectionProvider().getConnection(exec);
				ResultSet rs =
						getResultSetFromMetaData(connection.getMetaData());
				// We dont use the statement but otherwise a warning is thrown
				// that it isnt closed
				Statement statement = rs.getStatement()) {

			long rowCnt = 0L;
			while (rs.next()) {
				output.push(new DefaultRow(RowKey.createRowKey(rowCnt++),
						getRowCellsFromResultSet(rs)));
				exec.checkCanceled();
				exec.setProgress("Added " + rowCnt + " rows");
			}
		} finally {
			output.close();
		}
	}

	/**
	 * @param rs
	 *            the ResultSet with the current row ready to read.
	 *            Implementations must not change the state of the
	 *            {@link ResultSet} only read data from it
	 * 
	 * @return the array of {@link DataCell}s representing the next output row
	 * 
	 * @throws SQLException
	 *             if there was a problem getting the cells from the current
	 *             {@link ResultSet}
	 */
	protected abstract DataCell[] getRowCellsFromResultSet(ResultSet rs)
			throws SQLException;

	/**
	 * @param dbMetaData
	 *            the {@link DatabaseMetaData} for the incoming connection
	 * 
	 * @return the required ResultSet based on the metadata
	 * 
	 * @throws SQLException
	 *             if there was an error retrieving the required result from the
	 *             metadata
	 */
	protected abstract ResultSet getResultSetFromMetaData(
			DatabaseMetaData dbMetaData) throws SQLException;

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {

		configure(PortObjectSpecHelper.asDBSessionPortObjectSpec(inSpecs[0],
				true));
		return new DataTableSpec[] { getOutputSpec() };
	}

	/**
	 * Method hook to allow any {@link DBSessionPortObjectSpec} configuration to
	 * be performed
	 * 
	 * @param inSpec
	 *            the incoming {@link DBSessionPortObjectSpec}
	 * 
	 * @throws InvalidSettingsException
	 *             thrown if there is a problem configuring the node
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	protected void configure(DBSessionPortObjectSpec inSpec)
			throws InvalidSettingsException {
		// Nothing in default

	}

	/**
	 * @return the output table spec
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	protected abstract DataTableSpec getOutputSpec();

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

}
