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

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.VariableType;
import org.knime.core.node.workflow.VariableType.StringType;

import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapper;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapperCheckbox;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.SettingsModelFlowVarCondition;

/**
 * {@link FlowVarCondition} implementation to check whether a String variable
 * contains an empty string
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class EmptyStringFlowVarCondition implements FlowVarCondition<String> {

	@Override
	public String getID() {
		return "Empty String";
	}

	@Override
	public String getDisplayName() {
		return "Is Empty";
	}

	@Override
	public VariableType<String> getApplicableVariableType() {
		return StringType.INSTANCE;
	}

	/**
	 * @param referenceValue
	 * @param ignoreCase
	 * @param trim
	 * @return
	 */
	private String preprocessValue(String str, boolean trim) {
		return trim ? str.trim() : str;
	}

	@Override
	public boolean compare(FlowVariable variable,
			SettingsModelFlowVarCondition model) {
		boolean trim = ((ComponentWrapperCheckbox) model.getComponents().get(0))
				.getValue();
		checkType(variable);
		String varVal = preprocessValue(
				variable.getValue(getApplicableVariableType()), trim);
		boolean retVal = varVal.isEmpty();
		if (model.isInverted()) {
			retVal = !retVal;
		}
		return retVal;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.flowcontrol.nodes.abstrct.varvalifswitch.
	 * FlowVarCondition#getReferenceComponents()
	 */
	@Override
	public List<ComponentWrapper<?, ?, ?>> getReferenceComponents() {
		List<ComponentWrapper<?, ?, ?>> retVal = new ArrayList<>();
		retVal.add(new ComponentWrapperCheckbox(
				"Ignore leading / trailing whitespace", true));
		return retVal;
	}

	@Override
	public String getDescription() {
		return "Checks for an empty string";
	}

}
