/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
 *******************************************************************************/
package com.vernalis.knime.chem.speedysmiles.nodes.manip.dummy;

import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.chem.speedysmiles.nodes.abstrct.AbstractSpeedySmilesNodeDialog;

public class ReplaceDummyAtomNodeDialog extends AbstractSpeedySmilesNodeDialog {

	private static final String DUMMY_ATOM_REPLACEMENT =
			"Dummy Atom Replacement";

	/**
	 *
	 */
	public ReplaceDummyAtomNodeDialog() {
		super(true, true);
		addDialogComponent(new DialogComponentString(createReplacementModel(),
				DUMMY_ATOM_REPLACEMENT, true, 5));
	}

	static SettingsModelString createReplacementModel() {
		return new SettingsModelString(DUMMY_ATOM_REPLACEMENT, "At");
	}

}
