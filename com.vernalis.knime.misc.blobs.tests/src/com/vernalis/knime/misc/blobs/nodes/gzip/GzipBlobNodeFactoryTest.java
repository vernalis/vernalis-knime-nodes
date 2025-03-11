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
package com.vernalis.knime.misc.blobs.nodes.gzip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;


/**
  * Test class for the {@link GzipBlobNodeFactory} class
  *
  * @author s.roughley knime@vernalis.com
  */
public class GzipBlobNodeFactoryTest {

    /**
      * Test that the node factory returns the correct number of views
      */
    @Test
    public void testGetNrNodeViews() {
        GzipBlobNodeFactory nodeFactory = new GzipBlobNodeFactory();

        int nrviews = nodeFactory.getNrNodeViews();

        assertEquals(0, nrviews);
    }

    /**
      * Test that the node factory correctly determines whether there
      * is a node dialog
      */
    @Test
    public void testHasDialog() {
        GzipBlobNodeFactory nodeFactory = new GzipBlobNodeFactory();

        boolean hasDialog = nodeFactory.hasDialog();

        assertEquals(true, hasDialog);
    }

    
    /**
     * Test the createNodeView() method for an index higher than the number of
     * views (Any actual views will be tested during testflow execution)
     *
     * @since 1.38.0
     * 
     */
    @Test
    public void testCreateNodeView() {
        GzipBlobNodeFactory nodeFactory = new GzipBlobNodeFactory();
        assertNull(nodeFactory.createNodeView(nodeFactory.getNrNodeViews(), nodeFactory.createNodeModel()));
    }
    
    /**
     * Test that there is a node description
     *
     * @since 1.38.0
     * 
     */
    @Test
    public void testCreateNodeDescription() {
        GzipBlobNodeFactory nodeFactory = new GzipBlobNodeFactory();
        assertNotNull(nodeFactory.getXMLDescription());
    }
    
    /**
     * Test that there is an icon
     *
     * @since 1.38.0
     * 
     */
    @Test
    public void testGetIcon() {
        GzipBlobNodeFactory nodeFactory = new GzipBlobNodeFactory();
        assertNotNull(nodeFactory.getIcon());
    }
    

}
