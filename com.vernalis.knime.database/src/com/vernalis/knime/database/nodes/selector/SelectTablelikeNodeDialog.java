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
package com.vernalis.knime.database.nodes.selector;

import java.awt.Dimension;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
public class SelectTablelikeNodeDialog extends DefaultNodeSettingsPane {

	static final String NO_SCHEMAS = "<--No Schemas-->";
	private static final String SCHEMA_NAME = "Schema Name";
	private static final String TABLE_NAME = "Table Name";
	private static final String TABLE_TYPE = "Table Type";
	private static final int DROPDOWN_WIDTH = 225;
	static final String INCLUDE_SCHEMA_NAME = "Include Schema Name";
	static final String INITIALIZING_DIALOG = "Initializing dialog...";
	static final String NO_METADATA_AVAILABLE = "<No Metadata Available>";
	protected DatabaseConnectionSettings m_upstreamDbSettings;
	protected DialogComponentStringSelection m_typeSelector, m_tableSelector,
			m_schemaSelector;
	protected NodeLogger m_logger = NodeLogger.getLogger(this.getClass());
	private SettingsModelString tableTypeMdl;
	private SettingsModelString tableNameMdl;
	private SettingsModelString schemaNameMdl;
	private SettingsModelBoolean inclSchemaNameMdl;

