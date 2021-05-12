/*******************************************************************************
 * Copyright (c) 2016,2020 Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.pdbconnector.nodes.describehet;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.knime.chem.types.InchiAdapterCell;
import org.knime.chem.types.InchiCellFactory;
import org.knime.chem.types.SmilesAdapterCell;
import org.knime.chem.types.SmilesCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.vernalis.pdbconnector2.graphql.RCSBGraphQLRunnerBuilder;

import static com.vernalis.pdbconnector.nodes.describehet.PdbDescribeHetNodeDialog.createPageSizeModel;

/**
 * This is the model implementation of PdbDescribeHet. Node to retrieve
 * Heterogen details from the PDB "Describe Heterogen" webservice
 * 
 * @author S.Roughley
 */
public class PdbDescribeHetNodeModel extends NodeModel {

	// Settings Model Keys
	/** The key for the 'Het ID Column' setting */
	static final String HETID_COL_NAME_KEY = "Het ID Column";
	/** The key for the 'Type' setting */
	static final String TYPE_KEY = "Type";
	/** The key for the 'M Wt' setting */
	static final String MWT_KEY = "M_Wt";
	/** The key for the 'Chemical Name' setting */
	static final String CHEM_NAME_KEY = "Chemical Name";
	/** The key for the 'Formula' setting */
	static final String FORMULA_KEY = "Formula";
	/** The key for the 'InChI Key' setting */
	static final String INCHI_KEY_KEY = "InChI Key";
	/** The key for the 'InChI' setting */
	static final String INCHI_KEY = "InChI";
	/** The key for the 'SMILES' setting */
	static final String SMILES_KEY = "SMILES String";

	/** The base URL of the webservice */
	static final String SERVICE_BASE_URL = "https://data.rcsb.org/graphql";
	/**
	 * The GraphQL Query
	 */
	// See https://data.rcsb.org/migration-guide.html#pdb-ligands - modified
	// slightly!
	//:@formatter:off
	static final String GRAPHQL = "query ($ids: [String]!, $inchiSmiles: Boolean!){chem_comps(comp_ids: $ids) {\n"
			+ "    chem_comp {\n" + "      id\n" + "      type\n"
			+ "      formula_weight\n" + "      name\n" + "      formula\n"
			+ "    }\n"
			+ "    pdbx_chem_comp_descriptor @include(if: $inchiSmiles) {\n"
			+ "      descriptor\n"
			+ "      type\n"
			+ "      program\n"
			+ "    }\n"
			+ "  }\n"
			+ "}";
	//:@formatter:on

	/**
	 * The retry delays to use if an attempt to call the webservice fails
	 */
	static final int[] retryDelays = { 0, 1, 5, 10, 30, 60 };

	// The SettingsModels
	private SettingsModelString m_colName =
			PdbDescribeHetNodeDialog.createHetIdColumnNameModel();
	private SettingsModelBoolean m_type =
			PdbDescribeHetNodeDialog.createTypeModel();
	private SettingsModelBoolean m_MolWt =
			PdbDescribeHetNodeDialog.createMolWtModel();
	private SettingsModelBoolean m_ChemName =
			PdbDescribeHetNodeDialog.createChemicalNameModel();
	private SettingsModelBoolean m_Formula =
			PdbDescribeHetNodeDialog.createFormulaModel();
	private SettingsModelBoolean m_InChiKey =
			PdbDescribeHetNodeDialog.createInchiKeyModel();
	private SettingsModelBoolean m_InChi =
			PdbDescribeHetNodeDialog.createInchiModel();
	private SettingsModelBoolean m_Smiles =
			PdbDescribeHetNodeDialog.createSmilesModel();
	private final SettingsModelIntegerBounded pageSizeMdl =
			createPageSizeModel();

	private Map<String, DataType> m_newCols;// The added columns and their types
	private Map<String, JsonNode> m_hetDetails; // The heterogen details cache
	private CloseableRowIterator iter = null;

	private int idColIndex;
	private RCSBGraphQLRunnerBuilder builder;

