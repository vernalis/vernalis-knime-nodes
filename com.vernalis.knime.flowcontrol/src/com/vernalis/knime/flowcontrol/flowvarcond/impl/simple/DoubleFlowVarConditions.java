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
import java.util.Arrays;
import java.util.List;

import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.SpinnerNumberModel;

import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.VariableType;
import org.knime.core.node.workflow.VariableType.DoubleType;

import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapper;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapperLabel;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapperNumberSpinner;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.SettingsModelFlowVarCondition;

/**
 * Flow variable conditions for double variables
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public enum DoubleFlowVarConditions implements FlowVarCondition<Double> {

	Equals("=", "Checks if the variable is equal to the reference") {

		@Override
		protected boolean getResult(int comp, boolean isWithinEpsilon) {
			return comp == 0 || isWithinEpsilon;
		}
	},
	LT("<", "Checks if the variable is less than the reference") {

		@Override
		protected boolean getResult(int comp, boolean isWithinEpsilon) {
			return comp < 0;
		}
	},
	LTE("<=", "Checks if the variable is less than or equal to the reference") {

		@Override
		protected boolean getResult(int comp, boolean isWithinEpsilon) {
			return comp <= 0 || isWithinEpsilon;
		}
	},
	GT(">", "Checks if the variable is greater than the reference") {

		@Override
		protected boolean getResult(int comp, boolean isWithinEpsilon) {
			return comp > 0;
		}
	},
	GTE(">=",
			"Checks if the variable is greater than or equal to the reference") {

		@Override
		protected boolean getResult(int comp, boolean isWithinEpsilon) {
			return comp >= 0 || isWithinEpsilon;
		}
	};

	private final String description, displayName;

	private DoubleFlowVarConditions(String displayName, String description) {
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
		final JSpinner refSpinner = new JSpinner(new SpinnerNumberModel(0.0,
				-Double.MAX_VALUE, Double.MAX_VALUE, 0.1));
		final Dimension preferredSize =
				new Dimension(200, refSpinner.getPreferredSize().height);
		refSpinner.setPreferredSize(preferredSize);
		refSpinner.setMinimumSize(preferredSize);
		refSpinner.setMaximumSize(preferredSize);

		final JSpinner deltaSpinner = new JSpinner(
				new SpinnerNumberModel(0.0001, 0.0, Double.MAX_VALUE, 0.0001));
		deltaSpinner.setPreferredSize(
				new Dimension(200, deltaSpinner.getPreferredSize().height));
		deltaSpinner.setMinimumSize(deltaSpinner.getPreferredSize());
		deltaSpinner.setMaximumSize(deltaSpinner.getPreferredSize());
		deltaSpinner.setEditor(new NumberEditor(deltaSpinner, "#.#####"));
		return Arrays.asList(
				new ComponentWrapperNumberSpinner(refSpinner, "Reference"),
				new ComponentWrapperLabel("\u03B5", true),
				new ComponentWrapperNumberSpinner(deltaSpinner, "\u03B5",
						true));
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
	public VariableType<Double> getApplicableVariableType() {
		return DoubleType.INSTANCE;
	}

	@Override
	public boolean compare(FlowVariable variable,
			SettingsModelFlowVarCondition model) {
		checkType(variable);
		final double varVal =
				variable.getValue(getApplicableVariableType()).doubleValue();
		final double refVal =
				((ComponentWrapperNumberSpinner) model.getComponents().get(0))
						.getValue().doubleValue();
		final double epsilon =
				((ComponentWrapperNumberSpinner) model.getComponents().get(2))
						.getValue().doubleValue();
		int comp = Double.compare(varVal, refVal);
		boolean isWithinEpsilon = Math.abs(refVal - varVal) < epsilon;
		boolean retVal = getResult(comp, isWithinEpsilon);
		if (model.isInverted()) {
			retVal = !retVal;
		}
		return retVal;
	}

	/**
	 * @param comp
	 *            the value of {@link Double#compare(double, double)} on the
	 *            variable and reference values
	 * @param isWithinEpsilon
	 *            whether the two numbers are within the epsilon value
	 * 
	 * @return the result of the comparison
	 */
	protected abstract boolean getResult(int comp, boolean isWithinEpsilon);

	@Override
	public String getDescription() {
		return description;
	}
}
