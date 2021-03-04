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
package com.vernalis.knime.io.nodes.knimeresolver;

import org.knime.core.data.StringValue;
import org.knime.core.data.uri.URIDataValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentFlowVariableNameSelection2;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.DataValueColumnFilter;
import org.knime.core.node.workflow.VariableType.StringType;

/**
 * Node Dialog for the KNIME URI Resolver nodes
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.30.0
 *
 */
public class KNIMEResolverNodeDialog extends DefaultNodeSettingsPane {

	private static final String CANONICALISE_OUTPUT = "Canonicalise Output";
	private static final String RETURN_FILE_URI = "Return file:/ URI";
	private static final String VARIABLE_NAME = "Variable Name";
	private static final String KNIME_URI_COLUMN_NAME = "KNIME URI Column Name";
	private static final String REPLACE_INPUT = "Replace Input";
	/**
	 * Column filter for String and URI cell types
	 */
	@SuppressWarnings("unchecked")
	static final ColumnFilter URI_COLUMN_FILTER =
			new DataValueColumnFilter(StringValue.class, URIDataValue.class);

	/**
	 * Constructor
	 * 
	 * @param flowVar
	 *            If {@code true} the node is a flow variable node rather than a
	 *            Table manipulator
	 */
	public KNIMEResolverNodeDialog(boolean flowVar) {
		if (flowVar) {
			addDialogComponent(new DialogComponentFlowVariableNameSelection2(
					createVarNameModel(), VARIABLE_NAME,
					() -> getAvailableFlowVariables(StringType.INSTANCE)));
			addDialogComponent(new DialogComponentBoolean(
					createReturnURIModel(), RETURN_FILE_URI));
		} else {
			addDialogComponent(
					new DialogComponentColumnNameSelection(createColNameModel(),
							KNIME_URI_COLUMN_NAME, 0, URI_COLUMN_FILTER));
		}
		addDialogComponent(new DialogComponentBoolean(
				createCanonicalizeOutputModel(), CANONICALISE_OUTPUT));
		addDialogComponent(new DialogComponentBoolean(createReplaceInputModel(),
				REPLACE_INPUT));
	}

	/**
	 * @return The model for the canonicalise output setting
	 */
	static SettingsModelBoolean createCanonicalizeOutputModel() {
		return new SettingsModelBoolean(CANONICALISE_OUTPUT, false);
	}

	/**
	 * @return The model for the return file:/ URI setting
	 */
	static SettingsModelBoolean createReturnURIModel() {
		return new SettingsModelBoolean(RETURN_FILE_URI, false);
	}

	/**
	 * @return The model for the variable name selector
	 */
	static SettingsModelString createVarNameModel() {
		return new SettingsModelString(VARIABLE_NAME, null);
	}

	/**
	 * @return The model for the URI column
	 */
	static SettingsModelString createColNameModel() {
		return new SettingsModelString(KNIME_URI_COLUMN_NAME, null);
	}

	/**
	 * @return The model for the replace input setting
	 */
	static SettingsModelBoolean createReplaceInputModel() {
		return new SettingsModelBoolean(REPLACE_INPUT, true);
	}

}
