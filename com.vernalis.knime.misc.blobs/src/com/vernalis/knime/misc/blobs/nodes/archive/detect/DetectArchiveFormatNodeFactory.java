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

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;


/**
 * Node factory implementation for the Detect Archive Format node
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class DetectArchiveFormatNodeFactory
        extends NodeFactory<DetectArchiveFormatNodeModel> {

    @Override
    public DetectArchiveFormatNodeModel createNodeModel() {

        return new DetectArchiveFormatNodeModel();
    }

    @Override
    protected int getNrNodeViews() {

        return 0;
    }

    @Override
    public NodeView<DetectArchiveFormatNodeModel> createNodeView(int viewIndex,
            DetectArchiveFormatNodeModel nodeModel) {

        return null;
    }

    @Override
    protected boolean hasDialog() {

        return true;
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {

        return new DetectArchiveFormatNodeDialog();
    }

}
