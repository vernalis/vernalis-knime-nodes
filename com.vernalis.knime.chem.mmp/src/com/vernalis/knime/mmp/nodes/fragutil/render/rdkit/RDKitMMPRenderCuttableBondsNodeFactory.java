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
package com.vernalis.knime.mmp.nodes.fragutil.render.rdkit;

import org.RDKit.ROMol;
import org.RDKit.RWMol;

import com.vernalis.knime.mmp.fragutils.RWMolFragmentationUtilsFactory;
import com.vernalis.knime.mmp.nodes.fragutil.render.abstrct.AbstractMMPCuttableBondsRenderNodeFactory;

/**
 * The node factory class for the RDKit implementation of the Render Cuttable
 * bonds node
 * 
 * @author s.roughley
 *
 */
public class RDKitMMPRenderCuttableBondsNodeFactory
		extends AbstractMMPCuttableBondsRenderNodeFactory<RWMol, ROMol> {

	/**
	 * Constructor
	 */
	public RDKitMMPRenderCuttableBondsNodeFactory() {
		super(RWMolFragmentationUtilsFactory.class);
	}

}
