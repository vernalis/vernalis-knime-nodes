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

import com.vernalis.testing.AbstractWorkflowTest;
import com.vernalis.testing.NodeTestFlow;

/**
  * Test case to run the test workflow for the 'List Binary Objects Archive Contents' node
  *
  * @author s.roughley
  *
  * @since 1.38.0
  * 
  */
@NodeTestFlow("com.vernalis.knime.misc.blobs.nodes.archive.expand.BlobListArchiveNodeFactory")
public class BlobListArchiveWorkflowTest extends AbstractWorkflowTest{
    
    /**
      * Public constructor for test class
      *
      * @since 1.38.0
      * 
      */
    public BlobListArchiveWorkflowTest() {
        super("src/knime/BlobListArchive");
    }
}