	/**
	 * Constructor
	 */
	public SelectTablelikeNodeDialog() {
		super();

		tableTypeMdl = createTableTypeModel();
		tableNameMdl = createTableNameModel();
		schemaNameMdl = createSchemaNameModel();
		inclSchemaNameMdl = createIncludeSchemaNamesModel();

		// List the tables K = schema, V = list of tables for Schema
		Map<String, SortedSet<String>> tables =
				createTablesList(m_upstreamDbSettings, getCredentialsProvider(),
						m_logger, tableTypeMdl.getStringValue());

		m_typeSelector = new DialogComponentStringSelection(tableTypeMdl,
				TABLE_TYPE, Arrays.asList(TableTypes.getAllNames()), false,
				createFlowVariableModel(tableTypeMdl));
		m_tableSelector = new DialogComponentStringSelection(tableNameMdl,
				TABLE_NAME, tables.keySet(), false,
				createFlowVariableModel(tableNameMdl));
		m_schemaSelector =
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
				// fixComponentSizes();
			}
		});

		schemaNameMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				updateModelsAfterSchemaChange();
				// fixComponentSizes();
			}
		});
		// tableNameMdl.addChangeListener(new ChangeListener() {
		//
		// @Override
		// public void stateChanged(ChangeEvent e) {
		// fixComponentSizes();
		//
		// }
		// });

		updateModelsAfterTypeChange();
		updateModelsAfterSchemaChange();

		createNewGroup("Select the table");
		addDialogComponent(m_typeSelector);
		addDialogComponent(m_schemaSelector);
		addDialogComponent(m_tableSelector);
		closeCurrentGroup();

		addDialogComponent(new DialogComponentBoolean(inclSchemaNameMdl,
				INCLUDE_SCHEMA_NAME));
	}

	/**
	 */
	protected void updateModelsAfterTypeChange() {
		TreeMap<String, SortedSet<String>> options =
				createTablesList(m_upstreamDbSettings, getCredentialsProvider(),
						m_logger, tableTypeMdl.getStringValue());
		if (options.containsKey(schemaNameMdl.getStringValue())) {
			m_tableSelector.replaceListItems(
					options.get(schemaNameMdl.getStringValue()), null);
		}
		m_schemaSelector.replaceListItems(options.keySet(), null);
		// SettingsModel model = m_schemaSelector.getModel();
		schemaNameMdl.setEnabled(true);
		if (options.isEmpty()) {
			schemaNameMdl.setEnabled(false);
		} else if (options.size() == 1) {
			String val = options.firstKey();
			if (val.equals(INITIALIZING_DIALOG)
					|| val.equals(NO_METADATA_AVAILABLE)
					|| val.equals(NO_SCHEMAS) || (val.startsWith("<No ")
							&& val.endsWith(" tables available>"))) {
				schemaNameMdl.setEnabled(false);
			}
		}
		inclSchemaNameMdl.setEnabled(schemaNameMdl.isEnabled());
	}

	/**
	 */
	protected void updateModelsAfterSchemaChange() {
		SortedSet<String> options =
				createTablesList(m_upstreamDbSettings, getCredentialsProvider(),
						m_logger, tableTypeMdl.getStringValue()).getOrDefault(
								schemaNameMdl.getStringValue(),
								new TreeSet<>(Collections.emptySet()));
		m_tableSelector.replaceListItems(options, null);
		// SettingsModel model = m_tableSelector.getModel();
		tableNameMdl.setEnabled(true);
		if (options.isEmpty()) {
			tableNameMdl.setEnabled(false);
		} else if (options.size() == 1) {
			String val = options.iterator().next();
			if (val.equals(INITIALIZING_DIALOG)
					|| val.equals(NO_METADATA_AVAILABLE)
					|| (val.startsWith("<No ")
							&& val.endsWith(" tables available>"))) {
				tableNameMdl.setEnabled(false);
			}
		}
	}

	/**
	 * Make sure all the labels for the dropdown selectors are the same size to
	 * keep the pane looking vaguely pretty
	 */
	protected void fixLabelSizes() {
		// Bit inefficient, but we only do it once per dialog
		// Have to next so that streams can have it as effectively final
		int maxWidth = Math.max(
				Math.max(Arrays
						.stream(m_tableSelector.getComponentPanel()
								.getComponents())
						.filter(x -> x instanceof JLabel)
						.mapToInt(x -> ((JLabel) x).getPreferredSize().width)
						.findFirst().orElse(0),
						Arrays
								.stream(m_typeSelector.getComponentPanel()
										.getComponents())
								.filter(x -> x instanceof JLabel)
								.mapToInt(x -> ((JLabel) x)
										.getPreferredSize().width)
								.findFirst().orElse(0)),
				Arrays.stream(
						m_schemaSelector.getComponentPanel().getComponents())
						.filter(x -> x instanceof JLabel)
						.mapToInt(x -> ((JLabel) x).getPreferredSize().width)
						.findFirst().orElse(0));

		Arrays.stream(m_tableSelector.getComponentPanel().getComponents())
				.filter(x -> x instanceof JLabel).findFirst()
				.ifPresent(x -> ((JLabel) x).setPreferredSize(
						new Dimension(maxWidth, x.getPreferredSize().height)));
		Arrays.stream(m_schemaSelector.getComponentPanel().getComponents())
				.filter(x -> x instanceof JLabel).findFirst()
				.ifPresent(x -> ((JLabel) x).setPreferredSize(
						new Dimension(maxWidth, x.getPreferredSize().height)));
		Arrays.stream(m_typeSelector.getComponentPanel().getComponents())
				.filter(x -> x instanceof JLabel).findFirst()
				.ifPresent(x -> ((JLabel) x).setPreferredSize(
						new Dimension(maxWidth, x.getPreferredSize().height)));
	}

	/**
	 * Set all the dropdown selectors to the same fixed width
	 */
	protected void fixComponentSizes() {

		JComboBox<?> tabSelectCombo = Arrays
				.stream(m_tableSelector.getComponentPanel().getComponents())
				.filter(x -> x instanceof JComboBox<?>)
				.map(x -> (JComboBox<?>) x).findFirst().get();
		tabSelectCombo.setPreferredSize(new Dimension(DROPDOWN_WIDTH,
				tabSelectCombo.getPreferredSize().height));

		JComboBox<?> tabTypeSelectCombo = Arrays
				.stream(m_typeSelector.getComponentPanel().getComponents())
				.filter(x -> x instanceof JComboBox<?>)
				.map(x -> (JComboBox<?>) x).findFirst().get();
		tabTypeSelectCombo.setPreferredSize(new Dimension(DROPDOWN_WIDTH,
				tabTypeSelectCombo.getPreferredSize().height));

		JComboBox<?> schemaSelectCombo = Arrays
				.stream(m_schemaSelector.getComponentPanel().getComponents())
				.filter(x -> x instanceof JComboBox<?>)
				.map(x -> (JComboBox<?>) x).findFirst().get();
		schemaSelectCombo.setPreferredSize(new Dimension(DROPDOWN_WIDTH,
				schemaSelectCombo.getPreferredSize().height));
		getPanel().revalidate();

		// Dimension tabSelectPrefSize = tabSelectCombo.getPreferredSize();
		// Dimension typeSelectPrefSize = tabTypeSelectCombo.getPreferredSize();
		// Dimension schemaSelectPrefSize =
		// schemaSelectCombo.getPreferredSize();
		// int width = Math.max(
		// Math.max(tabSelectPrefSize.width, typeSelectPrefSize.width),
		// schemaSelectPrefSize.width);
		// tabSelectCombo.setSize(width, tabSelectPrefSize.height);
		// tabTypeSelectCombo.setSize(width, typeSelectPrefSize.height);
		// schemaSelectCombo.setSize(width, schemaSelectPrefSize.height);
		// // schemaSelectCombo.validate();
		// // tabSelectCombo.validate();
		// // tabTypeSelectCombo.validate();
		// getPanel().repaint();

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
	static TreeMap<String, SortedSet<String>> createTablesList(
			DatabaseConnectionSettings dcs, CredentialsProvider cp,
			NodeLogger logger, String tableType) {
		final TreeMap<String, SortedSet<String>> tableNames = new TreeMap<>();

		if (dcs == null || cp == null) {
			// happens on first dialog class instantiaion
			// as loadAdditionalSettings is not called until afetr initial call,
			// but we need something in the dropdown list. Hopefully, this is
			// never actually shown...
			tableNames.put(INITIALIZING_DIALOG,
					new TreeSet<>(Collections.singleton(INITIALIZING_DIALOG)));
			return tableNames;
		}
		try {
			DatabaseMetaData dbMetaData = dcs.getUtility()
					.getReader(new DatabaseQueryConnectionSettings(dcs, ""))
					.getDatabaseMetaData(cp);
			ResultSet rs = dbMetaData.getTables(null, null, "%",
					new String[] { tableType.toUpperCase() });
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
				String msg =
						"<No " + tableType.toLowerCase() + " tables available>";
				tableNames.put(msg, new TreeSet<>(Collections.singleton(msg)));
			}
		} catch (SQLException e) {
			// SQL problem - strange but conceivably possible if the driver is
			// not correctly functioning
			logger.warn("Unable to load " + tableType.toLowerCase() + " names");
			tableNames.put(NO_METADATA_AVAILABLE, new TreeSet<>(
					Collections.singleton(NO_METADATA_AVAILABLE)));
		}
		// return tableNames;
		return tableNames;

	}

	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings,
			PortObjectSpec[] specs) throws NotConfigurableException {
		super.loadAdditionalSettingsFrom(settings, specs);
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
						Map<String, SortedSet<String>> tableNames =
								createTablesList(m_upstreamDbSettings,
										getCredentialsProvider(), m_logger,
										tableTypeMdl.getStringValue());
						m_schemaSelector.replaceListItems(tableNames.keySet(),
								null);
						m_tableSelector.replaceListItems(
								tableNames.get(schemaNameMdl.getStringValue()),
								null);
						// Also, we lose the setting on first load if we do not
						// call the loadSettings method here
						m_tableSelector.loadSettingsFrom(settings, specs);
						break;
					}
				} catch (InvalidSettingsException e) {
					NodeLogger.getLogger(this.getClass())
							.warn("Unable to load settings to dialog");
				}
			}
		}
		updateModelsAfterSchemaChange();
		updateModelsAfterTypeChange();
	}
}
