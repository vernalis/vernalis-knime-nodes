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
package com.vernalis.knime.db.nodes.extractbdt;

import org.knime.workflow.migration.NodeMigrationRule;

import com.vernalis.knime.db.SimpleNodeMigrationRule;

/**
 * {@link NodeMigrationRule} to map the Database Query to Empty Table (Legacy)
 * node to the DB Query to Empty Table node
 * 
 * @author S Roughley
 *
 */
public class DBQueryToEmptyTableNodeMigrationRule
		extends SimpleNodeMigrationRule {

	/**
	 * Constructor
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	public DBQueryToEmptyTableNodeMigrationRule() {
		super(DBQueryToEmptyTableNodeFactory.class,
				"com.vernalis.knime.database.nodes.extractbdt.DbQueryToEmptyTableNodeFactory");
	}

}
