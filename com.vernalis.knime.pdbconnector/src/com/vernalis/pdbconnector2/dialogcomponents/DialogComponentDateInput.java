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

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.port.PortObjectSpec;

import com.vernalis.pdbconnector2.dialogcomponents.swing.DateInputPanel;

/**
 * A {@link DialogComponent} to allow the user to input a date in DD-MM-YYYY
 * format
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class DialogComponentDateInput extends DialogComponent {

	private final DateInputPanel date;
	private final JLabel label;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            The settings model
	 * @param label
	 *            The label to add to the component
	 */
	public DialogComponentDateInput(SettingsModelDateBounded model,
			String label) {
		super(model);

		this.label = new JLabel(label);
		getComponentPanel().add(this.label);

		date = new DateInputPanel(model.getDate(), model.getMinBound(),
				model.getMaxBound());
		date.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					updateModel();
				} catch (final InvalidSettingsException ise) {
					// Ignore it here...
				}
			}
		});
		((SettingsModelDateBounded) getModel())
				.prependChangeListener(new ChangeListener() {

					@Override
					public void stateChanged(ChangeEvent e) {
						updateComponent();

					}
				});
		getComponentPanel().add(date);
		updateComponent();

	}

	/**
	 * Highlight an error in the input value
	 */
	protected void showError() {

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
				clearError();
				date.removeChangeListener(this);
			}
		});
	}

	/**
	 * Clear an error highlight in the component
	 */
	protected void clearError() {
		date.setEditorForeground(DEFAULT_FG);
		date.setEditorBackground(DEFAULT_BG);
	}

	/**
	 * Update the settings model with the displayed value
	 * 
	 * @throws InvalidSettingsException
	 *             If there was an error updating the model
	 */
	protected void updateModel() throws InvalidSettingsException {
		try {
			((SettingsModelDateBounded) getModel())
					.setDate(date.getCurrentDate());
		} catch (final Exception e) {
			showError();
			throw new InvalidSettingsException(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.defaultnodesettings.DialogComponent#updateComponent()
	 */
	@Override
	protected void updateComponent() {
		final Date mdlDate = ((SettingsModelDateBounded) getModel()).getDate();
		if (mdlDate.getTime() != date.getCurrentDate().getTime()) {
			date.setCurrentDate(mdlDate);
		}
		// also update the enable status of all components...
		setEnabledComponents(getModel().isEnabled());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.DialogComponent#
	 * validateSettingsBeforeSave()
	 */
	@Override
	protected void validateSettingsBeforeSave()
			throws InvalidSettingsException {
		// Make sure we have a valid value
		updateModel();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.DialogComponent#
	 * checkConfigurabilityBeforeLoad(org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	protected void checkConfigurabilityBeforeLoad(PortObjectSpec[] specs)
			throws NotConfigurableException {
		// Nothing to do here

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.DialogComponent#
	 * setEnabledComponents(boolean)
	 */
	@Override
	protected void setEnabledComponents(boolean enabled) {
		date.setEnabled(enabled);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.defaultnodesettings.DialogComponent#setToolTipText(
	 * java.lang.String)
	 */
	@Override
	public void setToolTipText(String text) {
		label.setToolTipText(text);
		date.setToolTipText(text);

	}

}
