/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
package com.vernalis.knime.chem.speedysmiles.nodes.manip.hacdesalt;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.knime.chem.speedysmiles.nodes.abstrct.AbstractSpeedySmilesNodeDialog;

/**
 * <code>NodeDialog</code> for the "SmilesHACLargestComponentDesalt" Node. Node
 * to find largest component of SMILES by Heavy Atom Count
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author S Roughley
 */
public class SmilesHACLargestComponentDesaltNodeDialog
		extends AbstractSpeedySmilesNodeDialog {

	/**
	 * New pane for configuring the SmilesHACLargestComponentDesalt node.
	 */
	protected SmilesHACLargestComponentDesaltNodeDialog() {
		super(true, true);
		createNewGroup("Tie-break behaviour");
		final SettingsModelBoolean keepFirstOnlyModel =
				createKeepFirstOnlyModel();
		final SettingsModelBoolean keepLongestSmilesStringModel =
				createKeepLongestModel();
		keepLongestSmilesStringModel.setEnabled(keepFirstOnlyModel.isEnabled());
		keepFirstOnlyModel.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				keepLongestSmilesStringModel
						.setEnabled(keepFirstOnlyModel.getBooleanValue());

			}
		});

		addDialogComponent(new DialogComponentBoolean(keepFirstOnlyModel,
				"Keep only 1st unique component"));
		addDialogComponent(
				new DialogComponentBoolean(keepLongestSmilesStringModel,
						"Keep the longest SMILES String"));

	}

	/**
	 * @return Settings Model for keeping the longest SMILES String
	 */
	public static SettingsModelBoolean createKeepLongestModel() {
		return new SettingsModelBoolean("Keep longest SMILES String", true);
	}

	/**
	 * @return Settings Model for only keeping the 1st component when more than
	 *         1 matches
	 */
	public static SettingsModelBoolean createKeepFirstOnlyModel() {
		return new SettingsModelBoolean("Keep only 1st component", false);
	}

}
