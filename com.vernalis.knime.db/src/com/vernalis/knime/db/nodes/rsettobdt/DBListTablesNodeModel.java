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
import org.knime.database.port.DBSessionPortObjectSpec;

import com.vernalis.knime.misc.ArrayUtils;

import static com.vernalis.knime.db.DBConstants.NO_SCHEMAS;
import static com.vernalis.knime.db.DBConstants.NO_TABLE_TYPE_SELECTED;
import static com.vernalis.knime.db.nodes.rsettobdt.DBListTablesNodeDialog.createSchemaNameModel;
import static com.vernalis.knime.db.nodes.rsettobdt.DBListTablesNodeDialog.createSchemasList;
import static com.vernalis.knime.db.nodes.rsettobdt.DBListTablesNodeDialog.createTableTypeModel;

/**
 * {@link AbstractDBResultSetToTableNodeModel} implementation for the DB List
 * Tables node node
 * 
 * @author S Roughley
 *
 */
public class DBListTablesNodeModel extends AbstractDBResultSetToTableNodeModel {

	private final SettingsModelString tableTypeMdl =
			registerSettingsModel(createTableTypeModel());
	private final SettingsModelString schemaNameMdl =
			registerSettingsModel(createSchemaNameModel());

	/**
	 * Constructor
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	DBListTablesNodeModel() {
		super();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.db.nodes.rsettobdt.
	 * AbstractDBResultSetToTableNodeModel#getRowCellsFromResultSet(java.sql.
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

		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.db.nodes.rsettobdt.
	 * AbstractDBResultSetToTableNodeModel#getResultSetFromMetaData(java.sql.
	 * DatabaseMetaData)
	 */
	@Override
	protected ResultSet getResultSetFromMetaData(DatabaseMetaData dbMetaData)
			throws SQLException {
		return dbMetaData.getTables(null,
				schemaNameMdl.getStringValue().equals(NO_SCHEMAS) ? null
						: schemaNameMdl.getStringValue(),
				"%", new String[] { tableTypeMdl.getStringValue() });
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
		if (tableTypeMdl.getStringValue() == null
				|| tableTypeMdl.getStringValue().isEmpty()) {
			getLogger().error(NO_TABLE_TYPE_SELECTED);
			throw new InvalidSettingsException(NO_TABLE_TYPE_SELECTED);
		}

		SortedSet<String> dbTableNames =
				createSchemasList(inSpec.getDBSession(),
						tableTypeMdl.getStringValue(), getLogger());
		if (!dbTableNames.contains(schemaNameMdl.getStringValue())) {
			getLogger().error("Database connection does not contain "
					+ tableTypeMdl.getStringValue() + " schema '"
					+ schemaNameMdl.getStringValue() + "'");
			throw new InvalidSettingsException(
					"Database connection does not contain "
							+ tableTypeMdl.getStringValue() + " '"
							+ schemaNameMdl.getStringValue() + "'");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.db.nodes.rsettobdt.
	 * AbstractDBResultSetToTableNodeModel#getOutputSpec()
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
		newColSpecs[colIdx] =
				new DataColumnSpecCreator("Table Comment", StringCell.TYPE)
						.createSpec();
		return new DataTableSpecCreator().addColumns(newColSpecs).createSpec();
	}

}
