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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.database.node.util.DBNodeDialogHelper;
import org.knime.database.port.DBSessionPortObjectSpec;
import org.knime.database.session.DBSession;

import com.vernalis.knime.database.TableTypes;

import static com.vernalis.knime.db.DBConstants.INITIALIZING_DIALOG;
import static com.vernalis.knime.db.DBConstants.NO_METADATA_AVAILABLE;
import static com.vernalis.knime.db.DBConstants.NO_SCHEMAS;
import static com.vernalis.knime.db.DBConstants.SCHEMA_NAME;
import static com.vernalis.knime.db.DBConstants.TABLE_TYPE;

/**
 * Node Dialog Pane for the DB List Tables node
 * 
 * @since 07-Sep-2022
 * @since v1.36.0

 * @author S Roughley
 *
 */
public class DBListTablesNodeDialog extends DefaultNodeSettingsPane {

	private DialogComponentStringSelection typeSelector;
	private DialogComponentStringSelection schemaSelector;
	private SettingsModelString tableTypeMdl;
	private SettingsModelString schemaNameMdl;
	private SortedSet<String> schemas;
	private DBSession session;

	/**
	 * Constructor
	 */
	public DBListTablesNodeDialog() {
		super();

		tableTypeMdl = createTableTypeModel();
		schemaNameMdl = createSchemaNameModel();

		schemas = createSchemasList(session, tableTypeMdl.getStringValue(),
				getLogger());

		typeSelector = new DialogComponentStringSelection(tableTypeMdl,
				TABLE_TYPE, Arrays.asList(TableTypes.getAllNames()), false,
				createFlowVariableModel(tableTypeMdl));
		schemaSelector =
				new DialogComponentStringSelection(schemaNameMdl, SCHEMA_NAME,
						schemas, false, createFlowVariableModel(schemaNameMdl));
		tableTypeMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateModelsAfterTypeChange();

			}
		});

		updateModelsAfterTypeChange();

		addDialogComponent(typeSelector);
		addDialogComponent(schemaSelector);

	}

	private void updateModelsAfterTypeChange() {

		schemas = createSchemasList(session, tableTypeMdl.getStringValue(),
				getLogger());
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
		schemaSelector.replaceListItems(schemas, null);
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
	 * @param dbSession
	 *            the DBSession with the connection and setting details
	 * @param tableType
	 *            the table type
	 * @param logger
	 *            a logger instance to report errors
	 * 
	 * @return A map of the Schema names and associated table names for the
	 *         specified type. The Map is sorted by Key, and each value is a
	 *         sorted set
	 */
	static SortedSet<String> createSchemasList(DBSession dbSession,
			String tableType, NodeLogger logger) {
		if (dbSession == null) {
			// happens on first dialog class instantiaion
			// as loadAdditionalSettings is not called until afetr initial call,
			// but we need something in the dropdown list. Hopefully, this is
			// never actually shown...
			return new TreeSet<>(Collections.singleton(INITIALIZING_DIALOG));
		}

		final SortedSet<String> schemaNames = new TreeSet<>();
		ExecutionMonitor exec = new ExecutionMonitor();
		try (Connection connection =
				dbSession.getConnectionProvider().getConnection(exec);
				ResultSet rs = connection.getMetaData().getSchemas();
				// We dont use the statement but otherwise a warning is thrown
				// that it isnt closed
				Statement statement = rs.getStatement()) {

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
			schemaNames.clear();
			schemaNames.add(NO_METADATA_AVAILABLE);
		} catch (CanceledExecutionException e1) {
			logger.warn("User cancelled metadata load");
			schemaNames.clear();
			schemaNames.add(NO_METADATA_AVAILABLE);
		}
		return schemaNames;

	}

	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings,
			PortObjectSpec[] specs) throws NotConfigurableException {

		final DBSessionPortObjectSpec sessionSpec =
				DBNodeDialogHelper.asDBSessionPortObjectSpec(specs[0]);

		try {
			if (sessionSpec != null && sessionSpec.isSessionExists()) {
				session = sessionSpec.getDBSession();
				schemas = createSchemasList(session,
						tableTypeMdl.getStringValue(), getLogger());
				schemaSelector.replaceListItems(schemas, null);

				// Also, we lose the setting on first load if we do not
				// call the loadSettings method here
				schemaNameMdl.loadSettingsFrom(settings);
			}
		} catch (InvalidSettingsException e) {
			getLogger().warn("Unable to load settings to dialog");
		}
	}
}
