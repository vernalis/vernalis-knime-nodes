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
package com.vernalis.knime.misc.blobs.nodes.base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
  * Test class for the {@link ConversionFailureBehaviour} class
  *
  * @author s.roughley
  *
  * @since 1.38.0
  * 
  */
public class ConversionFailureBehaviourTest {
    
    /**
     * Test the enum has the correct number of values
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testConversionFailureBehaviourSize() throws Exception {
    
        assertEquals(2, ConversionFailureBehaviour.values().length);
    }


    /**
     * Test the {@link ConversionFailureBehaviour#Fail} {@link ConversionFailureBehaviour#getActionCommand()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testFail_GetActionCommandMethod() throws Exception {
    
        assertEquals("Fail",
                ConversionFailureBehaviour.Fail.getActionCommand());
    }


    /**
     * Test the {@link ConversionFailureBehaviour#Skip} {@link ConversionFailureBehaviour#getActionCommand()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testSkip_GetActionCommandMethod() throws Exception {
    
        assertEquals("Skip",
                ConversionFailureBehaviour.Skip.getActionCommand());
    }


    /**
     * Test the static method {@link ConversionFailureBehaviour#getDefault()}
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testGetDefaultStaticMethod() throws Exception {
    
        assertEquals(ConversionFailureBehaviour.Fail,
                ConversionFailureBehaviour.getDefault());
    }


    /**
     * Test the {@link ConversionFailureBehaviour#Fail} {@link ConversionFailureBehaviour#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testFail_GetTextMethod() throws Exception {
    
        assertEquals("Fail", ConversionFailureBehaviour.Fail.getText());
    }


    /**
     * Test the {@link ConversionFailureBehaviour#Skip} {@link ConversionFailureBehaviour#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testSkip_GetTextMethod() throws Exception {
    
        assertEquals("Skip", ConversionFailureBehaviour.Skip.getText());
    }


    /**
     * Test the {@link ConversionFailureBehaviour#Fail} {@link ConversionFailureBehaviour#getToolTip()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testFail_GetToolTipMethod() throws Exception {
    
        assertNull(ConversionFailureBehaviour.Fail.getToolTip());
    }


    /**
     * Test the {@link ConversionFailureBehaviour#Skip} {@link ConversionFailureBehaviour#getToolTip()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testSkip_GetToolTipMethod() throws Exception {
    
        assertNull(ConversionFailureBehaviour.Skip.getToolTip());
    }


    /**
     * Test the {@link ConversionFailureBehaviour#Fail} {@link ConversionFailureBehaviour#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testFail_IsDefaultMethod() throws Exception {
    
        assertTrue(ConversionFailureBehaviour.Fail.isDefault());
    }


    /**
     * Test the {@link ConversionFailureBehaviour#Skip} {@link ConversionFailureBehaviour#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testSkip_IsDefaultMethod() throws Exception {
    
        assertFalse(ConversionFailureBehaviour.Skip.isDefault());
    }

    
}
