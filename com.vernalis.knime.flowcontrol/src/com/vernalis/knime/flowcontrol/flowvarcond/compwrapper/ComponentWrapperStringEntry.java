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

import java.util.EventListener;
import java.util.function.Consumer;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * Component to enter a string in a {@link JTextField}
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class ComponentWrapperStringEntry
		extends ComponentWrapper<JTextField, String, ChangeEvent> {

	/**
	 * Overloaded constructor. The component is not an option
	 * 
	 * @param c
	 *            the {@link JTextField} to wrap
	 * @param id
	 *            the id
	 */
	public ComponentWrapperStringEntry(JTextField c, String id) {
		this(c, id, false);
	}

	/**
	 * Constructor
	 * 
	 * @param c
	 *            the {@link JTextField} to wrap
	 * @param id
	 *            the id
	 * @param isOption
	 *            whether the component is an option
	 */
	public ComponentWrapperStringEntry(JTextField c, String id,
			boolean isOption) {
		super(c, id, isOption);
	}

	@Override
	public void setValue(String value) {
		if (!getValue().equals(value)) {
			getComponent().setText(value);
		}
	}

	@Override
	public String getValue() {
		return getComponent().getText();
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
	public ComponentWrapperStringEntry createClone() {
		return new ComponentWrapperStringEntry(
				new JTextField(getValue(), getComponent().getColumns()),
				getID());
	}

	@Override
	public EventListener registerValueChangeComponentListener(Runnable action) {
		DocumentListener l = new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				action.run();

			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				action.run();

			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				action.run();

			}
		};
		getComponent().getDocument().addDocumentListener(l);
		return l;
	}

	@Override
	public EventListener registerValueChangeComponentListener(
			Consumer<? super ChangeEvent> action) {
		// This workround is because DocumentEvent does not extend EventObject
		// Based on https://stackoverflow.com/a/27190162/6076839
		ChangeListener cl = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				action.accept(e);

			}
		};
		DocumentListener l = new DocumentListener() {

			private int lastChange = 0;
			private int lastNotified = 0;

			@Override
			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);

			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);

			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				lastChange++;
				SwingUtilities.invokeLater(() -> {
					if (lastChange != lastNotified) {
						lastNotified = lastChange;
						cl.stateChanged(new ChangeEvent(getComponent()));
					}
				});

			}
		};
		getComponent().getDocument().addDocumentListener(l);
		return l;
	}

}
