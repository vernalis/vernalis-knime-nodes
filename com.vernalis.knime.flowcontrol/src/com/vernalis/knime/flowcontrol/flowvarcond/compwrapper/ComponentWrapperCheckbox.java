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

import javax.swing.JCheckBox;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * A Checkbox wrapper
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class ComponentWrapperCheckbox
		extends ComponentWrapper<JCheckBox, Boolean, ItemEvent> {

	/**
	 * Overloaded constructor showing the ID as the checkbox label and not
	 * selected, and not an option component
	 * 
	 * @param id
	 *            the ID
	 */
	public ComponentWrapperCheckbox(String id) {
		this(id, false);
	}

	/**
	 * Overloaded constructor with the ID as checkbox label and not selected
	 * 
	 * @param id
	 *            the ID
	 * @param isOption
	 *            whether the component is an option
	 */
	public ComponentWrapperCheckbox(String id, boolean isOption) {
		this(new JCheckBox(id), id, isOption);
	}

	/**
	 * Overloaded constructor with the ID as checkbox label
	 * 
	 * @param id
	 *            the ID
	 * @param selected
	 *            whether the checkbox is selected
	 * @param isOption
	 *            whether the component is an option
	 */
	public ComponentWrapperCheckbox(String id, boolean selected,
			boolean isOption) {
		this(new JCheckBox(id, selected), id, isOption);
	}

	/**
	 * Overloaded constructor for a non-option wrapper
	 * 
	 * @param c
	 *            a {@link JCheckBox} to display
	 * @param id
	 *            the ID
	 */
	public ComponentWrapperCheckbox(JCheckBox c, String id) {
		this(c, id, false);
	}

	/**
	 * Full Constructor
	 * 
	 * @param c
	 *            a {@link JCheckBox} to display
	 * @param id
	 *            the ID
	 * @param isOption
	 *            whether the component is an option
	 */
	public ComponentWrapperCheckbox(JCheckBox c, String id, boolean isOption) {
		super(c, id, isOption);

	}

	@Override
	public void setValue(Boolean value) {
		getComponent().setSelected(value.booleanValue());

	}

	@Override
	public Boolean getValue() {
		return getComponent().isSelected();
	}

	@Override
	public void saveToSettings(NodeSettingsWO settings)
			throws InvalidSettingsException {
		settings.addBoolean(getID(), getValue());

	}

	@Override
	public void loadFromSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		setValue(settings.getBoolean(getID()));

	}

	@Override
	public ComponentWrapperCheckbox createClone() {
		return new ComponentWrapperCheckbox(
				new JCheckBox(getComponent().getText(), getValue()), getID());
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

}
