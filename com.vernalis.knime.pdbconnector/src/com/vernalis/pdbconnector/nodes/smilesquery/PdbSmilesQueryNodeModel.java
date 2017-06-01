/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector.nodes.smilesquery;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.knime.chem.types.InchiCellFactory;
import org.knime.chem.types.SmilesCell;
import org.knime.chem.types.SmilesCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;

import com.vernalis.pdbconnector.containers.HeterogenStructureDetails;
import com.vernalis.rest.GetRunner;

;

/**
 * This is the model implementation of PdbSmilesQuery. Node to perform SMILES
 * string queries using the PDB smilesQuery webservice
 * 
 * @author S.Roughley
 */
public class PdbSmilesQueryNodeModel extends NodeModel {

	/** The node logger instance */
	static final NodeLogger logger = NodeLogger.getLogger(PdbSmilesQueryNodeModel.class);

	// Settings Model Keys
	static final String SMILES_STRING_KEY = "SMILES Query String";
	static final String QUERY_TYPE_KEY = "Query Type";
	static final String SIMILARITY_KEY = "Similarity";
	static final String TYPE_KEY = "Type";
	static final String MWT_KEY = "M_Wt";
	static final String CHEM_NAME_KEY = "Chemical Name";
	static final String FORMULA_KEY = "Formula";
	static final String INCHI_KEY_KEY = "InChI Key";
	static final String INCHI_KEY = "InChI";
	static final String SMILES_KEY = "SMILES String";

	/** The base URL of the webservice */
	static final String SERVICE_BASE_URL = "//www.rcsb.org/pdb/rest/smilesQuery";
	static final String SMILES_PREFIX = "smiles=";
	static final String SEARCH_TYPE_PREFIX = "&search_type=";
	static final String SIMILARITY_PREFIX = "&similarity=";

	/**
	 * The possible query type options. NB These need #toLowerCase() in the URL
	 */
	static final String[] QUERY_TYPES = { "Exact", "Substructure", "Superstructure", "Similarity" };

	// The SettingsModels
	private SettingsModelString m_SmilesQuery = PdbSmilesQueryNodeDialog.createSmilesQueryModel();
	private SettingsModelDoubleBounded m_Similarity = PdbSmilesQueryNodeDialog
			.createSimilarityQueryModel();
	private SettingsModelString m_QueryType = PdbSmilesQueryNodeDialog.createQueryTypeModel();
	private SettingsModelBoolean m_type = PdbSmilesQueryNodeDialog.createTypeModel();
	private SettingsModelBoolean m_MolWt = PdbSmilesQueryNodeDialog.createMolWtModel();
	private SettingsModelBoolean m_ChemName = PdbSmilesQueryNodeDialog.createChemicalNameModel();
	private SettingsModelBoolean m_Formula = PdbSmilesQueryNodeDialog.createFormulaModel();
	private SettingsModelBoolean m_InChiKey = PdbSmilesQueryNodeDialog.createInchiKeyModel();
	private SettingsModelBoolean m_InChi = PdbSmilesQueryNodeDialog.createInchiModel();
	private SettingsModelBoolean m_Smiles = PdbSmilesQueryNodeDialog.createSmilesModel();

	private Map<String, DataType> m_newCols;// The added columns and their types

	// The het-structure pair details. Only need an ArrayList as no lookup of
	// key required
	// private ArrayList<HeterogenStructureDetails> m_hetDetails;

