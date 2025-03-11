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
package com.vernalis.knime.misc.blobs.nodes.gzip;
import com.vernalis.testing.AbstractWorkflowTest;
import com.vernalis.testing.NodeTestFlow;

/**
 * Test case to run the test workflow for the GzipBlob node
 *
 * @author s.roughley knime@vernalis.com
 */
@NodeTestFlow("com.vernalis.knime.internal.misc.nodes.binaryobject.gzip.GzipBlobNodeFactory")
@NodeTestFlow("com.vernalis.knime.internal.misc.nodes.binaryobject.gzip.UnGzipBlobNodeFactory")
public class GzipUnGzipBlobWorkflowTest extends AbstractWorkflowTest {

    /** Constructor */
    public GzipUnGzipBlobWorkflowTest() {

        super("src/knime/GzipUnGzipBlob");
    }
}
