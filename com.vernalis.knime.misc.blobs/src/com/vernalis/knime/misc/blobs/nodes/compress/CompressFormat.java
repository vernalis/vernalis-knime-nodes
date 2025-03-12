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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStream;
import org.apache.commons.compress.compressors.brotli.BrotliUtils;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream;
import org.apache.commons.compress.compressors.deflate64.Deflate64CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorOutputStream;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorOutputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;
import org.apache.commons.compress.compressors.lzma.LZMAUtils;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorInputStream;
import org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStream;
import org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorOutputStream;
import org.apache.commons.compress.compressors.snappy.SnappyCompressorInputStream;
import org.apache.commons.compress.compressors.snappy.SnappyCompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZUtils;
import org.apache.commons.compress.compressors.z.ZCompressorInputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.MissingCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.time.localdatetime.LocalDateTimeCellFactory;
import org.knime.core.node.util.ButtonGroupEnumInterface;

import com.vernalis.knime.misc.blobs.nodes.GuessInputStreamOptions;
import com.vernalis.knime.misc.blobs.nodes.InputStreamWrapper;
import com.vernalis.knime.misc.blobs.nodes.InputStreamWrapperOptions;
import com.vernalis.knime.misc.blobs.nodes.KnowsSizeOutputStream;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapper;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapperOptions;
import com.vernalis.knime.misc.blobs.nodes.archive.BlankInOutStreamOptions;
import com.vernalis.testing.NoTest;

/**
 * An enum listing non-archival compression formats
 * 
 * @author S.Roughley knime@vernalis.com
 */
