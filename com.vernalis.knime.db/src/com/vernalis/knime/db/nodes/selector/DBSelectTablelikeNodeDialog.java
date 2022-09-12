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
package com.vernalis.knime.db.nodes.selector;

import java.awt.Dimension;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.database.datatype.mapping.DBTypeMappingRegistry;
import org.knime.database.datatype.mapping.DBTypeMappingService;
import org.knime.database.node.datatype.mapping.SettingsModelDatabaseDataTypeMapping;
import org.knime.database.node.util.DBNodeDialogHelper;
import org.knime.database.port.DBSessionPortObjectSpec;
import org.knime.database.session.DBSession;
import org.knime.datatype.mapping.DataTypeMappingConfiguration;
import org.knime.datatype.mapping.DataTypeMappingDirection;
import org.knime.node.datatype.mapping.DialogComponentDataTypeMapping;

import com.vernalis.knime.database.TableTypes;

import static com.vernalis.knime.db.DBConstants.DROPDOWN_WIDTH;
import static com.vernalis.knime.db.DBConstants.EXTERNAL_TO_KNIME_MAPPING;
import static com.vernalis.knime.db.DBConstants.INITIALIZING_DIALOG;
import static com.vernalis.knime.db.DBConstants.INPUT_TYPE_MAPPING;
import static com.vernalis.knime.db.DBConstants.NO_METADATA_AVAILABLE;
import static com.vernalis.knime.db.DBConstants.NO_SCHEMAS;
import static com.vernalis.knime.db.DBConstants.SCHEMA_NAME;
import static com.vernalis.knime.db.DBConstants.TABLE_NAME;
import static com.vernalis.knime.db.DBConstants.TABLE_TYPE;

/**
 * Node Dialog Pane for the Database Select Tables (Interative) node
 * 
 * @author S Roughley
 *
 */
