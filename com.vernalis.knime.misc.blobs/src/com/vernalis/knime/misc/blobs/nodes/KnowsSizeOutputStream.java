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
import java.io.OutputStream;
import java.util.Objects;

import com.vernalis.testing.NoTest;


/**
 * An {@link OutputStream} wrapper for a stream that knows definitively the
 * total
 * number of bytes which will be writtedn to the stream
 * 
 * @author S.Roughley knime@vernalis.com
 */
@NoTest
public class KnowsSizeOutputStream extends OutputStream {

    private final OutputStream os;
    private final long size;

    /**
     * Constructor
     * 
     * @param os
     *            the stream to wrap
     * @param size
     *            the known size. In situations where it is possible and
     *            acceptable not to know the size, then a value of {@code -1}
     *            may be
     *            used
     */
    public KnowsSizeOutputStream(OutputStream os, long size) {

        this.os = Objects.requireNonNull(os);
        this.size = size;
    }

    /**
     * @return the known size
     */
    public long getSize() {

        return size;
    }

    @Override
    public int hashCode() {

        return Objects.hash(os, size);
    }

    @Override
    public void write(int b) throws IOException {

        os.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {

        os.write(b);
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KnowsSizeOutputStream other = (KnowsSizeOutputStream) obj;
        return Objects.equals(os, other.os) && size == other.size;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {

        os.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {

        os.flush();
    }

    @Override
    public void close() throws IOException {

        os.close();
    }

    @Override
    public String toString() {

        return os.toString();
    }


}
