/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.knime.dialog.components;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObjectSpec;

/**
 * An empty {@link DialogComponent} as a convenience to allow simple creation of
 * 'hidden' {@link SettingsModel}s which are only accessible through the 'Flow
 * Variables' tab of the Node Dialog
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class DialogComponentEmpty extends DialogComponent {

	/**
	 * @param model
	 *            The settings model to wrap
	 */
	public DialogComponentEmpty(SettingsModel model) {
		super(model);

	}

	@Override
	protected void updateComponent() {
		// Nothing to do

	}

	@Override
	protected void validateSettingsBeforeSave()
			throws InvalidSettingsException {
		// nothing to do

	}

	@Override
	protected void checkConfigurabilityBeforeLoad(PortObjectSpec[] specs)
			throws NotConfigurableException {
		// nothing to do

	}

	@Override
	protected void setEnabledComponents(boolean enabled) {
		// nothing to do

	}

	@Override
	public void setToolTipText(String text) {
		// nothing to do

	}

}
