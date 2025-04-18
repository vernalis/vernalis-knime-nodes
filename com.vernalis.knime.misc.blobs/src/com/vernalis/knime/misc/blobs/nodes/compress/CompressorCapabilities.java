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

/**
 * An enum defining the capabilities of a compression format
 * 
 * @author S.Roughley knime@vernalis.com
 */
public enum CompressorCapabilities {

    /** Compression is supported */
    Compress,

    /** Expansion (decompression) is supported */
    Expand,

    /**
     * Concatenated files are supported - each will be expanded to a new output
     * row
     */
    Concatenation,

    /** File last modified timestamps are supported */
    Timestamp,

    /** Filename headers are supported */
    Filename,

    /** Comments are supported in the headers */
    Comment;




}
