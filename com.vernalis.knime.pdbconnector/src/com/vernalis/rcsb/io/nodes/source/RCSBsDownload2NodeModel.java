/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.rcsb.io.nodes.source;

import static com.vernalis.rcsb.io.nodes.source.RCSBsDownload2NodeDialog.createFiletypesModel;
import static com.vernalis.rcsb.io.nodes.source.RCSBsDownload2NodeDialog.createPDBIDsModel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DefaultRow;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;

import com.vernalis.io.FileDownloadException;
import com.vernalis.io.FileHelpers;
import com.vernalis.rcsb.io.helpers.RCSBFileTypes;

/**
 * This is the model implementation of RCSBmultiDownload. Node to allow download
 * of multiple RCSB PDB filetypes from a column of RCSB Structure IDs
 */
public class RCSBsDownload2NodeModel extends NodeModel {

	// the logger instance
	private static final NodeLogger logger = NodeLogger.getLogger(RCSBsDownloadNodeModel.class);

	private final SettingsModelString m_PDBID = createPDBIDsModel();
	private final SettingsModelStringArray m_fTypes = createFiletypesModel();

	private RCSBFileTypes[] fTypes;

	// DataTableSpec for the output table. This will be created during the
	// configure
	// method, once the column options have been specified
	private static DataTableSpec spec;

	/**
	 * Constructor for the node model.
	 */

	protected RCSBsDownload2NodeModel() {
		super(0, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		BufferedDataTableRowOutput output =
				new BufferedDataTableRowOutput(exec.createDataContainer(spec));
		createStreamableOperator(null, null).runFinal(new PortInput[0], new PortOutput[] { output },
				exec);
		return new BufferedDataTable[] { output.getDataTable() };
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
				fTypes = Arrays.stream(m_fTypes.getStringArrayValue()).map(x -> x.replace(" ", "_"))
						.map(x -> RCSBFileTypes.valueOf(x)).toArray(x -> new RCSBFileTypes[x]);
				String[] pdb_ids = m_PDBID.getStringValue().toUpperCase().split(";");
				RowOutput out = (RowOutput) outputs[0];
				long currentRowID = 0;
				for (String pdbid : pdb_ids) {
					exec.setProgress(currentRowID + " PDB ID(s) analysed");
					exec.checkCanceled();
					pdbid = pdbid.trim();
					out.push(buildRow(pdbid, currentRowID++, exec));
				}
				out.close();
			}
		};
	}

	private DataRow buildRow(final String pdbid, long currentRowID, final ExecutionContext exec)
			throws CanceledExecutionException, FileDownloadException {
		/*
		 * Utility function to add a row to the output table
		 */

		DataCell[] row = new DataCell[fTypes.length + 1];
		Arrays.fill(row, DataType.getMissingCell());
		row[0] = new StringCell(pdbid);
		for (int i = 0; i < fTypes.length; i++) {
			try {
				String r = FileHelpers.readURLToString(fTypes[i].getURL(pdbid));
				row[i + 1] = fTypes[i].getCellFromContent(r);
			} catch (FileDownloadException e) {
				logger.info(e.getMessage());
			}
		}
		return new DefaultRow("Row " + currentRowID, row);

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

		// Start by checking the user has enters something in the PDB ID box
		// NB - No check is made that it looks like a PDB ID - a url will be
		// built round
		// it on execute and it will succeed or fail gracefully
		if (m_PDBID.getStringValue() == null) {
			setWarningMessage("No valid PDB ID(s) entered");
			throw new InvalidSettingsException("No valid PDB ID entered.");
		}
		for (String pdbid : m_PDBID.getStringValue().split(";")) {
			if (pdbid.length() != 4) {
				setWarningMessage("Invalid PDB ID(s) entered");
				throw new InvalidSettingsException("Invalid PDB ID(s) entered");
			}
		}

		// everything seems to fine - create the datatable spec
		spec = new DataTableSpec(createDataColumnSpec());
		return new DataTableSpec[] { spec };
	}

	private DataColumnSpec[] createDataColumnSpec() {
		fTypes = Arrays.stream(m_fTypes.getStringArrayValue()).map(x -> x.replace(" ", "_"))
				.map(x -> RCSBFileTypes.valueOf(x)).toArray(x -> new RCSBFileTypes[x]);
		DataColumnSpec[] dcs = new DataColumnSpec[fTypes.length + 1];
		dcs[0] = new DataColumnSpecCreator("PDB ID", StringCell.TYPE).createSpec();
		for (int i = 0; i < fTypes.length; i++) {
			dcs[i + 1] = new DataColumnSpecCreator(fTypes[i].getName(), fTypes[i].getOutputType())
					.createSpec();
		}
		return dcs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_PDBID.saveSettingsTo(settings);
		m_fTypes.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_PDBID.loadSettingsFrom(settings);
		m_fTypes.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_PDBID.validateSettings(settings);
		m_fTypes.validateSettings(settings);
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
