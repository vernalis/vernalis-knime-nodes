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
package com.vernalis.pdbconnector.nodes.describehet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.knime.chem.types.InchiCellFactory;
import org.knime.chem.types.SmilesCell;
import org.knime.chem.types.SmilesCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
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
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.pdbconnector.containers.HeterogenDetails;
import com.vernalis.rest.GetRunner;

/**
 * This is the model implementation of PdbDescribeHet. Node to retrieve
 * Heterogen details from the PDB "Describe Heterogen" webservice
 * 
 * @author S.Roughley
 */
public class PdbDescribeHetNodeModel extends NodeModel {
	/**
	 * A container class to contain the data returned for a given HetID
	 * 
	 * @author s.roughley
	 * 
	 */

	/** The node logger instance */
	static final NodeLogger logger = NodeLogger.getLogger(PdbDescribeHetNodeModel.class);

	// Settings Model Keys
	static final String HETID_COL_NAME_KEY = "Het ID Column";
	static final String TYPE_KEY = "Type";
	static final String MWT_KEY = "M_Wt";
	static final String CHEM_NAME_KEY = "Chemical Name";
	static final String FORMULA_KEY = "Formula";
	static final String INCHI_KEY_KEY = "InChI Key";
	static final String INCHI_KEY = "InChI";
	static final String SMILES_KEY = "SMILES String";
	static final String MAX_URL_LENGTH_KEY = "Max URL Length";

	/** The base URL of the webservice */
	static final String SERVICE_BASE_URL = "http://www.rcsb.org/pdb/rest/describeHet?chemicalID=";

	// The SettingsModels
	private SettingsModelString m_colName = PdbDescribeHetNodeDialog.createHetIdColumnNameModel();
	private SettingsModelBoolean m_type = PdbDescribeHetNodeDialog.createTypeModel();
	private SettingsModelBoolean m_MolWt = PdbDescribeHetNodeDialog.createMolWtModel();
	private SettingsModelBoolean m_ChemName = PdbDescribeHetNodeDialog.createChemicalNameModel();
	private SettingsModelBoolean m_Formula = PdbDescribeHetNodeDialog.createFormulaModel();
	private SettingsModelBoolean m_InChiKey = PdbDescribeHetNodeDialog.createInchiKeyModel();
	private SettingsModelBoolean m_InChi = PdbDescribeHetNodeDialog.createInchiModel();
	private SettingsModelBoolean m_Smiles = PdbDescribeHetNodeDialog.createSmilesModel();
	private SettingsModelIntegerBounded m_maxUrlLength =
			PdbDescribeHetNodeDialog.createMaxUrlLengthModel();

	private Map<String, DataType> m_newCols;// The added columns and their types
	private Map<String, HeterogenDetails> m_hetDetails; // The heterogen details

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

		ColumnRearranger rearranger = createColumnRearranger(inData[0].getSpec());
		HashSet<String> hetIds = new HashSet<>();
		int idColIndex = inData[0].getDataTableSpec().findColumnIndex(m_colName.getStringValue());

		// Parsing the input table is 10% of time
		ExecutionContext exec0 = exec.createSubExecutionContext(0.10);
		// Check for an empty table - only try to get data if we need to
		long numRows = inData[0].size();
		if (numRows != 0) {
			// Need first to populate the map of Heterogen Details
			// Start by listing them
			int rowcnt = 0;
			for (DataRow row : inData[0]) {
				DataCell cell = row.getCell(idColIndex);
				if (!cell.isMissing()) {
					hetIds.add(((StringValue) cell).getStringValue());
				}
				exec.checkCanceled();
				exec0.setProgress(((double) rowcnt / (double) numRows),
						"Gathering Het IDs - Processing row " + (rowcnt++) + " of " + rowcnt
								+ " rows. Found " + hetIds.size() + " IDs");
			}
			logger.info("Found " + hetIds.size() + " unique Heterogen IDs");

			// Now build the URLs
			exec0.setProgress(1.0, "Building query URLs");
			String url = SERVICE_BASE_URL;
			ArrayList<String> urls = new ArrayList<>();
			for (String id : hetIds) {
				url += id;
				if ((m_maxUrlLength.getIntValue() - url.length()) < 4) {
					// The URL is full, so add the URL and return it to base
					urls.add(url);
					url = SERVICE_BASE_URL;
				} else {
					url += ",";
				}
			}
			// Ensure the last URL is added, removing the trailing ,
			if (!urls.contains(url) && !SERVICE_BASE_URL.equals(url)) {
				url = url.substring(0, url.length() - 1);
				urls.add(url);
			}

			// And now actually get the data...
			m_hetDetails = new HashMap<>();
			int urlCounter = 0;
			for (String url1 : urls) {
				exec0 = exec.createSubExecutionContext(0.65 / urls.size()); // Running
																			// the
																			// queries
																			// is
																			// 65%
																			// of
																			// time
				exec0.setProgress((double) urlCounter / (double) urls.size(),
						"Sending query batch " + (urlCounter++) + " of " + urls.size());
				m_hetDetails.putAll(getHeterogenData(url1, exec0));
			}
		}

