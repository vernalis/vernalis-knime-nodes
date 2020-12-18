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

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

import com.vernalis.pdbconnector2.dialogcomponents.suggester.Suggester;
import com.vernalis.pdbconnector2.dialogcomponents.swing.JSuggestingTextField;

/**
 * A {@link DialogComponent} which populates a dropdown with suggestions based
 * on entered text
 *
 * @author S.Roughley knime@vernalis.com
 * 
 * @since 1.28.0
 *
 */
public class DialogComponentStringSuggester extends DialogComponent {

	private static final int MAX_SIZE = 50;
	private static final int MIN_SIZE = 0;
	private final JLabel label;
	private final JSuggestingTextField textField;
	private final boolean allowEmpty;

	/**
	 * Constructor allowing optional field with automatic field width and
	 * default minimum suggesting size
	 *
	 * @param model
	 *            The settings model
	 * @param label
	 *            The label to display
	 * @param suggester
	 *            The {@link Suggester}
	 */
	public DialogComponentStringSuggester(SettingsModelString model,
			String label, Suggester suggester) {
		this(model, label, suggester,
				JSuggestingTextField.DEFAULT_MIN_SUGGEST_SIZE);
	}

	/**
	 * Constructor allowing optional field with automatic field width
	 *
	 * @param model
	 *            The settings model
	 * @param label
	 *            The label to display
	 * @param suggester
	 *            The {@link Suggester}
	 * @param minSuggestSize
	 *            The minimum number of characters required in the input field
	 *            to trigger a suggestion
	 */
	public DialogComponentStringSuggester(SettingsModelString model,
			String label, Suggester suggester, int minSuggestSize) {
		this(model, label, suggester, minSuggestSize,
				model.getStringValue() == null
						|| model.getStringValue().isEmpty()
								? JSuggestingTextField.DEFAULT_FIELD_WIDTH
								: Math.max(
										Math.min(MAX_SIZE, model
												.getStringValue().length()),
										MIN_SIZE));
	}

	/**
	 * Constructor allowing optional field
	 *
	 * @param model
	 *            The settings model
	 * @param label
	 *            The label to display
	 * @param suggester
	 *            The {@link Suggester}
	 * @param minSuggestSize
	 *            The minimum number of characters required in the input field
	 *            to trigger a suggestion
	 * @param componentSize
	 *            The width of the text field
	 */
	public DialogComponentStringSuggester(SettingsModelString model,
			String label, Suggester suggester, int minSuggestSize,
			int componentSize) {
		this(model, label, suggester, minSuggestSize, componentSize, true);
	}

	/**
	 * Full constructor
	 * 
	 * @param model
	 *            The settings model
	 * @param label
	 *            The label to display
	 * @param suggester
	 *            The {@link Suggester}
	 * @param minSuggestSize
	 *            The minimum number of characters required in the input field
	 *            to trigger a suggestion
	 * @param componentSize
	 *            The width of the text field
	 * @param allowEmpty
	 *            Should an empty string be allowed
	 */
	public DialogComponentStringSuggester(SettingsModelString model,
			String label, Suggester suggester, int minSuggestSize,
			int componentSize, boolean allowEmpty) {
		super(model);
		this.allowEmpty = allowEmpty;
		this.label = new JLabel(label);
		getComponentPanel().add(this.label);
		this.textField = new JSuggestingTextField(suggester, minSuggestSize,
				componentSize);
		textField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(final DocumentEvent e) {
				try {
					updateModel();
				} catch (final InvalidSettingsException ise) {
					// Ignore it here.
				}
			}

			@Override
			public void insertUpdate(final DocumentEvent e) {
				try {
					updateModel();
				} catch (final InvalidSettingsException ise) {
					// Ignore it here.
				}
			}

			@Override
			public void changedUpdate(final DocumentEvent e) {
				try {
					updateModel();
				} catch (final InvalidSettingsException ise) {
					// Ignore it here.
				}
			}
		});

		// update the text field, whenever the model changes
		getModel().addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(final ChangeEvent e) {
				updateComponent();
			}
		});

		getComponentPanel().add(textField);
		// Make sure the component is in sync with the model
		updateComponent();
	}

	private void updateModel() throws InvalidSettingsException {
		if (!allowEmpty && (textField.getText() == null
				|| textField.getText().isEmpty())) {
			showError(textField);
			throw new InvalidSettingsException("A value must be entered");
		}

		// Set the model with the field value
		((SettingsModelString) getModel()).setStringValue(textField.getText());
	}

	@Override
	protected void updateComponent() {
		clearError(textField);
		final String mdlVal =
				((SettingsModelString) getModel()).getStringValue();
		if (!textField.getText().equals(mdlVal)) {
			// Different - update the component without firing the popup!
			textField.silentlySetText(mdlVal);
		}
		setEnabledComponents(getModel().isEnabled());
	}

	@Override
	protected void validateSettingsBeforeSave()
			throws InvalidSettingsException {
		updateModel();

	}

	@Override
	protected void checkConfigurabilityBeforeLoad(PortObjectSpec[] specs)
			throws NotConfigurableException {
		// Nothing to do

	}

	@Override
	protected void setEnabledComponents(boolean enabled) {
		textField.setEnabled(enabled);

	}

	@Override
	public void setToolTipText(String text) {
		label.setToolTipText(text);
		textField.setToolTipText(text);

	}

	/**
	 * Method to set the size of the text input field
	 * 
	 * @param w
	 *            The new width
	 * @param h
	 *            The new height
	 */
	public void setTextFieldSize(int w, int h) {
		setTextFieldSize(new Dimension(w, h));
	}

	/**
	 * Method to set the size of the text input field
	 * 
	 * @param dim
	 *            the new size
	 */
	public void setTextFieldSize(Dimension dim) {
		textField.setPreferredSize(dim);
	}
}
