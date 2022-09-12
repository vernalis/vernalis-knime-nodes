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

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;

import com.vernalis.knime.misc.ArrayUtils;

public class ListForeignKeysNodeModel extends ListPrimaryKeysNodeModel {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.database.nodes.rsettobdt.
	 * ListPrimaryKeysNodeModel#getRowCellsFromResultSet(java.sql.ResultSet)
	 */
	@Override
	protected DataCell[] getRowCellsFromResultSet(ResultSet rs)
			throws SQLException {
		DataCell[] retVal =
				ArrayUtils.fill(new DataCell[10], DataType.getMissingCell());
		int colIdx = 0;
		if (rs.getString("FKCOLUMN_NAME") != null) {
			retVal[colIdx] = new StringCell(rs.getString("FKCOLUMN_NAME"));
		}
		colIdx++;
		if (rs.getString("FK_NAME") != null) {
			retVal[colIdx] = new StringCell(rs.getString("FK_NAME"));
		}
		colIdx++;
		if (rs.getString("PKTABLE_SCHEM") != null) {
			retVal[colIdx] = new StringCell(rs.getString("PKTABLE_SCHEM"));
		}
		colIdx++;
		if (rs.getString("PKTABLE_NAME") != null) {
			retVal[colIdx] = new StringCell(rs.getString("PKTABLE_NAME"));
		}
		colIdx++;
		if (rs.getString("PKCOLUMN_NAME") != null) {
			retVal[colIdx] = new StringCell(rs.getString("PKCOLUMN_NAME"));
		}
		colIdx++;
		if (rs.getString("PK_NAME") != null) {
			retVal[colIdx] = new StringCell(rs.getString("PK_NAME"));
		}
		colIdx++;
		// if (rs.getString("FKTABLE_SCHEM") != null) {
		// retVal[colIdx] = new StringCell(rs.getString("FKTABLE_SCHEM"));
		// }
		// colIdx++;
		// if (rs.getString("FKTABLE_NAME") != null) {
		// retVal[colIdx] = new StringCell(rs.getString("FKTABLE_NAME"));
		// }
		// colIdx++;

		retVal[colIdx++] = new IntCell(rs.getShort("KEY_SEQ"));
		retVal[colIdx++] = new IntCell(rs.getShort("UPDATE_RULE"));
		retVal[colIdx++] = new IntCell(rs.getShort("DELETE_RULE"));
		retVal[colIdx++] = new IntCell(rs.getShort("DEFERRABILITY"));
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.database.nodes.rsettobdt.
	 * ListPrimaryKeysNodeModel#getResultSetFromMetaData(java.sql.DatabaseMetaData)
	 */
	@Override
	protected ResultSet getResultSetFromMetaData(DatabaseMetaData dbMetaData)
			throws SQLException {
		return dbMetaData.getImportedKeys(null,
				m_schemaNameMdl.getStringValue(),
				m_tableNameMdl.getStringValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.database.nodes.rsettobdt.
	 * ListPrimaryKeysNodeModel#getOutputSpec()
	 */
	@Override
	protected DataTableSpec getOutputSpec() {
		DataColumnSpec[] newColSpecs = new DataColumnSpec[10];
		int colIdx = 0;
		newColSpecs[colIdx++] =
				new DataColumnSpecCreator("Foreign Key Column Name",
						StringCell.TYPE).createSpec();
		newColSpecs[colIdx++] =
				new DataColumnSpecCreator("Foreign Key Name", StringCell.TYPE)
						.createSpec();
		newColSpecs[colIdx++] =
				new DataColumnSpecCreator("Primary Key Table Schema",
						StringCell.TYPE).createSpec();
		newColSpecs[colIdx++] =
				new DataColumnSpecCreator("Primary Key Table Name",
						StringCell.TYPE).createSpec();
		newColSpecs[colIdx++] =
				new DataColumnSpecCreator("Primary Key Column Name",
						StringCell.TYPE).createSpec();
		newColSpecs[colIdx++] =
				new DataColumnSpecCreator("Primary Key Name", StringCell.TYPE)
						.createSpec();
		// newColSpecs[colIdx++] =
		// new DataColumnSpecCreator("Foreign Key Table Schema",
		// StringCell.TYPE).createSpec();
		// newColSpecs[colIdx++] =
		// new DataColumnSpecCreator("Foreign Key Table Name",
		// StringCell.TYPE).createSpec();
		newColSpecs[colIdx++] =
				new DataColumnSpecCreator("Primary Key Sequence Index",
						IntCell.TYPE).createSpec();
		newColSpecs[colIdx++] =
				new DataColumnSpecCreator("Update Rule", IntCell.TYPE)
						.createSpec();
		newColSpecs[colIdx++] =
				new DataColumnSpecCreator("Delete Rule", IntCell.TYPE)
						.createSpec();
		newColSpecs[colIdx++] =
				new DataColumnSpecCreator("Deferability", IntCell.TYPE)
						.createSpec();

		return new DataTableSpecCreator().addColumns(newColSpecs).createSpec();
	}

}
