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
package com.vernalis.knime.db.nodes.replaceheader;

import java.sql.SQLType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.database.DBDataObject;
import org.knime.database.SQLQuery;
import org.knime.database.agent.columnrename.DBColumnRename;
import org.knime.database.agent.metadata.DBMetadataReader;
import org.knime.database.datatype.mapping.DBTypeMappingRegistry;
import org.knime.database.datatype.mapping.DBTypeMappingService;
import org.knime.database.node.DBNodeModel;
import org.knime.database.node.util.PortObjectSpecHelper;
import org.knime.database.port.DBDataPortObject;
import org.knime.database.session.DBSession;
import org.knime.datatype.mapping.DataTypeMappingConfiguration;
import org.knime.datatype.mapping.DataTypeMappingDirection;

import com.vernalis.knime.nodes.SettingsModelRegistry;

import static com.vernalis.knime.db.nodes.replaceheader.DBReplaceColumnHeaderNodeDialog.STRING_FILTER;
import static com.vernalis.knime.db.nodes.replaceheader.DBReplaceColumnHeaderNodeDialog.createLookupColumnNameModel;
import static com.vernalis.knime.db.nodes.replaceheader.DBReplaceColumnHeaderNodeDialog.createMissingColumnModel;
import static com.vernalis.knime.db.nodes.replaceheader.DBReplaceColumnHeaderNodeDialog.createValueColumnNameModel;

/**
 * {@link DBNodeModel} implementatin for the DB Replace Column Header node
 * 
 * @author S Roughley
 *
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public class DBReplaceColumnHeaderNodeModel extends DBNodeModel {

	private final Set<SettingsModel> models = new LinkedHashSet<>();
	private final SettingsModelRegistry smr = new SettingsModelRegistry() {

		@Override
		public Set<SettingsModel> getModels() {
			return models;
		}
	};
	private final SettingsModelString lookupColNameMdl =
			smr.registerSettingsModel(createLookupColumnNameModel());
	private final SettingsModelString valueColNameMdl =
			smr.registerSettingsModel(createValueColumnNameModel());
	private final SettingsModelString missingColumnActionModel =
			smr.registerSettingsModel(createMissingColumnModel());

	private int lookupColIdx;
	private int valueColIdx;
	private MissingColumnAction missingColumnAction;

	/**
	 * Constructor
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	DBReplaceColumnHeaderNodeModel() {
		super(new PortType[] { DBDataPortObject.TYPE, BufferedDataTable.TYPE },
				new PortType[] { DBDataPortObject.TYPE });
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
		DataTableSpec lookupSpec =
				PortObjectSpecHelper.asDataTableSpec(inSpecs[1]);
		valueColIdx = smr.getValidatedColumnSelectionModelColumnIndex(
				valueColNameMdl, STRING_FILTER, lookupSpec, getLogger(),
				lookupColNameMdl);
		lookupColIdx = smr.getValidatedColumnSelectionModelColumnIndex(
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
		// Now we need to generate the new output

		final DBDataPortObject dbInPort = (DBDataPortObject) inObjects[0];
		DBSession session = dbInPort.getDBSession();
		DBDataObject data = dbInPort.getData();

		List<String> incomingColNamesToKeep = new ArrayList<>();
		for (final String colName : data.getKNIMETableSpec().getColumnNames()) {
			if (!missingColumnAction.testColumn(colName, colNameMap,
					incomingColNamesToKeep)) {
				throw new RuntimeException("Column '" + colName
						+ "' is not mapped to the output table");
			}
		}

		final SQLQuery renamedQuery = session.getAgent(DBColumnRename.class)
				.createRenamedQuery(data.getQuery(),
						incomingColNamesToKeep.toArray(
								new String[incomingColNamesToKeep.size()]),
						colNameMap);

		final DBTypeMappingService<?, ?> mappingService = DBTypeMappingRegistry
				.getInstance().getDBTypeMappingService(session.getDBType());
		DataTypeMappingConfiguration<SQLType> externalToKnime =
				dbInPort.getExternalToKnimeTypeMapping().resolve(mappingService,
						DataTypeMappingDirection.EXTERNAL_TO_KNIME);

		DBDataObject outDbData = session.getAgent(DBMetadataReader.class)
				.getDBDataObject(exec, renamedQuery, externalToKnime);
		exec.setProgress(1.0);
		return new PortObject[] { new DBDataPortObject(dbInPort, outDbData) };
	}

	@Override
	public void saveSettingsToInternal(NodeSettingsWO settings) {
		smr.saveSettingsTo(settings);

	}

	@Override
	public void validateSettingsInternal(NodeSettingsRO settings)
			throws InvalidSettingsException {
		smr.validateSettings(settings);

	}

	@Override
	public void loadValidatedSettingsFromInternal(NodeSettingsRO settings)
			throws InvalidSettingsException {
		smr.loadValidatedSettingsFrom(settings);

	}

}
