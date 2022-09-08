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
package com.vernalis.knime.testing.nodes.missingvals;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
    
import org.junit.Test;


/**
  * Test class for the {@link EmptyColumnTestNodeFactory} class
  *
  * @author S Roughley
  */
public class EmptyColumnTestNodeFactoryTest {

    /**
      * Test that the node factory returns the correct number of views
      */
    @Test
    public void testGetNrNodeViews() {
        EmptyColumnTestNodeFactory nodeFactory = new EmptyColumnTestNodeFactory();

        int nrviews = nodeFactory.getNrNodeViews();

        assertEquals(0, nrviews);
    }

    /**
      * Test that the node factory correctly determines whether there
      * is a node dialog
      */
    @Test
    public void testHasDialog() {
        EmptyColumnTestNodeFactory nodeFactory = new EmptyColumnTestNodeFactory();

        boolean hasDialog = nodeFactory.hasDialog();

        assertEquals(true, hasDialog);
    }

    
    /**
     * Test the createNodeView() method for an index higher than the number of
     * views (Any actual views will be tested during testflow execution)
     *
     * @since 07-Sep-2022
     * @since v1.36.0
     * @since 07-Sep-2022
     * @since v1.36.0
     */
    @Test
    public void testCreateNodeView() {
        EmptyColumnTestNodeFactory nodeFactory = new EmptyColumnTestNodeFactory();
        assertNull(nodeFactory.createNodeView(nodeFactory.getNrNodeViews(), nodeFactory.createNodeModel()));
    }
    
    /**
     * Test that there is a node description
     *
     * @since 07-Sep-2022
     * @since v1.36.0
     * @since 07-Sep-2022
     * @since v1.36.0
     */
    @Test
    public void testCreateNodeDescription() {
        EmptyColumnTestNodeFactory nodeFactory = new EmptyColumnTestNodeFactory();
        assertNotNull(nodeFactory.getXMLDescription());
    }
    
    /**
     * Test that there is an icon
     *
     * @since 07-Sep-2022
     * @since v1.36.0
     * @since 07-Sep-2022
     * @since v1.36.0
     */
    @Test
    public void testGetIcon() {
        EmptyColumnTestNodeFactory nodeFactory = new EmptyColumnTestNodeFactory();
        assertNotNull(nodeFactory.getIcon());
    }
    

}
