/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2012, Vernalis (R&D) Ltd and Enspiral Discovery Limited
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
package com.vernalis.pdbconnector;

import org.knime.core.node.NodeView;

/**
 * PdbConnectorNode view class.
 */
public class PdbConnectorNodeView extends NodeView<PdbConnectorNodeModel> {

	protected PdbConnectorNodeView(final PdbConnectorNodeModel nodeModel) {
        super(nodeModel);

        // TODO instantiate the components of the view here.

    }

    /* (non-Javadoc)
     * @see org.knime.core.node.AbstractNodeView#modelChanged()
     */
    @Override
    protected void modelChanged() {

        // TODO retrieve the new model from your nodemodel and 
        // update the view.
        PdbConnectorNodeModel nodeModel = 
            (PdbConnectorNodeModel)getNodeModel();
        assert nodeModel != null;
        
        // be aware of a possibly not executed nodeModel! The data you retrieve
        // from your nodemodel could be null, emtpy, or invalid in any kind.
        
    }

    /* (non-Javadoc)
     * @see org.knime.core.node.NodeView#onClose()
     */
    @Override
    protected void onClose() {
    
        // TODO things to do when closing the view
    }

    /* (non-Javadoc)
     * @see org.knime.core.node.NodeView#onOpen()
     */
    @Override
    protected void onOpen() {

        // TODO things to do when opening the view
    }

}

