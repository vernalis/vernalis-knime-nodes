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

import org.RDKit.ROMol;

import com.vernalis.knime.nodes.propcalc.CalculatedPropertyInterface;

/**
 * Abstract NodeFactory implementation for RDKit-based nodes. As RDKit can parse
 * and always use all formats except CTab the only question is whether to accept
 * the strictly 2D SMILES format
 * 
 * @author s.roughley
 * 
 */
public class AbstractRdkitPropertyCalcNodeFactory extends
		AbstractMoleculePropertyCalcNodeFactory<AbstractRdkitPropertyCalcNodeModel, ROMol> {

	private final boolean checkZCoordinates;

	/**
	 * Overloaded constructor for node name same as property dialog name
	 * 
	 * @param nodeName
	 *            The node name
	 * @param properties
	 *            The properties to calculate
	 * @param acceptsSMILES
	 *            Should the node accept SMILES cells?
	 * @param acceptsRDKit
	 *            Should the node accept RDKit Cells?
	 * @param propertyFilterDialogTitle
	 *            The property dialog component title
	 * @param iconPath
	 *            The path to the node icon
	 * @param checkZCoordinates
	 *            Should the node check that there are non-zero z-coordinates
	 *            present in the table? If {@code true} and there are none found
	 *            then a warning message will be shown after node execution
	 */
	public AbstractRdkitPropertyCalcNodeFactory(
			CalculatedPropertyInterface<ROMol>[] properties,
			boolean acceptsSMILES, String propertyFilterDialogTitle,
			String iconPath, boolean checkZCoordinates) {
		this(propertyFilterDialogTitle, properties, acceptsSMILES,
				propertyFilterDialogTitle, iconPath, checkZCoordinates);
	}

	/**
	 * Constructor
	 * 
	 * @param nodeName
	 *            The node name
	 * @param properties
	 *            The properties to calculate
	 * @param acceptsSMILES
	 *            Should the node accept SMILES cells?
	 * @param acceptsRDKit
	 *            Should the node accept RDKit Cells?
	 * @param propertyFilterDialogTitle
	 *            The property dialog component title
	 * @param iconPath
	 *            The path to the node icon
	 * @param checkZCoordinates
	 *            Should the node check that there are non-zero z-coordinates
	 *            present in the table? If {@code true} and there are none found
	 *            then a warning message will be shown after node execution
	 */
	public AbstractRdkitPropertyCalcNodeFactory(String nodeName,
			CalculatedPropertyInterface<ROMol>[] properties,
			boolean acceptsSMILES, String propertyFilterDialogTitle,
			String iconPath, boolean checkZCoordinates) {

		super(nodeName, properties, true, true,
				false/* RDKit cannot parse CTab */, acceptsSMILES, true, true,
				propertyFilterDialogTitle, iconPath);
		this.checkZCoordinates = checkZCoordinates;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeModel()
	 */
	@Override
	public AbstractRdkitPropertyCalcNodeModel createNodeModel() {
		return new AbstractRdkitPropertyCalcNodeModel(getPropertyFilterTitle(),
				getProperties(), getAcceptedColumnsFilter(), checkZCoordinates);
	}

}
