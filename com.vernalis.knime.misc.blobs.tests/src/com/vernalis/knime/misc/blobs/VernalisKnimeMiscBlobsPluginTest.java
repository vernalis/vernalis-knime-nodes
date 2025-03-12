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
package com.vernalis.knime.misc.blobs;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * Test case for the {@link VernalisKnimeMiscBlobsPlugin} class
 *
 * @author s.roughley
 * @since 1.38.0
 */
public class VernalisKnimeMiscBlobsPluginTest {
    
    /**
      * Test to check the activator loads and returns the correct bundle symbolic name
      *
      * @since 1.38.0
      */
    @Test
    public void idTest() {

        VernalisKnimeMiscBlobsPlugin activator =
                VernalisKnimeMiscBlobsPlugin.getDefault();
        Bundle bundle = activator.getBundle();
        String symbolicName = bundle.getSymbolicName();
        assertEquals("com.vernalis.knime.misc.blobs", symbolicName);
    }
}
