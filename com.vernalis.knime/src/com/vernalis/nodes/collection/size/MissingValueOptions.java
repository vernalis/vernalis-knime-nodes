/*******************************************************************************
 * Copyright (c) 2024, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.nodes.collection.size;

import java.util.stream.Stream;

import org.knime.core.data.DataCell;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.data.collection.ListDataValue;
import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * Enum of the options for collection counting
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 08-May-2024
 */
enum MissingValueOptions implements ButtonGroupEnumInterface {

    /** All cells are included - the default option */
    All {

        @Override
        long getCount(CollectionDataValue cVal, boolean uniqueValues) {

            if (uniqueValues && cVal instanceof ListDataValue lVal) {
                return lVal.stream().distinct().count();
            }
            // Otherwise we have a set so values are distinct by definition, or
            // we don't care
            return cVal.size();
        }
    },

    /** Only Missing cells are included */
    Missing_Only {

        @Override
        long getCount(CollectionDataValue cVal, boolean uniqueValues) {

            Stream<DataCell> cells = cVal.stream().filter(DataCell::isMissing);
            if (uniqueValues) {
                // All missing cells are considered equal, so we can short-cut
                return cells.findAny().isPresent() ? 1 : 0;
            }
            return cells.count();
        }
    },

    /** Missing cells are excluded */
    Exclude_Missing {

        @Override
        long getCount(CollectionDataValue cVal, boolean uniqueValues) {

            Stream<DataCell> cells = cVal.stream().filter(dc -> !dc.isMissing());
            if (uniqueValues && cVal instanceof ListDataValue) {
                // We need the unique values in a list
                cells = cells.distinct();
            }
            return cells.count();
        }
    };

    @Override
    public String getText() {

        return name().replace('_', ' ');
    }

    @Override
    public String getActionCommand() {

        return name();
    }

    @Override
    public String getToolTip() {

        return null;
    }

    @Override
    public boolean isDefault() {

        return this == getDefault();
    }

    /**
     * @return the default option (All)
     */
    static final MissingValueOptions getDefault() {

        return All;
    }

    /**
     * Method to perform the counting
     * 
     * @param cVal
     *            the collection to count
     * @param uniqueValues
     *            should only unique values be counted
     * @return the count
     */
    abstract long getCount(CollectionDataValue cVal, boolean uniqueValues);
}
