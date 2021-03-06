/*******************************************************************************
 * Copyright (c) 2018, 2019 Vernalis (R&D) Ltd
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;

/**
 * This interface defines methods for streamlining {@link SettingsModel}
 * handling. Default implementations are provided
 * 
 * @author S.Roughley
 *
 */
public interface SettingsModelRegistry {

	Set<SettingsModel> getModels();

	/**
	 * Method to store non-null {@link SettingsModel}s for saving/loading
	 * 
	 * @param model
	 *            The model
	 * @return The model unchanged
	 */
	default <T extends SettingsModel> T registerSettingsModel(T model) {
		if (model != null) {
			getModels().add(model);
		}
		return model;
	}

	/**
	 * Method to store multiple {@link SettingsModel}s for saving/loading
	 * 
	 * @param models
	 *            an {@link Iterable} of models
	 * @return the unchanged models
	 */
	default <T extends Iterable<U>, U extends SettingsModel> T registerModels(
			T models) {
		for (U model : models) {
			if (model != null) {
				registerSettingsModel(model);
			}
		}
		return models;
	}

	/**
	 * Method to store multiple models passed as the values of a {@link Map} for
	 * saving / loading
	 * 
	 * @param models
	 *            a {@link Map} of the models
	 * @return the unchanged map
	 */
	default <T extends Map<?, V>, V extends SettingsModel> T registerMapValuesModels(
			T models) {
		registerModels(models.values());
		return models;
	}

	/**
	 * Method to store multiple models passed as the keys of a {@link Map} for
	 * saving / loading
	 * 
	 * @param models
	 *            a {@link Map} of the models
	 * @return the unchanged map
	 */
	default <T extends Map<K, ?>, K extends SettingsModel> T registerMapKeysModels(
			T models) {
		registerModels(models.keySet());
		return models;
	}

	default int getValidatedColumnSelectionModelColumnIndex(
			SettingsModelString model, ColumnFilter filter, DataTableSpec spec,
			Pattern preferredPattern, boolean matchWholeName, NodeLogger logger,
			boolean dontAllowDuplicatesWithAvoid,
			SettingsModelString... modelsToAvoid)
			throws InvalidSettingsException {
		List<String> colNames = spec.stream()
				.filter(col -> filter.includeColumn(col))
				.map(colSpec -> colSpec.getName()).collect(Collectors.toList());

		// Ensure we have a column selected
		if (isStringModelFilled(model)) {
			// We have a selection - check it is present and correct...
			if (!spec.containsName(model.getStringValue())) {
				throw new InvalidSettingsException(
						"The selected column (" + model.getStringValue()
								+ ") is not present in the incoming table");
			} else if (!colNames.contains(model.getStringValue())) {
				throw new InvalidSettingsException(
						"The selected column (" + model.getStringValue()
								+ ") is not of the correct type");
			}
			StringBuilder warningBuilder = new StringBuilder();
			for (SettingsModelString modelToAvoid : modelsToAvoid) {
				if (model.getStringValue()
						.equals(modelToAvoid.getStringValue())) {
					if (warningBuilder.length() == 0) {
						warningBuilder.append("Column selection for '")
								.append(model.getKey()).append("' (")
								.append(model.getStringValue())
								.append(") is also selected for ");
					} else {
						warningBuilder.append(", ");
					}
					warningBuilder.append('\'').append(modelToAvoid.getKey())
							.append('\'');
				}
			}
			if (warningBuilder.length() > 0) {
				if (dontAllowDuplicatesWithAvoid) {
					throw new InvalidSettingsException(
							warningBuilder.toString());
				}
				logger.warn(warningBuilder.toString());
			}
		} else {
			// No column selected - guess, avoiding duplicates
			Arrays.stream(modelsToAvoid).filter(x -> x.getStringValue() != null)
					.forEach(x -> colNames.remove(x.getStringValue()));
			if (colNames.isEmpty()) {
				throw new InvalidSettingsException(filter.allFilteredMsg());
			}
			// Try to match the regex first
			List<String> regexMatches = colNames.stream()
					.filter(x -> matchWholeName
							? preferredPattern.matcher(x).matches()
							: preferredPattern.matcher(x).find())
					.collect(Collectors.toList());
			if (!regexMatches.isEmpty()) {
				model.setStringValue(regexMatches.get(regexMatches.size() - 1));
			} else {
				model.setStringValue(colNames.get(colNames.size() - 1));
			}
			logger.warn("No column selected - guessed '"
					+ model.getStringValue() + "'");

		}
		return spec.findColumnIndex(model.getStringValue());
	}

	/**
	 * Method to check that the table spec contains a column of the name
	 * supplied in the settings model, and that the selected column is of the
	 * required type. If no column is selected (the model contains either
	 * {@code null} or an empty string), then an attempt is made to guess by
	 * picking the last column of the required type
	 * 
	 * @param model
	 *            The model to validate
	 * @param filter
	 *            The {@link ColumnFilter} which the column must pass
	 * @param spec
	 *            The {@link DataTableSpec} to check against
	 * @param logger
	 *            A {@link NodeLogger} instance to warn of guessed column(s)
	 * @return The column index of a (guessed) validated column
	 * @throws InvalidSettingsException
	 *             If the column could not be validated or guessed
	 */
	default int getValidatedColumnSelectionModelColumnIndex(
			SettingsModelString model, ColumnFilter filter, DataTableSpec spec,
			NodeLogger logger, SettingsModelString... modelsToAvoid)
			throws InvalidSettingsException {
		return getValidatedColumnSelectionModelColumnIndex(model, filter, spec,
				Pattern.compile(".*"), true, logger, false, modelsToAvoid);
	}

	/**
	 * Method to check that a {@link SettingsModelString} is filled with a
	 * non-null, non-empty value
	 * 
	 * @param stringModel
	 *            The model to check
	 * @return True of the model holds a non-null, non-empty value
	 */
	default boolean isStringModelFilled(SettingsModelString stringModel) {
		return stringModel.getStringValue() != null
				&& !stringModel.getStringValue().isEmpty();
	}

	default void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		for (SettingsModel model : getModels()) {
			model.loadSettingsFrom(settings);
		}
	}

	default void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		for (SettingsModel model : getModels()) {
			model.validateSettings(settings);
		}

	}

	default void saveSettingsTo(NodeSettingsWO settings) {
		getModels().stream().forEach(x -> x.saveSettingsTo(settings));

	}

}