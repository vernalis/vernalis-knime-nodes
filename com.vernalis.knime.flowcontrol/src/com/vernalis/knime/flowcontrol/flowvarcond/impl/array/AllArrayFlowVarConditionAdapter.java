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
package com.vernalis.knime.flowcontrol.flowvarcond.impl.array;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.VariableType;

import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapper;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapperCheckbox;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.SettingsModelFlowVarCondition;

/**
 * A {@link FlowVarCondition} which wraps a simple type {@link FlowVarCondition}
 * such that all values of an array flow variable must pass the test
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 * @param <T>
 *            the simple type of the simple non-array variable
 */
public class AllArrayFlowVarConditionAdapter<T>
		implements FlowVarCondition<T[]> {

	private final FlowVarCondition<T> cond;
	private final VariableType<T[]> variableType;

	/**
	 * Constructor
	 * 
	 * @param cond
	 *            the simple type condition to wrap
	 * @param variableType
	 *            the variable array variable type
	 */
	public AllArrayFlowVarConditionAdapter(FlowVarCondition<T> cond,
			VariableType<T[]> variableType) {
		this.cond = cond;
		this.variableType = variableType;
	}

	@Override
	public String getID() {
		return "Array All(" + cond.getID() + ")";
	}

	@Override
	public String getDisplayName() {
		return "All " + cond.getID();
	}

	@Override
	public VariableType<T[]> getApplicableVariableType() {
		return variableType;
	}

	@Override
	public boolean compare(FlowVariable variable,
			SettingsModelFlowVarCondition model) {
		checkType(variable);

		boolean retVal = true;
		boolean isInverted = model.isInverted();
		boolean invertIndividuals =
				((ComponentWrapperCheckbox) model.getComponents()
						.get(model.getComponents().size() - 1)).getValue();
		T[] vArr = variable.getValue(getApplicableVariableType());
		for (T val : vArr) {
			FlowVariable fVar = new FlowVariable("temp",
					cond.getApplicableVariableType(), val);
			// The condition will have applied the 'global' 'NOT' from the
			// model...
			retVal &= (isInverted ^ invertIndividuals)
					^ cond.compare(fVar, model);
			if (!retVal) {
				break;
			}
		}
		retVal = isInverted ^ retVal;
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition#
	 * getReferenceComponents()
	 */
	@Override
	public List<ComponentWrapper<?, ?, ?>> getReferenceComponents() {
		// Wrap in a new collection in case the underlying condition returns
		// something unmodifiable (e.g Collections.emptyList(),
		// Collections.singletonList(), Collections.unmodifiableCollection(),
		// Arrays.asList(...)...)
		List<ComponentWrapper<?, ?, ?>> retVal =
				new ArrayList<>(cond.getReferenceComponents());
		retVal.add(
				new ComponentWrapperCheckbox("NOT Array Member Check", true));
		return retVal;
	}

	@Override
	public String getDescription() {
		return "All members of "
				+ getApplicableVariableType().getSimpleType().getSimpleName()
				+ " match the condition '" + cond.getDescription() + "'";
	}

}
