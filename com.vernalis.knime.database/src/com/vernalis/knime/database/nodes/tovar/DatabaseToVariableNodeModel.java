/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.knime.database.nodes.tovar;

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
import org.knime.core.node.port.database.DatabaseQueryConnectionSettings;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;

public class DatabaseToVariableNodeModel extends NodeModel {

	protected DatabaseToVariableNodeModel() {
		super(new PortType[] { DatabaseConnectionPortObject.TYPE },
				new PortType[] { FlowVariablePortObject.TYPE });

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
		DatabaseConnectionPortObjectSpec dbSpec =
				(DatabaseConnectionPortObjectSpec) inObjects[0].getSpec();
		DatabaseConnectionSettings dbConn =
				dbSpec.getConnectionSettings(getCredentialsProvider());
		writeSettingsToFlowVars(dbConn);
		return new PortObject[] { FlowVariablePortObject.INSTANCE };
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
		DatabaseConnectionPortObjectSpec dbSpec =
				(DatabaseConnectionPortObjectSpec) inSpecs[0];
		DatabaseConnectionSettings dbConn =
				dbSpec.getConnectionSettings(getCredentialsProvider());
		writeSettingsToFlowVars(dbConn);
		return new PortObjectSpec[] { FlowVariablePortObjectSpec.INSTANCE };
	}

	/**
	 * @param dbConn
	 */
	private final void writeSettingsToFlowVars(
			DatabaseConnectionSettings dbConn) {
		pushFlowVariableString("Database Driver", dbConn.getDriver());
		pushFlowVariableString("Database URL", dbConn.getJDBCUrl());
		pushFlowVariableString("Database Type", dbConn.getDatabaseIdentifier());
		if (dbConn instanceof DatabaseQueryConnectionSettings) {
			pushFlowVariableString("Database Query SQL",
					((DatabaseQueryConnectionSettings) dbConn).getQuery());
		}
		pushFlowVariableString("Database Username",
				dbConn.getUserName(getCredentialsProvider()));
	}

}
