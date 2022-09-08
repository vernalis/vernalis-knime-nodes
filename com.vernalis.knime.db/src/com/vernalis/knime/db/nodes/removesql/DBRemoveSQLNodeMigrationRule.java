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
package com.vernalis.knime.db.nodes.removesql;

import com.vernalis.knime.db.SimpleNodeMigrationRule;

/**
 * {@link SimpleNodeMigrationRule} implmentation for the migration of Database
 * Remove SQL to DB Remove SQL nodes
 * 
 * @author S Roughley
 *
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public class DBRemoveSQLNodeMigrationRule extends SimpleNodeMigrationRule {

	/**
	 * Constructor
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	public DBRemoveSQLNodeMigrationRule() {
		super(DBRemoveSQLNodeFactory.class,
				"com.vernalis.knime.database.nodes.removesql.RemoveSQLNodeFactory");
	}

}
