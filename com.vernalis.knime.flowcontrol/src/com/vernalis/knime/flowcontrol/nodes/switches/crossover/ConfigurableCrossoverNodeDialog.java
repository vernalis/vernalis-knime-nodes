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
package com.vernalis.knime.flowcontrol.nodes.switches.crossover;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarConditionRegistry;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.DialogComponentFlowVariableCondition;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.SettingsModelFlowVarCondition;

/**
 * Node dialog for the 'Configurable Crossover' node
 * 
 * @author "Stephen Roughley knime@vernalis.com"
 */
public class ConfigurableCrossoverNodeDialog extends DefaultNodeSettingsPane {

	private static final String VARIABLE_CONDITION = "Variable Condition";

	/**
	 * Instantiates a new node dialog.
	 */
	public ConfigurableCrossoverNodeDialog() {
		super();

		final DialogComponentFlowVariableCondition diaC =
				new DialogComponentFlowVariableCondition(createCondModel(),
						VARIABLE_CONDITION,
						() -> getAvailableFlowVariables(
								FlowVarConditionRegistry.getInstance()
										.getAvailableFilterVariableTypes()));
		diaC.hideOutputVariable(true);
		addDialogComponent(diaC);

	}

	/**
	 * @return model for the Variable condition setting
	 *
	 * @since 17-May-2022
	 */
	static SettingsModelFlowVarCondition createCondModel() {
		return new SettingsModelFlowVarCondition(VARIABLE_CONDITION);
	}

}
