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
import org.knime.core.node.port.database.DatabaseConnectionPortObjectSpec;

import static com.vernalis.knime.database.nodes.rsettobdt.ListKeysNodeDialog.INITIALIZING_DIALOG;
import static com.vernalis.knime.database.nodes.rsettobdt.ListKeysNodeDialog.NO_METADATA_AVAILABLE;
import static com.vernalis.knime.database.nodes.rsettobdt.ListKeysNodeDialog.createSchemaNameModel;
import static com.vernalis.knime.database.nodes.rsettobdt.ListKeysNodeDialog.createTableNameModel;
import static com.vernalis.knime.database.nodes.rsettobdt.ListKeysNodeDialog.createTablesList;

/**
 * Node Model implementation for the Database Select Table node
 * 
 * @author s.roughley
 *
 */
public class ListPrimaryKeysNodeModel
		extends AbstractResultSetToTableNodeModel {

	protected final SettingsModelString m_tableNameMdl =
			registerSettingsModel(createTableNameModel());
	protected final SettingsModelString m_schemaNameMdl =
			registerSettingsModel(createSchemaNameModel());

	public ListPrimaryKeysNodeModel() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.database.nodes.rsettobdt.
	 * AbstractResultSetToTableNodeModel#configure(org.knime.core.node.port.
	 * database.DatabaseConnectionPortObjectSpec)
	 */
	@Override
	protected void configure(DatabaseConnectionPortObjectSpec inSpec)
			throws InvalidSettingsException {
		if (m_tableNameMdl.getStringValue() == null
				|| "".equals(m_tableNameMdl.getStringValue())) {
			getLogger().error("No table selected");
			throw new InvalidSettingsException("No table selected");
		}
		if (m_tableNameMdl.getStringValue().equals(INITIALIZING_DIALOG)
				|| m_tableNameMdl.getStringValue()
						.equals(NO_METADATA_AVAILABLE)) {
			getLogger().error(
					"Invalid selection - " + m_tableNameMdl.getStringValue());
			throw new InvalidSettingsException(
					"Invalid selection - " + m_tableNameMdl.getStringValue());
		}
		if (m_tableNameMdl.getStringValue().equals("<No tables available>")) {
			getLogger().error("No tables available");
			throw new InvalidSettingsException("No tables available");
		}

		Map<String, SortedSet<String>> dbTableNames =
				createTablesList(getConnectionSettings(inSpec),
						getCredentialsProvider(), getLogger());

		if (!dbTableNames.containsKey(m_schemaNameMdl.getStringValue())
				|| !dbTableNames.get(m_schemaNameMdl.getStringValue())
						.contains(m_tableNameMdl.getStringValue())) {
			getLogger().error("Database connection does not contain table '"
					+ m_tableNameMdl.getStringValue() + "'");
			throw new InvalidSettingsException(
					"Database connection does not contain table '"
							+ m_tableNameMdl.getStringValue() + "'");
		}
	}

	/**
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	@Override
	protected DataCell[] getRowCellsFromResultSet(ResultSet rs)
			throws SQLException {
		return new DataCell[] { new StringCell(rs.getString("COLUMN_NAME")),
				new IntCell(rs.getShort("KEY_SEQ")),
				rs.getString("PK_NAME") == null ? DataType.getMissingCell()
						: new StringCell(rs.getString("PK_NAME")) };
	}

	/**
	 * @param dbMetaData
	 * @return
	 * @throws SQLException
	 */
	@Override
	protected ResultSet getResultSetFromMetaData(DatabaseMetaData dbMetaData)
			throws SQLException {
		return dbMetaData.getPrimaryKeys(null, m_schemaNameMdl.getStringValue(),
				m_tableNameMdl.getStringValue());
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
		newColSpecs[colIdx++] =
				new DataColumnSpecCreator("Primary Key Name", StringCell.TYPE)
						.createSpec();

		return new DataTableSpecCreator().addColumns(newColSpecs).createSpec();
	}

}
