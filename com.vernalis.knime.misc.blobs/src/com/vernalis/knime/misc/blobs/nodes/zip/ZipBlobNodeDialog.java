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
package com.vernalis.knime.misc.blobs.nodes.zip;

import static com.vernalis.knime.misc.blobs.nodes.BlobConstants.BLOB_COLUMN_FILTER;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.DataValueColumnFilter;

import com.vernalis.knime.dialog.components.DialogComponentMultilineStringFlowvar;
import com.vernalis.knime.dialog.components.SettingsModelMultilineString;

/**
 * NodeDialog for the Zip Blobs node
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class ZipBlobNodeDialog extends DefaultNodeSettingsPane {

	private static final String ZIP_COMMENT = "ZIP Comment";
	private static final String COMPRESSION_LEVEL = "Compression level";
	private static final String KEEP_DIRECTORIES =
			"Keep Empty Blobs as Directories";
	private static final String BINARY_OBJECT_COLUMN = "Binary Object Column";

    /**
     * {@link ColumnFilter} for String columns
     */
	static final ColumnFilter STRING_COLUMN_FILTER =
			new DataValueColumnFilter(StringValue.class);
	private static final String ZIP_PATH_COLUMN = "Zip Path Column";

	/**
	 * Constructor
	 */
	public ZipBlobNodeDialog() {
		addDialogComponent(
				new DialogComponentColumnNameSelection(createBlobColNameModel(),
						BINARY_OBJECT_COLUMN, 0, BLOB_COLUMN_FILTER));
		addDialogComponent(new DialogComponentColumnNameSelection(
				createZipPathColumnModel(), ZIP_PATH_COLUMN, 0,
				STRING_COLUMN_FILTER));

		final SettingsModelMultilineString zipCommentMdl =
				createZipCommentModel();
		addDialogComponent(new DialogComponentMultilineStringFlowvar(
				zipCommentMdl, ZIP_COMMENT, false, 60, 5,
				createFlowVariableModel(zipCommentMdl)));

		final SettingsModelIntegerBounded compressionLevelMdl =
				createCompressionLevelModel();
		addDialogComponent(new DialogComponentNumber(compressionLevelMdl,
				COMPRESSION_LEVEL, 1,
				createFlowVariableModel(compressionLevelMdl)));
		addDialogComponent(new DialogComponentBoolean(
				createKeepDirectoriesModel(), KEEP_DIRECTORIES));

	}

	/**
	 * @return model for the ZIP Comment setting
	 */
	static SettingsModelMultilineString createZipCommentModel() {
		return new SettingsModelMultilineString(ZIP_COMMENT, "");
	}

	/**
	 * @return model for the compression level setting
	 */
	static SettingsModelIntegerBounded createCompressionLevelModel() {
		return new SettingsModelIntegerBounded(COMPRESSION_LEVEL, 6, 0, 9);
	}

	/**
	 * @return Settings model for the Zip path column
	 */
	static SettingsModelColumnName createZipPathColumnModel() {
		final SettingsModelColumnName retVal =
				new SettingsModelColumnName(ZIP_PATH_COLUMN, null);
		retVal.setSelection(null, true);
		return retVal;
	}

	/**
	 * @return Settings model for the keep directories option
	 */
	static SettingsModelBoolean createKeepDirectoriesModel() {
		return new SettingsModelBoolean(KEEP_DIRECTORIES, false);
	}

	/**
	 * @return Settings model for the blob column
	 */
	static SettingsModelString createBlobColNameModel() {
		return new SettingsModelString(BINARY_OBJECT_COLUMN, null);
	}
}
