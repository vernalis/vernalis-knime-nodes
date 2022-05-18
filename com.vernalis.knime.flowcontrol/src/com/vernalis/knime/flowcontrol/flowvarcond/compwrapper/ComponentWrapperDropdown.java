/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.flowcontrol.flowvarcond.compwrapper;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EventListener;
import java.util.function.Consumer;

import javax.swing.JComboBox;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * A component wrapping a dropdown to choose a value from
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class ComponentWrapperDropdown
		extends ComponentWrapper<JComboBox<String>, String, ItemEvent> {

	/**
	 * Constuctor
	 * 
	 * @param id
	 *            the component ID
	 * @param isOption
	 *            is the component an option
	 * @param values
	 *            the values for the dropdown
	 */
	public ComponentWrapperDropdown(String id, boolean isOption,
			String... values) {
		this(new JComboBox<>(values), id, isOption);
	}

	/**
	 * Constructor
	 * 
	 * @param c
	 *            A {@link JComboBox} to wrap
	 * @param id
	 *            the component ID
	 * @param isOption
	 *            is the component an option
	 */
	public ComponentWrapperDropdown(JComboBox<String> c, String id,
			boolean isOption) {
		super(c, id, isOption);
	}

	/**
	 * Overloaded constructor. The component is not an option
	 * 
	 * @param c
	 *            the {@link JComboBox} to wrap
	 * @param id
	 *            the component ID
	 */
	public ComponentWrapperDropdown(JComboBox<String> c, String id) {
		super(c, id);
	}

	@Override
	public void setValue(String value) {
		getComponent().setSelectedItem(value);

	}

	@Override
	public String getValue() {
		return (String) getComponent().getSelectedItem();
	}

	@Override
	public EventListener registerValueChangeComponentListener(
			Consumer<? super ItemEvent> action) {
		ItemListener l = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				action.accept(e);

			}
		};
		getComponent().addItemListener(l);
		return l;
	}

	@Override
	public EventListener registerValueChangeComponentListener(Runnable action) {
		ItemListener l = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				action.run();

			}
		};
		getComponent().addItemListener(l);
		return l;
	}

	@Override
	public void saveToSettings(NodeSettingsWO settings)
			throws InvalidSettingsException {
		settings.addString(getID(), getValue());
	}

	@Override
	public void loadFromSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		setValue(settings.getString(getID()));

	}

	@Override
	public ComponentWrapperDropdown createClone() {
		String[] items = new String[getComponent().getItemCount()];
		for (int i = 0; i < items.length; i++) {
			items[i] = getComponent().getItemAt(i);
		}
		JComboBox<String> combo = new JComboBox<>(items);
		combo.setSelectedItem(getValue());
		return new ComponentWrapperDropdown(combo, getValue(), isOption());
	}

}
