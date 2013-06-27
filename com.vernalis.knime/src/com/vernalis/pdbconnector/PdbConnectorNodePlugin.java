/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2012, Vernalis (R&D) Ltd and Enspiral Discovery Limited
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ------------------------------------------------------------------------
 */
package com.vernalis.pdbconnector;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * PdbConnectorNode plugin class.
 */
public class PdbConnectorNodePlugin extends Plugin {
    // The shared instance.
    private static PdbConnectorNodePlugin plugin;

    public PdbConnectorNodePlugin() {
        super();
        plugin = this;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);

    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }

    public static PdbConnectorNodePlugin getDefault() {
        return plugin;
    }

}

