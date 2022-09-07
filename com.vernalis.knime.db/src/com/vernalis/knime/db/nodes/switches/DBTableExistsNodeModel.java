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
package com.vernalis.knime.db.nodes.switches;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.knime.base.node.io.database.DBNodeModel;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;
import org.knime.core.node.port.inactive.InactiveBranchPortObject;
import org.knime.core.node.port.inactive.InactiveBranchPortObjectSpec;
import org.knime.database.node.util.ConfigureExecutionMonitor;
import org.knime.database.node.util.PortObjectSpecHelper;
import org.knime.database.port.DBSessionPortObject;
import org.knime.database.port.DBSessionPortObjectSpec;
import org.knime.database.session.DBSession;

import com.vernalis.knime.misc.ArrayUtils;

import static com.vernalis.knime.db.nodes.switches.DBTableExistsNodeDialog.createCheckAtConfigureModel;
import static com.vernalis.knime.db.nodes.switches.DBTableExistsNodeDialog.createIgnoreCaseModel;
import static com.vernalis.knime.db.nodes.switches.DBTableExistsNodeDialog.createQueryTableNameModel;
import static com.vernalis.knime.db.nodes.switches.DBTableExistsNodeDialog.createTableTypesModel;

/**
 * {@link NodeModel} implementation for the Database Table Exists nodes
 * 
 * @author S Roughley
 */
public class DBTableExistsNodeModel extends DBNodeModel {

	private final SettingsModelString queryTableNameMdl =
			createQueryTableNameModel();
	private final SettingsModelStringArray tableTypesMdl =
			createTableTypesModel();
	private final SettingsModelBoolean checkAtConfigMdl =
			createCheckAtConfigureModel();
	private final SettingsModelBoolean ignoreCaseMdl = createIgnoreCaseModel();

	/**
	 * Constructor allowing port types specification
	 * 
	 * @param inPorts
	 *            the incoming port types
	 * @param outPorts
	 *            the outgoing port types
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	protected DBTableExistsNodeModel(PortType[] inPorts, PortType[] outPorts) {
		super(inPorts, outPorts);
	}

	/**
	 * Constructor for the single input default implementation
	 * 
	 * @param creationConfig
	 *            the {@link NodeCreationConfiguration}
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	DBTableExistsNodeModel(NodeCreationConfiguration creationConfig) {
		// 1 inputs
		// 2 outputs - either DBSession or Flow Variable
		this(creationConfig.getPortConfig().orElseThrow().getInputPorts(),
				creationConfig.getPortConfig().orElseThrow().getOutputPorts());
	}

	@Override
	protected final PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {

		checkTableNameSupplied();
		// Check incoming port type
		DBSessionPortObjectSpec sessionSpec = PortObjectSpecHelper
				.asDBSessionPortObjectSpec(inSpecs[getDBInputPort()], true);

		// Output ports - first spec is passed to both outputs
		PortObjectSpec[] outPorts = getOutputSpecs(inSpecs);

		if (checkAtConfigMdl.getBooleanValue()) {
			// Need to set an inactive branch
			try {
				final DBSession session = sessionSpec.getDBSession();
				outPorts[getInactiveBranchId(session,
						new ConfigureExecutionMonitor(session))] =
								InactiveBranchPortObjectSpec.INSTANCE;
			} catch (SQLException | CanceledExecutionException e) {
				throw new InvalidSettingsException("Unable to configure", e);
			}
		}

		return outPorts;
	}

	private final void checkTableNameSupplied()
			throws InvalidSettingsException {
		if (queryTableNameMdl.getStringValue() == null
				|| queryTableNameMdl.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("A table name must be supplied");
		}
	}

	/**
	 * Method to create the base output port specs called by the
	 * {@link #configure(PortObjectSpec[])} method. The calling method will
	 * check whether the active branch should be set during configure, and if
	 * required replace the inactive branch. The calling method will also check
	 * for the name of the table being present
	 * 
	 * @param inSpecs
	 *            the incoming port spec(s)
	 * 
	 * @return the outgoing port specs
	 * 
	 * @throws InvalidSettingsException
	 *             if there was a problem creating the output
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	protected PortObjectSpec[] getOutputSpecs(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		final PortObjectSpec[] retVal =
				ArrayUtils.fill(new PortObjectSpec[2], inSpecs[0]);
		for (int i = 0; i < getNrOutPorts(); i++) {
			if (getOutPortType(i).acceptsPortObjectClass(
					FlowVariablePortObject.TYPE.getPortObjectClass())) {
				retVal[i] = FlowVariablePortObjectSpec.INSTANCE;
			}
		}
		return retVal;
	}

	/**
	 * @return The index of the input port corresponding to the database
	 *         connection - always the last in put
	 */
	private int getDBInputPort() {
		return getNrInPorts() - 1;
	}

