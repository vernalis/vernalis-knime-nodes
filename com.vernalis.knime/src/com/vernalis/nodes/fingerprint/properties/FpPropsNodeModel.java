/*******************************************************************************
 * Copyright (c) 2014, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License, Version 3, as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *  
 *******************************************************************************/
package com.vernalis.nodes.fingerprint.properties;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.SparseBitVectorCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * This is the model implementation of SparseToDense. Node to convert a
 * SparseBitVector Fingerprint to a DenseBitVector fingerprint
 * 
 * @author S. Roughley
 */
public class FpPropsNodeModel extends NodeModel {
	static final String CFG_FPCOL = "Fingerprint_Column_Name";
	static final String CFG_FPTYPE = "Fingerprint_Type";
	static final String CFG_FPLENGTH = "Fingerprint_Length";
	static final String CFG_FPCARDINALITY = "Fingerprint_Cardinality";

	private SettingsModelString m_fpColName = new SettingsModelString(
			CFG_FPCOL, null);
	private SettingsModelBoolean m_fpType = new SettingsModelBoolean(
			CFG_FPTYPE, true);
	private SettingsModelBoolean m_fpLen = new SettingsModelBoolean(
			CFG_FPLENGTH, true);
	private SettingsModelBoolean m_fpCardinality = new SettingsModelBoolean(
			CFG_FPCARDINALITY, true);

	private DataTableSpec m_Spec_0; // The datatable spec
	private Map<String, DataType> m_NewColNames_0;

	/**
	 * Constructor for the node model.
	 */
	protected FpPropsNodeModel() {
		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		ColumnRearranger rearranger = createColumnRearranger(inData[0]
				.getDataTableSpec());
		BufferedDataTable outTable = exec.createColumnRearrangeTable(inData[0],
				rearranger, exec);
		// // Find the FP column
		// BufferedDataTable table = inData[0];
		// final int fpInd = table.getSpec().findColumnIndex(
		// m_fpColName.getStringValue());
		//
		// // Create the new output table Buffered Data Containers
		// final BufferedDataContainer dc_0 =
		// exec.createDataContainer(m_Spec_0);
		//
		// // Handle Empty Tables
		// if (table.getRowCount() == 0) {
		// dc_0.close();
		// return new BufferedDataTable[] { dc_0.getTable() };
		// }
		//
		// // Count the new columns
		// int newColCnt = 0;
		// newColCnt += (m_fpType.getBooleanValue()) ? 1 : 0;
		// newColCnt += (m_fpLen.getBooleanValue()) ? 1 : 0;
		// newColCnt += (m_fpCardinality.getBooleanValue()) ? 1 : 0;
		//
		// // Now loop through the table
		// int nRows = table.getRowCount();
		// Double progressPerRow = 1.0 / nRows;
		// int rowCnt = 0;
		// DataType fpType = inData[0].getDataTableSpec()
		// .getColumnSpec(m_fpColName.getStringValue()).getType();
		//
		// for (final DataRow row : table) {
		// exec.checkCanceled();
		// exec.setProgress((rowCnt * progressPerRow), "Processing Row "
		// + (rowCnt++) + " of " + nRows);
		//
		// DataCell fpCell = row.getCell(fpInd);
		//
		// // Create a container array for the new cells with missing values
		// DataCell[] newCells = new DataCell[newColCnt];
		// Arrays.fill(newCells, DataType.getMissingCell());
		//
		// if (fpCell.isMissing()) {
		// // Deal with a missing FP cell
		// dc_0.addRowToTable(createClone(row.getKey(), row, newCells));
		// continue;
		// }
		//
		// int newColId = 0;
		// if (m_fpType.getBooleanValue()) {
		// newCells[newColId++] = new StringCell(fpType.toString());
		// }
		//
		// if (m_fpLen.getBooleanValue()) {
		// newCells[newColId++] = new LongCell(
		// (fpType == SparseBitVectorCell.TYPE) ? ((SparseBitVectorCell) fpCell)
		// .length() : ((DenseBitVectorCell) fpCell)
		// .length());
		// }
		//
		// if (m_fpCardinality.getBooleanValue()) {
		// newCells[newColId++] = new LongCell(
		// (fpType == SparseBitVectorCell.TYPE) ? ((SparseBitVectorCell) fpCell)
		// .cardinality() : ((DenseBitVectorCell) fpCell)
		// .cardinality());
		// }
		// dc_0.addRowToTable(createClone(row.getKey(), row, newCells));
		// }
		// dc_0.close();
		// BufferedDataTable outTable = dc_0.getTable();

		return new BufferedDataTable[] { outTable };
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

		// Check the selection for the FP column
		int colIndex = -1;
		if (m_fpColName.getStringValue() == null) {
			int i = 0;
			for (DataColumnSpec cs : inSpecs[0]) {
				if (cs.getType().isCompatible(BitVectorValue.class)) {
					if (colIndex != -1) {
						setWarningMessage("No FP column selected");
						throw new InvalidSettingsException(
								"No FP column selected.");
					}
					colIndex = i;
				}
				i++;
			}

			if (colIndex == -1) {
				setWarningMessage("No FP column selected");
				throw new InvalidSettingsException("No FP column selected.");
			}
			m_fpColName.setStringValue(inSpecs[0].getColumnSpec(colIndex)
					.getName());
			setWarningMessage("Column '" + m_fpColName.getStringValue()
					+ "' auto selected for FP column");
		} else {
			colIndex = inSpecs[0].findColumnIndex(m_fpColName.getStringValue());
			if (colIndex < 0) {
				setWarningMessage("No such column: "
						+ m_fpColName.getStringValue());
				throw new InvalidSettingsException("No such column: "
						+ m_fpColName.getStringValue());
			}

			DataColumnSpec colSpec = inSpecs[0].getColumnSpec(colIndex);
			if (!colSpec.getType().isCompatible(BitVectorValue.class)) {
				setWarningMessage("Column \"" + m_fpColName.getStringValue()
						+ "\" does not contain FP values");
				throw new InvalidSettingsException("Column \"" + m_fpColName
						+ "\" does not contain FP values: "
						+ colSpec.getType().toString());
			}
		}

		// Now we need to build the output table spec
		// We omit the old FP column and create a new DenseBitVector column with
		// the same name
		String suffix = " (" + m_fpColName.getStringValue() + ")";
		m_NewColNames_0 = new LinkedHashMap<String, DataType>();
		if (m_fpType.getBooleanValue()) {
			m_NewColNames_0.put("Fingerprint Type" + suffix, StringCell.TYPE);
		}
		if (m_fpLen.getBooleanValue()) {
			m_NewColNames_0.put("Fingerprint Length" + suffix, LongCell.TYPE);
		}
		if (m_fpCardinality.getBooleanValue()) {
			m_NewColNames_0.put("Fingerprint Cardinality" + suffix,
					LongCell.TYPE);
		}

		if (m_NewColNames_0.isEmpty()) {
			// Check we are actually adding some columns
			setWarningMessage("No new columns selected");
			throw new InvalidSettingsException("No new columns selected");
		}

		m_Spec_0 = createColumnRearranger(inSpecs[0]).createSpec();
		// m_Spec_0 = createTableSpec(inSpecs[0], newColNames_0);
		return new DataTableSpec[] { m_Spec_0 };
	}

