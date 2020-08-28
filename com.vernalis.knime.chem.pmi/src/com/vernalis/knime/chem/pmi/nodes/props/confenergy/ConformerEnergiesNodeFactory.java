/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.knime.chem.pmi.nodes.props.confenergy;

import com.vernalis.knime.chem.pmi.props.RdkitConformerEnergy;
import com.vernalis.knime.core.nodes.props.abstrct.AbstractRdkitPropertyCalcNodeFactory;

/**
 * Node Factory implementation for the Conformer Energies node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class ConformerEnergiesNodeFactory
		extends AbstractRdkitPropertyCalcNodeFactory {

	/**
	 * Constructor
	 */
	public ConformerEnergiesNodeFactory() {
		super("Force Field Conformer Energies", RdkitConformerEnergy.values(),
				false, "Force Fields", "confs.png", true);
	}

}
