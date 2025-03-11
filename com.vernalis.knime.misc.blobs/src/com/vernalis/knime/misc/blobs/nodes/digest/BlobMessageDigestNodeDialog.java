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
package com.vernalis.knime.misc.blobs.nodes.digest;

import static com.vernalis.knime.misc.blobs.nodes.BlobConstants.BLOB_COLUMN_FILTER;

import java.security.Security;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * Node Dialog pane for the Binary Objects Message Digest (Checksum) node
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.38.0
 */
public class BlobMessageDigestNodeDialog extends DefaultNodeSettingsPane {

	private static final String CONVERT_DIGEST_TO_UPPERCASE =
			"Convert digest to uppercase";
	private static final String ALGORITHM = "Algorithm";
	private static final String BINARY_OBJECTS_COLUMN = "Binary Objects column";

	/** The available digest algorithm names */
	static final NavigableSet<String> DIGEST_ALGORITHMS =
			new TreeSet<>(Security.getAlgorithms("MessageDigest"));

	/**
	 * Constructor
	 *
	 * @since 1.38.0
	 */
	public BlobMessageDigestNodeDialog() {

		addDialogComponent(
				new DialogComponentColumnNameSelection(createColNameModel(),
						BINARY_OBJECTS_COLUMN, 0, BLOB_COLUMN_FILTER));
		final SettingsModelString algoMdl = createAlgorithmNameModel();
		FlowVariableModel fvm = createFlowVariableModel(algoMdl);
		addDialogComponent(new DialogComponentStringSelection(algoMdl,
				ALGORITHM, DIGEST_ALGORITHMS, false, fvm));
		addDialogComponent(new DialogComponentBoolean(
				createConvertToUpperCaseModel(), CONVERT_DIGEST_TO_UPPERCASE));
	}

	/**
	 * @return model for the Convert to uppercase setting
	 *
	 * @since 1.38.0
	 */
	static SettingsModelBoolean createConvertToUpperCaseModel() {
		return new SettingsModelBoolean(CONVERT_DIGEST_TO_UPPERCASE, false);
	}

	/**
	 * @return model for the Algorithm setting
	 *
	 * @since 1.38.0
	 */
	static SettingsModelString createAlgorithmNameModel() {
		return new SettingsModelString(ALGORITHM,
				DIGEST_ALGORITHMS.contains("MD5") ? "MD5"
						: DIGEST_ALGORITHMS.first());
	}

	/**
	 * @return model for the Binary Objects column name setting
	 *
	 * @since 1.38.0
	 */
	static SettingsModelString createColNameModel() {
		return new SettingsModelString(BINARY_OBJECTS_COLUMN, null);
	}

}
