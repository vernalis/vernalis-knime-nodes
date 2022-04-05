/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.defaultnodesettings.SettingsModel;

/**
 * An Abstract {@link SettingsModelRegistry} implementation for use as a field
 * in NodeModels. To retro-fit this to a NodeModel which implements
 * {@link SettingsModelRegistry}, it should be set up as a final field, and all
 * interface methods in the NodeModel should delegate to the field
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public abstract class SettingsModelRegistryImpl
		implements SettingsModelRegistry {

	private static final int DEFAULT_VERSION = 1;
	private final int currentVersion;
	private final NodeLogger logger;
	private final Set<SettingsModel> models = new LinkedHashSet<>();
	private final Map<SettingsModel, SettingsModelWrapper<?>> wrappers =
			new LinkedHashMap<>();

	/**
	 * Constructor with default version number
	 * 
	 * @param logger
	 *            the {@link NodeLogger} instance
	 */
	protected SettingsModelRegistryImpl(NodeLogger logger) {
		this(DEFAULT_VERSION, logger);
	}

	/**
	 * Full Constructor
	 * 
	 * @param currentVersion
	 *            the current version number of the settings object
	 * @param logger
	 *            The {@link NodeLogger} instance
	 */
	protected SettingsModelRegistryImpl(int currentVersion, NodeLogger logger) {
		this.currentVersion = currentVersion;
		this.logger = logger;
	}

	@Override
	public int getSettingsVersion() {
		return currentVersion;
	}

	@Override
	public NodeLogger getNodeLogger() {
		return logger;
	}

	@Override
	public <T extends SettingsModel> T registerSettingsModel(T model,
			int sinceVersion, BiConsumer<T, NodeSettingsRO> defaultSetter,
			String successMessage)
			throws UnsupportedOperationException, IllegalArgumentException {
		// We make sure the new model is registered. All the other
		// 'defaultSetter' methods delegate here so only this one needs
		// overridden
		registerSettingsModel(model);
		return SettingsModelRegistry.super.registerSettingsModel(model,
				sinceVersion, defaultSetter, successMessage);
	}

	@Override
	public Set<SettingsModel> getModels() {
		return models;
	}

	@Override
	public Map<SettingsModel, SettingsModelWrapper<?>> getModelWrappers() {
		return wrappers;
	}

	@Override
	public void setNodeWarningMessage(String message) {
		doSetWarningMessage(message);
	}

	/**
	 * Helper method to be a hook in NodeModels
	 * 
	 * @param message
	 *            The message to set
	 *
	 * @since 21-Jan-2022
	 */
	public abstract void doSetWarningMessage(String message);

}
