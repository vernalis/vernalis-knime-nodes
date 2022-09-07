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
import java.util.SortedSet;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.database.DatabaseConnectionPortObjectSpec;

import com.vernalis.knime.misc.ArrayUtils;

import static com.vernalis.knime.database.nodes.rsettobdt.ListTablesNodeDialog.NO_SCHEMAS;
import static com.vernalis.knime.database.nodes.rsettobdt.ListTablesNodeDialog.createSchemaNameModel;
import static com.vernalis.knime.database.nodes.rsettobdt.ListTablesNodeDialog.createSchemasList;
import static com.vernalis.knime.database.nodes.rsettobdt.ListTablesNodeDialog.createTableTypeModel;

/**
 * Node Model implementation for the Database Select Table node
 * 
 * @author s.roughley
 *
 */
public class ListTablesNodeModel extends AbstractResultSetToTableNodeModel {

	private static final String NO_TABLE_TYPE_SELECTED =
			"No table type selected";
	protected final SettingsModelString m_tableTypeMdl =
			registerSettingsModel(createTableTypeModel());
	protected final SettingsModelString m_schemaNameMdl =
			registerSettingsModel(createSchemaNameModel());

	public ListTablesNodeModel() {
		super();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.database.nodes.rsettobdt.
	 * AbstractResultSetToTableNodeModel#getRowCellsFromResultSet(java.sql.
	 * ResultSet)
	 */
	@Override
	protected DataCell[] getRowCellsFromResultSet(ResultSet rs)
			throws SQLException {
		DataCell[] retVal =
				ArrayUtils.fill(new DataCell[3], DataType.getMissingCell());
		int colIdx = 0;
		retVal[colIdx++] = new StringCell(rs.getString("TABLE_NAME"));
		if (rs.getString("TABLE_CAT") != null) {
			retVal[colIdx] = new StringCell(rs.getString("TABLE_CAT"));
		}
		colIdx++;
		if (rs.getString("REMARKS") != null) {
			retVal[colIdx] = new StringCell(rs.getString("REMARKS"));
		}
		colIdx++;

		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.database.nodes.rsettobdt.
	 * AbstractResultSetToTableNodeModel#getResultSetFromMetaData(java.sql.
	 * DatabaseMetaData)
	 */
	@Override
	protected ResultSet getResultSetFromMetaData(DatabaseMetaData dbMetaData)
			throws SQLException {
		return dbMetaData.getTables(null,
				m_schemaNameMdl.getStringValue().equals(NO_SCHEMAS) ? null
						: m_schemaNameMdl.getStringValue(),
				"%", new String[] { m_tableTypeMdl.getStringValue() });
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
		if (m_tableTypeMdl.getStringValue() == null
				|| m_tableTypeMdl.getStringValue().isEmpty()) {
			getLogger().error(NO_TABLE_TYPE_SELECTED);
			throw new InvalidSettingsException(NO_TABLE_TYPE_SELECTED);
		}

		SortedSet<String> dbTableNames = createSchemasList(
				getConnectionSettings(inSpec), getCredentialsProvider(),
				getLogger(), m_tableTypeMdl.getStringValue());
		if (!dbTableNames.contains(m_schemaNameMdl.getStringValue())) {
			getLogger().error("Database connection does not contain "
					+ m_tableTypeMdl.getStringValue() + " schema '"
					+ m_schemaNameMdl.getStringValue() + "'");
			throw new InvalidSettingsException(
					"Database connection does not contain "
							+ m_tableTypeMdl.getStringValue() + " '"
							+ m_schemaNameMdl.getStringValue() + "'");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.database.nodes.rsettobdt.
	 * AbstractResultSetToTableNodeModel#getOutputSpec()
	 */
	@Override
	protected DataTableSpec getOutputSpec() {
		DataColumnSpec[] newColSpecs = new DataColumnSpec[3];
		int colIdx = 0;
		newColSpecs[colIdx++] =
				new DataColumnSpecCreator("Table Name", StringCell.TYPE)
						.createSpec();
		newColSpecs[colIdx++] =
				new DataColumnSpecCreator("Catalog Name", StringCell.TYPE)
						.createSpec();
		newColSpecs[colIdx++] =
				new DataColumnSpecCreator("Table Comment", StringCell.TYPE)
						.createSpec();
		return new DataTableSpecCreator().addColumns(newColSpecs).createSpec();
	}

}
