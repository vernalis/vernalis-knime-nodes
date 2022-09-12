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
package com.vernalis.knime.database.nodes.rsettobdt;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.database.DatabaseConnectionPortObjectSpec;
import org.knime.core.node.port.database.DatabaseConnectionSettings;
import org.knime.core.node.port.database.DatabaseQueryConnectionSettings;
import org.knime.core.node.workflow.CredentialsProvider;

import com.vernalis.knime.database.TableTypes;

/**
 * Node Dialog Pane for the Database Select Tables (Interative) node
 * 
 * @author s.roughley
 *
 */
public class ListTablesNodeDialog extends DefaultNodeSettingsPane {

	static final String NO_SCHEMAS = "<--No Schemas-->";
	private static final String SCHEMA_NAME = "Schema Name";
	private static final String TABLE_TYPE = "Table Type";
	static final String INITIALIZING_DIALOG = "Initializing dialog...";
	static final String NO_METADATA_AVAILABLE = "<No Metadata Available>";
	protected DatabaseConnectionSettings m_upstreamDbSettings;
	protected DialogComponentStringSelection m_typeSelector, m_tableSelector,
			m_schemaSelector;
	protected NodeLogger m_logger = NodeLogger.getLogger(this.getClass());
	private SettingsModelString tableTypeMdl;
	private SettingsModelString schemaNameMdl;
	private SortedSet<String> schemas;

	/**
	 * Constructor
	 */
	public ListTablesNodeDialog() {
		super();

		tableTypeMdl = createTableTypeModel();
		schemaNameMdl = createSchemaNameModel();

		schemas = createSchemasList(m_upstreamDbSettings,
				getCredentialsProvider(), m_logger,
				tableTypeMdl.getStringValue());

		m_typeSelector = new DialogComponentStringSelection(tableTypeMdl,
				TABLE_TYPE, Arrays.asList(TableTypes.getAllNames()), false,
				createFlowVariableModel(tableTypeMdl));
		m_schemaSelector =
				new DialogComponentStringSelection(schemaNameMdl, SCHEMA_NAME,
						schemas, false, createFlowVariableModel(schemaNameMdl));
		tableTypeMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateModelsAfterTypeChange();

			}
		});

		updateModelsAfterTypeChange();

		addDialogComponent(m_typeSelector);
		addDialogComponent(m_schemaSelector);

	}

	/**
	 */
	protected void updateModelsAfterTypeChange() {

		schemas = createSchemasList(m_upstreamDbSettings,
				getCredentialsProvider(), m_logger,
				tableTypeMdl.getStringValue());
		schemaNameMdl.setEnabled(true);
		if (schemas.isEmpty()) {
			schemaNameMdl.setEnabled(false);
			return;
		} else if (schemas.size() == 1) {
			String val = schemas.first();
			if (val.equals(INITIALIZING_DIALOG)
					|| val.equals(NO_METADATA_AVAILABLE)
					|| val.equals(NO_SCHEMAS) || (val.startsWith("<No ")
							&& val.endsWith(" tables available>"))) {
				schemaNameMdl.setEnabled(false);
			}
		}
		m_schemaSelector.replaceListItems(schemas, null);
	}

	/**
	 * @return The Settings model for the Table Type
	 */
	static SettingsModelString createTableTypeModel() {
		return new SettingsModelString(TABLE_TYPE,
				TableTypes.getDefault().getTypeName());
	}

	/**
	 * @return The settings model for the Schema name
	 */
	static SettingsModelString createSchemaNameModel() {
		return new SettingsModelString(SCHEMA_NAME, "");
	}

	/**
	 * @param dcs
	 *            The {@link DatabaseConnectionSettings} to access the database
	 * @param cp
	 *            The {@link CredentialsProvider} to access the database
	 * @param logger
	 *            A {@link NodeLogger} instance
	 * @param tableType
	 *            The table type
	 * @return A map of the Schema names and associated table names for the
	 *         specified type. The Map is sorted by Key, and each value is a
	 *         sorted set
	 */
	static SortedSet<String> createSchemasList(DatabaseConnectionSettings dcs,
			CredentialsProvider cp, NodeLogger logger, String tableType) {
		final SortedSet<String> schemaNames = new TreeSet<>();

		if (dcs == null || cp == null) {
			// happens on first dialog class instantiaion
			// as loadAdditionalSettings is not called until afetr initial call,
			// but we need something in the dropdown list. Hopefully, this is
			// never actually shown...
			return new TreeSet<>(Collections.singleton(INITIALIZING_DIALOG));
		}
		try {
			DatabaseMetaData dbMetaData = dcs.getUtility()
					.getReader(new DatabaseQueryConnectionSettings(dcs, ""))
					.getDatabaseMetaData(cp);
			ResultSet rs = dbMetaData.getSchemas();
			while (rs.next()) {
				String schemaName = rs.getString("TABLE_SCHEM");
				if (schemaName == null) {
					schemaName = NO_SCHEMAS;
				}
				schemaNames.add(schemaName);
			}
			if (schemaNames.isEmpty()) {
				schemaNames.add("<No " + tableType.toLowerCase()
						+ " schemas available>");
			}
		} catch (SQLException e) {
			// SQL problem - strange but conceivably possible if the driver is
			// not correctly functioning
			logger.warn("Unable to load " + tableType.toLowerCase() + " names");
			schemaNames.add(NO_METADATA_AVAILABLE);
		}
		// return tableNames;
		return schemaNames;

	}

	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings,
			PortObjectSpec[] specs) throws NotConfigurableException {

		for (PortObjectSpec pos : specs) {
			// Look for the incoming connection port
			if (pos instanceof DatabaseConnectionPortObjectSpec) {
				try {
					m_upstreamDbSettings =
							((DatabaseConnectionPortObjectSpec) pos)
									.getConnectionSettings(
											getCredentialsProvider());
					if (m_upstreamDbSettings != null) {
						// At load, this will be null, and the settings will be
						// lost if attempting to replace
						schemas = createSchemasList(m_upstreamDbSettings,
								getCredentialsProvider(), m_logger,
								tableTypeMdl.getStringValue());
						m_schemaSelector.replaceListItems(schemas, null);

						// Also, we lose the setting on first load if we do not
						// call the loadSettings method here
						schemaNameMdl.loadSettingsFrom(settings);
						break;
					}
				} catch (InvalidSettingsException e) {
					NodeLogger.getLogger(this.getClass())
							.warn("Unable to load settings to dialog");
				}
			}
		}
	}
}
