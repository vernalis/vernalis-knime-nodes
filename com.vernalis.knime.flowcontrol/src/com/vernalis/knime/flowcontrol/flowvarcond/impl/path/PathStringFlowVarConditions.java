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
package com.vernalis.knime.flowcontrol.flowvarcond.impl.path;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.swing.JTextField;

import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.VariableType;
import org.knime.filehandling.core.connections.FSLocation;
import org.knime.filehandling.core.connections.FSPath;
import org.knime.filehandling.core.connections.location.FSPathProvider;
import org.knime.filehandling.core.connections.location.FSPathProviderFactory;
import org.knime.filehandling.core.data.location.variable.FSLocationVariableType;

import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapper;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapperCheckbox;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapperStringEntry;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.SettingsModelFlowVarCondition;

/**
 * An enum of Path variable conditions based on String comparisons on the
 * filename (the last component of the path)
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public enum PathStringFlowVarConditions
		implements FlowVarCondition<FSLocation> {

	FNameEndsWith("Filename ends with",
			"Whether the filename (the last component of the path) ends with the given string") {

		@Override
		protected boolean doComparison(String fName, String reference) {
			return fName.endsWith(reference);
		}

	},

	FNameStartsWith("Filename starts with",
			"Whether the filename (the last component of the path) starts with the given string") {

		@Override
		protected boolean doComparison(String fName, String reference) {
			return fName.startsWith(reference);
		}

	},

	FNameEqual("Filename =",
			"Whether the filename (the last component of the path) is equal to the given string") {

		@Override
		protected boolean doComparison(String fName, String reference) {
			return fName.equals(reference);
		}

	},

	FNameContains("Filename contains",
			"Whether the filename (the last component of the path) contains with the given string") {

		@Override
		protected boolean doComparison(String fName, String reference) {
			return fName.contains(reference);
		}

	};

	private final String description;
	private final String displayName;

	private PathStringFlowVarConditions(String displayName,
			String description) {
		this.displayName = displayName;
		this.description = description;
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
	public VariableType<FSLocation> getApplicableVariableType() {
		return FSLocationVariableType.INSTANCE;
	}

	@Override
	public boolean compare(FlowVariable variable,
			SettingsModelFlowVarCondition model) {
		checkType(variable);
		boolean retVal;
		FSLocation loc = variable.getValue(getApplicableVariableType());

		try (final FSPathProvider pathProvider = FSPathProviderFactory
				.newFactory(Optional.empty(), loc).create(loc)) {
			final FSPath fsPath = pathProvider.getPath();
			String fName = fsPath.getFileName().toString();
			boolean ignoreCase = false;
			boolean trim = false;
			String reference = null;
			for (ComponentWrapper<?, ?, ?> comp : model.getComponents()) {
				if (comp instanceof ComponentWrapperCheckbox) {
					ComponentWrapperCheckbox cb =
							(ComponentWrapperCheckbox) comp;
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
			fName = preprocessValue(fName, ignoreCase, trim);
			reference = preprocessValue(reference, ignoreCase, trim);

			retVal = doComparison(fName, reference);
		} catch (final SecurityException | IOException e) {
			retVal = false;
		}
		if (model.isInverted()) {
			retVal = !retVal;
		}
		return retVal;
	}

	/**
	 * Actually do the comparison. The values compared will have been adjusted
	 * to account for the case sensitivity and leading/trailing whitespace
	 * 
	 * @param fName
	 *            the file name
	 * @param reference
	 *            the reference calue
	 * 
	 * @return the result of the comparison
	 */
	protected abstract boolean doComparison(String fName, String reference);

	private String preprocessValue(String referenceValue, boolean ignoreCase,
			boolean trim) {
		String retVal =
				ignoreCase ? referenceValue.toLowerCase() : referenceValue;
		retVal = trim ? retVal.trim() : retVal;
		return retVal;
	}

	@Override
	public String getDescription() {
		return description;
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
		return Arrays.asList(
				new ComponentWrapperStringEntry(new JTextField(20),
						"Reference Value"),
				new ComponentWrapperCheckbox("Ignore Case", true),
				new ComponentWrapperCheckbox(
						"Ignore leading / trailing whitespace", true));

	}
}