public class DBSelectTablelikeNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * 
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	private static final String NO = "<No ";
	/**
	 * 
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	private static final String TABLES_AVAILABLE = " tables available>";
	private static final String INCLUDE_SCHEMA_NAME = "Include Schema Name";
	private DialogComponentStringSelection typeSelector;
	private DialogComponentStringSelection tableSelector;
	private DialogComponentStringSelection schemaSelector;
	private SettingsModelString tableTypeMdl;
	private SettingsModelString tableNameMdl;
	private SettingsModelString schemaNameMdl;
	private SettingsModelBoolean inclSchemaNameMdl;
	private SortedMap<String, SortedSet<String>> tables;
	private DBSession session;
	private final DialogComponentDataTypeMapping<SQLType> ext2KnimeDiaC =
			new DialogComponentDataTypeMapping<>(
					createExternalToKnimeMappingModel());

	/**
	 * Constructor
	 */
	public DBSelectTablelikeNodeDialog() {
		super();

		tableTypeMdl = createTableTypeModel();
		tableNameMdl = createTableNameModel();
		schemaNameMdl = createSchemaNameModel();
		inclSchemaNameMdl = createIncludeSchemaNamesModel();

		tables = createTablesList(session, tableTypeMdl.getStringValue(),
				getLogger());

		typeSelector = new DialogComponentStringSelection(tableTypeMdl,
				TABLE_TYPE, Arrays.asList(TableTypes.getAllNames()), false,
				createFlowVariableModel(tableTypeMdl));
		tableSelector = new DialogComponentStringSelection(tableNameMdl,
				TABLE_NAME, tables.keySet(), false,
				createFlowVariableModel(tableNameMdl));
		schemaSelector =
				new DialogComponentStringSelection(schemaNameMdl, SCHEMA_NAME,
						tables.getOrDefault(tableNameMdl.getStringValue(),
								new TreeSet<>(Collections.emptySet())),
						false, createFlowVariableModel(schemaNameMdl));
		fixLabelSizes();
		fixComponentSizes();
		tableTypeMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateModelsAfterTypeChange();

			}
		});

		schemaNameMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				updateModelsAfterSchemaChange();

			}
		});

		updateModelsAfterTypeChange();
		updateModelsAfterSchemaChange();

		createNewGroup("Select the table");
		addDialogComponent(typeSelector);
		addDialogComponent(schemaSelector);
		addDialogComponent(tableSelector);
		closeCurrentGroup();

		addDialogComponent(new DialogComponentBoolean(inclSchemaNameMdl,
				INCLUDE_SCHEMA_NAME));
		createNewTab(INPUT_TYPE_MAPPING);
		addDialogComponent(ext2KnimeDiaC);
	}

	private void updateModelsAfterTypeChange() {
		tables = createTablesList(session, tableTypeMdl.getStringValue(),
				getLogger());
		if (tables.containsKey(schemaNameMdl.getStringValue())) {
			tableSelector.replaceListItems(
					tables.get(schemaNameMdl.getStringValue()), null);
		}
		schemaNameMdl.setEnabled(true);
		if (tables.isEmpty()) {
			schemaNameMdl.setEnabled(false);
			return;
		} else if (tables.size() == 1) {
			String val = tables.keySet().iterator().next();// first key
			if (val.equals(INITIALIZING_DIALOG)
					|| val.equals(NO_METADATA_AVAILABLE)
					|| val.equals(NO_SCHEMAS)
					|| (val.startsWith(NO) && val.endsWith(TABLES_AVAILABLE))) {
				schemaNameMdl.setEnabled(false);
			}
		}
		schemaSelector.replaceListItems(tables.keySet(), null);
		inclSchemaNameMdl.setEnabled(schemaNameMdl.isEnabled());
	}

	private void updateModelsAfterSchemaChange() {
		SortedSet<String> options =
				tables.getOrDefault(schemaNameMdl.getStringValue(),
						new TreeSet<>(Collections.emptySet()));
		tableNameMdl.setEnabled(true);
		if (options.isEmpty()) {
			tableNameMdl.setEnabled(false);
			return;
		} else if (options.size() == 1) {
			String val = options.iterator().next();
			if (val.equals(INITIALIZING_DIALOG)
					|| val.equals(NO_METADATA_AVAILABLE)
					|| (val.startsWith(NO) && val.endsWith(TABLES_AVAILABLE))) {
				tableNameMdl.setEnabled(false);
			}
		}
		tableSelector.replaceListItems(options, null);

	}

	/**
	 * Make sure all the labels for the dropdown selectors are the same size to
	 * keep the pane looking vaguely pretty
	 */
	private void fixLabelSizes() {
		// Bit inefficient, but we only do it once per dialog
		// Have to next so that streams can have it as effectively final
		int maxWidth = Math.max(
				Math.max(Arrays
						.stream(tableSelector
								.getComponentPanel().getComponents())
						.filter(x -> x instanceof JLabel)
						.mapToInt(
								x -> ((JLabel) x).getPreferredSize().width)
						.findFirst().orElse(0),
						Arrays.stream(typeSelector.getComponentPanel()
								.getComponents())
								.filter(x -> x instanceof JLabel)
								.mapToInt(x -> ((JLabel) x)
										.getPreferredSize().width)
								.findFirst().orElse(0)),
				Arrays.stream(
						schemaSelector.getComponentPanel().getComponents())
						.filter(x -> x instanceof JLabel)
						.mapToInt(x -> ((JLabel) x).getPreferredSize().width)
						.findFirst().orElse(0));

		Arrays.stream(tableSelector.getComponentPanel().getComponents())
				.filter(x -> x instanceof JLabel).findFirst()
				.ifPresent(x -> ((JLabel) x).setPreferredSize(
						new Dimension(maxWidth, x.getPreferredSize().height)));
		Arrays.stream(schemaSelector.getComponentPanel().getComponents())
				.filter(x -> x instanceof JLabel).findFirst()
				.ifPresent(x -> ((JLabel) x).setPreferredSize(
						new Dimension(maxWidth, x.getPreferredSize().height)));
		Arrays.stream(typeSelector.getComponentPanel().getComponents())
				.filter(x -> x instanceof JLabel).findFirst()
				.ifPresent(x -> ((JLabel) x).setPreferredSize(
						new Dimension(maxWidth, x.getPreferredSize().height)));
	}

	/**
	 * Set all the dropdown selectors to the same fixed width
	 */
	private void fixComponentSizes() {

		JComboBox<?> tabSelectCombo =
				Arrays.stream(tableSelector.getComponentPanel().getComponents())
						.filter(x -> x instanceof JComboBox<?>)
						.map(x -> (JComboBox<?>) x).findFirst().get();
		tabSelectCombo.setPreferredSize(new Dimension(DROPDOWN_WIDTH,
				tabSelectCombo.getPreferredSize().height));

		JComboBox<?> tabTypeSelectCombo =
				Arrays.stream(typeSelector.getComponentPanel().getComponents())
						.filter(x -> x instanceof JComboBox<?>)
						.map(x -> (JComboBox<?>) x).findFirst().get();
		tabTypeSelectCombo.setPreferredSize(new Dimension(DROPDOWN_WIDTH,
				tabTypeSelectCombo.getPreferredSize().height));

		JComboBox<?> schemaSelectCombo = Arrays
				.stream(schemaSelector.getComponentPanel().getComponents())
				.filter(x -> x instanceof JComboBox<?>)
				.map(x -> (JComboBox<?>) x).findFirst().get();
		schemaSelectCombo.setPreferredSize(new Dimension(DROPDOWN_WIDTH,
				schemaSelectCombo.getPreferredSize().height));
		getPanel().revalidate();
	}

	/**
	 * Returns the external to KNIME
	 * {@link SettingsModelDatabaseDataTypeMapping}.
	 *
	 * @return the {@link SettingsModelDatabaseDataTypeMapping}
	 */
	static SettingsModelDatabaseDataTypeMapping
			createExternalToKnimeMappingModel() {
		return new SettingsModelDatabaseDataTypeMapping(
				EXTERNAL_TO_KNIME_MAPPING,
				DataTypeMappingDirection.EXTERNAL_TO_KNIME);
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
	 * @return The settings model for the table name
	 */
	static SettingsModelString createTableNameModel() {
		return new SettingsModelString(TABLE_NAME, "");
	}

	/**
	 * @return The settings model for the 'include schema name' setting
	 */
	static SettingsModelBoolean createIncludeSchemaNamesModel() {
		return new SettingsModelBoolean(INCLUDE_SCHEMA_NAME, true);
	}

	/**
	 * @param dbSession
	 *            The database session
	 * @param tableType
	 *            The table type
	 * @param logger
	 *            a NodeLogger instance for reporting issues
	 * 
	 * @return A map of the Schema names and associated table names for the
	 *         specified type. The Map is sorted by Key, and each value is a
	 *         sorted set
	 */
	static synchronized SortedMap<String, SortedSet<String>> createTablesList(
			DBSession dbSession, String tableType, NodeLogger logger) {
		final TreeMap<String, SortedSet<String>> tableNames = new TreeMap<>();

		if (dbSession == null) {
			// happens on first dialog class instantiaion
			// as loadAdditionalSettings is not called until afetr initial call,
			// but we need something in the dropdown list. Hopefully, this is
			// never actually shown...
			tableNames.put(INITIALIZING_DIALOG,
					new TreeSet<>(Collections.singleton(INITIALIZING_DIALOG)));
			return tableNames;
		}
		ExecutionMonitor exec = new ExecutionMonitor();
		try (Connection connection =
				dbSession.getConnectionProvider().getConnection(exec);
				ResultSet rs = connection.getMetaData().getTables(null, null,
						"%", new String[] { tableType.toUpperCase() });
				// We dont use the statement but otherwise a warning is thrown
				// that it isnt closed
				Statement statement = rs.getStatement()) {
			while (rs.next()) {
				String tableName = rs.getString("TABLE_NAME");
				String schemaName = rs.getString("TABLE_SCHEM");
				if (schemaName == null) {
					schemaName = NO_SCHEMAS;
				}
				if (!tableNames.containsKey(schemaName)) {
					tableNames.put(schemaName, new TreeSet<>());
				}
				tableNames.get(schemaName).add(tableName);

			}
			if (tableNames.isEmpty()) {
				String msg = NO + tableType.toLowerCase() + TABLES_AVAILABLE;
				tableNames.put(msg, new TreeSet<>(Collections.singleton(msg)));
			}
		} catch (SQLException e) {
			// SQL problem - strange but conceivably possible if the driver is
			// not correctly functioning
			logger.warn("Unable to load " + tableType.toLowerCase() + " names");
			tableNames.clear();
			tableNames.put(NO_METADATA_AVAILABLE, new TreeSet<>(
					Collections.singleton(NO_METADATA_AVAILABLE)));
		} catch (CanceledExecutionException e1) {
			logger.warn("User cancelled metadata load");
			tableNames.clear();
			tableNames.put(NO_METADATA_AVAILABLE, new TreeSet<>(
					Collections.singleton(NO_METADATA_AVAILABLE)));
		}
		// return tableNames;
		return tableNames;

	}

	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings,
			PortObjectSpec[] specs) throws NotConfigurableException {

		final DBSessionPortObjectSpec sessionSpec =
				DBNodeDialogHelper.asDBSessionPortObjectSpec(specs[0]);

		try {
			if (sessionSpec != null && sessionSpec.isSessionExists()) {
				session = sessionSpec.getDBSession();
				// At load, this will be null, and the settings will be
				// lost if attempting to replace
				tables = createTablesList(session,
						tableTypeMdl.getStringValue(), getLogger());
				schemaSelector.replaceListItems(tables.keySet(), null);
				tableSelector.replaceListItems(
						tables.get(schemaNameMdl.getStringValue()), null);
				// Also, we lose the setting on first load if we do not
				// call the loadSettings method here
				schemaNameMdl.loadSettingsFrom(settings);
				tableNameMdl.loadSettingsFrom(settings);

				// Handle the type mapping
				final DBTypeMappingService<?, ?> mappingService =
						DBTypeMappingRegistry.getInstance()
								.getDBTypeMappingService(session.getDBType());
				ext2KnimeDiaC.setMappingService(mappingService);

				final DataTypeMappingConfiguration<SQLType> externalToKnime =
						sessionSpec.getExternalToKnimeTypeMapping().resolve(
								mappingService,
								DataTypeMappingDirection.EXTERNAL_TO_KNIME);
				ext2KnimeDiaC
						.setInputDataTypeMappingConfiguration(externalToKnime);
			}
		} catch (InvalidSettingsException e) {
			getLogger().warn("Unable to load settings to dialog");
		}
		updateModelsAfterSchemaChange();
	}
}
