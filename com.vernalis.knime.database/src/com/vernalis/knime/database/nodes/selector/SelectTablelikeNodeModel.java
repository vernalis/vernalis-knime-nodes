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
package com.vernalis.knime.database.nodes.selector;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.SortedSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.database.DatabaseConnectionPortObject;
import org.knime.core.node.port.database.DatabaseConnectionPortObjectSpec;
import org.knime.core.node.port.database.DatabaseConnectionSettings;
import org.knime.core.node.port.database.DatabasePortObject;
import org.knime.core.node.port.database.DatabasePortObjectSpec;
import org.knime.core.node.port.database.DatabaseQueryConnectionSettings;
import org.knime.core.node.port.database.reader.DBReader;

import static com.vernalis.knime.database.nodes.selector.SelectTablelikeNodeDialog.INITIALIZING_DIALOG;
import static com.vernalis.knime.database.nodes.selector.SelectTablelikeNodeDialog.NO_METADATA_AVAILABLE;
import static com.vernalis.knime.database.nodes.selector.SelectTablelikeNodeDialog.NO_SCHEMAS;
import static com.vernalis.knime.database.nodes.selector.SelectTablelikeNodeDialog.createIncludeSchemaNamesModel;
import static com.vernalis.knime.database.nodes.selector.SelectTablelikeNodeDialog.createSchemaNameModel;
import static com.vernalis.knime.database.nodes.selector.SelectTablelikeNodeDialog.createTableNameModel;
import static com.vernalis.knime.database.nodes.selector.SelectTablelikeNodeDialog.createTableTypeModel;
import static com.vernalis.knime.database.nodes.selector.SelectTablelikeNodeDialog.createTablesList;

/**
 * Node Model implementation for the Database Select Table node
 * 
 * @author s.roughley
 *
 */
public class SelectTablelikeNodeModel extends NodeModel {

	private static final String NO_TABLE_TYPE_SELECTED =
			"No table type selected";
	protected final SettingsModelString m_tableTypeMdl = createTableTypeModel();
	protected final SettingsModelString m_tableNameMdl = createTableNameModel();
	protected final SettingsModelString m_schemaNameMdl =
			createSchemaNameModel();
	protected final SettingsModelBoolean m_inclSchemaNamesMdl =
			createIncludeSchemaNamesModel();

	protected String sql;

