/*******************************************************************************
 * Copyright (c) 2018, 2024, Vernalis (R&D) Ltd
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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

import com.vernalis.knime.data.datacolumn.RegexColumnNameColumnFilter;

/**
 * This interface defines methods for streamlining {@link SettingsModel}
 * handling. Default implementations are provided for most methods
 * 
 * <p>
 * 1.31.0
 * </p>
 * <p>
 * New methods added to allow a version to be stored and a default be applied to
 * new settings models rather than fail. The default implementation fails to
 * allow the new {@link #registerSettingsModel(SettingsModel, int, Consumer)}
 * and {@link #registerSettingsModel(SettingsModel, int, Consumer, String)}
 * methods to work. Implementations wishing to enable this feature need to
 * enable {@link #getModelWrappers()} to return a modifiable collection. They
 * should also do one of:
 * <ul>
 * <li>Redirect {@link #registerSettingsModel(SettingsModel)} to call one of
 * these methods with suitable default values, and {@link #getModels()} to
 * {@code return getModelWrappers()}.keySet()}</li>
 * <li>Override
 * {@link #registerSettingsModel(SettingsModel, int, Consumer, String)} with a
 * call to {@link #registerSettingsModel(SettingsModel)} followed by
 * {@code super.registerSettingsModel(model, fromVersion, defaultSetter, successMessage}</li>
 * </ul>
 * Implementations should also provide a functional implementation of
 * {@link #setNodeWarningMessage(String)}, and may also which to override
 * {@link #getNodeLogger()} to return the logger instance for the node itself is
 * this is used as a field in the NodeModel. The implementing class
 * {@link SettingsModelRegistryImpl} can be used for this purpose
 * </p>
 * 
 * 
 * @author S.Roughley
 *
 */
public interface SettingsModelRegistry {

	/**
	 * @return A set containing all the registered models
	 */
	Set<SettingsModel> getModels();

