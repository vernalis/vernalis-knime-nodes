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

import java.awt.Dimension;
import java.util.Collections;
import java.util.List;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.VariableType;
import org.knime.core.node.workflow.VariableType.IntType;

import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapper;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapperNumberSpinner;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.SettingsModelFlowVarCondition;

/**
 * An enum of {@link FlowVarCondition}s for integer variables
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public enum IntegerFlowVarConditions implements FlowVarCondition<Integer> {

	Equals("=", "Checks if the variable is equal to the reference") {

		@Override
		protected boolean compareValToRef(int varValue, int refValue) {
			return varValue == refValue;
		}
	},
	LT("<", "Checks if the variable is less than the reference") {

		@Override
		protected boolean compareValToRef(int varValue, int refValue) {
			return varValue < refValue;
		}
	},
	LTE("<=", "Checks if the variable is less than or equal to the reference") {

		@Override
		protected boolean compareValToRef(int varValue, int refValue) {
			return varValue <= refValue;
		}
	},
	GT(">", "Checks if the variable is greater than the reference") {

		@Override
		protected boolean compareValToRef(int varValue, int refValue) {
			return varValue > refValue;
		}
	},
	GTE(">=",
			"Checks if the variable is greater than or equal to the reference") {

		@Override
		protected boolean compareValToRef(int varValue, int refValue) {
			return varValue >= refValue;
		}
	};

	private final String description;
	private final String displayName;

	private IntegerFlowVarConditions(String displayName, String description) {
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
		final JSpinner intSpinner = new JSpinner(new SpinnerNumberModel(0,
				Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
		Dimension size =
				new Dimension(200, intSpinner.getPreferredSize().height);
		intSpinner.setPreferredSize(size);
		intSpinner.setMaximumSize(size);
		intSpinner.setMinimumSize(size);
		return Collections.singletonList(
				new ComponentWrapperNumberSpinner(intSpinner, "Reference"));
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
	public VariableType<Integer> getApplicableVariableType() {
		return IntType.INSTANCE;
	}

	@Override
	public boolean compare(FlowVariable variable,
			SettingsModelFlowVarCondition model) {
		checkType(variable);
		boolean retVal = compareValToRef(
				variable.getValue(getApplicableVariableType()).intValue(),
				((ComponentWrapperNumberSpinner) model.getComponents().get(0))
						.getValue().intValue());
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
	 * @return the result of the comparison between variable and reference
	 *         values
	 */
	protected abstract boolean compareValToRef(int varValue, int refValue);

	@Override
	public String getDescription() {
		return description;
	}
}
