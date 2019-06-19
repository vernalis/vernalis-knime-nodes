/*******************************************************************************
 * Copyright (c) 2014, Vernalis (R&D) Ltd
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
 *******************************************************************************/
package com.vernalis.knime.flowcontrol.nodes.abstrct.caseselect;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * Shared <code>NodeDialog</code> for the IF Switch and CASE Select Nodes.
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author S. D. Roughley
 */
public class AbstractCaseSelectNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring the node.
	 * 
	 * @param numPorts
	 *            the num ports
	 */
	public AbstractCaseSelectNodeDialog(int numPorts) {
		super();
		// NB We can add a FlowVariableModel to the DCN
		SettingsModelIntegerBounded smib = createSettingsModel(numPorts);
		FlowVariableModel fvm = createFlowVariableModel(smib);
		addDialogComponent(new DialogComponentNumber(smib,
				"Select the active port", 1, fvm));

	}

	/**
	 * Creates the settings model.
	 * 
	 * @param numPorts
	 *            the num ports
	 * @return the settings model integer bounded
	 */
	static SettingsModelIntegerBounded createSettingsModel(int numPorts) {
		return new SettingsModelIntegerBounded("Port Selection", 0, 0,
				(numPorts - 1));
	}
}
