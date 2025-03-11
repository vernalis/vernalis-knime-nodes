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
package com.vernalis.knime.misc.blobs.nodes.base64;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.DataValueColumnFilter;

/**
 * Node Dialog for the Binary Object to Base-64 Encoded String node
 * 
 * @author S.Roughley knime@vernalis.com
 * 
 * @since 1.38.0
 *
 */
public class Base642BlobNodeDialog extends DefaultNodeSettingsPane {

	private static final String ERROR_BEHAVIOUR = "Error behaviour";
	private static final String REPLACE_INPUT_COLUMN = "Replace Input Column";
	private static final String BASE64_STRING_COLUMN = "Base64 String Column";
	/**
	 * {@link ColumnFilter} for String columns
	 */
	static final ColumnFilter STRING_COLUMN_FILTER =
			new DataValueColumnFilter(StringValue.class);

	/**
	 * Constructor
	 */
	public Base642BlobNodeDialog() {
		addDialogComponent(
				new DialogComponentColumnNameSelection(createStringColNameModel(),
						BASE64_STRING_COLUMN, 0, STRING_COLUMN_FILTER));
		addDialogComponent(new DialogComponentBoolean(
				createReplaceInputColModel(), REPLACE_INPUT_COLUMN));
		addDialogComponent(new DialogComponentButtonGroup(
				createErrorBehaviourModel(), ERROR_BEHAVIOUR, false,
				ConversionFailureBehaviour.values()));
	}

	/**
	 * @return model for the Error behaviour setting
	 *
	 * @since 1.38.0
	 */
	static final SettingsModelString createErrorBehaviourModel() {
		return new SettingsModelString(ERROR_BEHAVIOUR,
				ConversionFailureBehaviour.getDefault().getActionCommand());
	}

	/**
	 * @return Settings Model for the Replace Input Column option
	 * 
	 * @since 1.38.0
	 */
	static SettingsModelBoolean createReplaceInputColModel() {
		return new SettingsModelBoolean(REPLACE_INPUT_COLUMN, true);
	}

	/**
	 * @return Settings Model for the Base64 String column name
	 * 
	 * @since 1.38.0
	 */
	static SettingsModelString createStringColNameModel() {
		return new SettingsModelString(BASE64_STRING_COLUMN, null);
	}
}