public enum CompressFormat implements ButtonGroupEnumInterface,
        InputStreamWrapper, OutputStreamWrapper {

    /** Brotli algorithm */
    Brotli(BrotliUtils.isBrotliCompressionAvailable()
            ? Collections.singleton(Expand)
            : Collections.emptySet()) {

        @Override
        public CompressorInputStream wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts)
                throws IOException, InputStreamWrapException {

            if (supportsDecompression()) {
                // Only if available in this JVM
                return new BrotliCompressorInputStream(is);
            } else {
                return super.wrapInputStream(is, opts);
            }
        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            // No signature magic bytes for Brotli...
            return false;
        }

    },

    /** bzip2 formt */
    bzip2(Compress, Expand, Concatenation) {

        @Override
        public CompressorInputStream wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts) throws IOException {

            return new BZip2CompressorInputStream(is, false);

        }

        @Override
        public CompressorOutputStream wrapOutputStream(OutputStream os,
                OutputStreamWrapperOptions opts)
                throws UnsupportedOperationException, IOException {

            return new BZip2CompressorOutputStream(os);

        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            return BZip2CompressorInputStream.matches(signature, length);
        }

    },

    /** deflate format */
    deflate(Compress, Expand) {

        @Override
        public CompressorInputStream wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts) throws IOException {

            DeflateInOutStreamOptions deflateOpts = checkedCastOrDefault(opts,
                    DeflateInOutStreamOptions.class, deflate);
            return new DeflateCompressorInputStream(is,
                    deflateOpts.getParameters());

        }

        @Override
        public InputStreamWrapperOptions createInputStreamOptions() {

            return new DeflateInOutStreamOptions();
        }

        @Override
        public CompressorOutputStream wrapOutputStream(OutputStream os,
                OutputStreamWrapperOptions opts)
                throws UnsupportedOperationException, IOException,
                OutputStreamWrapException {

            DeflateInOutStreamOptions deflateOpt = checkedCastOrDefault(opts,
                    DeflateInOutStreamOptions.class, deflate);

            return new DeflateCompressorOutputStream(os,
                    deflateOpt.getParameters());

        }

        @Override
        public OutputStreamWrapperOptions createOutputStreamOptions() {

            return new DeflateInOutStreamOptions();
        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            return DeflateCompressorInputStream.matches(signature, length);
        }

    },

    /** Deflate64 format */
    deflate64(Expand) {

        @Override
        public CompressorInputStream wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts) throws IOException {

            return new Deflate64CompressorInputStream(is);

        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            return DeflateCompressorInputStream.matches(signature, length);
        }

    },

    /** GZip format */
    gzip(Compress, Expand, Concatenation, Comment, Filename, Timestamp) {

        @Override
        public CompressorInputStream wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts) throws IOException {

            return new GzipCompressorInputStream(is, false);

        }

        @Override
        public List<DataColumnSpec> getAdditionalDecompressionOutputColumnSpecs() {

            List<DataColumnSpec> retVal = new ArrayList<>();
            retVal.add(new DataColumnSpecCreator("Modification Time",
                    LocalDateTimeCellFactory.TYPE).createSpec());
            retVal.add(new DataColumnSpecCreator("Filename", StringCell.TYPE)
                    .createSpec());
            retVal.add(new DataColumnSpecCreator("Comment", StringCell.TYPE)
                    .createSpec());
            retVal.add(new DataColumnSpecCreator("Operating System",
                    StringCell.TYPE).createSpec());
            return retVal;
        }

        @Override
        public DataCell[] getAdditionalDecompressionOutputCells(
                CompressorInputStream cis) {

            GzipParameters meta =
                    ((GzipCompressorInputStream) cis).getMetaData();

            String[] OSNames =
                    new String[] { "FAT File System (MS-DOS, OS/2, NT/Win32)",
                            "Amiga", "VMS (or OpenVMS)", "Unix", "VM/CMS",
                            "Atari TOS", "HPFS file system (OS/2, NT)",
                            "Macintosh", "Z-System", "CP/M", "TOPS-20",
                            "NTFS file system  (NT)", "QDOS", "Acorn RISCOS" };

            return new DataCell[] {
                    LocalDateTimeCellFactory.create(Instant
                            .ofEpochMilli(meta.getModificationTime())
                            .atOffset(ZoneOffset.UTC).toLocalDateTime()),
                    meta.getFilename() == null ? DataType.getMissingCell()
                            : new StringCell(meta.getFilename()),
                    meta.getComment() == null ? DataType.getMissingCell()
                            : new StringCell(meta.getComment()),
                    meta.getOperatingSystem() >= OSNames.length
                            ? meta.getOperatingSystem() == 255
                                    ? new StringCell("Unknown")
                                    : new MissingCell(
                                            "Unknown operating system ID: "
                                                    + meta.getOperatingSystem())
                            : new StringCell(
                                    OSNames[meta.getOperatingSystem()]) };
        }

        @Override
        public CompressorOutputStream wrapOutputStream(OutputStream os,
                OutputStreamWrapperOptions opts)
                throws UnsupportedOperationException, IOException,
                OutputStreamWrapException {

            GZipOutstreamOption gzipOpts =
                    checkedCastOrDefault(opts, GZipOutstreamOption.class, gzip);
            return new GzipCompressorOutputStream(os, gzipOpts.getParameters());

        }

        @Override
        public OutputStreamWrapperOptions createOutputStreamOptions() {

            return new GZipOutstreamOption();
        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            return GzipCompressorInputStream.matches(signature, length);
        }

    },

    /** LZ4 Block format */
    LZ4_Block(Compress, Expand) {

        @Override
        public CompressorInputStream wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts) throws IOException {

            return new BlockLZ4CompressorInputStream(is);

        }

        @Override
        public CompressorOutputStream wrapOutputStream(OutputStream os,
                OutputStreamWrapperOptions opts)
                throws UnsupportedOperationException, IOException {

            return new BlockLZ4CompressorOutputStream(os);

        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            // No magic bytes...
            return false;
        }

    },

    /** LZ4 Framed format */
    LZ4_Framed(Compress, Expand, Concatenation) {

        @Override
        public CompressorInputStream wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts) throws IOException {

            return new FramedLZ4CompressorInputStream(is, false);

        }

        @Override
        public CompressorOutputStream wrapOutputStream(OutputStream os,
                OutputStreamWrapperOptions opts)
                throws UnsupportedOperationException, IOException,
                OutputStreamWrapException {

            FramedLZ4OutstreamOptions lz4Opts = checkedCastOrDefault(opts,
                    FramedLZ4OutstreamOptions.class, LZ4_Framed);

            return new FramedLZ4CompressorOutputStream(os,
                    lz4Opts.getParameters());

        }

        @Override
        public OutputStreamWrapperOptions createOutputStreamOptions() {

            return new FramedLZ4OutstreamOptions();
        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            return FramedLZ4CompressorInputStream.matches(signature, length);
        }

    },

    /** LZMA format */
    LZMA(LZMAUtils.isLZMACompressionAvailable() ? EnumSet.of(Expand, Compress)
            : Collections.emptySet()) {

        @Override
        public CompressorInputStream wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts)
                throws IOException, InputStreamWrapException {

            if (supportsDecompression()) {
                return new LZMACompressorInputStream(is);
            }
            return super.wrapInputStream(is, opts);
        }

        @Override
        public CompressorOutputStream wrapOutputStream(OutputStream os,
                OutputStreamWrapperOptions opts)
                throws UnsupportedOperationException, IOException,
                OutputStreamWrapException {

            if (supportsCompression()) {
                return new LZMACompressorOutputStream(os);
            }
            return super.wrapOutputStream(os, opts);
        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            return LZMAUtils.matches(signature, length);
        }

    },

    /** Framed Snappy format */
    Snappy_Framed(Compress, Expand) {

        @Override
        public CompressorInputStream wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts)
                throws IOException, InputStreamWrapException {

            return new FramedSnappyCompressorInputStream(is);
        }

        @Override
        public CompressorOutputStream wrapOutputStream(OutputStream os,
                OutputStreamWrapperOptions opts)
                throws UnsupportedOperationException, IOException {

            return new FramedSnappyCompressorOutputStream(os);
        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            return FramedSnappyCompressorInputStream.matches(signature, length);
        }

    },

    /** Snappy format */
    Snappy(Compress, Expand) {

        @Override
        public CompressorInputStream wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts)
                throws IOException, InputStreamWrapException {

            return new SnappyCompressorInputStream(is);
        }

        @Override
        public CompressorOutputStream wrapOutputStream(OutputStream os,
                OutputStreamWrapperOptions opts)
                throws UnsupportedOperationException, IOException,
                OutputStreamWrapException {

            if (os instanceof KnowsSizeOutputStream ksos) {
                return new SnappyCompressorOutputStream(os, ksos.getSize());
            } else {
                throw new OutputStreamWrapException(
                        "Snappy Compression needs a KnowsSizeOutputStream to compress");
            }
        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            return false;
        }

    },

    /** xz format */
    xz(XZUtils.isXZCompressionAvailable()
            ? EnumSet.of(Compress, Expand, Concatenation)
            : Collections.emptySet()) {

        @Override
        public CompressorInputStream wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts)
                throws IOException, InputStreamWrapException {

            if (supportsDecompression()) {
                return new XZCompressorInputStream(is, false);
            }
            return super.wrapInputStream(is, opts);
        }

        @Override
        public CompressorOutputStream wrapOutputStream(OutputStream os,
                OutputStreamWrapperOptions opts)
                throws UnsupportedOperationException, IOException,
                OutputStreamWrapException {

            if (supportsCompression()) {
                XZOutstreamOptions xzOpts = checkedCastOrDefault(opts,
                        XZOutstreamOptions.class, xz);
                return new XZCompressorOutputStream(os, xzOpts.getPreset());
            }
            return super.wrapOutputStream(os, opts);
        }

        @Override
        public OutputStreamWrapperOptions createOutputStreamOptions() {

            if (supportsCompression()) {
                return new XZOutstreamOptions();
            }
            return super.createOutputStreamOptions();
        }

        @Override
        public String getCompressDescription() {

            if (supportsCompression()) {
                return "XZ compressor using the specified LZMA2 preset level.\n"
                        + "\n"
                        + "The presets 0-3 are fast presets with medium compression.\n"
                        + "The presets 4-6 are fairly slow presets with high compression.\n"
                        + "The default preset is 6.\n\n"
                        + "The presets 7-9 are like the preset 6 but use bigger dictionaries\n"
                        + "and have higher compressor and decompressor memory requirements.\n"
                        + "Unless the uncompressed size of the file exceeds 8&nbsp;MiB,\n"
                        + "16 MiB, or 32 MiB, it is waste of memory to use the\n"
                        + "presets 7, 8, or 9, respectively.";
            }
            return super.getDescription();
        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            return XZUtils.matches(signature, length);
        }

    },

    /** 'z' format */
    z(Expand) {

        @Override
        public CompressorInputStream wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts)
                throws InputStreamWrapException, IOException {

            return new ZCompressorInputStream(is);
        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            return ZCompressorInputStream.matches(signature, length);
        }

    },

    /** Zstd format */
    ZStandard(ZstdUtils.isZstdCompressionAvailable()
            ? EnumSet.of(Compress, Expand)
            : Collections.emptySet()) {

        @Override
        public CompressorInputStream wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts)
                throws IOException, InputStreamWrapException {

            if (supportsDecompression()) {
                return new ZstdCompressorInputStream(is);
            }
            return super.wrapInputStream(is, opts);
        }

        @Override
        public CompressorOutputStream wrapOutputStream(OutputStream os,
                OutputStreamWrapperOptions opts)
                throws UnsupportedOperationException, IOException,
                OutputStreamWrapException {

            if (supportsCompression()) {
                // TODO: Parameters
                return new ZstdCompressorOutputStream(os);
            }
            return super.wrapOutputStream(os, opts);

        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            return ZstdUtils.matches(signature, length);
        }

    },

    /** Guess the format for expansion */
    Guess(Expand) {

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.vernalis.knime.misc.blobs.nodes.compress.CompressFormat#
         * createInputStreamOptions()
         */
        @Override
        public GuessInputStreamOptions createInputStreamOptions() {

            return new GuessInputStreamOptions();
        }

        @Override
        public CompressorInputStream wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts)
                throws InputStreamWrapException {

            try {
                return new CompressorStreamFactory()
                        .createCompressorInputStream(is.markSupported() ? is
                                : new BufferedInputStream(is));
            } catch (CompressorException e) {
                // No match
                GuessInputStreamOptions guessOpts = checkedCastOrDefault(opts,
                        GuessInputStreamOptions.class, Guess);
                if (guessOpts.isFail()) {
                    throw new InputStreamWrapException(e.getMessage(), e);
                } else if (guessOpts.isMissing()) {
                    return null;
                } else {
                    return new PassThroughCompressorInputStream(is);
                }
            }
        }

        @Override
        public DataCell[] getAdditionalDecompressionOutputCells(
                CompressorInputStream cis) {

            // TODO: Ideally we would user CompressorStreamFactory#detect() on
            // the input to get to this and keep it in a wrapped
            // CompressorInputStream
            // This is a makeshift workaround...

            DataCell cell = DataType.getMissingCell();

            if (cis != null
                    && !(cis instanceof PassThroughCompressorInputStream)) {
                cell = new StringCell(cis.getClass().getSimpleName()
                        .replace("CompressorInputStream", ""));
            }
            return new DataCell[] { cell };
        }

        @Override
        public List<DataColumnSpec> getAdditionalDecompressionOutputColumnSpecs() {

            return Collections
                    .singletonList(new DataColumnSpecCreator("Compression Type",
                            StringCell.TYPE).createSpec());
        }

        @Override
        public String getDescription() {

            return "This format will attempt to guess the incoming compressed format. "
                    + "For formats supporting concatenation, only the first entry will be returned";
        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            return Arrays.stream(values()).filter(v -> v != this)
                    .anyMatch(v -> v.matchesSignature(signature, length))
                    || Pack200CompressorInputStream.matches(signature, length);
        }

    };

    private Set<CompressorCapabilities> capabilities;

    /**
     * Constructor
     */
    private CompressFormat(CompressorCapabilities... capabilities) {

        this.capabilities = EnumSet.noneOf(CompressorCapabilities.class);

        if (capabilities != null) {
            Collections.addAll(this.capabilities, capabilities);
        }
    }

    /**
     * Constructor
     */
    private CompressFormat(Collection<CompressorCapabilities> capabilities) {

        this.capabilities = capabilities == null || capabilities.isEmpty()
                ? EnumSet.noneOf(CompressorCapabilities.class)
                : EnumSet.copyOf(capabilities);

    }

    @Override
    @NoTest
    public CompressorInputStream wrapInputStream(InputStream is,
            InputStreamWrapperOptions opts)
            throws UnsupportedOperationException, IOException,
            InputStreamWrapException {

        throw new UnsupportedOperationException(
                String.format("Cannot compress '%s' format", getText()));

    }

    @Override
    @NoTest
    public InputStreamWrapperOptions createInputStreamOptions() {

        // Most formats dont have options
        if (supportsDecompression()) {
            return BlankInOutStreamOptions.getInstance();
        }
        throw new UnsupportedOperationException(
                String.format("Cannot compress '%s' format", getText()));

    }

    @Override
    @NoTest
    public CompressorOutputStream wrapOutputStream(OutputStream os,
            OutputStreamWrapperOptions opts)
            throws UnsupportedOperationException, IOException,
            OutputStreamWrapException {

        throw new UnsupportedOperationException(
                String.format("Cannot decompress '%s' format", getText()));

    }

    @Override
    @NoTest
    public OutputStreamWrapperOptions createOutputStreamOptions() {

        // Most formats dont have options
        if (supportsCompression()) {
            return BlankInOutStreamOptions.getInstance();
        }
        throw new UnsupportedOperationException(
                String.format("Cannot decompress '%s' format", getText()));
    }

    private static <T extends InputStreamWrapperOptions> T checkedCastOrDefault(
            InputStreamWrapperOptions opts, Class<T> clz,
            InputStreamWrapper wrapper) {

        InputStreamWrapperOptions retVal =
                opts == null ? wrapper.createInputStreamOptions() : opts;

        if (retVal.getClass().equals(clz)) {
            return clz.cast(retVal);
        }
        throw new IllegalArgumentException();

    }

    private static <T extends OutputStreamWrapperOptions> T checkedCastOrDefault(
            OutputStreamWrapperOptions opts, Class<T> clz,
            OutputStreamWrapper wrapper) throws OutputStreamWrapException {

        OutputStreamWrapperOptions retVal =
                opts == null ? wrapper.createOutputStreamOptions() : opts;

        if (retVal.getClass().equals(clz)) {
            return clz.cast(retVal);
        }
        throw new OutputStreamWrapException(
                "Wrong options class - expected " + clz.getSimpleName()
                        + ", got " + retVal.getClass().getSimpleName());

    }

    /**
     * @return a list of additional columns which may be added during
     *             decompression
     */
    public List<DataColumnSpec> getAdditionalDecompressionOutputColumnSpecs() {

        return Collections.emptyList();
    }

    /**
     * @param cis
     *            the {@link CompressorInputStream} performing the
     *            decompression. NB this method will be called once the stream
     *            is decompressed
     * @return the additional cells
     */
    @NoTest
    public DataCell[] getAdditionalDecompressionOutputCells(
            CompressorInputStream cis) {

        return new DataCell[0];
    }

    /**
     * Method to test whether a byte array matches the signature for the screen.
     * This method is intended for finding an appropriate decoding format via
     * {@link #getFormatForStream(InputStream)}, not for testing whether a
     * stream will decode successfully, as some formats do not have magic number
     * bytes and will return {@code false} here whilst being able to decode the
     * stream successfully
     * 
     * @param signature
     *            the signature bytes
     * @param length
     *            the actual number of butes in the signature
     * @return whether the signature bytes match the expected values for the
     *             format
     */
    public abstract boolean matchesSignature(byte[] signature, int length);

    /**
     * Method to find the first matching format for an input stream, based on
     * the magic number bytes
     * 
     * @param is
     *            the input stream
     * @return the format, or {@code null} if no matching format found
     * @throws IOException
     *             If there was an error reading the magic number bytes
     * @throws IllegalArgumentException
     *             if the supplied input stream does not support marking
     * @throws NullPointerException
     *             if the supplied input stream is {@code null}
     */
    public static CompressFormat getFormatForStream(InputStream is)
            throws IOException, IllegalArgumentException, NullPointerException {
        // This is based closely on CompressorStreamFactory#detect() but
        // reworked to ensure the matches are identical to the names in the node
        // dialogs

        if (!Objects.requireNonNull(is).markSupported()) {
            throw new IllegalArgumentException(
                    "The stream must support marking!");
        }
        byte[] sig = new byte[12];
        is.mark(sig.length);
        int sigLength = -1;
        sigLength = IOUtils.readFully(is, sig);
        is.reset();

        for (CompressFormat cf : values()) {
            if (cf.matchesSignature(sig, sigLength)) {
                return cf;
            }
        }
        // No matches
        return null;
    }

    @Override
    public boolean isDefault() {

        return this == getDefault();

    }

    /**
     * @return the default format, which will be the first that supports both
     *             compression and decompression
     * @throws NoSuchElementException
     *             if there is no format filling the requirement for both
     *             compression and decompression
     */
    public static CompressFormat getDefault() throws NoSuchElementException {

        return Arrays.stream(values()).filter(
                fmt -> fmt.supportsCompression() && fmt.supportsDecompression())
                .findFirst().orElseThrow();

    }

    /**
     * @return an array of all the formats supporting compression
     */
    public static CompressFormat[] getCompressionFormats() {

        return Arrays.stream(values())
                .filter(CompressFormat::supportsCompression)
                .toArray(CompressFormat[]::new);
    }

    /**
     * @return an array of all the formats supporting decompression
     */
    public static CompressFormat[] getDecompressionFormats() {

        return Arrays.stream(values())
                .filter(CompressFormat::supportsDecompression)
                .toArray(CompressFormat[]::new);
    }

    @Override
    public String getText() {

        return name().replace('_', ' ');

    }

    @Override
    public String getActionCommand() {

        return name();

    }

    @Override
    @NoTest
    public String getToolTip() {

        return null;

    }

    /**
     * @return whether decompression of this format is supported on this
     *             platform
     */
    @NoTest
    public final boolean supportsDecompression() {

        return capabilities.contains(Expand);

    }

    /**
     * @return whether compression is supported by this format on this platform
     */
    @NoTest
    public final boolean supportsCompression() {

        return capabilities.contains(Compress);

    }

    /**
     * @return whether concatenation is supported
     */
    @NoTest
    public final boolean supportsConcatenation() {

        return capabilities.contains(Concatenation);
    }

    @Override
    @NoTest
    public String getDescription() {

        return null;
    }

    /**
     * @return {@code true} if the format has a non-{@code null} non-blank
     *             description
     */
    @NoTest
    public boolean hasDescription() {

        return getDescription() != null && !getDescription().isBlank();
    }

    /**
     * @return an optional compression-specific description
     */
    @NoTest
    public String getCompressDescription() {

        return getDescription();
    }

    /**
     * @return {@code true} if the format has a non-{@code null} non-blank
     *             compression-specific description
     */
    @NoTest
    public boolean hasCompressDescription() {

        return getCompressDescription() != null
                && !getCompressDescription().isBlank();
    }

    /**
     * @return an optional expansion-specific description
     */
    @NoTest
    public String getExpandDescription() {

        return getDescription();
    }

    /**
     * @return {@code true} if the format has a non-{@code null} non-blank
     *             expansion-specific description
     */
    @NoTest
    public boolean hasExpandDescription() {

        return getExpandDescription() != null
                && !getExpandDescription().isBlank();
    }

    /**
     * @return a copy of the internal available compressor capabilities
     */
    public Set<CompressorCapabilities> getCapabilities() {

        return EnumSet.copyOf(capabilities);
    }

}