	private ColumnRearranger createColumnRearranger(DataTableSpec spec) {
		ColumnRearranger rearranger = new ColumnRearranger(spec);

		// Create the specs of the new columns
		DataColumnSpec[] colSpecs = new DataColumnSpec[m_NewColNames_0.size()];
		int colind = 0;
		for (Entry<String, DataType> col : m_NewColNames_0.entrySet()) {
			colSpecs[colind++] = new DataColumnSpecCreator(col.getKey(),
					col.getValue()).createSpec();
		}

		final int fpInd = spec.findColumnIndex(m_fpColName.getStringValue());

		final DataType fpType = spec
				.getColumnSpec(m_fpColName.getStringValue()).getType();
		rearranger.append(new AbstractCellFactory(colSpecs) {

			@Override
			public DataCell[] getCells(DataRow row) {

				DataCell[] resultCells = new DataCell[m_NewColNames_0.size()];
				Arrays.fill(resultCells, DataType.getMissingCell());
				DataCell fpCell = row.getCell(fpInd);
				if (!fpCell.isMissing()) {
					int newColId = 0;
					if (m_fpType.getBooleanValue()) {
						resultCells[newColId++] = new StringCell(fpType
								.toString());
					}

					if (m_fpLen.getBooleanValue()) {
						resultCells[newColId++] = new LongCell(
								(fpType == SparseBitVectorCell.TYPE) ? ((SparseBitVectorCell) fpCell)
										.length()
										: ((DenseBitVectorCell) fpCell)
												.length());
					}

					if (m_fpCardinality.getBooleanValue()) {
						resultCells[newColId++] = new LongCell(
								(fpType == SparseBitVectorCell.TYPE) ? ((SparseBitVectorCell) fpCell)
										.cardinality()
										: ((DenseBitVectorCell) fpCell)
												.cardinality());
					}
				}
				return resultCells;
			}
		});
		return rearranger;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_fpCardinality.saveSettingsTo(settings);
		m_fpColName.saveSettingsTo(settings);
		m_fpLen.saveSettingsTo(settings);
		m_fpType.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_fpCardinality.loadSettingsFrom(settings);
		m_fpColName.loadSettingsFrom(settings);
		m_fpLen.loadSettingsFrom(settings);
		m_fpType.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_fpCardinality.validateSettings(settings);
		m_fpColName.validateSettings(settings);
		m_fpLen.validateSettings(settings);
		m_fpType.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

	}

}
