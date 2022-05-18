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

import javax.swing.JTextField;

import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.VariableType;
import org.knime.core.node.workflow.VariableType.StringType;

import com.vernalis.knime.flowcontrol.flowvarcond.BooleanTestFunction;
import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapper;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapperCheckbox;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapperStringEntry;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.SettingsModelFlowVarCondition;

/**
 * An enum of {@link FlowVarCondition}s for String variables
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public enum StringFlowVarConditions implements FlowVarCondition<String> {

	Equals("=", new BooleanTestFunction<String>() {

		@Override
		public boolean apply(String value, String reference) {
			return value.equals(reference);
		}

	}, "Test for string equality"),

	Starts_With("Starts with", new BooleanTestFunction<String>() {

		@Override
		public boolean apply(String value, String reference) {
			return value.startsWith(reference);
		}
	}, "Test that the string starts with the reference string"),

	Ends_With("Ends with", new BooleanTestFunction<String>() {

		@Override
		public boolean apply(String value, String reference) {
			return value.endsWith(reference);
		}
	}, "Test that the string ends with the reference string"),

	Contains(new BooleanTestFunction<String>() {

		@Override
		public boolean apply(String value, String reference) {
			return value.endsWith(reference);
		}
	}, "Test that the string contains the reference "
			+ "string anywhere within"),

	Greater_Than(">", new BooleanTestFunction<String>() {

		@Override
		public boolean apply(String value, String reference) {
			return value.compareTo(reference) > 0;
		}
	}, "Tests whether the variable value is lexicographically "
			+ "greater than the reference value"),
	Greater_Than_Or_Equals(">=", new BooleanTestFunction<String>() {

		@Override
		public boolean apply(String value, String reference) {
			return value.compareTo(reference) > 0;
		}
	}, "Tests whether the variable value is lexicographically "
			+ "greater than or equal to the reference value"),
	Less_Than("<", new BooleanTestFunction<String>() {

		@Override
		public boolean apply(String value, String reference) {
			return value.compareTo(reference) < 0;
		}
	}, "Tests whether the variable value is lexicographically "
			+ "less than to the reference value"),
	Less_Than_Or_Equals("<=", new BooleanTestFunction<String>() {

		@Override
		public boolean apply(String value, String reference) {
			return value.compareTo(reference) <= 0;
		}
	}, "Tests whether the variable value is lexicographically "
			+ "less than or equal to the reference value");

	private final String displayName;
	private final String description;
	private final BooleanTestFunction<String> testFunction;

	private StringFlowVarConditions(BooleanTestFunction<String> testFunction,
			String description) {
		this(null, testFunction, description);
	}

	private StringFlowVarConditions(String displayName,
			BooleanTestFunction<String> testFunction, String description) {
		this.displayName = displayName;
		this.testFunction = testFunction;
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
	public VariableType<String> getApplicableVariableType() {
		return StringType.INSTANCE;
	}

	/**
	 * @param referenceValue
	 * @param ignoreCase
	 * @param trim
	 * 
	 * @return
	 */
	private String preprocessValue(String referenceValue, boolean ignoreCase,
			boolean trim) {
		if (referenceValue == null) {
			return null;
		}
		String retVal =
				ignoreCase ? referenceValue.toLowerCase() : referenceValue;
		retVal = trim ? retVal.trim() : retVal;
		return retVal;
	}

	@Override
	public boolean compare(FlowVariable variable,
			SettingsModelFlowVarCondition model) {
		boolean ignoreCase = false;
		boolean trim = false;
		String reference = null;
		for (ComponentWrapper<?, ?, ?> comp : model.getComponents()) {
			if (comp instanceof ComponentWrapperCheckbox) {
				ComponentWrapperCheckbox cb = (ComponentWrapperCheckbox) comp;
				if (cb.getID().equals("Ignore Case")) {
					ignoreCase = cb.getValue();
				} else if (cb.getID()
						.equals("Ignore leading / trailing whitespace")) {
					trim = cb.getValue();
				}
			} else if (comp instanceof ComponentWrapperStringEntry) {
				reference = ((ComponentWrapperStringEntry) comp).getValue();
			}
		}

		checkType(variable);

		String varVal =
				preprocessValue(variable.getValue(getApplicableVariableType()),
						ignoreCase, trim);
		reference = preprocessValue(reference, ignoreCase, trim);
		boolean retVal = testFunction.apply(varVal, reference);
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
		retVal.add(new ComponentWrapperStringEntry(new JTextField(20),
				"Reference Value"));
		retVal.add(new ComponentWrapperCheckbox("Ignore Case", true));
		retVal.add(new ComponentWrapperCheckbox(
				"Ignore leading / trailing whitespace", true));
		return retVal;
	}

	@Override
	public String getDescription() {
		return description;
	}

}