	/**
	 * Constructor for the node model.
	 */
	protected PdbSmilesQueryNodeModel() {

		super(0, 3);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		// // First we need to perform the query
		// // URL is built (NB the SMILES needs "cleaned" of special characters)
		// // Query run and results processed and then tables written
		//
		// // Build the URL - use the URI constructor to clean the SMILES string
		// String url = (new URI("http", null, SERVICE_BASE_URL,
		// getQueryString(), null)).toString();
		// logger.info("Query URL: " + url);
		//
		// // Sets for the IDs - we use TreeSets to have sorted IDs
		// Set<String> hetIDs = new TreeSet<String>();
		// Set<String> pdbIDs = new TreeSet<String>();
		//
		// exec.setMessage("Running Query");
		// // Running the query is 50% of time
		// ExecutionMonitor exec0 = exec.createSubProgress(0.50);
		// exec0.setProgress(0.0);
		// ArrayList<HeterogenStructureDetails> hetDetails =
		// getHeterogenData(url, exec0);
		// Collections.sort(hetDetails);
		// exec0.setProgress(1.0, "Query phase Complete");
		// logger.info("Query returned " + hetDetails.size() + " hits");
		//
		// // Write the first (main) table - 30% of time
		// exec0 = exec.createSubProgress(0.3);
		// // Container for the 0th table - the full results table
		// BufferedDataContainer cont0 = exec.createDataContainer(getSpec0());
		// long row = 0;
		// long numRows = hetDetails.size();
		// double progPerRow = 1.0 / numRows;
		//
		// exec.setMessage("Writing 1st output Table");
		// for (HeterogenStructureDetails entry : hetDetails) {
		// RowKey rowKey = RowKey.createRowKey(row);
		// exec0.checkCanceled();
		// exec0.setProgress(row * progPerRow, "Row " + (++row) + " of " +
		// numRows);
		// DefaultRow newRow = createDataRow(rowKey, entry);
		// cont0.addRowToTable(newRow);
		//
		// // While we are here, extract the hetID and structureId for the
		// // other table
		// hetIDs.add(entry.getHetID());
		// pdbIDs.add(entry.getStructureID());
		// }
		//
		// // Now write the 1st table
		// cont0.close();
		// BufferedDataTable out0 = cont0.getTable();
		//
		// // Now the second table - 10% of time
		// exec0 = exec.createSubProgress(0.1);
		// BufferedDataContainer cont1 = exec.createDataContainer(getSpec1());
		// row = 0;
		// numRows = hetIDs.size();
		// progPerRow = 1.0 / numRows;
		//
		// exec.setMessage("Writing 2nd output Table");
		// logger.info(numRows + " Unique matching chemical components found");
		// for (String entry : hetIDs) {
		// RowKey rowKey = RowKey.createRowKey(row);
		// exec0.checkCanceled();
		// exec0.setProgress(row * progPerRow, "Row " + (++row) + " of " +
		// numRows);
		// cont1.addRowToTable(new DefaultRow(rowKey, new DataCell[] { new
		// StringCell(entry) }));
		// }
		//
		// // Now write the 2nd table
		// cont1.close();
		// BufferedDataTable out1 = cont1.getTable();
		//
		// // Now the third table - 10% of time
		// exec0 = exec.createSubProgress(0.1);
		// BufferedDataContainer cont2 = exec.createDataContainer(getSpec2());
		// row = 0;
		// numRows = pdbIDs.size();
		// progPerRow = 1.0 / numRows;
		//
		// exec.setMessage("Writing 3rd output Table");
		// logger.info(numRows + " Structures containing the matching chemical
		// components returned");
		// for (String entry : pdbIDs) {
		// RowKey rowKey = RowKey.createRowKey(row);
		// exec0.checkCanceled();
		// exec0.setProgress(row * progPerRow, "Row " + (++row) + " of " +
		// numRows);
		// cont2.addRowToTable(new DefaultRow(rowKey, new DataCell[] { new
		// StringCell(entry) }));
		// }
		//
		// // Now write the 3rd table
		// cont2.close();
		// BufferedDataTable out2 = cont2.getTable();
		BufferedDataTableRowOutput out0 = new BufferedDataTableRowOutput(
				exec.createDataContainer(getSpec0()));
		BufferedDataTableRowOutput out1 = new BufferedDataTableRowOutput(
				exec.createDataContainer(getSpec1()));
		BufferedDataTableRowOutput out2 = new BufferedDataTableRowOutput(
				exec.createDataContainer(getSpec2()));
		createStreamableOperator(null, null).runFinal(new PortInput[0],
				new PortOutput[] { out0, out1, out2 }, exec);
		return new BufferedDataTable[] { out0.getDataTable(), out1.getDataTable(),
				out2.getDataTable() };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#createStreamableOperator(org.knime.core.
	 * node.streamable.PartitionInfo, org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	public StreamableOperator createStreamableOperator(PartitionInfo partitionInfo,
			PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		return new StreamableOperator() {

			@Override
			public void runFinal(PortInput[] inputs, PortOutput[] outputs, ExecutionContext exec)
					throws Exception {
				// First we need to perform the query
				// URL is built (NB the SMILES needs "cleaned" of special
				// characters)
				// Query run and results processed and then tables written

				// Build the URL - use the URI constructor to clean the SMILES
				// string
				String url = (new URI("http", null, SERVICE_BASE_URL, getQueryString(), null))
						.toString();
				logger.info("Query URL: " + url);

				// Sets for the IDs - we use TreeSets to have sorted IDs
				Set<String> hetIDs = new TreeSet<String>();
				Set<String> pdbIDs = new TreeSet<String>();

				exec.setMessage("Running Query");
				// Running the query is 50% of time
				ExecutionMonitor exec0 = exec.createSubProgress(0.50);
				exec0.setProgress(0.0);
				ArrayList<HeterogenStructureDetails> hetDetails = getHeterogenData(url, exec0);
				Collections.sort(hetDetails);
				exec0.setProgress(1.0, "Query phase Complete");
				logger.info("Query returned " + hetDetails.size() + " hits");

				// Write the first (main) table - 30% of time
				exec0 = exec.createSubProgress(0.3);
				// Container for the 0th table - the full results table
				RowOutput cont0 = (RowOutput) outputs[0];
				long row = 0;
				long numRows = hetDetails.size();
				double progPerRow = 1.0 / numRows;

				exec.setMessage("Writing 1st output Table");
				for (HeterogenStructureDetails entry : hetDetails) {
					RowKey rowKey = RowKey.createRowKey(row);
					exec0.checkCanceled();
					exec0.setProgress(row * progPerRow, "Row " + (++row) + " of " + numRows);
					DefaultRow newRow = createDataRow(rowKey, entry);
					cont0.push(newRow);

					// While we are here, extract the hetID and structureId for
					// the
					// other table
					hetIDs.add(entry.getHetID());
					pdbIDs.add(entry.getStructureID());
				}
				cont0.close();

				// Now the second table - 10% of time
				exec0 = exec.createSubProgress(0.1);
				RowOutput cont1 = (RowOutput) outputs[1];
				row = 0;
				numRows = hetIDs.size();
				progPerRow = 1.0 / numRows;

				exec.setMessage("Writing 2nd output Table");
				logger.info(numRows + " Unique matching chemical components found");
				for (String entry : hetIDs) {
					RowKey rowKey = RowKey.createRowKey(row);
					exec0.checkCanceled();
					exec0.setProgress(row * progPerRow, "Row " + (++row) + " of " + numRows);
					cont1.push(new DefaultRow(rowKey, new DataCell[] { new StringCell(entry) }));
				}

				cont1.close();

				// Now the third table - 10% of time
				exec0 = exec.createSubProgress(0.1);
				RowOutput cont2 = (RowOutput) outputs[2];
				row = 0;
				numRows = pdbIDs.size();
				progPerRow = 1.0 / numRows;

				exec.setMessage("Writing 3rd output Table");
				logger.info(numRows
						+ " Structures containing the matching chemical components returned");
				for (String entry : pdbIDs) {
					RowKey rowKey = RowKey.createRowKey(row);
					exec0.checkCanceled();
					exec0.setProgress(row * progPerRow, "Row " + (++row) + " of " + numRows);
					cont2.push(new DefaultRow(rowKey, new DataCell[] { new StringCell(entry) }));
				}

				// Now write the 3rd table
				cont2.close();
			}
		};

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#getOutputPortRoles()
	 */
	@Override
	public OutputPortRole[] getOutputPortRoles() {
		OutputPortRole[] retVal = new OutputPortRole[3];
		Arrays.fill(retVal, OutputPortRole.DISTRIBUTED);
		return retVal;
	}

	private DefaultRow createDataRow(RowKey rowKey, HeterogenStructureDetails entry) {
		// There are always the Heterogen and Structure ID columns
		DataCell[] cells = new DataCell[m_newCols.size()];
		Arrays.fill(cells, DataType.getMissingCell());
		int i = 0;
		String val = entry.getHetID();
		cells[i] = (!"".equals(val) && val != null) ? new StringCell(val) : cells[i];
		i++;
		val = entry.getStructureID();
		cells[i] = (!"".equals(val) && val != null) ? new StringCell(val) : cells[i];
		i++;
		if (m_type.getBooleanValue()) {
			val = entry.getType();
			cells[i] = (!"".equals(val) && val != null) ? new StringCell(val) : cells[i];
			i++;
		}
		if (m_MolWt.getBooleanValue()) {
			Double valDbl = entry.getMWt();
			cells[i] = (valDbl != null) ? new DoubleCell(valDbl) : cells[i];
			i++;
		}
		if (m_ChemName.getBooleanValue()) {
			val = entry.getChemName();
			cells[i] = (!"".equals(val) && val != null) ? new StringCell(val) : cells[i];
			i++;
		}
		if (m_Formula.getBooleanValue()) {
			val = entry.getFormula();
			cells[i] = (!"".equals(val) && val != null) ? new StringCell(val) : cells[i];
			i++;
		}
		if (m_InChiKey.getBooleanValue()) {
			val = entry.getInChiKey();
			cells[i] = (!"".equals(val) && val != null) ? new StringCell(val) : cells[i];
			i++;
		}
		if (m_InChi.getBooleanValue()) {
			val = entry.getInChi();
			cells[i] = (!"".equals(val) && val != null) ? InchiCellFactory.create(val) : cells[i];
			i++;
		}
		if (m_Smiles.getBooleanValue()) {
			val = entry.getSmiles();
			cells[i] = (!"".equals(val) && val != null) ? SmilesCellFactory.createAdapterCell(val)
					: cells[i];
			i++;
		}
		return new DefaultRow(rowKey, cells);
	}

	/**
	 * Utility function to construct the 'query' portion of the URL as a string
	 * 
	 * @return
	 */
	private String getQueryString() {
		StringBuilder sb = new StringBuilder(SMILES_PREFIX);
		sb.append(m_SmilesQuery.getStringValue());
		sb.append(SEARCH_TYPE_PREFIX);
		sb.append(m_QueryType.getStringValue().toLowerCase());
		if (m_QueryType.getStringValue().toLowerCase().equals("similarity")) {
			sb.append(SIMILARITY_PREFIX);
			sb.append(m_Similarity.getDoubleValue());
		}
		return sb.toString();
	}

	/**
	 * Run the service Call. Calls are sent in a separate thread
	 * 
	 * @param url
	 *            The URL of the GET request
	 * @param exec
	 *            The {@link ExecutionContext} to allow cancellation
	 * @return A list of {@link HeterogenStructureDetails} for the query result
	 * @throws Exception
	 */
	private ArrayList<HeterogenStructureDetails> getHeterogenData(final String url,
			final ExecutionMonitor exec) throws Exception {

		// Increasing delays in seconds - we start with 0 in case the url
		// timeout is a sufficient delay
		int[] retryDelays = { 0, 1, 5, 10, 30, 60, 300, 600 };

		// The return object
		ArrayList<HeterogenStructureDetails> dataReturn = new ArrayList<HeterogenStructureDetails>();
		// Another Arraylist to store the individual lines
		List<String> lines = null;

		for (int delay : retryDelays) {
			try {
				// Now send the request in a separate thread, waiting for it to
				// complete
				ExecutorService pool = Executors.newSingleThreadExecutor();
				Future<List<String>> future = pool.submit(new GetRunner(new URL(url)));
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
				lines = future.get();
				break;
			} catch (IOException e) {
				// Wait for an increasing period before retrying
				if ("Read timed out".equals(e.getMessage())) {
					// This is the robot checker or just poor connection - so
					// wait a bit and retry
					logger.warn("GET request failed for data block - Waiting " + delay
							+ " seconds before re-trying...");
					exec.checkCanceled();
					pause(delay, exec);
				} else {
					// We will assume this is the server exception error, and
					// fail execution
					logger.error("Server unable to interpret query");
					throw new Exception("Server unable to interpret query");
				}
			}
		}
		if (lines == null) {
			// In this case, we never managed to contact the server...
			String errMsg = "Unable to contact the remote server - please try again later!";
			logger.error(errMsg);
			throw new IOException(errMsg);
		}

		exec.setProgress(0.5, "Processing Raw Results");

		// Now we need to parse the lines into records
		final String RECORD_START = "<ligand ";
		final String RECORD_END = "</ligand>";

		String currentRecord = "";
		boolean inRec = false;
		for (String line : lines) {
			exec.checkCanceled();

			line = line.trim();
			if (line.startsWith(RECORD_START)) {
				// New Record
				inRec = true;
				currentRecord = line;
			} else if (line.startsWith(RECORD_END)) {
				// End of record
				// Set flag back to false, complete record
				// and add to results
				inRec = false;
				currentRecord += line;
				HeterogenStructureDetails het = new HeterogenStructureDetails(currentRecord);
				dataReturn.add(het);
				currentRecord = "";
			} else if (inRec) {
				currentRecord += line;
			}
		}

		return dataReturn;
	}

	private static void pause(int seconds, ExecutionMonitor exec1) throws Exception {
		// simple delay function without using threads
		Date start = new Date();
		while (new Date().getTime() - start.getTime() < seconds * 1000) {
			exec1.checkCanceled();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		// Check there is a SMILES String - no validation!
		if (m_SmilesQuery.getStringValue() == null || "".equals(m_SmilesQuery.getStringValue())) {
			logger.warn("No SMILES query string entered");
			throw new InvalidSettingsException("No SMILES query string entered");
		}

		// Now we need to decide which columns are added
		m_newCols = new LinkedHashMap<String, DataType>();
		m_newCols.put("Ligand ID", StringCell.TYPE);
		m_newCols.put("Structure ID", StringCell.TYPE);
		if (m_type.getBooleanValue()) {
			m_newCols.put("Type", StringCell.TYPE);
		}
		if (m_MolWt.getBooleanValue()) {
			m_newCols.put("M. Wt.", DoubleCell.TYPE);
		}
		if (m_ChemName.getBooleanValue()) {
			m_newCols.put("Chemical Name", StringCell.TYPE);
		}
		if (m_Formula.getBooleanValue()) {
			m_newCols.put("Chemical Formula", StringCell.TYPE);
		}
		if (m_InChiKey.getBooleanValue()) {
			m_newCols.put("InChI Key", StringCell.TYPE);
		}
		if (m_InChi.getBooleanValue()) {
			m_newCols.put("InChI", InchiCellFactory.TYPE);
		}
		if (m_Smiles.getBooleanValue()) {
			m_newCols.put("Ligand SMILES", SmilesCell.TYPE);
		}

		return new DataTableSpec[] { getSpec0(), getSpec1(), getSpec2() };
	}

	/**
	 * Create the data spec for the table listing the structure-ligand pairs
	 * 
	 * @return
	 */
	private DataTableSpec getSpec0() {
		DataColumnSpec[] newSpec = new DataColumnSpec[m_newCols.size()];
		int i = 0;
		for (Entry<String, DataType> col : m_newCols.entrySet()) {
			newSpec[i++] = new DataColumnSpecCreator(col.getKey(), col.getValue()).createSpec();
		}
		return new DataTableSpec(newSpec);
	}

	/**
	 * Create the dataspec for the table listing the ligand IDs
	 * 
	 * @return
	 */
	private DataTableSpec getSpec1() {
		DataColumnSpec[] newSpec = new DataColumnSpec[1];
		newSpec[0] = new DataColumnSpecCreator("Ligand ID", StringCell.TYPE).createSpec();
		return new DataTableSpec(newSpec);
	}

	/**
	 * Create the data spec for the table listing the structure IDs
	 * 
	 * @return
	 */
	private DataTableSpec getSpec2() {
		DataColumnSpec[] newSpec = new DataColumnSpec[1];
		newSpec[0] = new DataColumnSpecCreator("Structure ID", StringCell.TYPE).createSpec();
		return new DataTableSpec(newSpec);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_SmilesQuery.saveSettingsTo(settings);
		m_QueryType.saveSettingsTo(settings);
		m_Similarity.saveSettingsTo(settings);
		m_ChemName.saveSettingsTo(settings);
		m_Formula.saveSettingsTo(settings);
		m_InChi.saveSettingsTo(settings);
		m_InChiKey.saveSettingsTo(settings);
		m_MolWt.saveSettingsTo(settings);
		m_Smiles.saveSettingsTo(settings);
		m_type.saveSettingsTo(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_SmilesQuery.loadSettingsFrom(settings);
		m_QueryType.loadSettingsFrom(settings);
		m_Similarity.loadSettingsFrom(settings);
		m_ChemName.loadSettingsFrom(settings);
		m_Formula.loadSettingsFrom(settings);
		m_InChi.loadSettingsFrom(settings);
		m_InChiKey.loadSettingsFrom(settings);
		m_MolWt.loadSettingsFrom(settings);
		m_Smiles.loadSettingsFrom(settings);
		m_type.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_SmilesQuery.validateSettings(settings);
		m_QueryType.validateSettings(settings);
		m_Similarity.validateSettings(settings);
		m_ChemName.validateSettings(settings);
		m_Formula.validateSettings(settings);
		m_InChi.validateSettings(settings);
		m_InChiKey.validateSettings(settings);
		m_MolWt.validateSettings(settings);
		m_Smiles.validateSettings(settings);
		m_type.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

}
