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

import java.sql.SQLException;
import java.sql.SQLType;
import java.util.SortedMap;
import java.util.SortedSet;

import org.knime.base.node.io.database.DBNodeModel;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.database.DBDataObject;
import org.knime.database.SQLQuery;
import org.knime.database.agent.metadata.DBMetadataReader;
import org.knime.database.datatype.mapping.DBTypeMappingRegistry;
import org.knime.database.datatype.mapping.DBTypeMappingService;
import org.knime.database.model.DBTable;
import org.knime.database.model.impl.DefaultDBTable;
import org.knime.database.node.datatype.mapping.SettingsModelDatabaseDataTypeMapping;
import org.knime.database.node.util.ConfigureExecutionMonitor;
import org.knime.database.node.util.PortObjectSpecHelper;
import org.knime.database.port.DBDataPortObject;
import org.knime.database.port.DBDataPortObjectSpec;
import org.knime.database.port.DBPortObject;
import org.knime.database.port.DBSessionPortObject;
import org.knime.database.port.DBSessionPortObjectSpec;
import org.knime.database.session.DBSession;
import org.knime.datatype.mapping.DataTypeMappingConfiguration;
import org.knime.datatype.mapping.DataTypeMappingDirection;
import org.knime.node.datatype.mapping.DataTypeMappingConfigurationData;

import static com.vernalis.knime.db.DBConstants.INITIALIZING_DIALOG;
import static com.vernalis.knime.db.DBConstants.NO_METADATA_AVAILABLE;
import static com.vernalis.knime.db.DBConstants.NO_SCHEMAS;
import static com.vernalis.knime.db.nodes.selector.DBSelectTablelikeNodeDialog.createExternalToKnimeMappingModel;
import static com.vernalis.knime.db.nodes.selector.DBSelectTablelikeNodeDialog.createIncludeSchemaNamesModel;
import static com.vernalis.knime.db.nodes.selector.DBSelectTablelikeNodeDialog.createSchemaNameModel;
import static com.vernalis.knime.db.nodes.selector.DBSelectTablelikeNodeDialog.createTableNameModel;
import static com.vernalis.knime.db.nodes.selector.DBSelectTablelikeNodeDialog.createTableTypeModel;
import static com.vernalis.knime.db.nodes.selector.DBSelectTablelikeNodeDialog.createTablesList;

/**
 * Node Model implementation for the Database Select Table node
 * 
 * @author S Roughley
 *
 */
public class DBSelectTablelikeNodeModel extends DBNodeModel {

	private static final String NO_TABLE_TYPE_SELECTED =
			"No table type selected";
	private final SettingsModelString tableTypeMdl = createTableTypeModel();
	private final SettingsModelString tableNameMdl = createTableNameModel();
	private final SettingsModelString schemaNameMdl = createSchemaNameModel();
	private final SettingsModelBoolean inclSchemaNamesMdl =
			createIncludeSchemaNamesModel();

	private final SettingsModelDatabaseDataTypeMapping externalToKnimeMdl =
			createExternalToKnimeMappingModel();
	private DBDataPortObjectSpec outputSpec;

