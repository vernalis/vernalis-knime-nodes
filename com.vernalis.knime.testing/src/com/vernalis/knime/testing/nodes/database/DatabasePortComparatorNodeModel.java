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
package com.vernalis.knime.testing.nodes.database;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
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
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.database.DatabaseConnectionPortObject;
import org.knime.core.node.port.database.DatabaseConnectionSettings;
import org.knime.core.node.port.database.DatabasePortObject;
import org.knime.core.node.port.database.DatabasePortObjectSpec;
import org.knime.core.node.port.database.DatabaseQueryConnectionSettings;

import com.vernalis.knime.nodes.SettingsModelRegistry;

import static com.vernalis.knime.testing.nodes.database.DatabasePortComparatorNodeDialog.createCompareDbtypeModel;
import static com.vernalis.knime.testing.nodes.database.DatabasePortComparatorNodeDialog.createCompareDriversModel;
import static com.vernalis.knime.testing.nodes.database.DatabasePortComparatorNodeDialog.createComparePortTypesModel;
import static com.vernalis.knime.testing.nodes.database.DatabasePortComparatorNodeDialog.createCompareSqlModel;
import static com.vernalis.knime.testing.nodes.database.DatabasePortComparatorNodeDialog.createCompareTableSpecsModel;
import static com.vernalis.knime.testing.nodes.database.DatabasePortComparatorNodeDialog.createCompareURLModel;
import static com.vernalis.knime.testing.nodes.database.DatabasePortComparatorNodeDialog.createCompareUsernameModel;
import static com.vernalis.knime.testing.nodes.database.DatabasePortComparatorNodeDialog.createIgnoreSqlCommentsModel;

/**
 * NodeModel for the Database Port Comparator node
 * 
 * @since 07-Sep-2022
 * @since v1.36.0

 * @author S Roughley
 *
 */
