/*******************************************************************************
 * Copyright (c) 2018, 2022, Vernalis (R&D) Ltd
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

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;
import org.knime.core.node.util.ColumnFilter;

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

	private SettingsModelRegistryImpl smr;

	public AbstractSimpleStreamableFunctionNodeModel() {
		this(1);
	}

	/**
	 * @param nodeSettingsVersion
	 *            the version of the current node settings implementation
	 *
	 * @since 29-Mar-2022
	 */
	protected AbstractSimpleStreamableFunctionNodeModel(
			int nodeSettingsVersion) {
		smr = new SettingsModelRegistryImpl(nodeSettingsVersion, getLogger()) {

			@Override
			public void doSetWarningMessage(String message) {
				setWarningMessage(message);

			}
		};
	}

	@Override
	public Set<SettingsModel> getModels() {
		return smr.getModels();
	}

	@Override
	public int getSettingsVersion() {
		return smr.getSettingsVersion();
	}

	@Override
	public NodeLogger getNodeLogger() {
		return smr.getNodeLogger();
	}

	@Override
	public <T extends SettingsModel> T registerSettingsModel(T model,
			int sinceVersion, Consumer<T> defaultSetter, String successMessage)
			throws UnsupportedOperationException, IllegalArgumentException {
		return smr.registerSettingsModel(model, sinceVersion, defaultSetter,
				successMessage);
	}

	@Override
	public <T extends SettingsModel> T registerSettingsModel(T model,
			int sinceVersion, BiConsumer<T, NodeSettingsRO> defaultSetter) {
		return smr.registerSettingsModel(model, sinceVersion, defaultSetter);
	}

	@Override
	public <T extends SettingsModel> T registerSettingsModel(T model,
			int sinceVersion, BiConsumer<T, NodeSettingsRO> defaultSetter,
			String successMessage)
			throws UnsupportedOperationException, IllegalArgumentException {
		return smr.registerSettingsModel(model, sinceVersion, defaultSetter,
				successMessage);
	}

	@Override
	public Map<SettingsModel, SettingsModelWrapper<?>> getModelWrappers() {
		return smr.getModelWrappers();
	}

	@Override
	public void setNodeWarningMessage(String message) {
		smr.setNodeWarningMessage(message);
	}

	public void doSetWarningMessage(String message) {
		smr.doSetWarningMessage(message);
	}

	@Override
	public <T extends SettingsModel> T registerSettingsModel(T model) {
		return smr.registerSettingsModel(model);
	}

	@Override
	public <T extends Iterable<U>, U extends SettingsModel> T
			registerModels(T models) {
		return smr.registerModels(models);
	}

	@Override
	public <T extends Map<?, V>, V extends SettingsModel> T
			registerMapValuesModels(T models) {
		return smr.registerMapValuesModels(models);
	}

	@Override
	public <T extends Map<K, ?>, K extends SettingsModel> T
			registerMapKeysModels(T models) {
		return smr.registerMapKeysModels(models);
	}

	@Override
	public int getValidatedColumnSelectionModelColumnIndex(
			SettingsModelString model, ColumnFilter filter, DataTableSpec spec,
			Pattern preferredPattern, boolean matchWholeName, NodeLogger logger,
			boolean dontAllowDuplicatesWithAvoid,
			SettingsModelString... modelsToAvoid)
			throws InvalidSettingsException {
		return smr.getValidatedColumnSelectionModelColumnIndex(model, filter,
				spec, preferredPattern, matchWholeName, logger,
				dontAllowDuplicatesWithAvoid, modelsToAvoid);
	}

	@Override
	public int getValidatedColumnSelectionModelColumnIndex(
			SettingsModelString model, ColumnFilter filter, DataTableSpec spec,
			NodeLogger logger, SettingsModelString... modelsToAvoid)
			throws InvalidSettingsException {
		return smr.getValidatedColumnSelectionModelColumnIndex(model, filter,
				spec, logger, modelsToAvoid);
	}

	@Override
	public String toString() {
		return smr.toString();
	}

	@Override
	public boolean isStringModelFilled(SettingsModelString stringModel) {
		return smr.isStringModelFilled(stringModel);
	}

	@Override
	public int getSavedSettingsVersion(NodeSettingsRO settings) {
		return smr.getSavedSettingsVersion(settings);
	}

	@Override
	public <T extends SettingsModel> T registerSettingsModel(T model,
			int sinceVersion, Consumer<T> defaultSetter) {
		return smr.registerSettingsModel(model, sinceVersion, defaultSetter);
	}

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

}
