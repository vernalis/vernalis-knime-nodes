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
package com.vernalis.knime.chem.speedysmiles.nodes.count.abstrct;

import static com.vernalis.knime.chem.speedysmiles.nodes.count.abstrct.AbstractSpeedySmilesEnumBasedCountNodeDialog.createElementModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.knime.chem.speedysmiles.helpers.CalculatedProperty;

/**
 * This is the model implementation of SpeedySMILES count, using an enum
 * implementation of {@link CalculatedProperty}
 * 
 * @author S Roughley
 */
public class AbstractSpeedySmilesEnumBasedCountNodeModel<T extends Enum<T> & CalculatedProperty<U>, U extends Number>
		extends AbstractSpeedySmilesCountNodeModel {

	protected final SettingsModelBoolean[] m_elements;
	protected T[] ELEMENTS;
	/**
	 * The actual elements to count
	 */
	protected List<T> elemsToCount = new ArrayList<>();

	/**
	 * Constructor for the node model.
	 * 
	 * @param The
	 *            properties to calculate
	 */
	public AbstractSpeedySmilesEnumBasedCountNodeModel(T[] elements) {
		super();
		ELEMENTS = elements;
		m_elements = new SettingsModelBoolean[ELEMENTS.length];
		for (int i = 0; i < ELEMENTS.length; i++) {
			m_elements[i] = createElementModel(ELEMENTS[i].name());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		// Count the number of elements to count
		// m_elemCount = 0;
		elemsToCount.clear();

		for (int i = 0; i < m_elements.length; i++) {
			if (m_elements[i].getBooleanValue()) {
				elemsToCount.add(ELEMENTS[i]);
			}
		}

		// Deal with none selected
		if (elemsToCount.size() == 0) {
			m_logger.error("No properties selected");
			throw new InvalidSettingsException("No properties selected");
		}

		return super.configure(inSpecs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		super.saveSettingsTo(settings);
		for (int i = 0; i < m_elements.length; i++) {
			m_elements[i].saveSettingsTo(settings);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		super.loadValidatedSettingsFrom(settings);
		for (int i = 0; i < m_elements.length; i++) {
			try {
				m_elements[i].loadSettingsFrom(settings);
			} catch (InvalidSettingsException e) {
				// dont change output if new properties added since node last
				// loaded
				m_elements[i].setBooleanValue(false);
				m_logger.warn("Not all count settings had values - new counts will be ignored");
				setWarningMessage("Not all count settings had values - new counts will be ignored");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		super.validateSettings(settings);
		for (int i = 0; i < m_elements.length; i++) {
			try {
				m_elements[i].validateSettings(settings);
			} catch (InvalidSettingsException e) {
				m_logger.warn("Not all count settings had values - new counts will be ignored");
				setWarningMessage("Not all count settings had values - new counts will be ignored");
			}
		}
	}

	@Override
	protected String[] getColumnNameSuffixes() {
		String[] retVal = new String[elemsToCount.size()];
		Arrays.fill(retVal, "");
		return retVal;
	}

	@Override
	protected String[] getColumnNamePrefixes() {
		return elemsToCount.stream().map(x -> x.displayName()).toArray(String[]::new);

	}

	@Override
	protected Integer[] getResultCounts(String SMILES, int numCols) {
		return elemsToCount.stream().map(x -> x.calculate(SMILES)).toArray(Integer[]::new);
	}

}
