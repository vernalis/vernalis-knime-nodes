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
package com.vernalis.knime.flowcontrol.flowvarcond.impl.simple;

import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.VariableType;
import org.knime.core.node.workflow.VariableType.BooleanType;

import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.SettingsModelFlowVarCondition;

/**
 * An enumb of variable conditions for boolean variables
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public enum BooleanFlowVarConditions implements FlowVarCondition<Boolean> {

	IsTrue("is True", "Test whether the variable value is the logical 'true'") {

		@Override
		public boolean compare(FlowVariable variable,
				SettingsModelFlowVarCondition model) {
			checkType(variable);
			boolean retVal = variable.getValue(getApplicableVariableType());
			if (model.isInverted()) {
				retVal = !retVal;
			}
			return retVal;
		}

	},

	IsFalse("is False",
			"Test whether the variable value is the logical 'false'") {

		@Override
		public boolean compare(FlowVariable variable,
				SettingsModelFlowVarCondition model) {
			checkType(variable);
			boolean retVal = !variable.getValue(getApplicableVariableType());
			if (model.isInverted()) {
				retVal = !retVal;
			}
			return retVal;
		}
	};

	private final String displayName;
	private final String description;

	private BooleanFlowVarConditions(String displayName, String description) {
		this.displayName = displayName;
		this.description = description;
	}

	@Override
	public String getID() {
		return name();
	}

	@Override
	public String getDisplayName() {
		return displayName == null ? getID() : displayName;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public VariableType<Boolean> getApplicableVariableType() {
		return BooleanType.INSTANCE;
	}

}
