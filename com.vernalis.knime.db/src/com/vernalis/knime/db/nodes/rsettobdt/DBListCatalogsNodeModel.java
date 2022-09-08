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

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.def.StringCell;

/**
 * {@link AbstractDBResultSetToTableNodeModel} implementation for the DB List
 * Catalogs node
 * 
 * @author S Roughley
 *
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public class DBListCatalogsNodeModel
		extends AbstractDBResultSetToTableNodeModel {

	/**
	 * Constructor
	 */
	DBListCatalogsNodeModel() {

	}

	@Override
	protected DataCell[] getRowCellsFromResultSet(ResultSet rs)
			throws SQLException {

		return new DataCell[] { new StringCell(rs.getString("TABLE_CAT")) };
	}

	@Override
	protected ResultSet getResultSetFromMetaData(DatabaseMetaData dbMetaData)
			throws SQLException {
		return dbMetaData.getCatalogs();
	}

	@Override
	protected DataTableSpec getOutputSpec() {
		return new DataTableSpecCreator().addColumns(
				new DataColumnSpecCreator("Catalogue Name", StringCell.TYPE)
						.createSpec())
				.createSpec();
	}

}
