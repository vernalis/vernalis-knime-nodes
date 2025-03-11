/*******************************************************************************
 * Copyright (c) 2025, Vernalis (R&D) Ltd
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
package com.vernalis.knime.misc.blobs.nodes.gzip;

import static com.vernalis.knime.misc.blobs.nodes.BlobConstants.BLOB_COLUMN_FILTER;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
/**
 * Node Dialog for the Un-GZip Binary Object node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class UnGzipBlobNodeDialog extends DefaultNodeSettingsPane {

	private static final String REPLACE_INPUT_COLUMN = "Replace Input Column";
	private static final String BINARY_OBJECT_COLUMN = "Binary Object Column";

	/**
	 * Constructor
	 */
	public UnGzipBlobNodeDialog() {
		addDialogComponent(
				new DialogComponentColumnNameSelection(createBlobColNameModel(),
						BINARY_OBJECT_COLUMN, 0, BLOB_COLUMN_FILTER));
		addDialogComponent(new DialogComponentBoolean(
				createReplaceInputColModel(), REPLACE_INPUT_COLUMN));
	}

	/**
	 * @return Settings Model for the Replace Input Column option
	 */
	static SettingsModelBoolean createReplaceInputColModel() {
		return new SettingsModelBoolean(REPLACE_INPUT_COLUMN, true);
	}

	/**
	 * @return Settings Model for the Binary Object column name
	 */
	static SettingsModelString createBlobColNameModel() {
		return new SettingsModelString(BINARY_OBJECT_COLUMN, null);
	}
}
