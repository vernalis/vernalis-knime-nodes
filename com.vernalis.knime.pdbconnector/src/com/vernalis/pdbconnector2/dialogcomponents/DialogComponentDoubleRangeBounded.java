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

import com.vernalis.pdbconnector2.dialogcomponents.swing.ChangeableDoubleSpinner;

/**
 * Dialog component to enter a bound {@link Double} range. The values are
 * bounded - the bounds are supplied by the
 * {@link SettingsModelDoubleRangeBounded}
 * 
 * @author S.Roughley knime@vernalis.com
 * 
 * @since 1.28.0
 *
 */
public class DialogComponentDoubleRangeBounded extends
		AbstractDialogComponentRangeInput<ChangeableDoubleSpinner, SettingsModelDoubleRangeBounded, Double> {

	private final double stepSize;

	/**
	 * Overloaded constructor with both values on a single line and no label
	 * 
	 * @param model
	 *            The settings model
	 * @param stepSize
	 *            The step size of the spinners in the inputs
	 */
	public DialogComponentDoubleRangeBounded(
			SettingsModelDoubleRangeBounded model, double stepSize) {
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
	public DialogComponentDoubleRangeBounded(
			SettingsModelDoubleRangeBounded model, double stepSize,
			boolean splitRows) {
		super(model, splitRows);
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
	public DialogComponentDoubleRangeBounded(
			SettingsModelDoubleRangeBounded model, String label,
			double stepSize) {
		this(model, label, stepSize, false);
	}

	/**
	 * Overloaded constructor with optionally the upper / lower inputs on
	 * separate rows
	 * 
	 * @param model
	 *            The settings model
	 * @param label
	 *            The label to display. {@code null} or an empty string will
	 *            result in no label being displayed
	 * @param stepSize
	 *            The step size of the spinners in the inputs
	 * @param splitRows
	 *            SHould the min/max value inputs be on separate lines?
	 */
	public DialogComponentDoubleRangeBounded(
			SettingsModelDoubleRangeBounded model, String label,
			double stepSize, boolean splitRows) {
		super(model, label, splitRows);
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
	public DialogComponentDoubleRangeBounded(
			SettingsModelDoubleRangeBounded model, String label,
			String minPrefix, String maxPrefix, double stepSize) {
		this(model, label, minPrefix, maxPrefix, stepSize, false);
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
	public DialogComponentDoubleRangeBounded(
			SettingsModelDoubleRangeBounded model, String label,
			String minPrefix, String maxPrefix, double stepSize,
			boolean splitRows) {
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
	protected ChangeableDoubleSpinner createComponent(Double value,
			Double minbound, Double maxbound) {
		return new ChangeableDoubleSpinner(value.doubleValue(),
				minbound.doubleValue(), maxbound.doubleValue(), stepSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#setComponentMinimumBound(javax.swing.
	 * JComponent, java.lang.Comparable)
	 */
	@Override
	protected void setComponentMinimumBound(ChangeableDoubleSpinner comp,
			Double lowerBound) {
		comp.setMin(lowerBound.doubleValue());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#setComponentMaximumBound(javax.swing.
	 * JComponent, java.lang.Comparable)
	 */
	@Override
	protected void setComponentMaximumBound(ChangeableDoubleSpinner comp,
			Double upperBound) {
		comp.setMax(upperBound.doubleValue());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#setComponentValue(javax.swing.
	 * JComponent, java.lang.Comparable)
	 */
	@Override
	protected void setComponentValue(ChangeableDoubleSpinner comp,
			Double value) {
		comp.setValue(value.doubleValue());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#getComponentValue(javax.swing.
	 * JComponent)
	 */
	@Override
	protected Double getComponentValue(ChangeableDoubleSpinner comp) {
		return comp.getDoubleValue();
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
	protected SettingsModelDoubleRangeBounded createNewModel(String configName,
			Double minValue, Double maxValue, Double lowerBound,
			Double upperBound) throws Exception {
		return new SettingsModelDoubleRangeBounded(configName, minValue,
				maxValue, lowerBound, upperBound);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#showError(javax.swing.JComponent)
	 */
	@Override
	protected void showError(ChangeableDoubleSpinner comp) {
		comp.getEditor().setForeground(Color.RED);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.
	 * AbstractDialogComponentRangeInput#clearError(javax.swing.JComponent)
	 */
	@Override
	protected void clearError(ChangeableDoubleSpinner comp) {
		comp.setForeground(DEFAULT_FG);

	}

}
