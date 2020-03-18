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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.FlowVariableModelButton;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentMultiLineString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

/**
 * A {@link DialogComponentMultiLineString} wrapper which puts the title and
 * multiline string within an etched border, and adds a Flow Vairable button
 * immediately to the right of the title text
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class DialogComponentMultilineStringFlowvar extends DialogComponent {

	private final JTextArea textArea;
	private final boolean disallowEmptyString;
	private final FlowVariableModelButton fvmButton;

	/**
	 * Constructor. Empty strings are accepted in the input, and the editable
	 * text panel is of default size
	 * 
	 * @param stringModel
	 *            The Settings Model for the editable content
	 * @param label
	 *            The text label
	 * @param fvm
	 *            The flow variable model
	 */
	public DialogComponentMultilineStringFlowvar(
			SettingsModelMultilineString stringModel, String label,
			FlowVariableModel fvm) {
		this(stringModel, label, false, 50, 3, fvm);
	}

	/**
	 * Constructor.
	 * 
	 * @param stringModel
	 *            The Settings Model for the editable content
	 * @param label
	 *            The text label
	 * @param disallowEmptyString
	 *            if set true, the component request a non-empty string from the
	 *            user.
	 * @param cols
	 *            the number of columns.
	 * @param rows
	 *            the number of rows.
	 * @param fvm
	 *            The flow variable model
	 */
	public DialogComponentMultilineStringFlowvar(
			SettingsModelString stringModel, String label,
			boolean disallowEmptyString, int cols, int rows,
			FlowVariableModel fvm) {
		super(stringModel);
		this.disallowEmptyString = disallowEmptyString;
		getComponentPanel().setLayout(new BorderLayout());
		getComponentPanel().setBorder(new EtchedBorder());
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		titlePanel.add(new JLabel(label), BorderLayout.WEST);
		fvmButton = new FlowVariableModelButton(fvm);
		titlePanel.add(fvmButton, BorderLayout.EAST);
		getComponentPanel().add(titlePanel, BorderLayout.NORTH);

		textArea = new JTextArea();
		textArea.setColumns(cols);
		textArea.setRows(rows);
		JScrollPane jsp = new JScrollPane(textArea);
		getComponentPanel().add(jsp, BorderLayout.CENTER);

		textArea.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				try {
					updateModel(true);
				} catch (InvalidSettingsException ise) {
					// Ignore!
				}
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				try {
					updateModel(true);
				} catch (InvalidSettingsException ise) {
					// Ignore!
				}
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				try {
					updateModel(true);
				} catch (InvalidSettingsException ise) {
					// Ignore!
				}
			}
		});

		fvm.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(final ChangeEvent evt) {
				updateComponent();
			}
		});

		((SettingsModelMultilineString) getModel())
				.prependChangeListener(new ChangeListener() {

					@Override
					public void stateChanged(ChangeEvent e) {
						updateComponent();

					}
				});

		updateComponent();
	}

	private void updateModel(boolean noColouring)
			throws InvalidSettingsException {

		if (fvmButton.getFlowVariableModel().isVariableReplacementEnabled()) {
			// Dont do anything is we are using a flow variable
			return;
		}

		String str = textArea.getText();
		if (disallowEmptyString && (str == null || !str.isEmpty())) {
			if (!noColouring) {
				showError();
			}
			throw new InvalidSettingsException("A string value is required");
		}

		((SettingsModelString) getModel()).setStringValue(str);

	}

	private void clearError() {
		textArea.setBackground(DEFAULT_BG);
		textArea.setForeground(DEFAULT_FG);
	}

	private void showError() {
		if (!getModel().isEnabled()) {
			// dont process disabled components
			return;
		}

		if (textArea.getText().isEmpty()) {
			textArea.setBackground(Color.RED);
		} else {
			textArea.setForeground(Color.RED);
		}
		textArea.requestFocusInWindow();

		textArea.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				clearError();
				textArea.getDocument().removeDocumentListener(this);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				clearError();
				textArea.getDocument().removeDocumentListener(this);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				clearError();
				textArea.getDocument().removeDocumentListener(this);
			}
		});

	}

	@Override
	protected void updateComponent() {
		clearError();

		// Only update if model is out of sync
		final String str = ((SettingsModelString) getModel()).getStringValue();
		if (!textArea.getText().equals(str)) {
			textArea.setText(str);
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
		// TODO Auto-generated method stub

	}

	@Override
	protected void setEnabledComponents(boolean enabled) {
		textArea.setEnabled(enabled && !fvmButton.getFlowVariableModel()
				.isVariableReplacementEnabled());

	}

	@Override
	public void setToolTipText(String text) {
		textArea.setToolTipText(text);

	}

}
