/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.knime.chem.speedysmiles.nodes.filter.abstrct;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.knime.chem.speedysmiles.nodes.abstrct.AbstractSpeedySmilesNodeDialog;

/**
 * This is the base class for SpeedySMILES filter/splitter node dialog panes,
 * providing options to select the SMILES column, and also to determin the
 * behaviour of matching and missing cells
 * 
 * @author s.roughley
 * 
 */
public class AbstractSpeedySmilesFilterNodeDialog extends AbstractSpeedySmilesNodeDialog {
	/**
	 * Constructor for the node dialog pane
	 */
	public AbstractSpeedySmilesFilterNodeDialog() {
		super();
		addDialogComponent(
				new DialogComponentBoolean(createKeepMissingModel(), "Keep missing cells"));
		addDialogComponent(new DialogComponentBoolean(createKeepMatchingModel(), "Keep matches"));
	}

	/**
	 * @return Keep Matches Settings Model
	 */
	public static SettingsModelBoolean createKeepMatchingModel() {
		return new SettingsModelBoolean("Keep matches", true);
	}

	/**
	 * @return Keep Missing Cells Settings Model
	 */
	public static SettingsModelBoolean createKeepMissingModel() {
		return new SettingsModelBoolean("Keep missing cells", true);
	}
}
