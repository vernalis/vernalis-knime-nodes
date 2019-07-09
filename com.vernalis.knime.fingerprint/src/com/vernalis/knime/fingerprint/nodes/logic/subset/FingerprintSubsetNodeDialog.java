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
package com.vernalis.knime.fingerprint.nodes.logic.subset;

import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

import com.vernalis.knime.fingerprint.nodes.abstrct.dialog.AbstractSingleFingerprintNodeDialog;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 *
 */
public class FingerprintSubsetNodeDialog extends AbstractSingleFingerprintNodeDialog {

	public FingerprintSubsetNodeDialog() {
		super();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.fingerprint.all.nodes.abstrct.
	 * AbstractSingleFingerprintNodeDialog#addAdditionalColumnSelectors()
	 */
	@Override
	protected void addAdditionalColumnSelectors() {
		super.addAdditionalColumnSelectors();
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentNumberEdit(createStartModel(), "From:", 5));
		addDialogComponent(new DialogComponentNumberEdit(createEndModel(), "To:", 5));
		setHorizontalPlacement(false);
	}

	static SettingsModelIntegerBounded createStartModel() {
		return new SettingsModelIntegerBounded("Start Index", 0, 0, Integer.MAX_VALUE);
	}

	static SettingsModelIntegerBounded createEndModel() {
		return new SettingsModelIntegerBounded("End Index", -1, -1, Integer.MAX_VALUE);
	}
}
