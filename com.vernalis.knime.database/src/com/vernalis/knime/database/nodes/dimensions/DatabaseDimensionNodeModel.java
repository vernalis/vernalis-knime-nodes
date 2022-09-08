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
package com.vernalis.knime.database.nodes.dimensions;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.LongValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.LongCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.database.DatabasePortObject;
import org.knime.core.node.port.database.DatabasePortObjectSpec;
import org.knime.core.node.port.database.DatabaseQueryConnectionSettings;
import org.knime.core.node.port.database.DatabaseUtility;
import org.knime.core.node.port.database.StatementManipulator;
import org.knime.core.node.port.database.reader.DBReader;

public class DatabaseDimensionNodeModel extends NodeModel {

	private static final String NUMBER_COLUMNS = "Number Columns";
	private static final String NUMBER_ROWS = "Number Rows";

	protected DatabaseDimensionNodeModel() {
		super(new PortType[] { DatabasePortObject.TYPE },
				new PortType[] { BufferedDataTable.TYPE });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#execute(org.knime.core.node.port.PortObject
	 * [], org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {
		exec.setProgress("Opening database connection...");
		DatabasePortObject dbObj = (DatabasePortObject) inObjects[0];

		final DatabaseQueryConnectionSettings connSettings =
				dbObj.getConnectionSettings(getCredentialsProvider());

		final DatabaseUtility dbUtility = connSettings.getUtility();

		final DataTableSpec resultSpec = dbUtility.getReader(connSettings)
				.getDataTableSpec(getCredentialsProvider());
		int numCols = resultSpec.getNumColumns();

		// Run the query SELECT count(*) FROM (....) to get row count
		final BufferedDataTable table =
				dbUtility
						.getReader(new DatabaseQueryConnectionSettings(
								connSettings,
								getCountQuery(connSettings.getQuery(),
										dbUtility.getStatementManipulator())))
						.createTable(exec, getCredentialsProvider());
		// The row count is in the first (only!) cell of the first (only!) row
		final DataCell cell = table.iterator().next().getCell(0);
		long numRows = (long) ((cell instanceof LongValue)
				? ((LongValue) cell).getLongValue()
				: ((DoubleValue) cell).getDoubleValue());
		exec.clearTable(table);

		// Now create the real output table
		BufferedDataContainer bdc =
				exec.createDataContainer((DataTableSpec) configure(
						new PortObjectSpec[] { dbObj.getSpec() })[0]);
		bdc.addRowToTable(new DefaultRow(NUMBER_ROWS, new LongCell(numRows)));
		bdc.addRowToTable(new DefaultRow(NUMBER_COLUMNS, numCols));
		bdc.close();
		pushFlowVariableDouble(NUMBER_ROWS, numRows);
		pushFlowVariableInt(NUMBER_COLUMNS, numCols);
		return new BufferedDataTable[] { bdc.getTable() };
	}

	private String getCountQuery(String query,
			StatementManipulator manipulator) {

		final StringBuilder sb = new StringBuilder();
		final String[] queries = query.split(DBReader.SQL_QUERY_SEPARATOR);
		for (int i = 0; i < queries.length - 1; i++) {
			sb.append(queries[i]);
			sb.append(DBReader.SQL_QUERY_SEPARATOR);
		}
		// build SELECT statement
		sb.append("SELECT count(*) FROM (").append(queries[queries.length - 1])
				.append(") table_").append(System.identityHashCode(this));
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#configure(org.knime.core.node.port.
	 * PortObjectSpec[])
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		pushFlowVariableDouble(NUMBER_ROWS, -1);
		DatabasePortObjectSpec spec = (DatabasePortObjectSpec) inSpecs[0];
		final DatabaseQueryConnectionSettings connSettings =
				spec.getConnectionSettings(getCredentialsProvider());
		DataTableSpec dbSpec;
		if (connSettings.getRetrieveMetadataInConfigure()) {
			try {
				dbSpec = connSettings.getUtility().getReader(connSettings)
						.getDataTableSpec(getCredentialsProvider());
			} catch (SQLException e) {
				throw new InvalidSettingsException(
						"Unable to get DB spec during configure", e);
			}
		} else {
			dbSpec = spec.getDataTableSpec();
		}
		pushFlowVariableInt(NUMBER_COLUMNS, dbSpec.getNumColumns());
		return new DataTableSpec[] { new DataTableSpecCreator().addColumns(
				new DataColumnSpecCreator("Dimensions", LongCell.TYPE)
						.createSpec())
				.createSpec() };
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

	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {

	}

	@Override
	protected void reset() {

	}

}
