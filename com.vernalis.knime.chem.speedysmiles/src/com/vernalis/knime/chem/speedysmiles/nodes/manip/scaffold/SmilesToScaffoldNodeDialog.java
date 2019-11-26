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
package com.vernalis.knime.chem.speedysmiles.nodes.manip.scaffold;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.knime.chem.speedysmiles.nodes.abstrct.AbstractSpeedySmilesNodeDialog;

public class SmilesToScaffoldNodeDialog extends AbstractSpeedySmilesNodeDialog {

	private static final String KEEP_CHIRALITY = "Keep Chirality";
	private static final String KEEP_AROMATIC_ATOMS_AROMATIC =
			"Keep Aromatic atoms aromatic";
	private static final String KEEP_BOND_ORDERS = "Keep Bond Orders";
	private static final String KEEP_EXPLICIT_H_S = "Keep Explicit H's";
	private static final String USE_ANY_ATOM_OR_A_A_SYMBOLS =
			"Use 'Any Atom' (* or A/a) symbols";

	/**
	 * 
	 */
	public SmilesToScaffoldNodeDialog() {
		super(true, true);
		addDialogComponent(new DialogComponentBoolean(createAnyAtomModel(),
				USE_ANY_ATOM_OR_A_A_SYMBOLS));
		addDialogComponent(new DialogComponentBoolean(createExplHsModel(),
				KEEP_EXPLICIT_H_S));
		addDialogComponent(new DialogComponentBoolean(createBondOrdersModel(),
				KEEP_BOND_ORDERS));
		addDialogComponent(new DialogComponentBoolean(createAromaticModel(),
				KEEP_AROMATIC_ATOMS_AROMATIC));
		addDialogComponent(new DialogComponentBoolean(createChiralityModel(),
				KEEP_CHIRALITY));
	}

	static SettingsModelBoolean createChiralityModel() {
		return new SettingsModelBoolean(KEEP_CHIRALITY, false);
	}

	static SettingsModelBoolean createAromaticModel() {
		return new SettingsModelBoolean(KEEP_AROMATIC_ATOMS_AROMATIC, true);
	}

	static SettingsModelBoolean createBondOrdersModel() {
		return new SettingsModelBoolean(KEEP_BOND_ORDERS, true);
	}

	static SettingsModelBoolean createExplHsModel() {
		return new SettingsModelBoolean(KEEP_EXPLICIT_H_S, true);
	}

	static SettingsModelBoolean createAnyAtomModel() {
		return new SettingsModelBoolean(USE_ANY_ATOM_OR_A_A_SYMBOLS, false);
	}

}
