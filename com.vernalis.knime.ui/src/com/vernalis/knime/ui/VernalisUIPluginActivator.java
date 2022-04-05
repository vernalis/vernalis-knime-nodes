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
package com.vernalis.knime.ui;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.knime.workbench.core.util.ThreadsafeImageRegistry;
import org.osgi.framework.BundleContext;

/**
 * This is the eclipse bundle activator. Note: KNIME node developers probably
 * won't have to do anything in here, as this class is only needed by the
 * eclipse platform/plugin mechanism. If you want to move/rename this file, make
 * sure to change the plugin.xml file in the project root directory accordingly.
 *
 * @author sr
 */
public class VernalisUIPluginActivator extends AbstractUIPlugin {

	// The shared instance.
	private static VernalisUIPluginActivator plugin;
	/** The plugin ID */
	public static String PLUGIN_ID = "com.vernalis.knime.ui";

	/**
	 * The constructor.
	 */
	public VernalisUIPluginActivator() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation.
	 * 
	 * @param context
	 *            The OSGI bundle context
	 * 
	 * @throws Exception
	 *             If this plugin could not be started
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);

	}

	/**
	 * This method is called when the plug-in is stopped.
	 * 
	 * @param context
	 *            The OSGI bundle context
	 * 
	 * @throws Exception
	 *             If this plugin could not be stopped
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#createImageRegistry()
	 */
	@Override
	protected ImageRegistry createImageRegistry() {
		// If we are in the UI Thread use that
		if (Display.getCurrent() != null) {
			return new ThreadsafeImageRegistry(Display.getCurrent());
		}

		if (PlatformUI.isWorkbenchRunning()) {
			return new ThreadsafeImageRegistry(
					PlatformUI.getWorkbench().getDisplay());
		}

		// Invalid thread access if it is not the UI Thread
		// and the workbench is not created.
		throw new SWTError(SWT.ERROR_THREAD_INVALID_ACCESS);
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return Singleton instance of the Plugin
	 */
	public static VernalisUIPluginActivator getDefault() {
		return plugin;
	}

}
