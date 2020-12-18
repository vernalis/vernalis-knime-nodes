/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.query.chemical;

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * Enum detailing the possible Chemical Descriptor types. Slightly unusually
 * 'Descriptor' in the RCSB context refers to a structural representation
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public enum ChemicalDescriptorType implements ButtonGroupEnumInterface {

	/**
	 * A SMILES String representation
	 */
	SMILES,

	/**
	 * The InChI String representation
	 */
	InChI;

	@Override
	public String getText() {
		return name();
	}

	@Override
	public String getActionCommand() {
		return getText();
	}

	@Override
	public String getToolTip() {
		return null;
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	/**
	 * @return The default descriptor type
	 */
	public static ChemicalDescriptorType getDefault() {
		return SMILES;
	}

}
