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
package com.vernalis.knime.database.nodes.distinct;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.database.DatabasePortObject;
import org.knime.core.node.port.database.DatabasePortObjectSpec;
import org.knime.core.node.port.database.DatabaseQueryConnectionSettings;
import org.knime.core.node.port.database.StatementManipulator;
import org.knime.core.node.port.database.reader.DBReader;

import static com.vernalis.knime.database.nodes.distinct.DbDistinctNodeDialog.createColNamesModel;

public class DbDistinctNodeModel extends NodeModel {

	private final SettingsModelColumnFilter2 filtMdl = createColNamesModel();

	protected DbDistinctNodeModel() {
		super(new PortType[] { DatabasePortObject.TYPE },
				new PortType[] { DatabasePortObject.TYPE });
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
		return new PortObject[] { new DatabasePortObject(
				(DatabasePortObjectSpec) configure(new PortObjectSpec[] {
						((DatabasePortObject) inObjects[0]).getSpec() })[0]) };
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
		DatabasePortObjectSpec spec = (DatabasePortObjectSpec) inSpecs[0];
		final String[] includes =
				filtMdl.applyTo(spec.getDataTableSpec()).getIncludes();
		ColumnRearranger rearranger =
				new ColumnRearranger(spec.getDataTableSpec());
		rearranger.keepOnly(includes);
		if (rearranger.getColumnCount() < 1) {
			throw new InvalidSettingsException("No columns kept!");
		}
		// TODO: REMOVE THIS!
		// rearranger.replace(new SingleCellFactory(new DataColumnSpecCreator(
		// rearranger.createSpec().getColumnSpec(0).getName(),
		// SmilesAdapterCell.RAW_TYPE).createSpec()) {
		//
		// @Override
		// public DataCell getCell(DataRow row) {
		// // TODO Auto-generated method stub
		// return SmilesCellFactory.createAdapterCell(
		// ((StringValue) row.getCell(0)).getStringValue());
		// }
		// }, 0);
		DatabaseQueryConnectionSettings conn =
				spec.getConnectionSettings(getCredentialsProvider());

		final String newQuery = createQuery(conn, rearranger.createSpec());

		return new PortObjectSpec[] { new DatabasePortObjectSpec(
				rearranger.createSpec(),
				new DatabaseQueryConnectionSettings(
						spec.getConnectionSettings(getCredentialsProvider()),
						newQuery).createConnectionModel()) };
	}

	private String createQuery(final DatabaseQueryConnectionSettings conn,
			final DataTableSpec resultSpec) {
		final String query = conn.getQuery();
		final StatementManipulator manipulator =
				conn.getUtility().getStatementManipulator();
		final StringBuilder sb = new StringBuilder();
		final String[] queries = query.split(DBReader.SQL_QUERY_SEPARATOR);
		for (int i = 0; i < queries.length - 1; i++) {
			sb.append(queries[i]);
			sb.append(DBReader.SQL_QUERY_SEPARATOR);
		}
		// build SELECT statement
		sb.append("SELECT DISTINCT ");

		int i = 0;
		for (final DataColumnSpec colSpec : resultSpec) {
			final String colName = colSpec.getName();
			if (i++ > 0) {
				sb.append(",");
			}
			sb.append(manipulator.quoteIdentifier(colName));
		}

		final String selectQuery = queries[queries.length - 1];
		sb.append(" FROM (" + selectQuery + ") table_"
				+ System.identityHashCode(this));
		return sb.toString();
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
		filtMdl.saveSettingsTo(settings);

	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		filtMdl.validateSettings(settings);

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		filtMdl.loadSettingsFrom(settings);
	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}

}
