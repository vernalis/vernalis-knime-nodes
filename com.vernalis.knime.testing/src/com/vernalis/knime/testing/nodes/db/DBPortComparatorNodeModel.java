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
package com.vernalis.knime.testing.nodes.db;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelAuthentication.AuthenticationType;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.database.connection.UrlDBConnectionController;
import org.knime.database.connection.UserDBConnectionController;
import org.knime.database.port.DBDataPortObject;
import org.knime.database.port.DBPortObject;
import org.knime.database.session.DBSessionInformation;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.nodes.SettingsModelRegistry;

import static com.vernalis.knime.testing.nodes.db.DBPortComparatorNodeDialog.createCompareDbtypeModel;
import static com.vernalis.knime.testing.nodes.db.DBPortComparatorNodeDialog.createCompareDriversModel;
import static com.vernalis.knime.testing.nodes.db.DBPortComparatorNodeDialog.createCompareSqlModel;
import static com.vernalis.knime.testing.nodes.db.DBPortComparatorNodeDialog.createCompareTableSpecsModel;
import static com.vernalis.knime.testing.nodes.db.DBPortComparatorNodeDialog.createCompareURLModel;
import static com.vernalis.knime.testing.nodes.db.DBPortComparatorNodeDialog.createCompareUsernameModel;
import static com.vernalis.knime.testing.nodes.db.DBPortComparatorNodeDialog.createIgnoreSqlCommentsModel;

/**
 * NodeModel for the Database Port Comparator node
 * 
 * @author S Roughley
 *
 */
