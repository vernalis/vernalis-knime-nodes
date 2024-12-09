/*******************************************************************************
 * Copyright (c) 2020, 2024 Vernalis (R&D) Ltd
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

import static com.vernalis.pdbconnector2.nodes.execute.PdbConnector2QueryExecutionNodeDialog.createIncludeHitCountModel;
import static com.vernalis.pdbconnector2.nodes.execute.PdbConnector2QueryExecutionNodeDialog.createIncludeJsonModel;
import static com.vernalis.pdbconnector2.nodes.execute.PdbConnector2QueryExecutionNodeDialog.createLimitHitsModel;
import static com.vernalis.pdbconnector2.nodes.execute.PdbConnector2QueryExecutionNodeDialog.createMaxHitsModel;
import static com.vernalis.pdbconnector2.nodes.execute.PdbConnector2QueryExecutionNodeDialog.createPageSizeModel;
import static com.vernalis.pdbconnector2.nodes.execute.PdbConnector2QueryExecutionNodeDialog.createResultContentTypeModel;
import static com.vernalis.pdbconnector2.nodes.execute.PdbConnector2QueryExecutionNodeDialog.createReturnTypeModel;
import static com.vernalis.pdbconnector2.nodes.execute.PdbConnector2QueryExecutionNodeDialog.createScoringTypeModel;
import static com.vernalis.pdbconnector2.nodes.execute.PdbConnector2QueryExecutionNodeDialog.createVerboseOutputModel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.ColumnFilter;

import com.vernalis.knime.data.datacolumn.RegexColumnNameColumnFilter;
import com.vernalis.knime.nodes.SettingsModelRegistry;
import com.vernalis.knime.nodes.SettingsModelRegistryImpl;
import com.vernalis.knime.nodes.SettingsModelWrapper;
import com.vernalis.pdbconnector2.ports.MultiRCSBQueryModel;
import com.vernalis.pdbconnector2.ports.RCSBQueryPortObject;
import com.vernalis.pdbconnector2.query.QueryResultType;
import com.vernalis.pdbconnector2.query.RCSBQueryRunner;
import com.vernalis.pdbconnector2.query.RCSBQueryRunner.QueryException;
import com.vernalis.pdbconnector2.query.ResultContentType;
import com.vernalis.pdbconnector2.query.ScoringType;

/**
 * {@link NodeModel} implementation for the PDB Connector Query Executer node
 * 
 * Changes v1.28.3 - Added support for limiting returned hits
 * Changes v1.31.0 - Added backwards compatibility to SMR implementation and verbose output option
 * Changes v1.37.0 - Added option to return computational structures 
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class PdbConnector2QueryExecutionNodeModel extends NodeModel
		implements SettingsModelRegistry {

	private final SettingsModelRegistry smr =
			new SettingsModelRegistryImpl(4, getLogger()) {

				@Override
				public void doSetWarningMessage(String message) {
					setWarningMessage(message);

				}
			};

	private final SettingsModelString scoringTypeMdl =
			registerSettingsModel(createScoringTypeModel());
	private final SettingsModelString returnTypeMdl =
			registerSettingsModel(createReturnTypeModel());
	private final SettingsModelIntegerBounded pageSizeMdl =
			registerSettingsModel(createPageSizeModel());
	private final SettingsModelBoolean includeJsonMdl =
			registerSettingsModel(createIncludeJsonModel());
	// From v2
	private final SettingsModelBoolean limitHitsMdl = registerSettingsModel(
			createLimitHitsModel(), 2, mdl -> mdl.setBooleanValue(false));
	private final SettingsModelIntegerBounded maxHitsMdl =
			registerSettingsModel(createMaxHitsModel(), 2,
					mdl -> mdl.setIntValue(1000));
	private final SettingsModelBoolean includeHitCountMdl =
			registerSettingsModel(createIncludeHitCountModel(), 2,
					mdl -> mdl.setBooleanValue(false));
	// Since 1.31.0
	// From v3
	private final SettingsModelBoolean verboseOutputMdl = registerSettingsModel(
			createVerboseOutputModel(), 3, mdl -> mdl.setBooleanValue(true));

	// Since 1.37.0
	// From v4
	private final SettingsModelStringArray resultContentTypeMdl =
			registerSettingsModel(createResultContentTypeModel(), 4,
					mdl -> mdl.setStringArrayValue(Arrays
							.stream(ResultContentType.getDefaults())
							.map(rct -> rct.getText()).toArray(String[]::new)));

	/**
	 * Constructor
	 */
	public PdbConnector2QueryExecutionNodeModel() {
		super(new PortType[] { RCSBQueryPortObject.TYPE },
				new PortType[] { BufferedDataTable.TYPE });
		limitHitsMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				maxHitsMdl.setEnabled(limitHitsMdl.getBooleanValue());

			}
		});
		maxHitsMdl.setEnabled(limitHitsMdl.getBooleanValue());
		includeJsonMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				verboseOutputMdl.setEnabled(includeJsonMdl.getBooleanValue());

			}
		});
		verboseOutputMdl.setEnabled(includeJsonMdl.getBooleanValue());
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
			ScoringType scoring =
					ScoringType.fromText(scoringTypeMdl.getStringValue());
			if (!scoring.isEnabled(model)) {
				throw new InvalidSettingsException(
						"The current scoring method is not valid for the query");
			}
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

		// Since 23-Jan-2024
		if (resultContentTypeMdl.getStringArrayValue() == null
				|| resultContentTypeMdl.getStringArrayValue().length == 0) {
			throw new IllegalArgumentException(
					"A least one Result Content Type must be supplied");
		}
		try {
			Arrays.stream(resultContentTypeMdl.getStringArrayValue())
					.map(ResultContentType::fromText).toArray();
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new InvalidSettingsException(
					"'" + returnTypeMdl.getStringValue()
							+ "' is not a valid Return Type");
		}

		try {
			return new PortObjectSpec[] {
					createQueryRunner(model).getOutputTableSpec() };
		} catch (NullPointerException | IllegalArgumentException
				| QueryException e) {
			throw new InvalidSettingsException(e);
		}
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {
		final MultiRCSBQueryModel model =
				((RCSBQueryPortObject) inObjects[0]).getSpec();

		final RCSBQueryRunner runner = createQueryRunner(model);
		final BufferedDataContainer bdc =
				exec.createDataContainer(runner.getOutputTableSpec());
		if (!runner.runQueryToTable(bdc, exec)) {
			setWarningMessage(
					"Returned hits were truncated by node settings - not all hits were returned");
		}
		bdc.close();
		return new PortObject[] { bdc.getTable() };
	}

	/**
	 * @param model
	 *            the query model
	 * 
	 * @return a runner to run the query on the server
	 * 
	 * @throws NullPointerException
	 * @throws IllegalArgumentException
	 * @throws QueryException
	 */
	private RCSBQueryRunner createQueryRunner(final MultiRCSBQueryModel model)
			throws NullPointerException, IllegalArgumentException,
			QueryException {
		final RCSBQueryRunner runner = new RCSBQueryRunner(model);
		runner.setQueryResultType(
				QueryResultType.fromText(returnTypeMdl.getStringValue()));
		runner.setScoringType(
				ScoringType.fromText(scoringTypeMdl.getStringValue()));
		runner.setPageSize(pageSizeMdl.getIntValue());
		runner.setIncludeJson(includeJsonMdl.getBooleanValue());
		runner.setIncludeHitCount(includeHitCountMdl.getBooleanValue());
		runner.setVerboseOutput(includeJsonMdl.getBooleanValue()
				&& verboseOutputMdl.getBooleanValue());
		runner.setResultContentType(
				Arrays.stream(resultContentTypeMdl.getStringArrayValue())
						.map(ResultContentType::fromText)
						.toArray(ResultContentType[]::new));
		if (limitHitsMdl.getBooleanValue()) {
			runner.setReturnedHitsLimit(maxHitsMdl.getIntValue());
		} else {
			runner.clearReturnedHitsLimit();
		}
		return runner;
	}

	@Override
	public Set<SettingsModel> getModels() {
		return smr.getModels();
	}

	@Override
	public <T extends SettingsModel> T registerSettingsModel(T model) {
		return smr.registerSettingsModel(model);
	}

	@Override
	public <T extends Iterable<U>, U extends SettingsModel> T
			registerModels(T models) {
		return smr.registerModels(models);
	}

	@Override
	public <T extends Map<?, V>, V extends SettingsModel> T
			registerMapValuesModels(T models) {
		return smr.registerMapValuesModels(models);
	}

	@Override
	public <T extends Map<K, ?>, K extends SettingsModel> T
			registerMapKeysModels(T models) {
		return smr.registerMapKeysModels(models);
	}

	@Override
	public int getValidatedColumnSelectionModelColumnIndex(
			SettingsModelString model, ColumnFilter filter, DataTableSpec spec,
			Pattern preferredPattern, boolean matchWholeName, NodeLogger logger,
			boolean dontAllowDuplicatesWithAvoid,
			SettingsModelString... modelsToAvoid)
			throws InvalidSettingsException {
		return smr.getValidatedColumnSelectionModelColumnIndex(model, filter,
				spec, preferredPattern, matchWholeName, logger,
				dontAllowDuplicatesWithAvoid, modelsToAvoid);
	}

	@Override
	public int getValidatedColumnSelectionModelColumnIndex(
			SettingsModelString model, ColumnFilter filter, DataTableSpec spec,
			NodeLogger logger, SettingsModelString... modelsToAvoid)
			throws InvalidSettingsException {
		return smr.getValidatedColumnSelectionModelColumnIndex(model, filter,
				spec, logger, modelsToAvoid);
	}

	@Override
	public int getValidatedColumnSelectionModelColumnIndex(
			SettingsModelString model, RegexColumnNameColumnFilter filter,
			DataTableSpec spec, NodeLogger logger,
			boolean dontAllowDuplicatesWithAvoid,
			SettingsModelString... modelsToAvoid)
			throws InvalidSettingsException {
		return smr.getValidatedColumnSelectionModelColumnIndex(model, filter,
				spec, logger, dontAllowDuplicatesWithAvoid, modelsToAvoid);
	}

	@Override
	public boolean isStringModelFilled(SettingsModelString stringModel) {
		return smr.isStringModelFilled(stringModel);
	}

	@Override
	public int getSavedSettingsVersion(NodeSettingsRO settings) {
		return smr.getSavedSettingsVersion(settings);
	}

	@Override
	public int getSettingsVersion() {
		return smr.getSettingsVersion();
	}

	@Override
	public Map<SettingsModel, SettingsModelWrapper<?>> getModelWrappers() {
		return smr.getModelWrappers();
	}

	@Override
	public void setNodeWarningMessage(String message) {
		smr.setNodeWarningMessage(message);
	}

	@Override
	public NodeLogger getNodeLogger() {
		return smr.getNodeLogger();
	}

	@Override
	public <T extends SettingsModel> T registerSettingsModel(T model,
			int sinceVersion, Consumer<T> defaultSetter) {
		return smr.registerSettingsModel(model, sinceVersion, defaultSetter);
	}

	@Override
	public <T extends SettingsModel> T registerSettingsModel(T model,
			int sinceVersion, Consumer<T> defaultSetter, String successMessage)
			throws UnsupportedOperationException, IllegalArgumentException {
		return smr.registerSettingsModel(model, sinceVersion, defaultSetter,
				successMessage);
	}

	@Override
	public <T extends SettingsModel> T registerSettingsModel(T model,
			int sinceVersion, BiConsumer<T, NodeSettingsRO> defaultSetter) {
		return smr.registerSettingsModel(model, sinceVersion, defaultSetter);
	}

	@Override
	public <T extends SettingsModel> T registerSettingsModel(T model,
			int sinceVersion, BiConsumer<T, NodeSettingsRO> defaultSetter,
			String successMessage)
			throws UnsupportedOperationException, IllegalArgumentException {
		return smr.registerSettingsModel(model, sinceVersion, defaultSetter,
				successMessage);
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		//
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		//
	}

	@Override
	public void saveSettingsTo(NodeSettingsWO settings) {
		smr.saveSettingsTo(settings);

	}

	@Override
	public void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		smr.validateSettings(settings);

	}

	@Override
	public void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		smr.loadValidatedSettingsFrom(settings);

	}

	@Override
	protected void reset() {
		//
	}

}
