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
package com.vernalis.knime.misc.gc.node.heavygc;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * @author s.roughley
 * 
 */
public class RunHeavyGCNodeDialog extends DefaultNodeSettingsPane {

	public RunHeavyGCNodeDialog() {
		super();
		addDialogComponent(
				new DialogComponentNumber(createCollectionsNumberModel(),
						"Number of Garbage Collections", 1));
		addDialogComponent(new DialogComponentNumber(createDelayModel(),
				"Delay between collections (ms)", 50));
	}

	/**
	 * @return Settings model for the delay in milliseconds between Garbage
	 *         Collections
	 */
	protected static SettingsModelIntegerBounded createDelayModel() {
		return new SettingsModelIntegerBounded("GC Delay", 400, 50, 5000);
	}

	/**
	 * @return SettingsModel for the number of Garbage Collections
	 */
	protected static SettingsModelIntegerBounded createCollectionsNumberModel() {
		return new SettingsModelIntegerBounded("Number of Collections", 5, 1,
				10);
	}
}
