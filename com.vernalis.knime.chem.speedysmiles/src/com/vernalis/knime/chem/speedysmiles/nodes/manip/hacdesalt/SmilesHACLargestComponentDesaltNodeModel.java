/*******************************************************************************
 * Copyright (c) 2016, 2018 Vernalis (R&D) Ltd
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
package com.vernalis.knime.chem.speedysmiles.nodes.manip.hacdesalt;

import java.util.Set;

import org.knime.chem.types.SmilesAdapterCell;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.SetCell;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.knime.chem.speedysmiles.nodes.manip.abstrct.AbstractSpeedySmilesSingleCellManipNodeModel;

import static com.vernalis.knime.chem.speedysmiles.helpers.SmilesHelpers.getLargestDataCellByHacComponents;
import static com.vernalis.knime.chem.speedysmiles.helpers.SmilesHelpers.getLongestSmilesStringCell;
import static com.vernalis.knime.chem.speedysmiles.nodes.manip.hacdesalt.SmilesHACLargestComponentDesaltNodeDialog.createKeepFirstOnlyModel;
import static com.vernalis.knime.chem.speedysmiles.nodes.manip.hacdesalt.SmilesHACLargestComponentDesaltNodeDialog.createKeepLongestModel;

/**
 * This is the model implementation of SmilesHACLargestComponentDesalt. Node to
 * find largest component of SMILES by Heavy Atom Count
 * 
 * @author S Roughley
 */
public class SmilesHACLargestComponentDesaltNodeModel
		extends AbstractSpeedySmilesSingleCellManipNodeModel {

	private final SettingsModelBoolean m_keepFirstOnly =
			createKeepFirstOnlyModel();
	private final SettingsModelBoolean m_keepLongestSmiles =
			createKeepLongestModel();

	/**
	 * Constructor for the node model.
	 */
	protected SmilesHACLargestComponentDesaltNodeModel() {
		super(true);
		m_keepLongestSmiles.setEnabled(m_keepFirstOnly.getBooleanValue());
		registerSettingsModel(m_keepFirstOnly);
		registerSettingsModel(m_keepLongestSmiles);
	}

	@Override
	protected DataCell getResultCell(String SMILES) {
		Set<DataCell> newCells = getLargestDataCellByHacComponents(SMILES);
		if (newCells.isEmpty()) {
			return DataType.getMissingCell();
		}
		if (m_keepFirstOnly.getBooleanValue()) {
			if (m_keepLongestSmiles.isEnabled()
					&& m_keepLongestSmiles.getBooleanValue()
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
		return m_keepFirstOnly.getBooleanValue() ? SmilesAdapterCell.RAW_TYPE
				: SetCell.getCollectionType(SmilesAdapterCell.RAW_TYPE);
	}

	@Override
	protected String getColumnNameSuffix() {
		return "Largest component"
				+ ((m_keepFirstOnly.getBooleanValue()) ? "" : "(s)");
	}

	@Override
	protected String getColumnNamePrefix() {
		return null;
	}
}
