/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.misc.trimtable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.CloseableRowIterator;
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
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.nodes.SettingsModelRegistry;

import static com.vernalis.nodes.misc.trimtable.TrimTableNodeDialog.createColsModel;
import static com.vernalis.nodes.misc.trimtable.TrimTableNodeDialog.createTrimBehaviourModel;
import static com.vernalis.nodes.misc.trimtable.TrimTableNodeDialog.createTrimEndModel;
import static com.vernalis.nodes.misc.trimtable.TrimTableNodeDialog.createTrimStartModel;

/**
 * {@link NodeModel} implementation for the Trim Table node
 * 
 * @author s.roughley
 *
 */
public class TrimTableNodeModel extends NodeModel
		implements SettingsModelRegistry {

	private final HashSet<SettingsModel> models = new HashSet<>();
	private final SettingsModelColumnFilter2 colMdl =
			registerSettingsModel(createColsModel());
	private final SettingsModelBoolean trimStartMdl =
			registerSettingsModel(createTrimStartModel());
	private final SettingsModelBoolean trimEndMdl =
			registerSettingsModel(createTrimEndModel());
	private final SettingsModelString trimBehaviourMdl =
			registerSettingsModel(createTrimBehaviourModel());

	private int[] testCols;
	private TrimBehaviour trimBehaviour;

	public TrimTableNodeModel() {
		super(1, 1);

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

		testCols = Arrays.stream(colMdl.applyTo(inSpecs[0]).getIncludes())
				.mapToInt(colName -> inSpecs[0].findColumnIndex(colName))
				.toArray();

		if (testCols.length == 0) {
			throw new InvalidSettingsException(
					"No selected columns were found in the input table!");
		}

		if (!trimStartMdl.getBooleanValue() && !trimEndMdl.getBooleanValue()) {
			throw new InvalidSettingsException(
					"Neither end of the table will be trimmed!");
		}
		trimBehaviour =
				TrimBehaviour.valueOf(trimBehaviourMdl.getStringValue());
		return inSpecs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#execute(org.knime.core.node.
	 * BufferedDataTable[], org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {
		BufferedDataContainer bdc =
				exec.createDataContainer(inData[0].getDataTableSpec());

		CloseableRowIterator rowIter = inData[0].iterator();
		long rowIdx = 0;
		final long numRows = inData[0].size();
		double progressPerRow = 1.0 / numRows;
		boolean foundStart = trimStartMdl.getBooleanValue() ? false : true;
		String baseMessage = foundStart ? "Looking for first include row - "
				: "Adding rows to table - ";

		while (rowIter.hasNext()) {
			exec.checkCanceled();
			exec.setProgress((rowIdx++) * progressPerRow,
					baseMessage + " row " + rowIdx + " of " + numRows);
			DataRow row = rowIter.next();

			if (trimBehaviour.testRow(row, testCols)) {
				// The row passed the include test, and so should be added
				// We add the row...
				bdc.addRowToTable(row);
				if (!foundStart) {
					foundStart = true;
					baseMessage = "Adding rows to table - ";
				}
			} else if (!foundStart) {
				// We are still trimming the start - nothing else to do with
				// this row...
				continue;
			} else if (!trimEndMdl.getBooleanValue()) {
				// We aren't trimming the end, so just add the row anyway
				bdc.addRowToTable(row);
			} else {
				// The row didnt match, and we are trimming the end of the
				// table, and so now we need to look ahead to see if any more
				// rows do
				BufferedDataContainer tmpBdc =
						exec.createDataContainer(inData[0].getDataTableSpec());
				tmpBdc.addRowToTable(row);
				baseMessage =
						"Checking for further matching rows at end of table -";
				long lastTmpRowToAdd = 0;
				while (rowIter.hasNext()) {
					exec.checkCanceled();
					// NB We only add half progress for remaining rows as they
					// might still need to be processed.
					exec.setProgress((rowIdx++) * (progressPerRow / 2),
							baseMessage + " row " + rowIdx + " of " + numRows);
					row = rowIter.next();
					tmpBdc.addRowToTable(row);
					if (trimBehaviour.testRow(row, testCols)) {
						lastTmpRowToAdd = tmpBdc.size();
					}
				}

				// We have checked ahead to the end of the table, and now we
				// need to add any tmp rows to the main table
				tmpBdc.close();
				if (lastTmpRowToAdd > 0) {
					progressPerRow =
							(1.0 - exec.getProgressMonitor().getProgress())
									/ lastTmpRowToAdd;
					baseMessage = "Merging temporary tables - ";

					BufferedDataTable tmpTable = tmpBdc.getTable();
					long tmpRowIdx = 0;
					CloseableRowIterator tmpIter = tmpTable.iterator();
					while (tmpRowIdx++ < lastTmpRowToAdd && tmpIter.hasNext()) {
						exec.checkCanceled();
						exec.setProgress(tmpRowIdx * progressPerRow,
								baseMessage + " row " + tmpRowIdx + " of "
										+ lastTmpRowToAdd);
						bdc.addRowToTable(tmpIter.next());
					}
				} else {
					exec.setProgress(1.0);
				}
				tmpBdc.dispose();
			}
		}
		bdc.close();
		return new BufferedDataTable[] { bdc.getTable() };
	}

	@Override
	public Set<SettingsModel> getModels() {
		return models;
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
		//

	}

}
