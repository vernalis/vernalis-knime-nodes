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
package com.vernalis.pdbconnector.config;

/**
 * Manages the dialog options for PDB Connector KNIME Node.
 *
 * <P>
 * Singleton class to define the query and report options presented in the PDB
 * Connector nodes. The configuration is loaded dynamically from an external
 * <code>xml/PdbConnectorConfig.xml/dtd</code> file at run time, to allow for
 * updates to the supported PDB query and report options without the need for
 * code modification.
 *
 * @author dmorley
 */
@Deprecated
public class PdbConnectorConfig2 {

	/**
	 * Error message to display in all 'original' nodes
	 *
	 * @since 05-Jul-2022
	 */
	public static final String ERROR_MSG =
			"This node has been deprecated as the remote "
					+ "webservices have been permanently shutdown";

}
