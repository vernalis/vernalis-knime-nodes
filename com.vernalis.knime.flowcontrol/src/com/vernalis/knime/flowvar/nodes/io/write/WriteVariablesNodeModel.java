/*******************************************************************************
 * Copyright (c) 2014, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.flowvar.nodes.io.write;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.workflow.FlowVariable;

import static com.vernalis.knime.flowvar.nodes.io.write.WriteVariablesNodeDialog.createFilenameModel;
import static com.vernalis.knime.flowvar.nodes.io.write.WriteVariablesNodeDialog.createOverwriteModel;

/**
 * This is the NodeModel implementation of WriteVariables.
 * 
 * 
 * @author S.Roughley <knime@vernalis.com>
 */
public class WriteVariablesNodeModel extends NodeModel {

	/** The logger instance */
	private static final NodeLogger logger = NodeLogger.getLogger(WriteVariablesNodeModel.class);

	/** The overwrite file model */
	private final SettingsModelBoolean m_overwrite = createOverwriteModel();

	/** The filename model */
	private final SettingsModelString m_filename = createFilenameModel();

	/**
	 * Constructor for the node model.
	 */
	public WriteVariablesNodeModel() {

		super(new PortType[] { FlowVariablePortObject.TYPE_OPTIONAL }, new PortType[] {});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec)
			throws Exception {

		// First list the flow variables
		Map<String, FlowVariable> flowVars = getAvailableFlowVariables();
		ArrayList<String> fvXML = new ArrayList<>();
		for (Entry<String, FlowVariable> ent : flowVars.entrySet()) {
			String fvName = ent.getKey();
			if (fvName.startsWith(FlowVariable.Scope.Global.getPrefix() + ".")) {
				// Protected system variable - skip
				continue;
			}
			String fvVal = ent.getValue().getValueAsString();
			String fvType = ent.getValue().getType().name();
			fvXML.add("<flowvar name=\"" + fvName + "\" type=\"" + fvType + "\">" + fvVal
					+ "</flowvar>");
		}
		// Now we need to write them to a file
		if (!writeToFile(fvXML)) {
			logger.error("IO Error - failed to write file");
			throw new Exception("IO Error - failed to write file");
		}
		return new PortObject[] {};
	}

	/**
	 * Write to file.
	 * 
	 * @param fvXML
	 *            The XML representation of the flow variables
	 * @return true, if successful
	 * @throws Exception
	 */
	private boolean writeToFile(ArrayList<String> fvXML) throws Exception {
		String fname = m_filename.getStringValue();
		canWriteToFile(fname);
		StringBuilder sb = new StringBuilder("<flowvariables>\n");
		for (String xml : fvXML) {
			sb.append(xml).append("\n");
		}
		sb.append("</flowvariables>");
		return saveStringToPath(sb.toString(), fname, m_overwrite.getBooleanValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		String fname = m_filename.getStringValue();
		canWriteToFile(fname);

		return new PortObjectSpec[] {};
	}

	/**
	 * Check if can write to file.
	 * 
	 * @param fname
	 *            the path to the file
	 * @throws InvalidSettingsException
	 */
	private void canWriteToFile(String fname) throws InvalidSettingsException {
		// Correct extension?
		if (!fname.endsWith(".variables")) {
			setWarningMessage("Filename must have 'variables' extension");
			throw new InvalidSettingsException("Filename must have 'variables' extension");
		}
		File f;
		if (fname.startsWith("file:")) {
			try {
				f = new File(new URI(fname));
			} catch (URISyntaxException e) {
				setWarningMessage("File path looks like URL but unable to create URI");
				throw new InvalidSettingsException(
						"File path looks like URL but unable to create URI");
			}
		} else {
			f = new File(fname);
		}
		// Does it exist?
		if (f.exists()) {
			if (!m_overwrite.getBooleanValue() || !f.canWrite()) {
				// File exists, and we may not overwrite it
				setWarningMessage("File exists, cannot overwrite");
				throw new InvalidSettingsException("File exists, cannot overwrite");
			} else {
				setWarningMessage("File exists and will be over-written");
			}
		}

		// Is it a folder?
		if (f.isDirectory()) {
			setWarningMessage("Specified path is a directory");
			throw new InvalidSettingsException("Specified path is a directory");
		}

		// Does it's container folder exist?
		File fParent = f.getParentFile();
		if (!fParent.exists()) {
			logger.info("Creating parent directory");
			// Try to create it if not
			if (!fParent.mkdirs()) {
				setWarningMessage("Tried to create parent directory - FAILED!");
				throw new InvalidSettingsException("Tried to create parent directory - FAILED!");
			}
		}

	}

	/**
	 * Save string to path.
	 * 
	 * @param text
	 *            the test to write to the path
	 * @param PathToFile
	 *            the path to the file
	 * @param Overwrite
	 *            <code>true</code> if the file should be over-written if it
	 *            exists
	 * @return true, if successful
	 * @throws Exception
	 */
	private static boolean saveStringToPath(String text, String PathToFile, Boolean Overwrite)
			throws Exception {
		/*
		 * Helper function to attempt to save out a string to a file identified
		 * by a second string. Returns true or false depending on whether the
		 * file was successfully written. Assumes that the container folder
		 * exists. If overwrite is false the file will not be overwritten
		 */
		File f;
		if (PathToFile.startsWith("file:")) {
			try {
				f = new File(new URI(PathToFile));
			} catch (URISyntaxException e) {
				throw new Exception("File path looks like URL but unable to create URI");
			}
		} else {
			f = new File(PathToFile);
		}
		Boolean r = false;
		if (Overwrite || !f.exists()) {
			try {
				BufferedWriter output = new BufferedWriter(new FileWriter(f));
				// FileWriter always assumes default encoding is OK!
				output.write(text);
				output.close();
				r = true;
			} catch (Exception e) {
				e.printStackTrace();
				r = false;
			}
		}
		return r;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_filename.saveSettingsTo(settings);
		m_overwrite.saveSettingsTo(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_filename.loadSettingsFrom(settings);
		m_overwrite.loadSettingsFrom(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		m_filename.validateSettings(settings);
		m_overwrite.validateSettings(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

}
