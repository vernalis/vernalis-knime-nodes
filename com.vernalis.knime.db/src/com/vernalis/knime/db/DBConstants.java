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
package com.vernalis.knime.db;

/**
 * Various constants used throughout this plugin
 *
 * @author S Roughley
 *
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public final class DBConstants {

	/** Constant for Table Name */
	public static final String TABLE_NAME = "Table Name";
	/** Constant for Schema Name */
	public static final String SCHEMA_NAME = "Schema Name";
	/** Dropdown value to display if there are no schemas available */
	public static final String NO_SCHEMAS = "<--No Schemas-->";
	/** Dropdown value to display while the dialog is being initialised */
	public static final String INITIALIZING_DIALOG = "Initializing dialog...";
	/** Dropdown value to display if no metadata is available */
	public static final String NO_METADATA_AVAILABLE =
			"<No Metadata Available>";
	/** Constant for Table Type */
	public static final String TABLE_TYPE = "Table Type";
	/** Message when no table type has been selected */
	public static final String NO_TABLE_TYPE_SELECTED =
			"No table type selected";
	/** Display name for the type mapping */
	public static final String EXTERNAL_TO_KNIME_MAPPING =
			"External to KNIME mapping";
	/** The width for dropdowns in the nodes settings panes */
	public static final int DROPDOWN_WIDTH = 225;
	/** Display name for input type mappings */
	public static final String INPUT_TYPE_MAPPING = "Input Type Mapping";

	private DBConstants() {
		// Utility Class - Do not Instantiate
		throw new UnsupportedOperationException();
	}

}
