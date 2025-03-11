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

import org.knime.core.data.DataRow;

/**
 * A marker interface marking a {@link UserOptions} implementation as being
 * suitable for use in an {@link OutputStreamWrapper}
 * 
 * @author S.Roughley knime@vernalis.com
 */
public interface OutputStreamWrapperOptions extends UserOptions {

    /**
     * An interface providing an additional method for
     * {@link OutputStreamWrapperOptions} implementations which need to generate
     * a new individual option for each incoming {@link DataRow} that it is
     * applied to - for example when an {@link OutputStreamWrapper} needs values
     * from other columns in the data row
     * 
     * @author S.Roughley knime@vernalis.com
     */
    public interface RowSpecificOutputStreamWrapperOptions
            extends OutputStreamWrapperOptions {

        /**
         * Method to produce a derived {@link OutputStreamWrapperOptions}
         * implementation for use with the specified row. This may be a direct
         * clone of the calling class with some additional options, or a
         * non-saving/dialoging implementation which simply holds the relevant
         * values.
         * <p>
         * Implementations should not share objects between themselves which may
         * result in inconsistent states if for example 2 or more rows are being
         * processed simultaneously in different threads
         * </p>
         * <p>
         * Classes may choose to implement this interface in preference to the
         * parent interface as a placeholder for future options not currently
         * supported, in which case this method may simply be implemented as:
         * 
         * <pre>
         * {@code @Overide}
         * public OutputStreamWrapperOptions getOptionsForRow(DataRow row) {
         * 
         *     return this;
         * }
         * </pre>
         * 
         * until later options are added
         * </p>
         * 
         * @param row
         *            the incoming data row to which the settings are applied
         * @return the new object
         */
        public OutputStreamWrapperOptions getOptionsForRow(DataRow row);

    }

}
