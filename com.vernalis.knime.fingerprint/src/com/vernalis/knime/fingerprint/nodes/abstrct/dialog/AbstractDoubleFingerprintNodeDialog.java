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

import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * Abstract <code>NodeDialog</code> for the 2 fingerprint columns Nodes.
 * 
 * @author S. Roughley
 */
public class AbstractDoubleFingerprintNodeDialog extends AbstractSingleFingerprintNodeDialog {

	/**
	 * Constructor
	 * 
	 * @param allowSparseBitVector
	 *            Are sparse bit vectors accepted inputs
	 * @param allowDenseBitVector
	 *            Are dense bit vectors accepted inputs
	 * @param allowSparseByteVector
	 *            Are sparse byte vectors accepted inputs
	 * @param allowDenseByteVector
	 *            Are dense byte vectors accepted inputs
	 */
	public AbstractDoubleFingerprintNodeDialog(boolean allowSparseBitVector,
			boolean allowDenseBitVector, boolean allowSparseByteVector,
			boolean allowDenseByteVector) {
		super(allowSparseBitVector, allowDenseBitVector, allowSparseByteVector,
				allowDenseByteVector);
	}

	/** @return The settings model for the second selected fingerprint column */
	public static SettingsModelString createSecondFPColNameModel() {
		return new SettingsModelString("Second Fingerprint Column", null);
	}

	@Override
	protected void addAdditionalColumnSelectors() {
		super.addAdditionalColumnSelectors();
		addDialogComponent(new DialogComponentColumnNameSelection(createSecondFPColNameModel(),
				"Select the second fingerprint column", 0, true, fpFilter));

	}

}
