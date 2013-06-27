/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2013, Vernalis (R&D) Ltd
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ------------------------------------------------------------------------
 */
package com.vernalis.nodes.io.rcsb.source;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.knime.bio.types.PdbCell;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.data.xml.XMLCellFactory;


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
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.helpers.PDBHelperFunctions;

/**
 * This is the model implementation of RCSBmultiDownload.
 * Node to allow download of multiple RCSB PDB filetypes from a column of RCSB Structure IDs
 */
public class RCSBsDownloadNodeModel extends NodeModel {


	// the logger instance
	private static final NodeLogger logger = NodeLogger
			.getLogger(RCSBsDownloadNodeModel.class);

	//Settings Keys
	static final String CFG_PDB_ID = "PDB_ID";
	static final String CFG_PDB = "PDB";
	static final String CFG_CIF = "mmCIF";
	static final String CFG_SF = "StructureFactor";
	static final String CFG_PDBML = "PDBML";
	static final String CFG_FASTA = "FASTA";

	private final SettingsModelString m_PDBID = 
			new SettingsModelString(CFG_PDB_ID, null);

	private final static SettingsModelBoolean m_PDB = 
			new SettingsModelBoolean(CFG_PDB, true);

	private final static SettingsModelBoolean m_CIF = 
			new SettingsModelBoolean(CFG_CIF, false);

	private final static SettingsModelBoolean m_SF = 
			new SettingsModelBoolean(CFG_SF, false);

	private final static SettingsModelBoolean m_PDBML = 
			new SettingsModelBoolean(CFG_PDBML, false);

	private final static SettingsModelBoolean m_FASTA = 
			new SettingsModelBoolean(CFG_FASTA, false);

	private BufferedDataContainer m_dc;
	private int m_retrieved_ids;
	private int m_currentRowID;
	private static int m_colCnt;

	//DataTableSpec for the output table.  This will be created during the configure
	//method, once the column options have been specified
	private static DataTableSpec spec;	

	/**
	 * Constructor for the node model.
	 */

	protected RCSBsDownloadNodeModel() {
		super(0, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		String[] pdb_ids = m_PDBID.getStringValue().toUpperCase().split(";");

		//Create a datacontainer with the required columns
		m_dc = exec.createDataContainer(spec);
		m_currentRowID = 0;
		m_retrieved_ids = 0;

		for (String pdbid : pdb_ids){
			pdbid = pdbid.trim();
			addRow(pdbid,exec);
		}
		m_dc.close();
		return new BufferedDataTable[] {m_dc.getTable()};
	}

	private void addRow (final String pdbid, final ExecutionContext exec)
			throws CanceledExecutionException{
		/*
		 * Utility function to add a row to the output table
		 */
		m_retrieved_ids++;
		exec.setProgress(m_retrieved_ids + " PDB ID(s) analysed");
		exec.checkCanceled();

		DataCell[] row = new DataCell[m_colCnt];
		Arrays.fill(row, DataType.getMissingCell());
		row[0] = new StringCell(pdbid);
		int i=1;
		String r;
		if (m_PDB.getBooleanValue()){
			r = PDBHelperFunctions.readUrltoString(
					PDBHelperFunctions.createRCSBUrl(pdbid, "PDB", true));
			if (r!=null){
				row[i] = new PdbCell(r);
			}
			i ++;
		}
		if (m_CIF.getBooleanValue()){
			r = PDBHelperFunctions.readUrltoString(
					PDBHelperFunctions.createRCSBUrl(pdbid, "cif", true));
			if (r!=null){
				row[i] = new StringCell(r);
			}
			i ++;
		}
		if (m_SF.getBooleanValue()){
			r = PDBHelperFunctions.readUrltoString(
					PDBHelperFunctions.createRCSBUrl(pdbid, "sf", true));
			if (r!=null){
				row[i] = new StringCell(r);
			}
			i ++;
		}
		if (m_PDBML.getBooleanValue()){
			r = PDBHelperFunctions.readUrltoString(
					PDBHelperFunctions.createRCSBUrl(pdbid, "PDBML", true));
			if (r != null){
				try {
					row[i] = XMLCellFactory.create(r);
				} catch (Exception e){
					row[i] = new StringCell(r);
				}
			}
			i ++;
		}
		if (m_FASTA.getBooleanValue()){
			r = PDBHelperFunctions.readUrltoString(
					PDBHelperFunctions.createRCSBUrl(pdbid, "FASTA"));
			if (r != null){
				row[i] = new StringCell(r);
			}

		}
		m_dc.addRowToTable(new DefaultRow("Row "+m_currentRowID, row));
		m_currentRowID++;

	}




	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		//Start by checking the user has enters something in the PDB ID box
		//NB - No check is made that it looks like a PDB ID - a url will be built round
		//it on execute and it will succeed or fail gracefully
		if (m_PDBID.getStringValue() == null) {
			setWarningMessage("No valid PDB ID(s) entered");
			throw new InvalidSettingsException(
					"No valid PDB ID entered.");
		}
		for (String pdbid:m_PDBID.getStringValue().split(";")){
			if (pdbid.length() != 4){
				setWarningMessage("Invalid PDB ID(s) entered");
				throw new InvalidSettingsException(
						"Invalid PDB ID(s) entered");
			}
		}

		//Finally we need to find at least one property otherwise we are wasting a node!
		if(!(m_PDB.getBooleanValue() || m_CIF.getBooleanValue() || m_FASTA.getBooleanValue() ||
				m_PDBML.getBooleanValue() || m_SF.getBooleanValue())) {
			setWarningMessage("At least one format must be selected");
			throw new InvalidSettingsException("No format selected");
		}

		// everything seems to fine - create the datatable spec
		spec = new DataTableSpec(createDataColumnSpec());
		return new DataTableSpec[]{spec};
	}


