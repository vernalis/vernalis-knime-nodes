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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.SparseBitVectorCell;
import org.knime.core.data.vector.bytevector.ByteVectorValue;
import org.knime.core.data.vector.bytevector.DenseByteVectorCell;
import org.knime.core.data.vector.bytevector.SparseByteVectorCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;

import static com.vernalis.knime.fingerprint.nodes.abstrct.dialog.AbstractSingleFingerprintNodeDialog.createFirstFPColNameModel;
import static com.vernalis.knime.fingerprint.nodes.abstrct.dialog.AbstractSingleFingerprintNodeDialog.createKeepInputColumnsModel;

/**
 * The abstract streamable node model implementation for a single fingerprint
 * node.
 * 
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public abstract class AbstractSingleFingerprintNodeModel extends SimpleStreamableFunctionNodeModel {

	/* Settings Models */
	protected final SettingsModelString m_firstColName = createFirstFPColNameModel();
	protected final SettingsModelBoolean m_keepInputCols = createKeepInputColumnsModel();

	/** The NodeLogger Instance - Needs to be instantiated */
	protected final NodeLogger m_logger = NodeLogger.getLogger(getClass());

	/** The datatype of the selected column */
	protected DataType m_fpType;
	protected final List<DataType> availableColTypes;
	protected DataType[] resultTypes;

	/**
	 * Overloaded constructor for a node accepting any input fingerprint type
	 */
	public AbstractSingleFingerprintNodeModel(DataType[] resultType) {
		this(true, true, true, true, resultType);
	}

	/**
	 * Fully featured constructor
	 * 
	 * @param allowSparseBitVector
	 *            Does the node accept Sparse Bit Vector columns
	 * @param allowDenseBitVector
	 *            Does the node accept Dense Bit Vector columns
	 * @param allowSparseByteVector
	 *            Does the node accept Sparse Byte Vector columns
	 * @param allowDenseByteVector
	 *            Does the node accept Dense Byte Vector columns
	 */
	public AbstractSingleFingerprintNodeModel(boolean allowSparseBitVector,
			boolean allowDenseBitVector, boolean allowSparseByteVector,
			boolean allowDenseByteVector, DataType[] resultType) {
		super();
		availableColTypes = new ArrayList<>();
		if (allowSparseBitVector) {
			availableColTypes.add(SparseBitVectorCell.TYPE);
		}
		if (allowDenseBitVector) {
			availableColTypes.add(DenseBitVectorCell.TYPE);
		}
		if (allowSparseByteVector) {
			availableColTypes.add(SparseByteVectorCell.TYPE);
		}
		if (allowDenseByteVector) {
			availableColTypes.add(DenseByteVectorCell.TYPE);
		}
		if (allowSparseBitVector || allowDenseBitVector) {
			try {
				getBitVectorResultCells(DataType.getMissingCell(), null);
			} catch (UnsupportedOperationException e) {
				throw e;
			} catch (Exception e) {
				// Do nothing!
			}
		}
		if (allowSparseByteVector || allowDenseByteVector) {
			try {
				getByteVectorResultCells(DataType.getMissingCell(), null);
			} catch (UnsupportedOperationException e) {
				throw e;
			} catch (Exception e) {
				// Do nothing!
			}
		}
		this.resultTypes = resultType;
	}

	/**
	 * Column rearranger, handling the keep input column options and performing
	 * the output option
	 * 
	 * @param inSpec
	 *            The incoming table spec
	 * @return The column rearranger for the output type
	 * @throws InvalidSettingsException
	 *             If there is no suitable incoming column, or the incoming
	 *             selected column is not of the correct type
	 */
	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec inSpec)
			throws InvalidSettingsException {
		// Find a column of the appropriate type
		m_firstColName.setStringValue(guessColumnName(inSpec, m_firstColName.getStringValue(), null,
				inSpec.getNumColumns() - 1));

		m_fpType = inSpec.getColumnSpec(m_firstColName.getStringValue()).getType();
		ColumnRearranger result = new ColumnRearranger(inSpec);
		if (!m_keepInputCols.getBooleanValue()) {
			result.remove(m_firstColName.getStringValue());
		}
		String[] newColNames = getResultColumnNames();
		DataType[] newColTypes = getResultColumnTypes();
		if (newColNames.length != newColTypes.length) {
			throw new InvalidSettingsException("The Number of column names differs from the number "
					+ "of types - likely an implementation error!");
		}
		DataColumnSpec[] newColSpecs = new DataColumnSpec[newColTypes.length];
		for (int i = 0; i < newColSpecs.length; i++) {
			newColSpecs[i] = new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(inSpec, newColNames[i]), newColTypes[i])
							.createSpec();
		}
		final int fp1Idx = inSpec.findColumnIndex(m_firstColName.getStringValue());
		result.append(getCellFactory(newColSpecs, fp1Idx));
		return result;
	}

	/**
	 * @param firstFpColIdx
	 * @param newColSpecs
	 * @return
	 */
	protected AbstractCellFactory getCellFactory(DataColumnSpec[] newColSpecs, int... fpColIdx) {
		return new AbstractCellFactory(true, newColSpecs) {

			@Override
			public DataCell[] getCells(DataRow row) {
				DataCell fp1 = row.getCell(fpColIdx[0]);
				DataCell[] result = new DataCell[getResultColumnTypes().length];
				Arrays.fill(result, DataType.getMissingCell());
				if (fp1.isMissing()) {
					return result;
				}
				if (fp1 instanceof BitVectorValue) {
					getBitVectorResultCells(fp1, result);
				} else if (fp1 instanceof ByteVectorValue) {
					getByteVectorResultCells(fp1, result);
				} else {
					// Otherwise not a fingerprint cell
					throw new RuntimeException(
							"The supplied cell appears not to be a recognised fingerprint format");
				}
				return result;
			}
		};
	}

	/**
	 * Return the new column name. The abstract superclass will handle ensuring
	 * uniqueness
	 */
	abstract protected String[] getResultColumnNames();

	/**
	 * Return the new column {@link DataType}. If the resultType supplied to the
	 * constructor was null, then the output will be the same as the input type.
	 * Nodes with variable types depending on other factors should override this
	 * method
	 */
	protected DataType[] getResultColumnTypes() {
		return resultTypes;
	}

	/**
	 * Calculate the result cells for BitVectors - need to check for sparse or
	 * dense byte vector and handle accordingly. {@link #m_fpType} contains the
	 * {@link DataType} of the selected columns
	 * 
	 * @param fp1
	 *            The cell containing the first fingerprint. Guaranteed to be
	 *            {@link ByteVectorValue}, non-<code>null</code> and not
	 *            {@link DataType#getMissingCell()} except during node
	 *            initialisation when all exceptions will be trapped
	 * @param result
	 *            An array initialised with the correct number of missing cells,
	 *            into which the required results should be substituted
	 * @throws UnsupportedOperationException
	 *             If the method has not been implemented
	 */
	protected void getByteVectorResultCells(DataCell fp1, DataCell[] result)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Byte Vector results not implemented");
	}

	/**
	 * Return the result cell for BitVectors - need to check for sparse or dense
	 * byte vector and handle accordingly. {@link #m_fpType} contains the
	 * {@link DataType} of the selected columns
	 * 
	 * @param fp1
	 *            The cell containing the first fingerprint. Guaranteed to be
	 *            {@link BitVectorValue}, non-<code>null</code> and not
	 *            {@link DataType#getMissingCell()} except during node
	 *            initialisation when all exceptions will be trapped
	 * @param result
	 *            An array initialised with the correct number of missing cells,
	 *            into which the required results should be substituted
	 * @throws UnsupportedOperationException
	 *             If the method has not been implemented
	 */
	protected void getBitVectorResultCells(DataCell fp1, DataCell[] result)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Bit Vector results not implemented");
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
			String substringMatch, int startColIdx) throws InvalidSettingsException {
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
				for (DataType type : availableColTypes) {
					if (ithColType == type) {
						isCompatible = true;
						break;
					}
				}
				if (isCompatible && (substringMatch == null
						|| spec.getColumnSpec(i).getName().contains(substringMatch))) {
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
			DataType selectedColType = colSpec.getType();
			boolean isCompatible = false;
			for (DataType type : availableColTypes) {
				if (selectedColType == type) {
					isCompatible = true;
					break;
				}
			}
			if (!isCompatible) {
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
	 * @see org.knime.core.node.NodeModel#saveSettingsTo(org.knime.core.node.
	 * NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_firstColName.saveSettingsTo(settings);
		m_keepInputCols.saveSettingsTo(settings);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#validateSettings(org.knime.core.node.
	 * NodeSettingsRO)
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		m_firstColName.validateSettings(settings);
		m_keepInputCols.validateSettings(settings);

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
		m_firstColName.loadSettingsFrom(settings);
		m_keepInputCols.loadSettingsFrom(settings);

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
