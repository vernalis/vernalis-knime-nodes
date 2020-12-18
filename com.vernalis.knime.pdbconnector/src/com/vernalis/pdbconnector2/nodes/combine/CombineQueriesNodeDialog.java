/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.nodes.combine;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.pdbconnector2.query.text.dialog.QueryGroupConjunction;

/**
 * {@link DefaultNodeSettingsPane} implementation for the PDB Connector Combine
 * Queries node
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class CombineQueriesNodeDialog extends DefaultNodeSettingsPane {

	private static final String CONJUNCTION = "Conjunction";

	/**
	 * Constructor
	 */
	public CombineQueriesNodeDialog() {
		super();
		addDialogComponent(
				new DialogComponentButtonGroup(createConjunctionModel(),
						CONJUNCTION, false, QueryGroupConjunction.values()));
	}

	/**
	 * @return The model for the 'Conjunction' setting
	 */
	static SettingsModelString createConjunctionModel() {
		return new SettingsModelString(CONJUNCTION,
				QueryGroupConjunction.getDefault().getActionCommand());
	}

}
