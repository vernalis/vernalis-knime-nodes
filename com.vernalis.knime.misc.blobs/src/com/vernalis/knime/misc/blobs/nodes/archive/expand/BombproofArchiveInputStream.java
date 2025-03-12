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
package com.vernalis.knime.misc.blobs.nodes.archive.expand;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.utils.InputStreamStatistics;
import org.knime.core.node.NodeLogger;

import com.vernalis.knime.misc.blobs.nodes.ExpansionBombProof;

/**
 * A {@link CompressorInputStream} wrapper which implements
 * {@link ExpansionBombProof}
 * <p>
 * This method checks all read operations for violation of the maximum
 * compression ratio or expanded bytes size
 * </p>
 * <p>
 * <strong>NB</strong> Implementations must count files from concatenated
 * archives externally.
 * <p>
 * 
 * @author S.Roughley knime@vernalis.com
 * @param <E>
 *            The type of Entry returned by the archive stream
 * @since 1.38.0
 */
public class BombproofArchiveInputStream extends ArchiveInputStream
        implements ExpansionBombProof, InputStreamStatistics {

    private static final Consumer<String> EMPTY_CONSUMER = s -> {
        // do nothing
    };
    private ArchiveInputStream cis;
    private int entriesRead = 0;
    private long uncompressedBytesRead;

    private final long maxExpandedBytes;
    private final double maxCompressionRatio;
    private final int maxEntries;
    private final Predicate<String> pathPredicate;
    private final boolean keepDirectories;
    private final NodeLogger logger;
    private final Consumer<String> warningConsumer;

    /**
     * Constructor when the total bytes from a multifile input stream do not
     * matter
     * 
     * @param cis
     *            the {@link CompressorInputStream} to wrap. Must implement
     *            {@link InputStreamStatistics}
     * @param maxExpandedBytes
     *            the maximum allowed number of expanded bytes. Negative values
     *            will result in not checking for this limit
     * @param maxCompressionRatio
     *            the maximum compression ratio. Negative values will result in
     *            not checking this limit
     * @param maxEntries
     *            the maximum number of entries permitted from the archive.
     *            Negative values will result in
     *            not checking this limit
     * @param keepDirectories
     *            whether directory entries should be retained by calls to
     *            {@link #getNextEntry()}
     * @throws IllegalArgumentException
     *             If {@code cis} does not implement
     *             {@link InputStreamStatistics}
     * @throws NullPointerException
     *             if {@code cis} is {@code null}
     */
    public BombproofArchiveInputStream(ArchiveInputStream cis,
            long maxExpandedBytes, double maxCompressionRatio, int maxEntries,
            boolean keepDirectories)
            throws IllegalArgumentException, NullPointerException {

        this(cis, maxExpandedBytes, maxCompressionRatio, maxEntries,
                keepDirectories, null, null, null);
    }

    /**
     * Constructor when the total bytes from a multifile input stream do not
     * matter
     * 
     * @param cis
     *            the {@link CompressorInputStream} to wrap. Must implement
     *            {@link InputStreamStatistics}
     * @param maxExpandedBytes
     *            the maximum allowed number of expanded bytes. Negative values
     *            will result in not checking for this limit
     * @param maxCompressionRatio
     *            the maximum compression ratio. Negative values will result in
     *            not checking this limit
     * @param maxEntries
     *            the maximum number of entries permitted from the archive.
     *            Negative values will result in
     *            not checking this limit
     * @param keepDirectories
     *            whether directory entries should be retained by calls to
     *            {@link #getNextEntry()}
     * @param pathPredicate
     *            A predicate to filter entry paths by. {@code null} will return
     *            all entries
     * @param logger
     *            a node logger instance
     * @param warningConsumer
     *            an optional warning message consumer
     * @throws IllegalArgumentException
     *             If {@code cis} does not implement
     *             {@link InputStreamStatistics}
     * @throws NullPointerException
     *             if {@code cis} is {@code null}
     */
    public BombproofArchiveInputStream(ArchiveInputStream cis,
            long maxExpandedBytes, double maxCompressionRatio, int maxEntries,
            boolean keepDirectories, Predicate<String> pathPredicate,
            NodeLogger logger, Consumer<String> warningConsumer) {

        if (!(cis instanceof InputStreamStatistics)) {
            throw new IllegalArgumentException(
                    "The supplied archive input stream must implement InputStreamStatistics");
        }
        this.cis = Objects.requireNonNull(cis, "An input stream is required!");
        this.maxExpandedBytes = maxExpandedBytes;
        this.maxCompressionRatio = maxCompressionRatio;
        this.maxEntries = maxEntries;
        this.pathPredicate = pathPredicate == null ? p -> true : pathPredicate;
        this.keepDirectories = keepDirectories;
        this.logger =
                logger == null ? NodeLogger.getLogger(getClass()) : logger;
        this.warningConsumer =
                warningConsumer == null ? EMPTY_CONSUMER : warningConsumer;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vernalis.knime.misc.blobs.nodes.BombProof#
     * getMaxExpandedBytes()
     */
    @Override
    public long getMaxExpandedBytes() {

        return maxExpandedBytes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vernalis.knime.misc.blobs.nodes.BombProof#
     * getMaxExpansionFiles()
     */
    @Override
    public int getMaxEntries() {

        return maxEntries;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vernalis.knime.misc.blobs.nodes.BombProof#
     * getMaxCompressionRatio()
     */
    @Override
    public double getMaxCompressionRatio() {

        return maxCompressionRatio;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vernalis.knime.misc.blobs.nodes.BombProof#detonationCheck()
     */
    @Override
    public void detonationCheck() throws ExpansionDetonationException {

        if (getMaxExpandedBytes() > 0
                && getUncompressedCount() > getMaxExpandedBytes()) {
            throw new ExpansionDetonationException("Expanded size "
                    + getUncompressedCount() + " has exceeded the maximum "
                    + getMaxExpandedBytes());
        }
        if (getMaxCompressionRatio() > 0.0) {
            double compressionRatio = getCompressedCount() == 0 ? 1.0
                    : (double) getUncompressedCount() / getCompressedCount();
            if (compressionRatio > getMaxCompressionRatio()) {
                throw new ExpansionDetonationException(String.format(
                        "Compression ratio %.3f has exceeded the maximum %.3f",
                        compressionRatio, getMaxCompressionRatio()));
            }
        }
        if (maxEntries > 0 && entriesRead > maxEntries) {
            throw new ExpansionDetonationException(
                    "Expanded entries " + entriesRead
                            + " has exceeded the maximum " + getMaxEntries());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.compress.utils.InputStreamStatistics#
     * getCompressedCount()
     */
    @Override
    public long getCompressedCount() {

        return ((InputStreamStatistics) cis).getCompressedCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.compress.compressors.CompressorInputStream#
     * getUncompressedCount()
     */
    @Override
    public long getUncompressedCount() {
        // ZipArchiveInputStream (and therefore Jar...) reset this value on each
        // call to next entry
        // This is not documented

        return
        // Current entry
        ((InputStreamStatistics) cis).getUncompressedCount()
                // Previous entries
                + uncompressedBytesRead;
    }

    /**
     * @return the number of entries read from the archive
     */
    public int getEntriesRead() {

        return entriesRead;
    }

    @Deprecated
    @Override
    public int getCount() {

        return cis.getCount();
    }

    @Override
    public long getBytesRead() {

        return getUncompressedCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#markSupported()
     */
    @Override
    public boolean markSupported() {

        return cis.markSupported();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#mark(int)
     */
    @Override
    public synchronized void mark(int readlimit) {

        cis.mark(readlimit);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#reset()
     */
    @Override
    public synchronized void reset() throws IOException {

        cis.reset();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException {

        int retVal = cis.read();
        detonationCheck();
        return retVal;
    }

    @Override
    public int read(byte[] b) throws IOException {

        final int retVal = cis.read(b);
        detonationCheck();
        return retVal;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {

        final int retVal = cis.read(b, off, len);
        detonationCheck();
        return retVal;
    }

    @Override
    public byte[] readAllBytes() throws IOException {

        final byte[] retVal = cis.readAllBytes();
        detonationCheck();
        return retVal;
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {

        final byte[] retVal = cis.readNBytes(len);
        detonationCheck();
        return retVal;
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {

        final int retVal = cis.readNBytes(b, off, len);
        detonationCheck();
        return retVal;
    }

    @Override
    public long skip(long n) throws IOException {

        final long retVal = cis.skip(n);
        detonationCheck();
        return retVal;
    }

    @Override
    public void skipNBytes(long n) throws IOException {

        cis.skipNBytes(n);
        detonationCheck();
    }

    @Override
    public int available() throws IOException {

        return cis.available();
    }

    @Override
    public void close() throws IOException {

        cis.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.commons.compress.archivers.ArchiveInputStream#getNextEntry()
     */
    @Override
    public ArchiveEntry getNextEntry() throws IOException {

        // ZipArchiveInputStream (and therefore Jar...) reset this value on each
        // call to next entry
        // This is not documented
        uncompressedBytesRead +=
                ((InputStreamStatistics) cis).getUncompressedCount();

        ArchiveEntry retVal;
        // Look for the next entry in the underlying stream which we can read
        // and
        // which matches the path predicate
        while ((retVal = cis.getNextEntry()) != null) {
            if (!cis.canReadEntryData(retVal)) {
                logger.warnWithFormat("Unable to read entry '%s'",
                        retVal.getName());
                warningConsumer.accept(
                        "Unable to read all entries - see log for details");
                // Try again
                continue;
            }
            if ((keepDirectories || !retVal.isDirectory())
                    && pathPredicate.test(retVal.getName())) {
                // Use this entry
                entriesRead++;
                detonationCheck();
                return retVal;
            }
        }
        // If we are here we have run out of entries
        return null;
    }

}
