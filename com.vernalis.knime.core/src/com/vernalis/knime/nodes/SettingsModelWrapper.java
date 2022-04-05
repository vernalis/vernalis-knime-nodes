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

import java.util.function.BiConsumer;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.defaultnodesettings.SettingsModel;

/**
 * A simple wrapper class for a {@link SettingsModel} with methods to indicated
 * the version and provide a method to apply a default value to maintain
 * backwards compatibility
 * 
 * @author S.Roughley knime@vernalis.com
 *
 * @param <T>
 *            The type of the SettingsModel to wrap
 */
public class SettingsModelWrapper<T extends SettingsModel> {

	private final int fromVersion;
	private final T model;
	private final BiConsumer<T, NodeSettingsRO> defaultSetter;
	private String successMessage;

	/**
	 * Constructor
	 * 
	 * @param fromVersion
	 *            version the model was introduced
	 * @param model
	 *            the model
	 * @param defaultSetter
	 *            the default setter consumer which aplies backwards-compatible
	 *            settings for older model versions
	 * @param successMessage
	 *            message to log when legacy setting successfully applied
	 *
	 * @since 05-Apr-2022
	 */
	public SettingsModelWrapper(int fromVersion, T model,
			BiConsumer<T, NodeSettingsRO> defaultSetter,
			String successMessage) {
		this.fromVersion = fromVersion;
		this.model = model;
		this.defaultSetter = defaultSetter;
		this.successMessage = successMessage;
	}

	/**
	 * @return the version number of the node when it was introduced
	 *
	 * @since 21-Jan-2022
	 */
	public int getFromVersion() {
		return fromVersion;
	}

	/**
	 * @return the wrapped {@link SettingsModel}
	 *
	 * @since 21-Jan-2022
	 */
	public T getModel() {
		return model;
	}

	/**
	 * @param settings
	 *            The saved node settings object in case it is required to apply
	 *            the new or backwards compatible value
	 * 
	 * @return A message to show in the NodeWarning
	 * 
	 * @throws InvalidSettingsException
	 *             if the default value was able to be applied
	 *
	 * @since 21-Jan-2022
	 */
	public String setToDefault(NodeSettingsRO settings)
			throws InvalidSettingsException {
		try {
			defaultSetter.accept(model, settings);
			return successMessage;
		} catch (Exception e) {
			throw new InvalidSettingsException(
					"Unable to apply legacy setting to new option - "
							+ e.getMessage(),
					e);
		}
	}
}
