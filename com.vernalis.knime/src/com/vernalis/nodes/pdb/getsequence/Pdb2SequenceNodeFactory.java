/*******************************************************************************
 * Copyright (c) 2013, 2014, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.nodes.pdb.getsequence;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "Pdb2Sequence" Node.
 * Node to extract sequence(s) from PDB Cell column * n
 *
 * @author SDR
 */
public class Pdb2SequenceNodeFactory 
        extends NodeFactory<Pdb2SequenceNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Pdb2SequenceNodeModel createNodeModel() {
        return new Pdb2SequenceNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<Pdb2SequenceNodeModel> createNodeView(final int viewIndex,
            final Pdb2SequenceNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new Pdb2SequenceNodeDialog();
    }

}

