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
package com.vernalis.knime.misc.blobs.nodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
  * Test class for the {@link LastModifiedDateTimeOptions} class
  *
  * @author s.roughley
  *
  * @since 1.38.0
  * 
  */
public class LastModifiedDateTimeOptionsTest {
    
    /**
     * Test the enum has the correct number of values
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testLastModifiedDateTimeOptionsSize() throws Exception {
    
        assertEquals(4, LastModifiedDateTimeOptions.values().length);
    }


    /**
     * Test the {@link LastModifiedDateTimeOptions#Runtime} {@link LastModifiedDateTimeOptions#getActionCommand()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testRuntime_GetActionCommandMethod() throws Exception {
    
        assertEquals("Runtime",
                LastModifiedDateTimeOptions.Runtime.getActionCommand());
    }


    /**
     * Test the {@link LastModifiedDateTimeOptions#Column} {@link LastModifiedDateTimeOptions#getActionCommand()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testColumn_GetActionCommandMethod() throws Exception {
    
        assertEquals("Column",
                LastModifiedDateTimeOptions.Column.getActionCommand());
    }


    /**
     * Test the {@link LastModifiedDateTimeOptions#Fixed_Time} {@link LastModifiedDateTimeOptions#getActionCommand()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testFixed_Time_GetActionCommandMethod() throws Exception {
    
        assertEquals("Fixed_Time",
                LastModifiedDateTimeOptions.Fixed_Time.getActionCommand());
    }


    /**
     * Test the {@link LastModifiedDateTimeOptions#None} {@link LastModifiedDateTimeOptions#getActionCommand()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testNone_GetActionCommandMethod() throws Exception {
    
        assertEquals("None",
                LastModifiedDateTimeOptions.None.getActionCommand());
    }


    /**
     * Test the static method {@link LastModifiedDateTimeOptions#getDefault()}
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testGetDefaultStaticMethod() throws Exception {
    
        assertEquals(LastModifiedDateTimeOptions.Runtime,
                LastModifiedDateTimeOptions.getDefault());
    }


    /**
     * Test the {@link LastModifiedDateTimeOptions#Runtime} {@link LastModifiedDateTimeOptions#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testRuntime_GetTextMethod() throws Exception {
    
        assertEquals("Runtime", LastModifiedDateTimeOptions.Runtime.getText());
    }


    /**
     * Test the {@link LastModifiedDateTimeOptions#Column} {@link LastModifiedDateTimeOptions#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testColumn_GetTextMethod() throws Exception {
    
        assertEquals("Column", LastModifiedDateTimeOptions.Column.getText());
    }


    /**
     * Test the {@link LastModifiedDateTimeOptions#Fixed_Time} {@link LastModifiedDateTimeOptions#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testFixed_Time_GetTextMethod() throws Exception {
    
        assertEquals("Fixed Time",
                LastModifiedDateTimeOptions.Fixed_Time.getText());
    }


    /**
     * Test the {@link LastModifiedDateTimeOptions#None} {@link LastModifiedDateTimeOptions#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testNone_GetTextMethod() throws Exception {
    
        assertEquals("None", LastModifiedDateTimeOptions.None.getText());
    }


    /**
     * Test the {@link LastModifiedDateTimeOptions#Runtime} {@link LastModifiedDateTimeOptions#getToolTip()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testRuntime_GetToolTipMethod() throws Exception {
    
        assertNull(LastModifiedDateTimeOptions.Runtime.getToolTip());
    }


    /**
     * Test the {@link LastModifiedDateTimeOptions#Column} {@link LastModifiedDateTimeOptions#getToolTip()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testColumn_GetToolTipMethod() throws Exception {
    
        assertNull(LastModifiedDateTimeOptions.Column.getToolTip());
    }


    /**
     * Test the {@link LastModifiedDateTimeOptions#Fixed_Time} {@link LastModifiedDateTimeOptions#getToolTip()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testFixed_Time_GetToolTipMethod() throws Exception {
    
        assertNull(LastModifiedDateTimeOptions.Fixed_Time.getToolTip());
    }


    /**
     * Test the {@link LastModifiedDateTimeOptions#None} {@link LastModifiedDateTimeOptions#getToolTip()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testNone_GetToolTipMethod() throws Exception {
    
        assertNull(LastModifiedDateTimeOptions.None.getToolTip());
    }


    /**
     * Test the {@link LastModifiedDateTimeOptions#Runtime} {@link LastModifiedDateTimeOptions#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testRuntime_IsDefaultMethod() throws Exception {
    
        assertTrue(LastModifiedDateTimeOptions.Runtime.isDefault());
    }


    /**
     * Test the {@link LastModifiedDateTimeOptions#Column} {@link LastModifiedDateTimeOptions#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testColumn_IsDefaultMethod() throws Exception {
    
        assertFalse(LastModifiedDateTimeOptions.Column.isDefault());
    }


    /**
     * Test the {@link LastModifiedDateTimeOptions#Fixed_Time} {@link LastModifiedDateTimeOptions#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testFixed_Time_IsDefaultMethod() throws Exception {
    
        assertFalse(LastModifiedDateTimeOptions.Fixed_Time.isDefault());
    }


    /**
     * Test the {@link LastModifiedDateTimeOptions#None} {@link LastModifiedDateTimeOptions#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testNone_IsDefaultMethod() throws Exception {
    
        assertFalse(LastModifiedDateTimeOptions.None.isDefault());
    }

    
}
