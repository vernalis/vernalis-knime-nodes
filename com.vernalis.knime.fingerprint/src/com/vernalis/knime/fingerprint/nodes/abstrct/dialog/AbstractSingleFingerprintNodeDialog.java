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
package com.vernalis.knime.fingerprint.nodes.abstrct.dialog;

import java.util.ArrayList;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.SparseBitVectorCell;
import org.knime.core.data.vector.bytevector.DenseByteVectorCell;
import org.knime.core.data.vector.bytevector.SparseByteVectorCell;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;

/**
 * Abstract <code>NodeDialog</code> for single fingerprint column nodes
 * 
 * 
 * @author S. Roughley
 */
public class AbstractSingleFingerprintNodeDialog extends DefaultNodeSettingsPane {

	protected final ArrayList<DataType> availableColTypes;
	protected final ColumnFilter fpFilter;

	/**
	 * Overloaded constructor allowing all column types
	 */
	public AbstractSingleFingerprintNodeDialog() {
		this(true, true, true, true);
	}

	/**
	 * Fully functional constructor allowing control of incoming column types
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
	public AbstractSingleFingerprintNodeDialog(boolean allowSparseBitVector,
			boolean allowDenseBitVector, boolean allowSparseByteVector,
			boolean allowDenseByteVector) {
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
		fpFilter = new ColumnFilter() {

			@Override
			public boolean includeColumn(DataColumnSpec colSpec) {
				return availableColTypes.contains(colSpec.getType());
			}

			@Override
			public String allFilteredMsg() {
				return "No fingerprint columns of the appropriate types ("
						+ availableColTypes.toString() + ") available";
			}
		};

		addDialogComponent(new DialogComponentColumnNameSelection(createFirstFPColNameModel(),
				"Select the fingerprint column", 0, true, fpFilter));

		addAdditionalColumnSelectors();

		addDialogComponent(
				new DialogComponentBoolean(createKeepInputColumnsModel(), "Keep input columns"));

	}

	/**
	 * Handle to allow subclasses to add additional column selectors between the
	 * 1st fp selector and the 'Keep input columns' option
	 */
	protected void addAdditionalColumnSelectors() {

	}

	/** @return The settings model for the first selected fingerprint column */
	public static SettingsModelString createFirstFPColNameModel() {
		return new SettingsModelString("First Fingerprint Column", null);
	}

	/** Static method to create keep input columns settings model */
	public static SettingsModelBoolean createKeepInputColumnsModel() {
		return new SettingsModelBoolean("Keep input cols", true);
	}
}
