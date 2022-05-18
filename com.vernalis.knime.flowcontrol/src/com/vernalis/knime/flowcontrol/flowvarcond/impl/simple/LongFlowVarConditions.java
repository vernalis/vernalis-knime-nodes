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

import java.util.Collections;
import java.util.List;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.VariableType;
import org.knime.core.node.workflow.VariableType.LongType;

import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapper;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapperNumberSpinner;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.SettingsModelFlowVarCondition;

/**
 * {@link FlowVarCondition}s for long variables
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public enum LongFlowVarConditions implements FlowVarCondition<Long> {

	Equals("=", "Checks if the variable is equal to the reference") {

		@Override
		protected boolean compareValToRef(long varValue, long refValue) {
			return varValue == refValue;
		}
	},
	LT("<", "Checks if the variable is less than the reference") {

		@Override
		protected boolean compareValToRef(long varValue, long refValue) {
			return varValue < refValue;
		}
	},
	LTE("<=", "Checks if the variable is less than or equal to the reference") {

		@Override
		protected boolean compareValToRef(long varValue, long refValue) {
			return varValue <= refValue;
		}
	},
	GT(">", "Checks if the variable is greater than the reference") {

		@Override
		protected boolean compareValToRef(long varValue, long refValue) {
			return varValue > refValue;
		}
	},
	GTE(">=",
			"Checks if the variable is greater than or equal to the reference") {

		@Override
		protected boolean compareValToRef(long varValue, long refValue) {
			return varValue >= refValue;
		}
	};

	private final String description;
	private final String displayName;

	private LongFlowVarConditions(String displayName, String description) {
		this.displayName = displayName;
		this.description = description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition#
	 * getReferenceComponents()
	 */
	@Override
	public List<ComponentWrapper<?, ?, ?>> getReferenceComponents() {
		return Collections
				.singletonList(
						new ComponentWrapperNumberSpinner(
								new JSpinner(new SpinnerNumberModel(0L,
										Long.MIN_VALUE, Long.MAX_VALUE, 1L)),
								"Reference"));
	}

	@Override
	public String getID() {
		return name();
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public VariableType<Long> getApplicableVariableType() {
		return LongType.INSTANCE;
	}

	@Override
	public boolean compare(FlowVariable variable,
			SettingsModelFlowVarCondition model) {
		checkType(variable);
		boolean retVal = compareValToRef(
				variable.getValue(getApplicableVariableType()).longValue(),
				((ComponentWrapperNumberSpinner) model.getComponents().get(0))
						.getValue().longValue());
		if (model.isInverted()) {
			retVal = !retVal;
		}
		return retVal;
	}

	/**
	 * @param varValue
	 *            the variable value
	 * @param refValue
	 *            the reference value
	 * 
	 * @return the result of the comparison
	 */
	protected abstract boolean compareValToRef(long varValue, long refValue);

	@Override
	public String getDescription() {
		return description;
	}
}
