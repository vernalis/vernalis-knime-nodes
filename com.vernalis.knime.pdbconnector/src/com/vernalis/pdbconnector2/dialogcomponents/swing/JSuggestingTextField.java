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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.vernalis.pdbconnector2.dialogcomponents.suggester.Suggester;

/**
 * A text field with a {@link Suggester} which shows a dropdown once typing has
 * started if there are any matching suggestions. Selecting a value from the
 * dropdown enters it into the text field, or continuing typing updates the
 * suggestion list. Double-clicking or hitting enter in the text input field
 * also triggers the suggestion dropdown to be shown if there are any
 * suggestions for the current input
 *
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class JSuggestingTextField extends JTextField implements ActionListener {

	/**
	 * The default minimum suggest size - the number of characters in the input
	 * before suggesting is triggered
	 */
	public static final int DEFAULT_MIN_SUGGEST_SIZE = 1;
	private static final long serialVersionUID = 1L;
	/** The default input text field width */
	public static final int DEFAULT_FIELD_WIDTH = 15;
	private JScrollPopupMenu popup;
	private final com.vernalis.pdbconnector2.dialogcomponents.suggester.Suggester suggester;
	private boolean wasPopupClicked = false;
	private final int minSuggestSize;
	private boolean notifyListeners = true;

	/**
	 * Simple constructor with {@value #DEFAULT_FIELD_WIDTH} as the component
	 * width and suggestions for 1 or more characters
	 *
	 * @param suggester
	 *            the {@link Suggester}
	 */
	public JSuggestingTextField(Suggester suggester) {
		this(suggester, DEFAULT_MIN_SUGGEST_SIZE);
	}

	/**
	 * Constructor with {@value #DEFAULT_FIELD_WIDTH} as the component width
	 *
	 * @param suggester
	 *            the {@link Suggester}
	 * @param minSuggestSize
	 *            The minimum number of characters required in the input field
	 *            to trigger suggestion
	 */
	public JSuggestingTextField(Suggester suggester, int minSuggestSize) {
		this(suggester, minSuggestSize, DEFAULT_FIELD_WIDTH);
	}

	/**
	 * Full constructor
	 *
	 * @param suggester
	 *            The {@link Suggester}
	 * @param minSuggestSize
	 *            The minimum number of characters required in the input field
	 *            to trigger a suggestion
	 * @param componentSize
	 *            The width of the text field
	 */
	public JSuggestingTextField(Suggester suggester, int minSuggestSize,
			int componentSize) {
		super();
		if (suggester == null) {
			throw new NullPointerException("A suggester must be supplied");
		}
		this.suggester = suggester;
		this.minSuggestSize = minSuggestSize;
		setColumns(componentSize);

		setEditable(true);

		// Double clicking in the text field triggers the suggester
		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && !e.isConsumed()) {
					updateSuggestions(getText());
				}
			}
		});

		// Update the suggester when the contents is changed
		getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateSuggestions(getText());

			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateSuggestions(getText());

			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateSuggestions(getText());

			}
		});

		// Allow 'Enter' to select the menu item or trigger a suggestion menu
		addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
				// Down arrow set's focus to popup if it is displayed
				if (e.isConsumed()) {
					return;
				}
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (popup != null) {
						// Stop the component instantly re-running the suggester
						// and showing pop-up
						// again
						wasPopupClicked = true;
						final MenuElement[] selectedPath = MenuSelectionManager
								.defaultManager().getSelectedPath();
						if (selectedPath.length > 1) {
							// User had selected in the menu - transfer it to
							// the dialog
							setText(((JMenuItem) selectedPath[selectedPath.length
									- 1]).getText());
						}
					} else {
						// Enter in component without popup triggers suggestor
						updateSuggestions(getText());
					}
				} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					// User used escape key to close menu, so enter should now
					// show it again...
					if (popup != null) {
						popup.removeAll();
						popup.setVisible(false);
						popup = null;
					}
					wasPopupClicked = false;
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {

			}
		});
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		setBackground(Color.white);
		setMaximumSize(getPreferredSize());

		popup = null;

	}

	/**
	 * This method generates a new popup menu of suggestions
	 *
	 * @param text
	 *            The current value in the dialog box
	 */
	protected void updateSuggestions(String text) {
		if (!notifyListeners) {
			return;
		}
		// Start by clearing an existing menu
		if (popup != null) {
			popup.removeAll();
			popup.setVisible(false);
			popup = null;
		}

		// No text means we dont try to suggest
		if (text == null || text.isEmpty()) {
			return;
		}

		// We keep this separate as selecting a menu item fires two document
		// updates - a
		// 'removeUpdate' which calls this method with an empty string (trapped
		// above),
		// followed by an
		// insertUpdate with the new String
		if (wasPopupClicked) {
			wasPopupClicked = false;
			return;
		}

		// Text is still too short to suggest
		if (text.length() < minSuggestSize) {
			return;
		}

		popup = new JScrollPopupMenu();

		final List<String> suggestions = suggester.suggest(text);
		for (final String vals : suggestions) {
			final JMenuItem menuItem = new JMenuItem(vals);
			menuItem.addActionListener(this);
			popup.add(menuItem);
		}
		if (!suggestions.isEmpty()) {
			popup.show(this, 0, getHeight());
			popup.setFocusable(true);
			requestFocusInWindow();
		}
		requestFocusInWindow();

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final JMenuItem source = (JMenuItem) e.getSource();
		wasPopupClicked = true;
		setText(source.getText());
	}

	/**
	 * Set the displayed text to an empty string, notifying listeners if
	 * required
	 */
	public void clearText() {
		setText("");
	}

	/**
	 * Set a new text value without notifying the listeners, so no popup
	 * suggestions are triggered
	 * 
	 * @param newText
	 *            The new value to display
	 */
	public void silentlySetText(String newText) {
		notifyListeners = false;
		setText(newText);
		notifyListeners = true;
	}

	/**
	 * Set the displayed text to an empty string without notifying listeners
	 */
	public void silentlyClearText() {
		silentlySetText("");
	}
}
