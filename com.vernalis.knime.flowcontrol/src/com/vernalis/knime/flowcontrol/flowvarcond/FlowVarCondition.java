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
package com.vernalis.knime.flowcontrol.flowvarcond;

import java.util.Collections;
import java.util.List;

import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.VariableType;

import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapper;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.SettingsModelFlowVarCondition;

/**
 * An interface defining the API for a Flow Variable comparison, as used in the
 * new generation 'Flow Variable Value' flow control nodes
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 * @param <T>
 *            The simple type of the Variable (see {@link VariableType}) to
 *            which the condition applies
 */
public interface FlowVarCondition<T> {

	/**
	 * @return The ID of the comparison - this should convey the comparison type
	 */
	public String getID();

	/**
	 * @return The value to display in the dropdown in the node dialog
	 */
	public String getDisplayName();

	/**
	 * @return The variable type to which the condition applies
	 */
	public VariableType<T> getApplicableVariableType();

	/**
	 * The method which performs the comparison. The method should validate the
	 * variable with a call to {@link #checkType(FlowVariable)}, and also
	 * account for the value of the
	 * {@link SettingsModelFlowVarCondition#isInverted()} setting
	 * 
	 * @param variable
	 *            The variable to test the value of
	 * @param model
	 *            The settings model containing the variable
	 * 
	 * @return true if the variable value passed the conditions set by the model
	 */
	public boolean compare(FlowVariable variable,
			SettingsModelFlowVarCondition model);

	/**
	 * @return Any components needed for reference values and comparison
	 *         settings
	 */
	public default List<ComponentWrapper<?, ?, ?>> getReferenceComponents() {
		return Collections.emptyList();
	};

	/**
	 * @param variable
	 *            The variable to check the type of
	 * 
	 * @throws IllegalArgumentException
	 *             if the wrong type of variable is supplied
	 */
	public default void checkType(FlowVariable variable)
			throws IllegalArgumentException {
		if (!variable.getVariableType().equals(getApplicableVariableType())) {
			throw new IllegalArgumentException(
					"Wrong variable type supplied. Expected "
							+ getApplicableVariableType().getIdentifier()
							+ ", got "
							+ variable.getVariableType().getIdentifier());
		}

	}

	/**
	 * @return A unique ID for loading into the
	 *         {@link FlowVarConditionRegistry}. The default implementation
	 *         concatenates the variable type, ID and display name
	 */
	public default String getUniqueID() {
		return String.format("%s_%s_%s",
				getApplicableVariableType().getIdentifier(), getID(),
				getDisplayName());
	}

	/**
	 * @return A brief description of the comparison performed, which will be
	 *         displayed as a tooltip in the dropdown
	 */
	public String getDescription();
}
