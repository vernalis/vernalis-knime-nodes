/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.misc.distance;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

/**
 * Node Dialog for the distance nodes
 * 
 * @author s.roughley
 *
 */
public class AbstractDistanceNodeDialog
		extends AbstractVectorDistanceNodeDialog {

	private static final String RETURN_SQUARED_DISTANCE =
			"Return squared Distance";

	/**
	 * Constructor for a non-collection-based implementation. At least one
	 * dimension name must be supplied
	 * 
	 * @param firstDimension
	 *            The name of the first dimension
	 * @param extraDimensions
	 *            The name of any additional dimensions
	 */
	public AbstractDistanceNodeDialog(char firstDimension,
			char... extraDimensions) {
		this(false, firstDimension, extraDimensions);
	}

	/**
	 * Convenience constructor for implementations requiring only a pair of
	 * columns, which may be collection based
	 * 
	 * @param isCollectionBased
	 *            Are the columns collection columns, in which case the node is
	 *            n-dimensional
	 */
	public AbstractDistanceNodeDialog(boolean isCollectionBased) {
		this(isCollectionBased, isCollectionBased ? 'n' : 'x');
	}

	/**
	 * Full constructor, exposing all options, some combinations of which may be
	 * invalid
	 * 
	 * @param isCollectionBased
	 *            Are the columns collection columns, in which case the node is
	 *            n-dimensional, and only the first dimension can be supplied
	 * @param firstDimension
	 *            The name of the first dimension
	 * @param extraDimensions
	 *            The name of any additional dimensions
	 */
	private AbstractDistanceNodeDialog(boolean isCollectionBased,
			char firstDimension, char... extraDimensions) {
		super(isCollectionBased, firstDimension, extraDimensions);
		addDialogComponent(new DialogComponentBoolean(
				createSquareDistanceModel(), RETURN_SQUARED_DISTANCE));
	}

	/**
	 * @return The settings model for the option to return squared distance
	 */
	static SettingsModelBoolean createSquareDistanceModel() {
		return new SettingsModelBoolean(RETURN_SQUARED_DISTANCE, false);
	}

}
