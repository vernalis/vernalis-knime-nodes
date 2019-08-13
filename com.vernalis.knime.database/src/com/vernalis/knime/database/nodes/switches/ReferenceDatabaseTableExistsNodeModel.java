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
package com.vernalis.knime.database.nodes.switches;

import java.io.File;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.database.DatabaseConnectionPortObject;
import org.knime.core.node.port.database.DatabaseConnectionPortObjectSpec;
import org.knime.core.node.port.database.DatabaseConnectionSettings;
import org.knime.core.node.port.database.DatabasePortObjectSpec;
import org.knime.core.node.port.database.DatabaseQueryConnectionSettings;
import org.knime.core.node.port.database.reader.DBReader;
import org.knime.core.node.port.inactive.InactiveBranchPortObject;
import org.knime.core.node.port.inactive.InactiveBranchPortObjectSpec;

import static com.vernalis.knime.database.nodes.switches.DatabaseTableExistsNodeDialog.createCheckAtConfigureModel;
import static com.vernalis.knime.database.nodes.switches.DatabaseTableExistsNodeDialog.createIgnoreCaseModel;
import static com.vernalis.knime.database.nodes.switches.DatabaseTableExistsNodeDialog.createQueryTableNameModel;
import static com.vernalis.knime.database.nodes.switches.DatabaseTableExistsNodeDialog.createTableTypesModel;

/**
 * {@link NodeModel} implementation for the Reference Database Table Exists node
 * 
 * @author s.roughley
 *
 */
public final class ReferenceDatabaseTableExistsNodeModel extends NodeModel {

	private final SettingsModelString queryTableNameMdl =
			createQueryTableNameModel();
	private final SettingsModelStringArray tableTypesMdl =
			createTableTypesModel();
	private final SettingsModelBoolean checkAtConfigMdl =
			createCheckAtConfigureModel();
	private final SettingsModelBoolean ignoreCaseMdl = createIgnoreCaseModel();

	public ReferenceDatabaseTableExistsNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE,
				DatabaseConnectionPortObject.TYPE },
				new PortType[] { BufferedDataTable.TYPE,
						BufferedDataTable.TYPE });

	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		if (!(inSpecs[1] instanceof DatabaseConnectionPortObjectSpec)) {
			throw new InvalidSettingsException(
					"Incoming port is not a Database Connection Port");
		}
		if (inSpecs[1] instanceof DatabasePortObjectSpec) {
			throw new InvalidSettingsException(
					"Incompatible port spec, expected: DatabasePortObjectSpec, "
							+ "actual: DatabaseConnectionPortObjectSpec");
		}

		PortObjectSpec[] outPorts = new PortObjectSpec[2];
		Arrays.fill(outPorts, inSpecs[0]);
		if (checkAtConfigMdl.getBooleanValue()) {
			// Need to set an inactive branch
			DatabaseConnectionSettings connSettings =
					((DatabaseConnectionPortObjectSpec) inSpecs[1])
							.getConnectionSettings(getCredentialsProvider());
			outPorts[getInactiveBranchId(connSettings)] =
					InactiveBranchPortObjectSpec.INSTANCE;
		}

		return outPorts;
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {
		PortObject[] outPorts = new PortObject[2];
		Arrays.fill(outPorts, inObjects[0]);
		DatabaseConnectionSettings connSettings =
				((DatabaseConnectionPortObject) inObjects[1])
						.getConnectionSettings(getCredentialsProvider());
		outPorts[getInactiveBranchId(connSettings)] =
				InactiveBranchPortObject.INSTANCE;
		return outPorts;
	}

	/**
	 * @param connSettings
	 *            The Database Connection settings
	 * @return The index of the inactive branch
	 */
	private final int getInactiveBranchId(
			DatabaseConnectionSettings connSettings) {
		return checkDbContainsTable(connSettings) ? 1 : 0;
	}

	/**
	 * Perform a check the database table exists
	 * 
	 * @param dcs
	 *            The Database Connection settings
	 * @return {@code true} if a matching table was found
	 */
	private final boolean checkDbContainsTable(DatabaseConnectionSettings dcs) {

		final DBReader dbReader = dcs.getUtility()
				.getReader(new DatabaseQueryConnectionSettings(dcs, ""));

		if (ignoreCaseMdl.getBooleanValue()) {
			// We have to do this the hard way, and get and check all names
			try {
				DatabaseMetaData dbMetaData =
						dbReader.getDatabaseMetaData(getCredentialsProvider());
				ResultSet rs = dbMetaData.getTables(null, null, "%",
						tableTypesMdl.getStringArrayValue());
				while (rs.next()) {
					String dbTableName =
							(queryTableNameMdl.getStringValue().contains(".")
									? (rs.getString("TABLE_SCHEM") + ".") : "")
									+ rs.getString("TABLE_NAME");
					if (dbTableName.equalsIgnoreCase(
							queryTableNameMdl.getStringValue())) {
						rs.close();
						return true;
					}
				}
				return false;
			} catch (SQLException e) {
				// SQL problem - strange but conceivably possible if the driver
				// is
				// not correctly functioning
				getLogger().warn("Unable to load "
						+ Arrays.deepToString(
								tableTypesMdl.getStringArrayValue())
						+ " names");
				return false;
			} catch (NullPointerException e) {
				// Didnt find a setting - happens on first dialog class
				// instantiaion
				// as loadAdditionalSettings is not called until afetr initial
				// call,
				// but we need something in the dropdown list. Hopefully, this
				// is
				// never actually shown...
				return false;
			}

		} else {
			try {
				DatabaseMetaData dbMetaData =
						dbReader.getDatabaseMetaData(getCredentialsProvider());
				String schemaPattern =
						queryTableNameMdl.getStringValue().contains(".")
								? queryTableNameMdl.getStringValue()
										.split("\\.")[0]
								: null;
				String tableNamePattern =
						queryTableNameMdl.getStringValue().contains(".")
								? queryTableNameMdl.getStringValue()
										.split("\\.")[1]
								: queryTableNameMdl.getStringValue();
				ResultSet rs = dbMetaData.getTables(null, schemaPattern,
						tableNamePattern, tableTypesMdl.getStringArrayValue());
				// See if we have anything
				final boolean retVal = rs.next();
				rs.close();
				return retVal;
			} catch (SQLException e) {
				// SQL problem - strange but conceivably possible if the driver
				// is
				// not correctly functioning
				getLogger().warn("Unable to load "
						+ Arrays.deepToString(
								tableTypesMdl.getStringArrayValue())
						+ " names");
				return false;
			} catch (NullPointerException e) {
				// Didnt find a setting - happens on first dialog class
				// instantiaion
				// as loadAdditionalSettings is not called until afetr initial
				// call,
				// but we need something in the dropdown list. Hopefully, this
				// is
				// never actually shown...
				return false;
			}
		}

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