	/**
	 * @param session
	 *            the database session
	 * @param exec
	 *            An Execution monitor
	 * 
	 * @return {@code true} if the incoming database connection contains a table
	 *         in the relevant types
	 * 
	 * @throws SQLException
	 *             If there was an error getting the database metadata
	 * @throws CanceledExecutionException
	 *             If the user cancelled
	 * @throws InvalidSettingsException
	 *             if no value is supplied for the table name
	 */
	private boolean checkDbContainsTable(final DBSession session,
			ExecutionMonitor exec) throws SQLException,
			CanceledExecutionException, InvalidSettingsException {

		boolean isPresent = false;
		checkTableNameSupplied();
		String schemaPattern = ignoreCaseMdl.getBooleanValue() ? null
				: queryTableNameMdl.getStringValue().contains(".")
						? queryTableNameMdl.getStringValue().split("\\.")[0]
						: null;
		String tablePattern = ignoreCaseMdl.getBooleanValue() ? "%"
				: queryTableNameMdl.getStringValue().contains(".")
						? queryTableNameMdl.getStringValue().split("\\.")[1]
						: queryTableNameMdl.getStringValue();

		try (Connection connection =
				session.getConnectionProvider().getConnection(exec);
				ResultSet rs = connection.getMetaData().getTables(null,
						schemaPattern, tablePattern,
						tableTypesMdl.getStringArrayValue());
				// We dont use the statement but otherwise a warning is thrown
				// that it isnt closed
				Statement statement = rs.getStatement()) {

			if (ignoreCaseMdl.getBooleanValue()) {
				// And so the hard way...
				while (rs.next()) {
					exec.checkCanceled();
					String dbTableName =
							(queryTableNameMdl.getStringValue().contains(".")
									? (rs.getString("TABLE_SCHEM") + ".")
									: "") + rs.getString("TABLE_NAME");
					if (dbTableName.equalsIgnoreCase(
							queryTableNameMdl.getStringValue())) {
						isPresent = true;
						break;
					}
				}
			} else {
				// For exact match, we just check there is a row
				isPresent = rs.next();
			}
		}

		return isPresent;
	}

	/**
	 * @param session
	 *            The incoming Database Connection session
	 * 
	 * @return The inactive branch port Index
	 * 
	 * @throws SQLException
	 *             If there was an error getting the database metadata
	 * @throws CanceledExecutionException
	 *             If the user cancelled
	 * @throws InvalidSettingsException
	 *             if no value is supplied for the table name
	 */
	private final int getInactiveBranchId(final DBSession session,
			ExecutionMonitor exec) throws SQLException,
			CanceledExecutionException, InvalidSettingsException {
		return checkDbContainsTable(session, exec) ? 1 : 0;
	}

	@Override
	protected final PortObject[] execute(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {
		PortObject[] outPorts = getOutputPortObjects(inObjects, exec);

		DBSessionPortObject dbPortObj =
				(DBSessionPortObject) inObjects[getDBInputPort()];
		outPorts[getInactiveBranchId(dbPortObj.getDBSession(), exec)] =
				InactiveBranchPortObject.INSTANCE;
		return outPorts;
	}

	/**
	 * Method to create the base output ports. This is called from the
	 * {@link #execute(PortObject[], ExecutionContext)} method, and should
	 * create the base port objects. The calling method will then replace the
	 * appropriate value with an inactive branch
	 * 
	 * @param inObjects
	 *            in the incoming port object(s)
	 * @param exec
	 *            execution context in case it is needed during output creation
	 * 
	 * @return the outgoing port objects
	 * 
	 * @throws Exception
	 *             if there was an error during output creation
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	protected PortObject[] getOutputPortObjects(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {
		final PortObject[] retVal =
				ArrayUtils.fill(new PortObject[2], inObjects[0]);
		for (int i = 0; i < getNrOutPorts(); i++) {
			if (getOutPortType(i).acceptsPortObjectClass(
					FlowVariablePortObject.TYPE.getPortObjectClass())) {
				retVal[i] = FlowVariablePortObject.INSTANCE;
			}
		}
		return retVal;
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		queryTableNameMdl.saveSettingsTo(settings);
		tableTypesMdl.saveSettingsTo(settings);
		checkAtConfigMdl.saveSettingsTo(settings);
		ignoreCaseMdl.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		queryTableNameMdl.validateSettings(settings);
		tableTypesMdl.validateSettings(settings);
		checkAtConfigMdl.validateSettings(settings);
		ignoreCaseMdl.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		queryTableNameMdl.loadSettingsFrom(settings);
		tableTypesMdl.loadSettingsFrom(settings);
		checkAtConfigMdl.loadSettingsFrom(settings);
		ignoreCaseMdl.loadSettingsFrom(settings);
	}

	@Override
	protected void reset() {

	}

}
