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
package com.vernalis.pdbconnector2.nodes.execute;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.json.JSONCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import com.vernalis.knime.nodes.SettingsModelRegistry;
import com.vernalis.pdbconnector2.ports.MultiRCSBQueryModel;
import com.vernalis.pdbconnector2.ports.RCSBQueryPortObject;
import com.vernalis.pdbconnector2.query.QueryResultType;
import com.vernalis.pdbconnector2.query.RCSBQueryRunner;
import com.vernalis.pdbconnector2.query.ScoringType;

import static com.vernalis.pdbconnector2.nodes.execute.PdbConnector2QueryExecutionNodeDialog.createIncludeJsonModel;
import static com.vernalis.pdbconnector2.nodes.execute.PdbConnector2QueryExecutionNodeDialog.createPageSizeModel;
import static com.vernalis.pdbconnector2.nodes.execute.PdbConnector2QueryExecutionNodeDialog.createReturnTypeModel;
import static com.vernalis.pdbconnector2.nodes.execute.PdbConnector2QueryExecutionNodeDialog.createScoringTypeModel;

/**
 * {@link NodeModel} implementation for the PDB Connector Query Executer node
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
/**
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class PdbConnector2QueryExecutionNodeModel extends NodeModel
		implements SettingsModelRegistry {

	private final Set<SettingsModel> models = new LinkedHashSet<>();

	private final SettingsModelString scoringTypeMdl =
			registerSettingsModel(createScoringTypeModel());
	private final SettingsModelString returnTypeMdl =
			registerSettingsModel(createReturnTypeModel());
	private final SettingsModelIntegerBounded pageSizeMdl =
			registerSettingsModel(createPageSizeModel());
	private final SettingsModelBoolean includeJsonMdl =
			registerSettingsModel(createIncludeJsonModel());

	/**
	 * Constructor
	 */
	public PdbConnector2QueryExecutionNodeModel() {
		super(new PortType[] { RCSBQueryPortObject.TYPE },
				new PortType[] { BufferedDataTable.TYPE });

	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		if (inSpecs == null || !(inSpecs[0] instanceof MultiRCSBQueryModel)) {
			throw new InvalidSettingsException(
					"No incoming port connected, or incoming port is not expected type");
		}
		final MultiRCSBQueryModel model = (MultiRCSBQueryModel) inSpecs[0];
		if (!model.hasQuery()) {
			throw new InvalidSettingsException(
					"Incoming port does not contain a valid query");
		}

		try {
			ScoringType.fromText(scoringTypeMdl.getStringValue());
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new InvalidSettingsException(
					"'" + scoringTypeMdl.getStringValue()
							+ "' is not a valid Scoring strategy");
		}

		try {
			QueryResultType.fromText(returnTypeMdl.getStringValue());
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new InvalidSettingsException(
					"'" + returnTypeMdl.getStringValue()
							+ "' is not a valid Return Type");
		}

		return new PortObjectSpec[] { createOutputSpec(model) };
	}

	/**
	 * @param model
	 * @return
	 */
	private DataTableSpec createOutputSpec(final MultiRCSBQueryModel model) {

		if (includeJsonMdl.getBooleanValue()) {
			return new DataTableSpecCreator(model.getResultTableSpec())
					.addColumns(new DataColumnSpecCreator("Raw Json",
							JSONCellFactory.TYPE).createSpec())
					.createSpec();
		} else {
			return model.getResultTableSpec();
		}
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {
		final MultiRCSBQueryModel model =
				((RCSBQueryPortObject) inObjects[0]).getSpec();
		final BufferedDataContainer bdc =
				exec.createDataContainer(createOutputSpec(model));
		final RCSBQueryRunner runner = new RCSBQueryRunner(model);
		runner.setQueryResultType(
				QueryResultType.fromText(returnTypeMdl.getStringValue()));
		runner.setScoringType(
				ScoringType.fromText(scoringTypeMdl.getStringValue()));
		runner.setPageSize(pageSizeMdl.getIntValue());
		runner.setIncludeJson(includeJsonMdl.getBooleanValue());
		runner.runQueryToTable(bdc, exec);
		bdc.close();
		return new PortObject[] { bdc.getTable() };
	}

	@Override
	public Set<SettingsModel> getModels() {
		return models;
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

}
