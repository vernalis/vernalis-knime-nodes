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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

import com.vernalis.pdbconnector2.dialogcomponents.swing.NestedDropdownTextField;
import com.vernalis.pdbconnector2.dialogcomponents.swing.NestedDropdownTextField.MenuAction;

/**
 * A dialog component to allow the user to select a value from a nested
 * dropdown. Selectable menu items can either be in the top level or in
 * categories.
 *
 * This component should be created from one of the four factory methods:
 *
 * @see #fromStringOptions(SettingsModelString, String, Collection)
 * @see #fromStringOptions(SettingsModelString, String, Map)
 * @see #fromMenuActions(SettingsModelString, String, Collection)
 * @see #fromMenuActions(SettingsModelString, String, Map)
 *
 * @author S.Roughley knime@vernalis.com
 * 
 * @since 1.28.0
 *
 */
public class DialogComponentNestedDropdown extends DialogComponent {

	/**
	 * Builder class for the {@link DialogComponentNestedDropdown}
	 * 
	 * @author S.Roughley knime@vernalis.com
	 * @since 1.28.0
	 *
	 */
	public static class Builder {

		private final NestedDropdownTextField.Builder innerBuilder =
				new NestedDropdownTextField.Builder();
		private final SettingsModelString model;
		private String label;

		/**
		 * Constructor specifying a non-null Settings Model
		 * 
		 * @param model
		 *            The model
		 */
		public Builder(SettingsModelString model) {
			Objects.requireNonNull(model, "A model must be supplied");
			this.model = model;
		}

		/**
		 * Constructor providing model and component label
		 * 
		 * @param model
		 *            The model
		 * @param label
		 *            The label to display
		 */
		public Builder(SettingsModelString model, String label) {
			this(model);
			setLabel(label);
		}

		/**
		 * Set the label value
		 * 
		 * @param label
		 *            The label to set
		 * @return This object for method daisy-chaining
		 */
		public Builder setLabel(String label) {
			this.label = label;
			return this;
		}

		/**
		 * Method to add a {@link MenuAction} to the dropdown
		 * 
		 * @param action
		 *            The action to add
		 * @return This object for method daisy-chaining
		 * @throws UnsupportedOperationException
		 *             if the component has already been built once
		 */
		public Builder addMenuItem(MenuAction action)
				throws UnsupportedOperationException {
			innerBuilder.addMenuItem(action);
			return this;
		}

		/**
		 * Method to add a {@link MenuAction} to a sub-menu
		 * 
		 * @param subMenu
		 *            The name of the submenu, which will be created if not
		 *            already present
		 * @param action
		 *            The action to add
		 * @return This object for method daisy-chaining
		 * @throws UnsupportedOperationException
		 *             if the component has already been built once
		 */
		public Builder addMenuItem(String subMenu, MenuAction action)
				throws UnsupportedOperationException {
			innerBuilder.addMenuItem(subMenu, action);
			return this;
		}

		/**
		 * Method to add an item to a sub-menu
		 * 
		 * @param subMenu
		 *            The name of the submenu, which will be created if not
		 *            already present
		 * @param menuText
		 *            The text to display for the menu item
		 * @param actionCommand
		 *            The action command for the menu item
		 * @return This object for method daisy-chaining
		 * @throws UnsupportedOperationException
		 *             if the component has already been built once
		 */
		public Builder addMenuItem(String subMenu, String menuText,
				String actionCommand) throws UnsupportedOperationException {
			innerBuilder.addMenuItem(subMenu, menuText, actionCommand);
			return this;
		}

		/**
		 * Method to set the default action for the component
		 * 
		 * @param defaultAction
		 *            The defaultAction
		 * @return This object for method daisy-chaining
		 * @throws UnsupportedOperationException
		 *             if the component has already been built once
		 */
		public Builder setDefaultAction(String defaultAction)
				throws UnsupportedOperationException {
			innerBuilder.setDefaultAction(defaultAction);
			return this;
		}

		/**
		 * Method to build the component
		 * 
		 * @return The built component
		 * @throws UnsupportedOperationException
		 *             if the component has previously been built
		 */
		public DialogComponentNestedDropdown build()
				throws UnsupportedOperationException {
			return new DialogComponentNestedDropdown(model, label,
					innerBuilder);
		}

	}

	private final JLabel label;
	private final NestedDropdownTextField textField;

