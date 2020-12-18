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
package com.vernalis.pdbconnector2.nodes.combine;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.context.ports.PortsConfiguration;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.nodes.SettingsModelRegistry;
import com.vernalis.pdbconnector2.ports.MultiRCSBQueryModel;
import com.vernalis.pdbconnector2.ports.RCSBQueryPortObject;
import com.vernalis.pdbconnector2.query.text.dialog.QueryGroupConjunction;

import static com.vernalis.pdbconnector2.nodes.combine.CombineQueriesNodeDialog.createConjunctionModel;

/**
 * {@link NodeModel} implementation for the PDB Connector Combine Queries node
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class CombineQueriesNodeModel extends NodeModel
		implements SettingsModelRegistry {

	private final Set<SettingsModel> models = new LinkedHashSet<>();
	private final SettingsModelString conjMdl =
			registerSettingsModel(createConjunctionModel());

	/**
	 * Default constructor, with 2 input ports
	 */
	protected CombineQueriesNodeModel() {
		this(2);
	}

	/**
	 * Constructor specifiying the number of input ports
	 * 
	 * @param numInports
	 *            The number of input ports
	 */
	public CombineQueriesNodeModel(int numInports) {
		super(createInports(numInports),
				new PortType[] { RCSBQueryPortObject.TYPE });
	}

	/**
	 * Constructor from a {@link PortsConfiguration} object
	 * 
	 * @param portConfig
	 *            the config object
	 */
	public CombineQueriesNodeModel(PortsConfiguration portConfig) {
		super(portConfig.getInputPorts(), portConfig.getOutputPorts());
	}

	private static PortType[] createInports(int numInports) {
		if (numInports < 2) {
			throw new IllegalArgumentException(
					"Node must have 2 or more ports!");
		}
		final PortType[] retVal =
				ArrayUtils.of(RCSBQueryPortObject.TYPE_OPTIONAL, numInports);
		retVal[0] = RCSBQueryPortObject.TYPE;
		return retVal;
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		final MultiRCSBQueryModel model = new MultiRCSBQueryModel();
		try {
			model.setConjunction(
					QueryGroupConjunction.fromText(conjMdl.getStringValue()));
		} catch (NullPointerException | IllegalArgumentException e) {
			throw new InvalidSettingsException("'" + conjMdl.getStringValue()
					+ "' is not a valid conjunction", e);
		}
		Arrays.stream(inSpecs).filter(x -> x instanceof MultiRCSBQueryModel)
				.map(x -> MultiRCSBQueryModel.class.cast(x))
				.filter(x -> x.hasQuery()).forEach(x -> model.addModel(x));
		if (!model.hasQuery()) {
			throw new InvalidSettingsException(
					"The incoming ports contain not query");
		}
		return new PortObjectSpec[] { model };
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {

		final MultiRCSBQueryModel model = new MultiRCSBQueryModel();
		model.setConjunction(
				QueryGroupConjunction.fromText(conjMdl.getStringValue()));
		Arrays.stream(inObjects).filter(x -> x != null)
				.map(x -> RCSBQueryPortObject.class.cast(x))
				.map(x -> x.getModel()).filter(x -> x.hasQuery())
				.forEach(x -> model.addModel(x));
		return new PortObject[] { new RCSBQueryPortObject(model) };
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

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

	}

	@Override
	public Set<SettingsModel> getModels() {
		return models;
	}

}
