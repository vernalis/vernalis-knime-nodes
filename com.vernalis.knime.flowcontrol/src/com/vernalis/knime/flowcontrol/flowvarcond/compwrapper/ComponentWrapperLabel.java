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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;
import java.util.function.Consumer;

import javax.swing.JLabel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * A simple non-interactive text component
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class ComponentWrapperLabel
		extends ComponentWrapper<JLabel, String, PropertyChangeEvent> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.flowcontrol.nodes.abstrct.varvalifswitch.
	 * ComponentWrapper#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append(" [ID=");
		builder.append(getID());
		if (getComponent().getText() != null
				&& !getComponent().getText().equals(getID())) {
			builder.append(", Text=");
			builder.append(getComponent().getText());
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Overloaded constructor. The component is not an option
	 * 
	 * @param id
	 *            the label and ID
	 */
	public ComponentWrapperLabel(String id) {
		this(id, id);
	}

	/**
	 * Overloaded constructor
	 * 
	 * @param id
	 *            the label and ID
	 * @param isOption
	 *            whether the component is an option
	 */
	public ComponentWrapperLabel(String id, boolean isOption) {
		this(id, id, isOption);
	}

	/**
	 * Overloaded constructor. The component is not an option
	 * 
	 * @param label
	 *            the label
	 * @param id
	 *            the id
	 */
	public ComponentWrapperLabel(String label, String id) {
		this(new JLabel(label), id);
	}

	/**
	 * Constructor
	 * 
	 * @param label
	 *            the label
	 * @param id
	 *            the id
	 * @param isOption
	 *            whether the component is an option
	 */
	public ComponentWrapperLabel(String label, String id, boolean isOption) {
		this(new JLabel(label), id, isOption);
	}

	/**
	 * Overloaded constructor. The component is not an option
	 * 
	 * @param c
	 *            a {@link JLabel} to wrap
	 * @param id
	 *            the id
	 */
	public ComponentWrapperLabel(JLabel c, String id) {
		this(c, id, false);
	}

	/**
	 * Constructor
	 * 
	 * @param c
	 *            a {@link JLabel} to wrap
	 * @param id
	 *            the id
	 * @param isOption
	 *            whether the component is an option
	 */
	public ComponentWrapperLabel(JLabel c, String id, boolean isOption) {
		super(c, id, isOption);
	}

	@Override
	public String getValue() {
		return null;
	}

	@Override
	public void saveToSettings(NodeSettingsWO settings) {
		//
	}

	@Override
	public void loadFromSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		//

	}

	@Override
	public ComponentWrapperLabel createClone() {
		return new ComponentWrapperLabel(getComponent().getText(), getID());
	}

	@Override
	public void setValue(String value) {
		// T

	}

	@Override
	public EventListener registerValueChangeComponentListener(
			Consumer<? super PropertyChangeEvent> action) {
		PropertyChangeListener l = new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("text")) {
					action.accept(evt);
				}

			}
		};
		getComponent().addPropertyChangeListener(l);
		return l;
	}

	@Override
	public EventListener registerValueChangeComponentListener(Runnable action) {
		PropertyChangeListener l = new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("text")) {
					action.run();
				}

			}
		};
		getComponent().addPropertyChangeListener(l);
		return l;
	}

}