public class DatabasePortComparatorNodeModel extends NodeModel
		implements SettingsModelRegistry {

	private final Set<SettingsModel> models = new LinkedHashSet<>();
	private final SettingsModelBoolean cpPortTypesMdl =
			registerSettingsModel(createComparePortTypesModel());
	private final SettingsModelBoolean cpDriversMdl =
			registerSettingsModel(createCompareDriversModel());
	private final SettingsModelBoolean cpURLMdl =
			registerSettingsModel(createCompareURLModel());
	private final SettingsModelBoolean cpUNameMdl =
			registerSettingsModel(createCompareUsernameModel());
	private final SettingsModelBoolean cpDbTypeMdl =
			registerSettingsModel(createCompareDbtypeModel());
	private final SettingsModelBoolean cpSqlMdl;
	private final SettingsModelBoolean ignSqlCommentsMdl =
			registerSettingsModel(createIgnoreSqlCommentsModel());
	private final SettingsModelBoolean cpTblSpecsMdl =
			registerSettingsModel(createCompareTableSpecsModel());

	private boolean hasComparableSqlInput;
	private static final Pattern sqlCommentPattern =
			Pattern.compile("((?s)/\\*.*?\\*/(?-s)|--.*)");

	/**
	 * Constructor
	 */
	public DatabasePortComparatorNodeModel() {
		super(new PortType[] { DatabaseConnectionPortObject.TYPE,
				DatabaseConnectionPortObject.TYPE }, new PortType[0]);
		cpSqlMdl = registerSettingsModel(createCompareSqlModel());
		cpSqlMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateEnableStatus();

			}
		});
		updateEnableStatus();
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

		// Compare the types...
		if (cpPortTypesMdl.getBooleanValue()
				&& !inObjects[0].getClass().equals(inObjects[1].getClass())) {
			appendError(err, "PortTypes",
					inObjects[0].getClass().getSimpleName(),
					inObjects[1].getClass().getSimpleName());
		}

		// Now, regardless of type, do some comparisons...
		DatabaseConnectionPortObject goldenPort =
				(DatabaseConnectionPortObject) inObjects[1];
		DatabaseConnectionPortObject testPort =
				(DatabaseConnectionPortObject) inObjects[0];
		DatabaseConnectionSettings goldenConn =
				goldenPort.getConnectionSettings(getCredentialsProvider());
		DatabaseConnectionSettings testConn =
				testPort.getConnectionSettings(getCredentialsProvider());

		if (cpDriversMdl.getBooleanValue()
				&& !goldenConn.getDriver().equals(testConn.getDriver())) {
			appendError(err, "Drivers", goldenConn.getDriver(),
					testConn.getDriver());

		}

		if (cpURLMdl.getBooleanValue()
				&& !goldenConn.getJDBCUrl().equals(testConn.getJDBCUrl())) {
			appendError(err, "URLs", goldenConn.getJDBCUrl(),
					testConn.getJDBCUrl());
		}

		if (cpUNameMdl.getBooleanValue()
				&& !goldenConn.getUserName(getCredentialsProvider()).equals(
						testConn.getUserName(getCredentialsProvider()))) {
			appendError(err, "Usernames",
					goldenConn.getUserName(getCredentialsProvider()),
					testConn.getUserName(getCredentialsProvider()));
		}

		if (cpDbTypeMdl.getBooleanValue() && !DatabaseConnectionSettings
				.getDatabaseIdentifierFromJDBCUrl(goldenConn.getJDBCUrl())
				.equals(DatabaseConnectionSettings
						.getDatabaseIdentifierFromJDBCUrl(
								testConn.getJDBCUrl()))) {
			appendError(err, "Database Types",
					DatabaseConnectionSettings.getDatabaseIdentifierFromJDBCUrl(
							goldenConn.getJDBCUrl()),
					DatabaseConnectionSettings.getDatabaseIdentifierFromJDBCUrl(
							testConn.getJDBCUrl()));
		}

		if (hasComparableSqlInput) {
			// Both types are DCPO
			DatabaseQueryConnectionSettings goldenQueryConn =
					(DatabaseQueryConnectionSettings) goldenConn;
			DatabaseQueryConnectionSettings testQueryConn =
					(DatabaseQueryConnectionSettings) testConn;
			if (cpSqlMdl.getBooleanValue()) {
				String goldenSQL = goldenQueryConn.getQuery();
				String testSQL = testQueryConn.getQuery();
				if (ignSqlCommentsMdl.getBooleanValue()) {
					goldenSQL = sqlCommentPattern.matcher(goldenSQL)
							.replaceAll("").trim();
					testSQL = sqlCommentPattern.matcher(testSQL).replaceAll("")
							.trim();
				}
				// We need to remove all the temporary table names that KNIME
				// inserts
				goldenSQL = goldenSQL.replaceAll("table_[\\d]+", "table_1");
				testSQL = testSQL.replaceAll("table_[\\d]+", "table_1");
				if (!goldenSQL.equals(testSQL)) {
					appendError(err, "SQL", goldenSQL, testSQL);
				}
			}

			if (cpTblSpecsMdl.getBooleanValue()
					&& !((DatabasePortObject) goldenPort).getSpec()
							.getDataTableSpec()
							.equalStructure(((DatabasePortObject) goldenPort)
									.getSpec().getDataTableSpec())) {
				appendError(err, "Output table specs",
						((DatabasePortObject) goldenPort).getSpec()
								.getDataTableSpec().toString(),
						((DatabasePortObject) testPort).getSpec()
								.getDataTableSpec().toString());
			}
		}

		if (err.length() > 0) {
			if (err.indexOf("\n") >= 0) {
				throw new InvalidSettingsException(
						err.toString().replace("\n   ", " ").replace("\n", "; ")
								.replace("\r", ""));
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
		expectedValue = expectedValue.replace("\n", "\n\t").trim();
		expectedValue = breakLongStringsAtSpaces(expectedValue);
		obtainedValue = obtainedValue.replace("\n", "\n\t").trim();
		obtainedValue = breakLongStringsAtSpaces(obtainedValue);
		err.append(comparisonType.trim()).append(
				err.charAt(err.length() - 1) == 's' ? " don't" : " doesn't")
				.append(" match:");
		if (expectedValue.length() > 20) {
			err.append("\n   ");
		} else {
			err.append(" ");
		}
		err.append("Expected '");
		err.append(expectedValue);
		err.append("',");
		if (obtainedValue.length() > 20) {
			err.append("\n   ");
		} else {
			err.append(" ");
		}
		err.append("got '");
		err.append(obtainedValue);
		err.append("'");

	}

	private String breakLongStringsAtSpaces(String str) {
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
		hasComparableSqlInput = inSpecs[0] instanceof DatabasePortObjectSpec
				&& inSpecs[1] instanceof DatabasePortObjectSpec;
		updateEnableStatus();
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
		cpSqlMdl.setEnabled(hasComparableSqlInput);
		cpTblSpecsMdl.setEnabled(hasComparableSqlInput);
		ignSqlCommentsMdl.setEnabled(
				hasComparableSqlInput && cpSqlMdl.getBooleanValue());
	}

	@Override
	public Set<SettingsModel> getModels() {
		return models;
	}

}
