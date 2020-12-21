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
package com.vernalis.pdbconnector2.nodes.load;

import java.io.File;
import java.io.FileInputStream;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import com.vernalis.io.FileHelpers;
import com.vernalis.knime.nodes.SettingsModelRegistry;
import com.vernalis.pdbconnector2.ports.MultiRCSBQueryModel;
import com.vernalis.pdbconnector2.ports.RCSBQueryPortObject;

import static com.vernalis.pdbconnector2.nodes.load.LoadQueryNodeDialog.createFileModel;
import static com.vernalis.pdbconnector2.nodes.save.SaveQueryNodeModel.CFGKEY_RCSB_PDB_ADVANCED_QUERY;
import static com.vernalis.pdbconnector2.nodes.save.SaveQueryNodeModel.CFGKEY_VERSION;

/**
 * {@link NodeModel} implementation for the PDB Connector Read Query node
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class LoadQueryNodeModel extends NodeModel
		implements SettingsModelRegistry {

	private final Set<SettingsModel> models = new LinkedHashSet<>();
	private final SettingsModelString filePathMdl =
			registerSettingsModel(createFileModel());

	/**
	 * Constructor
	 */
	public LoadQueryNodeModel() {
		super(new PortType[0], new PortType[] { RCSBQueryPortObject.TYPE });

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

		try {

			String filePath = filePathMdl.getStringValue();
			if (filePath == null || filePath.isEmpty()) {
				throw new InvalidSettingsException("No file specified");
			}
			filePath = FileHelpers.forceURL(filePath);
			File inputFile =
					filePath.startsWith("file:") ? new File(new URI(filePath))
							: new File(filePath);

			if (!inputFile.exists()) {
				throw new InvalidSettingsException("File '"
						+ inputFile.getCanonicalPath() + "' does not exist!");
			}
		} catch (IOException | URISyntaxException e) {
			throw new InvalidSettingsException(
					"Error parsing file path - " + e.getMessage(), e);
		}

		return new PortObjectSpec[] { null };
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

		// Sort out the input file location
		String filePath = FileHelpers.forceURL(filePathMdl.getStringValue());
		File inputFile =
				filePath.startsWith("file:") ? new File(new URI(filePath))
						: new File(filePath);
		if (!inputFile.exists()) {
			throw new InvalidSettingsException("File '"
					+ inputFile.getCanonicalPath() + "' does not exist!");
		}

		// Store the query in a NodeSettings object

		NodeSettingsRO settings =
				NodeSettings.loadFromXML(new FileInputStream(inputFile));
		if (!settings.getKey().equals(CFGKEY_RCSB_PDB_ADVANCED_QUERY)) {
			throw new IOException("File does not contain a saved query");
		}
		MultiRCSBQueryModel outputModel = new MultiRCSBQueryModel();
		switch (settings.getInt(CFGKEY_VERSION)) {
			case 1:
				outputModel.loadSettings(settings);
				break;

			default:
				throw new IOException(
						"The saved version (" + settings.getInt(CFGKEY_VERSION)
								+ ") is not supported");
		}

		return new PortObject[] { new RCSBQueryPortObject(outputModel) };
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
		//

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		//
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		//
	}

}
