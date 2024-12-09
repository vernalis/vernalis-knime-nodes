/*******************************************************************************
 * Copyright (c) 2024 Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.pdbconnector2.nodes.hitcount;

import static com.vernalis.pdbconnector2.nodes.hitcount.PdbConnector2HitCountNodeDialog.createAddQueryToOutputModel;
import static com.vernalis.pdbconnector2.nodes.hitcount.PdbConnector2HitCountNodeDialog.createColumnNameModel;
import static com.vernalis.pdbconnector2.nodes.hitcount.PdbConnector2HitCountNodeDialog.createResultContentTypeModel;
import static com.vernalis.pdbconnector2.nodes.hitcount.PdbConnector2HitCountNodeDialog.createReturnTypeModel;
import static com.vernalis.pdbconnector2.nodes.hitcount.PdbConnector2HitCountNodeDialog.createScoringTypeModel;
import static com.vernalis.pdbconnector2.nodes.save.SaveQueryNodeModel.CFGKEY_RCSB_PDB_ADVANCED_QUERY;
import static com.vernalis.pdbconnector2.nodes.save.SaveQueryNodeModel.CFGKEY_VERSION;
import static com.vernalis.pdbconnector2.nodes.save.SaveQueryNodeModel.VERSION;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.xml.XMLCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
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
import com.vernalis.pdbconnector2.query.ResultContentType;
import com.vernalis.pdbconnector2.query.ScoringType;

/**
 * {@link NodeModel} implementation for the PDB Connector Hit Count node
 * 
 * @author S.Roughley knime@vernalis.com
 * 
 * @since 24-Jan-2023
 *
 */
public class PdbConnector2HitCountNodeModel extends NodeModel
		implements SettingsModelRegistry {

	private final SettingsModelRegistry smr =
			new SettingsModelRegistryImpl(3, getLogger()) {

				@Override
				public void doSetWarningMessage(String message) {
					setWarningMessage(message);

				}
			};

	private final SettingsModelString scoringTypeMdl =
			registerSettingsModel(createScoringTypeModel());
	private final SettingsModelString returnTypeMdl =
			registerSettingsModel(createReturnTypeModel());
	private final SettingsModelBoolean inclQueryMdl =
			registerSettingsModel(createAddQueryToOutputModel());
	// Since v2 of node
	private final SettingsModelString colNameMdl = registerSettingsModel(
			createColumnNameModel(), 2, mdl -> mdl.setStringValue("Query"));

	// Since 23-Jan-2024
		// From v3
		private final SettingsModelStringArray resultContentTypeMdl =
				registerSettingsModel(createResultContentTypeModel(), 4,
						mdl -> mdl.setStringArrayValue(Arrays
								.stream(ResultContentType.getDefaults())
								.map(rct -> rct.getText()).toArray(String[]::new)));
	private ScoringType scoringType;
	private QueryResultType resultType;
	private ResultContentType[] resultContentTypes;

	/**
	 * Constructor
	 */
	public PdbConnector2HitCountNodeModel() {
		super(new PortType[] { RCSBQueryPortObject.TYPE },
				new PortType[] { BufferedDataTable.TYPE });
		inclQueryMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				colNameMdl.setEnabled(inclQueryMdl.getBooleanValue());

			}
		});
		colNameMdl.setEnabled(inclQueryMdl.getBooleanValue());
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		if (inSpecs == null || !(inSpecs[0] instanceof MultiRCSBQueryModel)) {
			throw new InvalidSettingsException(
					"No incoming port connected, or incoming port is not expected type");
		}
		initialiseFieldsFromSettings((MultiRCSBQueryModel) inSpecs[0]);

		if (colNameMdl.isEnabled() && (colNameMdl.getStringValue() == null
				|| colNameMdl.getStringValue().isBlank())) {
			throw new InvalidSettingsException(
					"No output query column name supplied!");
		}
		return new PortObjectSpec[] { createOutputTableSpec() };
	}

	private final void initialiseFieldsFromSettings(
			MultiRCSBQueryModel inQuerySpec) throws InvalidSettingsException {
		MultiRCSBQueryModel model = inQuerySpec;
		if (!model.hasQuery()) {
			throw new InvalidSettingsException(
					"Incoming port does not contain a valid query");
		}
		if (model.hasInvalidQuery()) {
			throw new InvalidSettingsException(
					"Supplied model has invalid queries - check in query builder!");
		}

		try {
			scoringType = ScoringType.fromText(scoringTypeMdl.getStringValue());
			if (!scoringType.isEnabled(model)) {
				throw new InvalidSettingsException(
						"The current scoring method is not valid for the query");
			}
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new InvalidSettingsException(
					"'" + scoringTypeMdl.getStringValue()
							+ "' is not a valid Scoring strategy");
		}

		try {
			resultType =
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
			resultContentTypes =
					Arrays.stream(resultContentTypeMdl.getStringArrayValue())
							.map(ResultContentType::fromText)
							.toArray(ResultContentType[]::new);
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new InvalidSettingsException(
					"'" + returnTypeMdl.getStringValue()
							+ "' is not a valid Return Type");
		}
	}

	private DataTableSpec createOutputTableSpec() {
		DataTableSpecCreator specFact = new DataTableSpecCreator();
		if (inclQueryMdl.getBooleanValue()) {
			specFact.addColumns(
					new DataColumnSpecCreator(colNameMdl.getStringValue(),
							XMLCellFactory.TYPE).createSpec());
		}
		specFact.addColumns(new DataColumnSpecCreator("Hit Count", IntCell.TYPE)
				.createSpec());
		return specFact.createSpec();
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {
		if (inObjects == null
				|| !(inObjects[0] instanceof RCSBQueryPortObject)) {
			throw new InvalidSettingsException(
					"No incoming port connected, or incoming port is not expected type");
		}
		final MultiRCSBQueryModel model =
				((RCSBQueryPortObject) inObjects[0]).getSpec();
		initialiseFieldsFromSettings(model);

		final RCSBQueryRunner runner = new RCSBQueryRunner(model);
		runner.setQueryResultType(resultType);
		runner.setScoringType(scoringType);
		runner.setResultContentType(resultContentTypes);

		int hitCount = runner.getHitCount();
		DataCell hitCountCell = new IntCell(hitCount);

		final BufferedDataContainer bdc =
				exec.createDataContainer(createOutputTableSpec());

		if (inclQueryMdl.getBooleanValue()) {// Store the query in a
												// NodeSettings object
			NodeSettings settings =
					new NodeSettings(CFGKEY_RCSB_PDB_ADVANCED_QUERY);
			settings.addInt(CFGKEY_VERSION, VERSION);
			model.saveSettingsTo(settings);

			try (PipedInputStream pis = new PipedInputStream();
					PipedOutputStream pos = new PipedOutputStream(pis)) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							settings.saveToXML(pos);
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}

					}
				}).start();

				bdc.addRowToTable(new DefaultRow(RowKey.createRowKey(0L),
						XMLCellFactory.create(pis), hitCountCell));
			}
		} else {
			bdc.addRowToTable(
					new DefaultRow(RowKey.createRowKey(0L), hitCountCell));
		}

		bdc.close();
		return new PortObject[] { bdc.getTable() };
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
