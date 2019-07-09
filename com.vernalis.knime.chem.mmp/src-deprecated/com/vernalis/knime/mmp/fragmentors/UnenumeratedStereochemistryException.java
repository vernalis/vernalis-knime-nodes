/*******************************************************************************
 * Copyright (c) 2015, Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.fragmentors;

import com.vernalis.knime.mmp.MulticomponentSmilesFragmentParser;

/**
 * A simple exception class which should be thrown by the
 * {@link MulticomponentSmilesFragmentParser} if the smiles string contains a
 * dative bond character ('>' or '<')
 * 
 * @author s.roughley
 * @deprecated
 */
@Deprecated
public class UnenumeratedStereochemistryException extends Exception {
	private static final long serialVersionUID = 2406414402983464598L;
	private final String smiles;
	private final boolean removeHs;

	/**
	 * Constructer
	 * 
	 * @param message
	 *            The message
	 * @param SMILES
	 *            The SMILES String
	 * @param removeHs
	 *            Should Hs be removed
	 */
	public UnenumeratedStereochemistryException(String message, String SMILES, boolean removeHs) {
		super(message);
		this.smiles = SMILES;
		this.removeHs = removeHs;
	}

	public String getSmiles() {
		return smiles;
	}

	public boolean getRemoveHs() {
		return removeHs;
	}

}
