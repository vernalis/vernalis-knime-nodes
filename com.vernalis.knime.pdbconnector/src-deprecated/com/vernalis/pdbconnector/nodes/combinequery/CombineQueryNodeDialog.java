/*******************************************************************************
 * Copyright (c) 2016, 2022, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector.nodes.combinequery;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentLabel;

import com.vernalis.pdbconnector.config.PdbConnectorConfig2;

/**
 * Node Dialog Pane for the combine query node
 */
@Deprecated
public class CombineQueryNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * Constructor for the node dialog pane
	 */
	public CombineQueryNodeDialog() {

		addDialogComponent(
				new DialogComponentLabel(PdbConnectorConfig2.ERROR_MSG));
	}

}
