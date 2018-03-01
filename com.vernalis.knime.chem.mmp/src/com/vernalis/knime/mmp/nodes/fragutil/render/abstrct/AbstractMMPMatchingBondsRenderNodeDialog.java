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
package com.vernalis.knime.mmp.nodes.fragutil.render.abstrct;

import org.knime.core.node.defaultnodesettings.DialogComponentColorChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentLabel;
import org.knime.core.node.defaultnodesettings.SettingsModelColor;

import com.vernalis.knime.mmp.MMPConstants;
import com.vernalis.knime.mmp.MatchedPairsMultipleCutsNodePlugin;
import com.vernalis.knime.mmp.fragutils.FragmentationUtilsFactory;
import com.vernalis.knime.mmp.nodes.fragutil.abstrct.AbstractMMPFragmentationFactoryNodeDialog;

/**
 * The node dialog for the Render Matching/Cuttable bonds nodes
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The molecule type parameter
 * @param <U>
 *            The matcher type parameter
 */
public class AbstractMMPMatchingBondsRenderNodeDialog<T, U>
		extends AbstractMMPFragmentationFactoryNodeDialog<T, U> {

	/**
	 * @param fragUtilityFactory
	 *            The {@link FragmentationUtilsFactory} instance for the node
	 * @param labelText
	 *            The label text for the colour chooser
	 * @param hasNumCuts
	 *            Does the dialog have the number of cuts component?
	 * @param hasTwoCutsToBond
	 *            Does the dialog have the two cuts to one bond component?
	 */
	public AbstractMMPMatchingBondsRenderNodeDialog(
			FragmentationUtilsFactory<T, U> fragUtilityFactory, String labelText,
			boolean hasNumCuts, boolean hasTwoCutsToBond) {
		super(fragUtilityFactory, false, false, hasNumCuts, hasTwoCutsToBond);
		addDialogComponent(
				new DialogComponentColorChooser(createBondColourModel(), labelText, true));
		if ("rdkit".equalsIgnoreCase(fragUtilFactory.getToolkitName())
				&& !MatchedPairsMultipleCutsNodePlugin.CAN_RENDER_WITH_NEW_RDKIT_RENDERING) {
			addDialogComponent(new DialogComponentLabel(
					"<html><font color=\"red\">You need to update the RDKit plugin "
							+ "to at least version 3.3.1 in order for the rendering code "
							+ "to work</font></html>"));
		}
	}

	/**
	 * @return The settings model to use for the hilited bonds
	 */
	static SettingsModelColor createBondColourModel() {
		return new SettingsModelColor(MMPConstants.BOND_COLOUR, MMPConstants.DEFAULT_BOND_COLOUR);
	}

}
