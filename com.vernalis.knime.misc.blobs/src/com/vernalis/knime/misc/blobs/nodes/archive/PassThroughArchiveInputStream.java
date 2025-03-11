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

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;

import com.vernalis.knime.misc.blobs.nodes.archive.PassThroughArchiveInputStream.PassThroughArchiveEntry;

/**
 * A simple {@link ArchiveInputStream} which will always pass the wrapped
 * {@link InputStream} bytes through in a single entry. The {@link ArchiveEntry}
 * will always have {@code null} name, and unknown size
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.38.0
 */
public final class PassThroughArchiveInputStream
        extends
        ArchiveInputStream<PassThroughArchiveEntry> {

    /**
     * A simple {@link ArchiveEntry} for passing straight through
     * 
     * @author S.Roughley knime@vernalis.com
     * @since 1.38.0
     */
    static final class PassThroughArchiveEntry implements ArchiveEntry {

        private PassThroughArchiveEntry() {

        }

        /*
         * (non-Javadoc)
         * 
         * @see org.apache.commons.compress.archivers.ArchiveEntry#getName()
         */
        @Override
        public String getName() {

            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.apache.commons.compress.archivers.ArchiveEntry#getSize()
         */
        @Override
        public long getSize() {

            return SIZE_UNKNOWN;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.apache.commons.compress.archivers.ArchiveEntry#isDirectory()
         */
        @Override
        public boolean isDirectory() {

            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.apache.commons.compress.archivers.ArchiveEntry#
         * getLastModifiedDate()
         */
        @Override
        public Date getLastModifiedDate() {

            return null;
        }

    }

    private static final PassThroughArchiveEntry ENTRY =
            new PassThroughArchiveEntry();
    private InputStream wrapped;
    private PassThroughArchiveEntry entry;

    /**
     * Constructor
     * 
     * @param is
     *            the stream to wrap
     */
    public PassThroughArchiveInputStream(InputStream is) {

        wrapped = is;
        entry = is == null ? null : ENTRY;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.commons.compress.archivers.ArchiveInputStream#getNextEntry()
     */
    @Override
    public PassThroughArchiveEntry getNextEntry() throws IOException {

        return entry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.compress.archivers.ArchiveInputStream#read()
     */
    @Override
    public int read() throws IOException {

        if (wrapped == null) {
            throw new IOException("Wrapped stream is closed");
        }
        final int retVal = wrapped.read();
        if (retVal < 0) {
            // EOF..
            // Signal no more entries..
            // NB Don't close the wrapped stream as that will upset things later
            // when #close() is called
            entry = null;
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#close()
     */
    @Override
    public void close() throws IOException {

        if (wrapped == null) {
            throw new IOException("Stream already closed");
        }
        try {
            wrapped.close();
        } finally {
            wrapped = null;
            entry = null;
        }
    }

    /**
     * Static match check method which will always return {@code true}
     * 
     * @param signature
     *            the byte signature for the format
     * @param length
     *            the length of the signature
     * @return whether this stream can expand the supplied format
     */
    public static boolean matches(byte[] signature, int length) {

        // Any stream can be passed through unchanged...
        return true;
    }

}