public class DBPortComparatorNodeModel extends NodeModel
		implements SettingsModelRegistry {

	/**
	 * 
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	private static final String NEWLINE_SPACES = "\n   ";

	private final Set<SettingsModel> models = new LinkedHashSet<>();

	private final SettingsModelBoolean cpDriversMdl =
			registerSettingsModel(createCompareDriversModel());
	private final SettingsModelBoolean cpURLMdl =
			registerSettingsModel(createCompareURLModel());
	private final SettingsModelBoolean cpUNameMdl =
			registerSettingsModel(createCompareUsernameModel());
	private final SettingsModelBoolean cpDbTypeMdl =
			registerSettingsModel(createCompareDbtypeModel());
	private final SettingsModelBoolean cpSqlMdl;
	private final SettingsModelBoolean ignSqlCommentsMdl;
	private final SettingsModelBoolean cpTblSpecsMdl;

	private final boolean hasComparableSqlInput;
	private static final Pattern sqlCommentPattern =
			Pattern.compile("((?s)/\\*.*?\\*/(?-s)|--.*)");

	/**
	 * Constructor
	 * 
	 * @param creationConfig
	 *            the node creaction config with the port types
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	DBPortComparatorNodeModel(NodeCreationConfiguration creationConfig) {
		this(creationConfig.getPortConfig().orElseThrow().getInputPorts()[0]);
	}

	/**
	 * Constructor
	 * 
	 * @param portType
	 *            the type of the two input ports
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	DBPortComparatorNodeModel(PortType portType) {
		// The creationConfig only has 1 port type which we use twice
		super(ArrayUtils.fill(new PortType[2], portType), new PortType[0]);
		hasComparableSqlInput = getInPortType(0).getPortObjectClass()
				.equals(DBDataPortObject.class);
		if (hasComparableSqlInput) {
			cpSqlMdl = registerSettingsModel(createCompareSqlModel());
			cpSqlMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					updateEnableStatus();

				}
			});
			ignSqlCommentsMdl =
					registerSettingsModel(createIgnoreSqlCommentsModel());
			cpTblSpecsMdl =
					registerSettingsModel(createCompareTableSpecsModel());
			updateEnableStatus();
		} else {
			cpSqlMdl = null;
			ignSqlCommentsMdl = null;
			cpTblSpecsMdl = null;
		}
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		//
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

		// Somewhere to accumulate errors...
		StringBuilder err = new StringBuilder();

		// Now, regardless of type, do some comparisons...
		DBPortObject goldenPort = (DBPortObject) inObjects[1];
		DBPortObject testPort = (DBPortObject) inObjects[0];
		DBSessionInformation goldenInfo = goldenPort.getSessionInformation();
		DBSessionInformation testInfo = testPort.getSessionInformation();

		if (cpDriversMdl.getBooleanValue() && !goldenInfo.getDriverDefinition()
				.equals(testInfo.getDriverDefinition())) {
			appendError(err, "Drivers",
					goldenInfo.getDriverDefinition().getDescription(),
					testInfo.getDriverDefinition().getDescription());

		}

		if (cpURLMdl.getBooleanValue()) {
			String goldenUrl = goldenInfo
					.getConnectionController() instanceof UrlDBConnectionController
							? ((UrlDBConnectionController) goldenInfo
									.getConnectionController())
											.getConnectionJdbcUrl()
							: null;
			String testUrl = testInfo
					.getConnectionController() instanceof UrlDBConnectionController
							? ((UrlDBConnectionController) testInfo
									.getConnectionController())
											.getConnectionJdbcUrl()
							: null;
			if (!Objects.equals(goldenUrl, testUrl)) {
				appendError(err, "URLs", goldenUrl, testUrl);
			}
		}

		if (cpUNameMdl.getBooleanValue()) {
			UserDBConnectionController goldenConnCtrl = goldenInfo
					.getConnectionController() instanceof UserDBConnectionController
							? (UserDBConnectionController) goldenInfo
									.getConnectionController()
							: null;
			UserDBConnectionController testConnCtrl = testInfo
					.getConnectionController() instanceof UserDBConnectionController
							? (UserDBConnectionController) testInfo
									.getConnectionController()
							: null;
			AuthenticationType goldenAuthType = goldenConnCtrl == null ? null
					: goldenConnCtrl.getAuthenticationType();
			AuthenticationType testAuthType = testConnCtrl == null ? null
					: testConnCtrl.getAuthenticationType();
			if (!Objects.equals(goldenAuthType, testAuthType)) {
				appendError(err, "Authentication Type",
						goldenAuthType == null ? null
								: goldenAuthType.getText(),
						testAuthType == null ? null : testAuthType.getText());
			}
			if (goldenAuthType == AuthenticationType.USER
					|| goldenAuthType == AuthenticationType.USER_PWD
					|| goldenAuthType == AuthenticationType.CREDENTIALS) {
				String goldenUName = goldenConnCtrl.getUser();
				String testUName = testConnCtrl.getUser();
				if (!Objects.equals(testAuthType, testUName)) {
					appendError(err, "Usernames", goldenUName, testUName);
				}
			}
		}

		if (cpDbTypeMdl.getBooleanValue()) {
			final String goldenType = goldenInfo.getDBType().getName();
			final String testType = testInfo.getDBType().getName();
			if (!goldenType.equals(testType)) {
				appendError(err, "Database Types", goldenType, testType);
			}
		}

		if (hasComparableSqlInput) {
			// Both types are DCPO
			DBDataPortObject goldenQueryConn = (DBDataPortObject) goldenPort;
			DBDataPortObject testQueryConn = (DBDataPortObject) testPort;

			if (cpSqlMdl.getBooleanValue()) {
				String goldenSQL =
						goldenQueryConn.getData().getQuery().getSQL();
				String testSQL = testQueryConn.getData().getQuery().getSQL();
				if (ignSqlCommentsMdl.getBooleanValue()) {
					goldenSQL = sqlCommentPattern.matcher(goldenSQL)
							.replaceAll("").trim();
					testSQL = sqlCommentPattern.matcher(testSQL).replaceAll("")
							.trim();
				}
				// We need to remove all the temporary table names that KNIME
				// inserts and standardise them to 'table_1'
				goldenSQL = goldenSQL.replaceAll(
						"\\s*(\\sAS)\\s+(#)?[A-Za-z0-9_]+", "$1 $2table_1");
				testSQL = testSQL.replaceAll("\\s*(\\sAS)\\s+(#)?[A-Za-z0-9_]+",
						"$1 $2table_1");
				if (!goldenSQL.equals(testSQL)) {
					appendError(err, "SQL", goldenSQL, testSQL);
				}
			}

			if (cpTblSpecsMdl.getBooleanValue()
					&& !goldenQueryConn.getDataTableSpec()
							.equalStructure(testQueryConn.getDataTableSpec())) {
				appendError(err, "Output table specs",
						goldenQueryConn.getDataTableSpec().toString(),
						testQueryConn.getDataTableSpec().toString());
			}
		}

		if (err.length() > 0) {
			if (err.indexOf("\n") >= 0) {
				throw new IllegalStateException(
						err.toString().replace(NEWLINE_SPACES, " ")
								.replace("\n", "; ").replace("\r", ""));
			} else {
				throw new IllegalStateException(err.toString());
			}
		} else {
			getLogger().info("All tests completed satisfactorily");
		}
		return new PortObject[0];
	}

	private void appendError(StringBuilder err, String comparisonType,
			String expectedValue, String obtainedValue) {
		if (err.length() > 0) {
			err.append("\n");
		}
		expectedValue = expectedValue == null ? null
				: expectedValue.replace("\n", "\n\t").trim();
		expectedValue = breakLongStringsAtSpaces(expectedValue);
		obtainedValue = obtainedValue.replace("\n", "\n\t").trim();
		obtainedValue = breakLongStringsAtSpaces(obtainedValue);
		err.append(comparisonType.trim()).append(
				err.charAt(err.length() - 1) == 's' ? " don't" : " doesn't")
				.append(" match:");
		if (expectedValue != null && expectedValue.length() > 20) {
			err.append(NEWLINE_SPACES);
		} else {
			err.append(" ");
		}
		err.append("Expected '");
		err.append(expectedValue);
		err.append("',");
		if (obtainedValue != null && obtainedValue.length() > 20) {
			err.append(NEWLINE_SPACES);
		} else {
			err.append(" ");
		}
		err.append("got '");
		err.append(obtainedValue);
		err.append("'");

	}

	private String breakLongStringsAtSpaces(String str) {
		if (str == null) {
			return null;
		}
		str = str.replace("\r", "").replaceAll("[\t|\n]", " ").trim();
		if (str.indexOf(" ") >= 0 && str.length() > 55) {
			StringBuilder sb = new StringBuilder();
			int lineLength = 0;
			String[] words = str.split(" +");
			for (String word : words) {
				sb.append(word);
				lineLength += word.length();
				if (lineLength > 55) {
					sb.append("\n      ");
					lineLength = 6;
				} else {
					sb.append(" ");
					lineLength++;
				}
			}
			str = sb.toString().trim();
		}
		return str;
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
		if (hasComparableSqlInput) {
			updateEnableStatus();
		}
		return new PortObjectSpec[0];
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		//
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

	private void updateEnableStatus() {
		if (!hasComparableSqlInput) {
			return;
		}
		ignSqlCommentsMdl.setEnabled(cpSqlMdl.getBooleanValue());
	}

	@Override
	public Set<SettingsModel> getModels() {
		return models;
	}

}
