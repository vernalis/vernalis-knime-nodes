/*******************************************************************************
 * Copyright (c) 2017,2021 Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.frags.rdkit;

import org.RDKit.RWMol;

import com.vernalis.knime.mmp.ToolkitException;
import com.vernalis.knime.mmp.frags.abstrct.AbstractFragmentKey;

/**
 * A simple class to hold the fragment key as an ArrayList, which is then sorted
 * and concatenated to a multicomponent SMILES
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class RWMolFragmentKey extends AbstractFragmentKey<RWMol> {

	/**
	 * Constructor - initialises an empty key
	 */
	public RWMolFragmentKey() {
		super();
	}

	/**
	 * Constructor initialises from an existing key object
	 * 
	 * @param existingKey
	 *            The existing object
	 */
	public RWMolFragmentKey(RWMolFragmentKey existingKey) {
		super(existingKey);

	}

	/**
	 * Constructor initialising from a SMILES string, with one or more
	 * components.
	 * 
	 * @param keyAsString
	 *            The key as a SMILES string
	 * @throws ToolkitException
	 */
	public RWMolFragmentKey(String keyAsString) throws ToolkitException {
		super(keyAsString);
	}

	@Override
	protected RWMolLeaf getLeafFromString(String keyAsString)
			throws IllegalArgumentException, ToolkitException {
		return new RWMolLeaf(keyAsString);
	}

}
