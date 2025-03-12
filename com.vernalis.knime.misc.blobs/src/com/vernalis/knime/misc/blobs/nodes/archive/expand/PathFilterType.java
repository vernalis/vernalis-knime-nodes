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
package com.vernalis.knime.misc.blobs.nodes.archive.expand;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.knime.core.node.util.ButtonGroupEnumInterface;

import com.vernalis.knime.wildcardfilter.WildcardPattern;

/**
 * Enum for the filter types for paths
 * 
 * @author S.Roughley knime@vernalis.com
 */
enum PathFilterType implements ButtonGroupEnumInterface {

    /** Wild card pattern */
    Wildcard {

        @Override
        Predicate<String> getPathPredicate(String pattern,
                boolean caseSensitive) {

            WildcardPattern wp =
                    caseSensitive ? WildcardPattern.compile(pattern)
                            : WildcardPattern.compile(pattern,
                                    WildcardPattern.CASE_INSENSITIVE);
            return wp.asMatchPredicate();
        }

    },

    /** Regular Expression Pattern */
    Regex {

        @Override
        Predicate<String> getPathPredicate(String pattern,
                boolean caseSensitive) {

            Pattern p = caseSensitive ? Pattern.compile(pattern)
                    : Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            return p.asMatchPredicate();
        }

        @Override
        public String getText() {

            return "Regular Expression";
        }

    };

    @Override
    public String getText() {

        return name();
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
     * @return the default option
     */
    static final PathFilterType getDefault() {

        return Wildcard;
    }

    /**
     * @param  pattern
     *                           the supplied pattern
     * @param  caseSensitive
     *                           should the match be case sensitive
     * @return               a predicate to match the whole test string
     */
    abstract Predicate<String> getPathPredicate(String pattern,
            boolean caseSensitive);

}
