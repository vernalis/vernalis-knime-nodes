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

import org.knime.core.data.StringValue;
import org.knime.core.data.blob.BinaryObjectDataValue;
import org.knime.core.data.date.DateAndTimeValue;
import org.knime.core.data.time.localdatetime.LocalDateTimeValue;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.DataValueColumnFilter;

import com.vernalis.testing.NoTest;

/**
 * A utility class providing constants shared between Binary Objects nodes
 * 
 * @author S.Roughley knime@vernalis.com
 */
@SuppressWarnings("deprecation")
@NoTest
public class BlobConstants {

    /**
     * {@link ColumnFilter} for {@link BinaryObjectDataValue} column types
     */
    public static final ColumnFilter BLOB_COLUMN_FILTER =
            new DataValueColumnFilter(BinaryObjectDataValue.class);
    /**
     * {@link ColumnFilter} for Date Time (legacy or new Local Date-time)
     */
    static final ColumnFilter DATE_COLUMN_FILTER = new DataValueColumnFilter(
            DateAndTimeValue.class, LocalDateTimeValue.class);
    /**
     * {@link ColumnFilter} for Path columns (String)
     */
    public static final ColumnFilter PATH_COLUMN_FILTER =
            new DataValueColumnFilter(StringValue.class);

    /** Model key and dialog label for a binary objects column chooser */
    public static final String BINARY_OBJECTS_COLUMN = "Binary Objects Column";

    /**
     * @return settings model for the 'Binary Objects Column' option
     */
    public static final SettingsModelString createBlobColNameModel() {

        return new SettingsModelString(BINARY_OBJECTS_COLUMN, null);
    }

    private BlobConstants() {

        // Do not instantiate - utility class
        throw new UnsupportedOperationException(
                "Utility class - may not be instantiated!");
    }

}
