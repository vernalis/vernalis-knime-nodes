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
 ******************************************************************************/
package com.vernalis.knime.chem.speedysmiles.nodes.filter.organic;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.knime.chem.speedysmiles.nodes.filter.abstrct.AbstractSpeedySmilesFilterNodeDialog;

public class OrganicFilterSplitterNodeDialog
		extends AbstractSpeedySmilesFilterNodeDialog {

	private static final String KEEP_ANY_ATOMS = "Keep any atoms ('*')";

	public OrganicFilterSplitterNodeDialog() {
		super();
		addDialogComponent(new DialogComponentBoolean(createKeepAnyAtomModel(),
				KEEP_ANY_ATOMS));
	}

	static SettingsModelBoolean createKeepAnyAtomModel() {
		return new SettingsModelBoolean(KEEP_ANY_ATOMS, false);
	}
}
