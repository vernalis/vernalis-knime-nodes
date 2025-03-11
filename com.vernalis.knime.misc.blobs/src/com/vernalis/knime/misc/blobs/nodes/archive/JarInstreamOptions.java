/*******************************************************************************
 * Copyright (c) 2025, Vernalis (R&D) Ltd
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
package com.vernalis.knime.misc.blobs.nodes.archive;

import org.apache.commons.compress.utils.CharsetNames;

import com.vernalis.knime.misc.blobs.nodes.InputStreamWrapperOptions;

/**
 * {@link InputStreamWrapperOptions} for the JAR format
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class JarInstreamOptions extends ArjInstreamOptions
        implements InputStreamWrapperOptions {

    /**
     * Constructor for instances
     */
    public JarInstreamOptions() {

        this("JAR_OPTIONS");
    }

    /**
     * Constructor for subclasses
     * 
     * @param key
     *            the root settings key
     */
    protected JarInstreamOptions(String key) {

        super(key, CharsetNames.UTF_8);
    }

}
