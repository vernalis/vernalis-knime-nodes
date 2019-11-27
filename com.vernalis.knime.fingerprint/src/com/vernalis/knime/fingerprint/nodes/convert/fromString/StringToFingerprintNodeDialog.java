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
package com.vernalis.knime.fingerprint.nodes.convert.fromString;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.knime.fingerprint.nodes.abstrct.dialog.AbstractStringToFingerprintNodeDialog;

public class StringToFingerprintNodeDialog extends AbstractStringToFingerprintNodeDialog {
	public StringToFingerprintNodeDialog() {
		super();
		addDialogComponent(new DialogComponentBoolean(createIsByteModel(),
				"Return byte vector (count) fingerprint"));
	}

	static SettingsModelBoolean createIsByteModel() {
		return new SettingsModelBoolean("Is count fingerprint", false);
	}
}
