/*******************************************************************************
 * Copyright (c) 2014, 2015, Vernalis (R&D) Ltd
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
 *******************************************************************************/
package com.vernalis.knime.mmp.nodes.uniquifyids;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
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

/**
 * {@link NodeModel} for the Uniquify IDs node
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class UniquifyIdsNodeModel extends NodeModel {

	private final SettingsModelString m_IDColName = UniquifyIdsNodeDialog
			.createIDModel();

	Map<String, Long> IDsMap;

	/** The NodeLogger Instance */
	static final NodeLogger m_logger = NodeLogger
			.getLogger(UniquifyIdsNodeModel.class);

	/**
	 * Constructor for the node model class
	 */
	public UniquifyIdsNodeModel() {
		super(1, 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#execute(org.knime.core.node.BufferedDataTable
	 * [], org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {

		return new BufferedDataTable[] { exec.createColumnRearrangeTable(
				inData[0],
				createColumnRearranger(inData[0].getDataTableSpec()), exec) };
	}

	/**
	 * Method to create column rearranger to replace the ID column with
	 * uniquified IDs
	 * 
	 * @param dataTableSpec
	 *            The incoming table spec
	 * @return The Column Rearranger to uniquify the IDs
	 */
	private ColumnRearranger createColumnRearranger(DataTableSpec dataTableSpec) {
		ColumnRearranger rearranger = new ColumnRearranger(dataTableSpec);
		final int idColIdx = dataTableSpec.findColumnIndex(m_IDColName
				.getStringValue());
		// NB No point in concurrent execution as we need a concurrent hashmap
		// of atomic integers for the uniquify process, and little gain
		rearranger.replace(
				new SingleCellFactory(false, dataTableSpec
						.getColumnSpec(idColIdx)) {

					@Override
					public DataCell getCell(DataRow row) {
						DataCell IDCell = row.getCell(idColIdx);
						if (IDCell.isMissing()) {
							return IDCell;
						}
						String ID = ((StringValue) IDCell).getStringValue();
						if (!IDsMap.containsKey(ID)) {
							IDsMap.put(ID, 0L);
							return IDCell;
						}
						// Duplicate ID
						ID = uniquifyID(ID);
						return new StringCell(ID);
					}
				}, m_IDColName.getStringValue());
		return rearranger;
	}

	/**
	 * Recursively uniquify an ID. If the ID does not exist, it is added to the
	 * {@link Map} with a count of 0 and returned. Otherwise, it is suffixed
	 * with _[count], and the counter is incremented. The newly generated ID is
	 * then resubmitted, until novely is achieved.
	 * 
	 * @param ID
	 * @return A uniquified ID
	 */
	private String uniquifyID(String ID) {
		if (IDsMap.containsKey(ID)) {
			long subindex = IDsMap.get(ID);
			String retVal = uniquifyID(ID + "_" + (subindex++));
			IDsMap.put(ID, subindex);
			return retVal;
		}
		IDsMap.put(ID, 0L);
		return ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#configure(org.knime.core.data.DataTableSpec
	 * [])
	 */
	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		try {
			m_IDColName.setStringValue(guessColumnName(inSpecs[0],
					m_IDColName.getStringValue(), StringCell.TYPE, "ID",
					inSpecs[0].getNumColumns() - 1));
		} catch (InvalidSettingsException e) {
			m_IDColName.setStringValue(guessColumnName(inSpecs[0],
					m_IDColName.getStringValue(), StringCell.TYPE, null,
					inSpecs[0].getNumColumns() - 1));
		}
		IDsMap = new HashMap<>();

		return new DataTableSpec[] { createColumnRearranger(inSpecs[0])
				.createSpec() };
	}

	/**
	 * Checks a column name exists. If not, tries to autoguess, and matches as
	 * substring (if non-'null'), and DataType. Starts at supplied column index
	 * and works back through the table
	 * 
	 * @param spec
	 *            The input data table spec
	 * @param nameFromSettingsModel
	 *            The name supplied from the settings model
	 * @param type
	 *            The type of column
	 * @param substringMatch
	 *            A substring to match - ignored if null
	 * @param startColIdx
	 *            The start column index
	 * @return The column name - either the validated name from the settings
	 *         model, or a guessed name of the correct type
	 * @throws InvalidSettingsException
	 */
	protected String guessColumnName(DataTableSpec spec,
			String nameFromSettingsModel, DataType type, String substringMatch,
			int startColIdx) throws InvalidSettingsException {
		DataColumnSpec colSpec = spec.getColumnSpec(nameFromSettingsModel);
		String retVal = nameFromSettingsModel;
		if (colSpec == null) {
			if (startColIdx < 0 || startColIdx >= spec.getNumColumns()) {
				// Run out of columns of the appropriate type, or supplied too
				// high an index
				m_logger.error("Not enough columns of the required type in the input table");
				throw new InvalidSettingsException(
						"Not enough columns of the required type in the input table");
			}
			// No column selected, or selected column not found - autoguess!
			for (int i = startColIdx; i >= 0; i--) {
				// Reverse order to select most recently added
				if (spec.getColumnSpec(i).getType()
						.isCompatible(type.getPreferredValueClass())
						&& (substringMatch == null || spec.getColumnSpec(i)
								.getName().indexOf(substringMatch) >= 0)) {
					retVal = (spec.getColumnSpec(i).getName());
					m_logger.warn("No column selected. " + retVal
							+ " auto-selected.");
					break;
				}
				// If we are here when i = 0, then no suitable column found
				if (i == 0) {
					m_logger.error("No column of the accepted"
							+ " input formats was found.");
					throw new InvalidSettingsException(
							"No column of the accepted"
									+ " input formats was found.");
				}
			}

		} else {
			// We had a selected column, now lets see if it is a compatible type
			if (!colSpec.getType().isCompatible(type.getPreferredValueClass())) {
				// The column is not compatible with one of the accepted types
				m_logger.error("The column " + retVal
						+ " is not one of the accepted" + " input formats");
				throw new InvalidSettingsException("The column " + retVal
						+ " is not one of the accepted" + " input formats");
			}
		}
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#loadInternals(java.io.File,
	 * org.knime.core.node.ExecutionMonitor)
	 */
	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Nothing

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#saveInternals(java.io.File,
	 * org.knime.core.node.ExecutionMonitor)
	 */
	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Nothing

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#saveSettingsTo(org.knime.core.node.
	 * NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_IDColName.saveSettingsTo(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#validateSettings(org.knime.core.node.
	 * NodeSettingsRO)
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_IDColName.validateSettings(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#loadValidatedSettingsFrom(org.knime.core
	 * .node.NodeSettingsRO)
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_IDColName.loadSettingsFrom(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#reset()
	 */
	@Override
	protected void reset() {
		// nothing

	}

}
