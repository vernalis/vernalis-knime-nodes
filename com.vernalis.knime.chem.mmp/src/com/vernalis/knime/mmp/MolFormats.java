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
package com.vernalis.knime.mmp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.rdkit.knime.types.RDKitMolCellFactory;

/**
 * Abstract container class for the list of acceptable input formats for MMP
 * nodes
 * 
 * @author Stephen Roughley<s.roughley@vernalis.com>
 * 
 */
public abstract class MolFormats {
	/** The acceptable molecule input formats */
	@SuppressWarnings("unchecked")
	public static final ArrayList<Class<? extends DataValue>> m_molFormats = new ArrayList<Class<? extends DataValue>>(
			(Collection<? extends Class<? extends DataValue>>) Arrays
					.asList(new Class[] { SmilesValue.class, SdfValue.class,
							MolValue.class }));

	@SuppressWarnings("unchecked")
	public static final ArrayList<Class<? extends DataValue>> m_RDKitmolFormats = new ArrayList<Class<? extends DataValue>>(
			(Collection<? extends Class<? extends DataValue>>) Arrays
					.asList(new Class[] { SmilesValue.class, SdfValue.class,
							MolValue.class, RDKitMolCellFactory.class }));

	/**
	 * Check whether the column type is compatible with any of the acceptable
	 * Indigo formats
	 * 
	 * @param colType
	 *            The column type of the column to test
	 * @deprecated
	 * @see #isColTypeRDKitCompatible(DataType)
	 */
	@Deprecated
	public static boolean isColTypeIndigoCompatible(DataType colType) {
		boolean isCompatible = false;
		for (Class<? extends DataValue> valClass : m_molFormats) {
			if (colType.isCompatible(valClass)) {
				isCompatible = true;
				break;
			}
		}
		return isCompatible;
	}

	/**
	 * Check whether the column type is compatable with any of the acceptable
	 * RDKit formats
	 * 
	 * @param colType
	 *            The column type of the column to test
	 */
	public static boolean isColTypeRDKitCompatible(DataType colType) {
		boolean isCompatible = false;
		for (Class<? extends DataValue> valClass : m_RDKitmolFormats) {
			if (colType.isCompatible(valClass)) {
				isCompatible = true;
				break;
			}
		}
		return isCompatible;
	}
}
