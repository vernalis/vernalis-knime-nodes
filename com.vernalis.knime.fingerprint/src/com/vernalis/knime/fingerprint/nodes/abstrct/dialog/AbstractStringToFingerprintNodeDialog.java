/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.fingerprint.nodes.abstrct.dialog;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.fingerprint.abstrct.FingerPrintTypes;

/**
 * Abstract <code>NodeDialog</code> for string to fingerprint column nodes
 * 
 * 
 * @author S. Roughley
 */
public class AbstractStringToFingerprintNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * abstract node dialog providing a single fingerprint column
	 */
	public AbstractStringToFingerprintNodeDialog() {
		this(true);
	}

	@SuppressWarnings("unchecked")
	public AbstractStringToFingerprintNodeDialog(boolean showType) {
		addDialogComponent(new DialogComponentColumnNameSelection(createStringColNameModel(),
				"Select the string column", 0, true, StringValue.class));

		addDialogComponent(
				new DialogComponentBoolean(createKeepInputColumnsModel(), "Keep input column"));

		if (showType) {
			addDialogComponent(new DialogComponentButtonGroup(createFPTypeModel(),
					"Fingerprint Type", false, FingerPrintTypes.values()));
		}

	}

	/**
	 * @return The settings model for the fingerprint type
	 */
	public static SettingsModelString createFPTypeModel() {
		return new SettingsModelString("Fingerprint type",
				FingerPrintTypes.getDefaultMethod().getActionCommand());
	}

	/** @return The settings model for the first selected fingerprint column */
	public static SettingsModelString createStringColNameModel() {
		return new SettingsModelString("String Column", null);
	}

	/** Static method to create keep input columns settings model */
	public static SettingsModelBoolean createKeepInputColumnsModel() {
		return new SettingsModelBoolean("Keep input cols", true);
	}
}
