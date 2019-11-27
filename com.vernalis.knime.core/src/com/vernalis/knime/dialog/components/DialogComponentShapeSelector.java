/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.knime.dialog.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.property.ShapeFactory;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.DefaultStringIconOption;
import org.knime.core.node.util.StringIconListCellRenderer;
import org.knime.core.node.util.StringIconOption;

/**
 * A simple dialog component to select a shape from a combobox. Based closely on
 * {@link DialogComponentStringSelection}
 * 
 * @author S.Roughley
 *
 */
public class DialogComponentShapeSelector extends DialogComponent {

	@SuppressWarnings("rawtypes")
	private final JComboBox m_combobox;

	private final JLabel m_label;

	/**
	 * Constructor for a list for shape selection
	 *
	 * @param shapeModel
	 *            the model that stores the value for this component.
	 * @param label
	 *            label for dialog in front of combobox
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DialogComponentShapeSelector(final SettingsModelShape shapeModel, final String label) {

		super(shapeModel);

		m_label = new JLabel(label);
		getComponentPanel().add(m_label);
		m_combobox = new JComboBox();
		m_combobox.setRenderer(new StringIconListCellRenderer());
		ShapeFactory.getShapes().stream()
				.map(x -> new DefaultStringIconOption(x.toString(), x.getIcon()))
				.filter(x -> x != null).forEach(x -> m_combobox.addItem(x));
		getComponentPanel().add(m_combobox);

		m_combobox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					try {
						updateModel();
					} catch (final InvalidSettingsException ise) {
						// ignore
					}
				}
			}
		});

		// we need to update the selection, when the model changes.
		((SettingsModelShape) getModel()).prependChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				updateComponent();
			}
		});

		// call this method to be in sync with the settings model
		updateComponent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateComponent() {
		final String strVal = ((SettingsModelShape) getModel()).getStringValue();
		StringIconOption val = null;
		if (strVal == null) {
			val = null;
		} else {
			for (int i = 0, length = m_combobox.getItemCount(); i < length; i++) {
				final StringIconOption curVal = (StringIconOption) m_combobox.getItemAt(i);
				if (curVal.getText().equals(strVal)) {
					val = curVal;
					break;
				}
			}
			if (val == null) {
				val = new DefaultStringIconOption(strVal);
			}
		}
		boolean update;
		if (val == null) {
			update = m_combobox.getSelectedItem() != null;
		} else {
			update = !val.equals(m_combobox.getSelectedItem());
		}
		if (update) {
			m_combobox.setSelectedItem(val);
		}
		// also update the enable status
		setEnabledComponents(getModel().isEnabled());

		// make sure the model is in sync (in case model value isn't selected)
		StringIconOption selItem = (StringIconOption) m_combobox.getSelectedItem();
		try {
			if ((selItem == null && strVal != null)
					|| (selItem != null && !selItem.getText().equals(strVal))) {
				// if the (initial) value in the model is not in the list
				updateModel();
			}
		} catch (InvalidSettingsException e) {
			// ignore invalid values here
		}
	}

	/**
	 * Transfers the current value from the component into the model.
	 */
	private void updateModel() throws InvalidSettingsException {

		if (m_combobox.getSelectedItem() == null) {
			((SettingsModelShape) getModel()).setStringValue(null);
			m_combobox.setBackground(Color.RED);
			// put the color back to normal with the next selection.
			m_combobox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					m_combobox.setBackground(DialogComponent.DEFAULT_BG);
				}
			});
			throw new InvalidSettingsException("Please select an item from the list.");
		}
		// we transfer the value from the field into the model
		((SettingsModelShape) getModel())
				.setStringValue(((StringIconOption) m_combobox.getSelectedItem()).getText());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettingsBeforeSave() throws InvalidSettingsException {
		updateModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void checkConfigurabilityBeforeLoad(final PortObjectSpec[] specs)
			throws NotConfigurableException {
		// we are always good.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setEnabledComponents(final boolean enabled) {
		m_combobox.setEnabled(enabled);
	}

	/**
	 * Sets the preferred size of the internal component.
	 *
	 * @param width
	 *            The width.
	 * @param height
	 *            The height.
	 */
	public void setSizeComponents(final int width, final int height) {
		m_combobox.setPreferredSize(new Dimension(width, height));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setToolTipText(final String text) {
		m_label.setToolTipText(text);
		m_combobox.setToolTipText(text);
	}

}
