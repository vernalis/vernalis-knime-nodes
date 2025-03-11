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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.arj.ArjArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream;
import org.apache.commons.compress.archivers.dump.DumpArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.knime.core.node.util.ButtonGroupEnumInterface;

import com.vernalis.knime.misc.blobs.nodes.GuessInputStreamOptions;
import com.vernalis.knime.misc.blobs.nodes.InputStreamWrapper;
import com.vernalis.knime.misc.blobs.nodes.InputStreamWrapperOptions;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapper;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapperOptions;
import com.vernalis.testing.NoTest;

/**
 * An enum providing all currently supported Archiving formats
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.38.0
 */
public enum ArchiveFormat implements ButtonGroupEnumInterface,
        InputStreamWrapper, OutputStreamWrapper {

    /** Tar format */
    tar(true, true, false, "TAR archive format") {

        // Why cant I narrow opts to TIO here?
        @Override
        public TarArchiveInputStream wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts) {

            TarInstreamOptions tarOpts =
                    checkedCastOrDefault(opts, TarInstreamOptions.class, tar);

            return new TarArchiveInputStream(is, tarOpts.getBlockSize(),
                    TarConstants.DEFAULT_RCDSIZE, tarOpts.getEncoding(),
                    tarOpts.isLenient());

        }

        @Override
        public TarInstreamOptions createInputStreamOptions() {

            return new TarInstreamOptions();

        }

        @Override
        public TarArchiveOutputStream wrapOutputStream(OutputStream os,
                OutputStreamWrapperOptions opts)
                throws OutputStreamWrapException {

            TarOutstreamOptions tarOpts =
                    checkedCastOrDefault(opts, TarOutstreamOptions.class, tar);

            final TarArchiveOutputStream aos = new TarArchiveOutputStream(os,
                    tarOpts.getBlockSize(), tarOpts.getEncoding());
            aos.setBigNumberMode(tarOpts.getBigNumberMode());
            aos.setLongFileMode(tarOpts.getLongFileMode());
            return aos;

        }

        @Override
        public TarOutstreamOptions createOutputStreamOptions() {

            return new TarOutstreamOptions();

        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            if (TarArchiveInputStream.matches(signature, length)) {
                return true;
            }

            // This from ArchiveStreamFactory#detect()
            if (length >= signature.length) {
                TarArchiveInputStream tais = null;

                try {
                    tais = new TarArchiveInputStream(
                            new ByteArrayInputStream(signature));

                    // COMPRESS-191 - verify the header checksum
                    if (tais.getNextEntry().isCheckSumOK()) {
                        return true;
                    }
                } catch (final Exception e) { // NOPMD NOSONAR
                    // can generate IllegalArgumentException as well
                    // as IOException
                    // autodetection, simply not a TAR
                    // ignored
                    return false;
                } finally {
                    IOUtils.closeQuietly(tais);
                }
            }
            return false;
        }

    },

    /** 'ar' format */
    ar(false, true, false, "Unix Archiver 'ar' format") {

        @Override
        public ArArchiveInputStream wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts) {

            return new ArArchiveInputStream(is);

        }

        @Override
        public ArArchiveOutputStream wrapOutputStream(OutputStream os,
                OutputStreamWrapperOptions opts) {

            return new ArArchiveOutputStream(os);

        }

        @Override
        public BlankInOutStreamOptions createInputStreamOptions() {

            return BlankInOutStreamOptions.getInstance();

        }

        @Override
        public BlankInOutStreamOptions createOutputStreamOptions() {

            return BlankInOutStreamOptions.getInstance();

        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            return ArArchiveInputStream.matches(signature, length);
        }

    },

    /** 'arj' format */
    arj(true, false, true, "'arj' Archive format") {

        @Override
        public ArjArchiveInputStream wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts)
                throws InputStreamWrapException {

            ArjInstreamOptions arjOpts =
                    checkedCastOrDefault(opts, ArjInstreamOptions.class, arj);

            try {
                return new ArjArchiveInputStream(is, arjOpts.getEncoding());
            } catch (ArchiveException e) {
                throw new InputStreamWrapException(e.getMessage(), e);
            }
        }

        @Override
        public ArjInstreamOptions createInputStreamOptions() {

            return new ArjInstreamOptions();
        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            return ArjArchiveInputStream.matches(signature, length);
        }

    },

    /** 'cpio' format */
    cpio(true, true, false, "'cpio' archive format") {

        @Override
        public CpioArchiveInputStream wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts) {

            CpioInstreamOptions cpioOpts =
                    checkedCastOrDefault(opts, CpioInstreamOptions.class, cpio);
            return new CpioArchiveInputStream(is, cpioOpts.getBlockSize(),
                    cpioOpts.getEncoding());

        }

        @Override
        public CpioArchiveOutputStream wrapOutputStream(OutputStream os,
                OutputStreamWrapperOptions opts)
                throws OutputStreamWrapException {

            CpioOutstreamOptions cpioOpts = checkedCastOrDefault(opts,
                    CpioOutstreamOptions.class, cpio);
            return new CpioArchiveOutputStream(os, cpioOpts.getFormatFlag(),
                    cpioOpts.getBlockSize(), cpioOpts.getEncoding());

        }

        @Override
        public CpioInstreamOptions createInputStreamOptions() {

            return new CpioInstreamOptions();

        }

        @Override
        public CpioOutstreamOptions createOutputStreamOptions() {

            return new CpioOutstreamOptions();
        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            return CpioArchiveInputStream.matches(signature, length);
        }

    },

    /** 'dump' format */
    dump(true, false, true, "UNIX 'dump' Archive") {

        @Override
        public ArchiveInputStream<?> wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts)
                throws InputStreamWrapException {

            DumpInstreamOptions dumpOpts =
                    checkedCastOrDefault(opts, DumpInstreamOptions.class, dump);

            try {
                return new DumpArchiveInputStream(is, dumpOpts.getEncoding());
            } catch (ArchiveException e) {
                throw new InputStreamWrapException(e.getMessage(), e);
            }
        }

        @Override
        public InputStreamWrapperOptions createInputStreamOptions() {

            return new DumpInstreamOptions();

        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            return DumpArchiveInputStream.matches(signature, length);
        }

    },

    /** Zip format */
    zip(true, true, true, "ZIP Archive") {

        @Override
        public ZipArchiveInputStream wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts) {

            ZipInstreamOptions zipOpts =
                    checkedCastOrDefault(opts, ZipInstreamOptions.class, zip);
            return new ZipArchiveInputStream(is, zipOpts.getEncoding(),
                    zipOpts.isUseUnicodeExtraFields(),
                    zipOpts.isAllowStoredEntriesWithDataDescriptor());

        }

        @Override
        public ZipArchiveOutputStream wrapOutputStream(OutputStream os,
                OutputStreamWrapperOptions opts)
                throws UnsupportedOperationException,
                OutputStreamWrapException {

            ZipOutstreamOptions zipOpts =
                    checkedCastOrDefault(opts, ZipOutstreamOptions.class, zip);

            final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(os);
            zipOpts.applyToOutputStream(zos);
            return zos;

        }

        @Override
        public InputStreamWrapperOptions createInputStreamOptions() {

            return new ZipInstreamOptions();
        }

        @Override
        public ZipOutstreamOptions createOutputStreamOptions() {

            return new ZipOutstreamOptions();
        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            return ZipArchiveInputStream.matches(signature, length);
        }

    },

    /** Java 'jar' archive format */
    jar(true, true, true, "Java 'jar' archive") {

        @Override
        public ArchiveInputStream<?> wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts) {

            JarInstreamOptions jarOpts =
                    checkedCastOrDefault(opts, JarInstreamOptions.class, jar);
            return new JarArchiveInputStream(is, jarOpts.getEncoding());

        }

        @Override
        public ArchiveOutputStream<?> wrapOutputStream(OutputStream os,
                OutputStreamWrapperOptions opts)
                throws UnsupportedOperationException {

            return new JarArchiveOutputStream(os);

        }

        @Override
        public JarInstreamOptions createInputStreamOptions() {

            return new JarInstreamOptions();
        }

        @Override
        public OutputStreamWrapperOptions createOutputStreamOptions() {

            return new JarOutstreamOptions();
        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            return JarArchiveInputStream.matches(signature, length);
        }

    },

    /** Guess format */
    guess(true, false, true, "Attempt to guess the archive format") {

        @Override
        public ArchiveInputStream<?> wrapInputStream(InputStream is,
                InputStreamWrapperOptions opts)
                throws InputStreamWrapException {

            try {
                return new ArchiveStreamFactory().createArchiveInputStream(
                        is.markSupported() ? is : new BufferedInputStream(is));
            } catch (ArchiveException e) {
                // No match
                GuessInputStreamOptions guessOpts = checkedCastOrDefault(opts,
                        GuessInputStreamOptions.class, guess);
                if (guessOpts.isFail()) {
                throw new InputStreamWrapException(e.getMessage(), e);
                } else if (guessOpts.isMissing()) {
                    return null;
                } else {
                    return new PassThroughArchiveInputStream(is);
                }
            }
        }

        @Override
        public GuessInputStreamOptions createInputStreamOptions() {

            return new GuessInputStreamOptions();
            // return BlankInOutStreamOptions.getInstance();
        }

        @Override
        public boolean matchesSignature(byte[] signature, int length) {

            return Arrays.stream(values()).filter(v -> v != this)
                    .anyMatch(v -> v.matchesSignature(signature, length))
                    || SevenZFile.matches(signature, length);
        }

    };

    private final boolean supportsDirectories;
    private final boolean supportsArchiving;
    private final boolean includesCompression;
    private final String description;

    private ArchiveFormat(boolean supportsDirectories,
            boolean supportsArchiving, boolean includesCompression,
            String description) {

        this.supportsDirectories = supportsDirectories;
        this.supportsArchiving = supportsArchiving;
        this.includesCompression = includesCompression;
        this.description = Objects.requireNonNull(description);
    }

    @Override
    @NoTest
    public String getDescription() {

        return description;
    }

    // Narrow the wrapped type
    @Override
    @NoTest
    public abstract ArchiveInputStream<?> wrapInputStream(InputStream in,
            InputStreamWrapperOptions opts)
            throws IOException, InputStreamWrapException;

    @Override
    @NoTest
    public ArchiveOutputStream<?> wrapOutputStream(OutputStream os,
            OutputStreamWrapperOptions opts)
            throws UnsupportedOperationException, OutputStreamWrapException {

        throw new UnsupportedOperationException(
                String.format("Cannot archive to '%s' format", getText()));

    }

    @Override
    public OutputStreamWrapperOptions createOutputStreamOptions() {

        throw new UnsupportedOperationException(
                String.format("Cannot archive to '%s' format", getText()));

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
    public static ArchiveFormat getFormatForStream(InputStream is)
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
        sigLength = org.apache.commons.compress.utils.IOUtils.readFully(is, sig);
        is.reset();

        for (ArchiveFormat af : values()) {

            if (af == dump || af == tar) {
                // Dump & tar have longer signatures - test below
                continue;
            }

            if (af.matchesSignature(sig, sigLength)) {
                return af;
            }
        }
        sig = new byte[32];
        is.mark(sig.length);
        sigLength = org.apache.commons.compress.utils.IOUtils.readFully(is, sig);
        is.reset();

        if (dump.matchesSignature(sig, sigLength)) {
            return dump;
        }
        sig = new byte[512];
        is.mark(sig.length);
        sigLength = org.apache.commons.compress.utils.IOUtils.readFully(is, sig);
        is.reset();

        if (tar.matchesSignature(sig, sigLength)) {
            return tar;
        }
        // No matches
        return null;
    }

    @Override
    public boolean isDefault() {

        return this == getDefault();

    }

    /**
     * @return the default format
     */
    public static ArchiveFormat getDefault() {

        // The first option that supports archiving
        return getArchiveFormats()[0];

    }

    @Override
    public String getText() {

        return name();

    }

    @Override
    @NoTest
    public String getActionCommand() {

        return name();

    }

    @Override
    @NoTest
    public String getToolTip() {

        return null;

    }

    /**
     * @return whether the format supports directories
     */
    public final boolean supportsDirectories() {

        return supportsDirectories;

    }

    /**
     * @return whether the format supports archiving. (All format support
     *             expanding archives)
     */
    public final boolean supportsArchiving() {

        return supportsArchiving;

    }

    /**
     * @return whether the format also includes compression
     */
    public final boolean includesCompression() {

        return includesCompression;

    }

    /**
     * @return an array of those format which support archiving
     */
    public static ArchiveFormat[] getArchiveFormats() {

        return Arrays.stream(values()).filter(ArchiveFormat::supportsArchiving)
                .toArray(ArchiveFormat[]::new);
    }

}
