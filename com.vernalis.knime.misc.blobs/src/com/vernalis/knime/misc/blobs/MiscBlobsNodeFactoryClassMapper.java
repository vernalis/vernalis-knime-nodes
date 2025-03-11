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
package com.vernalis.knime.misc.blobs;

import java.util.Collections;
import java.util.Map;

import org.knime.core.node.NodeFactoryClassMapper;
import org.knime.core.node.RegexNodeFactoryClassMapper;
import org.knime.core.util.Pair;

/**
 * {@link NodeFactoryClassMapper} implementation which maps nodes from
 * {@code com.vernalis.nodes.blob} package (in the 'com.vernalis.knime' original
 * plugin) to the
 * relocated NodeFactory implementations in
 * {@code com.vernalis.knime.misc.blobs.nodes}
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.38.0
 */
public class MiscBlobsNodeFactoryClassMapper
        extends RegexNodeFactoryClassMapper {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.knime.core.node.RegexNodeFactoryClassMapper#getRegexRulesInternal()
     */
    @Override
    protected Map<String, Pair<String, String>> getRegexRulesInternal() {

        return Collections.singletonMap(
                "com\\.vernalis\\.nodes\\.blob\\..*",
                new Pair<>(
                        "^com\\.vernalis\\.nodes\\.blob\\.",
                        "com.vernalis.knime.misc.blobs.nodes."));
    }

}