	/**
	 * Method to store non-null {@link SettingsModel}s for saving/loading
	 * 
	 * @param <T>
	 *            The type of the model to register
	 * 
	 * @param model
	 *            The model
	 * 
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
	 * @param <T>
	 *            The type of the iterable of models
	 * @param <U>
	 *            the type of the actual model
	 * 
	 * @param models
	 *            an {@link Iterable} of models
	 * 
	 * @return the unchanged models
	 */
	default <T extends Iterable<U>, U extends SettingsModel> T
			registerModels(T models) {
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
	 * @param <T>
	 *            The type of the Map
	 * @param <V>
	 *            The type of the actual model (the map value)
	 * 
	 * @param models
	 *            a {@link Map} of the models
	 * 
	 * @return the unchanged map
	 */
	default <T extends Map<?, V>, V extends SettingsModel> T
			registerMapValuesModels(T models) {
		registerModels(models.values());
		return models;
	}

	/**
	 * Method to store multiple models passed as the keys of a {@link Map} for
	 * saving / loading
	 * 
	 * @param <T>
	 *            The type of the Map
	 * @param <K>
	 *            The type of the actual model (the map key)
	 * 
	 * @param models
	 *            a {@link Map} of the models
	 * 
	 * @return the unchanged map
	 */
	default <T extends Map<K, ?>, K extends SettingsModel> T
			registerMapKeysModels(T models) {
		registerModels(models.keySet());
		return models;
	}

	/**
	 * Method to get the index of a validated column selection. The method will
	 * guess the last column which is not selected by any of the
	 * {@code modelsToAvoid}. It will attempt to select the last column matching
	 * both the {@code filter} and the regex {@code preferredPattern}. If no
	 * column is available which matches the regex, and is not selected by
	 * another model and which passes the ColumnFilter, then the last column
	 * matching the ColumnFilter is returned
	 * 
	 * @param model
	 *            The model to check or guess
	 * @param filter
	 *            The ColumnFilter of allowed columns
	 * @param spec
	 *            The table spec
	 * @param preferredPattern
	 *            A regex of preferred column names
	 * @param matchWholeName
	 *            Should the regex match the whole name
	 * @param logger
	 *            A NodeLogger instance to pass warnings to
	 * @param dontAllowDuplicatesWithAvoid
	 *            Should duplicates with the models to avoid be allowed if no
	 *            other match is possible
	 * @param modelsToAvoid
	 *            The models to avoid
	 * 
	 * @return The column index
	 * 
	 * @throws InvalidSettingsException
	 *             if the selected column was not present or of the wrong type,
	 *             or no column was selected and none could be guessed
	 */
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
	 * @param modelsToAvoid
	 *            The settings models to avoid
	 * 
	 * @return The column index of a (guessed) validated column
	 * 
	 * @throws InvalidSettingsException
	 *             If the column could not be validated or guessed
	 * 
	 * @see #getValidatedColumnSelectionModelColumnIndex(SettingsModelString,
	 *      ColumnFilter, DataTableSpec, Pattern, boolean, NodeLogger, boolean,
	 *      SettingsModelString...)
	 */
	default int getValidatedColumnSelectionModelColumnIndex(
			SettingsModelString model, ColumnFilter filter, DataTableSpec spec,
			NodeLogger logger, SettingsModelString... modelsToAvoid)
			throws InvalidSettingsException {
		return getValidatedColumnSelectionModelColumnIndex(model, filter, spec,
				Pattern.compile(".*"), true, logger, false, modelsToAvoid);
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
	 * @param dontAllowDuplicatesWithAvoid
	 *            Should duplicates with the models to avoid be allowed if no
	 *            other match is possible
	 * @param modelsToAvoid
	 *            The settings models to avoid
	 * 
	 * 
	 * @return The column index of a validated, possibly guessed, column
	 * 
	 * @throws InvalidSettingsException
	 *             if the selected column was not present or of the wrong type,
	 *             or no column was selected and none could be guessed
	 * 
	 * @see #getValidatedColumnSelectionModelColumnIndex(SettingsModelString,
	 *      ColumnFilter, DataTableSpec, Pattern, boolean, NodeLogger, boolean,
	 *      SettingsModelString...)
	 * @since 1.37.0
	 */
	default int getValidatedColumnSelectionModelColumnIndex(
			SettingsModelString model, RegexColumnNameColumnFilter filter,
			DataTableSpec spec, NodeLogger logger,
			boolean dontAllowDuplicatesWithAvoid,
			SettingsModelString... modelsToAvoid)
			throws InvalidSettingsException {
		return getValidatedColumnSelectionModelColumnIndex(model, filter, spec,
				filter.getRegex(), true, logger, dontAllowDuplicatesWithAvoid,
				modelsToAvoid);
	}

	/**
	 * Method to check that a {@link SettingsModelString} is filled with a
	 * non-null, non-empty value
	 * 
	 * @param stringModel
	 *            The model to check
	 * 
	 * @return True of the model holds a non-null, non-empty value
	 */
	default boolean isStringModelFilled(SettingsModelString stringModel) {
		return stringModel.getStringValue() != null
				&& !stringModel.getStringValue().isEmpty();
	}

	/**
	 * Method to load the saved values into the models
	 * 
	 * @param settings
	 *            The node settings
	 * 
	 * @throws InvalidSettingsException
	 *             if there was a problem loading the settings
	 */
	default void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		int savedVersion = getSavedSettingsVersion(settings);