	/**
	 * Constructor for the node model.
	 */
	protected PdbDescribeHetNodeModel() {
		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		iter = inData[0].iterator();
		idColIndex = inData[0].getDataTableSpec()
				.findColumnIndex(m_colName.getStringValue());
		m_hetDetails = new HashMap<>();
		builder = new RCSBGraphQLRunnerBuilder(SERVICE_BASE_URL, GRAPHQL)
				.addBooleanVariable("inchiSmiles",
						m_InChi.getBooleanValue()
								|| m_InChiKey.getBooleanValue()
								|| m_Smiles.getBooleanValue());

		return new BufferedDataTable[] { exec.createColumnRearrangeTable(
				inData[0], createColumnRearranger(inData[0].getSpec(), exec),
				exec) };
	}

	/**
	 * Actually get the next block of heterogen data from the webservice. The
	 * calls are made in a separate thread
	 * 
	 * @param exec
	 *            The execution context to allow cancellation
	 * @throws CanceledExecutionException
	 *             If the user cancelled during execution
	 * @throws IOException
	 *             If there was an error retrieving the results
	 * @throws InterruptedException
	 *             If the thread was interrupted
	 * 
	 */
	private void getHeterogenData(final ExecutionContext exec)
			throws CanceledExecutionException, IOException,
			InterruptedException {

		// Clear the cache
		m_hetDetails.clear();
		exec.setMessage("Retrieving next result page from server");

		// A set for the next group of IDs
		HashSet<String> hetIds = new HashSet<>();
		while (iter.hasNext() && hetIds.size() < pageSizeMdl.getIntValue()) {
			DataRow row = iter.next();
			DataCell idCell = row.getCell(idColIndex);
			if (idCell.isMissing()) {
				continue;
			}

			String id = ((StringValue) idCell).getStringValue().trim()
					.toUpperCase();
			if (id.length() > 3 || id.isEmpty()) {
				// We add the invalid ID to the results so we dont incorrectly
				// trigger another WS call in the ColumnRearranger
				m_hetDetails.put(id, null);
				setWarningMessage("Invalid Het IDs encountered");
				continue;
			}
			hetIds.add(id);
		}

		builder.addStringArrayVariable("ids",
				hetIds.toArray(new String[hetIds.size()]));

		JsonNode result = null;
		for (int delay : retryDelays) {
			try {
				// Now send the request in a separate thread, waiting for it to
				// complete
				ExecutorService pool = Executors.newSingleThreadExecutor();
				Future<JsonNode> future = pool.submit(builder.getRunner());
				while (!future.isDone()) {
					// wait a 0.1 seconds
					long time = System.nanoTime();
					while (System.nanoTime() - time < 100000) {
						// Wait
					}
					try {
						exec.checkCanceled();
					} catch (CanceledExecutionException e) {
						future.cancel(true);
						while (!future.isCancelled()) {
							// Wait for the cancel to happen
						}
						throw e;
					}
				}

				result = future.get();
				break;
			} catch (IOException | ExecutionException e) {
				// Wait for an increasing period before retrying
				getLogger().warn("POST request failed for data block - Waiting "
						+ delay + " seconds before re-trying...");
				exec.checkCanceled();
				pause(delay, exec);
			}
		}
		if (result == null) {
			// In this case, we never managed to contact the server...
			String errMsg =
					"Unable to contact the remote server - please try again later!";
			getLogger().error(errMsg);
			throw new IOException(errMsg);
		}

		// Now we need to parse the lines into records
		if (result.has("errors")) {
			StringBuilder sb = new StringBuilder();
			Iterator<JsonNode> errIter =
					((ArrayNode) result.get("errors")).elements();
			while (errIter.hasNext()) {
				JsonNode err = errIter.next();
				sb.append(err.get("message").asText());
			}
			throw new IOException(sb.toString());
		}
		Iterator<JsonNode> resultIter =
				result.get("data").get("chem_comps").iterator();
		while (resultIter.hasNext()) {
			JsonNode res = resultIter.next();
			m_hetDetails.put(res.get("chem_comp").get("id").asText(), res);
		}

	}

