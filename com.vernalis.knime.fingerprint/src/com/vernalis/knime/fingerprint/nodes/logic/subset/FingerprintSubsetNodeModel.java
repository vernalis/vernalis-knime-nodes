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
package com.vernalis.knime.fingerprint.nodes.logic.subset;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCellFactory;
import org.knime.core.data.vector.bitvector.SparseBitVectorCell;
import org.knime.core.data.vector.bitvector.SparseBitVectorCellFactory;
import org.knime.core.data.vector.bytevector.ByteVectorValue;
import org.knime.core.data.vector.bytevector.DenseByteVectorCell;
import org.knime.core.data.vector.bytevector.DenseByteVectorCellFactory;
import org.knime.core.data.vector.bytevector.SparseByteVectorCell;
import org.knime.core.data.vector.bytevector.SparseByteVectorCellFactory;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractSingleFingerprintSingleOutputNodeModel;

import static com.vernalis.knime.fingerprint.nodes.logic.subset.FingerprintSubsetNodeDialog.createEndModel;
import static com.vernalis.knime.fingerprint.nodes.logic.subset.FingerprintSubsetNodeDialog.createStartModel;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public class FingerprintSubsetNodeModel extends AbstractSingleFingerprintSingleOutputNodeModel {
	SettingsModelIntegerBounded m_Start = createStartModel();
	SettingsModelIntegerBounded m_End = createEndModel();

	public FingerprintSubsetNodeModel() {
		// Same as input type
		super(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getResultColumnName() {
		return m_firstColName.getStringValue() + " (Subset " + m_Start.getIntValue() + " - "
				+ ((m_End.getIntValue() < 0) ? "End)" : (m_End.getIntValue() + ")"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataCell getByteVectorResultCell(DataCell fp1) {
		int start = m_Start.getIntValue();
		int end = m_End.getIntValue();
		if (end == -1 || end > ((ByteVectorValue) fp1).length()) {
			end = (int) ((ByteVectorValue) fp1).length();
		}
		if (start > ((ByteVectorValue) fp1).length()) {
			start = (int) ((ByteVectorValue) fp1).length();
		}

		if (fp1.getType() == DenseByteVectorCell.TYPE) {
			DenseByteVectorCellFactory fact =
					new DenseByteVectorCellFactory((DenseByteVectorCell) fp1, start, end);
			return fact.createDataCell();
		}
		SparseByteVectorCellFactory fact =
				new SparseByteVectorCellFactory((SparseByteVectorCell) fp1, start, end);
		return fact.createDataCell();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataCell getBitVectorResultCell(DataCell fp1) {
		int start = m_Start.getIntValue();
		long end = m_End.getIntValue();
		if (end == -1 || end > ((BitVectorValue) fp1).length()) {
			end = ((BitVectorValue) fp1).length();
		}
		if (start > ((BitVectorValue) fp1).length()) {
			start = (int) ((BitVectorValue) fp1).length();
		}
		if (fp1.getType() == DenseBitVectorCell.TYPE) {
			DenseBitVectorCellFactory fact =
					new DenseBitVectorCellFactory((DenseBitVectorCell) fp1, start, end);
			return fact.createDataCell();
		}
		SparseBitVectorCellFactory fact =
				new SparseBitVectorCellFactory((SparseBitVectorCell) fp1, start, end);
		return fact.createDataCell();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		int start = m_Start.getIntValue();
		int end = m_End.getIntValue();
		if (end >= 0 && start > end) {
			m_logger.error("Start must be before the end!");
			throw new InvalidSettingsException("Start must be before the end!");
		}
		if (end == -1 && start == 0) {
			m_logger.warn("The fingerprint will be returned unchanged!");
			setWarningMessage("The fingerprint will be returned unchanged!");
		}
		return super.configure(inSpecs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		super.saveSettingsTo(settings);
		m_End.saveSettingsTo(settings);
		m_Start.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		super.validateSettings(settings);
		m_End.validateSettings(settings);
		m_Start.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		super.loadValidatedSettingsFrom(settings);
		m_End.loadSettingsFrom(settings);
		m_Start.loadSettingsFrom(settings);
	}

}
