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
package com.vernalis.knime.flowvar.nodes.io.read;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeCreationContext;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.FlowVariable.Type;
import org.knime.core.util.pathresolve.ResolverUtil;

import com.vernalis.io.FileEncodingWithGuess;
import com.vernalis.io.FileHelpers;

import static com.vernalis.knime.flowvar.nodes.io.read.ReadVariablesNodeDialog.createDuplicateModel;
import static com.vernalis.knime.flowvar.nodes.io.read.ReadVariablesNodeDialog.createFilenameModel;

/**
 * This is the NodeModel implementation of the Read Variables NodeModel.
 * 
 * 
 * @author
 */
public class ReadVariablesNodeModel extends NodeModel {

	/** The NodeLogger Instance. */
	private static final NodeLogger logger =
			NodeLogger.getLogger(ReadVariablesNodeModel.class);

	/** The Create duplicates policy model. */
	private final SettingsModelString m_duplicatePolicy =
			createDuplicateModel();

	/** The filename model */
	private final SettingsModelString m_filename = createFilenameModel();

	/**
	 * Constructor for the node model.
	 */
	protected ReadVariablesNodeModel() {

		super(new PortType[] { FlowVariablePortObject.TYPE_OPTIONAL },
				new PortType[] { FlowVariablePortObject.TYPE });
	}

	/**
	 * Constructor called by drag-and-drop of .variables file
	 * 
	 * @param filename
	 *            the filename
	 */
	protected ReadVariablesNodeModel(final String filename) {
		this();
		if (filename.endsWith(".variables")) {
			m_filename.setStringValue(filename);
		}
	}

