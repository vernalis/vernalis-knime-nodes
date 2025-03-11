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
package com.vernalis.knime.misc.blobs.nodes.compress;

import static com.vernalis.knime.misc.blobs.nodes.compress.CompressorCapabilities.Comment;
import static com.vernalis.knime.misc.blobs.nodes.compress.CompressorCapabilities.Compress;
import static com.vernalis.knime.misc.blobs.nodes.compress.CompressorCapabilities.Concatenation;
import static com.vernalis.knime.misc.blobs.nodes.compress.CompressorCapabilities.Expand;
import static com.vernalis.knime.misc.blobs.nodes.compress.CompressorCapabilities.Filename;
import static com.vernalis.knime.misc.blobs.nodes.compress.CompressorCapabilities.Timestamp;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import org.apache.commons.compress.compressors.brotli.BrotliUtils;
import org.apache.commons.compress.compressors.lzma.LZMAUtils;
import org.apache.commons.compress.compressors.xz.XZUtils;
import org.apache.commons.compress.compressors.zstandard.ZstdUtils;
import org.junit.Test;

/**
 * Test class for the {@link CompressFormat} class
 *
 * @author s.roughley
 * @since 1.38.0
 * 
 */
public class CompressFormatTest {

    /**
     * Test the enum has the correct number of values
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testCompressFormatSize() throws Exception {

        assertEquals(14, CompressFormat.values().length);
    }

    /**
     * Test the {@link CompressFormat#Brotli}
     * {@link CompressFormat#getActionCommand()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testBrotli_GetActionCommandMethod() throws Exception {

        assertEquals("Brotli", CompressFormat.Brotli.getActionCommand());
    }

    /**
     * Test the {@link CompressFormat#bzip2}
     * {@link CompressFormat#getActionCommand()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testbzip2_GetActionCommandMethod() throws Exception {

        assertEquals("bzip2", CompressFormat.bzip2.getActionCommand());
    }

    /**
     * Test the {@link CompressFormat#deflate}
     * {@link CompressFormat#getActionCommand()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testdeflate_GetActionCommandMethod() throws Exception {

        assertEquals("deflate", CompressFormat.deflate.getActionCommand());
    }

    /**
     * Test the {@link CompressFormat#deflate64}
     * {@link CompressFormat#getActionCommand()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testdeflate64_GetActionCommandMethod() throws Exception {

        assertEquals("deflate64", CompressFormat.deflate64.getActionCommand());
    }

    /**
     * Test the {@link CompressFormat#gzip}
     * {@link CompressFormat#getActionCommand()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testgzip_GetActionCommandMethod() throws Exception {

        assertEquals("gzip", CompressFormat.gzip.getActionCommand());
    }

    /**
     * Test the {@link CompressFormat#LZ4_Block}
     * {@link CompressFormat#getActionCommand()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testLZ4_Block_GetActionCommandMethod() throws Exception {

        assertEquals("LZ4_Block", CompressFormat.LZ4_Block.getActionCommand());
    }

    /**
     * Test the {@link CompressFormat#LZ4_Framed}
     * {@link CompressFormat#getActionCommand()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testLZ4_Framed_GetActionCommandMethod() throws Exception {

        assertEquals("LZ4_Framed",
                CompressFormat.LZ4_Framed.getActionCommand());
    }

    /**
     * Test the {@link CompressFormat#LZMA}
     * {@link CompressFormat#getActionCommand()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testLZMA_GetActionCommandMethod() throws Exception {

        assertEquals("LZMA", CompressFormat.LZMA.getActionCommand());
    }

    /**
     * Test the {@link CompressFormat#Snappy_Framed}
     * {@link CompressFormat#getActionCommand()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testSnappy_Framed_GetActionCommandMethod() throws Exception {

        assertEquals("Snappy_Framed",
                CompressFormat.Snappy_Framed.getActionCommand());
    }

    /**
     * Test the {@link CompressFormat#Snappy}
     * {@link CompressFormat#getActionCommand()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testSnappy_GetActionCommandMethod() throws Exception {

        assertEquals("Snappy",
                CompressFormat.Snappy.getActionCommand());
    }

    /**
     * Test the {@link CompressFormat#xz}
     * {@link CompressFormat#getActionCommand()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testxz_GetActionCommandMethod() throws Exception {

        assertEquals("xz", CompressFormat.xz.getActionCommand());
    }

    /**
     * Test the {@link CompressFormat#z}
     * {@link CompressFormat#getActionCommand()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testz_GetActionCommandMethod() throws Exception {

        assertEquals("z", CompressFormat.z.getActionCommand());
    }

    /**
     * Test the {@link CompressFormat#ZStandard}
     * {@link CompressFormat#getActionCommand()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testZStandard_GetActionCommandMethod() throws Exception {

        assertEquals("ZStandard", CompressFormat.ZStandard.getActionCommand());
    }

    /**
     * Test the {@link CompressFormat#Guess}
     * {@link CompressFormat#getActionCommand()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testGuess_GetActionCommandMethod() throws Exception {

        assertEquals("Guess", CompressFormat.Guess.getActionCommand());
    }

    /**
     * Test the {@link CompressFormat#Brotli}
     * {@link CompressFormat#getAdditionalDecompressionOutputColumnSpecs()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testBrotli_GetAdditionalDecompressionOutputColumnSpecsMethod()
            throws Exception {

        assertEquals(Collections.emptyList(), CompressFormat.Brotli
                .getAdditionalDecompressionOutputColumnSpecs());
    }

    /**
     * Test the {@link CompressFormat#bzip2}
     * {@link CompressFormat#getAdditionalDecompressionOutputColumnSpecs()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testbzip2_GetAdditionalDecompressionOutputColumnSpecsMethod()
            throws Exception {

        assertEquals(Collections.emptyList(), CompressFormat.bzip2
                .getAdditionalDecompressionOutputColumnSpecs());
    }

    /**
     * Test the {@link CompressFormat#deflate}
     * {@link CompressFormat#getAdditionalDecompressionOutputColumnSpecs()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testdeflate_GetAdditionalDecompressionOutputColumnSpecsMethod()
            throws Exception {

        assertEquals(Collections.emptyList(), CompressFormat.deflate
                .getAdditionalDecompressionOutputColumnSpecs());
    }

    /**
     * Test the {@link CompressFormat#deflate64}
     * {@link CompressFormat#getAdditionalDecompressionOutputColumnSpecs()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testdeflate64_GetAdditionalDecompressionOutputColumnSpecsMethod()
            throws Exception {

        assertEquals(Collections.emptyList(), CompressFormat.deflate64
                .getAdditionalDecompressionOutputColumnSpecs());
    }

    /**
     * Test the {@link CompressFormat#gzip}
     * {@link CompressFormat#getAdditionalDecompressionOutputColumnSpecs()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testgzip_GetAdditionalDecompressionOutputColumnSpecsMethod()
            throws Exception {

        assertEquals(4, CompressFormat.gzip
                .getAdditionalDecompressionOutputColumnSpecs().size());
    }

    /**
     * Test the {@link CompressFormat#LZ4_Block}
     * {@link CompressFormat#getAdditionalDecompressionOutputColumnSpecs()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testLZ4_Block_GetAdditionalDecompressionOutputColumnSpecsMethod()
            throws Exception {

        assertEquals(Collections.emptyList(), CompressFormat.LZ4_Block
                .getAdditionalDecompressionOutputColumnSpecs());
    }

    /**
     * Test the {@link CompressFormat#LZ4_Framed}
     * {@link CompressFormat#getAdditionalDecompressionOutputColumnSpecs()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testLZ4_Framed_GetAdditionalDecompressionOutputColumnSpecsMethod()
            throws Exception {

        assertEquals(Collections.emptyList(), CompressFormat.LZ4_Framed
                .getAdditionalDecompressionOutputColumnSpecs());
    }

    /**
     * Test the {@link CompressFormat#LZMA}
     * {@link CompressFormat#getAdditionalDecompressionOutputColumnSpecs()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testLZMA_GetAdditionalDecompressionOutputColumnSpecsMethod()
            throws Exception {

        assertEquals(Collections.emptyList(), CompressFormat.LZMA
                .getAdditionalDecompressionOutputColumnSpecs());
    }

    /**
     * Test the {@link CompressFormat#Snappy_Framed}
     * {@link CompressFormat#getAdditionalDecompressionOutputColumnSpecs()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testSnappy_Framed_GetAdditionalDecompressionOutputColumnSpecsMethod()
            throws Exception {

        assertEquals(Collections.emptyList(), CompressFormat.Snappy_Framed
                .getAdditionalDecompressionOutputColumnSpecs());
    }

    /**
     * Test the {@link CompressFormat#Snappy}
     * {@link CompressFormat#getAdditionalDecompressionOutputColumnSpecs()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testSnappy_GetAdditionalDecompressionOutputColumnSpecsMethod()
            throws Exception {

        assertEquals(Collections.emptyList(), CompressFormat.Snappy
                .getAdditionalDecompressionOutputColumnSpecs());
    }

    /**
     * Test the {@link CompressFormat#xz}
     * {@link CompressFormat#getAdditionalDecompressionOutputColumnSpecs()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testxz_GetAdditionalDecompressionOutputColumnSpecsMethod()
            throws Exception {

        assertEquals(Collections.emptyList(), CompressFormat.xz
                .getAdditionalDecompressionOutputColumnSpecs());
    }

    /**
     * Test the {@link CompressFormat#z}
     * {@link CompressFormat#getAdditionalDecompressionOutputColumnSpecs()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testz_GetAdditionalDecompressionOutputColumnSpecsMethod()
            throws Exception {

        assertEquals(Collections.emptyList(),
                CompressFormat.z.getAdditionalDecompressionOutputColumnSpecs());
    }

    /**
     * Test the {@link CompressFormat#ZStandard}
     * {@link CompressFormat#getAdditionalDecompressionOutputColumnSpecs()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testZStandard_GetAdditionalDecompressionOutputColumnSpecsMethod()
            throws Exception {

        assertEquals(Collections.emptyList(), CompressFormat.ZStandard
                .getAdditionalDecompressionOutputColumnSpecs());
    }

    /**
     * Test the {@link CompressFormat#Guess}
     * {@link CompressFormat#getAdditionalDecompressionOutputColumnSpecs()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testGuess_GetAdditionalDecompressionOutputColumnSpecsMethod()
            throws Exception {

        assertEquals(1, CompressFormat.Guess
                .getAdditionalDecompressionOutputColumnSpecs().size());
    }

    /**
     * Test the {@link CompressFormat#Brotli}
     * {@link CompressFormat#getCapabilities()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testBrotli_GetCapabilitiesMethod() throws Exception {

        assertEquals(
                BrotliUtils.isBrotliCompressionAvailable()
                        ? Collections.singleton(Expand)
                        : Collections.emptySet(),
                CompressFormat.Brotli.getCapabilities());
    }

    /**
     * Test the {@link CompressFormat#bzip2}
     * {@link CompressFormat#getCapabilities()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testbzip2_GetCapabilitiesMethod() throws Exception {

        assertEquals(
                EnumSet.of(CompressorCapabilities.Compress,
                        CompressorCapabilities.Expand,
                        CompressorCapabilities.Concatenation),
                CompressFormat.bzip2.getCapabilities());
    }

    /**
     * Test the {@link CompressFormat#deflate}
     * {@link CompressFormat#getCapabilities()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testdeflate_GetCapabilitiesMethod() throws Exception {

        assertEquals(EnumSet.of(Compress, Expand),
                CompressFormat.deflate.getCapabilities());
    }

    /**
     * Test the {@link CompressFormat#deflate64}
     * {@link CompressFormat#getCapabilities()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testdeflate64_GetCapabilitiesMethod() throws Exception {

        assertEquals(EnumSet.of(Expand),
                CompressFormat.deflate64.getCapabilities());
    }

    /**
     * Test the {@link CompressFormat#gzip}
     * {@link CompressFormat#getCapabilities()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testgzip_GetCapabilitiesMethod() throws Exception {

        assertEquals(EnumSet.of(Compress, Expand, Concatenation, Comment,
                Filename, Timestamp), CompressFormat.gzip.getCapabilities());
    }

    /**
     * Test the {@link CompressFormat#LZ4_Block}
     * {@link CompressFormat#getCapabilities()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testLZ4_Block_GetCapabilitiesMethod() throws Exception {

        assertEquals(EnumSet.of(Compress, Expand),
                CompressFormat.LZ4_Block.getCapabilities());
    }

    /**
     * Test the {@link CompressFormat#LZ4_Framed}
     * {@link CompressFormat#getCapabilities()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testLZ4_Framed_GetCapabilitiesMethod() throws Exception {

        assertEquals(EnumSet.of(Compress, Expand, Concatenation),
                CompressFormat.LZ4_Framed.getCapabilities());
    }

    /**
     * Test the {@link CompressFormat#LZMA}
     * {@link CompressFormat#getCapabilities()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testLZMA_GetCapabilitiesMethod() throws Exception {

        assertEquals(
                LZMAUtils.isLZMACompressionAvailable()
                        ? EnumSet.of(Expand, Compress)
                        : Collections.emptySet(),
                CompressFormat.LZMA.getCapabilities());
    }

    /**
     * Test the {@link CompressFormat#Snappy_Framed}
     * {@link CompressFormat#getCapabilities()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testSnappy_Framed_GetCapabilitiesMethod() throws Exception {

        assertEquals(EnumSet.of(Compress, Expand),
                CompressFormat.Snappy_Framed.getCapabilities());
    }

    /**
     * Test the {@link CompressFormat#Snappy}
     * {@link CompressFormat#getCapabilities()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testSnappy_GetCapabilitiesMethod() throws Exception {

        assertEquals(EnumSet.of(Compress, Expand),
                CompressFormat.Snappy.getCapabilities());
    }

    /**
     * Test the {@link CompressFormat#xz}
     * {@link CompressFormat#getCapabilities()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testxz_GetCapabilitiesMethod() throws Exception {

        assertEquals(
                XZUtils.isXZCompressionAvailable()
                        ? EnumSet.of(Compress, Expand, Concatenation)
                        : Collections.emptySet(),
                CompressFormat.xz.getCapabilities());
    }

    /**
     * Test the {@link CompressFormat#z}
     * {@link CompressFormat#getCapabilities()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testz_GetCapabilitiesMethod() throws Exception {

        assertEquals(EnumSet.of(Expand), CompressFormat.z.getCapabilities());
    }

    /**
     * Test the {@link CompressFormat#ZStandard}
     * {@link CompressFormat#getCapabilities()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testZStandard_GetCapabilitiesMethod() throws Exception {

        assertEquals(
                ZstdUtils.isZstdCompressionAvailable()
                        ? EnumSet.of(Compress, Expand)
                        : Collections.emptySet(),
                CompressFormat.ZStandard.getCapabilities());
    }

    /**
     * Test the {@link CompressFormat#Guess}
     * {@link CompressFormat#getCapabilities()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testGuess_GetCapabilitiesMethod() throws Exception {

        assertEquals(EnumSet.of(Expand),
                CompressFormat.Guess.getCapabilities());
    }

    /**
     * Test the static method {@link CompressFormat#getCompressionFormats()}
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testGetCompressionFormatsStaticMethod() throws Exception {

        assertArrayEquals(
                Arrays.stream(CompressFormat.values())
                        .filter(cf -> cf.supportsCompression())
                        .toArray(CompressFormat[]::new),
                CompressFormat.getCompressionFormats());
    }

    /**
     * Test the static method {@link CompressFormat#getDecompressionFormats()}
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testGetDecompressionFormatsStaticMethod() throws Exception {

        assertArrayEquals(
                Arrays.stream(CompressFormat.values())
                        .filter(cf -> cf.supportsDecompression())
                        .toArray(CompressFormat[]::new),
                CompressFormat.getDecompressionFormats());
    }

    /**
     * Test the static method {@link CompressFormat#getDefault()}
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testGetDefaultStaticMethod() throws Exception {

        assertEquals(Arrays.stream(CompressFormat.values()).filter(
                fmt -> fmt.supportsCompression() && fmt.supportsDecompression())
                .findFirst().orElseThrow(), CompressFormat.getDefault());
    }

    /**
     * Test the {@link CompressFormat#Brotli} {@link CompressFormat#getText()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testBrotli_GetTextMethod() throws Exception {

        assertEquals("Brotli", CompressFormat.Brotli.getText());
    }

    /**
     * Test the {@link CompressFormat#bzip2} {@link CompressFormat#getText()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testbzip2_GetTextMethod() throws Exception {

        assertEquals("bzip2", CompressFormat.bzip2.getText());
    }

    /**
     * Test the {@link CompressFormat#deflate} {@link CompressFormat#getText()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testdeflate_GetTextMethod() throws Exception {

        assertEquals("deflate", CompressFormat.deflate.getText());
    }

    /**
     * Test the {@link CompressFormat#deflate64}
     * {@link CompressFormat#getText()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testdeflate64_GetTextMethod() throws Exception {

        assertEquals("deflate64", CompressFormat.deflate64.getText());
    }

    /**
     * Test the {@link CompressFormat#gzip} {@link CompressFormat#getText()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testgzip_GetTextMethod() throws Exception {

        assertEquals("gzip", CompressFormat.gzip.getText());
    }

    /**
     * Test the {@link CompressFormat#LZ4_Block}
     * {@link CompressFormat#getText()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testLZ4_Block_GetTextMethod() throws Exception {

        assertEquals("LZ4 Block", CompressFormat.LZ4_Block.getText());
    }

    /**
     * Test the {@link CompressFormat#LZ4_Framed}
     * {@link CompressFormat#getText()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testLZ4_Framed_GetTextMethod() throws Exception {

        assertEquals("LZ4 Framed", CompressFormat.LZ4_Framed.getText());
    }

    /**
     * Test the {@link CompressFormat#LZMA} {@link CompressFormat#getText()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testLZMA_GetTextMethod() throws Exception {

        assertEquals("LZMA", CompressFormat.LZMA.getText());
    }

    /**
     * Test the {@link CompressFormat#Snappy_Framed}
     * {@link CompressFormat#getText()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testSnappy_Framed_GetTextMethod() throws Exception {

        assertEquals("Snappy Framed", CompressFormat.Snappy_Framed.getText());
    }

    /**
     * Test the {@link CompressFormat#Snappy} {@link CompressFormat#getText()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testSnappy_GetTextMethod() throws Exception {

        assertEquals("Snappy", CompressFormat.Snappy.getText());
    }

    /**
     * Test the {@link CompressFormat#xz} {@link CompressFormat#getText()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testxz_GetTextMethod() throws Exception {

        assertEquals("xz", CompressFormat.xz.getText());
    }

    /**
     * Test the {@link CompressFormat#z} {@link CompressFormat#getText()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testz_GetTextMethod() throws Exception {

        assertEquals("z", CompressFormat.z.getText());
    }

    /**
     * Test the {@link CompressFormat#ZStandard}
     * {@link CompressFormat#getText()} method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testZStandard_GetTextMethod() throws Exception {

        assertEquals("ZStandard", CompressFormat.ZStandard.getText());
    }

    /**
     * Test the {@link CompressFormat#Guess} {@link CompressFormat#getText()}
     * method
     * 
     * @throws Exception
     *             if an error occurred during test execution
     * @since 1.38.0
     */
    @Test
    public void testGuess_GetTextMethod() throws Exception {

        assertEquals("Guess", CompressFormat.Guess.getText());
    }



}
