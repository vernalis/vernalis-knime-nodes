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
package com.vernalis.knime.mmp.nodes.fragutil.filter.rdkit;

import org.RDKit.ROMol;
import org.RDKit.RWMol;

import com.vernalis.knime.mmp.fragutils.RWMolFragmentationUtilsFactory;
import com.vernalis.knime.mmp.nodes.fragutil.filter.abstrct.AbstractMMPFragmentationFilterSplitterNodeFactory;

/**
 * Node Factory class for the MMP prefilter RDKit implementation
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class RDKitMMPFilterNodeFactory
		extends AbstractMMPFragmentationFilterSplitterNodeFactory<RWMol, ROMol> {

	/**
	 * Constructor
	 */
	public RDKitMMPFilterNodeFactory() {
		super(RWMolFragmentationUtilsFactory.class, false);
	}
}
