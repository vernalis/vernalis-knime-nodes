/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2013, Vernalis (R&D) Ltd
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ------------------------------------------------------------------------
 */
package com.vernalis.nodes.io.pdb.loadlocal;

import org.knime.core.node.NodeView;


/**
 * <code>NodeView</code> for the "LocalPDBLoad" Node.
 * Load a local copy of a PDB files from a column of source filepaths or URLs
 */
public class LocalPDBLoadNodeView2 extends NodeView<LocalPDBLoadNodeModel2> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link LocalPDBLoadNodeModel})
     */
    protected LocalPDBLoadNodeView2(final LocalPDBLoadNodeModel2 nodeModel) {
        super(nodeModel);
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {
        // TODO: generated method stub
    }

}

