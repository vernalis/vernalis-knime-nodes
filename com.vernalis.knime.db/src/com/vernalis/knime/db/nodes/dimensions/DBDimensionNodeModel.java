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
package com.vernalis.knime.db.nodes.dimensions;

import java.sql.SQLType;

import org.knime.base.node.io.database.DBNodeModel;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DirectAccessTable.UnknownRowCountException;
import org.knime.core.data.LongValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.convert.map.ProductionPath;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.LongCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.database.DBDataObject;
import org.knime.database.DBTableSpec;
import org.knime.database.SQLQuery;
import org.knime.database.agent.metadata.DBMetadataReader;
import org.knime.database.agent.reader.DBReader;
import org.knime.database.agent.reader.FileStoreFactoryCreator;
import org.knime.database.agent.reader.ReaderFunction;
import org.knime.database.datatype.mapping.DBTypeMappingRegistry;
import org.knime.database.datatype.mapping.DBTypeMappingService;
import org.knime.database.dialect.DBSQLDialect;
import org.knime.database.port.DBDataPortObject;
import org.knime.database.port.DBDataPortObjectSpec;
import org.knime.database.session.DBSession;
import org.knime.datatype.mapping.DataTypeMappingConfiguration;
import org.knime.datatype.mapping.DataTypeMappingDirection;

/**
 * {@link DBNodeModel} implementation for the DB Dimension node
 * 
 * @author S Roughley
 *
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public class DBDimensionNodeModel extends DBNodeModel {

	private static final String NUMBER_COLUMNS = "Number Columns";
	private static final String NUMBER_ROWS = "Number Rows";
	private DataTableSpec outTableSpec;

	/**
	 * Constructor
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	DBDimensionNodeModel() {
		super(new PortType[] { DBDataPortObject.TYPE },
				new PortType[] { BufferedDataTable.TYPE });
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

		final DBDataPortObject dbObject = (DBDataPortObject) inObjects[0];
		final DBSession dbSession = dbObject.getDBSession();
		final DBSQLDialect dialect = dbSession.getDialect();

		exec.setProgress("Opening database connection...");
		long numRows;
		try {
			numRows = dbObject.getRowCount();
		} catch (UnknownRowCountException e) {

			// Use the dialect to generate a count query for the incoming query
			final SQLQuery countQuery = new SQLQuery(dialect.asTable(
					dialect.dataManipulation().selectCount().getPart()
							+ " FROM (" + dbObject.getData().getQuery() + "\n)",
					dialect.getTempTableName()));

			// Figure the type mapping
			final DBTypeMappingService<?, ?> mappingService =
					DBTypeMappingRegistry.getInstance()
							.getDBTypeMappingService(dbSession.getDBType());
			DataTypeMappingConfiguration<SQLType> extToKnime = dbObject
					.getExternalToKnimeTypeMapping().resolve(mappingService,
							DataTypeMappingDirection.EXTERNAL_TO_KNIME);

			// Now create a new 'temporary' DBDataPortObject with the count
			// query, which we will then read
			DBDataObject tempObj = dbSession.getAgent(DBMetadataReader.class)
					.getDBDataObject(exec, countQuery, extToKnime);
			DBDataPortObject tempDataPort =
					new DBDataPortObject(dbObject, tempObj);
			DBTableSpec tempTableSpec = tempObj.getDBTableSpec();
			ProductionPath[] tempProductionPaths =
					dbObject.getExternalToKnimeTypeMapping()
							.resolve(mappingService,
									DataTypeMappingDirection.EXTERNAL_TO_KNIME)
							.getProductionPathsFor(
									tempTableSpec.toExternalDataTableSpec());
			// A function which returns the first cell in the first row of the
			// result table from the count query, or -1L
			ReaderFunction<LongValue> function = output -> {
				while (output.hasNext()) {
					final DataCell cell = output.next().getCell(0);
					if (cell.isMissing()) {
						return new LongCell(-1);
					}
					// For reasons unclear, cell is a StringCell for some DBs
					// (SQLite), even though it
					// is an IntCell in a table preview with the same SQL
					if (cell instanceof LongValue) {
						return (LongValue) cell;
					} else if (cell instanceof StringValue) {
						return new LongCell(Long.parseLong(
								((StringValue) cell).getStringValue()));
					}
				}
				// We dont know...
				return new LongCell(-1);
			};
			numRows =
					tempDataPort.getDBSession().getAgent(DBReader.class)
							.read(exec, FileStoreFactoryCreator
									.createFileStoreFactoryCreator(exec),
									function, countQuery, tempTableSpec,
									tempProductionPaths)
							.getLongValue();
		}

		final BufferedDataContainer bdc =
				exec.createDataContainer(outTableSpec);
		bdc.addRowToTable(new DefaultRow(NUMBER_ROWS, new LongCell(numRows)));

		final int numCols = dbObject.getDataTableSpec().getNumColumns();
		bdc.addRowToTable(
				new DefaultRow(NUMBER_COLUMNS, new LongCell(numCols)));
		bdc.close();
		pushFlowVariableDouble(NUMBER_ROWS, numRows);
		pushFlowVariableInt(NUMBER_COLUMNS, numCols);
		return new BufferedDataTable[] { bdc.getTable() };
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

		pushFlowVariableDouble(NUMBER_ROWS, -1);

		final DBDataPortObjectSpec dbPortSpec =
				(DBDataPortObjectSpec) inSpecs[0];
		final DBTableSpec tableSpec = dbPortSpec.getData().getDBTableSpec();
		pushFlowVariableInt(NUMBER_COLUMNS, tableSpec.getColumnCount());

		// The output table has a single column
		outTableSpec = new DataTableSpecCreator().addColumns(
				new DataColumnSpecCreator("Dimensions", LongCell.TYPE)
						.createSpec())
				.createSpec();
		return new DataTableSpec[] { outTableSpec };
	}

}
