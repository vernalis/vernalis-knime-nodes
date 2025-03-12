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
  * Test class for the {@link TarBigNumericValues} class
  *
  * @author s.roughley
  *
  * @since 1.38.0
  * 
  */
public class TarBigNumericValuesTest {
    
    /**
     * Test the enum has the correct number of values
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testTarBigNumericValuesSize() throws Exception {
    
        assertEquals(3, TarBigNumericValues.values().length);
    }


    /**
     * Test the {@link TarBigNumericValues#Error} {@link TarBigNumericValues#getActionCommand()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testError_GetActionCommandMethod() throws Exception {
    
        assertEquals("Error", TarBigNumericValues.Error.getActionCommand());
    }


    /**
     * Test the {@link TarBigNumericValues#Star} {@link TarBigNumericValues#getActionCommand()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testStar_GetActionCommandMethod() throws Exception {
    
        assertEquals("Star", TarBigNumericValues.Star.getActionCommand());
    }


    /**
     * Test the {@link TarBigNumericValues#POSIX} {@link TarBigNumericValues#getActionCommand()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testPOSIX_GetActionCommandMethod() throws Exception {
    
        assertEquals("POSIX", TarBigNumericValues.POSIX.getActionCommand());
    }


    /**
     * Test the static method {@link TarBigNumericValues#getDefault()}
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testGetDefaultStaticMethod() throws Exception {
    
        assertEquals(TarBigNumericValues.Error,
                TarBigNumericValues.getDefault());
    }


    /**
     * Test the {@link TarBigNumericValues#Error} {@link TarBigNumericValues#getFlag()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testError_GetFlagMethod() throws Exception {
    
        assertEquals(TarArchiveOutputStream.BIGNUMBER_ERROR,
                TarBigNumericValues.Error.getFlag());
    }


    /**
     * Test the {@link TarBigNumericValues#Star} {@link TarBigNumericValues#getFlag()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testStar_GetFlagMethod() throws Exception {
    
        assertEquals(TarArchiveOutputStream.BIGNUMBER_STAR,
                TarBigNumericValues.Star.getFlag());
    }


    /**
     * Test the {@link TarBigNumericValues#POSIX} {@link TarBigNumericValues#getFlag()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testPOSIX_GetFlagMethod() throws Exception {
    
        assertEquals(TarArchiveOutputStream.BIGNUMBER_POSIX,
                TarBigNumericValues.POSIX.getFlag());
    }


    /**
     * Test the {@link TarBigNumericValues#Error} {@link TarBigNumericValues#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testError_GetTextMethod() throws Exception {
    
        assertEquals("Error", TarBigNumericValues.Error.getText());
    }


    /**
     * Test the {@link TarBigNumericValues#Star} {@link TarBigNumericValues#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testStar_GetTextMethod() throws Exception {
    
        assertEquals("Star", TarBigNumericValues.Star.getText());
    }


    /**
     * Test the {@link TarBigNumericValues#POSIX} {@link TarBigNumericValues#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testPOSIX_GetTextMethod() throws Exception {
    
        assertEquals("POSIX", TarBigNumericValues.POSIX.getText());
    }


    /**
     * Test the {@link TarBigNumericValues#Error} {@link TarBigNumericValues#getToolTip()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testError_GetToolTipMethod() throws Exception {
    
        assertNotNull(TarBigNumericValues.Error.getToolTip());
    }


    /**
     * Test the {@link TarBigNumericValues#Star} {@link TarBigNumericValues#getToolTip()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testStar_GetToolTipMethod() throws Exception {
    
        assertNotNull(TarBigNumericValues.Star.getToolTip());
    }


    /**
     * Test the {@link TarBigNumericValues#POSIX} {@link TarBigNumericValues#getToolTip()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testPOSIX_GetToolTipMethod() throws Exception {
    
        assertNotNull(TarBigNumericValues.POSIX.getToolTip());
    }


    /**
     * Test the {@link TarBigNumericValues#Error} {@link TarBigNumericValues#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testError_IsDefaultMethod() throws Exception {
    
        assertTrue(TarBigNumericValues.Error.isDefault());
    }


    /**
     * Test the {@link TarBigNumericValues#Star} {@link TarBigNumericValues#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testStar_IsDefaultMethod() throws Exception {
    
        assertFalse(TarBigNumericValues.Star.isDefault());
    }


    /**
     * Test the {@link TarBigNumericValues#POSIX} {@link TarBigNumericValues#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testPOSIX_IsDefaultMethod() throws Exception {
    
        assertFalse(TarBigNumericValues.POSIX.isDefault());
    }

    
}