	/**
	 * Private constructor - user the {@link Builder} class of one of the public
	 * static factory methods.
	 * 
	 * @param model
	 *            The settings model
	 * @param label
	 *            The component label
	 * @param nestedDropdownTextFieldBuilder
	 *            The Builder for the actual menu object
	 * @throws UnsupportedOperationException
	 *             If the menu object builder has previously been built
	 */
	private DialogComponentNestedDropdown(SettingsModelString model,
			String label,
			NestedDropdownTextField.Builder nestedDropdownTextFieldBuilder)
			throws UnsupportedOperationException {
		super(model);
		if (label != null) {
			this.label = new JLabel(label);
			getComponentPanel().add(this.label);
		} else {
			this.label = null;
		}
		this.textField = nestedDropdownTextFieldBuilder.build();
		textField.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {

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

	/**
	 * Factory method for a set of options in the root level with the same
	 * action command and display name
	 *
	 * @param model
	 *            The settings model
	 * @param label
	 *            The label for the component (maybe {@code null} for no label)
	 * @param options
	 *            The list of displayed options
	 * @return The dialog component
	 */
	public static DialogComponentNestedDropdown fromStringOptions(
			SettingsModelString model, String label,
			Collection<String> options) {
		return fromMenuActions(model, label, options.stream()
				.map(x -> new MenuAction(x)).collect(Collectors.toList()));
	}

	/**
	 * Factory method for a set of options in the root level
	 *
	 * @param model
	 *            The settings model
	 * @param label
	 *            The label for the component (maybe {@code null} for no label)
	 * @param options
	 *            The list of displayed options
	 * @return The dialog component
	 */
	public static DialogComponentNestedDropdown fromMenuActions(
			SettingsModelString model, String label,
			Collection<MenuAction> options) {
		return fromMenuActions(model, label,
				Collections.singletonMap("", options));
	}

	/**
	 * Factory method for a map of categories and options
	 * 
	 * @param model
	 *            The settings model
	 * @param label
	 *            The label for the component (maybe {@code null} for no label)
	 * @param optionMap
	 *            The map of category names list of displayed options. Map key
	 *            {@code null} or empty string corresponds to the root level
	 * @return The dialog component
	 */
	public static DialogComponentNestedDropdown fromMenuActions(
			SettingsModelString model, String label,
			Map<String, Collection<MenuAction>> optionMap) {
		final Builder retVal = new Builder(model, label);
		for (final Entry<String, Collection<MenuAction>> ent : optionMap
				.entrySet()) {
			final String k = ent.getKey();
			for (final MenuAction menuItem : ent.getValue()) {
				retVal.addMenuItem(k, menuItem);
			}
		}
		return retVal.build();
	}

	/**
	 * Factory method for a map of categories and options with the same display
	 * name and action commands
	 *
	 * @param model
	 *            The settings model
	 * @param label
	 *            The label for the component (maybe {@code null} for no label)
	 * @param optionMap
	 *            The map of category names list of displayed options. Map key
	 *            {@code null} or empty string corresponds to the root level
	 * @return The dialog component
	 */
	public static DialogComponentNestedDropdown fromStringOptions(
			SettingsModelString model, String label,
			Map<String, Collection<String>> optionMap) {
		final Builder retVal = new Builder(model, label);
		for (final Entry<String, Collection<String>> ent : optionMap
				.entrySet()) {
			final String k = ent.getKey();
			for (final String menuItem : ent.getValue()) {
				retVal.addMenuItem(k, menuItem, menuItem);
			}
		}
		return retVal.build();
	}

	private void updateModel() throws InvalidSettingsException {
		if (textField.getText() == null || textField.getText().isEmpty()) {
			showError(textField);
			throw new InvalidSettingsException("A value must be entered");
		}

		// Set the model with the field value
		((SettingsModelString) getModel())
				.setStringValue(textField.getCurrentAction());
	}

	@Override
	protected void updateComponent() {
		clearError(textField);
		final String mdlVal =
				((SettingsModelString) getModel()).getStringValue();
		final String currentAction = textField.getCurrentAction();
		boolean changedVal = currentAction == null && mdlVal != null;
		if (!changedVal && currentAction != null) {
			changedVal = !currentAction.equals(mdlVal);
		}
		if (changedVal) {
			// Different - update the component
			textField.setCurrentAction(mdlVal);
		}
		setEnabledComponents(getModel().isEnabled());

		// Now make sure the model is in sync - in case it contained a value we
		// couldnt
		// select
		final String actCmd = textField.getCurrentAction();
		try {
			if (actCmd == null && mdlVal != null
					|| actCmd != null && !actCmd.equals(mdlVal)) {
				updateModel();
			}
		} catch (final InvalidSettingsException e) {
			// Ignore exception
		}
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
		if (label != null) {
			label.setToolTipText(text);
		}
		textField.setToolTipText(text);

	}

	/**
	 * Method to set the dimensions of the dropdown field
	 * 
	 * @param w
	 *            the width
	 * @param h
	 *            the height
	 */
	public void setTextFieldSize(int w, int h) {
		setTextFieldSize(new Dimension(w, h));
	}

	/**
	 * Method to set the dimensions of the dropdown field
	 * 
	 * @param dim
	 *            The new dimensions
	 */
	public void setTextFieldSize(Dimension dim) {
		textField.setPreferredSize(dim);
	}

}
