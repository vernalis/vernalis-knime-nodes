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
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import com.vernalis.knime.misc.blobs.nodes.InputStreamWrapper.InputStreamWrapException;

/**
 * An interface defining an class which wraps an {@link OutputStream} in a new
 * stream. Implementations are not streams themselves, but rather provide
 * methods to perform the wrapping according to certain settings defined in an
 * {@link OutputStreamWrapperOptions}, and to provide a sensible default instance
 * of those options
 * 
 * @author S.Roughley knime@vernalis.com
 */
public interface OutputStreamWrapper {

    /**
     * Method to perform the stream wrapping. Implementations should define
     * their behaviour if {@code null} arguments are supplied, and should
     * ensure, where appropriate that the {@link OutputStreamWrapperOptions}
     * argument is of an appropriate type. The return type of this method should
     * almost certainly be narrowed
     * 
     * @param out
     *            the stream to wrap
     * @param opts
     *            the associated options
     * @return the wrapped stream
     * @throws IOException
     *             if the wrapping process threw in IOException
     * @throws OutputStreamWrapException
     *             for other exceptions thrown during the wrapping process
     */
    public OutputStream wrapOutputStream(OutputStream out,
            OutputStreamWrapperOptions opts)
            throws IOException, OutputStreamWrapException;

    /**
     * @return a new instance of the appropriate wrapper options for the
     *             implementation providing sensible defaults
     */
    public OutputStreamWrapperOptions createOutputStreamOptions();

    /**
     * @return an optional description of the wrapper and potentially of any
     *             user-facing options
     */
    public String getDescription();

    /**
     * An exception to be thrown if wrapping fails. Where required the
     * {@link #unchecked()} method can be used to wrap as an unchecked exception
     * 
     * @author S.Roughley knime@vernalis.com
     */
    public static class OutputStreamWrapException extends Exception {

        @java.io.Serial
        private static final long serialVersionUID = -7062850114049229460L;

        /**
         * No-args constructor
         */
        public OutputStreamWrapException() {

            super();
        }

        /**
         * Full constructor with message and cause
         * 
         * @param message
         *            the message
         * @param cause
         *            the cause
         */
        public OutputStreamWrapException(String message, Throwable cause) {

            super(message, cause);
        }

        /**
         * No-cause constructor
         * 
         * @param message
         *            the error message
         */
        public OutputStreamWrapException(String message) {

            super(message);
        }

        /**
         * Cause-only constructor
         * 
         * @param cause
         *            the causing exception
         */
        public OutputStreamWrapException(Throwable cause) {

            super(cause);
        }

        /**
         * @return an {@link UncheckedOutputStreamWrapException} wrapping this
         *             instance
         */
        public UncheckedOutputStreamWrapException unchecked() {

            return new UncheckedOutputStreamWrapException(getMessage(), this);
        }

    }

    /**
     * An unchecked wrapper for {@link OutputStreamWrapException} modelled on
     * {@link java.io.UncheckedIOException}. Most likely to be created via a
     * call to {@link OutputStreamWrapException#unchecked()}
     * 
     * @author S.Roughley knime@vernalis.com
     */
    public static class UncheckedOutputStreamWrapException
            extends RuntimeException {

        @java.io.Serial
        private static final long serialVersionUID = 1093878583442460757L;

        /**
         * Full constructor
         * 
         * @param message
         *            the error message
         * @param cause
         *            the causing {@link InputStreamWrapException}
         */
        public UncheckedOutputStreamWrapException(String message,
                OutputStreamWrapException cause) {

            super(message, cause);
        }

        /**
         * Cause-only constructor
         * 
         * @param cause
         *            the causing {@link InputStreamWrapException}
         */
        public UncheckedOutputStreamWrapException(
                OutputStreamWrapException cause) {

            super(cause);
        }

        /**
         * Called to read the object from a stream.
         *
         * @param  s
         *                                    the {@code ObjectInputStream} from
         *                                    which data is read
         * @throws IOException
         *                                    if an I/O error occurs
         * @throws ClassNotFoundException
         *                                    if a serialized class cannot be
         *                                    loaded
         * @throws InvalidObjectException
         *                                    if the object is invalid or has a
         *                                    cause that is not
         *                                    an {@code OutputStreamWrapException}
         */
        @java.io.Serial
        private void readObject(ObjectInputStream s)
                throws IOException, ClassNotFoundException {

            s.defaultReadObject();
            Throwable cause = super.getCause();
            if (!(cause instanceof OutputStreamWrapException))
                throw new InvalidObjectException(
                        "Cause must be an OutputStreamWrapException");
        }

    }

}
