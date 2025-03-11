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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

/**
  * Test class for the {@link ArchiveFormat} class
  *
  * @author s.roughley
  *
  * @since 1.38.0
  * 
  */
public class ArchiveFormatTest {
    
    /**
     * Test the enum has the correct number of values
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testArchiveFormatSize() throws Exception {
    
        assertEquals(8, ArchiveFormat.values().length);
    }


    /**
     * Test the {@link ArchiveFormat#tar} {@link ArchiveFormat#createOutputStreamOptions()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testtar_CreateOutputStreamOptionsMethod() throws Exception {
    
        assertNotNull(ArchiveFormat.tar.createOutputStreamOptions());
    }


    /**
     * Test the {@link ArchiveFormat#ar} {@link ArchiveFormat#createOutputStreamOptions()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testar_CreateOutputStreamOptionsMethod() throws Exception {
    
        assertNotNull(ArchiveFormat.ar.createOutputStreamOptions());
    }


    /**
     * Test the {@link ArchiveFormat#arj} {@link ArchiveFormat#createOutputStreamOptions()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testarj_CreateOutputStreamOptionsMethod() throws Exception {
    
        assertThrows(UnsupportedOperationException.class,
                () -> ArchiveFormat.arj.createOutputStreamOptions());
    }


    /**
     * Test the {@link ArchiveFormat#cpio} {@link ArchiveFormat#createOutputStreamOptions()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testcpio_CreateOutputStreamOptionsMethod() throws Exception {
    
        assertNotNull(ArchiveFormat.cpio.createOutputStreamOptions());
    }


    /**
     * Test the {@link ArchiveFormat#dump} {@link ArchiveFormat#createOutputStreamOptions()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testdump_CreateOutputStreamOptionsMethod() throws Exception {
    
        assertThrows(UnsupportedOperationException.class,
                () -> ArchiveFormat.dump.createOutputStreamOptions());
    }


    /**
     * Test the {@link ArchiveFormat#jar} {@link ArchiveFormat#createOutputStreamOptions()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testjar_CreateOutputStreamOptionsMethod() throws Exception {
    
        assertNotNull(ArchiveFormat.jar.createOutputStreamOptions());
    }


    /**
     * Test the {@link ArchiveFormat#zip} {@link ArchiveFormat#createOutputStreamOptions()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testzip_CreateOutputStreamOptionsMethod() throws Exception {
    
        assertNotNull(ArchiveFormat.zip.createOutputStreamOptions());
    }


    /**
     * Test the {@link ArchiveFormat#guess} {@link ArchiveFormat#createOutputStreamOptions()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testguess_CreateOutputStreamOptionsMethod() throws Exception {
    
        assertThrows(UnsupportedOperationException.class,
                () -> ArchiveFormat.guess.createOutputStreamOptions());
    }



    /**
     * Test the static method {@link ArchiveFormat#getArchiveFormats()}
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testGetArchiveFormatsStaticMethod() throws Exception {
    
        assertArrayEquals(
                Arrays.stream(ArchiveFormat.values())
                        .filter(fmt -> fmt.supportsArchiving())
                        .toArray(ArchiveFormat[]::new),
                ArchiveFormat.getArchiveFormats());
    }


    /**
     * Test the static method {@link ArchiveFormat#getDefault()}
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testGetDefaultStaticMethod() throws Exception {
    
        assertEquals(ArchiveFormat.tar, ArchiveFormat.getDefault());
    }


    
    /**
     * Test the {@link ArchiveFormat#tar} {@link ArchiveFormat#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testtar_GetTextMethod() throws Exception {
    
        assertEquals("tar", ArchiveFormat.tar.getText());
    }


    /**
     * Test the {@link ArchiveFormat#ar} {@link ArchiveFormat#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testar_GetTextMethod() throws Exception {
    
        assertEquals("ar", ArchiveFormat.ar.getText());
    }


    /**
     * Test the {@link ArchiveFormat#arj} {@link ArchiveFormat#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testarj_GetTextMethod() throws Exception {
    
        assertEquals("arj", ArchiveFormat.arj.getText());
    }


    /**
     * Test the {@link ArchiveFormat#cpio} {@link ArchiveFormat#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testcpio_GetTextMethod() throws Exception {
    
        assertEquals("cpio", ArchiveFormat.cpio.getText());
    }


    /**
     * Test the {@link ArchiveFormat#dump} {@link ArchiveFormat#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testdump_GetTextMethod() throws Exception {
    
        assertEquals("dump", ArchiveFormat.dump.getText());
    }


    /**
     * Test the {@link ArchiveFormat#jar} {@link ArchiveFormat#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testjar_GetTextMethod() throws Exception {
    
        assertEquals("jar", ArchiveFormat.jar.getText());
    }


    /**
     * Test the {@link ArchiveFormat#zip} {@link ArchiveFormat#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testzip_GetTextMethod() throws Exception {
    
        assertEquals("zip", ArchiveFormat.zip.getText());
    }


    /**
     * Test the {@link ArchiveFormat#guess} {@link ArchiveFormat#getText()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testguess_GetTextMethod() throws Exception {
    
        assertEquals("guess", ArchiveFormat.guess.getText());
    }


    

    /**
     * Test the {@link ArchiveFormat#tar} {@link ArchiveFormat#includesCompression()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testtar_IncludesCompressionMethod() throws Exception {
    
        assertFalse(ArchiveFormat.tar.includesCompression());
    }


    /**
     * Test the {@link ArchiveFormat#ar} {@link ArchiveFormat#includesCompression()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testar_IncludesCompressionMethod() throws Exception {
    
        assertFalse(ArchiveFormat.ar.includesCompression());
    }


    /**
     * Test the {@link ArchiveFormat#arj} {@link ArchiveFormat#includesCompression()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testarj_IncludesCompressionMethod() throws Exception {
    
        assertTrue(ArchiveFormat.arj.includesCompression());
    }


    /**
     * Test the {@link ArchiveFormat#cpio} {@link ArchiveFormat#includesCompression()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testcpio_IncludesCompressionMethod() throws Exception {
    
        assertFalse(ArchiveFormat.cpio.includesCompression());
    }


    /**
     * Test the {@link ArchiveFormat#dump} {@link ArchiveFormat#includesCompression()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testdump_IncludesCompressionMethod() throws Exception {
    
        assertTrue(ArchiveFormat.dump.includesCompression());
    }


    /**
     * Test the {@link ArchiveFormat#jar} {@link ArchiveFormat#includesCompression()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testjar_IncludesCompressionMethod() throws Exception {
    
        assertTrue(ArchiveFormat.jar.includesCompression());
    }


    /**
     * Test the {@link ArchiveFormat#zip} {@link ArchiveFormat#includesCompression()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testzip_IncludesCompressionMethod() throws Exception {
    
        assertTrue(ArchiveFormat.zip.includesCompression());
    }


    /**
     * Test the {@link ArchiveFormat#guess} {@link ArchiveFormat#includesCompression()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testguess_IncludesCompressionMethod() throws Exception {
    
        assertTrue(ArchiveFormat.guess.includesCompression());
    }


    /**
     * Test the {@link ArchiveFormat#tar} {@link ArchiveFormat#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testtar_IsDefaultMethod() throws Exception {
    
        assertTrue(ArchiveFormat.tar.isDefault());
    }


    /**
     * Test the {@link ArchiveFormat#ar} {@link ArchiveFormat#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testar_IsDefaultMethod() throws Exception {
    
        assertFalse(ArchiveFormat.ar.isDefault());
    }


    /**
     * Test the {@link ArchiveFormat#arj} {@link ArchiveFormat#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testarj_IsDefaultMethod() throws Exception {
    
        assertFalse(ArchiveFormat.arj.isDefault());
    }


    /**
     * Test the {@link ArchiveFormat#cpio} {@link ArchiveFormat#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testcpio_IsDefaultMethod() throws Exception {
    
        assertFalse(ArchiveFormat.cpio.isDefault());
    }


    /**
     * Test the {@link ArchiveFormat#dump} {@link ArchiveFormat#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testdump_IsDefaultMethod() throws Exception {
    
        assertFalse(ArchiveFormat.dump.isDefault());
    }


    /**
     * Test the {@link ArchiveFormat#jar} {@link ArchiveFormat#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testjar_IsDefaultMethod() throws Exception {
    
        assertFalse(ArchiveFormat.jar.isDefault());
    }


    /**
     * Test the {@link ArchiveFormat#zip} {@link ArchiveFormat#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testzip_IsDefaultMethod() throws Exception {
    
        assertFalse(ArchiveFormat.zip.isDefault());
    }


    /**
     * Test the {@link ArchiveFormat#guess} {@link ArchiveFormat#isDefault()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testguess_IsDefaultMethod() throws Exception {
    
        assertFalse(ArchiveFormat.guess.isDefault());
    }


    /**
     * Test the {@link ArchiveFormat#tar} {@link ArchiveFormat#supportsArchiving()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testtar_SupportsArchivingMethod() throws Exception {
    
        assertTrue(ArchiveFormat.tar.supportsArchiving());
    }


    /**
     * Test the {@link ArchiveFormat#ar} {@link ArchiveFormat#supportsArchiving()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testar_SupportsArchivingMethod() throws Exception {
    
        assertTrue(ArchiveFormat.ar.supportsArchiving());
    }


    /**
     * Test the {@link ArchiveFormat#arj} {@link ArchiveFormat#supportsArchiving()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testarj_SupportsArchivingMethod() throws Exception {
    
        assertFalse(ArchiveFormat.arj.supportsArchiving());
    }


    /**
     * Test the {@link ArchiveFormat#cpio} {@link ArchiveFormat#supportsArchiving()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testcpio_SupportsArchivingMethod() throws Exception {
    
        assertTrue(ArchiveFormat.cpio.supportsArchiving());
    }


    /**
     * Test the {@link ArchiveFormat#dump} {@link ArchiveFormat#supportsArchiving()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testdump_SupportsArchivingMethod() throws Exception {
    
        assertFalse(ArchiveFormat.dump.supportsArchiving());
    }


    /**
     * Test the {@link ArchiveFormat#jar} {@link ArchiveFormat#supportsArchiving()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testjar_SupportsArchivingMethod() throws Exception {
    
        assertTrue(ArchiveFormat.jar.supportsArchiving());
    }


    /**
     * Test the {@link ArchiveFormat#zip} {@link ArchiveFormat#supportsArchiving()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testzip_SupportsArchivingMethod() throws Exception {
    
        assertTrue(ArchiveFormat.zip.supportsArchiving());
    }


    /**
     * Test the {@link ArchiveFormat#guess} {@link ArchiveFormat#supportsArchiving()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testguess_SupportsArchivingMethod() throws Exception {
    
        assertFalse(ArchiveFormat.guess.supportsArchiving());
    }


    /**
     * Test the {@link ArchiveFormat#tar} {@link ArchiveFormat#supportsDirectories()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testtar_SupportsDirectoriesMethod() throws Exception {
    
        assertTrue(ArchiveFormat.tar.supportsDirectories());
    }


    /**
     * Test the {@link ArchiveFormat#ar} {@link ArchiveFormat#supportsDirectories()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testar_SupportsDirectoriesMethod() throws Exception {
    
        assertFalse(ArchiveFormat.ar.supportsDirectories());
    }


    /**
     * Test the {@link ArchiveFormat#arj} {@link ArchiveFormat#supportsDirectories()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testarj_SupportsDirectoriesMethod() throws Exception {
    
        assertTrue(ArchiveFormat.arj.supportsDirectories());
    }


    /**
     * Test the {@link ArchiveFormat#cpio} {@link ArchiveFormat#supportsDirectories()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testcpio_SupportsDirectoriesMethod() throws Exception {
    
        assertTrue(ArchiveFormat.cpio.supportsDirectories());
    }


    /**
     * Test the {@link ArchiveFormat#dump} {@link ArchiveFormat#supportsDirectories()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testdump_SupportsDirectoriesMethod() throws Exception {
    
        assertTrue(ArchiveFormat.dump.supportsDirectories());
    }


    /**
     * Test the {@link ArchiveFormat#jar} {@link ArchiveFormat#supportsDirectories()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testjar_SupportsDirectoriesMethod() throws Exception {
    
        assertTrue(ArchiveFormat.jar.supportsDirectories());
    }


    /**
     * Test the {@link ArchiveFormat#zip} {@link ArchiveFormat#supportsDirectories()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testzip_SupportsDirectoriesMethod() throws Exception {
    
        assertTrue(ArchiveFormat.zip.supportsDirectories());
    }


    /**
     * Test the {@link ArchiveFormat#guess} {@link ArchiveFormat#supportsDirectories()} method
     * 
     * @throws Exception if an error occurred during test execution
     * 
     * @since 1.38.0
     */
    @Test
    public void testguess_SupportsDirectoriesMethod() throws Exception {
    
        assertTrue(ArchiveFormat.guess.supportsDirectories());
    }




    
}
