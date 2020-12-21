/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.dialogcomponents;

import java.awt.Color;
import java.util.Date;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DialogComponent;

import com.vernalis.pdbconnector2.dialogcomponents.swing.DateInputPanel;

/**
 * A {@link DialogComponent} to input a data range via two date entry
 * components. The range is bounded, with the bounds determined in the
 * {@link SettingsModelDateRangeBounded} supplied
 *
 * @author S.Roughley knime@vernalis.com
 * 
 * @since 1.28.0
 *
 */
public class DialogComponentDateRangeInput extends
		AbstractDialogComponentRangeInput<DateInputPanel, SettingsModelDateRangeBounded, Date> {

	/**
	 * Constructor with both inputs on the same row
	 * 
	 * @param model
	 *            The settings model, which supplies the current values and the
	 *            bounds
	 * @param label
	 *            Label for the component
	 */
	public DialogComponentDateRangeInput(SettingsModelDateRangeBounded model,
			String label) {
		super(model, label, "Start:", "End:");
	}

	/**
	 * @param model
	 *            The settings model, which supplies the current values and the
	 *            bounds
	 * @param label
	 *            The label for the component
	 * @param splitRows
	 *            Should the Start and End values be on the same row or two
	 *            separate rows?
	 */
	public DialogComponentDateRangeInput(SettingsModelDateRangeBounded model,
			String label, boolean splitRows) {
		super(model, label, "Start:", "End:", splitRows);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#showError(javax.swing.JComponent)
	 */
	@Override
	protected void showError(DateInputPanel date) {

		if (!getModel().isEnabled()) {
			// don't show no error, if the model is not enabled.
			return;
		}

		date.setEditorForeground(Color.RED);
		date.requestFocusInWindow();

		// change the color back as soon as he changes something
		date.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				clearError(date);
				date.removeChangeListener(this);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#clearError(javax.swing.JComponent)
	 */
	@Override
	protected void clearError(DateInputPanel date) {
		date.setEditorForeground(DEFAULT_FG);
		date.setEditorBackground(DEFAULT_BG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#createComponent(java.lang.Comparable,
	 * java.lang.Comparable, java.lang.Comparable)
	 */
	@Override
	protected DateInputPanel createComponent(Date value, Date minbound,
			Date maxbound) {
		return new DateInputPanel(value, minbound, maxbound);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#setComponentMinimumBound(javax.swing.
	 * JComponent, java.lang.Comparable)
	 */
	@Override
	protected void setComponentMinimumBound(DateInputPanel comp,
			Date lowerBound) {
		comp.setMinimumDate(lowerBound);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#setComponentMaximumBound(javax.swing.
	 * JComponent, java.lang.Comparable)
	 */
	@Override
	protected void setComponentMaximumBound(DateInputPanel comp,
			Date upperBound) {
		comp.setMaximumDate(upperBound);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#setComponentValue(javax.swing.
	 * JComponent, java.lang.Comparable)
	 */
	@Override
	protected void setComponentValue(DateInputPanel comp, Date value) {
		comp.setCurrentDate(value);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#getComponentValue(javax.swing.
	 * JComponent)
	 */
	@Override
	protected Date getComponentValue(DateInputPanel comp) {
		return comp.getCurrentDate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#createNewModel(java.lang.String,
	 * java.lang.Comparable, java.lang.Comparable, java.lang.Comparable,
	 * java.lang.Comparable)
	 */
	@Override
	protected SettingsModelDateRangeBounded createNewModel(String configName,
			Date minValue, Date maxValue, Date lowerBound, Date upperBound)
			throws Exception {
		return new SettingsModelDateRangeBounded(configName, minValue, maxValue,
				lowerBound, upperBound);
	}

}
