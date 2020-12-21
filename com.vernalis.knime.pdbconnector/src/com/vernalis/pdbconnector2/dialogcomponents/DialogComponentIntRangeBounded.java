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

import com.vernalis.pdbconnector2.dialogcomponents.swing.ChangeableIntSpinner;

/**
 * Dialog component to enter a bound {@link Integer} range. The values are
 * bounded - the bounds are supplied by the
 * {@link SettingsModelIntegerRangeBounded}
 * 
 * @author S.Roughley knime@vernalis.com
 * 
 * @since 1.28.0
 *
 */
public class DialogComponentIntRangeBounded extends
		AbstractDialogComponentRangeInput<ChangeableIntSpinner, SettingsModelIntegerRangeBounded, Integer> {

	private final int stepSize;

	/**
	 * Overloaded constructor with both values on a single line and no label
	 * 
	 * @param model
	 *            The settings model
	 * @param stepSize
	 *            The step size of the spinners in the inputs
	 */
	public DialogComponentIntRangeBounded(
			SettingsModelIntegerRangeBounded model, int stepSize) {
		this(model, stepSize, false);
	}

	/**
	 * Overloaded constructor with optionally the upper / lower inputs on
	 * separate rows and no label
	 * 
	 * @param model
	 *            The settings model
	 * @param stepSize
	 *            The step size of the spinners in the inputs
	 * @param splitRows
	 *            SHould the min/max value inputs be on separate lines?
	 */
	public DialogComponentIntRangeBounded(
			SettingsModelIntegerRangeBounded model, int stepSize,
			boolean splitRows) {
		super(model, splitRows);
		this.stepSize = stepSize;
	}

	/**
	 * Overloaded constructor with the upper / lower inputs on a single row, and
	 * non-default labels for the inputs
	 * 
	 * @param model
	 *            The settings model
	 * @param label
	 *            The label to display. {@code null} or an empty string will
	 *            result in no label being displayed
	 * @param minPrefix
	 *            The label to prefix the minimum value input with
	 * @param maxPrefix
	 *            The label to prefix the maximum value input with
	 * @param stepSize
	 *            The step size of the spinners in the inputs
	 */
	public DialogComponentIntRangeBounded(
			SettingsModelIntegerRangeBounded model, String label,
			String minPrefix, String maxPrefix, int stepSize) {
		super(model, label, minPrefix, maxPrefix);
		this.stepSize = stepSize;
	}

	/**
	 * Overloaded constructor with both values on a single line
	 * 
	 * @param model
	 *            The settings model
	 * @param label
	 *            The label to display. {@code null} or an empty string will
	 *            result in no label being displayed
	 * @param stepSize
	 *            The step size of the spinners in the inputs
	 */
	public DialogComponentIntRangeBounded(
			SettingsModelIntegerRangeBounded model, String label,
			int stepSize) {
		super(model, label);
		this.stepSize = stepSize;
	}

	/**
	 * Full constructor with optionally the upper / lower inputs on separate
	 * rows, and non-default labels for the inputs
	 * 
	 * @param model
	 *            The settings model
	 * @param label
	 *            The label to display. {@code null} or an empty string will
	 *            result in no label being displayed
	 * @param minPrefix
	 *            The label to prefix the minimum value input with
	 * @param maxPrefix
	 *            The label to prefix the maximum value input with
	 * @param stepSize
	 *            The step size of the spinners in the inputs
	 * @param splitRows
	 *            SHould the min/max value inputs be on separate lines?
	 */
	public DialogComponentIntRangeBounded(
			SettingsModelIntegerRangeBounded model, String label,
			String minPrefix, String maxPrefix, boolean splitRows,
			int stepSize) {
		super(model, label, minPrefix, maxPrefix, splitRows);
		this.stepSize = stepSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#createComponent(java.lang.Comparable,
	 * java.lang.Comparable, java.lang.Comparable)
	 */
	@Override
	protected ChangeableIntSpinner createComponent(Integer value,
			Integer minbound, Integer maxbound) {
		return new ChangeableIntSpinner(value.intValue(), minbound.intValue(),
				maxbound.intValue(), stepSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#setComponentMinimumBound(javax.swing.
	 * JComponent, java.lang.Comparable)
	 */
	@Override
	protected void setComponentMinimumBound(ChangeableIntSpinner comp,
			Integer lowerBound) {
		comp.setMin(lowerBound.intValue());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#setComponentMaximumBound(javax.swing.
	 * JComponent, java.lang.Comparable)
	 */
	@Override
	protected void setComponentMaximumBound(ChangeableIntSpinner comp,
			Integer upperBound) {
		comp.setMax(upperBound.intValue());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#setComponentValue(javax.swing.
	 * JComponent, java.lang.Comparable)
	 */
	@Override
	protected void setComponentValue(ChangeableIntSpinner comp, Integer value) {
		comp.setValue(value.intValue());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#getComponentValue(javax.swing.
	 * JComponent)
	 */
	@Override
	protected Integer getComponentValue(ChangeableIntSpinner comp) {
		return comp.getIntValue();
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
	protected SettingsModelIntegerRangeBounded createNewModel(String configName,
			Integer minValue, Integer maxValue, Integer lowerBound,
			Integer upperBound) throws Exception {
		return new SettingsModelIntegerRangeBounded(configName, minValue,
				maxValue, lowerBound, upperBound);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#showError(javax.swing.JComponent)
	 */
	@Override
	protected void showError(ChangeableIntSpinner comp) {
		comp.getEditor().setForeground(Color.RED);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#clearError(javax.swing.JComponent)
	 */
	@Override
	protected void clearError(ChangeableIntSpinner comp) {
		comp.getEditor().setForeground(DEFAULT_FG);

	}

}