		for (SettingsModel model : getModels()) {
			try {
				model.loadSettingsFrom(settings);
			} catch (InvalidSettingsException e) {
				SettingsModelWrapper<?> modelWrapper =
						getModelWrappers().get(model);
				if (modelWrapper == null) {
					if (getSettingsVersion() == 1) {
						throw e;
					} else {
						throw new InvalidSettingsException(
								"No default value setter provided for new setting ("
										+ model.toString() + ")",
								e);
					}
				}

				if (modelWrapper.getFromVersion() <= savedVersion) {
					// A problem..
					throw e;
				}
				setNodeWarningMessage(modelWrapper.setToDefault(settings));
			}
		}
	}

	/**
	 * Method to check that the models can all be loaded from the saved settings
	 * 
	 * @param settings
	 *            The node settings
	 * 
	 * @throws InvalidSettingsException
	 *             if there was a problem validating the settings
	 */
	default void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		int savedVersion = getSavedSettingsVersion(settings);
		for (SettingsModel model : getModels()) {
			try {
				model.validateSettings(settings);
			} catch (InvalidSettingsException e) {
				SettingsModelWrapper<?> modelWrapper =
						getModelWrappers().get(model);
				if (modelWrapper == null) {
					if (getSettingsVersion() == 1) {
						throw e;
					} else {
						throw new InvalidSettingsException(
								"No default value setter provided for new setting ("
										+ model.toString() + ")",
								e);
					}
				}

				if (modelWrapper.getFromVersion() <= savedVersion) {
					// A problem..
					throw e;
				}
				setNodeWarningMessage(modelWrapper.setToDefault(settings));
			}
		}

	}

	/**
	 * Method to save the current values of the models to the settings object
	 * 
	 * @param settings
	 *            The node settings
	 */
	default void saveSettingsTo(NodeSettingsWO settings) {
		getModels().stream().forEach(x -> x.saveSettingsTo(settings));

		// And save the version (since 20-Jan-2022)
		settings.addInt(SMR_VERSION_KEY, getSettingsVersion());
	}

	/**
	 * The settings key for the version of the saved settings
	 * 
	 * @since 20-Jan-2022
	 */
	public static final String SMR_VERSION_KEY = "SMR_VERSION";

	/**
	 * @param settings
	 *            The node settings
	 * 
	 * @return The saved settings version
	 * 
	 * @since 20-Jan-2022
	 */
	public default int getSavedSettingsVersion(NodeSettingsRO settings) {
		final int retVal = settings.getInt(SMR_VERSION_KEY, 1);
		if (retVal > getSettingsVersion()) {
			// Strange!
			setNodeWarningMessage(
					"A newer version of the settings were found than the "
							+ "current node implementation - node behaviour may be unexpected!");
		}
		return retVal;
	}

	/**
	 * @return The current version of the settings to save
	 * 
	 * @since 20-Jan-2022
	 */
	public default int getSettingsVersion() {
		return 1;
	}

	/**
	 * @return A map of {@link SettingsModel} to {@link SettingsModelWrapper}
	 *         which indicate the version the model was introduced in, and
	 *         functionality to set a backwards compatible behaviour if possible
	 *
	 * @since 21-Jan-2022
	 */
	public default Map<SettingsModel, SettingsModelWrapper<?>>
			getModelWrappers() {
		return Collections.emptyMap();
	}

	/**
	 * Method to set a warning message. Implementations should implement this
	 * method by passing to the {@code NodeModel.setWarningMessage()} method
	 * 
	 * @param message
	 *            the message to set as a warning
	 *
	 * @since 20-Jan-2022
	 */
	public default void setNodeWarningMessage(String message) {
		// Default implementation does nothing - nodes should implement this by
		// passing to NodeModel#setWarningMessage()
	}

	/**
	 * @return A NodeLogger instance for logging to
	 *
	 * @since 20-Jan-2022
	 */
	public default NodeLogger getNodeLogger() {
		// As this method performs a lookup to ensure only one logger instance
		// per class, there is no need to override this in implemenations with
		// their own NodeLogger
		return NodeLogger.getLogger(getClass());
	}

	/**
	 * Method to register a settings model with versioning behaviour, returning
	 * a default warning message. This method does not require access to the
	 * saved node settings to apply the default
	 * 
	 * @param <T>
	 *            The type of SettingsModel implementation
	 * @param model
	 *            The model
	 * @param sinceVersion
	 *            The version number which this was introduced in
	 * @param defaultSetter
	 *            The Consumer to set the default value for legacy behaviour
	 * 
	 * @return the registered model
	 * 
	 * @throws UnsupportedOperationException
	 *             if registration could not be completed. In the default
	 *             implementation this is likely because
	 *             {@link #getModelWrappers()} has not been overridden
	 * @throws IllegalArgumentException
	 *             If the version is not a positive integer
	 *
	 * @see #registerSettingsModel(SettingsModel, int, Consumer, String)
	 * @see #registerSettingsModel(SettingsModel, int, BiConsumer)
	 * @see #registerSettingsModel(SettingsModel, int, BiConsumer, String)
	 * 
	 * @since 20-Jan-2022
	 */
	public default <T extends SettingsModel> T registerSettingsModel(T model,
			int sinceVersion, Consumer<T> defaultSetter) {
		return registerSettingsModel(model, sinceVersion,
				(t, u) -> defaultSetter.accept(t));
	}

	/**
	 * Method to register a settings model with versioning behaviour, returning
	 * a default warning message. This method does not require access to the
	 * saved node settings to apply the default. A custom warning message may be
	 * supplied.
	 * 
	 * @param <T>
	 *            The type of SettingsModel implementation
	 * @param model
	 *            The model
	 * @param sinceVersion
	 *            The version number which this was introduced in
	 * @param defaultSetter
	 *            The Consumer to set the default value for legacy behaviour
	 * @param successMessage
	 *            A message indicating successful application of a
	 *            legacy-compatible settings model value
	 * 
	 * @return the registered model
	 * 
	 * @throws UnsupportedOperationException
	 *             if registration could not be completed. In the default
	 *             implementation this is likely because
	 *             {@link #getModelWrappers()} has not been overridden
	 * @throws IllegalArgumentException
	 *             If the version is not a positive integer
	 *
	 * @see #registerSettingsModel(SettingsModel, int, Consumer, String)
	 * 
	 * @since 21-Jan-2022
	 * 
	 * @see #registerSettingsModel(SettingsModel, int, BiConsumer)
	 * @see #registerSettingsModel(SettingsModel, int, BiConsumer, String)
	 */
	public default <T extends SettingsModel> T registerSettingsModel(T model,
			int sinceVersion, Consumer<T> defaultSetter, String successMessage)
			throws UnsupportedOperationException, IllegalArgumentException {
		return registerSettingsModel(model, sinceVersion,
				(t, u) -> defaultSetter.accept(t), successMessage);
	}

	/**
	 * Method to register a settings model with versioning behaviour, returning
	 * a default warning message
	 * 
	 * @param <T>
	 *            The type of SettingsModel implementation
	 * @param model
	 *            The model
	 * @param sinceVersion
	 *            The version number which this was introduced in
	 * @param defaultSetter
	 *            The Consumer to set the default value for legacy behaviour
	 * 
	 * @return the registered model
	 * 
	 * @throws UnsupportedOperationException
	 *             if registration could not be completed. In the default
	 *             implementation this is likely because
	 *             {@link #getModelWrappers()} has not been overridden
	 * @throws IllegalArgumentException
	 *             If the version is not a positive integer
	 *
	 * @see #registerSettingsModel(SettingsModel, int, Consumer, String)
	 * 
	 * @since 20-Jan-2022
	 * 
	 */
	public default <T extends SettingsModel> T registerSettingsModel(T model,
			int sinceVersion, BiConsumer<T, NodeSettingsRO> defaultSetter) {
		return registerSettingsModel(model, sinceVersion, defaultSetter,
				"New settings found - applying legacy behaviour-preserving options");
	}

	/**
	 * Method to register a settings model with versioning behaviour, returning
	 * a custom warning message
	 * 
	 * @param <T>
	 *            The type of SettingsModel implementation
	 * @param model
	 *            The model
	 * @param sinceVersion
	 *            The version number which this was introduced in
	 * @param defaultSetter
	 *            The Consumer to set the default value for legacy behaviour
	 * @param successMessage
	 *            A message indicating successful application of a
	 *            legacy-compatible settings model value
	 * 
	 * @return the registered model
	 * 
	 * @throws UnsupportedOperationException
	 *             if registration could not be completed. In the default
	 *             implementation this is likely because
	 *             {@link #getModelWrappers()} has not been overridden
	 * @throws IllegalArgumentException
	 *             If the version is not a positive integer
	 *
	 * @see #registerSettingsModel(SettingsModel, int, Consumer, String)
	 * 
	 * @since 21-Jan-2022
	 */
	public default <T extends SettingsModel> T registerSettingsModel(T model,
			int sinceVersion, BiConsumer<T, NodeSettingsRO> defaultSetter,
			String successMessage)
			throws UnsupportedOperationException, IllegalArgumentException {
		if (model != null) {
			if (sinceVersion < 1) {
				throw new IllegalArgumentException(
						"Since version must be a positive integer");
			}
			getModelWrappers().put(model, new SettingsModelWrapper<>(
					sinceVersion, model, defaultSetter, successMessage));
		}
		return model;
	}

}