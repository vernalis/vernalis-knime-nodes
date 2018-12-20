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
package com.vernalis.knime.fingerprint.nodes.convert.fromsetbitslist;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.LongValue;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.collection.SetCell;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.LongCell;
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
public class FingerprintFromSetBitsListNodeModel extends SimpleStreamableFunctionNodeModel {

	/* Settings Models */
	protected final SettingsModelString m_lengthColName =
			FingerprintFromSetBitsListNodeDialog.createLengthColNameModel();
	protected final SettingsModelString m_setBitsColName =
			FingerprintFromSetBitsListNodeDialog.createBitsColNameModel();
	protected final SettingsModelBoolean m_keepInputCols =
			FingerprintFromSetBitsListNodeDialog.createKeepInputColumnsModel();
	protected final SettingsModelString m_fpType =
			FingerprintFromSetBitsListNodeDialog.createFPTypeModel();

	/** The NodeLogger Instance - Needs to be instantiated */
	protected NodeLogger m_logger = NodeLogger.getLogger(FingerprintFromSetBitsListNodeModel.class);

	/** The {@link FingerPrintTypes} of the fp */
	protected FingerPrintTypes m_fpType1;

	protected static final DataType[] availableLengthColTypes =
			new DataType[] { LongCell.TYPE, IntCell.TYPE };
	protected static final DataType[] availableCollectionColTypes = new DataType[] {
			ListCell.getCollectionType(LongCell.TYPE), ListCell.getCollectionType(IntCell.TYPE),
			SetCell.getCollectionType(LongCell.TYPE), SetCell.getCollectionType(IntCell.TYPE) };

	public FingerprintFromSetBitsListNodeModel() {
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

		// Find columns of the appropriate types for the length
		m_lengthColName.setStringValue(guessColumnName(inSpec, m_lengthColName.getStringValue(),
				availableLengthColTypes, "(Length)", inSpec.getNumColumns() - 1));

		// Find columns of the appropriate types for the setbits
		m_setBitsColName.setStringValue(guessColumnName(inSpec, m_setBitsColName.getStringValue(),
				availableCollectionColTypes, "(Set bits)", inSpec.getNumColumns() - 1));

		m_fpType1 = FingerPrintTypes.valueOf(m_fpType.getStringValue());

		ColumnRearranger result = new ColumnRearranger(inSpec);
		if (!m_keepInputCols.getBooleanValue()) {
			result.remove(m_lengthColName.getStringValue(), m_setBitsColName.getStringValue());
		}

		DataColumnSpec newColSpec = (new DataColumnSpecCreator(
				DataTableSpec.getUniqueColumnName(inSpec, getResultColumnName()),
				m_fpType1.getDataType(false))).createSpec();
		final int lenIdx = inSpec.findColumnIndex(m_lengthColName.getStringValue());
		final int setBitsIdx = inSpec.findColumnIndex(m_setBitsColName.getStringValue());

		result.append(new SingleCellFactory(newColSpec) {

			@Override
			public DataCell getCell(DataRow row) {
				DataCell len = row.getCell(lenIdx);

				// Handle missing cells
				if (len.isMissing() || row.getCell(setBitsIdx).isMissing()) {
					return DataType.getMissingCell();
				}

				// Handle the length
				long length = ((LongValue) len).getLongValue();
				if (length > m_fpType1.getMaxLength()) {
					m_logger.warn("Length " + length + " is too long for "
							+ m_fpType1.getActionCommand() + " fingerprint.  Truncating to "
							+ m_fpType1.getMaxLength() + " bits");
					setWarningMessage("Some fingerprints were unable to be stored fully as "
							+ m_fpType1.getActionCommand());
					length = m_fpType1.getMaxLength();
				} else if (length < 0) {
					m_logger.warn("Negative length encountered in row '" + row.getKey().getString()
							+ "' - returning missing cell");
				}

				// Cast to CDV to handle Set and List cells easily
				CollectionDataValue setBits = (CollectionDataValue) row.getCell(setBitsIdx);

				// Handle the set bits
				if (setBits.size() > m_fpType1.getMaxCardinality()) {
					m_logger.warn("Cardinality " + setBits.size() + " is too big for "
							+ m_fpType1.getActionCommand()
							+ " fingerprint.  Some bits will be lost");
					setWarningMessage("Some fingerprints were unable to be stored fully as "
							+ m_fpType1.getActionCommand());
				}

				long[] setBitsIdx =
						new long[(int) Math.min(setBits.size(), m_fpType1.getMaxCardinality())];
				int idx = 0;
				for (DataCell setBit : setBits) {
					setBitsIdx[idx++] = ((LongValue) setBit).getLongValue();
				}

				return m_fpType1.getDataCellFromSetBits(length, setBitsIdx);
			}
		});
		return result;
	};

	/**
	 * Return the new column name. The calling method will handle ensuring
	 * uniqueness
	 */
	protected String getResultColumnName() {
		return m_setBitsColName.getStringValue() + " (" + m_fpType1.getToolTip() + ")";
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
		m_lengthColName.saveSettingsTo(settings);
		m_setBitsColName.saveSettingsTo(settings);
		m_keepInputCols.saveSettingsTo(settings);
		m_fpType.saveSettingsTo(settings);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#validateSettings(org.knime.core.node.
	 * NodeSettingsRO)
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		m_lengthColName.validateSettings(settings);
		m_setBitsColName.validateSettings(settings);
		m_keepInputCols.validateSettings(settings);
		m_fpType.validateSettings(settings);

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
		m_lengthColName.loadSettingsFrom(settings);
		m_setBitsColName.loadSettingsFrom(settings);
		m_keepInputCols.loadSettingsFrom(settings);
		m_fpType.loadSettingsFrom(settings);

	}

}
