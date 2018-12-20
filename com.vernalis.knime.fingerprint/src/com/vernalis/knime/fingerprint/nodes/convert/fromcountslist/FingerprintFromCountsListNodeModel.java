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
package com.vernalis.knime.fingerprint.nodes.convert.fromcountslist;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.IntValue;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.collection.SetCell;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;

import com.vernalis.knime.fingerprint.abstrct.FingerPrintTypes;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public class FingerprintFromCountsListNodeModel extends SimpleStreamableFunctionNodeModel {

	/* Settings Models */
	protected final SettingsModelString m_countsColName =
			FingerprintFromCountsListNodeDialog.createCountsColNameModel();
	protected final SettingsModelBoolean m_keepInputCols =
			FingerprintFromCountsListNodeDialog.createKeepInputColumnsModel();
	// TODO: Reinstate once SpareseByteVector bug is fixed
	// protected final SettingsModelString m_fpType =
	// FingerprintFromCountsListNodeDialog.createFPTypeModel();

	/** The NodeLogger Instance - Needs to be instantiated */
	protected NodeLogger m_logger = NodeLogger.getLogger(FingerprintFromCountsListNodeModel.class);

	/** The {@link FingerPrintTypes} of the fp */
	protected FingerPrintTypes m_fpType1;

	protected static final DataType[] availableCollectionColTypes = new DataType[] {
			ListCell.getCollectionType(IntCell.TYPE), SetCell.getCollectionType(IntCell.TYPE) };

	public FingerprintFromCountsListNodeModel() {
		super();
	}

	/**
	 * Column rearranger, handling the keep input column options and performing
	 * the output option
	 * 
	 * @param inSpec
	 * @return
	 * @throws InvalidSettingsException
	 */
	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec inSpec)
			throws InvalidSettingsException {

		// Find columns of the appropriate types for the setbits
		m_countsColName.setStringValue(guessColumnName(inSpec, m_countsColName.getStringValue(),
				availableCollectionColTypes, "(Set bits)", inSpec.getNumColumns() - 1));

		// TODO: Reinstate once SpareseByteVector bug is fixed
		// m_fpType1 = FingerPrintTypes.valueOf(m_fpType.getStringValue());
		m_fpType1 = FingerPrintTypes.DENSE;
		ColumnRearranger result = new ColumnRearranger(inSpec);
		if (!m_keepInputCols.getBooleanValue()) {
			result.remove(m_countsColName.getStringValue());
		}

		DataColumnSpec newColSpec = (new DataColumnSpecCreator(
				DataTableSpec.getUniqueColumnName(inSpec, getResultColumnName()),
				m_fpType1.getDataType(true))).createSpec();
		final int countsIdx = inSpec.findColumnIndex(m_countsColName.getStringValue());

		result.append(new SingleCellFactory(newColSpec) {

			@Override
			public DataCell getCell(DataRow row) {

				// Handle missing cells
				final DataCell countsCell = row.getCell(countsIdx);
				if (countsCell.isMissing()) {
					return DataType.getMissingCell();
				}

				// Cast to CDV to handle Set and List cells easily
				CollectionDataValue counts = (CollectionDataValue) countsCell;

				// Handle the set bits
				if (counts.size() > m_fpType1.getMaxLength()) {
					m_logger.warn("Length " + counts.size() + " is too big for "
							+ m_fpType1.getActionCommand()
							+ " fingerprint.  Some counts will be lost");
					setWarningMessage("Some fingerprints were unable to be stored fully as "
							+ m_fpType1.getActionCommand());
				}

				byte[] countsVals =
						new byte[(int) Math.min(counts.size(), m_fpType1.getMaxLength())];
				int idx = 0;
				for (DataCell count : counts) {
					countsVals[idx++] = (byte) ((((IntValue) count)).getIntValue() & 0x000000FF);
				}

				return m_fpType1.getDataCellFromCounts(countsVals);
			}
		});
		return result;
	};

	/**
	 * Return the new column name. The calling method will handle ensuring
	 * uniqueness
	 */
	protected String getResultColumnName() {
		return m_countsColName.getStringValue() + " (" + m_fpType1.getToolTip() + ")";
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
	 * @param dataTypes
	 *            [0] The types of column allowed
	 * @param substringMatch
	 *            A substring to match - ignored if null
	 * @param startColIdx
	 *            The start column index
	 * @return The column name - either the validated name from the settings
	 *         model, or a guessed name of the correct type
	 * @throws InvalidSettingsException
	 */
	protected String guessColumnName(DataTableSpec spec, String nameFromSettingsModel,
			DataType[] dataTypes, String substringMatch, int startColIdx)
			throws InvalidSettingsException {
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
				DataType ithColType = spec.getColumnSpec(i).getType();

				if (checkIfCompatible(ithColType, dataTypes) && (substringMatch == null
						|| spec.getColumnSpec(i).getName().indexOf(substringMatch) >= 0)) {
					retVal = (spec.getColumnSpec(i).getName());
					m_logger.warn("No column selected. " + retVal + " auto-selected.");
					break;
				}
				// If we are here when i = 0, then no suitable column found
				if (i == 0) {
					m_logger.error("No column of the accepted" + " input formats was found.");
					throw new InvalidSettingsException(
							"No column of the accepted" + " input formats was found.");
				}
			}

		} else {
			// We had a selected column, now lets see if it is a compatible type
			if (!checkIfCompatible(colSpec.getType(), dataTypes)) {
				// The column is not compatible with one of the accepted types
				m_logger.error(
						"The column " + retVal + " is not one of the accepted" + " input formats");
				throw new InvalidSettingsException(
						"The column " + retVal + " is not one of the accepted" + " input formats");
			}
		}
		return retVal;
	}

	/**
	 * Checks if the {@link DataType} in ithColType is the same as one of the
	 * entries in the list of dataTypes
	 * 
	 * @param ithColType
	 *            The {@link DataType} if the current ('test') column
	 * @param dataTypes
	 *            An array of all the acceptable {@link DataType}s for the
	 *            column selection
	 * @return <code>true</code> if the test column {@link DataType} is one of
	 *         the acceptable values
	 */
	private boolean checkIfCompatible(DataType ithColType, DataType[] dataTypes) {
		for (DataType type : dataTypes) {
			if (ithColType == type) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#saveSettingsTo(org.knime.core.node.
	 * NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_countsColName.saveSettingsTo(settings);
		m_keepInputCols.saveSettingsTo(settings);
		// TODO: Reinstate once SpareseByteVector bug is fixed
		// m_fpType.saveSettingsTo(settings);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#validateSettings(org.knime.core.node.
	 * NodeSettingsRO)
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		m_countsColName.validateSettings(settings);
		m_keepInputCols.validateSettings(settings);
		// TODO: Reinstate once SpareseByteVector bug is fixed
		// m_fpType.validateSettings(settings);

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
		m_countsColName.loadSettingsFrom(settings);
		m_keepInputCols.loadSettingsFrom(settings);
		// TODO: Reinstate once SpareseByteVector bug is fixed
		// m_fpType.loadSettingsFrom(settings);

	}

}