	private static void pause(int seconds, ExecutionMonitor exec)
			throws CanceledExecutionException {
		// simple delay function without using threads
		Date start = new Date();
		while (new Date().getTime() - start.getTime() < seconds * 1000) {
			exec.checkCanceled();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		if (iter != null) {
			iter.close();
		}
		iter = null;
		if (m_hetDetails != null) {
			m_hetDetails.clear();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		// Ensure a column is selected, and autoselect one if it isnt
		int colIndex = -1;
		if (m_colName.getStringValue() == null) {
			int i = 0;
			for (DataColumnSpec cs : inSpecs[0]) {
				if (cs.getType().isCompatible(StringValue.class)) {
					if (colIndex != -1) {
						setWarningMessage("No ID column selected");
						throw new InvalidSettingsException(
								"No ID column selected.");
					}
					colIndex = i;
				}
				i++;
			}

			if (colIndex == -1) {
				setWarningMessage("No ID column selected");
				throw new InvalidSettingsException("No ID column selected.");
			}
			m_colName.setStringValue(
					inSpecs[0].getColumnSpec(colIndex).getName());
			setWarningMessage("Column '" + m_colName.getStringValue()
					+ "' auto selected for ID column");
		} else {
			colIndex = inSpecs[0].findColumnIndex(m_colName.getStringValue());
			if (colIndex < 0) {
				setWarningMessage(
						"No such column: " + m_colName.getStringValue());
				throw new InvalidSettingsException(
						"No such column: " + m_colName.getStringValue());
			}

			DataColumnSpec colSpec = inSpecs[0].getColumnSpec(colIndex);
			if (!colSpec.getType().isCompatible(StringValue.class)) {
				setWarningMessage("Column \"" + m_colName.getStringValue()
						+ "\" does not contain string values");
				throw new InvalidSettingsException("Column \"" + m_colName
						+ "\" does not contain string values: "
						+ colSpec.getType().toString());
			}
		}

		// Now we need to decide which columns are added
		m_newCols = new LinkedHashMap<>();
		if (m_type.getBooleanValue()) {
			m_newCols.put(DataTableSpec.getUniqueColumnName(inSpecs[0], "Type"),
					StringCell.TYPE);
		}
		if (m_MolWt.getBooleanValue()) {
			m_newCols.put(
					DataTableSpec.getUniqueColumnName(inSpecs[0], "M. Wt."),
					DoubleCell.TYPE);
		}
		if (m_ChemName.getBooleanValue()) {
			m_newCols.put(DataTableSpec.getUniqueColumnName(inSpecs[0],
					"Chemical Name"), StringCell.TYPE);
		}
		if (m_Formula.getBooleanValue()) {
			m_newCols.put(DataTableSpec.getUniqueColumnName(inSpecs[0],
					"Chemical Formula"), StringCell.TYPE);
		}
		if (m_InChiKey.getBooleanValue()) {
			m_newCols.put(
					DataTableSpec.getUniqueColumnName(inSpecs[0], "InChI Key"),
					StringCell.TYPE);
		}
		if (m_InChi.getBooleanValue()) {
			m_newCols.put(
					DataTableSpec.getUniqueColumnName(inSpecs[0], "InChI"),
					InchiAdapterCell.RAW_TYPE);
		}
		if (m_Smiles.getBooleanValue()) {
			m_newCols.put(DataTableSpec.getUniqueColumnName(inSpecs[0],
					"Ligand SMILES"), SmilesAdapterCell.RAW_TYPE);
		}

		// Ensure at least one new column
		if (m_newCols.isEmpty()) {
			setWarningMessage("No properties selected!");
			throw new InvalidSettingsException("No properties selected!");
		}

		ColumnRearranger rearranger = createColumnRearranger(inSpecs[0], null);

		return new DataTableSpec[] { rearranger.createSpec() };
	}

	private ColumnRearranger createColumnRearranger(DataTableSpec dataTableSpec,
			ExecutionContext exec) {

		// The spec of the new columns to add
		DataColumnSpec[] newColsSpec = getNewColsSpec();

		ColumnRearranger rearranger = new ColumnRearranger(dataTableSpec);
		rearranger.append(new AbstractCellFactory(newColsSpec) {

			@Override
			public DataCell[] getCells(DataRow row) {
				DataCell[] result = new DataCell[newColsSpec.length];
				Arrays.fill(result, DataType.getMissingCell());
				DataCell c = row.getCell(idColIndex);
				if (c.isMissing()) {
					return result;
				}
				String hetID =
						((StringValue) c).getStringValue().trim().toUpperCase();
				if (!m_hetDetails.containsKey(hetID) && iter.hasNext()) {
					// get the next block of data...
					try {
						getHeterogenData(exec);
					} catch (CanceledExecutionException | IOException
							| InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
				JsonNode het = m_hetDetails.get(hetID);

				if (het == null) {
					// Nothing found for that hetID
					return result;
				}

				// Now we add the correct data
				int currCol = 0;
				JsonNode chemComp = het.get("chem_comp");
				if (m_type.getBooleanValue()) {
					String type =
							chemComp.has("type") ? chemComp.get("type").asText()
									: null;
					if (type != null && !type.isEmpty()) {
						result[currCol] = new StringCell(type);
					}
					currCol++;
				}
				if (m_MolWt.getBooleanValue()) {
					Double mWt = chemComp.has("formula_weight")
							? chemComp.get("formula_weight").asDouble()
							: null;
					if (mWt != null) {
						result[currCol] = new DoubleCell(mWt);
					}
					currCol++;
				}
				if (m_ChemName.getBooleanValue()) {
					String name =
							chemComp.has("name") ? chemComp.get("name").asText()
									: null;
					if (name != null && !name.isEmpty()) {
						result[currCol] = new StringCell(name);
					}
					currCol++;
				}
				if (m_Formula.getBooleanValue()) {
					String formula = chemComp.has("formula")
							? chemComp.get("formula").asText()
							: null;
					if (formula != null && !formula.isEmpty()) {
						result[currCol] = new StringCell(formula);
					}
					currCol++;
				}

				het = het.get("pdbx_chem_comp_descriptor");
				if (het == null) {
					return result;
				}
				String InChI = null;
				String InChIKey = null;
				String SMILES = null;
				for (JsonNode propNode : het) {
					switch (propNode.get("type").asText()) {
						case "InChI":
							InChI = propNode.get("descriptor").asText();
							break;
						case "InChIKey":
							InChIKey = propNode.get("descriptor").asText();
							break;
						case "SMILES_CANONICAL":
							if (SMILES == null) {
								SMILES = propNode.get("descriptor").asText();
							}
							break;
						default:
							break;
					}
				}
				if (m_InChiKey.getBooleanValue()) {
					if (InChIKey != null && !InChIKey.isEmpty()) {
						result[currCol] = new StringCell(InChIKey);
					}
					currCol++;
				}
				if (m_InChi.getBooleanValue()) {
					if (InChI != null && !InChI.isEmpty()) {
						result[currCol] =
								InchiCellFactory.createAdapterCell(InChI);
					}
					currCol++;
				}
				if (m_Smiles.getBooleanValue()) {
					if (SMILES != null && !SMILES.isEmpty()) {
						result[currCol] =
								SmilesCellFactory.createAdapterCell(SMILES);
					}
					currCol++;
				}

				return result;
			}
		});
		return rearranger;
	}

	private DataColumnSpec[] getNewColsSpec() {
		DataColumnSpec[] newSpec = new DataColumnSpec[m_newCols.size()];
		int i = 0;
		for (Entry<String, DataType> col : m_newCols.entrySet()) {
			newSpec[i++] =
					new DataColumnSpecCreator(col.getKey(), col.getValue())
							.createSpec();
		}
		return newSpec;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_ChemName.saveSettingsTo(settings);
		m_colName.saveSettingsTo(settings);
		m_Formula.saveSettingsTo(settings);
		m_InChi.saveSettingsTo(settings);
		m_InChiKey.saveSettingsTo(settings);
		m_MolWt.saveSettingsTo(settings);
		m_Smiles.saveSettingsTo(settings);
		m_type.saveSettingsTo(settings);
		pageSizeMdl.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_ChemName.loadSettingsFrom(settings);
		m_colName.loadSettingsFrom(settings);
		m_Formula.loadSettingsFrom(settings);
		m_InChi.loadSettingsFrom(settings);
		m_InChiKey.loadSettingsFrom(settings);
		m_MolWt.loadSettingsFrom(settings);
		m_Smiles.loadSettingsFrom(settings);
		m_type.loadSettingsFrom(settings);
		try {
			pageSizeMdl.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			setWarningMessage(
					"No Page Size setting saved - using default value");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_ChemName.validateSettings(settings);
		m_colName.validateSettings(settings);
		m_Formula.validateSettings(settings);
		m_InChi.validateSettings(settings);
		m_InChiKey.validateSettings(settings);
		m_MolWt.validateSettings(settings);
		m_Smiles.validateSettings(settings);
		m_type.validateSettings(settings);
		// Dont validate pageSizeMdl for backwards compatibility

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

}