		// Now generate the output table - 25% of time
		exec0 = exec.createSubExecutionContext(0.25);
		return new BufferedDataTable[] {
				exec.createColumnRearrangeTable(inData[0], rearranger, exec0) };
	}

	/**
	 * Actually get the heterogen data from the webservice. The calls are made
	 * in a separate thread
	 * 
	 * @param url
	 *            The URL containing the Service GET request
	 * @param exec
	 *            The execution context to allow cancellation
	 * @return A Map keyed on the Het ID
	 * @throws Exception
	 */
	private Map<String, HeterogenDetails> getHeterogenData(final String url,
			final ExecutionContext exec) throws Exception {

		// Increasing delays in seconds - we start with 0 in case the url
		// timeout is a sufficient delay
		int[] retryDelays = { 0, 1, 5, 10, 30, 60, 300, 600 };

		// The return object
		HashMap<String, HeterogenDetails> dataReturn = new HashMap<>();
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
				// lines = (ArrayList<String>) RestClient.getResultList(new
				// URL(url));
				lines = future.get();
				break;
			} catch (IOException e) {
				// Wait for an increasing period before retrying
				logger.warn("GET request failed for data block - Waiting " + delay
						+ " seconds before re-trying...");
				exec.checkCanceled();
				pause(delay, exec);
			}
		}
		if (lines == null) {
			// In this case, we never managed to contact the server...
			String errMsg = "Unable to contact the remote server - please try again later!";
			logger.error(errMsg);
			throw new IOException(errMsg);
		}

		// Now we need to parse the lines into records
		final String RECORD_START = "<ligand ";
		final String RECORD_END = "</ligand>";

		String currentRecord = "";
		boolean inRec = false;
		int numRecords = (url.length() - SERVICE_BASE_URL.length()) / 4;
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
				HeterogenDetails het = new HeterogenDetails(currentRecord);
				dataReturn.put(het.getID(), het);
				// xmls.add(currentRecord);
				currentRecord = "";
				exec.setProgress((double) dataReturn.size() / (double) numRecords,
						"Processed " + dataReturn.size() + " of " + numRecords);
			} else if (inRec) {
				currentRecord += line;
			}
		}

		return dataReturn;
	}

	private static void pause(int seconds, ExecutionMonitor exec) throws Exception {
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
						throw new InvalidSettingsException("No ID column selected.");
					}
					colIndex = i;
				}
				i++;
			}

			if (colIndex == -1) {
				setWarningMessage("No ID column selected");
				throw new InvalidSettingsException("No ID column selected.");
			}
			m_colName.setStringValue(inSpecs[0].getColumnSpec(colIndex).getName());
			setWarningMessage(
					"Column '" + m_colName.getStringValue() + "' auto selected for ID column");
		} else {
			colIndex = inSpecs[0].findColumnIndex(m_colName.getStringValue());
			if (colIndex < 0) {
				setWarningMessage("No such column: " + m_colName.getStringValue());
				throw new InvalidSettingsException("No such column: " + m_colName.getStringValue());
			}

			DataColumnSpec colSpec = inSpecs[0].getColumnSpec(colIndex);
			if (!colSpec.getType().isCompatible(StringValue.class)) {
				setWarningMessage("Column \"" + m_colName.getStringValue()
						+ "\" does not contain string values");
				throw new InvalidSettingsException("Column \"" + m_colName
						+ "\" does not contain string values: " + colSpec.getType().toString());
			}
		}

		// Now we need to decide which columns are added
		m_newCols = new LinkedHashMap<>();
		if (m_type.getBooleanValue()) {
			m_newCols.put(DataTableSpec.getUniqueColumnName(inSpecs[0], "Type"), StringCell.TYPE);
		}
		if (m_MolWt.getBooleanValue()) {
			m_newCols.put(DataTableSpec.getUniqueColumnName(inSpecs[0], "M. Wt."), DoubleCell.TYPE);
		}
		if (m_ChemName.getBooleanValue()) {
			m_newCols.put(DataTableSpec.getUniqueColumnName(inSpecs[0], "Chemical Name"),
					StringCell.TYPE);
		}
		if (m_Formula.getBooleanValue()) {
			m_newCols.put(DataTableSpec.getUniqueColumnName(inSpecs[0], "Chemical Formula"),
					StringCell.TYPE);
		}
		if (m_InChiKey.getBooleanValue()) {
			m_newCols.put(DataTableSpec.getUniqueColumnName(inSpecs[0], "InChI Key"),
					StringCell.TYPE);
		}
		if (m_InChi.getBooleanValue()) {
			m_newCols.put(DataTableSpec.getUniqueColumnName(inSpecs[0], "InChI"),
					InchiCellFactory.TYPE);
		}
		if (m_Smiles.getBooleanValue()) {
			m_newCols.put(DataTableSpec.getUniqueColumnName(inSpecs[0], "Ligand SMILES"),
					SmilesCell.TYPE);
		}

		// Ensure at least one new column
		if (m_newCols.isEmpty()) {
			setWarningMessage("No properties selected!");
			throw new InvalidSettingsException("No properties selected!");
		}

		ColumnRearranger rearranger = createColumnRearranger(inSpecs[0]);

		return new DataTableSpec[] { rearranger.createSpec() };
	}

	private ColumnRearranger createColumnRearranger(DataTableSpec dataTableSpec) {

		// The column index of the Het Id
		final int hetIdIndex = dataTableSpec.findColumnIndex(m_colName.getStringValue());
		final int newColcnt = m_newCols.size();

		// The spec of the new columns to add
		DataColumnSpec[] newColsSpec = getNewColsSpec();

		ColumnRearranger rearranger = new ColumnRearranger(dataTableSpec);
		rearranger.append(new AbstractCellFactory(newColsSpec) {

			@Override
			public DataCell[] getCells(DataRow row) {
				DataCell[] result = new DataCell[newColcnt];
				Arrays.fill(result, DataType.getMissingCell());
				DataCell c = row.getCell(hetIdIndex);
				if (c.isMissing()) {
					return result;
				}
				String hetID = ((StringValue) c).getStringValue().toUpperCase();
				HeterogenDetails het = m_hetDetails.get(hetID);
				if (het == null) {
					// Nothing found for that hetID
					return result;
				}

				// Now we add the correct data
				int currCol = 0;
				if (m_type.getBooleanValue()) {
					if (het.getType() != null && !"".equals(het.getType())) {
						result[currCol] = new StringCell(het.getType());
					}
					currCol++;
				}
				if (m_MolWt.getBooleanValue()) {
					if (het.getMWt() != null) {
						result[currCol] = new DoubleCell(het.getMWt());
					}
					currCol++;
				}
				if (m_ChemName.getBooleanValue()) {
					if (het.getChemName() != null && !"".equals(het.getChemName())) {
						result[currCol] = new StringCell(het.getChemName());
					}
					currCol++;
				}
				if (m_Formula.getBooleanValue()) {
					if (het.getFormula() != null && !"".equals(het.getFormula())) {
						result[currCol] = new StringCell(het.getFormula());
					}
					currCol++;
				}
				if (m_InChiKey.getBooleanValue()) {
					if (het.getInChiKey() != null && !"".equals(het.getInChiKey())) {
						result[currCol] = new StringCell(het.getInChiKey());
					}
					currCol++;
				}
				if (m_InChi.getBooleanValue()) {
					if (het.getInChi() != null && !"".equals(het.getInChi())) {
						result[currCol] = InchiCellFactory.create(het.getInChi());
					}
					currCol++;
				}
				if (m_Smiles.getBooleanValue()) {
					if (het.getSmiles() != null && !"".equals(het.getSmiles())) {
						result[currCol] = SmilesCellFactory.createAdapterCell(het.getSmiles());
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
			newSpec[i++] = new DataColumnSpecCreator(col.getKey(), col.getValue()).createSpec();
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
		m_maxUrlLength.saveSettingsTo(settings);
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
		m_maxUrlLength.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_ChemName.validateSettings(settings);
		m_colName.validateSettings(settings);
		m_Formula.validateSettings(settings);
		m_InChi.validateSettings(settings);
		m_InChiKey.validateSettings(settings);
		m_MolWt.validateSettings(settings);
		m_Smiles.validateSettings(settings);
		m_type.validateSettings(settings);
		m_maxUrlLength.validateSettings(settings);
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
