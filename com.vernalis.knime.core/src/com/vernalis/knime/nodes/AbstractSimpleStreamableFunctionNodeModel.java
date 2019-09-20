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

	@Override
	public Set<SettingsModel> getModels() {
		return models;
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
