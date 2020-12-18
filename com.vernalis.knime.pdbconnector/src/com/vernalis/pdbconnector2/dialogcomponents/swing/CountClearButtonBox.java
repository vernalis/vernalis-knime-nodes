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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.CanceledExecutionException;

import com.vernalis.pdbconnector2.query.QueryModel;
import com.vernalis.pdbconnector2.query.QueryPanel;
import com.vernalis.pdbconnector2.query.RCSBQueryRunner;
import com.vernalis.pdbconnector2.query.RCSBQueryRunner.QueryException;

/**
 * A box to add 'Count' and 'Clear' buttons to a {@link QueryPanel} The buttons
 * are laid out horizontally (the default) or vertically, with a 5 pixel spacer
 * around and between them.
 * 
 * <p>
 * The 'Clear' button should clear the query in the container panel
 * </p>
 * <p>
 * The 'Count' button should only be enabled when there is a query. Clicking the
 * button should count the hits for the query panel, and disable the button
 * until the query changes. If there is an error in the query, then the text
 * 'Error!' will be displayed and the button again disabled until the query is
 * changed
 * </p>
 *
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class CountClearButtonBox extends Box implements ChangeListener {

	private static final String ERROR_TEXT = "Error!";
	private static final String COUNT_TEXT = "Count";
	private static final long serialVersionUID = 1L;
	private final QueryPanel<?> panel;
	private final JButton countButton, clearButton;
	private final Color foregroundColor;
	private final Dimension buttonSize;

	/**
	 * Overloaded constructor with the two buttons side-by-side
	 * 
	 * @param panel
	 *            The containing panel
	 */
	public CountClearButtonBox(QueryPanel<? extends QueryModel> panel) {
		this(panel, false);
	}

	/**
	 * Constructor
	 * 
	 * @param panel
	 *            The containing panel
	 * @param vertical
	 *            Should the buttons be side-by-side ({@code false}) or one
	 *            above the other ({@code true})?
	 */
	public CountClearButtonBox(QueryPanel<?> panel, boolean vertical) {
		super(vertical ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS);
		this.panel = panel;
		this.panel.getQueryModel().addChangeListener(this);

		// A small amount of space around
		setBorder(new EmptyBorder(5, 5, 5, 5));
		countButton = new JButton(COUNT_TEXT);
		showHitCount(1000000);
		buttonSize = countButton.getPreferredSize();

		if (vertical) {
			countButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		}
		add(countButton);
		foregroundColor = countButton.getForeground();
		countButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				countButton.setEnabled(false);
				final int hitCount = countHits();
				if (hitCount < 0) {
					// Error with the query
					showError();
				} else {
					showHitCount(hitCount);
				}

			}
		});
		resetCountButton();

		// A bit of space between the buttons
		add(vertical ? createVerticalStrut(5) : createHorizontalStrut(5));

		clearButton = new JButton("Clear");
		if (vertical) {
			clearButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		}
		clearButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				CountClearButtonBox.this.panel.clearQuery();
				clearButton
						.setEnabled(CountClearButtonBox.this.panel.hasQuery());

			}
		});
		resetClearButton();
		add(clearButton);
		SwingUtils.forceToPreferredSize(this);
	}

	/**
	 * Reset the 'Count' button to it's default colour, text and enabled status
	 */
	public void resetCountButton() {

		countButton.setForeground(foregroundColor);
		countButton.setText(COUNT_TEXT);
		countButton.setEnabled(panel.hasQuery());
		SwingUtils.forceToSize(countButton, buttonSize);

	}

	/**
	 * Reset the 'Clear' button to it's correct enabled status
	 */
	public void resetClearButton() {
		clearButton.setEnabled(panel.hasQuery());
		SwingUtils.forceToSize(clearButton, buttonSize);

	}

	private int countHits() {
		if (!panel.hasQuery()) {
			assert false : "This should only be callable when the panel has a query!";
			return -1;
		}
		try {
			return new RCSBQueryRunner(panel.getQueryModel()).getHitCount();
		} catch (QueryException | CanceledExecutionException e) {
			return -1;
		}

	}

	/**
	 * Display a hit count on the 'Count' button and disable it
	 * 
	 * @param hitCount
	 *            The value to show on the count button
	 */
	private void showHitCount(final int hitCount) {
		countButton.setText(String.format("%d hits", hitCount));
		countButton.setForeground(foregroundColor);

		countButton.setMinimumSize(buttonSize);
		countButton.setPreferredSize(buttonSize);
		countButton.setMaximumSize(buttonSize);
	}

	/**
	 * Display an 'Error!' label in red text on the 'Count' button
	 */
	private void showError() {
		countButton.setText(ERROR_TEXT);
		countButton.setForeground(Color.red);

		countButton.setMinimumSize(buttonSize);
		countButton.setPreferredSize(buttonSize);
		countButton.setMaximumSize(buttonSize);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		panel.resetCountClearButtons();

	}

	/**
	 * Add an {@link ActionListener} to the Count button
	 * 
	 * @param l
	 *            the listener
	 */
	public void addCountButtonActionListener(ActionListener l) {
		countButton.addActionListener(l);
	}

	/**
	 * Remove an {@link ActionListener} from the Count button
	 * 
	 * @param l
	 *            the listener
	 */
	public void removeCountButtonActionListener(ActionListener l) {
		countButton.removeActionListener(l);
	}

	/**
	 * Add a {@link MouseListener} to the Count button
	 * 
	 * @param l
	 *            the listener
	 */
	public void addCountButtonMouseListener(MouseListener l) {
		countButton.addMouseListener(l);
	}

	/**
	 * Remove a {@link MouseListener} from the Count button
	 * 
	 * @param l
	 *            the listener
	 */
	public void removeCountButtonMouseListener(MouseListener l) {
		countButton.removeMouseListener(l);
	}

	/**
	 * Add an {@link ActionListener} to the Clear button
	 * 
	 * @param l
	 *            the listener
	 */
	public void addClearButtonActionListener(ActionListener l) {
		clearButton.addActionListener(l);
	}

	/**
	 * Remove an {@link ActionListener} from the Clear button
	 * 
	 * @param l
	 *            the listener
	 */
	public void removeClearButtonActionListener(ActionListener l) {
		clearButton.removeActionListener(l);
	}

	/**
	 * Add a {@link MouseListener} to the Clear button
	 * 
	 * @param l
	 *            the listener
	 */
	public void addClearButtonMouseListener(MouseListener l) {
		clearButton.addMouseListener(l);
	}

	/**
	 * Remove an {@link MouseListener} from the Clear button
	 * 
	 * @param l
	 *            the listener
	 */
	public void removeClearButtonMouseListener(MouseListener l) {
		clearButton.removeMouseListener(l);
	}
}
