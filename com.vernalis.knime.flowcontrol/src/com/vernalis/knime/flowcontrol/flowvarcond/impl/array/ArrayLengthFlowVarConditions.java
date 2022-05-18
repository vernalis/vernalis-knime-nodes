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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.VariableType;

import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapper;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapperNumberSpinner;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.SettingsModelFlowVarCondition;

/**
 * A set of conditions for array types based on the array length
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 * @param <T>
 *            The type of the simple (non-array) variable
 */
public class ArrayLengthFlowVarConditions<T> implements FlowVarCondition<T[]> {

	private final String id;
	private final String description;
	private final VariableType<T[]> type;
	private final BiIntToBooleanFunction testFunction;

	@FunctionalInterface
	private interface BiIntToBooleanFunction {

		boolean apply(int value, int reference);
	}

	/** An enum of the array length test functions to use */
	private enum TestFunctions implements BiIntToBooleanFunction {

		LT {

			@Override
			public boolean apply(int value, int reference) {
				return value < reference;
			}
		},
		LTE {

			@Override
			public boolean apply(int value, int reference) {
				return value <= reference;
			}

		},
		E {

			@Override
			public boolean apply(int value, int reference) {
				return value == reference;
			}

		},
		GT {

			@Override
			public boolean apply(int value, int reference) {
				return value > reference;
			}

		},
		GTE {

			@Override
			public boolean apply(int value, int reference) {
				return value >= reference;
			}

		};

		@Override
		public String toString() {
			return name().replace("GT", ">").replace("LT", "<").replace("E",
					"=");
		}
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID
	 * @param description
	 *            the description
	 * @param type
	 *            the array variable type
	 * @param testFunction
	 *            the test function for the condition
	 */
	private ArrayLengthFlowVarConditions(String id, String description,
			VariableType<T[]> type, BiIntToBooleanFunction testFunction) {
		this.id = id;
		this.description = description;
		this.type = type;
		this.testFunction = testFunction;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public String getDisplayName() {
		return "length " + testFunction.toString();
	}

	@Override
	public VariableType<T[]> getApplicableVariableType() {
		return type;
	}

	@Override
	public boolean compare(FlowVariable variable,
			SettingsModelFlowVarCondition model) {
		checkType(variable);
		int refLength =
				((ComponentWrapperNumberSpinner) model.getComponents().get(0))
						.getValue().intValue();
		int varArrLength =
				variable.getValue(getApplicableVariableType()).length;
		boolean retVal = testFunction.apply(varArrLength, refLength);
		if (model.isInverted()) {
			retVal = !retVal;
		}
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
		return Collections
				.singletonList(new ComponentWrapperNumberSpinner(
						new JSpinner(new SpinnerNumberModel(0,
								Integer.MIN_VALUE, Integer.MAX_VALUE, 1)),
						"Reference"));
	}

	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Method to get an iterator over the conditions for a particular variable
	 * type
	 * 
	 * @param <E>
	 *            the type of the simple element
	 * @param variableType
	 *            the array variable type
	 * 
	 * @return an iterator over the {@link FlowVarCondition}s
	 */
	public static <E> Iterator<FlowVarCondition<E[]>>
			getArrayLengthFunctionIterator(VariableType<E[]> variableType) {
		return new ArrayFlowVarConditionIterator<>(variableType);

	}

	private static class ArrayFlowVarConditionIterator<T>
			implements Iterator<FlowVarCondition<T[]>> {

		private final VariableType<T[]> varType;

		private ArrayFlowVarConditionIterator(VariableType<T[]> varType) {
			this.varType = Objects.requireNonNull(varType,
					"A variable type must be supplied");
		}

		private Iterator<TestFunctions> fnIter = createFunctionIterator();

		@Override
		public boolean hasNext() {
			return fnIter.hasNext();
		}

		private Iterator<TestFunctions> createFunctionIterator() {
			return Arrays.asList(TestFunctions.values()).iterator();
		}

		@Override
		public FlowVarCondition<T[]> next() {

			// Get the next function and use it
			TestFunctions fn = fnIter.next();
			return new ArrayLengthFlowVarConditions<>(
					getPrettyName() + " " + fn.name(),
					"Tests the " + getPrettyName() + " length is "
							+ fn.toString() + " to the reference",
					varType, fn);

		}

		/**
		 * @return
		 */
		private String getPrettyName() {
			return varType.getSimpleType().getSimpleName();
		}

	}

}
