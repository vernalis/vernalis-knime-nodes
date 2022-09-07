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
package com.vernalis.knime.database.nodes.removesql;

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
import org.knime.core.node.port.database.DatabaseConnectionPortObject;
import org.knime.core.node.port.database.DatabaseConnectionPortObjectSpec;
import org.knime.core.node.port.database.DatabaseConnectionSettings;
import org.knime.core.node.port.database.DatabasePortObject;
import org.knime.core.node.port.database.DatabasePortObjectSpec;

/**
 * NodeModel implementation for the RemoveSQL node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 *
 * @since 07-Sep-2022
 */
public class RemoveSQLNodeModel extends NodeModel {

	/**
	 * Constructor
	 *
	 * @since 07-Sep-2022
	 */
	public RemoveSQLNodeModel() {
		super(new PortType[] { DatabasePortObject.TYPE },
				new PortType[] { DatabaseConnectionPortObject.TYPE });
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {
		DatabasePortObject inPort = (DatabasePortObject) inObjects[0];
		// Going via a DCS object means we lose the SQL embedded in the
		// DatabaseQueryConnectionSettings of the inport
		// We dont create a new connection
		DatabaseConnectionSettings dcs = new DatabaseConnectionSettings(
				inPort.getConnectionSettings(getCredentialsProvider()));
		DatabaseConnectionPortObject outPort = new DatabaseConnectionPortObject(
				new DatabaseConnectionPortObjectSpec(dcs));
		return new PortObject[] { outPort };
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		DatabasePortObjectSpec inSpec = (DatabasePortObjectSpec) inSpecs[0];
		DatabaseConnectionSettings dcs = new DatabaseConnectionSettings(
				inSpec.getConnectionSettings(getCredentialsProvider()));
		DatabaseConnectionPortObjectSpec outSpec =
				new DatabaseConnectionPortObjectSpec(dcs);
		return new PortObjectSpec[] { outSpec };
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
	protected void saveSettingsTo(NodeSettingsWO settings) {

	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {

	}

	@Override
	protected void reset() {

	}

}
