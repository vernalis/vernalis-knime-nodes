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
package com.vernalis.knime.core.nodes.props.abstrct;

import com.vernalis.knime.nodes.propcalc.AbstractPropertyCalcNodeFactory;
import com.vernalis.knime.nodes.propcalc.CalculatedPropertyInterface;

/**
 * Abstract Node Factory implementation for calculated properties nodes
 * 
 * @author s.roughley
 * @param <T>
 *            The NodeModel implementation class
 * @param <U>
 *            The {@link CalculatedPropertyInterface} type parameter
 * 
 */
public abstract class AbstractMoleculePropertyCalcNodeFactory<T extends AbstractMoleculePropertyCalcNodeModel<U>, U>
		extends AbstractPropertyCalcNodeFactory<T, U> {

	protected final boolean acceptsMol, acceptsSDF, acceptsCTab, acceptsSMILES,
			acceptsPDB, acceptsRDKit;
	public static final String MOLECULE_COLUMN = "Molecule column";

	/**
	 * Overloaded constructor for node name same as property dialog name
	 * 
	 * @param properties
	 *            The properties to calculate
	 * @param acceptsMol
	 *            Should the node accept Mol cells?
	 * @param acceptsSDF
	 *            Should the node accept SDF cells?
	 * @param acceptsCTab
	 *            Should the node accept CTab cells?
	 * @param acceptsSMILES
	 *            Should the node accept SMILES cells?
	 * @param acceptsPDB
	 *            Should the node accept PDB Cells?
	 * @param acceptsRDKit
	 *            Should the node accept RDKit Cells?
	 * @param propertyFilterDialogTitle
	 *            The property dialog component title (and also the node name)
	 * @param iconPath
	 *            The path to the node icon
	 */
	public AbstractMoleculePropertyCalcNodeFactory(
			CalculatedPropertyInterface<U>[] properties, boolean acceptsMol,
			boolean acceptsSDF, boolean acceptsCTab, boolean acceptsSMILES,
			boolean acceptsPDB, boolean acceptsRDKit,
			String propertyFilterDialogTitle, String iconPath) {
		this(propertyFilterDialogTitle, properties, acceptsMol, acceptsSDF,
				acceptsCTab, acceptsSMILES, acceptsPDB, acceptsRDKit,
				propertyFilterDialogTitle, iconPath);
	}

	/**
	 * Full constructor
	 * 
	 * @param nodeName
	 *            The node name
	 * @param properties
	 *            The properties to calculate
	 * @param acceptsMol
	 *            Should the node accept Mol cells?
	 * @param acceptsSDF
	 *            Should the node accept SDF cells?
	 * @param acceptsCTab
	 *            Should the node accept CTab cells?
	 * @param acceptsSMILES
	 *            Should the node accept SMILES cells?
	 * @param acceptsPDB
	 *            Should the node accept PDB Cells?
	 * @param acceptsRDKit
	 *            Should the node accept RDKit Cells?
	 * @param propertyFilterDialogTitle
	 *            The property dialog component title
	 * @param iconPath
	 *            The path to the node icon
	 */
	public AbstractMoleculePropertyCalcNodeFactory(String nodeName,
			CalculatedPropertyInterface<U>[] properties, boolean acceptsMol,
			boolean acceptsSDF, boolean acceptsCTab, boolean acceptsSMILES,
			boolean acceptsPDB, boolean acceptsRDKit,
			String propertyFilterDialogTitle, String iconPath) {

		super(nodeName, MOLECULE_COLUMN, propertyFilterDialogTitle, iconPath,
				properties, new MoleculeTypeColumnFilter(acceptsMol, acceptsSDF,
						acceptsCTab, acceptsSMILES, acceptsPDB, acceptsRDKit));
		this.acceptsCTab = acceptsCTab;
		this.acceptsMol = acceptsMol;
		this.acceptsPDB = acceptsPDB;
		this.acceptsRDKit = acceptsRDKit;
		this.acceptsSDF = acceptsSDF;
		this.acceptsSMILES = acceptsSMILES;

	}

}
