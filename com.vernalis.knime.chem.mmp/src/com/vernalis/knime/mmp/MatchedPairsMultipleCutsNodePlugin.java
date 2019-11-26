/*******************************************************************************
 * Copyright (c) 2014, 2017, Vernalis (R&D) Ltd
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
/* @(#)$RCSfile$ 
 * $Revision$ $Date$ $Author$
 *
 */
package com.vernalis.knime.mmp;

import org.RDKit.ROMol;
import org.RDKit.RWMol;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * This is the eclipse bundle activator. This has to do some activation stuff to
 * ensure that RDKit works properly Based on
 * org.rdkit.knime.RDKitTypesPluginActivator.java
 * 
 * Note: KNIME node developers probably won't have to do anything in here, as
 * this class is only needed by the eclipse platform/plugin mechanism. If you
 * want to move/rename this file, make sure to change the plugin.xml file in the
 * project root directory accordingly.
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 */
public class MatchedPairsMultipleCutsNodePlugin extends AbstractUIPlugin {
	// The shared instance.
	private static MatchedPairsMultipleCutsNodePlugin plugin;
	public static final String PLUGIN_ID = "com.vernalis.knime.mmp";

	/**
	 * A ROMol Query molecule to match an attachment point
	 */
	public static ROMol AP_QUERY_MOL;

	/**
	 * Constant indicating whether latest version of RDKit rendering is
	 * available (because the user could have an old version of RDKit plugin and
	 * a new version of ours)
	 */
	public static final boolean CAN_RENDER_WITH_NEW_RDKIT_RENDERING = checkRendering();

	/**
	 * The constructor.
	 */
	public MatchedPairsMultipleCutsNodePlugin() {
		super();
		plugin = this;
	}

	/**
	 * Helper method to check we have new version of RDKit rendering code
	 * post-API-break (rdkit 3.3.1 or later)
	 * 
	 * @return
	 */
	private static boolean checkRendering() {
		boolean retVal = true;
		try {
			Class.forName("org.RDKit.ColourPalette");
		} catch (ClassNotFoundException e) {
			retVal = false;
		}
		return retVal;
	}

	/**
	 * This method is called upon plug-in activation.
	 * 
	 * @param context
	 *            The OSGI bundle context
	 * @throws Exception
	 *             If this plugin could not be started
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		AP_QUERY_MOL = RWMol.MolFromSmarts("[#0]");
	}

	/**
	 * This method is called when the plug-in is stopped.
	 * 
	 * @param context
	 *            The OSGI bundle context
	 * @throws Exception
	 *             If this plugin could not be stopped
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		AP_QUERY_MOL.delete();
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return Singleton instance of the Plugin
	 */
	public static MatchedPairsMultipleCutsNodePlugin getDefault() {
		return plugin;
	}

}
