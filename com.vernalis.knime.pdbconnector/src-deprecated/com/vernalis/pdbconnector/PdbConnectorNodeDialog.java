/*******************************************************************************
 * Copyright (C) 2012, 2024 Vernalis (R&D) Ltd and Enspiral Discovery Limited
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 ******************************************************************************/
package com.vernalis.pdbconnector;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentLabel;

import com.vernalis.pdbconnector.config.PdbConnectorConfig;
import com.vernalis.pdbconnector.config.PdbConnectorConfig2;

/**
 * PdbConnectorNode dialog class.
 * 
 * @deprecated Use {@link PdbConnectorNodeDialog2} - ListCell types added
 */
@Deprecated
public class PdbConnectorNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * Instantiates a new pdb connector node dialog.
	 * 
	 * @param config
	 *            the configuration
	 */
	public PdbConnectorNodeDialog(final PdbConnectorConfig config) {
		super();
        addDialogComponent(
                new DialogComponentLabel(PdbConnectorConfig2.ERROR_MSG));
	}


}
