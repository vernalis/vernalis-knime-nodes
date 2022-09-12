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
package com.vernalis.knime.db.nodes.selector;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
  * Test class for the {@link DBSelectTablelikeNodeFactory} class
  * 
  * <p>
  * We subclass {@link DBSelectTablelikeNodeFactory} to ensure the tested 
  * methods are available here even if they come from an intermediate
  * super class
  * </p>
  *
  * @author S Roughley
  *
  * @since 07-Sep-2022
  * @since v1.36.0
  * @since 07-Sep-2022
  * @since v1.36.0
  */
public class DBSelectTablelikeNodeFactoryTest extends DBSelectTablelikeNodeFactory {
    
    /**
      * Test that the node factory returns the correct number of views
      *
      * @since 07-Sep-2022
      * @since v1.36.0
      * @since 07-Sep-2022
      * @since v1.36.0
      */
    @Test
    public void testGetNrNodeViews() {
        
        int nrviews = getNrNodeViews();
        
        assertEquals(0, nrviews);
    }
    
    /**
      * Test that the node factory correctly determines whether there
      * is a node dialog
      *
      * @since 07-Sep-2022
      * @since v1.36.0
      * @since 07-Sep-2022
      * @since v1.36.0
      */
    @Test
    public void testHasDialog() {
        
        boolean hasDialog = hasDialog();
        
        assertEquals(true, hasDialog);
    }
    
    
}