	/**
	 * Constructor called by drag-and-drop. Sets the {@link #m_filename}
	 * settings model to point to the dropped file.
	 * 
	 * @param droppedFile
	 *            the dropped file
	 */
	protected ReadVariablesNodeModel(final NodeCreationContext droppedFile) {
		this();
		try {
			m_filename.setStringValue(
					(new File(droppedFile.getUrl().toURI()).getPath()));
		} catch (URISyntaxException e) {
			logger.error("Variables File reader: " + e.getMessage());
			logger.error("Variables File reader: Location not set");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData,
			final ExecutionContext exec) throws Exception {
		// Get the existing variable names
		// We have to do this in 2 steps (instantiate, addall) as
		// getAvailableInputFlowVariables() returns an unmodifiable Map
		Set<String> inputVars = new HashSet<>();
		inputVars.addAll(getAvailableInputFlowVariables().keySet());

		// Get the Duplicate policy
		DuplicateVariablePolicy varPol = DuplicateVariablePolicy
				.valueOf(m_duplicatePolicy.getStringValue());

		// Read the file
		List<String> fvXML = readFile(m_filename.getStringValue());

		// Now parse each one by turn
		for (String fvar : fvXML) {
			// (?s) at start of regex means DOTALL mode (Pattern.DOTALL) - which
			// means .* includes newlines
			String fvName =
					fvar.replaceAll("(?s).*?name=\\\"(.*?)\\\".*", "$1");

			if (!fvName
					.startsWith(FlowVariable.Scope.Global.getPrefix() + ".")) {
				// Skip global and reserved variables

				FlowVariable.Type fvType = FlowVariable.Type.valueOf(
						fvar.replaceAll("(?s).*?type=\\\"(.*?)\\\".*", "$1"));
				String fvVal = fvar.replaceAll(
						"(?s).*?<flowvar(.*?(name|type)=\\\".*?\\\"){2}>(.*?)</flowvar>.*",
						"$3");

				switch (varPol) {
				case OVERWRITE:
					// Just write the value
					writeVariable(fvName, fvType, fvVal);
					break;
				case IGNORE:
					// Only write the value if it doesnt exist
					if (inputVars.add(fvName)) {
						writeVariable(fvName, fvType, fvVal);
					}
					break;
				case RENAME:
					writeVariable(getUniqueVarName(fvName, inputVars), fvType,
							fvVal);
					break;
				case RENAME_DIFFERENT:
					if (inputVars.add(fvName)) {
						// Different name - just write it!
						writeVariable(fvName, fvType, fvVal);
					} else {
						// Name exists - uniquify only if
						// * Different value
						// * Different type
						FlowVariable oldVar =
								getAvailableInputFlowVariables().get(fvName);
						String oldVal = oldVar.getValueAsString();
						FlowVariable.Type oldType = oldVar.getType();
						if (!oldVal.replace("\r", "")
								.equals(fvVal.replace("\r", ""))
								|| oldType != fvType) {
							writeVariable(getUniqueVarName(fvName, inputVars),
									fvType, fvVal);
						}
					}
					break;
				}
			}
		}
		return new PortObject[] { FlowVariablePortObject.INSTANCE };
	}

	/**
	 * Gets the unique var name.
	 * 
	 * @param fvName
	 *            the fv name
	 * @param inputVars
	 *            the input vars
	 * @return the unique var name
	 */
	private String getUniqueVarName(String fvName, Set<String> inputVars) {
		String newName = fvName;
		int i = 0;
		while (!inputVars.add(newName)) {
			newName = fvName + "(#" + (i++) + ")";
		}
		return newName;
	}

	/**
	 * Write variable.
	 * 
	 * @param fvName
	 *            the fv name
	 * @param fvType
	 *            the fv type
	 * @param fvVal
	 *            the fv val
	 */
	private void writeVariable(String fvName, Type fvType, String fvVal) {
		assert fvType != Type.CREDENTIALS : "Flow variable type should noy be Credentials";

		switch (fvType) {
		case INTEGER:
			try {
				pushFlowVariableInt(fvName, Integer.parseInt(fvVal));
			} catch (Exception e) {
				logger.warn("Unable to parse value (" + fvVal + ") of variable "
						+ fvName + " as Integer");
			}
			break;
		case DOUBLE:
			try {
				pushFlowVariableDouble(fvName, Double.parseDouble(fvVal));
			} catch (Exception e) {
				logger.warn("Unable to parse value (" + fvVal + ") of variable "
						+ fvName + " as Double");
			}
			break;
		case STRING:
			pushFlowVariableString(fvName, fvVal);
			break;
		default:
			setWarningMessage("Flow variable of type " + fvType.toString()
					+ " found in file - unable to restore to stack");
		}

	}

	/**
	 * Read file.
	 * 
	 * @param fname
	 *            the fname
	 * @return the array list
	 */
	private List<String> readFile(String fname) {

		ArrayList<String> retVal = new ArrayList<>();

		try {
			// Form a URL connection

			// Now set up a buffered reader to read it
			BufferedReader in = FileHelpers.getReaderFromUrl(
					FileHelpers.forceURL(fname), FileEncodingWithGuess.GUESS);
			StringBuilder output = new StringBuilder();
			String str;
			// Read the first line - which should be "<flowvariables>"
			str = in.readLine();
			if (!str.equals("<flowvariables>")) {
				logger.error("Invalid file");
				in.close();
				throw new InvalidSettingsException("Invalid file");
			}
			while ((str = in.readLine()) != null) {
				if (str.indexOf("<flowvar ") >= 0) {
					// start a new record
					output = new StringBuilder(str);
				} else {
					output.append(str);
				}

				if (output.toString().endsWith("</flowvariables>")) {
					// End of file
					break;
				}
				// Now check we don't have an end marker
				if (output.toString().indexOf("</flowvar>") >= 0) {
					// If we do, then we add it to the list of variables
					retVal.add(output.toString());
					// And clear the StringBuilder...
					output = new StringBuilder();
				} else {
					// Otherwise we have a multiline string, so add a linebreak
					output.append("\n");
				}

			}
			in.close();

			// Return the result as a string
			return retVal;
		} catch (Exception e) {
			// e.printStackTrace();
			return null;
		}
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

		canReadFile(m_filename.getStringValue());

		return new PortObjectSpec[] { FlowVariablePortObjectSpec.INSTANCE };
	}

	/**
	 * Checks if the file can be read.
	 * 
	 * @param fname
	 *            the path to the file to be checked
	 * @throws InvalidSettingsException
	 */
	private void canReadFile(String fname) throws InvalidSettingsException {
		// Correct extension?
		if (!fname.endsWith(".variables")) {
			setWarningMessage("Filename must have 'variables' extension");
			throw new InvalidSettingsException(
					"Filename must have 'variables' extension");
		}
		File f;
		if (fname.startsWith("file:")) {
			try {
				f = new File(new URI(fname));
			} catch (URISyntaxException e) {
				setWarningMessage(
						"File path looks like URL but unable to create URI");
				throw new InvalidSettingsException(
						"File path looks like URL but unable to create URI");
			}
		} else if (fname.startsWith("knime:")) {
			try {
				f = ResolverUtil.resolveURItoLocalFile(new URI(fname));
			} catch (Exception e) {
				setWarningMessage(
						"Unable to resolve knime: protocol URI: " + fname);
				throw new InvalidSettingsException(
						"Unable to resolve knime: protocol URI: " + fname);
			}
		} else {
			f = new File(fname);
		}

		// Does it exist?
		if (!f.exists()) {
			setWarningMessage("File does not exist!");
			throw new InvalidSettingsException("File does not exist!");
		}
		// Is it a folder?
		if (f.isDirectory()) {
			setWarningMessage("Specified path is a directory");
			throw new InvalidSettingsException("Specified path is a directory");
		}
		if (!f.canRead()) {
			setWarningMessage("File cannot be read!");
			throw new InvalidSettingsException("File cannot be read!");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_filename.saveSettingsTo(settings);
		m_duplicatePolicy.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_filename.loadSettingsFrom(settings);
		m_duplicatePolicy.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_filename.validateSettings(settings);
		m_duplicatePolicy.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

}
