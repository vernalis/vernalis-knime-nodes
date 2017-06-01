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
package com.vernalis.knime.chem.speedysmiles.helpers;

import com.vernalis.knime.chem.speedysmiles.nodes.count.abstrct.AbstractSpeedySmilesEnumBasedCountNodeDialog;
import com.vernalis.knime.chem.speedysmiles.nodes.count.abstrct.AbstractSpeedySmilesEnumBasedCountNodeModel;

/**
 * Interface to define calculated properties for SMILES Strings. It is suggested
 * that implementations are as {@link Enum}s, in which case they can be used in
 * subclasses of {@link AbstractSpeedySmilesEnumBasedCountNodeDialog} and
 * {@link AbstractSpeedySmilesEnumBasedCountNodeModel}
 * 
 * @author S.Roughley
 *
 * @param <T>
 *            The number type to be returned by the calculation
 */
public interface CalculatedProperty<T extends Number> {
	/**
	 * Method to calculate the property
	 * 
	 * @param SMILES
	 *            The SMILES String
	 * @return The calculated property
	 */
	T calculate(String SMILES);

	/**
	 * @return A user-friendly display name. If used via
	 *         {@link AbstractSpeedySmilesEnumBasedCountNodeModel}, this will
	 *         form the columne heading, and the dialog options in
	 *         {@link AbstractSpeedySmilesEnumBasedCountNodeDialog}
	 */
	String displayName();
}
