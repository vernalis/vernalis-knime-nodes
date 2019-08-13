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

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.data.vector.bytevector.ByteVectorValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.fingerprint.nodes.abstrct.dialog.AbstractDoubleFingerprintNodeDialog;

/**
 * Abstract Streamable NodeModel implementation for two fingerprints nodes.
 * Implementing classes need to override at lease one of
 * {@link #getBitVectorResultCell(DataCell, DataCell)} or
 * {@link #getByteVectorResultCell(DataCell, DataCell)}
 * 
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public abstract class AbstractDoubleFingerprintSingleOutNodeModel
		extends AbstractSingleFingerprintSingleOutputNodeModel {

	protected final SettingsModelString m_secondColName =
			AbstractDoubleFingerprintNodeDialog.createSecondFPColNameModel();

	/**
	 * Overloaded constructor for a node accepting any input fingerprint type
	 */
	public AbstractDoubleFingerprintSingleOutNodeModel(DataType resultType) {
		super(resultType);
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
	public AbstractDoubleFingerprintSingleOutNodeModel(boolean allowSparseBitVector,
			boolean allowDenseBitVector, boolean allowSparseByteVector,
			boolean allowDenseByteVector, DataType resultType) {
		super(allowSparseBitVector, allowDenseBitVector, allowSparseByteVector,
				allowDenseByteVector, resultType);

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
		m_secondColName.setStringValue(guessColumnName(inSpec, m_secondColName.getStringValue(),
				null, inSpec.findColumnIndex(m_firstColName.getStringValue()) - 1));
		// We need both fingerprints to be Bit or Byte
		if (!inSpec.getColumnSpec(m_firstColName.getStringValue()).getType()
				.isCompatible(inSpec.getColumnSpec(m_secondColName.getStringValue()).getType()
						.getPreferredValueClass())) {
			throw new InvalidSettingsException("The two columns need to be of compatible types!");
		}
		m_fpType = inSpec.getColumnSpec(m_firstColName.getStringValue()).getType();
		ColumnRearranger result = new ColumnRearranger(inSpec);
		if (!m_keepInputCols.getBooleanValue()) {
			result.remove(m_firstColName.getStringValue());
			result.remove(m_secondColName.getStringValue());
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
		final int fp2Idx = inSpec.findColumnIndex(m_secondColName.getStringValue());
		result.append(getCellFactory(newColSpecs, fp1Idx, fp2Idx));
		return result;
	}

	/**
	 * @param firstFpColIdx
	 * @param newColSpecs
	 * @return
	 */
	@Override
	protected AbstractCellFactory getCellFactory(DataColumnSpec[] newColSpecs, int... fpColIdx) {
		return new SingleCellFactory(true, newColSpecs[0]) {

			@Override
			public DataCell getCell(DataRow row) {
				DataCell fp1 = row.getCell(fpColIdx[0]);
				DataCell fp2 = row.getCell(fpColIdx[1]);

				if (fp1.isMissing() || fp2.isMissing()) {
					return DataType.getMissingCell();
				}
				if (fp1 instanceof BitVectorValue) {
					return getBitVectorResultCell(fp1, fp2);
				} else if (fp1 instanceof ByteVectorValue) {
					return getByteVectorResultCell(fp1, fp2);
				}
				// Otherwise not a fingerprint cell
				throw new RuntimeException(
						"The supplied cell appears not to be a recognised fingerprint format");

			}
		};
	}

	protected DataCell getByteVectorResultCell(DataCell fp1, DataCell fp2)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Byte Vector results not implemented");
	}

	protected DataCell getBitVectorResultCell(DataCell fp1, DataCell fp2)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Bit Vector results not implemented");
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		super.saveSettingsTo(settings);
		m_secondColName.saveSettingsTo(settings);

	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		super.validateSettings(settings);
		m_secondColName.validateSettings(settings);

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		super.loadValidatedSettingsFrom(settings);
		m_secondColName.loadSettingsFrom(settings);

	}

	@Override
	protected final DataCell getByteVectorResultCell(DataCell fp1)
			throws UnsupportedOperationException {
		// We have to do something which is never called to stop the node
		// intantiation failing!
		return null;
	}

	@Override
	protected final DataCell getBitVectorResultCell(DataCell fp1)
			throws UnsupportedOperationException {
		// We have to do something which is never called to stop the node
		// intantiation failing!
		return null;
	}

}
