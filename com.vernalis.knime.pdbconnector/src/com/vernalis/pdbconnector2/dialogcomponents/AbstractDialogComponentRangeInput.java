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

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObjectSpec;

import com.vernalis.pdbconnector2.dialogcomponents.swing.Changeable;

/**
 * A {@link DialogComponent} to input a data range via two data entry
 * components. The range is bounded, with the bounds determined in the
 * {@link SettingsModelComparableRangeBounded} supplied
 *
 * @author S.Roughley knime@vernalis.com
 * @param <T>
 *            The type of the min/max dialog components
 * @param <M>
 *            The type of the SettingsModel
 * @param <U>
 *            The type of the component value stored in the model
 * @since 1.28.0
 */
public abstract class AbstractDialogComponentRangeInput<T extends JComponent & Changeable, M extends SettingsModel & SettingsModelComparableRangeBounded<U>, U extends Comparable<U>>
		extends DialogComponent {

	/** The default label for the upper bound */
	protected static final String DEFAULT_UPPER_DIALOG_LABEL = "Max:";
	/** The default label for the lower bound */
	protected static final String DEFAULT_LOWER_DIALOG_LABEL = "Min:";
	private final T minComp;
	private final T maxComp;

	/**
	 * Overloaded constructor with both components on a single row, no label and
	 * default max/min labels
	 * 
	 * @param model
	 *            The settings model
	 */
	public AbstractDialogComponentRangeInput(M model) {
		this(model, false);
	}

	/**
	 * Overloaded constructor with no label and default max/min labels
	 * 
	 * @param model
	 *            The settings model
	 * @param splitRows
	 *            should the two values be on separate rows?
	 */
	public AbstractDialogComponentRangeInput(M model, boolean splitRows) {
		this(model, null, splitRows);
	}

	/**
	 * Overloaded constructor with a label and both value inputs on a single row
	 * and default max/min labels
	 * 
	 * @param model
	 *            The settings model
	 * @param label
	 *            The label to display with the component
	 */
	public AbstractDialogComponentRangeInput(M model, String label) {
		this(model, label, false);
	}

	/**
	 * Overloaded constructor, with default max/min labels
	 * 
	 * @param model
	 *            The settings model
	 * @param label
	 *            The label to display with the component {@code null} means no
	 *            label is displayed
	 * @param splitRows
	 *            should the two values be on separate rows?
	 */
	public AbstractDialogComponentRangeInput(M model, String label,
			boolean splitRows) {
		this(model, label, DEFAULT_LOWER_DIALOG_LABEL,
				DEFAULT_UPPER_DIALOG_LABEL, splitRows);
	}

	/**
	 * Overloaded constructor with both components on one row
	 * 
	 * @param model
	 *            The settings model
	 * @param label
	 *            The label to display with the component {@code null} means no
	 *            label is displayed
	 * @param minPrefix
	 *            The label to display in front of the minimum value
	 * @param maxPrefix
	 *            The label to display in front of the maximum value
	 */
	public AbstractDialogComponentRangeInput(M model, String label,
			String minPrefix, String maxPrefix) {
		this(model, label, minPrefix, maxPrefix, false);
	}

	/**
	 * Full constructor
	 * 
	 * @param model
	 *            The settings model
	 * @param label
	 *            The label to display with the component {@code null} means no
	 *            label is displayed
	 * @param minPrefix
	 *            The label to display in front of the minimum value
	 * @param maxPrefix
	 *            The label to display in front of the maximum value
	 * @param splitRows
	 *            should the two values be on separate rows?
	 */
	public AbstractDialogComponentRangeInput(M model, String label,
			String minPrefix, String maxPrefix, boolean splitRows) {
		super(model);

		// Handle the label
		if (label != null && !label.isEmpty()) {
			getComponentPanel().add(new JLabel(label));
		}

		// A box for the min/max panels
		final Box rowsBox =
				new Box(splitRows ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS);
		// Sort out the minimum input component
		final JPanel minPanel = new JPanel();
		rowsBox.add(minPanel);
		if (minPrefix != null && !minPrefix.isEmpty()) {
			minPanel.add(new JLabel(minPrefix));
		}
		minComp = createComponent(model.getLowerValue(), model.getLowerBound(),
				model.getUpperValue());
		minComp.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateMinModel();

			}
		});
		minPanel.add(minComp);

		// Sort out the maximum input component
		final JPanel maxPanel = new JPanel();
		rowsBox.add(maxPanel);
		if (maxPrefix != null && !maxPrefix.isEmpty()) {
			maxPanel.add(new JLabel(maxPrefix));
		}
		maxComp = createComponent(model.getUpperValue(), model.getLowerValue(),
				model.getUpperBound());
		maxComp.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateMaxModel();

			}
		});
		maxPanel.add(maxComp);

		getComponentPanel().add(rowsBox);
		model.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateComponent();

			}
		});

		// Sync with the model
		updateComponent();
	}

	/**
	 * This method is called by a change to the min component value. The default
	 * implementation sets the lower value of the settings model, and sets the
	 * max component's minimum bound to this value. An error is shown in the min
	 * component in the UI if an exception was thrown
	 */
	protected void updateMinModel() {
		@SuppressWarnings("unchecked")
		final SettingsModelComparableRangeBounded<U> model =
				(SettingsModelComparableRangeBounded<U>) getModel();
		try {
			final U lowerValue = getComponentValue(minComp);
			model.setLowerValue(lowerValue);
			setComponentMinimumBound(maxComp, lowerValue);
		} catch (final Exception e) {
			showError(minComp);
		}

	}

	/**
	 * This method is called by a change to the max component value. The default
	 * implementation sets the upper value of the settings model, and sets the
	 * min component's maximum bound to this value. An error is shown in the max
	 * component in the UI if an exception was thrown
	 */
	protected void updateMaxModel() {
		@SuppressWarnings("unchecked")
		final SettingsModelComparableRangeBounded<U> model =
				(SettingsModelComparableRangeBounded<U>) getModel();
		try {
			// Update the upper value displayed into the model, and change the
			// lower value's
			// upper bound to not allow the dates to crossover
			final U upperValue = getComponentValue(maxComp);
			model.setUpperValue(upperValue);
			setComponentMaximumBound(minComp, upperValue);
		} catch (final Exception e) {
			// It should be impossible to get here
			showError(maxComp);
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
		clearError(minComp);
		clearError(maxComp);

		@SuppressWarnings("unchecked")
		final SettingsModelComparableRangeBounded<U> model =
				(SettingsModelComparableRangeBounded<U>) getModel();
		// Update the input components
		if (!getComponentValue(minComp).equals(model.getLowerValue())) {
			setComponentValue(minComp, model.getLowerValue());
		}
		if (!getComponentValue(maxComp).equals(model.getUpperValue())) {
			setComponentValue(maxComp, model.getUpperValue());
		}

		setEnabledComponents(model.isEnabled());

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
		@SuppressWarnings("unchecked")
		final SettingsModelComparableRangeBounded<U> model =
				(SettingsModelComparableRangeBounded<U>) getModel();
		try {
			createNewModel(model.getConfigKey(), getComponentValue(minComp),
					getComponentValue(maxComp), model.getLowerBound(),
					model.getUpperBound());
		} catch (final Exception e) {
			showError(minComp);
			showError(maxComp);
			throw new InvalidSettingsException(e.getMessage());
		}

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
		// Incoming specs dont matter

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.DialogComponent#
	 * setEnabledComponents(boolean)
	 */
	@Override
	protected void setEnabledComponents(boolean enabled) {
		minComp.setEnabled(enabled);
		maxComp.setEnabled(enabled);

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
		minComp.setToolTipText(text);
		maxComp.setToolTipText(text);

	}

	/**
	 * Method to create an input component. The {@link ChangeListener} must be
	 * registered to the object
	 *
	 * @param value
	 *            The initial display value
	 * @param minbound
	 *            The initial minimum bound
	 * @param maxbound
	 *            The initial maximum bound
	 * @return The {@link Component}
	 */
	protected abstract T createComponent(U value, U minbound, U maxbound);

	/**
	 * Method to set the minimum bound of a component
	 *
	 * @param comp
	 *            The component
	 * @param lowerBound
	 *            The new lowerBound
	 */
	protected abstract void setComponentMinimumBound(T comp, U lowerBound);

	/**
	 * Method to set the maximum bound of a component
	 *
	 * @param comp
	 *            The component
	 * @param upperBound
	 *            The new lowerBound
	 */
	protected abstract void setComponentMaximumBound(T comp, U upperBound);

	/**
	 * Method to set the value displayed by the component
	 *
	 * @param comp
	 *            The component
	 * @param value
	 *            The value to display
	 */
	protected abstract void setComponentValue(T comp, U value);

	/**
	 * Method to get the current value of the component
	 *
	 * @param comp
	 *            The component
	 * @return The value displayed
	 */
	protected abstract U getComponentValue(T comp);

	/**
	 * Method to create a new copy of the SettingsModel during the
	 * {@link #validateSettingsBeforeSave()} method
	 *
	 * @param configName
	 *            the config name
	 * @param minValue
	 *            The minimum value
	 * @param maxValue
	 *            The maximum value
	 * @param lowerBound
	 *            The lower bound
	 * @param upperBound
	 *            The upper bound
	 * @return A copy of the settings model with the current values
	 * @throws Exception
	 *             Any exception thrown during cloning
	 */
	protected abstract M createNewModel(String configName, U minValue,
			U maxValue, U lowerBound, U upperBound) throws Exception;

	/**
	 * @param comp
	 *            The component to show in an error state
	 */
	protected abstract void showError(T comp);

	/**
	 * @param comp
	 *            The component to return to a non-error state
	 */
	protected abstract void clearError(T comp);

}
