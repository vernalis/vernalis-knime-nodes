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

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.FlowVariableModelButton;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

/**
 * A {@link DialogComponent} to allow long string inputs with line-wrapping
 * 
 * @author S.Roughley knime@vernalis.com
 * 
 * @since 1.28.0
 *
 */
public class DialogComponentLineWrappedStringInput extends DialogComponent {

	private final JTextArea textPanel;
	private final FlowVariableModelButton fvmButton;
	private final boolean disallowEmptyString;

	/**
	 * Constructur
	 * 
	 * @param model
	 *            The settings model to store the value
	 * @param label
	 *            The label
	 * @param rows
	 *            The number of rows for the input area
	 * @param columns
	 *            The number of columns for the input area
	 * @param wrapTextAtWords
	 *            Should the text be wrapped at word breaks?
	 * @param disallowEmptyString
	 *            Are empty strings allowed
	 * @param fvm
	 *            Optional {@link FlowVariableModel}. If a value is supplied,
	 *            then a {@link FlowVariableModelButton} will be displayed with
	 *            the component
	 */
	public DialogComponentLineWrappedStringInput(SettingsModelString model,
			String label, int rows, int columns, boolean wrapTextAtWords,
			boolean disallowEmptyString, FlowVariableModel fvm) {
		super(model);
		this.disallowEmptyString = disallowEmptyString;
		getComponentPanel().add(new JLabel(label));
		textPanel = new JTextArea(rows, columns);
		textPanel.setLineWrap(true);
		textPanel.setWrapStyleWord(wrapTextAtWords);
		updateComponent();
		textPanel.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				try {
					updateModel(true);
				} catch (final InvalidSettingsException ise) {
					// Ignore!
				}
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				try {
					updateModel(true);
				} catch (final InvalidSettingsException ise) {
					// Ignore!
				}
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				try {
					updateModel(true);
				} catch (final InvalidSettingsException ise) {
					// Ignore!
				}
			}
		});
		getComponentPanel().add(textPanel);

		if (fvm != null) {
			fvmButton = new FlowVariableModelButton(fvm);
			getComponentPanel().add(fvmButton);
			fvm.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(final ChangeEvent evt) {
					updateComponent();
				}
			});
		} else {
			fvmButton = null;
		}
		model.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateComponent();

			}
		});
	}

	private void updateModel(boolean noColouring)
			throws InvalidSettingsException {

		if (fvmButton != null && fvmButton.getFlowVariableModel()
				.isVariableReplacementEnabled()) {
			// Dont do anything is we are using a flow variable
			return;
		}

		final String str = textPanel.getText();
		if (disallowEmptyString && (str == null || !str.isEmpty())) {
			if (!noColouring) {
				showError();
			}
			throw new InvalidSettingsException("A string value is required");
		}

		((SettingsModelString) getModel()).setStringValue(str);

	}

	private void clearError() {
		textPanel.setBackground(DEFAULT_BG);
		textPanel.setForeground(DEFAULT_FG);
	}

	private void showError() {
		if (!getModel().isEnabled()) {
			// dont process disabled components
			return;
		}

		if (textPanel.getText().isEmpty()) {
			textPanel.setBackground(Color.RED);
		} else {
			textPanel.setForeground(Color.RED);
		}
		textPanel.requestFocusInWindow();

		textPanel.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				clearError();
				textPanel.getDocument().removeDocumentListener(this);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				clearError();
				textPanel.getDocument().removeDocumentListener(this);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				clearError();
				textPanel.getDocument().removeDocumentListener(this);
			}
		});

	}

	@Override
	protected void updateComponent() {
		clearError();

		// Only update if model is out of sync
		final String str = ((SettingsModelString) getModel()).getStringValue();
		if (!textPanel.getText().equals(str)) {
			textPanel.setText(str);
		}

		setEnabledComponents(getModel().isEnabled());
	}

	@Override
	protected void validateSettingsBeforeSave()
			throws InvalidSettingsException {
		updateModel(false);

	}

	@Override
	protected void checkConfigurabilityBeforeLoad(PortObjectSpec[] specs)
			throws NotConfigurableException {
		// Nothing to do here!

	}

	@Override
	protected void setEnabledComponents(boolean enabled) {
		textPanel.setEnabled(enabled && fvmButton == null || !fvmButton
				.getFlowVariableModel().isVariableReplacementEnabled());

	}

	@Override
	public void setToolTipText(String text) {
		textPanel.setToolTipText(text);

	}

}
