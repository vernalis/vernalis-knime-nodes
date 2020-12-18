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
package com.vernalis.pdbconnector2.dialogcomponents.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A {@link JTextField} with a hierarchical popup menu (2 levels) of categories
 * and possible values (which can be in a category sub-menu or the top level
 * menu). An allowed option can have a difference 'action command' to the
 * displayed text (see {@link MenuAction}. Various static factory methods are
 * available for constructing from a pre-existing List or Map of values.
 * Alternatively, the menu can be build using the {@link Builder} class
 *
 *
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class NestedDropdownTextField extends JTextField
		implements ActionListener, Changeable {

	private static final long serialVersionUID = 1L;

	/**
	 * A convenience class to encapsulate a menu item representing an allowed
	 * value with a different action command to displayed text (which for
	 * example allows two categories to have an option with the same display
	 * name but different actions)
	 *
	 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
	 *
	 */
	public static class MenuAction {

		private final String displayName, actionCommand;

		/**
		 * Overloaded constructor with same action command as display name
		 * 
		 * @param displayName
		 *            The text to display in the menu
		 */
		public MenuAction(String displayName) {
			this(displayName, null);
		}

		/**
		 * Constructor
		 * 
		 * @param displayName
		 *            The text to display in the menu
		 * @param actionCommand
		 *            The action command on selecting - {@code null} or empty
		 *            string will result in the action command being the same as
		 *            the displayed text
		 */
		public MenuAction(String displayName, String actionCommand) {
			super();
			this.displayName = displayName;
			this.actionCommand =
					actionCommand == null || actionCommand.isEmpty()
							? displayName
							: actionCommand;
		}

		/**
		 * @return the text to display in the menu
		 */
		public final String getDisplayName() {
			return displayName;
		}

		/**
		 * @return The action command on selecting the item
		 */
		public final String getActionCommand() {
			return actionCommand;
		}

	}

	/**
	 * A Builder class to create a {@link NestedDropdownTextField}. The Builder
	 * can only be used once. Once used, by a call to {@link Builder#build()},
	 * subsequent calls of {@code build()} will throw
	 * {@link UnsupportedOperationException}, as will attempts to add additional
	 * menu items
	 * 
	 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
	 *
	 */
	public static class Builder {

		private final Map<String, JMenuItem> actionLookup = new HashMap<>();// K
																			// =
																			// action
																			// Command,
																			// V
																			// =
																			// menu
																			// item
		private final Map<String, JMenu> subMenuLookup = new HashMap<>();
		private final JPopupMenu popup = new JPopupMenu();
		private String defaultAction = null;
		private int prefWidth = 0;
		private boolean isLocked = false;

		/**
		 * Constructor
		 */
		public Builder() {
		}

		/**
		 * Method to add a menu item to the root level
		 *
		 * @param action
		 *            The {@link MenuAction} to add to the root level
		 * @return this object for method call daisy-chaining
		 * @throws UnsupportedOperationException
		 *             if the builder has been consumed by a call to
		 *             {@link #build()}
		 */
		public Builder addMenuItem(MenuAction action)
				throws UnsupportedOperationException {
			return addMenuItem("", action);
		}

		/**
		 * Method to add a menu item
		 *
		 * @param subMenu
		 *            The category name ({@code null} or empty string for the
		 *            root level)
		 * @param action
		 *            The {@link MenuAction} to add to the menu
		 * @return this object for method call daisy-chaining
		 * @throws UnsupportedOperationException
		 *             if the builder has been consumed by a call to
		 *             {@link #build()}
		 */
		public Builder addMenuItem(String subMenu, MenuAction action)
				throws UnsupportedOperationException {
			return addMenuItem(subMenu, action.getDisplayName(),
					action.getActionCommand());
		}

		/**
		 * Method to add a menu item
		 *
		 * @param subMenu
		 *            The category name ({@code null} or empty string for the
		 *            root level)
		 * @param menuText
		 *            The name of the option to display
		 * @param actionCommand
		 *            The action command to store with it
		 * @return this object for method call daisy-chaining
		 * @throws UnsupportedOperationException
		 *             if the builder has been consumed by a call to
		 *             {@link #build()}
		 */
		public Builder addMenuItem(String subMenu, String menuText,
				String actionCommand) throws UnsupportedOperationException {
			checkLocked();
			if (defaultAction == null) {
				defaultAction = actionCommand;
			}
			final JMenuItem menuItem = createMenuItem(menuText, actionCommand);
			prefWidth = Math.max(prefWidth, menuItem.getPreferredSize().width);

			if (subMenu == null || subMenu.isEmpty()) {
				popup.add(menuItem);
			} else {
				addSubMenu(subMenu).add(menuItem);
			}
			if (defaultAction == null) {
				defaultAction = actionCommand;
			}
			return this;
		}

		/**
		 * Method to set the default action. This must be non-null, and already
		 * registered as an action via one of the addMenuItem methods
		 *
		 * @param defaultAction
		 *            The new default action
		 * @return this object for method call daisy-chaining
		 * @throws UnsupportedOperationException
		 *             If the builder has already been built or an invalid
		 *             action is supplied
		 */
		public Builder setDefaultAction(String defaultAction)
				throws UnsupportedOperationException {
			checkLocked();
			if (defaultAction == null
					|| !actionLookup.containsKey(defaultAction)) {
				throw new UnsupportedOperationException(
						"A valid default action must be supplied!");
			}
			this.defaultAction = defaultAction;
			return this;
		}

		/**
		 * Method to build the {@link NestedDropdownTextField}. Once called
		 * successfully, the object cannot be reused
		 * 
		 * @return The dialog component
		 * @throws UnsupportedOperationException
		 *             if the builder has been consumed by a previous call to
		 *             {@link #build()}
		 * @throws IllegalStateException
		 *             If there are no menu items
		 */
		public NestedDropdownTextField build()
				throws UnsupportedOperationException, IllegalStateException {
			checkLocked();
			if (actionLookup.isEmpty()) {
				throw new IllegalStateException(
						"At least one menu item must be present!");
			}
			isLocked = true;
			return new NestedDropdownTextField(this);
		}

		private void checkLocked() throws UnsupportedOperationException {
			if (isLocked) {
				throw new UnsupportedOperationException(
						"The builder has been built and is now locked");
			}
		}

		private JMenuItem createMenuItem(String menuText,
				String actionCommand) {
			if (actionLookup.containsKey(actionCommand)) {
				throw new IllegalArgumentException(
						"Action command '" + actionCommand + "' already used");
			}
			final JMenuItem retVal = new JMenuItem(menuText);
			retVal.setActionCommand(actionCommand);
			actionLookup.put(actionCommand, retVal);
			return retVal;
		}

		private JMenu addSubMenu(String menuText) {
			JMenu retVal = subMenuLookup.get(menuText);
			if (retVal == null) {
				retVal = new JMenu(menuText);
				subMenuLookup.put(menuText, retVal);
				popup.add(retVal);
				prefWidth =
						Math.max(prefWidth, retVal.getPreferredSize().width);
			}
			return retVal;
		}

		/**
		 * Method to register an action listener to all menu items
		 * 
		 * @param l
		 *            The listener to register
		 */
		void registerActionListenerToMenuItems(ActionListener l) {
			actionLookup.values()
					.forEach(menuItem -> menuItem.addActionListener(l));
		}

	}

	private String currentAction = null;
	private final Builder builder;

	/**
	 * Constructor
	 */
	private NestedDropdownTextField(Builder builder) {
		super();
		this.builder = builder;
		this.builder.registerActionListenerToMenuItems(this);

		setEditable(false);

		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				builder.popup.show(NestedDropdownTextField.this, 0,
						NestedDropdownTextField.this.getHeight());
			}
		});

		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		setBackground(Color.white);
		setPreferredSize(
				new Dimension(builder.prefWidth, getPreferredSize().height));
		setMaximumSize(getPreferredSize());

		builder.popup.setSize(getWidth(), builder.popup.getHeight());
		setCurrentAction(builder.defaultAction);
	}

	/**
	 * Convenience factory method for a set of options, all in the root level,
	 * with the same action command and display name
	 *
	 * @param options
	 *            The list of displayed options
	 * @return The dialog component
	 */
	public static NestedDropdownTextField
			fromStringOptions(Collection<String> options) {
		return fromMenuActions(options.stream().map(x -> new MenuAction(x))
				.collect(Collectors.toList()));
	}

	/**
	 * Convenience factory method for a set of options in the root level
	 *
	 * @param options
	 *            The list of displayed options
	 * @return The dialog component
	 */
	public static NestedDropdownTextField
			fromMenuActions(Collection<MenuAction> options) {
		return fromMenuActions(Collections.singletonMap("", options));
	}

	/**
	 * Convenience factory method for a map of categories and options
	 *
	 * @param optionMap
	 *            The map of category names list of displayed options. Map key
	 *            {@code null} or empty string corresponds to the root level
	 * @return The dialog component
	 */
	public static NestedDropdownTextField
			fromMenuActions(Map<String, Collection<MenuAction>> optionMap) {
		final Builder retVal = new Builder();
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
	 * Convenience factory method for a map of categories and options with the
	 * same display name and action commands
	 *
	 * @param optionMap
	 *            The map of category names list of displayed options. Map key
	 *            {@code null} or empty string corresponds to the root level
	 * @return The dialog component
	 */
	public static NestedDropdownTextField
			fromStringOptions(Map<String, Collection<String>> optionMap) {
		final Builder retVal = new Builder();
		for (final Entry<String, Collection<String>> ent : optionMap
				.entrySet()) {
			final String k = ent.getKey();
			for (final String menuItem : ent.getValue()) {
				retVal.addMenuItem(k, menuItem, menuItem);
			}
		}
		return retVal.build();
	}

	/**
	 * @return The default action, which will be the first added, or
	 *         {@code null} if none have been added
	 */
	public String getDefaultAction() {
		return builder.defaultAction;
	}

	/**
	 * @return The currently selected option as it's action command
	 */
	public String getCurrentAction() {
		return currentAction;
	}

	/**
	 * Change the selection
	 * 
	 * @param newAction
	 *            The new action to select
	 * @return {@code true} if the action was successfully selected
	 */
	public boolean setCurrentAction(String newAction) {
		if (!builder.actionLookup.containsKey(newAction)) {
			return false;
		}
		setText(builder.actionLookup.get(newAction).getText());
		currentAction = newAction;
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final JMenuItem source = (JMenuItem) e.getSource();
		setText(source.getText());
		currentAction = source.getActionCommand();
		fireStateChanged();
	}

	/**
	 * Method to notify change listeners that there has been a change of state
	 */
	protected void fireStateChanged() {
		final Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChangeListener.class) {
				((ChangeListener) listeners[i + 1])
						.stateChanged(new ChangeEvent(currentAction));
			}
		}
	}

	@Override
	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);

	}

	@Override
	public void removeChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);

	}

	@Override
	public ChangeListener[] getChangeListeners() {
		return listenerList.getListeners(ChangeListener.class);
	}

}