	public SelectTablelikeNodeModel() {
		super(new PortType[] { DatabaseConnectionPortObject.TYPE },
				new PortType[] { DatabasePortObject.TYPE });

		m_tableTypeMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateModelsFromTypeChange();
			}
		});

		m_schemaNameMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {

				updateTableNameModel();

			}
		});

		updateModelsFromTypeChange();
		updateTableNameModel();
	}

	/**
	 * Method to ensure model enabled stati following change to table type model
	 */
	protected void updateModelsFromTypeChange() {
		// m_schemaNameMdl.setEnabled(true);
		// String val = m_schemaNameMdl.getStringValue();
		// if (/* val == null || val.isEmpty() ||
		// */val.equals(INITIALIZING_DIALOG)
		// || val.equals(NO_METADATA_AVAILABLE) || val.equals(NO_SCHEMAS)
		// || (val.startsWith("<No ")
		// && val.endsWith(" tables available>"))) {
		// m_schemaNameMdl.setEnabled(false);
		// }
		// m_inclSchemaNamesMdl.setEnabled(m_schemaNameMdl.isEnabled());
	}

	/**
	 * 
	 */
	protected void updateTableNameModel() {
		// m_tableNameMdl.setEnabled(true);
		// String val = m_tableNameMdl.getStringValue();
		// if (/* val == null || val.isEmpty() ||
		// */val.equals(INITIALIZING_DIALOG)
		// || val.equals(NO_METADATA_AVAILABLE) || (val.startsWith("<No ")
		// && val.endsWith(" tables available>"))) {
		// m_tableNameMdl.setEnabled(false);
		// }
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {
		exec.setMessage("Retrieving metadata from database");
		DatabaseConnectionPortObject incomingConnection =
				(DatabaseConnectionPortObject) inObjects[0];
		DatabaseConnectionSettings connSettings = incomingConnection
				.getConnectionSettings(getCredentialsProvider());

		DatabaseQueryConnectionSettings querySettings =
				new DatabaseQueryConnectionSettings(connSettings, sql);

		DBReader conn = querySettings.getUtility().getReader(querySettings);
		try {
			DataTableSpec tableSpec =
					conn.getDataTableSpec(getCredentialsProvider());

			return new PortObject[] { new DatabasePortObject(
					new DatabasePortObjectSpec(tableSpec, querySettings)) };
		} catch (SQLException ex) {
			throw new InvalidSettingsException(
					"Error while validating SQL query: " + ex.getMessage(), ex);
		}
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		if (!(inSpecs[0] instanceof DatabaseConnectionPortObjectSpec)) {
			throw new InvalidSettingsException(
					"Incoming port is not a Database Connection Port");
		}
		if (inSpecs[0] instanceof DatabasePortObjectSpec) {
			throw new InvalidSettingsException(
					"Incompatible port spec, expected: DatabasePortObjectSpec, "
							+ "actual: DatabaseConnectionPortObjectSpec");
		}

		if (m_tableTypeMdl.getStringValue() == null
				|| m_tableTypeMdl.getStringValue().isEmpty()) {
			getLogger().error(NO_TABLE_TYPE_SELECTED);
			throw new InvalidSettingsException(NO_TABLE_TYPE_SELECTED);
		}

		if (m_tableNameMdl.getStringValue() == null
				|| "".equals(m_tableNameMdl.getStringValue())) {
			getLogger()
					.error("No " + m_tableTypeMdl.getStringValue().toLowerCase()
							+ " selected");
			throw new InvalidSettingsException(
					"No " + m_tableTypeMdl.getStringValue().toLowerCase()
							+ " selected");
		}
		if (m_tableNameMdl.getStringValue().equals(INITIALIZING_DIALOG)
				|| m_tableNameMdl.getStringValue()
						.equals(NO_METADATA_AVAILABLE)) {
			getLogger().error(
					"Invalid selection - " + m_tableNameMdl.getStringValue());
			throw new InvalidSettingsException(
					"Invalid selection - " + m_tableNameMdl.getStringValue());
		}
		if (m_tableNameMdl.getStringValue()
				.equals("<No " + m_tableTypeMdl.getStringValue().toLowerCase()
						+ " tables available>")) {
			getLogger()
					.error("No " + m_tableTypeMdl.getStringValue().toLowerCase()
							+ " tables available");
			throw new InvalidSettingsException(
					"No " + m_tableTypeMdl.getStringValue().toLowerCase()
							+ " tables available");
		}

		DatabaseConnectionPortObjectSpec incomingConnection =
				(DatabaseConnectionPortObjectSpec) inSpecs[0];
		DatabaseConnectionSettings connSettings = incomingConnection
				.getConnectionSettings(getCredentialsProvider());

		Map<String, SortedSet<String>> dbTableNames =
				createTablesList(connSettings, getCredentialsProvider(),
						getLogger(), m_tableTypeMdl.getStringValue());
		if (!dbTableNames.containsKey(m_schemaNameMdl.getStringValue())
				|| !dbTableNames.get(m_schemaNameMdl.getStringValue())
						.contains(m_tableNameMdl.getStringValue())) {
			getLogger().error("Database connection does not contain "
					+ m_tableTypeMdl.getStringValue() + " '"
					+ m_tableNameMdl.getStringValue() + "'");
			throw new InvalidSettingsException(
					"Database connection does not contain "
							+ m_tableTypeMdl.getStringValue() + " '"
							+ m_tableNameMdl.getStringValue() + "'");
		}

		sql = "SELECT * FROM "
				+ (m_inclSchemaNamesMdl.isEnabled()
						&& m_inclSchemaNamesMdl.getBooleanValue()
						&& !NO_SCHEMAS.equals(m_schemaNameMdl.getStringValue())
								? m_schemaNameMdl.getStringValue() + "." : "")
				+ m_tableNameMdl.getStringValue();
		DatabaseQueryConnectionSettings querySettings =
				new DatabaseQueryConnectionSettings(connSettings, sql);

		if (!connSettings.getRetrieveMetadataInConfigure()) {
			return new PortObjectSpec[1];
		}

		try {
			final DBReader conn =
					connSettings.getUtility().getReader(querySettings);
			DataTableSpec tableSpec =
					conn.getDataTableSpec(getCredentialsProvider());

			return new PortObjectSpec[] {
					new DatabasePortObjectSpec(tableSpec, querySettings) };
		} catch (SQLException ex) {
			throw new InvalidSettingsException(
					"Error while validating SQL query '" + sql + "' : "
							+ ex.getMessage(),
					ex);
		}
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
		m_tableTypeMdl.saveSettingsTo(settings);
		m_tableNameMdl.saveSettingsTo(settings);
		m_inclSchemaNamesMdl.saveSettingsTo(settings);
		m_schemaNameMdl.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_tableTypeMdl.validateSettings(settings);
		m_tableNameMdl.validateSettings(settings);
		m_inclSchemaNamesMdl.validateSettings(settings);
		m_schemaNameMdl.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_tableTypeMdl.loadSettingsFrom(settings);
		m_tableNameMdl.loadSettingsFrom(settings);
		m_inclSchemaNamesMdl.loadSettingsFrom(settings);
		m_schemaNameMdl.loadSettingsFrom(settings);
	}

	@Override
	protected void reset() {

	}

}
