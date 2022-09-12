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
package com.vernalis.knime.db.nodes.tovar;

import java.util.Optional;

import org.knime.base.node.io.database.DBNodeModel;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;
import org.knime.database.SQLQuery;
import org.knime.database.connection.UrlDBConnectionController;
import org.knime.database.connection.UserDBConnectionController;
import org.knime.database.port.DBDataPortObjectSpec;
import org.knime.database.port.DBPortObject;
import org.knime.database.session.DBSessionInformation;

/**
 * NodeModel for the DB To Variable configurable node and deprecated-from-new
 * Database to DB node migration-only DB Data To Variable node
 * 
 * @author S Roughley
 *
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public class DBToVariableNodeModel extends DBNodeModel {

	/**
	 * Constructor for direct specification of port types
	 * 
	 * @param inPorts
	 *            the incoming ports
	 * @param outPorts
	 *            the outgoing ports
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	DBToVariableNodeModel(PortType[] inPorts, PortType[] outPorts) {
		super(inPorts, outPorts);
	}

	/**
	 * Constructor for configurable nodes
	 * 
	 * @param creationConfig
	 *            the {@link NodeCreationConfiguration} containing the
	 *            configurable ports info
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	DBToVariableNodeModel(NodeCreationConfiguration creationConfig) {
		this(creationConfig.getPortConfig().orElseThrow().getInputPorts(),
				creationConfig.getPortConfig().orElseThrow().getOutputPorts());

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
		// To do the work of the node, we just call the configure method
		configure(new PortObjectSpec[] {
				inObjects[0] == null ? null : inObjects[0].getSpec() });
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
		// All new 'DB' ports implement the DBPortObject interface
		if (!(inSpecs[0] instanceof DBPortObject)) {
			throw new InvalidSettingsException(
					"Expected an incoming DB connection!");
		}
		DBPortObject dbSpec = (DBPortObject) inSpecs[0];
		DBSessionInformation sessionInfo =
				dbSpec.getDBSession().getSessionInformation();
		// See if we have a query embedded in the input port
		SQLQuery query = dbSpec instanceof DBDataPortObjectSpec
				? ((DBDataPortObjectSpec) dbSpec).getData().getQuery()
				: null;

		writeSettingsToFlowVars(sessionInfo, Optional.ofNullable(query));
		return new PortObjectSpec[] { FlowVariablePortObjectSpec.INSTANCE };
	}

	/**
	 * @param sessionInfo
	 * @param query
	 *            TODO
	 */
	private final void writeSettingsToFlowVars(DBSessionInformation sessionInfo,
			Optional<SQLQuery> query) {
		pushFlowVariableString("Database Driver",
				sessionInfo.getDriverDefinition().getName());
		if (sessionInfo
				.getConnectionController() instanceof UrlDBConnectionController) {
			pushFlowVariableString("Database URL",
					((UrlDBConnectionController) sessionInfo
							.getConnectionController()).getJdbcUrl());
		}
		pushFlowVariableString("Database Type",
				sessionInfo.getDBType().getName());
		if (sessionInfo
				.getConnectionController() instanceof UserDBConnectionController) {
			pushFlowVariableString("Database Username",
					((UserDBConnectionController) sessionInfo
							.getConnectionController()).getUser());
		}
		if (query.isPresent()) {
			pushFlowVariableString("Database Query SQL", query.get().getSQL());
		}
	}

}
