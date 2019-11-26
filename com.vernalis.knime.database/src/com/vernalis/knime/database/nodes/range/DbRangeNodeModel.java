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
package com.vernalis.knime.database.nodes.range;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
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

import com.vernalis.knime.data.datacolumn.EnhancedDataColumnSpecCreator;

import static com.vernalis.knime.database.nodes.range.DbRangeNodeDialog.createColNamesModel;

public class DbRangeNodeModel extends NodeModel {

	/** The incoming column name property name */
	private static final String INCOMING_NAME =
			"Database Numeric Range Incoming Name";
	private final SettingsModelColumnFilter2 filtMdl = createColNamesModel();

	protected DbRangeNodeModel() {
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

		if (includes.length < 1) {
			throw new InvalidSettingsException("No columns kept!");
		}
		DataColumnSpec[] newColSpecs = new DataColumnSpec[includes.length * 2];
		int colIdx = 0;
		for (String colName : includes) {
			DataColumnSpec colSpec =
					spec.getDataTableSpec().getColumnSpec(colName);
			newColSpecs[colIdx++] = new EnhancedDataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(spec.getDataTableSpec(),
							"MIN(" + colName + ")"),
					colSpec.getType())
							.setProperties(colSpec.getProperties()
									.cloneAndOverwrite(Collections.singletonMap(
											INCOMING_NAME, colName)))
							.createSpec();
			newColSpecs[colIdx++] = new EnhancedDataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(spec.getDataTableSpec(),
							"MAX(" + colName + ")"),
					colSpec.getType())
							.setProperties(colSpec.getProperties()
									.cloneAndOverwrite(Collections.singletonMap(
											INCOMING_NAME, colName)))
							.createSpec();
		}

		DatabaseQueryConnectionSettings conn =
				spec.getConnectionSettings(getCredentialsProvider());

		final DataTableSpec outTableSpec =
				new DataTableSpecCreator().addColumns(newColSpecs).createSpec();
		final String newQuery = createQuery(conn, outTableSpec);

		return new PortObjectSpec[] { new DatabasePortObjectSpec(outTableSpec,
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
		sb.append("SELECT ");

		int i = 0;
		for (final DataColumnSpec colSpec : resultSpec) {
			final String outColName = colSpec.getName();
			final String inColName =
					colSpec.getProperties().getProperty(INCOMING_NAME);
			if (i++ > 0) {
				sb.append(",");
			}
			if (outColName.startsWith("MIN")) {
				sb.append("MIN");
			} else {
				sb.append("MAX");
			}
			sb.append("(").append(manipulator.quoteIdentifier(inColName))
					.append(") as ");
			sb.append(manipulator.quoteIdentifier(outColName));
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

	}

}
