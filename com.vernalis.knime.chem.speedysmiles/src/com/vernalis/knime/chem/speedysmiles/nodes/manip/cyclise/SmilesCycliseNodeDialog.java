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
package com.vernalis.knime.chem.speedysmiles.nodes.manip.cyclise;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.knime.chem.speedysmiles.nodes.abstrct.AbstractSpeedySmilesNodeDialog;

public class SmilesCycliseNodeDialog extends AbstractSpeedySmilesNodeDialog {

	private static final String REMOVE_FIRST_ATOM = "Remove first atom";
	private static final String REMOVE_LAST_ATOM = "Remove last atom";

	/**
	 * 
	 */
	public SmilesCycliseNodeDialog() {
		super(true, true);
		addDialogComponent(new DialogComponentBoolean(
				createRemoveFirstAtomModel(), REMOVE_FIRST_ATOM));
		addDialogComponent(new DialogComponentBoolean(
				createRemoveLastAtomModel(), REMOVE_LAST_ATOM));
		;
	}

	static SettingsModelBoolean createRemoveFirstAtomModel() {
		return new SettingsModelBoolean(REMOVE_FIRST_ATOM, false);
	}

	static SettingsModelBoolean createRemoveLastAtomModel() {
		return new SettingsModelBoolean(REMOVE_LAST_ATOM, true);
	}

}
