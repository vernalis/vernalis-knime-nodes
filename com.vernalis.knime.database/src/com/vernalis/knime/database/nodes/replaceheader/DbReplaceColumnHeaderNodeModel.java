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
package com.vernalis.knime.database.nodes.replaceheader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
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
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.database.DatabasePortObject;
import org.knime.core.node.port.database.DatabasePortObjectSpec;
import org.knime.core.node.port.database.DatabaseQueryConnectionSettings;
import org.knime.core.node.port.database.StatementManipulator;
import org.knime.core.node.port.database.reader.DBReader;

import com.vernalis.knime.nodes.SettingsModelRegistry;

import static com.vernalis.knime.database.nodes.replaceheader.DbReplaceColumnHeaderNodeDialog.STRING_FILTER;
import static com.vernalis.knime.database.nodes.replaceheader.DbReplaceColumnHeaderNodeDialog.createLookupColumnNameModel;
import static com.vernalis.knime.database.nodes.replaceheader.DbReplaceColumnHeaderNodeDialog.createMissingColumnModel;
import static com.vernalis.knime.database.nodes.replaceheader.DbReplaceColumnHeaderNodeDialog.createValueColumnNameModel;

public class DbReplaceColumnHeaderNodeModel extends NodeModel
		implements SettingsModelRegistry {

	private Set<SettingsModel> models = new LinkedHashSet<>();
	private final SettingsModelString lookupColNameMdl =
			registerSettingsModel(createLookupColumnNameModel());
	private final SettingsModelString valueColNameMdl =
			registerSettingsModel(createValueColumnNameModel());
	private final SettingsModelString missingColumnActionModel =
			registerSettingsModel(createMissingColumnModel());

	private int lookupColIdx, valueColIdx;
	private MissingColumnAction missingColumnAction;

	protected DbReplaceColumnHeaderNodeModel() {
		super(new PortType[] { DatabasePortObject.TYPE,
				BufferedDataTable.TYPE },
				new PortType[] { DatabasePortObject.TYPE });
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
		DataTableSpec lookupSpec = (DataTableSpec) inSpecs[1];
		valueColIdx = getValidatedColumnSelectionModelColumnIndex(
				valueColNameMdl, STRING_FILTER, lookupSpec, getLogger(),
				lookupColNameMdl);
		lookupColIdx = getValidatedColumnSelectionModelColumnIndex(
				lookupColNameMdl, STRING_FILTER, lookupSpec, getLogger(),
				valueColNameMdl);
		try {
			missingColumnAction = MissingColumnAction
					.valueOf(missingColumnActionModel.getStringValue());
		} catch (Exception e) {
			throw new InvalidSettingsException(e);
		}
		// We cant return a spec at this point
		return new PortObjectSpec[] { null };
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

		// Read the lookup Table
		Map<String, String> colNameMap = new HashMap<>();
		BufferedDataTable lookupTable = (BufferedDataTable) inObjects[1];
		exec.setMessage("Reading lookup table");
		ExecutionContext exec0 = exec.createSubExecutionContext(0.95);
		double progPerRow = 1.0 / lookupTable.size();
		long rowCount = 0;
		for (DataRow row : lookupTable) {
			exec0.setProgress((rowCount++) * progPerRow, "Read " + rowCount
					+ " of " + lookupTable.size() + " lookup rows");
			DataCell lookupCell = row.getCell(lookupColIdx);
			if (lookupCell.isMissing()) {
				continue;
			}
			String lookup = ((StringValue) lookupCell).getStringValue();
			if (lookup == null || lookup.isEmpty()) {
				continue;
			}

			DataCell valueCell = row.getCell(valueColIdx);
			if (valueCell.isMissing()) {
				continue;
			}
			String value = ((StringValue) valueCell).getStringValue();
			if (value == null || value.isEmpty()) {
				continue;
			}
			if (!colNameMap.containsKey(lookup)) {
				colNameMap.put(lookup, value);
			}
		}

		exec.setMessage("Creating new SQL...");
		// Now we need to generate the new output spec
		DatabasePortObjectSpec inDbSpec =
				(DatabasePortObjectSpec) inObjects[0].getSpec();
		DataTableSpec inSpec = inDbSpec.getDataTableSpec();

		DataTableSpec outSpec =
				new DataTableSpecCreator().addColumns(inSpec.stream()
						.map(inColSpec -> missingColumnAction
								.createOutputColumnSpec(colNameMap, inColSpec))
						.filter(colSpec -> colSpec != null)
						.toArray(DataColumnSpec[]::new)).createSpec();

		DatabaseQueryConnectionSettings conn =
				inDbSpec.getConnectionSettings(getCredentialsProvider());

		final String newQuery = createQuery(conn, inSpec, colNameMap);

		DatabasePortObjectSpec outDbSpec = new DatabasePortObjectSpec(outSpec,
				new DatabaseQueryConnectionSettings(inDbSpec
						.getConnectionSettings(getCredentialsProvider()),
						newQuery).createConnectionModel());
		exec.setProgress(1.0);
		return new PortObject[] { new DatabasePortObject(outDbSpec) };
	}

	private String createQuery(final DatabaseQueryConnectionSettings conn,
			DataTableSpec inSpec, Map<String, String> colNameMap) {
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

		boolean isFirst = true;
		for (final DataColumnSpec colSpec : inSpec) {
			final String colName = colSpec.getName();
			final String sqlFragment = missingColumnAction
					.returnSqlSelectComponent(colNameMap, colName, manipulator);
			if (sqlFragment == null || sqlFragment.isEmpty()) {
				continue;
			}
			if (!isFirst) {
				sb.append(", ");
			} else {
				isFirst = false;
			}
			sb.append(sqlFragment);
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
		// TODO Auto-generated method stub

	}

	@Override
	public Set<SettingsModel> getModels() {
		return models;
	}

}
