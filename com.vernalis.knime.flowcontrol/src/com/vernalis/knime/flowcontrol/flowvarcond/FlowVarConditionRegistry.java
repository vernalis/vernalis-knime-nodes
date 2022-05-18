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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.workflow.VariableType;
import org.knime.core.node.workflow.VariableType.BooleanArrayType;
import org.knime.core.node.workflow.VariableType.BooleanType;
import org.knime.core.node.workflow.VariableType.DoubleArrayType;
import org.knime.core.node.workflow.VariableType.DoubleType;
import org.knime.core.node.workflow.VariableType.IntArrayType;
import org.knime.core.node.workflow.VariableType.IntType;
import org.knime.core.node.workflow.VariableType.LongArrayType;
import org.knime.core.node.workflow.VariableType.LongType;
import org.knime.core.node.workflow.VariableType.StringArrayType;
import org.knime.core.node.workflow.VariableType.StringType;

import com.vernalis.knime.flowcontrol.flowvarcond.impl.array.AllArrayFlowVarConditionAdapter;
import com.vernalis.knime.flowcontrol.flowvarcond.impl.array.AnyArrayFlowVarConditionAdapter;
import com.vernalis.knime.flowcontrol.flowvarcond.impl.array.ArrayLengthFlowVarConditions;
import com.vernalis.knime.flowcontrol.flowvarcond.impl.path.PathFSPathFlowVarConditions;
import com.vernalis.knime.flowcontrol.flowvarcond.impl.path.PathFlowVarConditions;
import com.vernalis.knime.flowcontrol.flowvarcond.impl.path.PathStringFlowVarConditions;
import com.vernalis.knime.flowcontrol.flowvarcond.impl.simple.BooleanFlowVarConditions;
import com.vernalis.knime.flowcontrol.flowvarcond.impl.simple.DoubleFlowVarConditions;
import com.vernalis.knime.flowcontrol.flowvarcond.impl.simple.IntegerFlowVarConditions;
import com.vernalis.knime.flowcontrol.flowvarcond.impl.simple.LongFlowVarConditions;
import com.vernalis.knime.flowcontrol.flowvarcond.impl.simple.StringFlowVarConditions;

