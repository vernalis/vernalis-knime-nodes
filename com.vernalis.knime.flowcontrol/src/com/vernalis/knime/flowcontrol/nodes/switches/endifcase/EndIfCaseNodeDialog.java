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
package com.vernalis.knime.flowcontrol.nodes.switches.endifcase;

import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortType;

import com.vernalis.knime.flowcontrol.portcombiner.PortTypeCombinerRegistry;
import com.vernalis.knime.flowcontrol.portcombiner.api.PortTypeCombiner;

/**
 * Node dialog for the Configurable End IF/Case node
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class EndIfCaseNodeDialog extends DefaultNodeSettingsPane {

	private static final String MULTIPLE_ACTIVES_BEHAVIOUR =
			"Multiple-active behaviour";

	/**
	 * Constructor
	 * 
	 * @param config
	 *            The node config to determine the appropriate dialog options
	 */
	public EndIfCaseNodeDialog(NodeCreationConfiguration config) {
		super();
		PortType pType = config.getPortConfig()
				.orElseThrow(IllegalArgumentException::new).getOutputPorts()[0];
		PortTypeCombinerRegistry ptcr = PortTypeCombinerRegistry.getInstance();
		PortTypeCombiner combiner = ptcr.getCombiner(pType);
		if (combiner == null) {
			addDialogComponent(new DialogComponentButtonGroup(
					createMultiActivesModel(), MULTIPLE_ACTIVES_BEHAVIOUR,
					false, MultiActivesPolicy.values()));
		} else if (combiner.hasDialogOptions()) {
			combiner.createDialog(this, combiner.getCombinerModels());
			renameTab("Options", "Port combining options");
		} else {
			removeTab("Options");
		}
	}

	/**
	 * Creates the settings model for behaviour when multiple input ports are
	 * active
	 * 
	 * @return the settings model string
	 */
	static SettingsModelString createMultiActivesModel() {
		return new SettingsModelString("Multiple-active behaviour",
				MultiActivesPolicy.getDefault().getActionCommand());
	}
}
