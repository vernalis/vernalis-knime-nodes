/*******************************************************************************
 * Copyright (c) 2019,2023, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.collection.missing;

import com.vernalis.nodes.collection.abstrct.AbstractMultiCollectionNodeDialog;

/**
 * Node Dialog implementation for the Empty collection to missing cell node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class MissingToEmptyCollectionNodeDialog
		extends AbstractMultiCollectionNodeDialog {

	/**
	 * Constructor
	 */
	public MissingToEmptyCollectionNodeDialog() {
		super(true);
	}

}
