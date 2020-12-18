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
package com.vernalis.pdbconnector2.dialogcomponents.swing;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * A {@link JSpinner} implementation which also implements {@link Changeable}
 * and stores a {@code double} value
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class ChangeableDoubleSpinner extends JSpinner implements Changeable {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param val
	 *            The initial value to display
	 * @param min
	 *            The minimum value
	 * @param max
	 *            The maximum value
	 * @param stepSize
	 *            The step size
	 */
	public ChangeableDoubleSpinner(double val, double min, double max,
			double stepSize) {
		super(new SpinnerNumberModel(val, min, max, stepSize));

	}

	@Override
	public SpinnerNumberModel getModel() {
		return (SpinnerNumberModel) super.getModel();
	}

	/**
	 * @return The displayed value
	 */
	public double getDoubleValue() {
		return ((Double) getValue()).doubleValue();
	}

	/**
	 * Set a new value to display
	 * 
	 * @param val
	 *            the new value
	 */
	public void setValue(double val) {
		super.setValue(val);
	}

	/**
	 * Set a new minimum value
	 * 
	 * @param min
	 *            The new minimum
	 */
	public void setMin(double min) {
		getModel().setMinimum(Double.valueOf(min));
	}

	/**
	 * Set a new maximum
	 * 
	 * @param max
	 *            The new maximum
	 */
	public void setMax(double max) {
		getModel().setMaximum(Double.valueOf(max));
	}
}
