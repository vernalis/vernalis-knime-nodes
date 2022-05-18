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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.swing.JTextField;

import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.VariableType;
import org.knime.filehandling.core.connections.FSCategory;
import org.knime.filehandling.core.connections.FSLocation;
import org.knime.filehandling.core.connections.FSPath;
import org.knime.filehandling.core.connections.location.FSPathProvider;
import org.knime.filehandling.core.connections.location.FSPathProviderFactory;
import org.knime.filehandling.core.data.location.variable.FSLocationVariableType;

import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapper;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapperDropdown;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapperStringEntry;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.SettingsModelFlowVarCondition;

/**
 * An enum if flow variable conditions for Path variables
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public enum PathFlowVarConditions implements FlowVarCondition<FSLocation> {

	FileSystemIs("File System Equals",
			"Whether the path of the file system is as indicated") {

		@Override
		protected boolean doComparison(FSLocation loc,
				SettingsModelFlowVarCondition model) {
			return loc.getFSCategory().getLabel()
					.equals(model.getComponents().get(0).getValue());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition#
		 * getReferenceComponents()
		 */
		@Override
		public List<ComponentWrapper<?, ?, ?>> getReferenceComponents() {
			return Collections.singletonList(new ComponentWrapperDropdown("FS",
					false, Arrays.stream(FSCategory.values())
							.map(c -> c.getLabel()).toArray(String[]::new)));
		}

	},

	StartsWith("Starts with",
			"Whether the first component of the path is the value shown, "
					+ "e.g. foo/bar does start with foo or foo/bar, "
					+ "but does not start with f, fo etc.") {

		@Override
		protected boolean doComparison(FSLocation loc,
				SettingsModelFlowVarCondition model) {
			boolean retVal;
			// The following is lifted from the String To Path node
			try (final FSPathProvider pathProvider = FSPathProviderFactory
					.newFactory(Optional.empty(), loc).create(loc)) {
				final FSPath fsPath = pathProvider.getPath();
				String start = (String) model.getComponents().get(0).getValue();
				retVal = fsPath.startsWith(start);
			} catch (final SecurityException | IOException e) {
				retVal = false;
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
			return Collections.singletonList(new ComponentWrapperStringEntry(
					new JTextField(20), "Reference Value", false));
		}

	},

	EndsWith("Ends with",
			"Whether the last component of the path is the value shown, "
					+ "e.g. foo/bar does end with bar or foo/bar, "
					+ "but does not end with r, ar etc.") {

		@Override
		protected boolean doComparison(FSLocation loc,
				SettingsModelFlowVarCondition model) {
			boolean retVal;
			// The following is lifted from the String To Path node
			try (final FSPathProvider pathProvider = FSPathProviderFactory
					.newFactory(Optional.empty(), loc).create(loc)) {
				final FSPath fsPath = pathProvider.getPath();
				String start = (String) model.getComponents().get(0).getValue();
				retVal = fsPath.endsWith(start);
			} catch (final SecurityException | IOException e) {
				retVal = false;
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
			return Collections.singletonList(new ComponentWrapperStringEntry(
					new JTextField(20), "Reference Value", false));
		}

	};

	private final String description;
	private final String displayName;

	private PathFlowVarConditions(String displayName, String description) {
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

		retVal = doComparison(loc, model);
		if (model.isInverted()) {
			retVal = !retVal;
		}
		return retVal;
	}

	/**
	 * @param loc
	 *            the location
	 * @param model
	 *            the settings model
	 * 
	 * @return the result of the comparison
	 */
	protected abstract boolean doComparison(FSLocation loc,
			SettingsModelFlowVarCondition model);

	@Override
	public String getDescription() {
		return description;
	}

}
