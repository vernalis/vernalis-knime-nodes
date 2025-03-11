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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.compressors.CompressorInputStream;


/**
 * A {@link CompressorInputStream} implementation of pass through the incoming
 * stream unchanged
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.38.0
 */
public class PassThroughCompressorInputStream extends CompressorInputStream {

    private InputStream is;

    /**
     * Constructor
     * 
     * @param is
     *            the stream to wrap
     */
    public PassThroughCompressorInputStream(InputStream is) {

        this.is = is;
    }

    /* (non-Javadoc)
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException {

        if (is == null) {
            throw new IOException("Wrapped stream disposed");
        }
        return is.read();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#close()
     */
    @Override
    public void close() throws IOException {

        if (is != null) {
            is.close();
        }
        is = null;
    }

}
