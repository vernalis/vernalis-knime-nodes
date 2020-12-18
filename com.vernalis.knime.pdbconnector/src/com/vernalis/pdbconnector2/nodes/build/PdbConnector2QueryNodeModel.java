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
package com.vernalis.pdbconnector2.nodes.build;

import java.io.File;
import java.io.IOException;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import com.vernalis.pdbconnector2.ports.MultiRCSBQueryModel;
import com.vernalis.pdbconnector2.ports.RCSBQueryPortObject;
import com.vernalis.pdbconnector2.query.RCSBQueryModel;

/**
 * {@link NodeModel} implementation for the PDB Connector Advanced Query Builder
 * node
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class PdbConnector2QueryNodeModel extends NodeModel {

	private final RCSBQueryModel model = new RCSBQueryModel();

	/**
	 * Constructor for the node model
	 */
	protected PdbConnector2QueryNodeModel() {
		super(new PortType[] {}, new PortType[] { RCSBQueryPortObject.TYPE });
	}

	@Override
	protected PortObject[] execute(PortObject[] inData, ExecutionContext exec)
			throws Exception {
		return new PortObject[] { new RCSBQueryPortObject(model) };
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		if (!model.hasQuery()) {
			throw new InvalidSettingsException("No query specified");
		}
		final MultiRCSBQueryModel retVal = new MultiRCSBQueryModel();
		retVal.addModel(model);
		return new PortObjectSpec[] { retVal };
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		model.saveSettingsTo(settings);

	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		model.validateSettings(settings);

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		model.loadSettings(settings);

	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}

}
