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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.SortedSet;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.database.port.DBSessionPortObjectSpec;

import static com.vernalis.knime.db.DBConstants.INITIALIZING_DIALOG;
import static com.vernalis.knime.db.DBConstants.NO_METADATA_AVAILABLE;
import static com.vernalis.knime.db.nodes.rsettobdt.DBListKeysNodeDialog.createSchemaNameModel;
import static com.vernalis.knime.db.nodes.rsettobdt.DBListKeysNodeDialog.createTableNameModel;
import static com.vernalis.knime.db.nodes.rsettobdt.DBListKeysNodeDialog.createTablesList;

/**
 * {@link AbstractDBResultSetToTableNodeModel} implementation for the DB List
 * Primary Keys node
 * 
 * @author S Roughley
 *
 */
public class DBListPrimaryKeysNodeModel
		extends AbstractDBResultSetToTableNodeModel {

	/** Settings Model for the Table Name */
	protected final SettingsModelString tableNameMdl =
			registerSettingsModel(createTableNameModel());

	/** Settings Model for the Schema Name */
	protected final SettingsModelString schemaNameMdl =
			registerSettingsModel(createSchemaNameModel());

	/**
	 * Constructor
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	DBListPrimaryKeysNodeModel() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.db.nodes.rsettobdt.
	 * AbstractDBResultSetToTableNodeModel#configure(org.knime.core.node.port.
	 * database.DatabaseConnectionPortObjectSpec)
	 */
	@Override
	protected void configure(DBSessionPortObjectSpec inSpec)
			throws InvalidSettingsException {
		if (tableNameMdl.getStringValue() == null
				|| "".equals(tableNameMdl.getStringValue())) {
			getLogger().error("No table selected");
			throw new InvalidSettingsException("No table selected");
		}
		if (tableNameMdl.getStringValue().equals(INITIALIZING_DIALOG)
				|| tableNameMdl.getStringValue()
						.equals(NO_METADATA_AVAILABLE)) {
			getLogger().error(
					"Invalid selection - " + tableNameMdl.getStringValue());
			throw new InvalidSettingsException(
					"Invalid selection - " + tableNameMdl.getStringValue());
		}
		if (tableNameMdl.getStringValue().equals("<No tables available>")) {
			getLogger().error("No tables available");
			throw new InvalidSettingsException("No tables available");
		}

		Map<String, SortedSet<String>> dbTableNames =
				createTablesList(inSpec.getDBSession(), getLogger());

		if (!dbTableNames.containsKey(schemaNameMdl.getStringValue())
				|| !dbTableNames.get(schemaNameMdl.getStringValue())
						.contains(tableNameMdl.getStringValue())) {
			getLogger().error("Database connection does not contain table '"
					+ tableNameMdl.getStringValue() + "'");
			throw new InvalidSettingsException(
					"Database connection does not contain table '"
							+ tableNameMdl.getStringValue() + "'");
		}
	}

	@Override
	protected DataCell[] getRowCellsFromResultSet(ResultSet rs)
			throws SQLException {
		return new DataCell[] { new StringCell(rs.getString("COLUMN_NAME")),
				new IntCell(rs.getShort("KEY_SEQ")),
				rs.getString("PK_NAME") == null ? DataType.getMissingCell()
						: new StringCell(rs.getString("PK_NAME")) };
	}

	@Override
	protected ResultSet getResultSetFromMetaData(DatabaseMetaData dbMetaData)
			throws SQLException {
		return dbMetaData.getPrimaryKeys(null, schemaNameMdl.getStringValue(),
				tableNameMdl.getStringValue());
	}

	@Override
	protected DataTableSpec getOutputSpec() {
		DataColumnSpec[] newColSpecs = new DataColumnSpec[3];
		int colIdx = 0;
		newColSpecs[colIdx++] =
				new DataColumnSpecCreator("Column Name", StringCell.TYPE)
						.createSpec();
		newColSpecs[colIdx++] =
				new DataColumnSpecCreator("Key Sequence", IntCell.TYPE)
						.createSpec();
		newColSpecs[colIdx] =
				new DataColumnSpecCreator("Primary Key Name", StringCell.TYPE)
						.createSpec();

		return new DataTableSpecCreator().addColumns(newColSpecs).createSpec();
	}

}
