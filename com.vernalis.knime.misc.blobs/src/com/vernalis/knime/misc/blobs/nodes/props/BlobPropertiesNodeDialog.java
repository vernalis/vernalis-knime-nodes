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
package com.vernalis.knime.misc.blobs.nodes.props;

import static com.vernalis.knime.misc.blobs.nodes.BlobConstants.BLOB_COLUMN_FILTER;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
/**
 * Node dialog for the Binary Objects Properties node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 *
 * @since 1.38.0
 */
public class BlobPropertiesNodeDialog extends DefaultNodeSettingsPane {

	private static final String BINARY_OBJECTS_COLUMN = "Binary Objects Column";

	/**
	 * Constructor
	 *
	 * @since 1.38.0
	 */
	BlobPropertiesNodeDialog() {
		addDialogComponent(
				new DialogComponentColumnNameSelection(createBlobColNameModel(),
						BINARY_OBJECTS_COLUMN, 0, BLOB_COLUMN_FILTER));
	}

	/**
	 * @return model for the Binary Objects Column setting
	 *
	 * @since 1.38.0
	 */
	static SettingsModelString createBlobColNameModel() {
		return new SettingsModelString(BINARY_OBJECTS_COLUMN, null);
	}

}
