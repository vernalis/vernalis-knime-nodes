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
package com.vernalis.knime.misc.blobs.nodes.compress.expand;

import java.io.IOException;
import java.util.Objects;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.utils.InputStreamStatistics;

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
 * @since 1.38.0
 */
public class BombproofCompressorInputStream extends CompressorInputStream
        implements ExpansionBombProof, InputStreamStatistics {

    private CompressorInputStream cis;

    private final long readBytes;
    private final long expandedBytes;
    private final long maxExpandedBytes;
    private final double maxCompressionRatio;

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
     * @throws IllegalArgumentException
     *             If {@code cis} does not implement
     *             {@link InputStreamStatistics}
     * @throws NullPointerException
     *             if {@code cis} is {@code null}
     */
    public BombproofCompressorInputStream(CompressorInputStream cis,
            long maxExpandedBytes, double maxCompressionRatio)
            throws IllegalArgumentException, NullPointerException {

        this(cis, maxExpandedBytes, maxCompressionRatio, 0L, 0L);
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
     * @param expandedBytes
     *            the de-compressed (i.e. expanded) bytes read previously from
     *            the stream
     * @param readBytes
     *            the raw compressed bytes read previously from the stream
     * @throws IllegalArgumentException
     *             If {@code cis} does not implement
     *             {@link InputStreamStatistics}
     * @throws NullPointerException
     *             if {@code cis} is {@code null}
     */
    public BombproofCompressorInputStream(CompressorInputStream cis,
            long maxExpandedBytes, double maxCompressionRatio,
            long expandedBytes, long readBytes) {

        if (!(cis instanceof InputStreamStatistics)) {
            throw new IllegalArgumentException(
                    "The supplied compressor input stream must implement InputStreamStatistics");
        }
        this.cis = Objects.requireNonNull(cis, "An input stream is required!");
        this.maxExpandedBytes = maxExpandedBytes;
        this.maxCompressionRatio = maxCompressionRatio;
        if (expandedBytes < 0L) {
            throw new IllegalArgumentException("expandedBytes must be >= 0");
        }
        this.expandedBytes = expandedBytes;
        this.readBytes = readBytes;
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

        throw new UnsupportedOperationException();
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
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.compress.utils.InputStreamStatistics#
     * getCompressedCount()
     */
    @Override
    public long getCompressedCount() {

        return ((InputStreamStatistics) cis).getCompressedCount() + readBytes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.compress.compressors.CompressorInputStream#
     * getUncompressedCount()
     */
    @Override
    public long getUncompressedCount() {

        return cis.getUncompressedCount() + expandedBytes;
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

}
