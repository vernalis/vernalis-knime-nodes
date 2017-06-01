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
package com.vernalis.knime.perfmon.nodes.testing.missingvals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.util.filter.NameFilterConfiguration.FilterResult;

public class EmptyColumnTestNodeModel extends NodeModel {
	protected final SettingsModelColumnFilter2 m_columns = EmptyColumnTestNodeDialogPane
			.createFilterSettingsModel();

	protected NodeLogger logger = NodeLogger.getLogger(this.getClass());

	protected EmptyColumnTestNodeModel() {
		super(1, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#execute(org.knime.core.node.
	 * BufferedDataTable[], org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec)
			throws Exception {
		List<Integer> colIdsToCheck = Arrays
				.stream(m_columns.applyTo(inData[0].getDataTableSpec()).getIncludes())
				.map(x -> inData[0].getSpec().findColumnIndex(x)).collect(Collectors.toList());
		// boolean[] hasNonBlank = new boolean[colIdsToCheck.length];
		long rowCnt = 0;
		table: for (DataRow row : inData[0]) {
			exec.checkCanceled();
			exec.setProgress((double) rowCnt / (double) inData[0].size(),
					"Tested " + rowCnt + " of " + inData[0].size() + " rows. "
							+ colIdsToCheck.size() + " columns still failing...");

			Iterator<Integer> iter = colIdsToCheck.iterator();
			while (iter.hasNext()) {
				if (!checkCell(row.getCell(iter.next()))) {
					iter.remove();
					if (colIdsToCheck.size() == 0) {
						logger.info("All columns passed after " + rowCnt + " rows");
						break table;
					}
				}
			}
		}
		if (colIdsToCheck.size() > 0) {
			List<String> failedColumns = colIdsToCheck.stream()
					.map(x -> inData[0].getDataTableSpec().getColumnSpec(x).getName())
					.collect(Collectors.toList());
			logger.error("Failed:  The following columns had no non-missing values:");
			failedColumns.forEach(x -> logger.error("\t" + x));
			throw new IllegalStateException(
					"Failed:  The following columns had no non-missing values: '"
							+ failedColumns.stream().collect(Collectors.joining("', '")) + "'");
		} else {
			logger.info("Test succeeded - No columns were entirely composed of missing values");
		}
		return new BufferedDataTable[0];
	}

	/**
	 * @param cell
	 * @return {@code true} if the cell is missing. For collection cells,
	 *         returns true if all subcells are missing or the size==0
	 */
	private boolean checkCell(DataCell cell) {
		if (cell.isMissing()) {
			return true;
		} else if (cell.getType().isCollectionType()) {
			if (((CollectionDataValue) cell).size() == 0) {
				return true;
			}
			for (DataCell innerCell : ((CollectionDataValue) cell)) {
				if (!innerCell.isMissing()) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#configure(org.knime.core.data.DataTableSpec
	 * [])
	 */
	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		FilterResult filt = m_columns.applyTo(inSpecs[0]);
		if (filt.getIncludes().length < 1) {
			throw new InvalidSettingsException("No columns selected");
		}
		StringBuilder err = new StringBuilder();
		if (filt.getRemovedFromExcludes().length > 0) {
			err.append("The following excluded columns are no longer available: '");
			err.append(Arrays.stream(filt.getRemovedFromExcludes())
					.collect(Collectors.joining("', '")));
			err.append("'. ");
		}
		if (filt.getRemovedFromIncludes().length > 0) {
			err.append("The following included columns are no longer available: '");
			err.append(Arrays.stream(filt.getRemovedFromIncludes())
					.collect(Collectors.joining("', '")));
			err.append("'.");
		}
		if (err.length() > 0) {
			setWarningMessage(err.toString().trim());
		}
		return new DataTableSpec[0];
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
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_columns.saveSettingsTo(settings);

	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		m_columns.validateSettings(settings);

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_columns.loadSettingsFrom(settings);

	}

	@Override
	protected void reset() {
	}

}