/**
 * Class to hold the registered and default {@link FlowVarCondition}s for use in
 * Flow Variable value comparison nodes
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class FlowVarConditionRegistry {

	private static final String CLASS = "class";
	private Map<String, FlowVarCondition<?>> uniqueIDLookup = new HashMap<>();
	private Map<VariableType<?>, Set<FlowVarCondition<?>>> typeLookup =
			new HashMap<>();
	private String longestDisplayName;

	private static final NodeLogger logger =
			NodeLogger.getLogger(FlowVarConditionRegistry.class);

	/**
	 * Private constructor
	 */
	@SuppressWarnings("unchecked")
	private FlowVarConditionRegistry() {

		// Handle the pre-defined ones
		Arrays.stream(StringFlowVarConditions.values())
				.forEach(x -> addCondition(x));
		Arrays.stream(BooleanFlowVarConditions.values())
				.forEach(x -> addCondition(x));
		Arrays.stream(IntegerFlowVarConditions.values())
				.forEach(x -> addCondition(x));
		Arrays.stream(LongFlowVarConditions.values())
				.forEach(x -> addCondition(x));
		Arrays.stream(DoubleFlowVarConditions.values())
				.forEach(x -> addCondition(x));
		Arrays.stream(PathFlowVarConditions.values())
				.forEach(x -> addCondition(x));
		Arrays.stream(PathFSPathFlowVarConditions.values())
				.forEach(x -> addCondition(x));
		Arrays.stream(PathStringFlowVarConditions.values())
				.forEach(x -> addCondition(x));

		// Now the Array Types - all have the same set of 'length' filters:
		ArrayLengthFlowVarConditions
				.getArrayLengthFunctionIterator(StringArrayType.INSTANCE)
				.forEachRemaining(x -> addCondition(x));
		ArrayLengthFlowVarConditions
				.getArrayLengthFunctionIterator(BooleanArrayType.INSTANCE)
				.forEachRemaining(x -> addCondition(x));
		ArrayLengthFlowVarConditions
				.getArrayLengthFunctionIterator(IntArrayType.INSTANCE)
				.forEachRemaining(x -> addCondition(x));
		ArrayLengthFlowVarConditions
				.getArrayLengthFunctionIterator(LongArrayType.INSTANCE)
				.forEachRemaining(x -> addCondition(x));
		ArrayLengthFlowVarConditions
				.getArrayLengthFunctionIterator(DoubleArrayType.INSTANCE)
				.forEachRemaining(x -> addCondition(x));

		// Now the extension point....
		List<IConfigurationElement> config = new ArrayList<>();
		Collections.addAll(config,
				Platform.getExtensionRegistry().getConfigurationElementsFor(
						"com.vernalis.knime.internal.flowcontrol.variablecondition"));
		Collections.addAll(config,
				Platform.getExtensionRegistry().getConfigurationElementsFor(
						"com.vernalis.knime.flowcontrol.variablecondition"));
		for (IConfigurationElement elem : config) {
			try {
				Object obj = elem.createExecutableExtension(CLASS);
				if (obj instanceof FlowVarCondition) {
					addCondition((FlowVarCondition<?>) obj);
				} else {
					logger.coding(
							"Error loading flow variable condition with class '"
									+ elem.getAttribute(CLASS)
									+ "' - Implementation is not of 'FlowVarCondition' interface");
				}
			} catch (CoreException e) {
				logger.coding(
						"Error loading flow variable condition with class '"
								+ elem.getAttribute(CLASS) + "' - "
								+ e.getMessage(),
						e);
			}
		}

		// Finally, we wrap all the 'singleton' types into the corresponding
		// 'array' types with 'All' and 'Any':
		getConditions(StringType.INSTANCE).forEach(
				x -> addCondition(new AllArrayFlowVarConditionAdapter<>(
						(FlowVarCondition<String>) x,
						StringArrayType.INSTANCE)));
		getConditions(BooleanType.INSTANCE).forEach(
				x -> addCondition(new AllArrayFlowVarConditionAdapter<>(
						(FlowVarCondition<Boolean>) x,
						BooleanArrayType.INSTANCE)));
		getConditions(IntType.INSTANCE).forEach(
				x -> addCondition(new AllArrayFlowVarConditionAdapter<>(
						(FlowVarCondition<Integer>) x, IntArrayType.INSTANCE)));
		getConditions(LongType.INSTANCE).forEach(
				x -> addCondition(new AllArrayFlowVarConditionAdapter<>(
						(FlowVarCondition<Long>) x, LongArrayType.INSTANCE)));
		getConditions(DoubleType.INSTANCE).forEach(
				x -> addCondition(new AllArrayFlowVarConditionAdapter<>(
						(FlowVarCondition<Double>) x,
						DoubleArrayType.INSTANCE)));

		getConditions(StringType.INSTANCE).forEach(
				x -> addCondition(new AnyArrayFlowVarConditionAdapter<>(
						(FlowVarCondition<String>) x,
						StringArrayType.INSTANCE)));
		getConditions(BooleanType.INSTANCE).forEach(
				x -> addCondition(new AnyArrayFlowVarConditionAdapter<>(
						(FlowVarCondition<Boolean>) x,
						BooleanArrayType.INSTANCE)));
		getConditions(IntType.INSTANCE).forEach(
				x -> addCondition(new AnyArrayFlowVarConditionAdapter<>(
						(FlowVarCondition<Integer>) x, IntArrayType.INSTANCE)));
		getConditions(LongType.INSTANCE).forEach(
				x -> addCondition(new AnyArrayFlowVarConditionAdapter<>(
						(FlowVarCondition<Long>) x, LongArrayType.INSTANCE)));
		getConditions(DoubleType.INSTANCE).forEach(
				x -> addCondition(new AnyArrayFlowVarConditionAdapter<>(
						(FlowVarCondition<Double>) x,
						DoubleArrayType.INSTANCE)));
	}

	/**
	 * Holding class idiom provides automatic lazy instantiation and
	 * synchronization See Joshua Bloch Effective Java Item 71
	 */
	private static final class HoldingClass {

		private static FlowVarConditionRegistry INSTANCE =
				new FlowVarConditionRegistry();
	}

	/**
	 * Method to get the singleton instance
	 * 
	 * @return The singleton instance of FlowVarConditionRegistry
	 */
	public static FlowVarConditionRegistry getInstance() {
		return HoldingClass.INSTANCE;
	}

	private void addCondition(FlowVarCondition<?> cond) {
		String uid = cond.getUniqueID();
		if (uniqueIDLookup.containsKey(uid)) {
			logger.codingWithFormat(
					"Implementation error - duplicate variable condition unique IDs (%s)"
							+ " - ignoring condition %s (Display name: %s) for variable type %s",
					uid, cond.getID(), cond.getDisplayName(),
					cond.getApplicableVariableType().getIdentifier());
			return;
		}
		typeLookup.computeIfAbsent(cond.getApplicableVariableType(),
				k -> new LinkedHashSet<>()).add(cond);
		uniqueIDLookup.put(uid, cond);
		if (longestDisplayName == null || cond.getDisplayName()
				.length() > longestDisplayName.length()) {
			longestDisplayName = cond.getDisplayName();
		}
	}

	/**
	 * @return an array of all the variable types for which conditions are
	 *         available
	 */
	public VariableType<?>[] getAvailableFilterVariableTypes() {
		return typeLookup.keySet().toArray(new VariableType<?>[0]);
	}

	/**
	 * @param varType
	 *            the variable type
	 * 
	 * @return the available conditions for the variable type
	 */
	public List<FlowVarCondition<?>> getConditions(VariableType<?> varType) {
		return Collections
				.unmodifiableList(new ArrayList<>(typeLookup.get(varType)));
	}

	/**
	 * @param varType
	 *            the variable type
	 * @param displayName
	 *            the display name
	 * 
	 * @return the condition with the given display name for the specified
	 *         variable type
	 */
	public FlowVarCondition<?> getCondition(VariableType<?> varType,
			String displayName) {

		return typeLookup.getOrDefault(varType, Collections.emptySet()).stream()
				.filter(x -> x.getDisplayName().equals(displayName)).findFirst()
				.orElse(null);
	}

	/**
	 * @param uniqueID
	 *            the unique ID for the condition
	 * 
	 * @return the condition corresponding to the unique ID
	 */
	public FlowVarCondition<?> getCondition(String uniqueID) {
		return uniqueIDLookup.get(uniqueID);
	}

	/**
	 * @return the longest display name of the registered conditions
	 */
	public String getLongestDisplayName() {
		return longestDisplayName;
	}

}
