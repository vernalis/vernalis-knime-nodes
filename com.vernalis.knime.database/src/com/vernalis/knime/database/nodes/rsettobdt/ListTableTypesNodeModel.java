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
import org.knime.core.data.def.StringCell;

import com.vernalis.knime.misc.ArrayUtils;

public class ListTableTypesNodeModel extends AbstractResultSetToTableNodeModel {

	/**
	 * 
	 */
	protected ListTableTypesNodeModel() {

	}

	@Override
	protected DataCell[] getRowCellsFromResultSet(ResultSet rs)
			throws SQLException {
		DataCell[] retVal =
				ArrayUtils.fill(new DataCell[1], DataType.getMissingCell());
		int colIdx = 0;
		retVal[colIdx++] = new StringCell(rs.getString("TABLE_TYPE"));
		return retVal;
	}

	@Override
	protected ResultSet getResultSetFromMetaData(DatabaseMetaData dbMetaData)
			throws SQLException {
		return dbMetaData.getTableTypes();
	}

	@Override
	protected DataTableSpec getOutputSpec() {
		DataColumnSpec[] newColSpecs = new DataColumnSpec[1];
		int colIdx = 0;
		newColSpecs[colIdx++] =
				new DataColumnSpecCreator("Table Type", StringCell.TYPE)
						.createSpec();
		return new DataTableSpecCreator().addColumns(newColSpecs).createSpec();
	}

}