	private static DataColumnSpec[] createDataColumnSpec() {
		/*
		 * Create the column specs for the output table based
		 * on the specified column selections
		 */
		m_colCnt = countCols();
		DataColumnSpec[] dcs = new DataColumnSpec[m_colCnt];
		dcs[0]=new DataColumnSpecCreator("PDB ID", StringCell.TYPE).createSpec();
		int i = 1;
		if (m_PDB.getBooleanValue()){
			dcs[i] = new DataColumnSpecCreator("PDB",
					PdbCell.TYPE).createSpec();
			i ++;
		}
		if (m_CIF.getBooleanValue()){
			dcs[i] = new DataColumnSpecCreator("CIF",
					StringCell.TYPE).createSpec();
			i ++;
		}
		if (m_SF.getBooleanValue()){
			dcs[i] = new DataColumnSpecCreator("Structure Factor",
					StringCell.TYPE).createSpec();
			i ++;
		}
		if (m_PDBML.getBooleanValue()){
			dcs[i] = new DataColumnSpecCreator("PDBML",
					XMLCell.TYPE).createSpec();
			i ++;
		}
		if (m_FASTA.getBooleanValue()){
			dcs[i] = new DataColumnSpecCreator("FASTA",
					StringCell.TYPE).createSpec();
			i ++;
		}
		return dcs;
	}

	private static int countCols(){
		//Count the new columns to add NB - We start at 1 because we always add the PDB ID as a column
		int i=1;
		i += (m_PDB.getBooleanValue()) ? 1 : 0;
		i += (m_CIF.getBooleanValue()) ? 1 : 0;
		i += (m_SF.getBooleanValue()) ? 1 : 0;
		i += (m_PDBML.getBooleanValue()) ? 1 : 0;
		i += (m_FASTA.getBooleanValue()) ? 1 : 0;
		return i;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		m_CIF.saveSettingsTo(settings);
		m_FASTA.saveSettingsTo(settings);
		m_PDB.saveSettingsTo(settings);
		m_PDBID.saveSettingsTo(settings);
		m_PDBML.saveSettingsTo(settings);
		m_SF.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		m_CIF.loadSettingsFrom(settings);
		m_FASTA.loadSettingsFrom(settings);
		m_PDB.loadSettingsFrom(settings);
		m_PDBID.loadSettingsFrom(settings);
		m_PDBML.loadSettingsFrom(settings);
		m_SF.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		m_CIF.validateSettings(settings);
		m_FASTA.validateSettings(settings);
		m_PDB.validateSettings(settings);
		m_PDBID.validateSettings(settings);
		m_PDBML.validateSettings(settings);
		m_SF.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// TODO: generated method stub
	}

}

