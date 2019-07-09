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
package com.vernalis.knime.nodes;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;

/**
 * A subclass of {@link SimpleStreamableFunctionNodeModel} for 1 -> 1 nodes.
 * Convenience methods are provided to handle the loading, saving and validation
 * of {@link SettingsModel}s.
 * 
 * @see #registerSettingsModel(SettingsModel)
 * @see #registerModels(Iterable)
 * @see #registerMapValuesModels(Map)
 * @see #registerMapKeysModels(Map)
 * 
 * @author S.Roughley
 *
 */
public abstract class AbstractSimpleStreamableFunctionNodeModel extends
		SimpleStreamableFunctionNodeModel implements SettingsModelRegistry {

	private final Set<SettingsModel> models = new LinkedHashSet<>();

	public AbstractSimpleStreamableFunctionNodeModel() {
		super();
	}

	// /**
	// * Method to store non-null {@link SettingsModel}s for saving/loading
	// *
	// * @param model
	// * The model
	// * @return The model unchanged
	// */
	// @Override
	// public <T extends SettingsModel> T registerSettingsModel(T model) {
	// if (model != null) {
	// models.add(model);
	// }
	// return model;
	// }

	@Override
	public Set<SettingsModel> getModels() {
		return models;
	}

	// /**
	// * Method to store multiple {@link SettingsModel}s for saving/loading
	// *
	// * @param models
	// * an {@link Iterable} of models
	// * @return the unchanged models
	// */
	// @Override
	// public final <T extends Iterable<U>, U extends SettingsModel> T
	// registerModels(
	// T models) {
	// for (U model : models) {
	// if (model != null) {
	// registerSettingsModel(model);
	// }
	// }
	// return models;
	// }
	//
	// /**
	// * Method to store multiple models passed as the values of a {@link Map}
	// for
	// * saving / loading
	// *
	// * @param models
	// * a {@link Map} of the models
	// * @return the unchanged map
	// */
	// @Override
	// public final <T extends Map<?, V>, V extends SettingsModel> T
	// registerMapValuesModels(
	// T models) {
	// registerModels(models.values());
	// return models;
	// }
	//
	// /**
	// * Method to store multiple models passed as the keys of a {@link Map} for
	// * saving / loading
	// *
	// * @param models
	// * a {@link Map} of the models
	// * @return the unchanged map
	// */
	// @Override
	// public final <T extends Map<K, ?>, K extends SettingsModel> T
	// registerMapKeysModels(
	// T models) {
	// registerModels(models.keySet());
	// return models;
	// }

	@Override
	public void saveSettingsTo(NodeSettingsWO settings) {
		// Delegate to the default implementation in SettingsModelRegistry
		SettingsModelRegistry.super.saveSettingsTo(settings);
	}

	@Override
	public void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		SettingsModelRegistry.super.validateSettings(settings);

	}

	@Override
	public void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		SettingsModelRegistry.super.loadValidatedSettingsFrom(settings);
	}

	// /**
	// * Method to check that a {@link SettingsModelString} contains a value
	// (i.e.
	// * a non-{@code null} non-empty String
	// *
	// * @param stringModel
	// * The {@link SettingsModelString} to test
	// * @return {@code true} if a value is provided
	// */
	// @Override
	// public boolean isStringModelFilled(SettingsModelString stringModel) {
	// return stringModel.getStringValue() != null
	// && !stringModel.getStringValue().isEmpty();
	// }

	// /**
	// * Method to check that the table spec contains a column of the name
	// * supplied in the settings model, and that the selected column is of the
	// * required type. If no column is selected (the model contains either
	// * {@code null} or an empty string), then an attempt is made to guess by
	// * picking the last column of the required type
	// *
	// * @param model
	// * The model to validate
	// * @param filter
	// * The {@link ColumnFilter} which the column must pass
	// * @param spec
	// * The {@link DataTableSpec} to check against
	// * @return The column index of a (guessed) validated column
	// * @throws InvalidSettingsException
	// * If the column could not be validated or guessed
	// */
	// @Override
	// public final int getValidatedColumnSelectionModelColumnIndex(
	// SettingsModelString model, ColumnFilter filter, DataTableSpec spec,
	// NodeLogger logger)
	// throws InvalidSettingsException {
	//
	// // Ensure we have a ph4 column selected
	// if (!isStringModelFilled(model)) {
	// // No column selected - guess...
	// for (int i = spec.getNumColumns() - 1; i >= 0; i--) {
	// final DataColumnSpec columnSpec = spec.getColumnSpec(i);
	// if (filter.includeColumn(columnSpec)) {
	// model.setStringValue(columnSpec.getName());
	// setWarningMessage("No column selected - guessed '"
	// + model.getStringValue() + "'");
	// break;
	// }
	// if (i == 0) {
	// throw new InvalidSettingsException(filter.allFilteredMsg());
	// }
	// }
	// } else {
	// // We have a selection - check it is present and correct...
	// final DataColumnSpec columnSpec =
	// spec.getColumnSpec(model.getStringValue());
	// if (columnSpec == null) {
	// throw new InvalidSettingsException(
	// "The selected column (" + model.getStringValue()
	// + ") is not present in the incoming table");
	// } else if (!filter.includeColumn(columnSpec)) {
	// throw new InvalidSettingsException(
	// "The selected column (" + model.getStringValue()
	// + ") is not of the correct type");
	// }
	// }
	// return spec.findColumnIndex(model.getStringValue());
	// }

}
