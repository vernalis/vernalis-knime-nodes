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
package com.vernalis.knime.misc.blobs.nodes.archive.detect;

import static com.vernalis.knime.misc.blobs.nodes.BlobConstants.BINARY_OBJECTS_COLUMN;
import static com.vernalis.knime.misc.blobs.nodes.BlobConstants.BLOB_COLUMN_FILTER;
import static com.vernalis.knime.misc.blobs.nodes.BlobConstants.createBlobColNameModel;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;

/**
 * Node settings pane for the Detect Archive Format node
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class DetectArchiveFormatNodeDialog extends DefaultNodeSettingsPane {

    /**
     * Constructor
     */
    DetectArchiveFormatNodeDialog() {

        addDialogComponent(
                new DialogComponentColumnNameSelection(createBlobColNameModel(),
                        BINARY_OBJECTS_COLUMN, 0, BLOB_COLUMN_FILTER));
    }


}
