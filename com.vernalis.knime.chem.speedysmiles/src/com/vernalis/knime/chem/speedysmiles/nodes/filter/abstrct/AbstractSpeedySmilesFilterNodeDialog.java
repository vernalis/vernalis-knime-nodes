/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.knime.chem.speedysmiles.nodes.filter.abstrct;

import com.vernalis.knime.chem.speedysmiles.nodes.abstrct.AbstractSpeedySmilesNodeDialog;
import com.vernalis.knime.nodes.AbstractStreamableParallelisedFilterSplitterNodeModel;

/**
 * This is the base class for SpeedySMILES filter/splitter node dialog panes,
 * providing options to select the SMILES column, and also to determin the
 * behaviour of matching and missing cells
 * 
 * @author s.roughley
 * 
 */
public class AbstractSpeedySmilesFilterNodeDialog
		extends AbstractSpeedySmilesNodeDialog {

	/**
	 * Constructor for the node dialog pane
	 */
	public AbstractSpeedySmilesFilterNodeDialog() {
		super();
		AbstractStreamableParallelisedFilterSplitterNodeModel
				.addFilterSplitterBehaviourDialogComponents(this);
	}
}
