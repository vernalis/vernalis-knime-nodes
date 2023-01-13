/*******************************************************************************
 * Copyright (c) 2023, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.collection.abstrct;

import org.knime.core.data.DataValue;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.data.collection.ListDataValue;
import org.knime.core.data.collection.SetDataValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;

import static java.util.Objects.requireNonNull;

/**
 * Abstract base class for node dialogs for collection handling nodes which take
 * multiple input collection columns
 * 
 * @author S.Roughley knime@vernalis.com
 * 
 * @since 1.36.2
 *
 */
public abstract class AbstractMultiCollectionNodeDialog
		extends DefaultNodeSettingsPane {

	private static final String FAIL_IF_NO_MATCHING_COLUMNS =
			"Fail if no matching columns";
	/**
	 * Constant for the default acceptSets value
	 *
	 * @since 1.36.2
	 */
	static final boolean DEFAULT_ACCEPT_SETS = true;
	/**
	 * Constant for the default acceptLists value
	 *
	 * @since 1.36.2
	 */
	static final boolean DEFAULT_ACCEPT_LISTS = true;
	/**
	 * Constant for the default column selector name
	 *
	 * @since 1.36.2
	 */
	static final String COLLECTION_COLUMNS = "Collection Columns";
	private static final String REPLACE_INPUT_COLUMNS =
			"Replace input columns?";

	/**
	 * Overloaded constructor for nodes accepting any collection with default
	 * selector name ('Collection Columns')
	 * 
	 * @param includeReplaceInputsOption
	 *            Should the dialog include an option to replace input columns
	 *
	 * @since 1.36.2
	 */
	protected AbstractMultiCollectionNodeDialog(
			boolean includeReplaceInputsOption) {
		this(COLLECTION_COLUMNS, includeReplaceInputsOption,
				DEFAULT_ACCEPT_LISTS, DEFAULT_ACCEPT_SETS);
	}

	/**
	 * Constructor
	 * 
	 * @param collectionSelectorName
	 *            The title for the box containing the optional
	 * @param includeReplaceInputsOption
	 *            Should the dialog include an option to replace input columns
	 * @param acceptLists
	 *            Should the filter accept Lists
	 * @param acceptSets
	 *            Should the filter accept Sets
	 * 
	 * @throws IllegalArgumentException
	 *             if neither acceptLists nor acceptSets is {@code true}
	 * @throws NullPointerException
	 *             if collectionSelectorName is {@code null}
	 */
	protected AbstractMultiCollectionNodeDialog(String collectionSelectorName,
			boolean includeReplaceInputsOption, boolean acceptLists,
			boolean acceptSets)
			throws IllegalArgumentException, NullPointerException {
		prependDialogComponents();
		createNewGroup(
				requireNonNull(collectionSelectorName, "columnSelectorName"));
		addDialogComponent(
				new DialogComponentColumnFilter2(createColumnFilterModel(
						collectionSelectorName, acceptLists, acceptSets), 0));
		setHorizontalPlacement(true);
		addDialogComponent(
				new DialogComponentBoolean(createFailIfNoMatchingColumnsModel(),
						FAIL_IF_NO_MATCHING_COLUMNS));
		if (includeReplaceInputsOption) {
			addDialogComponent(new DialogComponentBoolean(
					createReplaceInputColumnsModel(), REPLACE_INPUT_COLUMNS));
		}
		setHorizontalPlacement(false);
		closeCurrentGroup();
		postpendDialogComponents();
	}

	/**
	 * @return model for the 'Fail if no matching columns' setting
	 *
	 * @since 1.36.2
	 */
	static SettingsModelBoolean createFailIfNoMatchingColumnsModel() {
		return new SettingsModelBoolean(FAIL_IF_NO_MATCHING_COLUMNS, false);
	}

	/**
	 * Optional hook to add additional dialog components after the collection
	 * column selector
	 *
	 * @since 1.36.2
	 */
	protected void postpendDialogComponents() {
		// Do nothing

	}

	/**
	 * Optional hook to add additional dialog components before the collection
	 * column selector
	 * 
	 * @since 1.36.2
	 */
	protected void prependDialogComponents() {
		// Do nothing
	};

	/**
	 * @param modelKey
	 *            The key for the settings model
	 * @param acceptSets
	 *            Should the filter accept Sets
	 * @param acceptLists
	 *            Should the filter accept Lists
	 * 
	 * @return The settings model for the List column names
	 * 
	 * @throws IllegalArgumentException
	 *             if neither acceptLists nor acceptSets is {@code true}
	 * @throws NullPointerException
	 *             if modelKey is {@code null}
	 */
	@SuppressWarnings("unchecked")
	static SettingsModelColumnFilter2 createColumnFilterModel(String modelKey,
			boolean acceptLists, boolean acceptSets)
			throws IllegalArgumentException, NullPointerException {
		Class<? extends DataValue> allowedType;
		if (acceptLists && acceptSets) {
			allowedType = CollectionDataValue.class;
		} else if (acceptSets) {
			allowedType = SetDataValue.class;
		} else if (acceptLists) {
			allowedType = ListDataValue.class;
		} else {
			throw new IllegalArgumentException(
					"At least one of acceptLists and acceptSets must be 'true'");
		}
		return new SettingsModelColumnFilter2(
				requireNonNull(modelKey, "modelKey"), allowedType);
	}

	/**
	 * @return the model for the replace input columns option
	 */
	static SettingsModelBoolean createReplaceInputColumnsModel() {
		return new SettingsModelBoolean(REPLACE_INPUT_COLUMNS, true);
	}
}
