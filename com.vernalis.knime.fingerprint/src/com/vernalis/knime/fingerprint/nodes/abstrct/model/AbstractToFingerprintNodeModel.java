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
package com.vernalis.knime.fingerprint.nodes.abstrct.model;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;

import com.vernalis.knime.fingerprint.abstrct.FingerPrintTypes;
import com.vernalis.knime.fingerprint.nodes.abstrct.dialog.AbstractStringToFingerprintNodeDialog;

/**
 * Abstract Streamable node model implementation for nodes generating
 * fingerprints from another column(s)
 * 
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public abstract class AbstractToFingerprintNodeModel extends SimpleStreamableFunctionNodeModel {

	/* Settings Models */
	protected final SettingsModelString m_ColName =
			AbstractStringToFingerprintNodeDialog.createStringColNameModel();
	protected final SettingsModelBoolean m_keepInputCols =
			AbstractStringToFingerprintNodeDialog.createKeepInputColumnsModel();
	protected final SettingsModelString m_fpTypeMdl;

	/** The NodeLogger Instance - Needs to be instantiated */
	protected NodeLogger m_logger = NodeLogger.getLogger(getClass());

	/** The {@link FingerPrintTypes} of the fp */
	protected FingerPrintTypes m_fpType;

	protected final DataType[] availableColTypes;
	protected boolean isOutputCountFp = false;

	/**
	 * Constructor assuming that there is a sparse/dense option
	 * @param incomingColumnTypes The acceptable incoming column type(s)
	 */
	public AbstractToFingerprintNodeModel(DataType[] incomingColumnTypes) {
		this(incomingColumnTypes, true);
	}

	/**
	 * Constructor
	 * @param incomingColumnTypes The acceptable incoming column type(s)
	 * @param hasFpType Does the node have a sparse/dense setting?
	 */
	public AbstractToFingerprintNodeModel(DataType[] incomingColumnTypes, boolean hasFpType) {
		availableColTypes = incomingColumnTypes;
		if (hasFpType) {
			m_fpTypeMdl = AbstractStringToFingerprintNodeDialog.createFPTypeModel();
		} else {
			m_fpTypeMdl = null;
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

		ColumnRearranger rearranger = createColumnRearranger(inSpecs[0]);

		return new DataTableSpec[] { rearranger.createSpec() };
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
		// Find a column of the appropriate type
		m_ColName.setStringValue(guessColumnName(inSpec, m_ColName.getStringValue(),
				availableColTypes, null, inSpec.getNumColumns() - 1));

		m_fpType = m_fpTypeMdl == null ? FingerPrintTypes.DENSE
				: FingerPrintTypes.valueOf(m_fpTypeMdl.getStringValue());

		ColumnRearranger result = new ColumnRearranger(inSpec);
		if (!m_keepInputCols.getBooleanValue()) {
			result.remove(m_ColName.getStringValue());
		}

		DataColumnSpec newColSpec = (new DataColumnSpecCreator(
				DataTableSpec.getUniqueColumnName(inSpec, getResultColumnName()),
				m_fpType.getDataType(isOutputCountFp))).createSpec();
		final int fp1Idx = inSpec.findColumnIndex(m_ColName.getStringValue());

		result.append(new SingleCellFactory(newColSpec) {

			@Override
			public DataCell getCell(DataRow row) {
				DataCell inCell = row.getCell(fp1Idx);
				if (inCell.isMissing()) {
					return DataType.getMissingCell();
				}
				return getFingerprintCell(inCell);

			}

		});
		return result;
	};

	/**
	 * Return the new column name. The abstract superclass will handle ensuring
	 * uniqueness
	 */
	abstract protected String getResultColumnName();

	/**
	 * Return the result cell - need to check for sparse or dense bit vector and
	 * handle accordingly. {@link #m_fpTypeMdl} contains the {@link DataType} of
	 * the selected columns
	 * 
	 * @param fpString
	 *            The string to convert to a fingerprint
	 * @return The result cell
	 */
	abstract protected DataCell getFingerprintCell(DataCell inCell);

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
				boolean isCompatible = false;
				for (DataType type : dataTypes) {
					if (ithColType == type) {
						isCompatible = true;
						break;
					}
				}
				if (isCompatible && (substringMatch == null
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
			if (!colSpec.getType().isCompatible(dataTypes[0].getPreferredValueClass())) {
				// The column is not compatible with one of the accepted types
				m_logger.error(
						"The column " + retVal + " is not one of the accepted" + " input formats");
				throw new InvalidSettingsException(
						"The column " + retVal + " is not one of the accepted" + " input formats");
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
		m_ColName.saveSettingsTo(settings);
		m_keepInputCols.saveSettingsTo(settings);
		if (m_fpTypeMdl != null) {
			m_fpTypeMdl.saveSettingsTo(settings);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#validateSettings(org.knime.core.node.
	 * NodeSettingsRO)
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		m_ColName.validateSettings(settings);
		m_keepInputCols.validateSettings(settings);
		if (m_fpTypeMdl != null) {
			m_fpTypeMdl.validateSettings(settings);
		}
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
		m_ColName.loadSettingsFrom(settings);
		m_keepInputCols.loadSettingsFrom(settings);
		if (m_fpTypeMdl != null) {
			m_fpTypeMdl.loadSettingsFrom(settings);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#reset()
	 */
	@Override
	protected void reset() {
		// Nothing

	}

}
