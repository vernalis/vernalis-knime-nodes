/*******************************************************************************
 * Copyright (c) 2014, 2015, Vernalis (R&D) Ltd
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
 *******************************************************************************/
package com.vernalis.knime.chem.pmi.nodes.pmi2;

import static com.vernalis.knime.chem.pmi.util.misc.PmiUtils.createMolColNameModel;
import static com.vernalis.knime.chem.pmi.util.misc.PmiUtils.createNormalisedPmisModel;
import static com.vernalis.knime.chem.pmi.util.misc.PmiUtils.createPmisModel;

import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;

/**
 * <code>NodeDialog</code> for the "Pmi" Node. Node to calculate Principle
 * Moments of Inertia (PMI) and derived properties
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Vernalis
 */
public class Pmi2NodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring the Pmi node.
	 */
	@SuppressWarnings("unchecked")
	protected Pmi2NodeDialog() {

		addDialogComponent(new DialogComponentColumnNameSelection(createMolColNameModel(),
				"Select the SDF or MOL column containing the conformers", 0, true, SdfValue.class,
				MolValue.class));

		addDialogComponent(new DialogComponentBoolean(createPmisModel(),
				"Principle Moments of Inertia (PMI; I1, I2, I3)"));

		addDialogComponent(new DialogComponentBoolean(createNormalisedPmisModel(),
				"Normalised PMIs (I1 / I3, I2 / I3)"));

	}

}