	/**
	 * Constructor
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	DBSelectTablelikeNodeModel() {
		super(new PortType[] { DBSessionPortObject.TYPE },
				new PortType[] { DBDataPortObject.TYPE });

	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {

		DBSessionPortObject inDb = (DBSessionPortObject) inObjects[0];

		final DataTypeMappingConfiguration<SQLType> ext2KnimeTypeMapping =
				createExternalToKnimeTypeMapping(inDb);
		DBDataObject data =
				outputSpec == null
						? createDBDataObject(exec, inDb.getDBSession(),
								ext2KnimeTypeMapping)
						: outputSpec.getData();
		return new PortObject[] { new DBDataPortObject(
				inDb.getSessionSummary()
						.orElseThrow(() -> new InvalidSettingsException(
								"No DB Session summary available")),
				inDb.getKnimeToExternalTypeMapping(),
				DataTypeMappingConfigurationData.from(ext2KnimeTypeMapping),
				data) };

	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {

		if (tableTypeMdl.getStringValue() == null
				|| tableTypeMdl.getStringValue().isEmpty()) {
			getLogger().error(NO_TABLE_TYPE_SELECTED);
			throw new InvalidSettingsException(NO_TABLE_TYPE_SELECTED);
		}

		if (tableNameMdl.getStringValue() == null
				|| "".equals(tableNameMdl.getStringValue())) {
			getLogger()
					.error("No " + tableTypeMdl.getStringValue().toLowerCase()
							+ " selected");
			throw new InvalidSettingsException(
					"No " + tableTypeMdl.getStringValue().toLowerCase()
							+ " selected");
		}
		if (tableNameMdl.getStringValue().equals(INITIALIZING_DIALOG)
				|| tableNameMdl.getStringValue()
						.equals(NO_METADATA_AVAILABLE)) {
			getLogger().error(
					"Invalid selection - " + tableNameMdl.getStringValue());
			throw new InvalidSettingsException(
					"Invalid selection - " + tableNameMdl.getStringValue());
		}
		if (tableNameMdl.getStringValue()
				.equals("<No " + tableTypeMdl.getStringValue().toLowerCase()
						+ " tables available>")) {
			getLogger()
					.error("No " + tableTypeMdl.getStringValue().toLowerCase()
							+ " tables available");
			throw new InvalidSettingsException(
					"No " + tableTypeMdl.getStringValue().toLowerCase()
							+ " tables available");
		}

		final DBSessionPortObjectSpec inSpec = PortObjectSpecHelper
				.asDBSessionPortObjectSpec(inSpecs[0], true);

		final DBSession dbSession = inSpec.getDBSession();

		SortedMap<String, SortedSet<String>> dbTableNames = createTablesList(
				dbSession, tableTypeMdl.getStringValue(), getLogger());
		if (!dbTableNames.containsKey(schemaNameMdl.getStringValue())
				|| !dbTableNames.get(schemaNameMdl.getStringValue())
						.contains(tableNameMdl.getStringValue())) {
			getLogger().error("Database connection does not contain "
					+ tableTypeMdl.getStringValue() + " '"
					+ tableNameMdl.getStringValue() + "'");
			throw new InvalidSettingsException(
					"Database connection does not contain "
							+ tableTypeMdl.getStringValue() + " '"
							+ tableNameMdl.getStringValue() + "'");
		}

		final DataTypeMappingConfiguration<SQLType> externalToKnime =
				createExternalToKnimeTypeMapping(inSpec);

		if (!externalToKnime.isValid()) {
			throw new InvalidSettingsException(
					"The data type mapping configuration is not valid.");
		}

		ConfigureExecutionMonitor cem =
				new ConfigureExecutionMonitor(dbSession);
		outputSpec = DBDataPortObjectSpec.create(cem, inSpec.getSessionID(),
				inSpec.getKnimeToExternalTypeMapping(),
				inSpec.getExternalToKnimeTypeMapping(),
				() -> createDBDataObject(cem, dbSession, externalToKnime));
		return new PortObjectSpec[] { outputSpec };
	}

	private DataTypeMappingConfiguration<SQLType>
			createExternalToKnimeTypeMapping(final DBPortObject port)
					throws InvalidSettingsException {

		final DBTypeMappingService<?, ?> mappingService =
				DBTypeMappingRegistry.getInstance().getDBTypeMappingService(
						port.getDBSession().getDBType());

		return port.getExternalToKnimeTypeMapping()
				.resolve(mappingService,
						DataTypeMappingDirection.EXTERNAL_TO_KNIME)
				.with(externalToKnimeMdl
						.getDataTypeMappingConfiguration(mappingService));
	}

	private DBDataObject createDBDataObject(final ExecutionMonitor exec,
			final DBSession session,
			final DataTypeMappingConfiguration<SQLType> externalToKnime)
			throws CanceledExecutionException, InvalidSettingsException,
			SQLException {

		final String tableName = tableNameMdl.getStringValue();
		final boolean inclSchemaName = inclSchemaNamesMdl.isEnabled()
				&& inclSchemaNamesMdl.getBooleanValue()
				&& !schemaNameMdl.getStringValue().equals(NO_SCHEMAS);
		DBTable tbl = inclSchemaName
				? new DefaultDBTable(tableName, schemaNameMdl.getStringValue())
				: new DefaultDBTable(tableName);

		SQLQuery sql = session.getDialect().dataManipulation()
				.createSelectStatement(tbl);

		return session.getAgent(DBMetadataReader.class).getDBDataObject(exec,
				sql, externalToKnime);
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		tableTypeMdl.saveSettingsTo(settings);
		tableNameMdl.saveSettingsTo(settings);
		inclSchemaNamesMdl.saveSettingsTo(settings);
		schemaNameMdl.saveSettingsTo(settings);
		externalToKnimeMdl.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		tableTypeMdl.validateSettings(settings);
		tableNameMdl.validateSettings(settings);
		inclSchemaNamesMdl.validateSettings(settings);
		schemaNameMdl.validateSettings(settings);
		externalToKnimeMdl.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		tableTypeMdl.loadSettingsFrom(settings);
		tableNameMdl.loadSettingsFrom(settings);
		inclSchemaNamesMdl.loadSettingsFrom(settings);
		schemaNameMdl.loadSettingsFrom(settings);
		externalToKnimeMdl.loadSettingsFrom(settings);
	}

	@Override
	protected void reset() {

	}

}
