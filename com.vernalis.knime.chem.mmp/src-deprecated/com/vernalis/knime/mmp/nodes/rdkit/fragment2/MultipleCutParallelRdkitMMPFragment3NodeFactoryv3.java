/*******************************************************************************
 * Copyright (c) 2017,2018 Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.nodes.rdkit.fragment2;

import org.RDKit.ROMol;
import org.RDKit.RWMol;

import com.vernalis.knime.mmp.fragutils.RWMolFragmentationUtilsFactory;
import com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeFactory;

/**
 * The node factory for the Multiple Cuts fragmentation node RDKit.
 * implementaiton. This is a replacement for the class with the same name,
 * suffixed 'Old' to overcome RDKit segfaults as of v3.3.1
 * 
 * @author s.roughley
 * 
 */
public class MultipleCutParallelRdkitMMPFragment3NodeFactoryv3
		extends AbstractMMPFragmentNodeFactory<RWMol, ROMol> {

	/**
	 * Constructor- Supplies version=3 to force backwards-compatible settings
	 */
	public MultipleCutParallelRdkitMMPFragment3NodeFactoryv3() {
		super(true, RWMolFragmentationUtilsFactory.class, 3);
	}

}
