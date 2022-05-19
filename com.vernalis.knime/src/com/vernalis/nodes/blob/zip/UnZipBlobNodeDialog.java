/*******************************************************************************
 * Copyright (c) 2021, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.blob.zip;

import org.knime.core.data.blob.BinaryObjectDataValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.DataValueColumnFilter;

/**
 * Node Dialog for the UnZip blob node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class UnZipBlobNodeDialog extends DefaultNodeSettingsPane {

	private static final String REMOVE_INPUT_COLUMN = "Remove Input Column";
	private static final String KEEP_DIRECTORIES = "Keep Directories";
	private static final String BINARY_OBJECT_COLUMN = "Binary Object Column";
	/**
	 * Column Filter for Binary Object columns
	 */
	@SuppressWarnings("unchecked")
	static final ColumnFilter BLOB_COLUMN_FILTER =
			new DataValueColumnFilter(BinaryObjectDataValue.class);

	/**
	 * Constructor
	 */
	public UnZipBlobNodeDialog() {
		addDialogComponent(
				new DialogComponentColumnNameSelection(createBlobColNameModel(),
						BINARY_OBJECT_COLUMN, 0, BLOB_COLUMN_FILTER));
		addDialogComponent(new DialogComponentBoolean(
				createKeepDirectoriesModel(), KEEP_DIRECTORIES));
		addDialogComponent(new DialogComponentBoolean(
				createRemoveInputColumnModel(), REMOVE_INPUT_COLUMN));
	}

	/**
	 * @return Settings Model for the Remove input column option
	 */
	static SettingsModelBoolean createRemoveInputColumnModel() {
		return new SettingsModelBoolean(REMOVE_INPUT_COLUMN, true);
	}

	/**
	 * @return Settings Model for the Keep Directories option
	 */
	static SettingsModelBoolean createKeepDirectoriesModel() {
		return new SettingsModelBoolean(KEEP_DIRECTORIES, false);
	}

	/**
	 * @return Settings Model for the blob column name
	 */
	static SettingsModelString createBlobColNameModel() {
		return new SettingsModelString(BINARY_OBJECT_COLUMN, null);
	}
}
