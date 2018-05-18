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
package com.vernalis.knime.chem.speedysmiles.nodes.count.abstrct;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.knime.chem.speedysmiles.helpers.CalculatedProperty;
import com.vernalis.knime.chem.speedysmiles.nodes.abstrct.AbstractSpeedySmilesNodeDialog;

/**
 * <code>NodeDialog</code> for the SpeedySMILES count, using an enum
 * implementation of {@link CalculatedProperty}
 * 
 * @author S Roughley
 */
public class AbstractSpeedySmilesEnumBasedCountNodeDialog<T extends Enum<T> & CalculatedProperty<U>, U extends Number>
		extends AbstractSpeedySmilesNodeDialog {

	protected T[] ELEMENTS;

	/**
	 * New pane for configuring the SpeedySMILES Element Count Node protected
	 * 
	 * @param elements
	 *            The properties to calculate
	 */
	public AbstractSpeedySmilesEnumBasedCountNodeDialog(T[] elements) {
		ELEMENTS = elements;
		for (T elem : ELEMENTS) {
			addDialogComponent(new DialogComponentBoolean(
					createElementModel(elem.name()), elem.displayName()));
		}

	}

	/**
	 * 
	 * @param element
	 *            The element name
	 * @return Settings Model for calculating the element count
	 */
	public static SettingsModelBoolean createElementModel(String element) {
		return new SettingsModelBoolean(element, true);
	}

}
