/*******************************************************************************
 * Copyright (c) 2020,2021 Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.query;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.fasterxml.jackson.databind.JsonNode;
import com.vernalis.pdbconnector2.dialogcomponents.swing.CountClearButtonBox;

/**
 * An interface for query dialog components which will contain a
 * {@link CountClearButtonBox}. The query is backed by a {@link QueryModel}
 * implementation. Most methods have default implementations delegating to
 * either the backing {@link QueryModel} or the displayed
 * {@link CountClearButtonBox}
 *
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 * @param <T>
 *            The type of {@link QueryModel} backing the panel
 *
 */
public interface QueryPanel<T extends QueryModel> {

	/**
	 * @return whether the model has a query
	 * @see QueryModel#hasQuery()
	 */
	public default boolean hasQuery() {
		return getQueryModel().hasQuery();
	}

	/**
	 * Clear the query
	 * 
	 * @see QueryModel#clearQuery()
	 */
	public default void clearQuery() {
		getQueryModel().clearQuery();
	}

	/**
	 * Reset the 'Count' button
	 * 
	 * @see CountClearButtonBox#resetCountButton()
	 */
	public default void resetCountButton() {
		getButtons().resetCountButton();
	}

	/**
	 * Reset the 'Clear' button
	 * 
	 * @see CountClearButtonBox#resetClearButton()
	 */
	public default void resetClearButton() {
		getButtons().resetClearButton();
	}

	/**
	 * Reset both 'Count' and 'Clear' buttons
	 * 
	 * @see #resetCountButton()
	 * @see #resetClearButton()
	 */
	public default void resetCountClearButtons() {
		resetCountButton();
		resetClearButton();
	}

	/**
	 * @return The {@link CountClearButtonBox} in the panel
	 */
	public CountClearButtonBox getButtons();

	/**
	 * Return the JSON query for the current panel
	 * 
	 * @param scoringType
	 *            The scoring type
	 * @param resultType
	 *            The result type
	 * @return The JSON Query
	 * @see QueryModel#getCountQuery(ScoringType, QueryResultType)
	 */
	public default JsonNode getCountQuery(ScoringType scoringType,
			QueryResultType resultType) {
		return getQueryModel().getCountQuery(scoringType, resultType);
	}

	/**
	 * @return The {@link QueryModel} backing the panel
	 */
	public T getQueryModel();

	/**
	 * @return The JComponent representing the panel
	 */
	public JComponent getComponent();

	/**
	 * @return A title for the panel border. {@code null} if no title is to be
	 *         displayed
	 */
	public default String getBorderTitle() {
		return null;
	}

	/**
	 * Method to reset the panel border to it's default
	 */
	public default void resetBorder() {
		Border bord = BorderFactory.createCompoundBorder(
				new EtchedBorder(EtchedBorder.RAISED),
				new EtchedBorder(EtchedBorder.LOWERED));
		if (getBorderTitle() != null) {
			bord = new TitledBorder(bord, getBorderTitle());
		}
		bord = BorderFactory.createCompoundBorder(bord,
				new EmptyBorder(5, 5, 5, 5));
		getComponent().setBorder(bord);
	}

	/**
	 * Method to highlight the panel border with the given colour
	 * 
	 * @param highlightColor
	 *            The highlight colour for the border
	 */
	public default void highlightBorder(Color highlightColor) {
		Border bord = BorderFactory.createCompoundBorder(
				new EtchedBorder(EtchedBorder.RAISED, highlightColor,
						highlightColor),
				new EtchedBorder(EtchedBorder.LOWERED));
		if (getBorderTitle() != null) {
			bord = new TitledBorder(bord, getBorderTitle());
		}
		bord = BorderFactory.createCompoundBorder(bord,
				new EmptyBorder(5, 5, 5, 5));
		getComponent().setBorder(bord);
	}

}
