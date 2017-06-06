/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.knime.chem.speedysmiles.nodes.manip.hacdesalt;

import static com.vernalis.knime.chem.speedysmiles.helpers.SmilesHelpers.getLargestDataCellByHacComponents;
import static com.vernalis.knime.chem.speedysmiles.helpers.SmilesHelpers.getLongestSmilesStringCell;
import static com.vernalis.knime.chem.speedysmiles.nodes.manip.hacdesalt.SmilesHACLargestComponentDesaltNodeDialog.createKeepFirstOnlyModel;
import static com.vernalis.knime.chem.speedysmiles.nodes.manip.hacdesalt.SmilesHACLargestComponentDesaltNodeDialog.createKeepLongestModel;

import java.util.Set;

import org.knime.chem.types.SmilesCell;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.SetCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.knime.chem.speedysmiles.nodes.manip.abstrct.AbstractSpeedySmilesSingleCellManipNodeModel;

/**
 * This is the model implementation of SmilesHACLargestComponentDesalt. Node to
 * find largest component of SMILES by Heavy Atom Count
 * 
 * @author S Roughley
 */
public class SmilesHACLargestComponentDesaltNodeModel
		extends AbstractSpeedySmilesSingleCellManipNodeModel {

	private final SettingsModelBoolean m_keepFirstOnly = createKeepFirstOnlyModel();
	private final SettingsModelBoolean m_keepLongestSmiles = createKeepLongestModel();

	/**
	 * Constructor for the node model.
	 */
	protected SmilesHACLargestComponentDesaltNodeModel() {
		super(true);
		m_keepLongestSmiles.setEnabled(m_keepFirstOnly.getBooleanValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		super.saveSettingsTo(settings);
		m_keepFirstOnly.saveSettingsTo(settings);
		m_keepLongestSmiles.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		super.loadValidatedSettingsFrom(settings);
		m_keepFirstOnly.loadSettingsFrom(settings);
		m_keepLongestSmiles.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		super.validateSettings(settings);
		m_keepFirstOnly.validateSettings(settings);
		m_keepLongestSmiles.validateSettings(settings);
	}

	@Override
	protected DataCell getResultCell(String SMILES) {
		Set<DataCell> newCells = getLargestDataCellByHacComponents(SMILES);
		if (newCells.isEmpty()) {
			return DataType.getMissingCell();
		}
		if (m_keepFirstOnly.getBooleanValue()) {
			if (m_keepLongestSmiles.isEnabled() && m_keepLongestSmiles.getBooleanValue()
					&& newCells.size() > 1) {
				// The longest SMILES String
				return getLongestSmilesStringCell(newCells);
			}
			// We only return the first, arbitrary cell
			return newCells.iterator().next();
		} else {
			return CollectionCellFactory.createSetCell(newCells);
		}
	}

	@Override
	protected DataType getColumnType() {
		return m_keepFirstOnly.getBooleanValue() ? SmilesCell.TYPE
				: SetCell.getCollectionType(SmilesCell.TYPE);
	}

	@Override
	protected String getColumnNameSuffix() {
		return null;
	}

	@Override
	protected String getColumnNamePrefix() {
		return m_colName.getStringValue() + " (Largest component"
				+ ((m_keepFirstOnly.getBooleanValue()) ? ")" : "(s))");
	}
}
