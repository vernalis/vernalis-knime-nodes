/*******************************************************************************
 * Copyright (c) 2014,2022, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.flowcontrol.nodes.abstrct.endswitch;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * Abstract <code>NodeDialog</code> for the End Switch Nodes.
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author S. D. Roughley <s.d.roughley@vernalis.com>
 */
@Deprecated
public class AbstractEndSwitchNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring the EndSwitch node.
	 */
	public AbstractEndSwitchNodeDialog() {
		super();
		addDialogComponent(new DialogComponentButtonGroup(
				createSettingsModel(),
				false,
				"Select the behaviour when there are multiple active branches terminated",
				new String[] { "Use first active branch",
						"Use last active branch", "Fail execution" }));

	}

	/**
	 * Creates the settings model for behaviour when multiple input ports are
	 * active
	 * 
	 * @return the settings model string
	 */
	static SettingsModelString createSettingsModel() {
		return new SettingsModelString("Multiple-active behaviour",
				"Fail execution");
	}
}
