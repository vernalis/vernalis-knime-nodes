/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd, based on earlier PDB Connector work.
 * 
 * Copyright (c) 2012, 2014 Vernalis (R&D) Ltd and Enspiral Discovery Limited
 * 
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.pdbconnector.nodes.pdbconnector;

import org.knime.core.node.NodeModel;
import org.knime.core.node.port.PortType;

/**
 * Abstract NodeModel which serves as a marker for nodes able to provide an XML
 * Query String to the view classes.
 * 
 * @see AbstractXMLAdvancedQueryNodeView
 * @see AbstractXMLAdvancedQueryLogicalNodeView
 */
@Deprecated
public abstract class AbstractXMLQueryProviderNodeModel extends NodeModel {

	/**
	 * @param nrInDataPorts
	 * @param nrOutDataPorts
	 */
	public AbstractXMLQueryProviderNodeModel(int nrInDataPorts,
			int nrOutDataPorts) {
		super(nrInDataPorts, nrOutDataPorts);

	}

	/**
	 * @param inPortTypes
	 * @param outPortTypes
	 */
	public AbstractXMLQueryProviderNodeModel(PortType[] inPortTypes,
			PortType[] outPortTypes) {
		super(inPortTypes, outPortTypes);

	}

	/**
	 * Method called by the NodeView to obtain the raw XML Query string to parse
	 * 
	 * @return The XML Query
	 */
	public abstract String getXMLQuery();
}
