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
import java.nio.file.Files;
import java.util.Optional;

import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.VariableType;
import org.knime.filehandling.core.connections.FSLocation;
import org.knime.filehandling.core.connections.FSPath;
import org.knime.filehandling.core.connections.location.FSPathProvider;
import org.knime.filehandling.core.connections.location.FSPathProviderFactory;
import org.knime.filehandling.core.data.location.variable.FSLocationVariableType;

import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.SettingsModelFlowVarCondition;

/**
 * An enum of Path variable conditions based on the {@link FSPath} value derived
 * from the variable
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public enum PathFSPathFlowVarConditions
		implements FlowVarCondition<FSLocation> {

	FileExists("Exists", "Returns true if the file referred to exists") {

		@Override
		protected boolean doTest(FSPath fsPath) {
			return Files.exists(fsPath);
		}

	},

	IsAbsolute("Is Absolute", "Whether the path is an absolute path") {

		@Override
		protected boolean doTest(FSPath fsPath) {
			return fsPath.isAbsolute();
		}

	},

	IsDirectory("Is Directory",
			"Whether the path exists and represents a directory") {

		@Override
		protected boolean doTest(FSPath fsPath) {
			return Files.isDirectory(fsPath);
		}

	},

	IsHidden("Is Hidden", "Whether the path exists and is hidden") {

		@Override
		protected boolean doTest(FSPath fsPath)
				throws IOException, SecurityException {

			return Files.isHidden(fsPath);
		}

	},

	IsRegularFile("Is Regular File",
			"Whether the path exists and represents a regular file") {

		@Override
		protected boolean doTest(FSPath fsPath) {
			return Files.isRegularFile(fsPath);
		}

	},

	IsSymbolicLink("Is Symbolic Link",
			"Whether the path exists and represents a symbolic link") {

		@Override
		protected boolean doTest(FSPath fsPath) {
			return Files.isSymbolicLink(fsPath);
		}

	},

	IsReadable("Is Readable",
			"Whether the path exists and is readable from the JVM") {

		@Override
		protected boolean doTest(FSPath fsPath) {
			return Files.isReadable(fsPath);
		}

	},

	IsWritable("Is Writable",
			"Whether the path exists and is writable from the JVM") {

		@Override
		protected boolean doTest(FSPath fsPath) {
			return Files.isWritable(fsPath);
		}

	},

	IsEmpty("Is Empty", "Whether the path exists and has a length of 0 bytes") {

		@Override
		protected boolean doTest(FSPath fsPath)
				throws IOException, SecurityException {
			return Files.size(fsPath) == 0;
		}

	},

	IsExecutable("Is Executable",
			"Whether the path exists and is executable from the JVM") {

		@Override
		protected boolean doTest(FSPath fsPath) {
			return Files.isExecutable(fsPath);
		}

	};

	private final String description;
	private final String displayName;

	private PathFSPathFlowVarConditions(String displayName,
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
			retVal = doTest(fsPath);
		} catch (final SecurityException | IOException e) {
			retVal = false;
		}
		if (model.isInverted()) {
			retVal = !retVal;
		}
		return retVal;
	}

	/**
	 * @param fsPath
	 *            the path to test
	 *
	 * @return the result of the test
	 * 
	 * @throws IOException
	 *             If there was an error accessing the file
	 * @throws SecurityException
	 *             if there was a security error accessing the file
	 */
	protected abstract boolean doTest(FSPath fsPath)
			throws IOException, SecurityException;

	@Override
	public String getDescription() {
		return description;
	}

}
