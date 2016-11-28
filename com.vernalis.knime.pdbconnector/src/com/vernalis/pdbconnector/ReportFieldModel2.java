/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd, based on earlier PDB Connector work.
 * 
 * Copyright (c) 2012, 2014 Vernalis (R&D) Ltd and Enspiral Discovery Limited
 * 
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
package com.vernalis.pdbconnector;

import java.util.Iterator;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.pdbconnector.config.ReportField2;
import com.vernalis.pdbconnector.config.StandardReport;

/**
 * ReportFieldModel class.
 *
 * A ReportFieldModel defines the Boolean settings model for a single report field.
 */
public class ReportFieldModel2 {
	private final ReportField2 m_field;
	private final SettingsModelBoolean m_selected;

	/**
	 * Instantiates a new report field model for a given report field.
	 *
	 * @param field the field
	 */
	public ReportFieldModel2(final ReportField2 field) {
		m_field = field;
		m_selected = ComponentFactory2.createSelectionSettingsModel(m_field);
	}

	/**
	 * Checks if report field is selected.
	 *
	 * @return true, if is selected
	 */
	public final boolean isSelected() {
		return m_selected.getBooleanValue();
	}

	/**
	 * Gets the report field.
	 *
	 * @return the field
	 */
	public final ReportField2 getField() {
		return m_field;
	}

	/**
	 * Applies selection trigger.
	 *
	 * The selection state of this model is set to true if any of the report field models passed in is a
	 * trigger for this field AND is selected.
	 *
	 * @param models the report field models
	 * @return new selection state
	 */
	public boolean applyTrigger(final List<ReportFieldModel2> models) {
		m_selected.setBooleanValue(m_field.getDefault());//reset to initial state (mandatory or optional)
		Iterator<ReportFieldModel2> iter = models.iterator();
		while (iter.hasNext() && !m_selected.getBooleanValue()) {
			ReportFieldModel2 model = iter.next();
			if (model.isSelected() && m_field.isTriggered(model.getField())) {
				m_selected.setBooleanValue(true);
			}
		}
		return m_selected.getBooleanValue();
	}

	/**
	 * Applies standard report.
	 *
	 * Overrides the selection state of this model, if a non-customizable standard report is applied.
	 *
	 * @param stdReport the standard report to apply
	 * @return new selection state
	 */
	public boolean applyStandardReport(final StandardReport stdReport) {
		if ( (stdReport != null) && !stdReport.isCustom()) {
			m_selected.setBooleanValue(m_field.isTriggered(stdReport));
		}
		return m_selected.getBooleanValue();
	}

	/**
	 * Saves model settings.
	 *
	 * @param settings the settings
	 */
	public void saveSettingsTo(final NodeSettingsWO settings) {
		m_selected.saveSettingsTo(settings);
	}

	/**
	 * Load validated model settings.
	 *
	 * @param settings the settings
	 * @throws InvalidSettingsException
	 */
	public void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_selected.loadSettingsFrom(settings);
	}

	/**
	 * Validate model settings.
	 *
	 * @param settings the settings
	 * @throws InvalidSettingsException
	 */
	public void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_selected.validateSettings(settings);
	}

}
