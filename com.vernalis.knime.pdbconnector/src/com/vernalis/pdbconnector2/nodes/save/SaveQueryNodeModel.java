/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.nodes.save;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import com.vernalis.io.FileHelpers;
import com.vernalis.knime.nodes.SettingsModelRegistry;
import com.vernalis.pdbconnector2.ports.MultiRCSBQueryModel;
import com.vernalis.pdbconnector2.ports.RCSBQueryPortObject;

import static com.vernalis.pdbconnector2.nodes.save.SaveQueryNodeDialog.createFileModel;
import static com.vernalis.pdbconnector2.nodes.save.SaveQueryNodeDialog.createOverWriteExistingFileModel;

/**
 * {@link NodeModel} implementation for the 'Write Query' node
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class SaveQueryNodeModel extends NodeModel
		implements SettingsModelRegistry {

	/**
	 * The settings key for the advanced query
	 */
	public static final String CFGKEY_RCSB_PDB_ADVANCED_QUERY =
			"RCSB PDB Advanced Query";
	/**
	 * The settings key for the version setting
	 */
	public static final String CFGKEY_VERSION = "Version";
	private final Set<SettingsModel> models = new LinkedHashSet<>();
	private final SettingsModelString filePathMdl =
			registerSettingsModel(createFileModel());
	private final SettingsModelBoolean overwriteFileMdl =
			registerSettingsModel(createOverWriteExistingFileModel());

	/**
	 * The version of the query writing
	 */
	public static final int VERSION = 1;

	/**
	 * Constructor
	 */
	public SaveQueryNodeModel() {
		super(new PortType[] { RCSBQueryPortObject.TYPE }, new PortType[0]);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#configure(org.knime.core.node.port.
	 * PortObjectSpec[])
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		if (inSpecs[0] == null) {
			throw new InvalidSettingsException(
					"No Query port connected at input");
		}
		if (!((MultiRCSBQueryModel) inSpecs[0]).hasQuery()) {
			throw new InvalidSettingsException(
					"The incoming query connection has no query defined");
		}
		try {
			final String filePath =
					FileHelpers.forceURL(filePathMdl.getStringValue());
			File outputFile =
					filePath.startsWith("file:") ? new File(new URI(filePath))
							: new File(filePath);
			if (!overwriteFileMdl.getBooleanValue() && outputFile.exists()) {
				throw new InvalidSettingsException(
						"Unable to overwriteFileMdl existing output file");
			}
			outputFile = outputFile.getParentFile();
			if (!outputFile.exists()) {
				setWarningMessage(
						"Parent folder (" + outputFile.getCanonicalPath()
								+ ") does not exist and will be created");
			}
		} catch (IOException | URISyntaxException e) {
			throw new InvalidSettingsException(
					"Error parsing file path - " + e.getMessage(), e);
		}

		return new PortObjectSpec[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#execute(org.knime.core.node.port.PortObject
	 * [], org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {

		// Get and check the query
		MultiRCSBQueryModel querySpec =
				((RCSBQueryPortObject) inObjects[0]).getSpec();
		if (!querySpec.hasQuery()) {
			throw new InvalidSettingsException(
					"The incoming query connection has no query defined");
		}

		// Sort out the output location
		String filePath = FileHelpers.forceURL(filePathMdl.getStringValue());
		File outputFile =
				filePath.startsWith("file:") ? new File(new URI(filePath))
						: new File(filePath);
		if (!overwriteFileMdl.getBooleanValue() && outputFile.exists()) {
			throw new InvalidSettingsException(
					"Output file exists and cannot be overwritten");
		}
		if (!outputFile.getParentFile().exists()
				&& !outputFile.getParentFile().mkdirs()) {
			throw new IOException("Unable to create parent folder");
		}

		// Store the query in a NodeSettings object
		NodeSettings settings =
				new NodeSettings(CFGKEY_RCSB_PDB_ADVANCED_QUERY);
		settings.addInt(CFGKEY_VERSION, VERSION);
		querySpec.saveSettingsTo(settings);

		try (FileOutputStream fos = new FileOutputStream(outputFile)) {
			settings.saveToXML(fos);
		}

		return new PortObject[0];
	}

	@Override
	public Set<SettingsModel> getModels() {
		return models;
	}

	@Override
	public void saveSettingsTo(NodeSettingsWO settings) {
		SettingsModelRegistry.super.saveSettingsTo(settings);

	}

	@Override
	public void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		SettingsModelRegistry.super.validateSettings(settings);

	}

	@Override
	public void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		SettingsModelRegistry.super.loadValidatedSettingsFrom(settings);

	}

	@Override
	protected void reset() {
		// nothing to do

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// nothing to do
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// nothing to do
	}

}
