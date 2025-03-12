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
package com.vernalis.knime.misc.blobs.nodes.base64;

import com.vernalis.testing.AbstractWorkflowTest;
import com.vernalis.testing.NodeTestFlow;

/**
 * Test case to run the test workflow for the 'Binary Object to Base64-Encoded
 * String' node
 *
 * @author S.Roughley
 * @since 1.38.0
 * 
 */
@NodeTestFlow("com.vernalis.knime.internal.misc.nodes.binaryobject.base64.Blob2Base64NodeFactory")
@NodeTestFlow("com.vernalis.knime.internal.misc.nodes.binaryobject.base64.Base642BlobNodeFactory")
public class Base642Blob2Base64WorkflowTest extends AbstractWorkflowTest {

    /**
     * Public constructor for test class
     *
     * @since 1.38.0
     * 
     */
    public Base642Blob2Base64WorkflowTest() {

        super("src/knime/Base642Blob2Base64");
    }
}
