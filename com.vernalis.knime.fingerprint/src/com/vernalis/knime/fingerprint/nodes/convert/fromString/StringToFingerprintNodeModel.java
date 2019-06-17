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
package com.vernalis.knime.fingerprint.nodes.convert.fromString;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.knime.fingerprint.abstrct.FingerprintStringTypes;
import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractStringToFingerprintNodeModel;

public class StringToFingerprintNodeModel extends AbstractStringToFingerprintNodeModel {
	protected final SettingsModelBoolean isByteModel =
			StringToFingerprintNodeDialog.createIsByteModel();

	public StringToFingerprintNodeModel() {
		super(null);
		isByteModel.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateTypes();
			}
		});
		updateTypes();
	}

	/**
	 * 
	 */
	protected void updateTypes() {
		if (isByteModel.getBooleanValue()) {
			m_stringType = FingerprintStringTypes.BYTEVECTOR_STRING;
			isOutputCountFp = true;
		} else {
			m_stringType = FingerprintStringTypes.BITVECTOR_STRING;
			isOutputCountFp = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.fingerprint.nodes.abstrct.model.
	 * AbstractStringToFingerprintNodeModel#getResultCell(java.lang.String)
	 */
	@Override
	protected DataCell getResultCell(String fpString) {
		if (m_stringType == null) {
			updateTypes();
		}
		if (isByteModel.getBooleanValue()) {
			return m_fpType.getDataCellFromCounts(m_stringType.getCountsFromString(fpString));
		}
		return super.getResultCell(fpString);
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {

		return super.configure(inSpecs);
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		super.saveSettingsTo(settings);
		isByteModel.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		super.validateSettings(settings);
		isByteModel.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		super.loadValidatedSettingsFrom(settings);
		isByteModel.loadSettingsFrom(settings);
	}

}
