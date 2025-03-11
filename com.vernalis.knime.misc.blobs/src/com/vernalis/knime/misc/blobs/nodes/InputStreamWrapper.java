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
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;

/**
 * An interface defining an class which wraps an {@link InputStream} in a new
 * stream. Implementations are not streams themselves, but rather provide
 * methods to perform the wrapping according to certain settings defined in an
 * {@link InputStreamWrapperOptions}, and to provide a sensible default instance
 * of those options
 * 
 * @author S.Roughley knime@vernalis.com
 */
public interface InputStreamWrapper {

    /**
     * Method to perform the stream wrapping. Implementations should define
     * their behaviour if {@code null} arguments are supplied, and should
     * ensure, where appropriate that the {@link InputStreamWrapperOptions}
     * argument is of an appropriate type. The return type of this method should
     * almost certainly be narrowed
     * 
     * @param in
     *            the stream to wrap
     * @param opts
     *            the associated options
     * @return the wrapped stream
     * @throws IOException
     *             if the wrapping process threw in IOException
     * @throws InputStreamWrapException
     *             for other exceptions thrown during the wrapping process
     */
    public InputStream wrapInputStream(InputStream in,
            InputStreamWrapperOptions opts)
            throws IOException, InputStreamWrapException;

    /**
     * @return a new instance of the appropriate wrapper options for the
     *             implementation providing sensible defaults
     */
    public InputStreamWrapperOptions createInputStreamOptions();

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
    public static class InputStreamWrapException extends Exception {

        @java.io.Serial
        private static final long serialVersionUID = -3885192705788170658L;

        /**
         * No-args constructor
         */
        public InputStreamWrapException() {

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
        public InputStreamWrapException(String message, Throwable cause) {

            super(message, cause);
        }

        /**
         * No-cause constructor
         * 
         * @param message
         *            the error message
         */
        public InputStreamWrapException(String message) {

            super(message);
        }

        /**
         * Cause-only constructor
         * 
         * @param cause
         *            the causing exception
         */
        public InputStreamWrapException(Throwable cause) {

            super(cause);
        }

        /**
         * @return an {@link UncheckedInputStreamWrapException} wrapping this
         *             instance
         */
        public UncheckedInputStreamWrapException unchecked() {

            return new UncheckedInputStreamWrapException(getMessage(), this);
        }

    }

    /**
     * An unchecked wrapper for {@link InputStreamWrapException} modelled on
     * {@link java.io.UncheckedIOException}. Most likely to be created via a
     * call to {@link InputStreamWrapException#unchecked()}
     * 
     * @author S.Roughley knime@vernalis.com
     */
    public static class UncheckedInputStreamWrapException
            extends RuntimeException {

        @java.io.Serial
        private static final long serialVersionUID = -8912361889488223264L;

        /**
         * Full constructor
         * 
         * @param message
         *            the error message
         * @param cause
         *            the causing {@link InputStreamWrapException}
         */
        public UncheckedInputStreamWrapException(String message,
                InputStreamWrapException cause) {

            super(message, cause);
        }

        /**
         * Cause-only constructor
         * 
         * @param cause
         *            the causing {@link InputStreamWrapException}
         */
        public UncheckedInputStreamWrapException(
                InputStreamWrapException cause) {

            super(cause);
        }

        /**
         * Called to read the object from a stream.
         *
         * @param s
         *            the {@code ObjectInputStream} from
         *            which data is read
         * @throws IOException
         *             if an I/O error occurs
         * @throws ClassNotFoundException
         *             if a serialized class cannot be
         *             loaded
         * @throws InvalidObjectException
         *             if the object is invalid or has a
         *             cause that is not
         *             an {@code InputStreamWrapException}
         */
        @java.io.Serial
        private void readObject(ObjectInputStream s)
                throws IOException, ClassNotFoundException {

            s.defaultReadObject();
            Throwable cause = super.getCause();
            if (!(cause instanceof InputStreamWrapException))
                throw new InvalidObjectException(
                        "Cause must be an InputStreamWrapException");
        }

    }

}
