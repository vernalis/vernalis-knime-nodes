/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
/**
 * 
 */
package com.vernalis.knime.chem.rdkit.nodes.scaffoldkeys;

import java.awt.Color;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColorChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColor;

import com.vernalis.knime.chem.rdkit.nodes.abstrct.AbstractVerRDKitNodeDialog;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.rdkit.ROMolRevisedScaffoldKeysFactory;

/**
 * 
 * Node dialog pane for the Scaffold Keys node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 *
 * @since v1.34.0
 */
public class ScaffoldKeysNodeDialog extends AbstractVerRDKitNodeDialog {

	private static final String HIGHLIGHT_COLOUR = "Highlight Colour";
	private static final String DEPICT_KEYS = "Depict Keys";
	private static final String INPUTS_ARE_MURCKO_SCAFFOLDS =
			"Inputs are Murcko Scaffolds";

	/**
	 * Constructor
	 *
	 * @since v1.34.0
	 */
	ScaffoldKeysNodeDialog() {
		super();
		addDialogComponent(new DialogComponentBoolean(
				createIsMurckoScaffoldModel(), INPUTS_ARE_MURCKO_SCAFFOLDS));
		if (ROMolRevisedScaffoldKeysFactory.getInstance().canDepict()) {
			createNewGroup("Depiction Options");
			setHorizontalPlacement(true);
			final SettingsModelBoolean depictMdl = createDepictModel();
			final SettingsModelColor highlightColourMdl =
					createHighlightColourModel();
			depictMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					highlightColourMdl.setEnabled(depictMdl.getBooleanValue());

				}
			});
			highlightColourMdl.setEnabled(depictMdl.getBooleanValue());
			addDialogComponent(
					new DialogComponentBoolean(depictMdl, DEPICT_KEYS));
			addDialogComponent(new DialogComponentColorChooser(
					highlightColourMdl, HIGHLIGHT_COLOUR, true, "Choose..."));
			setHorizontalPlacement(false);
			closeCurrentGroup();
		}
	}

	/**
	 * @return model for the Highlight Colour setting
	 *
	 * @since v1.34.0
	 */
	static final SettingsModelColor createHighlightColourModel() {
		return new SettingsModelColor(HIGHLIGHT_COLOUR, Color.RED);
	}

	/**
	 * @return model for the Depict Keys setting
	 *
	 * @since v1.34.0
	 */
	static final SettingsModelBoolean createDepictModel() {
		return new SettingsModelBoolean(DEPICT_KEYS, true);
	}

	/**
	 * @return model for the Inputs are Murcko Scaffolds setting
	 *
	 * @since v1.34.0
	 */
	static final SettingsModelBoolean createIsMurckoScaffoldModel() {
		return new SettingsModelBoolean(INPUTS_ARE_MURCKO_SCAFFOLDS, false);
	}

}
