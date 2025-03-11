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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.Test;

/**
  * Test class for the {@link TarLongFileMode} class
  *
  * @author s.roughley
  *
  * @since 1.38.0
  * 
  */
public class TarLongFileModeTest {
    
    /**
     * Test the enum has the correct number of values
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testTarLongFileModeSize() throws Exception {
    
        assertEquals(4, TarLongFileMode.values().length);
    }


    /**
     * Test the {@link TarLongFileMode#Error} {@link TarLongFileMode#getActionCommand()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testError_GetActionCommandMethod() throws Exception {
    
        assertEquals("Error", TarLongFileMode.Error.getActionCommand());
    }


    /**
     * Test the {@link TarLongFileMode#Truncate} {@link TarLongFileMode#getActionCommand()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testTruncate_GetActionCommandMethod() throws Exception {
    
        assertEquals("Truncate", TarLongFileMode.Truncate.getActionCommand());
    }


    /**
     * Test the {@link TarLongFileMode#GNU} {@link TarLongFileMode#getActionCommand()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testGNU_GetActionCommandMethod() throws Exception {
    
        assertEquals("GNU", TarLongFileMode.GNU.getActionCommand());
    }


    /**
     * Test the {@link TarLongFileMode#POSIX} {@link TarLongFileMode#getActionCommand()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testPOSIX_GetActionCommandMethod() throws Exception {
    
        assertEquals("POSIX", TarLongFileMode.POSIX.getActionCommand());
    }


    /**
     * Test the static method {@link TarLongFileMode#getDefault()}
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testGetDefaultStaticMethod() throws Exception {
    
        assertEquals(TarLongFileMode.Error, TarLongFileMode.getDefault());
    }


    /**
     * Test the {@link TarLongFileMode#Error} {@link TarLongFileMode#getFlag()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testError_GetFlagMethod() throws Exception {
    
        assertEquals(TarArchiveOutputStream.LONGFILE_ERROR,
                TarLongFileMode.Error.getFlag());
    }


    /**
     * Test the {@link TarLongFileMode#Truncate} {@link TarLongFileMode#getFlag()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testTruncate_GetFlagMethod() throws Exception {
    
        assertEquals(TarArchiveOutputStream.LONGFILE_TRUNCATE,
                TarLongFileMode.Truncate.getFlag());
    }


    /**
     * Test the {@link TarLongFileMode#GNU} {@link TarLongFileMode#getFlag()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testGNU_GetFlagMethod() throws Exception {
    
        assertEquals(TarArchiveOutputStream.LONGFILE_GNU,
                TarLongFileMode.GNU.getFlag());
    }


    /**
     * Test the {@link TarLongFileMode#POSIX} {@link TarLongFileMode#getFlag()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testPOSIX_GetFlagMethod() throws Exception {
    
        assertEquals(TarArchiveOutputStream.LONGFILE_POSIX,
                TarLongFileMode.POSIX.getFlag());
    }


    /**
     * Test the {@link TarLongFileMode#Error} {@link TarLongFileMode#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testError_GetTextMethod() throws Exception {
    
        assertEquals("Error", TarLongFileMode.Error.getText());
    }


    /**
     * Test the {@link TarLongFileMode#Truncate} {@link TarLongFileMode#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testTruncate_GetTextMethod() throws Exception {
    
        assertEquals("Truncate", TarLongFileMode.Truncate.getText());
    }


    /**
     * Test the {@link TarLongFileMode#GNU} {@link TarLongFileMode#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testGNU_GetTextMethod() throws Exception {
    
        assertEquals("GNU", TarLongFileMode.GNU.getText());
    }


    /**
     * Test the {@link TarLongFileMode#POSIX} {@link TarLongFileMode#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testPOSIX_GetTextMethod() throws Exception {
    
        assertEquals("POSIX", TarLongFileMode.POSIX.getText());
    }


    /**
     * Test the {@link TarLongFileMode#Error} {@link TarLongFileMode#getToolTip()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testError_GetToolTipMethod() throws Exception {
    
        assertNotNull(TarLongFileMode.Error.getToolTip());
    }


    /**
     * Test the {@link TarLongFileMode#Truncate} {@link TarLongFileMode#getToolTip()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testTruncate_GetToolTipMethod() throws Exception {
    
        assertNotNull(TarLongFileMode.Truncate.getToolTip());
    }


    /**
     * Test the {@link TarLongFileMode#GNU} {@link TarLongFileMode#getToolTip()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testGNU_GetToolTipMethod() throws Exception {
    
        assertNotNull(TarLongFileMode.GNU.getToolTip());
    }


    /**
     * Test the {@link TarLongFileMode#POSIX} {@link TarLongFileMode#getToolTip()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testPOSIX_GetToolTipMethod() throws Exception {
    
        assertNotNull(TarLongFileMode.POSIX.getToolTip());
    }


    /**
     * Test the {@link TarLongFileMode#Error} {@link TarLongFileMode#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testError_IsDefaultMethod() throws Exception {
    
        assertTrue(TarLongFileMode.Error.isDefault());
    }


    /**
     * Test the {@link TarLongFileMode#Truncate} {@link TarLongFileMode#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testTruncate_IsDefaultMethod() throws Exception {
    
        assertFalse(TarLongFileMode.Truncate.isDefault());
    }


    /**
     * Test the {@link TarLongFileMode#GNU} {@link TarLongFileMode#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testGNU_IsDefaultMethod() throws Exception {
    
        assertFalse(TarLongFileMode.GNU.isDefault());
    }


    /**
     * Test the {@link TarLongFileMode#POSIX} {@link TarLongFileMode#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testPOSIX_IsDefaultMethod() throws Exception {
    
        assertFalse(TarLongFileMode.POSIX.isDefault());
    }

    
}
