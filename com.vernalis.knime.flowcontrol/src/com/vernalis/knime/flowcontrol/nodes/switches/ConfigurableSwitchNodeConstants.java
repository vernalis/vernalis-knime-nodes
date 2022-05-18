/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.flowcontrol.nodes.switches;

import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

/**
 * Some shared constants for the configurable switch nodes
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class ConfigurableSwitchNodeConstants {

	/**
	 * The available non-hidden, non-optional port types
	 */
	public static final PortType[] portTypes =
			PortTypeRegistry.getInstance().availablePortTypes().stream()
					.filter(pt -> !pt.isHidden() && !pt.isOptional())
					.toArray(PortType[]::new);
	/** Group name for the 'Else' output group */
	public static final String ELSE_OUTPUT_GROUP = "'Else' Output";
	/** Group name for the More outputs group */
	public static final String MORE_OUTPUTS_GROUP = "More Outputs";
	/** Group name for the Output group */
	public static final String OUTPUT_GROUP = "Output";
	/** Group name for the Input group */
	public static final String INPUT_GROUP = "Input";

	private ConfigurableSwitchNodeConstants() {
		// Utility Class - Do not Instantiate
		throw new UnsupportedOperationException();
	}
}
