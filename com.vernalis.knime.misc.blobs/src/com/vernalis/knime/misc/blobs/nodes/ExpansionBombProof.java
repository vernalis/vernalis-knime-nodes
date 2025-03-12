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

import java.io.IOException;

/**
 * An interface marking an stream as being resistant to decompression 'bombs'
 * (e.g GZip bomb, Brotli-bomb, Zip-Bomb etc)
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.38.0
 */
public interface ExpansionBombProof {

    /** Default maximum number of entries */
    public static int DEFAULT_MAX_ENTRIES = 10_000;

    /** Default maximum expanded size in bytes */
    public static long DEFAULT_MAX_EXPANDED_SIZE = 1_000_000_000; // 1GB

    /** Default maximum compression ratio */
    public static double DEFAULT_MAX_COMPRESSION_RATIO = 10.0;

    /**
     * Exception thrown when expansion failure is triggered by a call to
     * {@link ExpansionBombProof#detonationCheck()}
     * 
     * @author S.Roughley knime@vernalis.com
     * @since 1.38.0
     */
    public class ExpansionDetonationException extends IOException {

        private static final long serialVersionUID = 1L;

        /**
         * Simple constructor
         * 
         * @param message
         *            the error message
         */
        public ExpansionDetonationException(String message) {

            super(message);
        }

        /**
         * Constructor
         * 
         * @param message
         *            the error message
         * @param cause
         *            the optional causing exception
         */
        public ExpansionDetonationException(String message, Throwable cause) {

            super(message, cause);
        }

    }

    /**
     * @return the maximum number of bytes allowed to expand to. Negative values
     *             indicate no limit
     */
    public long getMaxExpandedBytes();

    /**
     * @return the maximum compression ratio allowed during expansion. Negative
     *             values indicate no limit
     */
    public double getMaxCompressionRatio();

    /**
     * @return the maximum number of files (entries) allowed from a single
     *             archive or
     *             concatenated compression
     * @throws UnsupportedOperationException
     *             if this is not supported by the implementation
     */
    public int getMaxEntries() throws UnsupportedOperationException;

    /**
     * Check to call to ensure none of the specified limits are being violated
     * by the current state.
     * 
     * @throws ExpansionDetonationException
     *             thrown if one or more limits are violated
     */
    public void detonationCheck() throws ExpansionDetonationException;
}
